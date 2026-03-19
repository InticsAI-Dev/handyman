package in.handyman.raven.lib.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lib.CoproProcessor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CoproProcessorAsyncHandler<I, O extends CoproProcessor.Entity> {

    private final BlockingQueue<I> queue;
    private final I stoppingSeed;
    private final ActionExecutionAudit actionExecutionAudit;
    private final String jdbiResourceName;
    private final Logger logger;

    public CoproProcessorAsyncHandler(BlockingQueue<I> queue, I stoppingSeed, ActionExecutionAudit actionExecutionAudit,
                                      String jdbiResourceName, Logger logger) {
        this.queue = queue;
        this.stoppingSeed = stoppingSeed;
        this.actionExecutionAudit = actionExecutionAudit;
        this.jdbiResourceName = jdbiResourceName;
        this.logger = logger;
    }

    /**
     * Async publish path: drains the producer queue (filled by startProducer()) and
     * publishes each input entity as a Kafka message carrying the routing metadata
     * needed for the response consumer to write results to the correct output table
     * and trigger the continuation script.
     */
    public void startKafkaAsyncPublisher(CoproProcessor.ConsumerProcess<I, O> callable) {
        final Map<String, String> context = actionExecutionAudit.getContext();

        final String requestType = callable.getRequestType() != null ? callable.getRequestType() : context.get("copro.processor.kafka.request.type");
        final String topic;
        if (callable.getKafkaTopic() != null) {
            topic = callable.getKafkaTopic();
        } else {
            topic = context.get("copro.processor.kafka.topic");
        }
        final String batchId = context.get("batch_id");
        final String outputTable = callable.getOutputTable() != null ? callable.getOutputTable() : context.getOrDefault("copro.processor.kafka.output.table", "");

        if (requestType == null || requestType.isBlank()) {
            throw new HandymanException("copro.processor.kafka.request.type must be set in context for KAFKA_ASYNC route", null, actionExecutionAudit);
        }
        if (topic == null || topic.isBlank()) {
            throw new HandymanException("copro.processor.kafka.topic.key must resolve to a valid topic name in context", null, actionExecutionAudit);
        }

        final List<I> items = new ArrayList<>();
        queue.drainTo(items);
        items.remove(stoppingSeed);

        if (items.isEmpty()) {
            logger.info("KAFKA_ASYNC: no items to publish for batch={} type={} — marking module complete", batchId, requestType);
            markModuleCompleteForEmptyItems(batchId, requestType);
            return;
        }

        logger.info("KAFKA_ASYNC: publishing {} items for batch={} type={} → topic={}", items.size(), batchId, requestType, topic);

        final ConcurrentLinkedQueue<I> failedItems = publishItems(items, callable, topic, outputTable, batchId, requestType, context);

        registerInferenceQueueActive(batchId, requestType, items.size());

        if (!failedItems.isEmpty()) {
            handleAsyncPublishFailures(failedItems, batchId, requestType);
        }

        persistAsyncWaitState(batchId, requestType, items.size() - failedItems.size(), context);
        updateActionToWaitingForAsync();
        logger.info("KAFKA_ASYNC: completed. published={} failed={} batch={} type={}", items.size() - failedItems.size(), failedItems.size(), batchId, requestType);
    }

    protected void markModuleCompleteForEmptyItems(String batchId, String requestType) {
        final Jdbi emptyJdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        emptyJdbi.useTransaction(handle -> {
            int inserted = handle.execute(
                    "INSERT INTO kafka_audit.inference_queue_active " +
                            "(batch_id, root_pipeline_id, request_type, total_requests, completed_requests, failed_requests, status) " +
                            "VALUES (?, ?, ?, 0, 0, 0, 'COMPLETED') ON CONFLICT (batch_id, request_type) DO NOTHING",
                    batchId, actionExecutionAudit.getRootPipelineId(), requestType);

            if (inserted > 0) {
                handle.execute(
                        "UPDATE kafka_audit.inference_batch_barrier " +
                                "SET completed_modules = completed_modules + 1, updated_at = NOW() " +
                                "WHERE batch_id = ? AND status = 'PROCESSING'",
                        batchId);
            }
        });
    }

    /**
     * Register the module in inference_queue_active with total_requests = totalItems.
     * Must be called BEFORE handleAsyncPublishFailures() so the row exists for its UPDATE.
     */
    protected void registerInferenceQueueActive(String batchId, String requestType, int totalItems) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        jdbi.useHandle(handle -> handle.execute(
                "INSERT INTO kafka_audit.inference_queue_active " +
                        "(batch_id, root_pipeline_id, request_type, total_requests, completed_requests, failed_requests, status) " +
                        "VALUES (?, ?, ?, ?, 0, 0, 'PROCESSING') " +
                        "ON CONFLICT (batch_id, request_type) DO NOTHING",
                batchId, actionExecutionAudit.getRootPipelineId(), requestType, totalItems));
    }

    protected ConcurrentLinkedQueue<I> publishItems(List<I> items,
                                                    CoproProcessor.ConsumerProcess<I, O> callable,
                                                    String topic,
                                                    String outputTable,
                                                    String batchId,
                                                    String requestType,
                                                    Map<String, String> context) {
        final ConcurrentLinkedQueue<I> failedItems = new ConcurrentLinkedQueue<>();
        final Map<String, Object> kafkaProducerProps = buildAsyncKafkaProps(context);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProducerProps)) {
            for (I item : items) {
                try {
                    String payload = callable.buildJsonForKafka(item);
                    String correlationId = java.util.UUID.randomUUID().toString();
                    ProducerRecord<String, String> recordData = buildProducerRecord(
                            topic, payload, outputTable, requestType, batchId, context, item, correlationId);

                    producer.send(recordData, (meta, ex) -> {
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

        return failedItems;
    }

    protected ProducerRecord<String, String> buildProducerRecord(String topic,
                                                                 String payload,
                                                                 String outputTable,
                                                                 String requestType,
                                                                 String batchId,
                                                                 Map<String, String> context,
                                                                 I item,
                                                                 String correlationId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> entityFields = new ObjectMapper().convertValue(item, Map.class);
        String partKey = String.valueOf(entityFields.getOrDefault("originId", entityFields.getOrDefault("origin_id", "")));
        String messageKey = partKey.isBlank() ? null : partKey;

        logger.info("Posting to Kafka topic={} with key={} batch={} type={}", topic, messageKey, batchId, requestType);
        ProducerRecord<String, String> recordData = new ProducerRecord<>(topic, messageKey, payload);
        recordData.headers().add("X-Route-OutputTable", outputTable.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-RequestType", requestType.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-BatchId", batchId.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-CorrelationId", correlationId.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-RootPipelineId", String.valueOf(actionExecutionAudit.getRootPipelineId()).getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-OriginId", messageKey != null ? messageKey.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        String pageNoVal = String.valueOf(entityFields.getOrDefault("pageNo", entityFields.getOrDefault("page_no",
                        entityFields.getOrDefault("paperNo", entityFields.getOrDefault("paper_no", "0")))));
        recordData.headers().add("X-Route-PageNo", pageNoVal.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-ProcessId", context.getOrDefault("init_process_id.process_id", "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-TenantId", context.getOrDefault("tenant_id", "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-GroupId", context.getOrDefault("group_id", "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add("X-Route-CreatedOn", String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));

        addHeaderIfPresent(recordData, entityFields, "X-Route-TemplateId", "templateId");
        addHeaderIfPresent(recordData, entityFields, "X-Route-TemplateName", "templateName");
        addHeaderIfPresent(recordData, entityFields, "X-Route-FilePath", "filePath");
        addHeaderIfPresent(recordData, entityFields, "X-Route-PromptType", "promptType");
        addHeaderIfPresent(recordData, entityFields, "X-Route-ModelName", "modelName");

        addHeaderIfPresent(recordData, entityFields, "X-Route-Process", "process");
        addHeaderIfPresent(recordData, entityFields, "X-Route-ModelRegistry", "modelRegistry");
        addHeaderIfPresent(recordData, entityFields, "X-Route-ApiName", "apiName");
        addHeaderIfPresent(recordData, entityFields, "X-Route-Category", "category");
        addHeaderIfPresent(recordData, entityFields, "X-Route-SorContainerId", "sorContainerId");
        addHeaderIfPresent(recordData, entityFields, "X-Route-SorContainerName", "sorContainerName");
        addHeaderIfPresent(recordData, entityFields, "X-Route-InputFilePath", "inputFilePath");

        addHeaderIfPresent(recordData, entityFields, "X-Route-PostProcess", "postProcess");
        addHeaderIfPresent(recordData, entityFields, "X-Route-PostProcessClassName", "postProcessClassName");
        addHeaderIfPresent(recordData, entityFields, "X-Route-PostProcessClass", "postProcessClass");

        return recordData;
    }

    private void addHeaderIfPresent(ProducerRecord<String, String> record, Map<String, Object> fields, String headerKey, String fieldKey) {
        Object val = fields.get(fieldKey);
        if (val != null) {
            record.headers().add(headerKey, String.valueOf(val).getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void updateActionToWaitingForAsync() {
        actionExecutionAudit.updateExecutionStatusId(ExecutionStatus.WAITING_FOR_ASYNC.getId());
        HandymanActorSystemAccess.update(actionExecutionAudit);
    }

    /**
     * Mark failed items as DLQ and decrement total_requests so aggregator barrier can still fire.
     */
    protected void handleAsyncPublishFailures(ConcurrentLinkedQueue<I> failedItems, String batchId, String requestType) {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        final ObjectMapper mapper = new ObjectMapper();

        jdbi.useTransaction(handle -> {
            for (I item : failedItems) {
                try {
                    Map<?, ?> fields = mapper.convertValue(item, Map.class);
                    Object originIdObj = fields.get("origin_id");
                    if (originIdObj == null) {
                        originIdObj = fields.get("originId");
                    }
                    String originId = String.valueOf(originIdObj != null ? originIdObj : "");

                    Object pageNoObj = fields.get("page_no");
                    if (pageNoObj == null) {
                        pageNoObj = fields.get("pageNo");
                    }
                    String pageNo = String.valueOf(pageNoObj != null ? pageNoObj : "1");
                    handle.execute(
                            "UPDATE kafka_audit.inference_queue_items SET status='DLQ', updated_at=NOW() " +
                                    "WHERE batch_id=? AND origin_id=? AND page_no=? AND request_type=?",
                            batchId, originId, pageNo, requestType);
                } catch (Exception e) {
                    logger.warn("Could not mark item as DLQ for batch={} type={}: {}", batchId, requestType, e.getMessage());
                }
            }

            handle.execute(
                    "UPDATE kafka_audit.inference_queue_active " +
                            "SET total_requests = total_requests - ?, failed_requests = failed_requests + ?, updated_at=NOW() " +
                            "WHERE batch_id=? AND request_type=?",
                    failedItems.size(), failedItems.size(), batchId, requestType);
        });
        logger.error("KAFKA_ASYNC: {} items moved to DLQ for batch={} type={}", failedItems.size(), batchId, requestType);
    }

    /**
     * Persist pipeline_wait_state so PipelineAggregatorService can trigger the continuation script.
     */
    protected void persistAsyncWaitState(String batchId, String requestType, int publishedCount, Map<String, String> context) {
        String nextScript = context.getOrDefault("continuation_pipeline_script", "");
        if (nextScript.isBlank()) {
            logger.warn("KAFKA_ASYNC: continuation_pipeline_script not set in context for batch={} type={}. Continuation will not trigger automatically.",
                    batchId, requestType);
        }

        String contextJson = "{}";
        try {
            contextJson = new ObjectMapper().writeValueAsString(context);
        } catch (Exception ignored) {
        }

        final String contextFinal = contextJson;
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        jdbi.useHandle(handle -> handle.execute(
                "INSERT INTO kafka_audit.pipeline_wait_state " +
                        "(root_pipeline_id, batch_id, request_type, next_pipeline_script, total_requests, context) " +
                        "VALUES (?, ?, ?, ?, ?, ?::jsonb) " +
                        "ON CONFLICT (root_pipeline_id, batch_id, request_type) DO NOTHING",
                actionExecutionAudit.getRootPipelineId(), batchId, requestType, nextScript, publishedCount, contextFinal));
    }

    /**
     * Build minimal Kafka producer properties for async publish.
     */
    protected Map<String, Object> buildAsyncKafkaProps(Map<String, String> context) {
        String bootstrapServers = context.getOrDefault("copro.processor.kafka.bootstrap.servers", "localhost:9092");

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, context.getOrDefault("copro.processor.kafka.producer.acks", "all"));
        props.put(ProducerConfig.RETRIES_CONFIG,
                Integer.parseInt(context.getOrDefault("copro.processor.kafka.producer.retries", "3")));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                context.getOrDefault("copro.processor.kafka.request.timeout.ms", "30000"));
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                context.getOrDefault("copro.processor.kafka.delivery.timeout.ms", "120000"));
        props.put(ProducerConfig.LINGER_MS_CONFIG,
                context.getOrDefault("copro.processor.kafka.producer.linger.ms", "100"));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,
                context.getOrDefault("copro.processor.kafka.producer.compression.type", "lz4"));
        return props;
    }
}
