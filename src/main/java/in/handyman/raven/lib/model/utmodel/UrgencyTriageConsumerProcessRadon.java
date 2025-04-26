package in.handyman.raven.lib.model.utmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.model.utmodel.copro.UrgencyTriageModelDataItemCopro;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.PropertyHandler;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.UrgencyTriageModelAction.urgencyTriageModel;

public class UrgencyTriageConsumerProcessRadon implements CoproProcessor.ConsumerProcess<UrgencyTriageInputTable, UrgencyTriageOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String URGENCY_TRIAGE_PROCESS_NAME = PipelineName.URGENCY_TRIAGE.getProcessName();
    public static final String RADON_START = "RADON START";
    public static final String URGENCY_TRIAGE_MODEL = "URGENCY_TRIAGE_MODEL";
    public static final String PIPELINE_REQ_RES_ENCRYPTION = "pipeline.req.res.encryption";
    private static final String PROCESS_NAME = PipelineName.ZERO_SHOT_CLASSIFIER.getProcessName();
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.activator.handler.name";

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final MediaType mediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();


    public UrgencyTriageConsumerProcessRadon(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<UrgencyTriageOutputTable> process(URL endpoint, UrgencyTriageInputTable entity) throws Exception {
        List<UrgencyTriageOutputTable> parentObj = new ArrayList<>();
        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);


        String inputFilePath = entity.getInputFilePath();
        String outputDir = urgencyTriageModel.getOutputDir();

        ObjectMapper objectMapper = new ObjectMapper();

//payload
        UrgencyTriageModelPayload urgencyTriageModelPayload = new UrgencyTriageModelPayload();
        urgencyTriageModelPayload.setRootPipelineId(entity.getRootPipelineId());
        urgencyTriageModelPayload.setProcess(PipelineName.URGENCY_TRIAGE.getProcessName());
        urgencyTriageModelPayload.setInputFilePath(entity.getInputFilePath());
        urgencyTriageModelPayload.setActionId(action.getActionId());
        urgencyTriageModelPayload.setOutputDir(outputDir);
        urgencyTriageModelPayload.setTenantId(entity.getTenantId());
        urgencyTriageModelPayload.setProcessId(entity.getProcessId());
        urgencyTriageModelPayload.setGroupId(entity.getGroupId());
        urgencyTriageModelPayload.setPaperNo(entity.getPaperNo());
        urgencyTriageModelPayload.setOriginId(entity.getOriginId());
        urgencyTriageModelPayload.setUserPrompt(entity.getUserPrompt());
        urgencyTriageModelPayload.setSystemPrompt(entity.getSystemPrompt());
        urgencyTriageModelPayload.setBatchId(entity.getBatchId());
        urgencyTriageModelPayload.setBase64Img(entity.getBase64img());

        String jsonInputRequest = objectMapper.writeValueAsString(urgencyTriageModelPayload);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(RADON_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("COPRO", coproHandlerName)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();

            String jsonRequestEnc = encryptRequestResponse(jsonRequest);
            coproRequestBuider(entity, request, objectMapper, parentObj, jsonRequestEnc);
        } else if (Objects.equals("RUNPOD", coproHandlerName)) {
            ReplicateRequest replicateRequest=new ReplicateRequest();
//            replicateRequest.setVersion(replicateUrgencyTriageVersion);
            replicateRequest.setInput(urgencyTriageModelPayload);
            String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(replicateJsonRequest, mediaTypeJSON))
                    .addHeader("Authorization", "Bearer " + PropertyHandler.get("runpod.api.token.v1"))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "wait")
                    .build();
            log.info("request\n\n\n"+ request+ "\n\n\n");
            log.info("replicateJsonRequest\n\n"+ replicateJsonRequest+ "\n\n");

            String jsonRequestEnc = encryptRequestResponse(replicateJsonRequest);
            replicateRequestBuilder(entity,request, parentObj , jsonRequestEnc, endpoint, objectMapper);
        }else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
            String jsonRequestEnc = encryptRequestResponse(jsonRequest);
            tritonRequestBuilder(entity, request, objectMapper, parentObj, jsonRequestEnc);
        }

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n coproUrl  {} ,inputFilePath : {} ,outputDir {} ", endpoint, inputFilePath, outputDir);
        }

        log.info("Total number of entries in the parent object: {}", parentObj.size());
        return parentObj;
    }

    private void tritonRequestBuilder(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String jsonRequestEnc) {
        String createdUserId = entity.getCreatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Long groupId = Long.valueOf(entity.getGroupId());
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                UrgencyTriageModelResponse modelResponse = objectMapper.readValue(responseBody, UrgencyTriageModelResponse.class);

                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(urgencyTriageModelDataItem -> {

                            extractedOutputRequest(entity, urgencyTriageModelDataItem, objectMapper, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());

                            log.info(aMarker, "Execute for urgency triage {}", response.isSuccessful());
                        });
                    });
                }
            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(createdUserId)
                        .lastUpdatedUserId(createdUserId)
                        .tenantId(tenantId)
                        .processId(processId)
                        .groupId(groupId)
                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .templateId(templateId)
                        .modelId(modelId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(URGENCY_TRIAGE_MODEL)
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .request(jsonRequestEnc)
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}", response);
            }
        } catch (Exception e) {
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .groupId(groupId)
                    .processId(processId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_MODEL)
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
        }
    }

    private static void extractedOutputRequest(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String modelName, String modelVersion) {
        String createdUserId = entity.getCreatedUserId();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try {
            RadonKvpLineItem modelResponse = objectMapper.readValue(urgencyTriageModelDataItem, RadonKvpLineItem.class);

//            UrgencyTriageModelDataItem urgencyTriageModelDataItem1 = objectMapper.readValue(urgencyTriageModelDataItem, UrgencyTriageModelDataItem.class);
            JsonNode qrBoundingBox = objectMapper.valueToTree(modelResponse.getBboxes());
            Float confScore = Float.valueOf(modelResponse.getConfidenceScore() == null ? 0 : modelResponse.getConfidenceScore());
            String paperType = modelResponse.getInferResponse();
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(modelResponse.getTenantId())
                    .processId(modelResponse.getProcessId())
                    .groupId(modelResponse.getGroupId())
                    .originId(modelResponse.getOriginId())
                    .paperNo(modelResponse.getPaperNo())
                    .templateId(templateId)
                    .modelId(modelId)
                    .utResult(paperType)
                    .confScore(confScore)
                    .bbox(qrBoundingBox.toString())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private void coproRequestBuider(UrgencyTriageInputTable entity, Request request, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String jsonRequest) {
        try (Response response = httpclient.newCall(request).execute()) {
            final String responseBody = Objects.requireNonNull(response.body()).string();
            String createdUserId = entity.getCreatedUserId();
            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Long groupId = Long.valueOf(entity.getGroupId());
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();
            if (response.isSuccessful()) {
                log.info("Response Details: {}", response);
                extractedCoproOutputResponse(entity, responseBody, objectMapper, parentObj, "", "");

            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .createdUserId(createdUserId)
                        .lastUpdatedUserId(createdUserId)
                        .tenantId(tenantId)
                        .processId(processId)
                        .groupId(groupId)
                        .originId(originId)
                        .paperNo(paperNo)
                        .templateId(templateId)
                        .modelId(modelId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(URGENCY_TRIAGE_PROCESS_NAME)
                        .message(response.message())
                        .rootPipelineId(entity.getRootPipelineId())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .request(jsonRequest)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .build());
                log.error(aMarker, "The Exception occurred in urgency triage {}", response);
            }
        } catch (Exception e) {
            String createdUserId = entity.getCreatedUserId();

            Long tenantId = entity.getTenantId();
            Long processId = entity.getProcessId();
            Long groupId = Long.valueOf(entity.getGroupId());
            String originId = entity.getOriginId();
            Integer paperNo = entity.getPaperNo();
            String templateId = entity.getTemplateId();
            Long modelId = entity.getModelId();

            parentObj.add(UrgencyTriageOutputTable.builder()

                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .groupId(groupId)
                    .processId(processId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
            log.error(aMarker, "The Exception occurred in urgency triage", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
        }
    }

    private static void extractedCoproOutputResponse(UrgencyTriageInputTable entity, String urgencyTriageModelDataItem, ObjectMapper objectMapper, List<UrgencyTriageOutputTable> parentObj, String modelName, String modelVersion) {
        String createdUserId = entity.getCreatedUserId();

        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Long groupId = Long.valueOf(entity.getGroupId());
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();
        try {
            UrgencyTriageModelDataItemCopro urgencyTriageModelDataItem1 = objectMapper.readValue(urgencyTriageModelDataItem, UrgencyTriageModelDataItemCopro.class);
            JsonNode qrBoundingBox = objectMapper.valueToTree(urgencyTriageModelDataItem1.getBboxes());
            Float confScore = urgencyTriageModelDataItem1.getConfidenceScore();
            String paperType = urgencyTriageModelDataItem1.getPaperType();
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .processId(processId)
                    .groupId(groupId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .utResult(paperType)
                    .confScore(confScore)
                    .bbox(qrBoundingBox.toString())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_PROCESS_NAME)
                    .message("Urgency Triage Finished")
                    .rootPipelineId(entity.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(PIPELINE_REQ_RES_ENCRYPTION);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }


    private void replicateRequestBuilder(UrgencyTriageInputTable entity, Request request, List<UrgencyTriageOutputTable> parentObj, String jsonRequest, URL endpoint, ObjectMapper objectMapper) {
        String createdUserId = entity.getCreatedUserId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        Long groupId = Long.valueOf(entity.getGroupId());
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();


        try (Response response = httpclient.newCall(request).execute()) {
            assert response.body() != null;
            String responseBody = response.body().string();
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode outputNode = rootNode.path("output");
            log.info("outputNode\n\n"+ outputNode+ "\n\n");

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                assert response.body() != null;

                if (!outputNode.isEmpty() && !outputNode.isNull()) {
                    extractReplicateOutputDataRequest(entity, outputNode, parentObj, "REPLICATE",  "1", jsonRequest, responseBody, endpoint.toString());
                }
            } else {
                parentObj.add(UrgencyTriageOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(Long.valueOf(groupId))
                        .tenantId(tenantId)
                        .processId(processId)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(URGENCY_TRIAGE_PROCESS_NAME)
                        .message(responseBody)
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(String.valueOf(entity.getTenantId()))
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(String.valueOf(entity.getTenantId()))
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response from replicate response {}", responseBody);
            }
        } catch (IOException e) {
            parentObj.add(UrgencyTriageOutputTable.builder()
                    .createdUserId(createdUserId)
                    .lastUpdatedUserId(createdUserId)
                    .tenantId(tenantId)
                    .processId(processId)
                    .groupId(groupId)
                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .templateId(templateId)
                    .modelId(modelId)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(URGENCY_TRIAGE_MODEL)
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("urgency triage consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from replicate server {}", ExceptionUtil.toString(e));
        }
    }


    private void extractReplicateOutputDataRequest(UrgencyTriageInputTable entity, JsonNode responseLineItems, List<UrgencyTriageOutputTable> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        String createdUserId = entity.getCreatedUserId();
        String templateId = entity.getTemplateId();
        Long modelId = entity.getModelId();

        UrgencyTriageReplicateDataLineItems urgencyTriageDataItem = mapper.treeToValue(responseLineItems, UrgencyTriageReplicateDataLineItems.class);
        parentObj.add(UrgencyTriageOutputTable.builder()
                .createdUserId(createdUserId)
                .lastUpdatedUserId(createdUserId)
                .tenantId(urgencyTriageDataItem.getTenantId())
                .processId(urgencyTriageDataItem.getProcessId())
                .groupId(Long.valueOf(urgencyTriageDataItem.getGroupId()))
                .originId(urgencyTriageDataItem.getOriginId())
                .paperNo(urgencyTriageDataItem.getPaperNo())
                .templateId(templateId)
                .modelId(modelId)
                .utResult(urgencyTriageDataItem.getInferResponse())
                .confScore(urgencyTriageDataItem.getConfidenceScore())
                .bbox(Optional.ofNullable(urgencyTriageDataItem.getBboxes()).map(Object::toString).orElse(null))
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(URGENCY_TRIAGE_PROCESS_NAME)
                .message("Urgency Triage Finished")
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(modelName)
                .modelVersion(modelVersion)
                .batchId(entity.getBatchId())
                .createdOn(entity.getCreatedOn())
                .endpoint(endpoint)
                .request(request)
                .response(response)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .build());
    }
}



