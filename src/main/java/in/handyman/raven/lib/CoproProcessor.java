package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lambda.doa.audit.StatementExecutionAudit;
import in.handyman.raven.lib.interfaces.coproprocessor.InboundBatchDataConsumer;
import in.handyman.raven.util.CommonQueryUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CoproProcessor<I, O extends CoproProcessor.Entity> {

    private final BlockingQueue<I> queue;
    private ExecutorService executorService;

    private final Class<O> outputTargetClass;
    private final Class<I> inputTargetClass;
    private final String jdbiResourceName;

    private final Logger logger;

    private final I stoppingSeed;

    private final List<URL> nodes;
    private final Integer nodeSize;

    private final AtomicInteger nodeCount = new AtomicInteger();

    private final ActionExecutionAudit actionExecutionAudit;

    public CoproProcessor(final BlockingQueue<I> queue, final Class<O> outputTargetClass,
                          final Class<I> inputTargetClass, final String jdbiResourceName, final Logger logger,
                          final I stoppingSeed, final List<URL> coproNodes,
                          final ActionExecutionAudit actionExecutionAudit) {
        this.queue = queue;
        this.inputTargetClass = inputTargetClass;
        this.stoppingSeed = stoppingSeed;
        this.nodes = coproNodes;
        this.executorService = Executors.newWorkStealingPool();
        this.outputTargetClass = outputTargetClass;
        this.jdbiResourceName = jdbiResourceName;
        this.logger = logger;
        this.actionExecutionAudit = actionExecutionAudit;
        this.nodeSize = coproNodes.size();
        final LocalDateTime startTime = LocalDateTime.now();
        if (nodeSize > 0) {
            this.logger.info("Copro processor created for copro coproNodes {}", nodeSize);
        } else {
            this.logger.info("Failed to create Copro processor due to empty copro coproNodes");
            HandymanException handymanException = new HandymanException("Failed to create Copro processor due to empty copro coproNodes");
            HandymanException.insertException("Failed to create Copro processor due to empty copro coproNodes", handymanException, actionExecutionAudit);
        }
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor created for " + actionExecutionAudit.getActionName())
                .build();
        addAudit(audit, startTime);
    }

    private void addAudit(final StatementExecutionAudit audit, final LocalDateTime startTime) {
        audit.setCreatedBy(actionExecutionAudit.getCreatedBy());
        audit.setLastModifiedBy(actionExecutionAudit.getLastModifiedBy());
        audit.setCreatedDate(startTime);
        audit.setLastModifiedDate(LocalDateTime.now());
        HandymanActorSystemAccess.insert(audit);
    }

    public void startProducer(final String sqlQuery, final Integer readBatchSize) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        final LocalDateTime startTime = LocalDateTime.now();
        final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(sqlQuery);
        //TODO SIMPLE FOR EACH , REMOVE STREAM, SEPARATE CLASS
        formattedQuery.forEach(sql -> jdbi.useTransaction(handle -> handle.createQuery(sql).mapToBean(inputTargetClass).useStream(stream -> {
            final AtomicInteger counter = new AtomicInteger();
            final Map<Integer, List<I>> partitions = stream.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / readBatchSize));
            logger.info("Total no of rows created {}", counter.get());
            //TODO REMOVE EXECUTOR SERVICE
            executorService.submit(() -> {
                try {
                    partitions.forEach((integer, ts) -> {
                        queue.addAll(ts);
                        insertRowsReadIntoStatementAudit(ts, startTime);
                        logger.info("Partition {} added to the queue", integer);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            logger.error("Error at Producer sleep", e);
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("Error at Producer sleep", handymanException, actionExecutionAudit);
                        }
                    });
                    logger.info("Total Partition added to the queue: {} ", partitions.size());
                    insertCompletionIntoStatementAudit(startTime);
                } finally {
                    queue.add(stoppingSeed);
                    logger.info("Added stopping seed to the queue");
                }
            });
        })));
    }

    private void insertRowsReadIntoStatementAudit(List<I> ts, LocalDateTime startTime) {
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor producer for " + actionExecutionAudit.getActionName())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .rowsRead(ts.size())
                .build();
        addAudit(audit, startTime);
    }

    private void insertCompletionIntoStatementAudit(LocalDateTime startTime) {
        final StatementExecutionAudit audit = StatementExecutionAudit.builder()
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .statementContent("CoproProcessor producer completed " + actionExecutionAudit.getActionName())
                .timeTaken((double) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now()))
                .build();
        addAudit(audit, startTime);
    }


    public void startConsumer(final String insertSql, final Integer consumerCount, final Integer writeBatchSize,
                                    final ConsumerProcess<I, O> callable) {
        String route = actionExecutionAudit.getContext().getOrDefault("copro.processor.consumer.route.type", "LEGACY");
        if ("KAFKA_ASYNC".equalsIgnoreCase(route)) {
            startKafkaAsyncPublisher(callable);
        } else if ("MODERN".equalsIgnoreCase(route)) {
            startConsumerModern(insertSql, consumerCount, writeBatchSize, callable);
        } else {
            startConsumerLegacy(insertSql, consumerCount, writeBatchSize, callable);
        }
    }

    private void startConsumerLegacy(String insertSql, Integer consumerCount, Integer writeBatchSize, ConsumerProcess<I, O> callable) {
        final LocalDateTime startTime = LocalDateTime.now();
        final Predicate<I> tPredicate = t -> !Objects.equals(t, stoppingSeed);

        int finalConsumerCount = Math.min(consumerCount, queue.size());
        final CountDownLatch countDownLatch = new CountDownLatch(finalConsumerCount);
        if (actionExecutionAudit.getContext().getOrDefault("copro.processor.thread.creator", "WORK_STEALING").equalsIgnoreCase("FIXED_THREAD")) {
            executorService = Executors.newFixedThreadPool(finalConsumerCount);
            logger.info("Copro processor created with fixed thread pool of size {}", finalConsumerCount);
        } else if (actionExecutionAudit.getContext().getOrDefault("copro.processor.thread.creator", "WORK_STEALING").equalsIgnoreCase("VIRTUAL_THREAD")) {
            executorService = Executors.newVirtualThreadPerTaskExecutor();
            logger.info("Copro processor created with Virtual Thread Per Task Executor");
        } else {
            executorService = Executors.newWorkStealingPool();
            logger.info("Copro processor created with work stealing pool");
        }
        for (int consumer = 0; consumer < finalConsumerCount; consumer++) {
            executorService.submit(new InboundBatchDataConsumer<>(insertSql, writeBatchSize, callable, tPredicate,
                    startTime, countDownLatch, queue, nodeCount, nodeSize, actionExecutionAudit, nodes, jdbiResourceName, logger));

            logger.info("Consumer {} submitted the process", consumer);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Consumer completed the process and persisted {} rows", nodeCount.get(), e);
        } finally {
            logger.info("Shutting down executor service");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private void startConsumerModern(String insertSql, Integer consumerCount, Integer writeBatchSize, ConsumerProcess<I, O> callable) {
        final LocalDateTime startTime = LocalDateTime.now();
        final Predicate<I> tPredicate = t -> !Objects.equals(t, stoppingSeed);
        int queueSize = queue.size();
        int finalConsumerCount = consumerCount;
        logger.info("Queue size is {} and configured consumer count is {}", queueSize, consumerCount);
        logger.info("Available processors: {}", Runtime.getRuntime().availableProcessors());
        logger.info("Initial consumer count: {}", finalConsumerCount);


        final CountDownLatch countDownLatch = new CountDownLatch(finalConsumerCount);
        AtomicInteger threadNumber = new AtomicInteger(1);

        ThreadFactory namedThreadFactory = r -> {
            Thread t = new Thread(r);
            t.setName("copro-processor-consumer-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            return t;
        };

        if (actionExecutionAudit.getContext().getOrDefault("copro.processor.thread.creator", "FIXED_THREAD").equalsIgnoreCase("VIRTUAL_THREAD")) {
            executorService = Executors.newVirtualThreadPerTaskExecutor();
            logger.info("Copro processor created with Virtual consumer thread pool of size {}", finalConsumerCount);
        } else {
            executorService = new ThreadPoolExecutor(
                    finalConsumerCount,               // core
                    finalConsumerCount,               // max
                    120L, TimeUnit.SECONDS,       // keepAlive
                    new LinkedBlockingQueue<>(),
                    namedThreadFactory,
                    new ThreadPoolExecutor.CallerRunsPolicy() // if pool full, run in caller
            );

            logger.info("Copro processor created with fixed consumer thread pool of size {}", finalConsumerCount);
        }

        for (int i = 0; i < consumerCount; i++) {
            executorService.submit(new InboundBatchDataConsumer<>(
                    insertSql, writeBatchSize, callable, tPredicate,
                    startTime, countDownLatch, queue, nodeCount, nodeSize,
                    actionExecutionAudit, nodes, jdbiResourceName, logger));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for consumers to finish", e);
        } finally {
            logger.info("Shutting down consumer executor service");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    public interface ConsumerProcess<I, O extends Entity> {
        List<O> process(final URL endpoint, final I entity) throws Exception;

        /**
         * Build the exact REST API JSON payload for Kafka async publishing.
         * Each ConsumerProcess implementation serializes the same request body
         * that its {@code process()} method would send to the copro endpoint.
         */
        default String buildJsonForKafka(final I entity) throws Exception {
            throw new UnsupportedOperationException(
                    "buildJsonForKafka not implemented for " + getClass().getSimpleName());
        }

        default String getOutputTable() { return null; }

        default String getRequestType() { return null; }

        default String getKafkaTopic() { return null; }
    }

    public interface Entity {
        List<Object> getRowData();

        String getStatus();
    }


    /**
     * Async publish path: drains the producer queue (filled by startProducer()) and
     * publishes
     * each input entity as a Kafka message carrying the routing metadata needed for
     * the response
     * consumer to write results to the correct output table and trigger the
     * continuation script.
     * Activated when copro.processor.consumer.route.type = KAFKA_ASYNC
     */
    private void startKafkaAsyncPublisher(ConsumerProcess<I, O> callable) {
        final Map<String, String> ctx = actionExecutionAudit.getContext();

        // Prefer dynamic values from ConsumerProcess; fall back to context variables
        final String requestType = callable.getRequestType() != null
                ? callable.getRequestType()
                : ctx.get("copro.processor.kafka.request.type");
        // Prefer callable-provided topic, fall back to context indirection
        final String topic;
        if (callable.getKafkaTopic() != null) {
            topic = callable.getKafkaTopic();
        } else {
            String topicKey = ctx.get("copro.processor.kafka.topic.key");
            topic = topicKey != null ? ctx.get(topicKey) : null;
        }
        final String batchId = ctx.get("batch_id");
        final String outputTable = callable.getOutputTable() != null
                ? callable.getOutputTable()
                : ctx.getOrDefault("copro.processor.kafka.output.table", "");

        if (requestType == null || requestType.isBlank()) {
            throw new HandymanException(
                    "copro.processor.kafka.request.type must be set in context for KAFKA_ASYNC route",
                    null, actionExecutionAudit);
        }
        if (topic == null || topic.isBlank()) {
            throw new HandymanException("copro.processor.kafka.topic.key must resolve to a valid topic name in context",
                    null, actionExecutionAudit);
        }

        final List<I> items = new ArrayList<>();
        queue.drainTo(items);
        items.remove(stoppingSeed);

        if (items.isEmpty()) {
            logger.info("KAFKA_ASYNC: no items to publish for batch={} type={} — marking module complete", batchId, requestType);

            final Jdbi emptyJdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
            emptyJdbi.useTransaction(handle -> {
                int inserted = handle.execute(
                        "INSERT INTO audit.inference_queue_active " +
                                "(batch_id, root_pipeline_id, request_type, total_requests, completed_requests, failed_requests, status) " +
                                "VALUES (?, ?, ?, 0, 0, 0, 'COMPLETED') ON CONFLICT (batch_id, request_type) DO NOTHING",
                        batchId, actionExecutionAudit.getRootPipelineId(), requestType);

                if (inserted > 0) {
                    handle.execute(
                            "UPDATE audit.inference_batch_barrier " +
                                    "SET completed_modules = completed_modules + 1, updated_at = NOW() " +
                                    "WHERE batch_id = ? AND status = 'PROCESSING'",
                            batchId);
                }
            });

            return;
        }

        logger.info("KAFKA_ASYNC: publishing {} items for batch={} type={} → topic={}", items.size(), batchId, requestType, topic);

        final ObjectMapper mapper = new ObjectMapper();
        final ConcurrentLinkedQueue<I> failedItems = new ConcurrentLinkedQueue<>();

        final Map<String, Object> kafkaProducerProps = buildAsyncKafkaProps(ctx);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProducerProps)) {
            for (I item : items) {
                try {
                    // Use buildJsonForKafka() for exact REST API payload
                    String payload = callable.buildJsonForKafka(item);
                    String correlationId = java.util.UUID.randomUUID().toString();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> entityFields = mapper.convertValue(item, Map.class);
                    String partKey = String.valueOf(entityFields.getOrDefault("originId",
                            entityFields.getOrDefault("origin_id", "")));
                    String messageKey = partKey.isBlank() ? null : partKey;

                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, messageKey, payload);
                    // Routing metadata in Kafka headers, not in the payload
                    record.headers().add("X-Route-OutputTable", outputTable.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-RequestType", requestType.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-BatchId", batchId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-CorrelationId", correlationId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-RootPipelineId",
                            String.valueOf(actionExecutionAudit.getRootPipelineId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    // Additional tracking headers for full request traceability
                    record.headers().add("X-Route-OriginId", messageKey != null
                            ? messageKey.getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0]);
                    String pageNoVal = String.valueOf(entityFields.getOrDefault("pageNo",
                            entityFields.getOrDefault("page_no", "1")));
                    record.headers().add("X-Route-PageNo", pageNoVal.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-ProcessId",
                            ctx.getOrDefault("init_process_id.process_id", "").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-TenantId",
                            ctx.getOrDefault("tenant_id", "").getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    record.headers().add("X-Route-GroupId",
                            ctx.getOrDefault("group_id", "").getBytes(java.nio.charset.StandardCharsets.UTF_8));

                    producer.send(record, (meta, ex) -> {
                        if (ex != null) {
                            logger.error("KAFKA_ASYNC send failed for batch={} type={}: {}", batchId, requestType, ex.getMessage());
                            failedItems.add(item);
                        }
                    });
                } catch (Exception e) {
                    logger.error("KAFKA_ASYNC: failed to serialize item for batch={} type={}", batchId, requestType, e);
                    failedItems.add(item);
                }
            }
            producer.flush();
        } catch (Exception e) {
            logger.error("KAFKA_ASYNC: producer error for batch={} type={}", batchId, requestType, e);
            HandymanException.insertException("KAFKA_ASYNC publish failed", new HandymanException(e), actionExecutionAudit);
        }

        if (!failedItems.isEmpty()) {
            handleAsyncPublishFailures(failedItems, batchId, requestType);
        }

        persistAsyncWaitState(batchId, requestType, items.size() - failedItems.size(), actionExecutionAudit.getContext());

        actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.WAITING_FOR_ASYNC.getId());
        HandymanActorSystemAccess.update(actionExecutionAudit);
        logger.info("KAFKA_ASYNC: completed. published={} failed={} batch={} type={}", items.size() - failedItems.size(), failedItems.size(), batchId, requestType);
    }

    /**
     * Mark failed items as DLQ and decrement total_requests so aggregator barrier
     * can still fire.
     */
    private void handleAsyncPublishFailures(ConcurrentLinkedQueue<I> failedItems,
            String batchId, String requestType) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        final ObjectMapper mapper = new ObjectMapper();
        jdbi.useTransaction(handle -> {
            for (I item : failedItems) {
                try {
                    Map<?, ?> fields = mapper.convertValue(item, Map.class);
                    Object originIdObj = fields.get("origin_id");
                    if (originIdObj == null)
                        originIdObj = fields.get("originId");
                    String originId = String.valueOf(originIdObj != null ? originIdObj : "");
                    Object pageNoObj = fields.get("page_no");
                    if (pageNoObj == null)
                        pageNoObj = fields.get("pageNo");
                    String pageNo = String.valueOf(pageNoObj != null ? pageNoObj : "1");
                    handle.execute(
                            "UPDATE audit.inference_queue_items SET status='DLQ', updated_at=NOW() " +
                                    "WHERE batch_id=? AND origin_id=? AND page_no=? AND request_type=?",
                            batchId, originId, pageNo, requestType);
                } catch (Exception e) {
                    logger.warn("Could not mark item as DLQ for batch={} type={}: {}",
                            batchId, requestType, e.getMessage());
                }
            }
            handle.execute(
                    "UPDATE audit.inference_queue_active " +
                            "SET total_requests = total_requests - ?, failed_requests = failed_requests + ?, updated_at=NOW() "
                            +
                            "WHERE batch_id=? AND request_type=?",
                    failedItems.size(), failedItems.size(), batchId, requestType);
        });
        logger.error("KAFKA_ASYNC: {} items moved to DLQ for batch={} type={}", failedItems.size(), batchId, requestType);
    }

    /**
     * Persist pipeline_wait_state so PipelineAggregatorService can trigger the
     * continuation script.
     */
    private void persistAsyncWaitState(String batchId, String requestType,
            int publishedCount, Map<String, String> ctx) {
        String nextScript = ctx.getOrDefault("next_pipeline_script", "");
        if (nextScript.isBlank()) {
            logger.warn("KAFKA_ASYNC: next_pipeline_script not set in context for batch={} type={}. " + "Continuation will not trigger automatically.", batchId, requestType);
        }
        String contextJson = "{}";
        try {
            contextJson = new ObjectMapper().writeValueAsString(ctx);
        } catch (Exception ignored) {
        }

        final String contextFinal = contextJson;
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        jdbi.useHandle(handle -> handle.execute(
                "INSERT INTO audit.pipeline_wait_state " +
                        "(root_pipeline_id, batch_id, request_type, next_pipeline_script, total_requests, context) " +
                        "VALUES (?, ?, ?, ?, ?, ?::jsonb) " +
                        "ON CONFLICT (root_pipeline_id, batch_id, request_type) DO NOTHING",
                actionExecutionAudit.getRootPipelineId(), batchId, requestType,
                nextScript, publishedCount, contextFinal));
    }

    /**
     * Build minimal Kafka producer properties for async publish (no auth by default).
     */
    private Map<String, Object> buildAsyncKafkaProps(Map<String, String> ctx) {
        String bootstrapServers = ctx.getOrDefault("copro.processor.kafka.bootstrap.servers",
                ctx.getOrDefault("vulcan.consumer.kafka.bootstrap.servers", "localhost:9092"));
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.ACKS_CONFIG,
                ctx.getOrDefault("copro.processor.kafka.producer.acks", "all"));
        props.put(ProducerConfig.RETRIES_CONFIG,
                Integer.parseInt(ctx.getOrDefault("copro.processor.kafka.producer.retries", "3")));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                ctx.getOrDefault("copro.processor.kafka.request.timeout.ms", "30000"));
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                ctx.getOrDefault("copro.processor.kafka.delivery.timeout.ms", "120000"));
        props.put(ProducerConfig.LINGER_MS_CONFIG,
                ctx.getOrDefault("copro.processor.kafka.producer.linger.ms", "100"));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,
                ctx.getOrDefault("copro.processor.kafka.producer.compression.type", "lz4"));
        return props;
    }
}