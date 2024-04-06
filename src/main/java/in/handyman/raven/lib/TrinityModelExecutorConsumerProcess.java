package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.trinitymodel.TrinityModelDataItem;
import in.handyman.raven.lib.model.trinitymodel.TrinityModelResponse;
import in.handyman.raven.lib.model.trinitymodel.copro.TrinityModelDataItemCopro;
import in.handyman.raven.lib.model.trinitymodel.trinityModelExecutor.TrinityModelExecutorApiCaller;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TrinityModelExecutorConsumerProcess implements CoproProcessor.ConsumerProcess<TrinityModelExecutorAction.TrinityModelExecutorInputTable, TrinityModelExecutorAction.TrinityModelExecutorOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private final ObjectMapper mapper = new ObjectMapper();
    private static String httpClientTimeout = new String();

    public TrinityModelExecutorConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        httpClientTimeout = Optional.ofNullable(action.getContext().get("okhttp.client.timeout")).orElse("100");
    }

    @Override
    public List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> process(URL endpoint, TrinityModelExecutorAction.TrinityModelExecutorInputTable entity) throws Exception {

        List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> parentObj = new ArrayList<>();

        final String filePath = entity.getFilePath();
        final String paperType = entity.getPaperType();
        final String modelRegistry = entity.getModelRegistry();
        final Long tenantId = entity.getTenantId();

        final List<String> questions = entity.getQuestions();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "1. preparing {} for rest api call ", questions.size());
            log.info(aMarker, "2. info's are {}, {}, {}, {}", filePath, paperType, questions, modelRegistry);
        }
        String tritonRequestActivator = action.getContext().get("triton.request.activator");

        String endpointURL = endpoint.toString();

        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator : {} , Copro API running in legacy mode", tritonRequestActivator);
            return coproRequestBuilder(endpointURL, filePath, paperType, questions, modelRegistry, mapper, parentObj, tenantId);
        } else {
            log.info("Triton request activator : {} , Copro API running in triton mode", tritonRequestActivator);
            return tritonRequestBuilder(endpointURL, filePath, paperType, questions, modelRegistry, mapper, parentObj, tenantId);
        }
    }

    public String getHttpClientTimeout() {
        return httpClientTimeout;
    }

    private List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> tritonRequestBuilder(String node, String filePath, String paperType, List<String> questions, String modelRegistry, ObjectMapper objectMapper, List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> parentObj, Long tenantId) throws JsonProcessingException {
        final String trinityModelResultLineItems = new TrinityModelExecutorApiCaller(this, node, log).computeTriton(filePath, paperType, questions, modelRegistry, tenantId, action);
        TrinityModelResponse trinityModelResponse = objectMapper.readValue(trinityModelResultLineItems, new TypeReference<>() {
        });
        trinityModelResponse.getOutputs().forEach(trinityModelOutput -> trinityModelOutput.getData().forEach(trinityModelResultLineItem -> {
            parentObj.addAll(extractedTritonOuputDataResponse(trinityModelResultLineItem, filePath, tenantId, paperType, trinityModelResponse.getModelName(), trinityModelResponse.getModelVersion(), modelRegistry, objectMapper, parentObj));
        }));
        return parentObj;
    }

    private List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> coproRequestBuilder(String node, String filePath, String paperType, List<String> questions, String modelRegistry, ObjectMapper objectMapper, List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> parentObj, Long tenantId) throws JsonProcessingException {
        final String trinityModelResultLineItems = new TrinityModelExecutorApiCaller(this, node, log).computeCopro(filePath, paperType, questions, modelRegistry, tenantId, action);
        return extractedCoproOutputResponse(trinityModelResultLineItems, filePath, tenantId, paperType, "", modelRegistry, "", mapper, parentObj);
    }

    private List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> extractedTritonOuputDataResponse(String trinityModelDataItems, String filePath, Long tenantId, String paperType, String modelName, String modelVersion, String modelRegistry, ObjectMapper objectMapper, List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> parentObj) {

        try {
            TrinityModelDataItem trinityModelDataItem = objectMapper.readValue(trinityModelDataItems, new TypeReference<>() {
            });

            log.info("TrinityModelLineItem size {}", trinityModelDataItem.getAttributes().size());

            Lists.partition(trinityModelDataItem.getAttributes(), 100).forEach(resultLineItems -> {
                log.info(aMarker, "inserting into trinity model action {}", resultLineItems.size());
                resultLineItems.forEach(resultLineItem -> {
                    TrinityModelExecutorAction.TrinityModelExecutorOutputTable trinityModelExecutorOutputTable =
                            TrinityModelExecutorAction.TrinityModelExecutorOutputTable.builder()
                                    .processId(action.getProcessId())
                                    .filePath(filePath)
                                    .question(resultLineItem.getQuestion())
                                    .predictedAttributionValue(resultLineItem.getPredictedAttributionValue())
//                                    .bBox(resultLineItem.getBboxes())
                                    .imageDPI(trinityModelDataItem.getImageDPI())
                                    .imageWidth(trinityModelDataItem.getImageWidth())
                                    .imageHeight(trinityModelDataItem.getImageHeight())
                                    .extractedImageUnit(trinityModelDataItem.getExtractedImageUnit())
                                    .actionId(action.getActionId())
                                    .rootPipelineId(action.getRootPipelineId())
                                    .status("COMPLETED")
                                    .stage("VQA_TRANSACTION")
                                    .paperType(paperType)
                                    .score(Double.valueOf(resultLineItem.getScores()))
                                    .modelName(modelName)
                                    .modelVersion(modelVersion)
                                    .tenantId(tenantId)
                                    .modelRegistry(modelRegistry)
                                    .build();
                    parentObj.add(trinityModelExecutorOutputTable);
                });
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to insert into output table with exception {}", e.getMessage());
            throw new HandymanException("Failed to insert into output table ", e);
        }
        log.info("result object converted {}", parentObj.size());
        return parentObj;
    }

    private List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> extractedCoproOutputResponse(String trinityModelDataItems, String filePath, Long
            tenantId, String paperType, String modelRegistry, String modelName, String modelVersion, ObjectMapper objectMapper, List<TrinityModelExecutorAction.TrinityModelExecutorOutputTable> parentObj) {

        try {
            TrinityModelDataItemCopro trinityModelDataItem = objectMapper.readValue(trinityModelDataItems, new TypeReference<>() {
            });

            log.info("TrinityModelLineItem size {}", trinityModelDataItem.getAttributes().size());
            Lists.partition(trinityModelDataItem.getAttributes(), 100).forEach(resultLineItems -> {
                log.info(aMarker, "inserting into trinity model action {}", resultLineItems.size());
                resultLineItems.forEach(resultLineItem -> {
                    TrinityModelExecutorAction.TrinityModelExecutorOutputTable trinityModelExecutorOutputTable =
                            TrinityModelExecutorAction.TrinityModelExecutorOutputTable.builder()
                                    .processId(action.getProcessId())
                                    .filePath(filePath)
                                    .question(resultLineItem.getQuestion())
                                    .predictedAttributionValue(resultLineItem.getPredictedAttributionValue())
                                    .bBox(resultLineItem.getBboxes())
                                    .imageDPI(trinityModelDataItem.getImageDPI())
                                    .imageWidth(trinityModelDataItem.getImageWidth())
                                    .imageHeight(trinityModelDataItem.getImageHeight())
                                    .extractedImageUnit(trinityModelDataItem.getExtractedImageUnit())
                                    .actionId(action.getActionId())
                                    .rootPipelineId(action.getRootPipelineId())
                                    .status("COMPLETED")
                                    .stage("VQA_TRANSACTION")
                                    .paperType(paperType)
                                    .score(Double.valueOf(resultLineItem.getScores()))
                                    .modelName(modelName)
                                    .modelVersion(modelVersion)
                                    .tenantId(tenantId)
                                    .modelRegistry(modelRegistry)
                                    .build();
                    parentObj.add(trinityModelExecutorOutputTable);
                });
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to insert into output table with exception {}", e.getMessage());
            throw new HandymanException("Failed to insert into output table ", e);
        }
        return parentObj;
    }


}

