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
import org.jdbi.v3.core.Handle;
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

    private static final String HEADER_OUTPUT_TABLE = "X-Route-OutputTable";
    private static final String HEADER_REQUEST_TYPE = "X-Route-RequestType";
    private static final String HEADER_BATCH_ID = "X-Route-BatchId";
    private static final String HEADER_CORRELATION_ID = "X-Route-CorrelationId";
    private static final String HEADER_ROOT_PIPELINE_ID = "X-Route-RootPipelineId";
    private static final String HEADER_ORIGIN_ID = "X-Route-OriginId";
    private static final String HEADER_PAGE_NO = "X-Route-PageNo";
    private static final String HEADER_PROCESS_ID = "X-Route-ProcessId";
    private static final String HEADER_TENANT_ID = "X-Route-TenantId";
    private static final String HEADER_GROUP_ID = "X-Route-GroupId";
    private static final String HEADER_CREATED_ON = "X-Route-CreatedOn";
    private static final String HEADER_ACTION_ID = "X-Route-ActionId";
    private static final String HEADER_TEMPLATE_ID = "X-Route-TemplateId";
    private static final String HEADER_TEMPLATE_NAME = "X-Route-TemplateName";
    private static final String HEADER_FILE_PATH = "X-Route-FilePath";
    private static final String HEADER_PROMPT_TYPE = "X-Route-PromptType";
    private static final String HEADER_MODEL_NAME = "X-Route-ModelName";
    private static final String HEADER_PROCESS = "X-Route-Process";
    private static final String HEADER_MODEL_REGISTRY = "X-Route-ModelRegistry";
    private static final String HEADER_API_NAME = "X-Route-ApiName";
    private static final String HEADER_CATEGORY = "X-Route-Category";
    private static final String HEADER_SOR_CONTAINER_ID = "X-Route-SorContainerId";
    private static final String HEADER_SOR_CONTAINER_NAME = "X-Route-SorContainerName";
    private static final String HEADER_INPUT_FILE_PATH = "X-Route-InputFilePath";
    private static final String HEADER_POST_PROCESS = "X-Route-PostProcess";
    private static final String HEADER_POST_PROCESS_CLASS_NAME = "X-Route-PostProcessClassName";
    private static final String HEADER_POST_PROCESS_CLASS = "X-Route-PostProcessClass";

    private static final String CONTEXT_REQUEST_TYPE = "copro.processor.kafka.request.type";
    private static final String CONTEXT_TOPIC = "copro.processor.kafka.topic";
    private static final String CONTEXT_OUTPUT_TABLE = "copro.processor.kafka.output.table";
    private static final String CONTEXT_BOOTSTRAP_SERVERS = "copro.processor.kafka.bootstrap.servers";
    private static final String CONTEXT_ACKS = "copro.processor.kafka.producer.acks";
    private static final String CONTEXT_RETRIES = "copro.processor.kafka.producer.retries";
    private static final String CONTEXT_REQUEST_TIMEOUT = "copro.processor.kafka.request.timeout.ms";
    private static final String CONTEXT_DELIVERY_TIMEOUT = "copro.processor.kafka.delivery.timeout.ms";
    private static final String CONTEXT_LINGER_MS = "copro.processor.kafka.producer.linger.ms";
    private static final String CONTEXT_COMPRESSION_TYPE = "copro.processor.kafka.producer.compression.type";
    private static final String CONTEXT_NEXT_SCRIPT = "continuation_pipeline_script";
    private static final String CONTEXT_INIT_PROCESS_ID = "init_process_id.process_id";
    private static final String CONTEXT_TENANT_ID = "tenant_id";
    private static final String CONTEXT_GROUP_ID = "group_id";

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

        final String requestType = callable.getRequestType() != null ? callable.getRequestType() : context.get(CONTEXT_REQUEST_TYPE);
        final String topic;
        if (callable.getKafkaTopic() != null) {
            topic = callable.getKafkaTopic();
        } else {
            topic = context.get(CONTEXT_TOPIC);
        }
        final String batchId = context.get("batch_id");
        final String outputTable = callable.getOutputTable() != null ? callable.getOutputTable() : context.getOrDefault(CONTEXT_OUTPUT_TABLE, "");

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
                    produceKafkaMessage(callable, topic, outputTable, batchId, requestType, context, item, producer, failedItems);
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

    private void produceKafkaMessage(CoproProcessor.ConsumerProcess<I, O> callable, String topic, String outputTable, String batchId, String requestType, Map<String, String> context, I item, KafkaProducer<String, String> producer, ConcurrentLinkedQueue<I> failedItems) throws Exception {
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

        recordData.headers().add(HEADER_OUTPUT_TABLE, outputTable.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_REQUEST_TYPE, requestType.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_BATCH_ID, batchId.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_ROOT_PIPELINE_ID, String.valueOf(actionExecutionAudit.getRootPipelineId()).getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_ORIGIN_ID, messageKey != null ? messageKey.getBytes(StandardCharsets.UTF_8) : new byte[0]);
        String pageNoVal = String.valueOf(entityFields.getOrDefault("pageNo", entityFields.getOrDefault("page_no",
                entityFields.getOrDefault("paperNo", entityFields.getOrDefault("paper_no", "0")))));
        recordData.headers().add(HEADER_PAGE_NO, pageNoVal.getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_PROCESS_ID, context.getOrDefault(CONTEXT_INIT_PROCESS_ID, "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_TENANT_ID, context.getOrDefault(CONTEXT_TENANT_ID, "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_GROUP_ID, context.getOrDefault(CONTEXT_GROUP_ID, "").getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_CREATED_ON, String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        recordData.headers().add(HEADER_ACTION_ID, String.valueOf(actionExecutionAudit.getActionId()).getBytes(StandardCharsets.UTF_8));

        addHeaderIfPresent(recordData, entityFields, HEADER_TEMPLATE_ID, "templateId");
        addHeaderIfPresent(recordData, entityFields, HEADER_TEMPLATE_NAME, "templateName");
        addHeaderIfPresent(recordData, entityFields, HEADER_FILE_PATH, "filePath");
        addHeaderIfPresent(recordData, entityFields, HEADER_PROMPT_TYPE, "promptType");
        addHeaderIfPresent(recordData, entityFields, HEADER_MODEL_NAME, "modelName");
        addHeaderIfPresent(recordData, entityFields, HEADER_PROCESS, "process");
        addHeaderIfPresent(recordData, entityFields, HEADER_MODEL_REGISTRY, "modelRegistry");
        addHeaderIfPresent(recordData, entityFields, HEADER_API_NAME, "apiName");
        addHeaderIfPresent(recordData, entityFields, HEADER_CATEGORY, "category");
        addHeaderIfPresent(recordData, entityFields, HEADER_SOR_CONTAINER_ID, "sorContainerId");
        addHeaderIfPresent(recordData, entityFields, HEADER_SOR_CONTAINER_NAME, "sorContainerName");
        addHeaderIfPresent(recordData, entityFields, HEADER_INPUT_FILE_PATH, "inputFilePath");
        addHeaderIfPresent(recordData, entityFields, HEADER_POST_PROCESS, "postProcess");
        addHeaderIfPresent(recordData, entityFields, HEADER_POST_PROCESS_CLASS_NAME, "postProcessClassName");
        addHeaderIfPresent(recordData, entityFields, HEADER_POST_PROCESS_CLASS, "postProcessClass");

        return recordData;
    }

    private void addHeaderIfPresent(ProducerRecord<String, String> recordData, Map<String, Object> fields, String headerKey, String fieldKey) {
        Object val = fields.get(fieldKey);
        if (val != null) {
            recordData.headers().add(headerKey, String.valueOf(val).getBytes(StandardCharsets.UTF_8));
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
                updateDlqStatusForFailedItems(batchId, requestType, handle, item, mapper);
            }

            handle.execute(
                    "UPDATE kafka_audit.inference_queue_active " +
                            "SET total_requests = total_requests - ?, failed_requests = failed_requests + ?, updated_at=NOW() " +
                            "WHERE batch_id=? AND request_type=?",
                    failedItems.size(), failedItems.size(), batchId, requestType);
        });
        logger.error("KAFKA_ASYNC: {} items moved to DLQ for batch={} type={}", failedItems.size(), batchId, requestType);
    }

    private void updateDlqStatusForFailedItems(String batchId, String requestType, Handle handle, I item, ObjectMapper mapper) {
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

    /**
     * Persist pipeline_wait_state so PipelineAggregatorService can trigger the continuation script.
     */
    protected void persistAsyncWaitState(String batchId, String requestType, int publishedCount, Map<String, String> context) {
        String nextScript = context.getOrDefault(CONTEXT_NEXT_SCRIPT, "");
        if (nextScript.isBlank()) {
            logger.warn("KAFKA_ASYNC: continuation_pipeline_script not set in context for batch={} type={}. Continuation will not trigger automatically.",
                    batchId, requestType);
        }

        String contextJson;
        try {
            contextJson = new ObjectMapper().writeValueAsString(context);
        } catch (Exception exception) {
            logger.error("KAFKA_ASYNC: failed to serialize context to JSON for batch={} type={}. Context will be empty in pipeline_wait_state.", batchId, requestType, exception);
            throw new HandymanException("Failed to serialize context to JSON for pipeline_wait_state", exception, actionExecutionAudit);
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
        String bootstrapServers = context.getOrDefault(CONTEXT_BOOTSTRAP_SERVERS, "localhost:9092");

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, context.getOrDefault(CONTEXT_ACKS, "all"));
        props.put(ProducerConfig.RETRIES_CONFIG,
                Integer.parseInt(context.getOrDefault(CONTEXT_RETRIES, "3")));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                context.getOrDefault(CONTEXT_REQUEST_TIMEOUT, "30000"));
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                context.getOrDefault(CONTEXT_DELIVERY_TIMEOUT, "120000"));
        props.put(ProducerConfig.LINGER_MS_CONFIG,
                context.getOrDefault(CONTEXT_LINGER_MS, "100"));
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG,
                context.getOrDefault(CONTEXT_COMPRESSION_TYPE, "lz4"));
        return props;
    }
}
