package in.handyman.raven.lib.model.zeroshotclassifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.model.zeroshotclassifier.copro.ZeroShotClassifierDataItemCopro;
import in.handyman.raven.lib.model.zeroshotclassifier.replicate.ZeroShotClassifierReplicate;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.PropertyHandler;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class ZeroShotConsumerProcess implements CoproProcessor.ConsumerProcess<ZeroShotClassifierInputTable, ZeroShotClassifierOutputTable> {
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String ZSC_START = "ZSC START";
    public static final String FILTER_ZSC_PAGE_CONTENT_LOWER = "filter.zsc.page.content.lower";
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public static final String PIPELINE_REQ_RES_ENCRYPTION = "pipeline.req.res.encryption";
    private static final String PROCESS_NAME = PipelineName.ZERO_SHOT_CLASSIFIER.getProcessName();
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.activator.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String REPLICATE_ZSC_VERSION = "replicate.zsc.version";

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public ZeroShotConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) throws JsonMappingException, JsonProcessingException {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<ZeroShotClassifierOutputTable> process(URL endpoint, ZeroShotClassifierInputTable entity) throws JsonProcessingException {
        List<ZeroShotClassifierOutputTable> parentObj = new ArrayList<>();
        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);
        String replicateZscVersion = action.getContext().get(REPLICATE_ZSC_VERSION);

        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();
        Long rootPipelineId = action.getRootPipelineId();
        String processId = String.valueOf(entity.getProcessId());
        String paperNo = String.valueOf(entity.getPaperNo());
        Long actionId = action.getActionId();


        String pageContent = String.valueOf(entity.getPageContent());
        String decryptedContent;
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        String encryptSotPageContent = action.getContext().get("pipeline.text.extraction.encryption");

        if (Objects.equals(encryptSotPageContent, "true")) {
            decryptedContent = encryption.decrypt(pageContent, "AES256", "ZSC_TEXT_DATA");
        } else {
            decryptedContent = pageContent;
        }


        String batchId = entity.getBatchId();
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, List<String>> keysToFilterObject = objectMapper.readValue(entity.getTruthPlaceholder(), new TypeReference<Map<String, List<String>>>() {
        });
        if("true".equals(action.getContext().get(FILTER_ZSC_PAGE_CONTENT_LOWER))){
            keysToFilterObject.replaceAll((key, valueList) ->
                    valueList.stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList())
            );
        }


        //payload
        String decryptedPageContentLower = normalizeCaseToLower(decryptedContent);
        ZeroShotClassifierData data = new ZeroShotClassifierData();
        data.setProcess(PROCESS_NAME);
        data.setProcessId(processId);
        data.setRootPipelineId(String.valueOf(rootPipelineId));
        data.setActionId(actionId);
        data.setProcess(entity.getProcessId());
        data.setOriginId(originId);
        data.setPaperNo(paperNo);
        data.setGroupId(groupId);
        data.setPageContent(decryptedPageContentLower);
        data.setKeysToFilter(keysToFilterObject);
        data.setBatchId(batchId);
        String jsonInputRequest = objectMapper.writeValueAsString(data);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(ZSC_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));


        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("COPRO", coproHandlerName)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
            String jsonInputRequestEnc = encryptRequestResponse(jsonInputRequest);
            coproRequestBuilder(entity, parentObj, request, objectMapper, jsonInputRequestEnc, endpoint);

        } else if (Objects.equals("RUNPOD", coproHandlerName)) {
            ZeroShotClassifierReplicate zeroShotClassifierReplicateData = new ZeroShotClassifierReplicate();
            zeroShotClassifierReplicateData.setProcess(PROCESS_NAME);
            zeroShotClassifierReplicateData.setProcessId(processId);
            zeroShotClassifierReplicateData.setRootPipelineId(rootPipelineId);
            zeroShotClassifierReplicateData.setActionId("1");
            zeroShotClassifierReplicateData.setProcess(entity.getProcessId());
            zeroShotClassifierReplicateData.setOriginId(originId);
            zeroShotClassifierReplicateData.setPaperNo(Integer.parseInt(paperNo));
            zeroShotClassifierReplicateData.setGroupId(Integer.valueOf(groupId));
            zeroShotClassifierReplicateData.setPageContent(pageContent);
            zeroShotClassifierReplicateData.setKeysToFilter(entity.getTruthPlaceholder());
            zeroShotClassifierReplicateData.setBatchId(batchId);

            ReplicateRequest replicateRequest=new ReplicateRequest();
//            replicateRequest.setVersion(replicateZscVersion);
            replicateRequest.setInput(zeroShotClassifierReplicateData);
            String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);


            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(replicateJsonRequest, MediaTypeJSON))
                    .addHeader("Authorization", "Bearer " + PropertyHandler.get("runpod.api.token.v1"))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "wait")
                    .build();
            String jsonRseponseEnc = encryptRequestResponse(jsonRequest);
            replicateRequestBuilder(entity, parentObj,request, jsonRseponseEnc, endpoint );
        }
        else if (Objects.equals("TRITON", coproHandlerName)){
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

            String jsonRequestEnc = encryptRequestResponse(jsonRequest);
            tritonRequestBuilder(entity, parentObj, request, jsonRequestEnc, endpoint);
        }
        log.info("Total number of entries in the parent object: {}", parentObj.size());
        return parentObj;
    }

    @NotNull
    private String normalizeCaseToLower(String pageContent) {
        if("true".equals(action.getContext().get(FILTER_ZSC_PAGE_CONTENT_LOWER))){
            pageContent = pageContent.toLowerCase();
            log.info("Converted the input string content into lower");
            return pageContent;
        }
        return pageContent;
    }


    private void replicateRequestBuilder(ZeroShotClassifierInputTable entity, List<ZeroShotClassifierOutputTable> parentObj, Request request, String jsonRequest, URL endpoint) {
        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();

        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            JsonNode rootNode = mapper.readTree(responseBody);
            JsonNode outputNode = rootNode.path("output");

            if (response.isSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();

                if (outputNode != null && !outputNode.isEmpty()) {
                    String jsonResponseEnc = encryptRequestResponse(responseBody);
                    extractedOutputDataRequest(entity, parentObj, String.valueOf(outputNode), objectMapper, "REPLICATE", "", jsonRequest, jsonResponseEnc, String.valueOf(endpoint));

                }
            } else {
                parentObj.add(
                        ZeroShotClassifierOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .paperNo(paperNo)
                                .stage(PROCESS_NAME)
                                .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                                .tenantId(entity.getTenantId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .request(jsonRequest)
                                .response(response.message())
                                .endpoint(String.valueOf(endpoint))
                                .build());
                log.error(aMarker, "Exception occurred in zero shot classifier replicate API call");
            }
        } catch (Exception exception) {
            parentObj.add(
                    ZeroShotClassifierOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .paperNo(paperNo)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .tenantId(entity.getTenantId())
                            .createdOn(entity.getCreatedOn())
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .request(jsonRequest)
                            .response("Error in Response")
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.error(aMarker, "Exception occurred in the zero shot classifier paper filter replicate action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Zero shot classifier paper filter replicate action failed for groupId - " + groupId + "and originId - " + originId + "and paperNo -" + paperNo, handymanException, action);
        }
    }


    private void tritonRequestBuilder(ZeroShotClassifierInputTable entity, List<ZeroShotClassifierOutputTable> parentObj, Request request, String jsonRequest, URL endpoint) {
        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();

        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ZeroShotClassifierModelResponse modelResponse = objectMapper.readValue(responseBody, ZeroShotClassifierModelResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(zeroShotClassifierDataItem -> {

                            extractedOutputDataRequest(entity, parentObj, zeroShotClassifierDataItem, objectMapper, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, String.valueOf(endpoint));
                        });
                    });
                }
            } else {
                parentObj.add(
                        ZeroShotClassifierOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .paperNo(paperNo)
                                .stage(PROCESS_NAME)
                                .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                                .tenantId(entity.getTenantId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .request(jsonRequest)
                                .response(encryptRequestResponse(response.message()))
                                .endpoint(String.valueOf(endpoint))
                                .build());
                log.error(aMarker, "Exception occurred in zero shot classifier API call");
            }
        } catch (Exception exception) {
            parentObj.add(
                    ZeroShotClassifierOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .paperNo(paperNo)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .tenantId(entity.getTenantId())
                            .createdOn(entity.getCreatedOn())
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .request(jsonRequest)
                            .response(encryptRequestResponse(exception.getMessage()))
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.error(aMarker, "Exception occurred in the zero shot classifier paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Zero shot classifier paper filter action failed for groupId - " + groupId + "and originId - " + originId + "and paperNo -" + paperNo, handymanException, action);
        }
    }

    private void coproRequestBuilder(ZeroShotClassifierInputTable entity, List<ZeroShotClassifierOutputTable> parentObj, Request request, ObjectMapper objectMapper, String jsonInputRequest, URL endpoint) {

        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();

        final Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, parentObj, responseBody, objectMapper, "", "", jsonInputRequest, responseBody, endpoint.toString());

            } else {
                parentObj.add(
                        ZeroShotClassifierOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .paperNo(paperNo)
                                .tenantId(entity.getTenantId())
                                .stage(PROCESS_NAME)
                                .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                                .tenantId(entity.getTenantId())
                                .createdOn(entity.getCreatedOn())
                                .request(jsonInputRequest)
                                .response(encryptRequestResponse(response.message()))
                                .endpoint(String.valueOf(endpoint))
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .build());
                log.error(aMarker, "Exception occurred in zero shot classifier API call");
            }
        } catch (Exception exception) {
            parentObj.add(
                    ZeroShotClassifierOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .paperNo(paperNo)
                            .tenantId(entity.getTenantId())
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .tenantId(entity.getTenantId())
                            .createdOn(entity.getCreatedOn())
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .request(encryptRequestResponse(jsonInputRequest))
                            .response("Error in Response")
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.error(aMarker, "Exception occurred in the zero shot classifier paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Zero shot classifier paper filter action failed for groupId - " + groupId + "and originId - " + originId + "and paperNo -" + paperNo, handymanException, action);
        }
    }

    private void extractedOutputDataRequest(ZeroShotClassifierInputTable entity, List<ZeroShotClassifierOutputTable> parentObj, String zeroShotClassifierDataItem, ObjectMapper objectMapper, String modelName, String modelVersion, String request, String response, String endpoint) {

        Long rootPipelineId = entity.getRootPipelineId();

        try {
            ZeroShotClassifierDataItem zeroShotClassifierOutputData = objectMapper.readValue(zeroShotClassifierDataItem, ZeroShotClassifierDataItem.class);
            String zeroShotClassifierDataItemEnc = encryptRequestResponse(zeroShotClassifierDataItem);
            zeroShotClassifierOutputData.getEntityConfidenceScore().forEach(score -> {
                String truthEntity = score.getTruthEntity();
                String key = score.getKey();
                double scoreValue = score.getScore();

                parentObj.add(ZeroShotClassifierOutputTable
                        .builder()
                        .originId(zeroShotClassifierOutputData.getOriginId())
                        .groupId(zeroShotClassifierOutputData.getGroupId())
                        .truthEntity(Optional.ofNullable(truthEntity).map(String::valueOf).orElse(null))
                        .entity(Optional.ofNullable(key).map(String::valueOf).orElse(null))
                        .confidenceScore(Optional.of(scoreValue).map(String::valueOf).orElse(null))
                        .paperNo(zeroShotClassifierOutputData.getPaperNo())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .tenantId(entity.getTenantId())
                        .message("Completed API call zero shot classifier")
                        .rootPipelineId(rootPipelineId)
                        .modelName(modelName)
                        .modelVersion(modelVersion)
                        .batchId(entity.getBatchId())
                        .tenantId(entity.getTenantId())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .request(request)
                        .response(zeroShotClassifierDataItemEnc)
                        .endpoint(endpoint)
                        .build());
            });
        } catch (JsonProcessingException e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Zero shot classifier paper filter action failed in response json processing - ", handymanException, action);
        }
    }

    private void extractedCoproOutputResponse(ZeroShotClassifierInputTable entity, List<ZeroShotClassifierOutputTable> parentObj, String zeroShotClassifierDataItem, ObjectMapper objectMapper, String modelName, String modelVersion, String request, String response, String endpoint) {

        Long rootPipelineId = entity.getRootPipelineId();

        try {
            ZeroShotClassifierDataItemCopro zeroShotClassifierDataItemCopro = objectMapper.readValue(zeroShotClassifierDataItem, ZeroShotClassifierDataItemCopro.class);
            String zeroShotClassifierDataItemEnc = encryptRequestResponse(zeroShotClassifierDataItem);
            zeroShotClassifierDataItemCopro.getEntityConfidenceScore().forEach(score -> {
                String truthEntity = score.getTruthEntity();
                String key = score.getKey();
                double scoreValue = score.getScore();

                parentObj.add(ZeroShotClassifierOutputTable
                        .builder()
                        .originId(Optional.ofNullable(zeroShotClassifierDataItemCopro.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(Optional.ofNullable(zeroShotClassifierDataItemCopro.getGroupId()).map(String::valueOf).orElse(null))
                        .truthEntity(Optional.ofNullable(truthEntity).map(String::valueOf).orElse(null))
                        .entity(Optional.ofNullable(key).map(String::valueOf).orElse(null))
                        .confidenceScore(Optional.of(scoreValue).map(String::valueOf).orElse(null))
                        .paperNo(zeroShotClassifierDataItemCopro.getPaperNo())
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .tenantId(entity.getTenantId())
                        .message("Completed API call zero shot classifier")
                        .rootPipelineId(rootPipelineId)
                        .modelName(modelName)
                        .modelVersion(modelVersion)
                        .batchId(entity.getBatchId())
                        .tenantId(entity.getTenantId())
                        .createdOn(entity.getCreatedOn())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .request(request)
                        .response(zeroShotClassifierDataItemEnc)
                        .endpoint(endpoint)
                        .build());
            });
        } catch (JsonProcessingException e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Zero shot classifier paper filter action failed in response json processing - ", handymanException, action);
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
}



