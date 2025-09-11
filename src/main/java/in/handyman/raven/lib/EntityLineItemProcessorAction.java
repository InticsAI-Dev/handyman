package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EntityLineItemProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.Base64toActualVaue;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section.EntitySectionValidator;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section.RadonMultiSectionOutputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section.RadonMultiSectionQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section.RadonMultiSectionResponseTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.TritonDataTypes;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.lib.utils.CustomBatchWithScaling;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

@ActionExecution(actionName = "EntityLineItemProcessor")
public class EntityLineItemProcessorAction implements IActionExecution {

    private final EntityLineItemProcessor entityLineItemProcessor;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final Marker aMarker;
    private final InticsIntegrity securityEngine;
    private final int threadSleepTime;
    private final int writeBatchSize;
    private int readBatchSize;
    private final int timeoutMinutes;
    private final OkHttpClient httpclient;

    private final String targetTableName;
    private final String radonKvpUrl;
    private final String insertQuery;
    private final String processBase64;

    private final List<RadonMultiSectionQueryInputTable> radonMultiSectionQueryInputTables = Collections.synchronizedList(new ArrayList<>());
    private final List<RadonMultiSectionQueryInputTable> radonMultiSectionApiInputTables = Collections.synchronizedList(new ArrayList<>());
    private final List<RadonMultiSectionResponseTable> parentObj = Collections.synchronizedList(new ArrayList<>());
    private final List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables = Collections.synchronizedList(new ArrayList<>());

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String ENCRYPTION_POLICY = "AES256";
    private static final String INSERT_INTO = "INSERT INTO";
    public static final String COLUMN_LIST = "created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path," +
            " total_response_json, paper_no, origin_id, process_id, action_id, process, group_id, tenant_id, " +
            "root_pipeline_id, batch_id, model_registry, status, stage, message, category,request,response,endpoint,sor_container_id";
    public static final String VAL_STRING_LIST = "VALUES( ?,?,?,?,?," +
            "?,?,?,?,?" +
            ",?,?,?,?,?," +
            "?,?, ?, ?" +
            ",?,?,?,?, ?)";

    public EntityLineItemProcessorAction(final ActionExecutionAudit action, final Logger log, final Object entityLineItemProcessor) {
        this.entityLineItemProcessor = (EntityLineItemProcessor) entityLineItemProcessor;
        this.actionExecutionAudit = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("EntityLineItemProcessor:" + this.entityLineItemProcessor.getName());
        this.securityEngine = SecurityEngine.getInticsIntegrityMethod(this.actionExecutionAudit, log);

        this.timeoutMinutes = parseIntFromContext(action, "copro.client.socket.timeout", 100);
        this.threadSleepTime = parseIntFromContext(action, "copro.client.api.sleeptime", 1000);
        this.writeBatchSize = parseIntFromContext(action, "write.batch.size", 10);

        this.targetTableName = Optional.ofNullable(this.entityLineItemProcessor.getOutputTable()).orElse("UNKNOWN_OUTPUT_TABLE");
        this.radonKvpUrl = this.entityLineItemProcessor.getEndpoint();
        this.processBase64 = Optional.ofNullable(action.getContext().get("pipeline.copro.api.process.file.format")).orElse(ProcessFileFormatE.FILE.name());
        this.insertQuery = INSERT_INTO + " " + targetTableName + "(" + COLUMN_LIST + ") " + " " + VAL_STRING_LIST;

        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMinutes(this.timeoutMinutes))
                .writeTimeout(Duration.ofMinutes(this.timeoutMinutes))
                .readTimeout(Duration.ofMinutes(this.timeoutMinutes))
                .build();

        safeLogInfo("Initialized EntityLineItemProcessorAction for: {}", this.entityLineItemProcessor.getName());
    }

    @Override
    public void execute() throws Exception {
        safeLogInfo("Entity Line Item Processor Action started for {}", entityLineItemProcessor.getName());
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(entityLineItemProcessor.getResourceConn());
        String inputQuery = entityLineItemProcessor.getQuerySet();
        startProducer(inputQuery, jdbi);

        FileProcessingUtils fileProcessingUtils = new FileProcessingUtils(log, aMarker, actionExecutionAudit);

        buildApiInputTablesFromQueries();

        int consumerApiCount = determineConsumerApiCount();
        readBatchSize = parseIntFromContext(actionExecutionAudit, "read.batch.size", 10);
        if (consumerApiCount >= readBatchSize) {
            safeLogInfo("Adjusting readBatchSize to consumer count: {} >= {}", consumerApiCount, readBatchSize);
            readBatchSize = consumerApiCount;
        } else {
            safeLogInfo("Using configured readBatchSize {}", readBatchSize);
        }

        final ExecutorService executorService = createExecutorService(consumerApiCount);
        final CountDownLatch countDownLatch = new CountDownLatch(Math.max(1, consumerApiCount));

        doPromptInferencing(executorService, fileProcessingUtils, countDownLatch);

        EntitySectionValidator multiSectionValidator = new EntitySectionValidator(actionExecutionAudit, log, aMarker, jdbi);
        List<RadonMultiSectionOutputTable> multiSectionOutputTables = multiSectionValidator.doValidation(parentObj, radonMultiSectionOutputTables);

        safeLogInfo("Entity Line Item Processor Action completed for {}", entityLineItemProcessor.getName());
        saveMultiSectionOutputDetails(multiSectionOutputTables, jdbi);
    }

    private void doPromptInferencing(ExecutorService executorService, FileProcessingUtils fileProcessingUtils, CountDownLatch countDownLatch) {
        try {
            for (RadonMultiSectionQueryInputTable input : radonMultiSectionApiInputTables) {
                executorService.submit(() -> {
                    try {
                        URL endpoint = new URL(radonKvpUrl);
                        parentObj.addAll(process(endpoint, input, fileProcessingUtils, countDownLatch));
                    } catch (Exception e) {
                        throw new HandymanException(e);
                    }
                });
            }
        } finally {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn(aMarker, "Interrupted while waiting for worker threads", e);
            } finally {
                safeLogInfo("Shutting down executor service");
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
    }

    private ExecutorService createExecutorService(int consumerApiCount) {
        String poolType = actionExecutionAudit.getContext().getOrDefault("copro.processor.thread.creator", "WORK_STEALING");
        if ("FIXED_THREAD".equalsIgnoreCase(poolType)) {
            safeLogInfo("Creating fixed thread pool with size {}", consumerApiCount);
            return Executors.newFixedThreadPool(Math.max(1, consumerApiCount));
        } else {
            safeLogInfo("Creating work-stealing pool");
            return Executors.newWorkStealingPool();
        }
    }

    private int determineConsumerApiCount() {
        int consumerApiCount = 0;
        CustomBatchWithScaling customBatchWithScaling = new CustomBatchWithScaling(actionExecutionAudit, log);
        boolean isPodScalingCheckEnabled = customBatchWithScaling.isPodScalingCheckEnabled();
        if (isPodScalingCheckEnabled) {
            safeLogInfo("Pod scaling check enabled, computing consumer API count");
            consumerApiCount = customBatchWithScaling.computeSorTransactionApiCount();
        }

        if (consumerApiCount <= 0) {
            safeLogInfo("Using fallback consumer API count from context");
            consumerApiCount = parseIntFromContext(actionExecutionAudit, "Radon.kvp.consumer.API.count", 1);
        }
        safeLogInfo("Consumer API count for kvp action: {}", consumerApiCount);
        return Math.max(1, consumerApiCount);
    }

    private void buildApiInputTablesFromQueries() {
        Map<String, List<RadonMultiSectionQueryInputTable>> byOrigin = radonMultiSectionQueryInputTables.stream()
                .collect(Collectors.groupingBy(RadonMultiSectionQueryInputTable::getOriginId, LinkedHashMap::new, Collectors.toList()));

        byOrigin.forEach((originId, listForOrigin) -> {

            Map<Integer, List<RadonMultiSectionQueryInputTable>> byPage = listForOrigin.stream()
                    .collect(Collectors.groupingBy(RadonMultiSectionQueryInputTable::getPaperNo, LinkedHashMap::new, Collectors.toList()));

            byPage.forEach((s, byPageInput) -> {
                Map<Integer, List<RadonMultiSectionQueryInputTable>> byPriority = byPageInput.stream()
                        .collect(Collectors.groupingBy(RadonMultiSectionQueryInputTable::getPriorityOrder, LinkedHashMap::new, Collectors.toList()));

                byPriority.entrySet().stream().min(Map.Entry.comparingByKey())
                        .ifPresent(entry -> radonMultiSectionApiInputTables.addAll(entry.getValue()));
            });
        });

        safeLogInfo("Prepared {} API input(s) from queries (origins: {})", radonMultiSectionApiInputTables.size(), radonMultiSectionQueryInputTables.stream().map(RadonMultiSectionQueryInputTable::getOriginId).distinct().count());
    }

    private void saveMultiSectionOutputDetails(List<RadonMultiSectionOutputTable> multiSectionOutputTables, Jdbi jdbi) {
        if (multiSectionOutputTables == null || multiSectionOutputTables.isEmpty()) {
            safeLogInfo("No records to insert into {}", targetTableName);
            return;
        }

        safeLogInfo("Starting batch insert of {} records into {}", multiSectionOutputTables.size(), targetTableName);
        jdbi.useTransaction(handle -> {
            PreparedBatch batch = handle.prepareBatch(insertQuery);
            int counter = 0;
            for (RadonMultiSectionOutputTable output : multiSectionOutputTables) {
                batch
                        .bind(0, output.getCreatedOn())
                        .bind(1, output.getCreatedUserId())
                        .bind(2, output.getLastUpdatedOn())
                        .bind(3, output.getLastUpdatedUserId())
                        .bind(4, output.getInputFilePath())
                        .bind(5, output.getTotalResponseJson())
                        .bind(6, output.getPaperNo())
                        .bind(7, output.getOriginId())
                        .bind(8, output.getProcessId())
                        .bind(9, output.getActionId())
                        .bind(10, output.getProcess())
                        .bind(11, output.getGroupId())
                        .bind(12, output.getTenantId())
                        .bind(13, output.getRootPipelineId())
                        .bind(14, output.getBatchId())
                        .bind(15, output.getModelRegistry())
                        .bind(16, output.getStatus())
                        .bind(17, output.getStage())
                        .bind(18, output.getMessage())
                        .bind(19, output.getCategory())
                        .bind(20, output.getRequest())
                        .bind(21, output.getResponse())
                        .bind(22, output.getEndpoint())
                        .bind(23, output.getSorContainerId())
                        .add();

                counter++;
                if (counter % writeBatchSize == 0) {
                    batch.execute();
                    batch = handle.prepareBatch(insertQuery);
                }
            }
            if (counter % writeBatchSize != 0) {
                batch.execute();
            }
            safeLogInfo("Completed batch insert of {} records into {}", multiSectionOutputTables.size(), targetTableName);
        });
    }

    public int getTimeOut() {
        return this.timeoutMinutes;
    }

    private int parseIntFromContext(ActionExecutionAudit action, String key, int defaultValue) {
        String raw = Optional.ofNullable(action.getContext().get(key)).orElse(String.valueOf(defaultValue)).trim();
        if (raw.isEmpty()) {
            safeLogDebug("Context key '{}' missing or empty; using default {}", key, defaultValue);
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(raw);
            safeLogDebug("Context key '{}' parsed as {}", key, parsed);
            return parsed;
        } catch (NumberFormatException e) {
            log.warn(aMarker, "Invalid integer for context key '{}': {}. Using default {}", key, raw, defaultValue);
            return defaultValue;
        }
    }


    public void startProducer(final String sqlQuery, Jdbi jdbi) {
        final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(sqlQuery);
        for (String sql : formattedQuery) {
            jdbi.useTransaction(handle -> {
                List<RadonMultiSectionQueryInputTable> results = handle.createQuery(sql)
                        .mapToBean(RadonMultiSectionQueryInputTable.class)
                        .collect(Collectors.toList());
                radonMultiSectionQueryInputTables.addAll(results);
                safeLogInfo("Loaded {} records from query", results.size());
            });
        }
    }


    public List<RadonMultiSectionResponseTable> process(URL endpoint, RadonMultiSectionQueryInputTable entity,
                                                        FileProcessingUtils fileProcessingUtils, CountDownLatch countDownLatch) {
        try {
            List<RadonMultiSectionResponseTable> entityObj = new ArrayList<>();

            Integer paperNo = entity.getPaperNo();
            String originId = entity.getOriginId();
            String userPrompt = buildUserPromptSafely(entity);

            RadonKvpExtractionRequest radonKvpExtractionRequest = buildRadonRequest(entity, userPrompt, fileProcessingUtils);

            String jsonInputRequest = MAPPER.writeValueAsString(radonKvpExtractionRequest);

            TritonRequest requestBody = new TritonRequest();
            requestBody.setName(entity.getApiName());
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype(TritonDataTypes.BYTES.name());
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));

            String jsonRequest = MAPPER.writeValueAsString(tritonInputRequest);

            radonKvpExtractionRequest.setBase64Img("");
            String jsonInsertRequest = MAPPER.writeValueAsString(radonKvpExtractionRequest);

            safeLogDebug("Built Triton request for rootPipelineId={}, paperNo={}, originId={}", entity.getRootPipelineId(), paperNo, maskForLog(originId));

            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON))
                    .build();

            tritonRequestBuilder(entity, request, entityObj, jsonInsertRequest, endpoint);

            safeLogInfo("Processed entity rootPipelineId={}, paperNo={}, originId={}, outputCount={}",
                    entity.getRootPipelineId(), paperNo, maskForLog(originId), entityObj.size());

            return entityObj;
        } catch (Exception e) {
            throw new HandymanException(e);
        } finally {
            countDownLatch.countDown();
        }
    }

    private RadonKvpExtractionRequest buildRadonRequest(RadonMultiSectionQueryInputTable entity, String userPrompt, FileProcessingUtils fileProcessingUtils) throws IOException {
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();
        radonKvpExtractionRequest.setRootPipelineId(entity.getRootPipelineId());
        radonKvpExtractionRequest.setActionId(actionExecutionAudit.getActionId());
        radonKvpExtractionRequest.setProcess(entity.getProcess());
        radonKvpExtractionRequest.setInputFilePath(entity.getInputFilePath());
        radonKvpExtractionRequest.setGroupId(entity.getGroupId());
        radonKvpExtractionRequest.setUserPrompt(userPrompt);
        radonKvpExtractionRequest.setSystemPrompt(entity.getSystemPrompt());
        radonKvpExtractionRequest.setBase64Img("");
        radonKvpExtractionRequest.setProcessId(entity.getProcessId());
        radonKvpExtractionRequest.setPaperNo(entity.getPaperNo());
        radonKvpExtractionRequest.setTenantId(entity.getTenantId());
        radonKvpExtractionRequest.setOriginId(entity.getOriginId());
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());
        radonKvpExtractionRequest.setSorContainerId(entity.getSorContainerId());
        radonKvpExtractionRequest.setModelName(entity.getModelName());

        if (ProcessFileFormatE.BASE64.name().equalsIgnoreCase(processBase64)) {
            radonKvpExtractionRequest.setBase64Img(fileProcessingUtils.convertFileToBase64(entity.getInputFilePath()));
        }

        return radonKvpExtractionRequest;
    }


    private String buildUserPromptSafely(RadonMultiSectionQueryInputTable entity) {
        String userPrompt;
        String base64Activator = actionExecutionAudit.getContext().get("sor.transaction.prompt.base64.activator");
        boolean isBboxEnabled = "true".equals(actionExecutionAudit.getContext().get("bbox.radon_bbox_activator"))
                && "RADON_KVP_ACTION".equals(entity.getProcess());

        if (isBboxEnabled) {
            safeLogInfo("BBox activator enabled for rootPipelineId={}, originId={}", entity.getRootPipelineId(), maskForLog(entity.getOriginId()));
            String inputResponseJsonString = entity.getInputResponseJson();
            String inputResponseJson;
            String encryptOutputJsonContent = actionExecutionAudit.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, log);

            if ("true".equals(encryptOutputJsonContent)) {
                inputResponseJson = encryption.decrypt(inputResponseJsonString, ENCRYPTION_POLICY, "RADON_KVP_JSON");
            } else {
                inputResponseJson = inputResponseJsonString;
            }

            if ("true".equals(base64Activator)) {
                Base64toActualVaue base64Caller = new Base64toActualVaue();
                String base64Value = base64Caller.base64toActual(entity.getUserPrompt());
                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                String decodedPrompt = new String(decodedBytes);
                String placeholder = actionExecutionAudit.getContext().get("prompt.bbox.json.placeholder.name");
                String updatedPrompt = decodedPrompt.replace(placeholder, inputResponseJson);
                userPrompt = Base64.getEncoder().encodeToString(updatedPrompt.getBytes());
            } else {
                String placeholder = actionExecutionAudit.getContext().get("prompt.bbox.json.placeholder.name");
                userPrompt = entity.getUserPrompt().replace(placeholder, inputResponseJson);
            }
        } else {
            safeLogDebug("BBox activator disabled or different process. Using original user prompt metadata only");
            userPrompt = entity.getUserPrompt();
        }
        return userPrompt;
    }

    private void tritonRequestBuilder(RadonMultiSectionQueryInputTable entity, Request request,
                                      List<RadonMultiSectionResponseTable> entityObj, String jsonRequest, URL endpoint) {
        long groupId = entity.getGroupId();
        long processId = entity.getProcessId();
        long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        long rootPipelineId = entity.getRootPipelineId();

        Timestamp createdOn = CreateTimeStamp.currentTimestamp();
        entity.setCreatedOn(createdOn);

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    safeLogInfo("Received empty response body for rootPipelineId={}, paperNo={}", rootPipelineId, paperNo);
                    return;
                }
                String responseString = responseBody.string();
                RadonKvpExtractionResponse modelResponse = MAPPER.readValue(responseString, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null) {
                    modelResponse.getOutputs().forEach(output -> output.getData().forEach(radonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, radonDataItem, entityObj, jsonRequest, responseString, endpoint.toString());
                        } catch (IOException e) {
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("Radon kvp consumer failed for group -" + groupId + " originId " + entity.getOriginId() + " paperNo " + entity.getPaperNo(), handymanException, this.actionExecutionAudit);
                            log.error(aMarker, "Error converting triton output: {}", ExceptionUtil.toString(e));
                        }
                    }));
                }
            } else {
                String msg = response.message();
                int code = response.code();
                safeLogWarn("Unsuccessful Triton response for rootPipelineId={}, paperNo={}, status={}, message={}", entity.getRootPipelineId(), entity.getPaperNo(), code, maskForLog(msg));
                String errorBody = "Unavailable";
                entityObj.add(RadonMultiSectionResponseTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .actionId(actionExecutionAudit.getActionId())
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(msg)
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(tenantId)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(tenantId)
                        .category(entity.getCategory())
                        .request(jsonRequest)
                        .response(errorBody)
                        .endpoint(String.valueOf(endpoint))
                        .priorityOrder(entity.getPriorityOrder())
                        .postprocessingScript(entity.getPostprocessingScript())
                        .build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : " + code + " message : " + maskForLog(msg));
                HandymanException.insertException("Radon kvp consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.actionExecutionAudit);
            }
        } catch (IOException e) {
            handleErrorEntityObject(entity, entityObj, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.actionExecutionAudit);
            log.error(aMarker, "IO Exception while calling Triton: {}", ExceptionUtil.toString(e));
        }
    }

    private void handleErrorEntityObject(RadonMultiSectionQueryInputTable entity, List<RadonMultiSectionResponseTable> entityObj, Exception e) {
        entityObj.add(RadonMultiSectionResponseTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(entity.getApiName())
                .message(ExceptionUtil.toString(e))
                .batchId(entity.getBatchId())
                .createdOn(entity.getCreatedOn())
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getTenantId())
                .category(entity.getCategory())
                .build());
    }

    private void extractTritonOutputDataResponse(RadonMultiSectionQueryInputTable entity, String radonDataItem, List<RadonMultiSectionResponseTable> entityObj, String request, String response, String endpoint) throws IOException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        RadonKvpLineItem modelResponse = MAPPER.readValue(radonDataItem, RadonKvpLineItem.class);

        String encryptOutputJsonContent = actionExecutionAudit.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, log);

        String extractedContent;
        if ("true".equals(encryptOutputJsonContent)) {
            extractedContent = encryption.encrypt(modelResponse.getInferResponse(), ENCRYPTION_POLICY, "RADON_KVP_JSON");
        } else {
            extractedContent = modelResponse.getInferResponse();
        }

        entityObj.add(RadonMultiSectionResponseTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(extractedContent)
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(actionExecutionAudit.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .batchId(modelResponse.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(request)
                .response(response)
                .sorContainerId(entity.getSorContainerId())
                .endpoint(String.valueOf(endpoint))
                .priorityOrder(entity.getPriorityOrder())
                .postprocessingScript(entity.getPostprocessingScript())
                .build()
        );
    }

    @Override
    public boolean executeIf() throws Exception {
        return entityLineItemProcessor.getCondition();
    }


    private void safeLogInfo(String pattern, Object... args) {
        try {
            log.info(aMarker, pattern, args);
        } catch (Exception ignored) {
            log.info(pattern, args);
        }
    }

    private void safeLogWarn(String pattern, Object... args) {
        try {
            log.warn(aMarker, pattern, args);
        } catch (Exception ignored) {
            log.warn(pattern, args);
        }
    }

    private void safeLogDebug(String pattern, Object... args) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(aMarker, pattern, args);
            }
        } catch (Exception ignored) {
            if (log.isDebugEnabled()) {
                log.debug(pattern, args);
            }
        }
    }

    private String maskForLog(String s) {
        if (s == null) return "null";
        int len = s.length();
        String prefix = s.length() <= 6 ? s : s.substring(0, 6);
        return String.format("%s... (len=%d)", prefix, len);
    }
}