package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TrinityModel;
import in.handyman.raven.lib.model.trinitymodel.*;
import in.handyman.raven.lib.model.trinitymodel.copro.TrinityModelDataItemCopro;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "TrinityModel"
)
public class TrinityModelAction implements IActionExecution {
    public static final String COLUMN_LIST = "process_id,file_path,question, predicted_attribution_value,b_box, image_dpi , image_width , image_height , extracted_image_unit , action_id, root_pipeline_id,status,stage,paper_type, score,model_name,model_version,tenant_id, model_registry";
    public static final String CREATE_TABLE_COLUMN = "id bigserial not null, file_path text,question text, predicted_attribution_value text , score float8 NULL, b_box json null, image_dpi int8 null, image_width int8 null, image_height int8 null, extracted_image_unit varchar null, action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now(),status varchar NULL,stage varchar NULL ,paper_type varchar NULL,tenant_id int8 null, model_registry varchar NULL,model_name varchar null,model_version varchar null";
    public static final String CREATE_ERROR_TABLE_COLUMN = "id bigserial not null, file_path text,error_message text,  action_id bigint, root_pipeline_id bigint,process_id bigint, created_on timestamp not null default now() ,tenant_id int8 null";
    private final ActionExecutionAudit action;

    private final Logger log;
    private final Long tenantId;

    private final TrinityModel trinityModel;

    private final Marker aMarker;
    private final AtomicInteger counter = new AtomicInteger();
    private static String httpClientTimeout = new String();


    public TrinityModelAction(final ActionExecutionAudit action, final Logger log,
                              final Object trinityModel) {
        this.trinityModel = (TrinityModel) trinityModel;
        this.action = action;
        this.log = log;
        this.tenantId = Long.valueOf(action.getContext().get("tenant_id"));
        this.aMarker = MarkerFactory.getMarker(" TrinityModel:" + this.trinityModel.getName());
        httpClientTimeout = Optional.ofNullable(action.getContext().get("okhttp.client.timeout")).orElse("100");
    }

    @Override
    public void execute() throws Exception {


        try {
            log.info(aMarker, "Trinity Model Attribution Action for {} has been started", trinityModel.getName());
            log.info(aMarker, "Api endpoint to copro {}", trinityModel.getRequestUrl());

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(trinityModel.getResourceConn());
            final List<TrinityModelQueryResult> trinityModelQueryResults = new ArrayList<>();

            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(trinityModel.getQuestionSql());
                formattedQuery.forEach(sqlToExecute -> trinityModelQueryResults.addAll(handle.createQuery(sqlToExecute)
                        .mapToBean(TrinityModelQueryResult.class)
                        .stream().collect(Collectors.toList())));
            });


            // Create DDL

            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModel.getResponseAs() + " ( " + CREATE_TABLE_COLUMN + ");"));
            jdbi.useTransaction(handle -> handle.execute("create table if not exists macro." + trinityModel.getResponseAs() + "_error ( " + CREATE_ERROR_TABLE_COLUMN + ");"));
            final List<TrinityModelLineItem> trinityModelLineItems = new ArrayList<>();
            trinityModelQueryResults.stream().forEach(trinityModelQueryResult -> {
                trinityModelLineItems.add(TrinityModelLineItem.builder()
                        .questions(trinityModelQueryResult.getQuestions())
                        .filePath(trinityModelQueryResult.getFilePath())
                        .paperType(trinityModelQueryResult.getPaperType())
                        .modelRegistry(trinityModelQueryResult.getModelRegistry())
                        .tenantId(trinityModelQueryResult.getTenantId())
                        .build());
            });

//            Map<String, Map<String, List<TrinityModelQueryResult>>> listTrinityModelQueryResult = trinityModelQueryResults.stream().collect(Collectors.groupingBy(TrinityModelQueryResult::getFilePath, Collectors.groupingBy(TrinityModelQueryResult::getPaperType)));
            ObjectMapper mapper = new ObjectMapper();
//            listTrinityModelQueryResult.forEach((s, stringListMap) -> {
//                stringListMap.forEach((s1, trinityModelQueryResults1) -> trinityModelLineItems.add(TrinityModelLineItem.builder()
//                        .filePath(s).paperType(s1).questions(trinityModelQueryResults1.stream().map(TrinityModelQueryResult::getQuestion).collect(Collectors.toList()))
//                        .build()));
//            });

            String trinityUrl = trinityModel.getRequestUrl();
            final List<String> nodes = Optional.ofNullable(trinityUrl).map(s -> Arrays.asList(s.split(","))).orElse(Collections.emptyList());

            doProcess(trinityModelLineItems, nodes, mapper);
        } catch (Exception e) {
            log.error(aMarker, "Error in trinity model attribution action", e);
            throw new HandymanException("Error in trinity model attribution action", e, action);
        }

    }

    private void doProcess(final List<TrinityModelLineItem> assetBatch, List<String> nodes, ObjectMapper mapper) {
        final int forkBatchSize;
        if (trinityModel.getForkBatchSize() != null) {
            forkBatchSize = Integer.parseInt(trinityModel.getForkBatchSize());
        } else {
            forkBatchSize = 1;
        }
        final int coproEndpointNodeCount = nodes.size();
        try {
            if (coproEndpointNodeCount > 0) {
                int assetBatchSize = assetBatch.size();
                final int batchCount = (assetBatchSize / forkBatchSize) > 0 ? (assetBatchSize / forkBatchSize) : 1;
                log.info(aMarker, " batchsize - {}, copro-endpoint-node-size - {}, forkBatchSize - {}, donut-line items {} ", batchCount, coproEndpointNodeCount, forkBatchSize, assetBatchSize);
                if (forkBatchSize > 1) {

                    final List<List<TrinityModelLineItem>> partitionedAssetBatch = Lists.partition(assetBatch, forkBatchSize);
                    log.info(aMarker, "vqa running in parallel, partitionedAssetBatch {} ", partitionedAssetBatch);

                    partitionedAssetBatch.forEach(assetBatchItem -> {
                        try {
                            computeProcess(coproEndpointNodeCount, assetBatchItem, nodes, mapper, forkBatchSize);
                        } catch (InterruptedException e) {
                            throw new HandymanException("");
                        }

                    });

                } else {
                    log.info(aMarker, "vqa running in sequential, batchsize - {}, copro-endpoint-node-size - {}, forkBatchSize - {}, donut-line items {} ", batchCount, coproEndpointNodeCount, forkBatchSize, assetBatchSize);
                    computeProcess(coproEndpointNodeCount, assetBatch, nodes, mapper, forkBatchSize);
                }

            }
            action.getContext().put(trinityModel.getResponseAs().concat(".error"), "false");
        } catch (Exception e) {
            log.error(aMarker, "The Failure Response {} --> {}", trinityModel.getResponseAs(), e.getMessage(), e);
            action.getContext().put(trinityModel.getResponseAs().concat(".error"), "true");
            throw new HandymanException("Failure in trinity model response", e, action);
        }
    }

    private void computeProcess(final int nodeSize, final List<TrinityModelLineItem> assetBatchItem, List<String> nodes, ObjectMapper mapper, int forkBatchSize) throws InterruptedException {
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(trinityModel.getResourceConn());
        final ExecutorService executorService = Executors.newFixedThreadPool(forkBatchSize);
        final CountDownLatch countDownLatch = new CountDownLatch(assetBatchItem.size());

        try {
            log.info("compute process for vqa with assetBatchItem {}", assetBatchItem);
            assetBatchItem.forEach(asset -> executorService.submit(() -> {
                try {

                    log.info("submitting new work with thread name {}", Thread.currentThread().getName());
                    doWork(nodeSize, nodes, mapper, asset, jdbi);
                } finally {
                    log.info("completed new work with thread name {}", Thread.currentThread().getName());

                    countDownLatch.countDown();
                }

            }));

        } finally {
            countDownLatch.await();
            log.info("completed batch ");

        }
        if (!executorService.isShutdown()) {
            executorService.shutdown();
            log.info("shutting down executor service ");
        }

    }

    private void doWork(int nodeSize, List<String> nodes, ObjectMapper mapper, TrinityModelLineItem asset, Jdbi jdbi) {
        final String filePath = asset.getFilePath();
        final String paperType = asset.getPaperType();
        final String modelRegistry = asset.getModelRegistry();


        try {
            final List<String> questions = asset.getQuestions();
            final String node = nodes.get(counter.incrementAndGet() % nodeSize);

            if (log.isInfoEnabled()) {
                log.info(aMarker, "1. preparing {} for rest api call ", questions.size());
                log.info(aMarker, "2. info's are {}, {}, {}, {}", filePath, paperType, questions, modelRegistry);
            }
            String tritonRequestActivator = action.getContext().get("triton.request.activator");

            if (Objects.equals("false", tritonRequestActivator)) {
                log.info("Triton request activator : {} , Copro API running in legacy mode", tritonRequestActivator);

                coproRequestBuilder(node, filePath, paperType, questions, modelRegistry, jdbi, mapper);
            } else {
                log.info("Triton request activator : {} , Copro API running in legacy mode", tritonRequestActivator);

                tritonRequestBuilder(node, filePath, paperType, questions, modelRegistry, jdbi, mapper);
            }

        } catch (JsonProcessingException e) {

            jdbi.useTransaction(handle -> handle.createUpdate("INSERT INTO macro." + trinityModel.getResponseAs() + "_error (file_path,error_message, action_id, root_pipeline_id,process_id) VALUES(:filePath,:errorMessage, " + action.getActionId() + ", " + action.getRootPipelineId() + "," + action.getPipelineId() + ");")
                    .bind("filePath", filePath)
                    .bind("errorMessage", e.getMessage())
                    .execute());
            log.error(aMarker, "Error in inserting into trinity model action {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in trinity model action", handymanException, action);
        }
    }

    private void tritonRequestBuilder(String node, String filePath, String paperType, List<String> questions, String modelRegistry, Jdbi jdbi, ObjectMapper objectMapper) throws JsonProcessingException {
        final String trinityModelResultLineItems = new TrinityModelApiCaller(this, node, log).computeTriton(filePath, paperType, questions, modelRegistry, tenantId, action);
        TrinityModelResponse trinityModelResponse = objectMapper.readValue(trinityModelResultLineItems, new TypeReference<>() {
        });
        trinityModelResponse.getOutputs().forEach(trinityModelOutput -> trinityModelOutput.getData().forEach(trinityModelResultLineItem -> {
            extractedTritonOuputDataResponse(trinityModelResultLineItem, jdbi, filePath, tenantId, paperType, trinityModelResponse.getModelName(), trinityModelResponse.getModelVersion(), modelRegistry, objectMapper);
        }));
    }

    private void coproRequestBuilder(String node, String filePath, String paperType, List<String> questions, String modelRegistry, Jdbi jdbi, ObjectMapper mapper) throws JsonProcessingException {
        final String trinityModelResultLineItems = new TrinityModelApiCaller(this, node, log).computeCopro(filePath, paperType, questions, modelRegistry, tenantId, action);
        extractedCoproOutputResponse(trinityModelResultLineItems, jdbi, filePath, tenantId, paperType, modelRegistry, "", "", mapper);

    }

    private void extractedTritonOuputDataResponse(String trinityModelDataItems, Jdbi jdbi, String filePath, Long tenantId, String paperType, String modelName, String modelVersion, String modelRegistry, ObjectMapper objectMapper) {

        try {

            TrinityModelDataItem trinityModelDataItem = objectMapper.readValue(trinityModelDataItems, new TypeReference<>() {
            });

            log.info("TrinityModelLineItem size {}", trinityModelDataItem.getAttributes().size());

            log.info(aMarker, "completed {}", trinityModelDataItem.getAttributes().size());
            jdbi.useTransaction(handle -> {
                final PreparedBatch batch = handle.prepareBatch("INSERT INTO macro." + trinityModel.getResponseAs() + " (" + COLUMN_LIST + ") VALUES(" + action.getPipelineId() + ",:filePath,:question,:predictedAttributionValue, :bBoxes::json, :imageDpi, :imageWidth, :imageHeight , :extractedImageUnit, " + action.getActionId() + "," + action.getRootPipelineId() + ",:status,:stage,:paperType, :scores, :modelName, :modelVersion,:tenantId, :modelRegistry);");

                Lists.partition(trinityModelDataItem.getAttributes(), 100).forEach(resultLineItems -> {
                    log.info(aMarker, "inserting into trinity model_action {}", resultLineItems.size());
                    resultLineItems.forEach(resultLineItem -> {
                        batch.bind("filePath", trinityModelDataItem.getInputFilePath())
                                .bind("question", resultLineItem.getQuestion())
                                .bind("predictedAttributionValue", resultLineItem.getPredictedAttributionValue())
                                .bind("scores", resultLineItem.getScores())
                                .bind("paperType", paperType)
                                .bind("bBoxes", String.valueOf(resultLineItem.getBboxes()))
                                .bind("imageDpi", trinityModelDataItem.getImageDPI())
                                .bind("imageWidth", trinityModelDataItem.getImageWidth())
                                .bind("imageHeight", trinityModelDataItem.getImageHeight())
                                .bind("extractedImageUnit", trinityModelDataItem.getExtractedImageUnit())
                                .bind("status", "COMPLETED")
                                .bind("stage", "VQA_TRANSACTION")
                                .bind("modelName", modelName)
                                .bind("modelVersion", modelVersion)
                                .bind("tenantId", resultLineItem.getTenantId())
                                .bind("modelRegistry", modelRegistry)
                                .add();

                    });
                    try {
                        int[] counts = batch.execute();
                        log.info(aMarker, " persisted {} in trinity model_action", counts);
                    } catch (Exception e) {
                        log.info(aMarker, " persisted {} in trinity model_action", e);
                    }
                });
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractedCoproOutputResponse(String trinityModelDataItems, Jdbi jdbi, String filePath, Long
            tenantId, String paperType, String modelRegistry, String modelName, String modelVersion, ObjectMapper objectMapper) {

        try {

            TrinityModelDataItemCopro trinityModelDataItem = objectMapper.readValue(trinityModelDataItems, new TypeReference<>() {
            });

            log.info("TrinityModelLineItem size {}", trinityModelDataItem.getAttributes().size());

            log.info(aMarker, "completed {}", trinityModelDataItem.getAttributes().size());
            jdbi.useTransaction(handle -> {
                final PreparedBatch batch = handle.prepareBatch("INSERT INTO macro." + trinityModel.getResponseAs() + " (" + COLUMN_LIST + ") VALUES(" + action.getPipelineId() + ",:filePath,:question,:predictedAttributionValue, :bBoxes::json, :imageDpi, :imageWidth, :imageHeight , :extractedImageUnit, " + action.getActionId() + "," + action.getRootPipelineId() + "," + action.getProcessId() + ",:status,:stage,:paperType, :scores, :modelName, :modelVersion,:tenantId, :modelRegistry);");

                Lists.partition(trinityModelDataItem.getAttributes(), 100).forEach(resultLineItems -> {
                    log.info(aMarker, "inserting into trinity model_action {}", resultLineItems.size());
                    resultLineItems.forEach(resultLineItem -> {
                        batch.bind("filePath", filePath)
                                .bind("question", resultLineItem.getQuestion())
                                .bind("predictedAttributionValue", resultLineItem.getPredictedAttributionValue())
                                .bind("scores", resultLineItem.getScores())
                                .bind("paperType", paperType)
                                .bind("bBoxes", String.valueOf(resultLineItem.getBboxes()))
                                .bind("imageDpi", trinityModelDataItem.getImageDPI())
                                .bind("imageWidth", trinityModelDataItem.getImageWidth())
                                .bind("imageHeight", trinityModelDataItem.getImageHeight())
                                .bind("extractedImageUnit", trinityModelDataItem.getExtractedImageUnit())
                                .bind("status", "COMPLETED")
                                .bind("stage", "VQA_TRANSACTION")
                                .bind("modelName", modelName)
                                .bind("modelVersion", modelVersion)
                                .bind("tenantId", tenantId)
                                .bind("modelRegistry", modelRegistry)
                                .add();

                    });
                    try {
                        int[] counts = batch.execute();
                        log.info(aMarker, " persisted {} in trinity model_action", counts);
                    } catch (Exception e) {
                        log.info(aMarker, " persisted {} in trinity model_action", e);
                    }


                });
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean executeIf() throws Exception {
        return trinityModel.getCondition();
    }

    public String getHttpClientTimeout() {
        return httpClientTimeout;
    }


}