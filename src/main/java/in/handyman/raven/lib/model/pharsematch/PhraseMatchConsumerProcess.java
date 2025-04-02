package in.handyman.raven.lib.model.pharsematch;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.pharsematch.copro.PharseMatchDataItemCopro;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PhraseMatchConsumerProcess implements CoproProcessor.ConsumerProcess<PhraseMatchInputTable, PhraseMatchOutputTable> {
    public static final String FILTER_ZSC_PAGE_CONTENT_LOWER = "filter.zsc.page.content.lower";
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = PipelineName.PHRASE_MATCH.getProcessName();

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();
    public static final String PIPELINE_REQ_RES_ENCRYPTION = "pipeline.req.res.encryption";

    public PhraseMatchConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<PhraseMatchOutputTable> process(URL endpoint, PhraseMatchInputTable entity) throws JsonProcessingException {
        List<PhraseMatchOutputTable> parentObj = new ArrayList<>();
        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();
        String paperNo = String.valueOf(entity.getPaperNo());
        Long actionId = action.getActionId();
        String pageContent = String.valueOf(entity.getPageContent());
        String batchId = entity.getBatchId();

        String decryptedContent;

        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        String encryptSotPageContent = action.getContext().get("pipeline.text.extraction.encryption");

        if (Objects.equals(encryptSotPageContent, "true")) {
            decryptedContent = encryption.decrypt(pageContent, "AES256", "PM_TEXT_INPUT");
        } else {
            decryptedContent = pageContent;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> keysToFilterObject = objectMapper.readValue(entity.getTruthPlaceholder(), new TypeReference<>() {
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

        PharseMatchData data = new PharseMatchData();
        data.setRootPipelineId(Math.toIntExact(action.getRootPipelineId()));
        data.setActionId(actionId);
        data.setProcess(entity.getProcessId());
        data.setOriginId(originId);
        data.setPaperNo(Long.valueOf(paperNo));
        data.setGroupId(groupId);
        data.setPageContent(decryptedPageContentLower);
        data.setKeysToFilter(keysToFilterObject);
        data.setProcess(PROCESS_NAME);
        data.setBatchId(batchId);


        String jsonInputRequest = objectMapper.writeValueAsString(data);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PM START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = action.getContext().get("triton.request.activator");


        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();

            String jsonInputRequestEnc = encryptRequestResponse(jsonInputRequest);
            coproRequestBuilder(entity, parentObj, request, objectMapper, jsonInputRequestEnc, endpoint);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            String jsonInputRequestEnc = encryptRequestResponse(jsonRequest);
            tritonRequestBuilder(entity, parentObj, request, jsonInputRequestEnc, endpoint);
        }

        return parentObj;
    }

    private void coproRequestBuilder(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, Request request, ObjectMapper objectMapper, String jsonInputRequest, URL endpoint) {
        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long tenantId = entity.getTenantId();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, parentObj, responseBody, objectMapper, "", "", jsonInputRequest, responseBody, endpoint.toString());

            } else {
                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .tenantId(tenantId)
                                .paperNo(paperNo)
                                .stage(PROCESS_NAME)
                                .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .request(jsonInputRequest)
                                .response(encryptRequestResponse(response.message()))
                                .endpoint(String.valueOf(endpoint))
                                .build());
                log.info(aMarker, "The Exception occurred in Phrase match API call");
            }

        } catch (Exception exception) {
            parentObj.add(
                    PhraseMatchOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .paperNo(paperNo)
                            .tenantId(tenantId)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .createdOn(entity.getCreatedOn())
                            .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                            .request(encryptRequestResponse(jsonInputRequest))
                            .response("Error in response")
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.error(aMarker, "Exception occurred in the phrase match paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error in inserting Intellimatch result table", handymanException, this.action);

        }
    }


    private void tritonRequestBuilder(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, Request request, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();


            if (response.isSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                PharseMatchResponse modelResponse = objectMapper.readValue(responseBody, PharseMatchResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(phraseMatchDataItem -> extractedOutputDataRequest(entity, parentObj, phraseMatchDataItem, objectMapper, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString())));
                } else {
                    parentObj.add(
                            PhraseMatchOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                    .tenantId(tenantId)
                                    .paperNo(paperNo)
                                    .stage(PROCESS_NAME)
                                    .message(encryptRequestResponse(response.message()))
                                    .rootPipelineId(rootPipelineId)
                                    .batchId(entity.getBatchId())
                                    .createdOn(entity.getCreatedOn())
                                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                    .request(jsonRequest)
                                    .response(encryptRequestResponse(response.message()))
                                    .endpoint(String.valueOf(endpoint))
                                    .build());
                    log.info(aMarker, "The Exception occurred in Phrase match API call");
                }
            }
        } catch (Exception exception) {
            parentObj.add(
                    PhraseMatchOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .tenantId(tenantId)
                            .paperNo(paperNo)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .request(encryptRequestResponse(jsonRequest))
                            .response("Error In Response")
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.error(aMarker, "Exception occurred in the phrase match paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error in inserting Intellimatch result table", handymanException, this.action);

        }
    }

    private void extractedOutputDataRequest(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, String phraseMatchDataItem, ObjectMapper objectMapper, String modelName, String modelVersion, String request, String response, String endpoint) {
        Long tenantId = entity.getTenantId();

        Long rootPipelineId = entity.getRootPipelineId();
        try {
            List<PharseMatchDataItem> phraseMatchOutputData = objectMapper.readValue(phraseMatchDataItem, new TypeReference<>() {

            });
            String phraseMatchDataItemEnc = encryptRequestResponse(phraseMatchDataItem);

            for (PharseMatchDataItem item : phraseMatchOutputData) {
                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(item.getOriginId())
                                .groupId(item.getGroupId())
                                .paperNo(item.getPaperNo())
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .tenantId(tenantId)
                                .stage(PROCESS_NAME)
                                .message("Completed API call phrase match")
                                .rootPipelineId(rootPipelineId)
                                .modelName(modelName)
                                .truthEntity(item.getTruthEntity())
                                .isKeyPresent(String.valueOf(item.getIsKeyPresent()))
                                .entity(item.getEntity())
                                .modelVersion(modelVersion)
                                .batchId(item.getBatchId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .request(request)
                                .response(phraseMatchDataItemEnc)
                                .endpoint(endpoint)
                                .build());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void extractedCoproOutputResponse(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, String phraseMatchDataItem, ObjectMapper objectMapper, String modelName, String modelVersion, String request, String response, String endpoint) {
        Long tenantId = entity.getTenantId();

        Long rootPipelineId = entity.getRootPipelineId();
        try {
            List<PharseMatchDataItemCopro> phraseMatchOutputData = objectMapper.readValue(phraseMatchDataItem, new TypeReference<>() {
            });

            String phraseMatchDataItemEnc = encryptRequestResponse(phraseMatchDataItem);


            for (PharseMatchDataItemCopro item : phraseMatchOutputData) {

                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(Optional.ofNullable(item.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(item.getGroupId()).map(String::valueOf).orElse(null))
                                .paperNo(item.getPaperNo())
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .tenantId(tenantId)
                                .stage(PROCESS_NAME)
                                .message("Completed API call phrase match")
                                .rootPipelineId(rootPipelineId)
                                .modelName(modelName)
                                .truthEntity(item.getTruthEntity())
                                .isKeyPresent(String.valueOf(item.getIsKeyPresent()))
                                .entity(item.getEntity())
                                .modelVersion(modelVersion)
                                .batchId(entity.getBatchId())
                                .createdOn(entity.getCreatedOn())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .response(phraseMatchDataItemEnc)
                                .request(request)
                                .endpoint(endpoint)
                                .build());
            }

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

    @NotNull
    private String normalizeCaseToLower(String pageContent) {
        if("true".equals(action.getContext().get(FILTER_ZSC_PAGE_CONTENT_LOWER))){
            pageContent = pageContent.toLowerCase();
            log.info("Converted the input string content into lower");
            return pageContent;
        }
        return pageContent;
    }
}

