package in.handyman.raven.lib.model.agenticPaperFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionRequest;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.textextraction.DataExtractionData;
import in.handyman.raven.lib.model.textextraction.DataExtractionDataItem;
import in.handyman.raven.lib.model.textextraction.DataExtractionResponse;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    public static final String AGENTIC_PAPER_FILTER_START = "AGENTIC PAPER FILTER START";
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.agentic.paper.filter.extraction.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    private static final String PROCESS_NAME = "AGENTIC PAPER FILTER";
    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    private final int pageContentMinLength;
    final OkHttpClient httpclient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();

    private static final ObjectMapper mapper = new ObjectMapper();

    private final String processBase64;
    private final FileProcessingUtils fileProcessingUtils;


    public AgenticPaperFilterConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.pageContentMinLength = pageContentMinLength;
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);
        String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);

        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = entity.getRootPipelineId();
        String filePath = String.valueOf(entity.getFilePath());
        ObjectMapper objectMapper = new ObjectMapper();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        Integer paperNumber = entity.getPaperNo();
        String processId = String.valueOf(entity.getProcessId());
        Long tenantId = entity.getTenantId();
        Long actionId = action.getActionId();
        String batchId = entity.getBatchId();

        //payload
        DataExtractionData dataExtractionData = new DataExtractionData();
        dataExtractionData.setOriginId(originId);
        dataExtractionData.setGroupId(groupId);
        dataExtractionData.setProcessId(Long.valueOf(processId));
        dataExtractionData.setTenantId(tenantId);
        dataExtractionData.setRootPipelineId(rootPipelineId);
        dataExtractionData.setActionId(actionId);
        dataExtractionData.setPaperNumber(paperNumber);
        dataExtractionData.setTemplateName(entity.getTemplateName());
        dataExtractionData.setProcess(PROCESS_NAME);
        dataExtractionData.setInputFilePath(filePath);
        dataExtractionData.setBatchId(batchId);
        dataExtractionData.setBase64Img(entity.getBase64Img());

        String jsonInputRequest = objectMapper.writeValueAsString(dataExtractionData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(AGENTIC_PAPER_FILTER_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} ", endpoint, inputFilePath);
        }

        if (Objects.equals("COPRO", coproHandlerName)) {
            if (log.isInfoEnabled()) {
                log.info(aMarker, "Executing COPRO handler for endpoint: {}", endpoint);
            }

            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, mediaType)).build();
            coproRequestBuilder(entity, request, parentObj, originId, groupId, jsonInputRequest, endpoint);
        } else if (Objects.equals("REPLICATE", coproHandlerName)) {
            if (textExtractionModelName.equals(ModelRegistry.ARGON.name())) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Executing REPLICATE handler for endpoint: {} and model: {}", endpoint, textExtractionModelName);
                }
                String base64ForPath = getBase64ForPath(dataExtractionData.getInputFilePath());
                dataExtractionData.setBase64Img(base64ForPath);

                ReplicateRequest replicateRequest = new ReplicateRequest();
                replicateRequest.setInput(dataExtractionData);

                String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);
                Request request = new Request.Builder()
                        .url(endpoint)
                        .post(RequestBody.create(replicateJsonRequest, mediaType))
                        .addHeader("Authorization", "Bearer " + replicateApiToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "wait")
                        .build();

                replicateResponseBuilder(endpoint, request, parentObj, entity, replicateJsonRequest);
            } else if (textExtractionModelName.equals(ModelRegistry.KRYPTON.name())) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Executing REPLICATE handler for endpoint: {} and model: {}", endpoint, textExtractionModelName);
                }
                String base64ForPath = getBase64ForPath(dataExtractionData.getInputFilePath());

                RadonKvpExtractionRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
                kryptonRequestPayloadFromQuery.setBase64Img(base64ForPath);

                String textExtractionPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);

                ReplicateRequest replicateRequest = new ReplicateRequest();
                replicateRequest.setInput(textExtractionPayloadString);

                String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);

                Request request = new Request.Builder()
                        .url(endpoint)
                        .post(RequestBody.create(replicateJsonRequest, mediaType))
                        .addHeader("Authorization", "Bearer " + replicateApiToken)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "wait")
                        .build();
                tritonRequestKryptonExecutor(entity, request, parentObj, textExtractionPayloadString, endpoint);
            }
        } else if (Objects.equals("TRITON", coproHandlerName)) {
            log.info(aMarker, "Executing TRITON handler for endpoint: {} and model: {}", endpoint, textExtractionModelName);
            if (textExtractionModelName.equals(ModelRegistry.ARGON.name())) {
                DataExtractionData dataExtractionPayload = getArgonRequestPayloadFromEntity(entity);
                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    dataExtractionPayload.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
                } else {
                    dataExtractionPayload.setBase64Img("");
                }
                String dataExtractionPayloadString = mapper.writeValueAsString(dataExtractionPayload);

                String jsonRequestTritonArgon = getTritonRequestPayload(dataExtractionPayloadString, AGENTIC_PAPER_FILTER_START, mapper);

                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonArgon, mediaType)).build();
                tritonRequestArgonExecutor(entity, request, parentObj, jsonRequestTritonArgon, endpoint);
            } else if (textExtractionModelName.equals(ModelRegistry.KRYPTON.name())) {
                RadonKvpExtractionRequest kryptonRequestPayloadFromQuery = getKryptonRequestPayloadFromQuery(entity);
                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    kryptonRequestPayloadFromQuery.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
                } else {
                    kryptonRequestPayloadFromQuery.setBase64Img("");
                }
                String textExtractionPayloadString = mapper.writeValueAsString(kryptonRequestPayloadFromQuery);

                String jsonRequestTritonKrypton = getTritonRequestPayload(textExtractionPayloadString, KRYPTON_START, mapper);

                Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequestTritonKrypton, mediaType)).build();
                tritonRequestKryptonExecutor(entity, request, parentObj, jsonRequestTritonKrypton, endpoint);
            }
        }

        return parentObj;
    }

    private RadonKvpExtractionRequest getKryptonRequestPayloadFromQuery(AgenticPaperFilterInput entity) {
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();
        radonKvpExtractionRequest.setOriginId(entity.getOriginId());
        radonKvpExtractionRequest.setProcessId(entity.getProcessId());
        radonKvpExtractionRequest.setTenantId(entity.getTenantId());
        radonKvpExtractionRequest.setRootPipelineId(entity.getRootPipelineId());
        radonKvpExtractionRequest.setActionId(action.getActionId());
        radonKvpExtractionRequest.setProcess(PROCESS_NAME);
        radonKvpExtractionRequest.setInputFilePath(entity.getFilePath());
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());
        radonKvpExtractionRequest.setUserPrompt(entity.getUserPrompt());
        radonKvpExtractionRequest.setSystemPrompt(entity.getSystemPrompt());
        radonKvpExtractionRequest.setPaperNo(entity.getPaperNo());
        radonKvpExtractionRequest.setGroupId(Long.valueOf(entity.getGroupId()));

        return radonKvpExtractionRequest;
    }

    private void tritonRequestKryptonExecutor(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedKryptonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                            } catch (JsonProcessingException e) {
                                String errorMessage = "Error in parsing response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + " message : " + response.message();
                                HandymanException handymanException = new HandymanException(errorMessage);
                                HandymanException.insertException(errorMessage, handymanException, this.action);
                                log.error(aMarker, errorMessage);
                            }
                        });
                    });
                } else {
                    String errorMessage = "Successful response in consumer but output node not found for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + response.message();
                    parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, this.action);
                    log.error(aMarker, errorMessage);
                }
            } else {
                String errorMessage = "Unsuccessful response in consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + " code : " + response.code() + "\n message : " + response.message();
                parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                log.error(aMarker, errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "Error in api call consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo() + "\n message : " + e.getMessage();
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response("Error In Response").endpoint(String.valueOf(endpoint)).build());
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, this.action);
        }
    }


    private void extractedKryptonOutputDataRequest(AgenticPaperFilterInput entity, String stringDataItem,
                                                   List<AgenticPaperFilterOutput> parentObj, String modelName,
                                                   String modelVersion, String request, String response,
                                                   String endpoint) throws JsonProcessingException {

        String cleanedJson = stringDataItem.replaceAll("```json", "").replaceAll("```", "").trim();

        RadonKvpLineItem dataExtractionDataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);

        String inferResponseJson = dataExtractionDataItem.getInferResponse();
        JsonNode inferResponseNode = mapper.readTree(inferResponseJson);
        String formattedInferResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inferResponseNode);
        String flag = (inferResponseJson.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;

        String encryptSotPageContent = action.getContext().get(ENCRYPT_AGENTIC_FILTER_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        String extractedContent = Objects.equals(encryptSotPageContent, "true")
                ? encryption.encrypt(formattedInferResponse, "AES256", "TEXT_DATA")
                : formattedInferResponse;

        String templateId = entity.getTemplateId();

        Iterator<Map.Entry<String, JsonNode>> fields = inferResponseNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String containerName = entry.getKey();
            String containerValue = entry.getValue().asText();

            parentObj.add(AgenticPaperFilterOutput.builder()
                    .filePath(entity.getFilePath())
                    .extractedText(extractedContent)
                    .originId(dataExtractionDataItem.getOriginId())
                    .groupId(Math.toIntExact(dataExtractionDataItem.getGroupId()))
                    .paperNo(dataExtractionDataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Agentic Paper Filter macro completed with krypton triton api call")
                    .createdOn(entity.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .isBlankPage(flag)
                    .tenantId(dataExtractionDataItem.getTenantId())
                    .templateId(templateId)
                    .processId(dataExtractionDataItem.getProcessId())
                    .templateName(entity.getTemplateName())
                    .rootPipelineId(dataExtractionDataItem.getRootPipelineId())
                    .modelName(modelName)
                    .modelVersion(modelVersion)
                    .batchId(entity.getBatchId())
                    .request(encryptRequestResponse(request))
                    .response(encryptRequestResponse(response))
                    .endpoint(String.valueOf(endpoint))
                    .containerName(containerName)
                    .containerValue(containerValue)
                    .build());
        }
    }

    private void extractedArgonOutputDataRequest(AgenticPaperFilterInput entity, String stringDataItem, List<AgenticPaperFilterOutput> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        DataExtractionDataItem dataExtractionDataItem = mapper.readValue(stringDataItem, DataExtractionDataItem.class);

        final String contentString = Optional.of(dataExtractionDataItem.getPageContent()).map(String::valueOf).orElse(null);
        final String flag = (contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;

        String extractedContent;
        String encryptSotPageContent = action.getContext().get(ENCRYPT_AGENTIC_FILTER_OUTPUT);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContent = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContent = contentString;
        }

        String templateId = entity.getTemplateId();
        parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).filePath(dataExtractionDataItem.getInputFilePath()).extractedText(extractedContent).originId(dataExtractionDataItem.getOriginId()).groupId(dataExtractionDataItem.getGroupId()).paperNo(dataExtractionDataItem.getPaperNumber()).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Data extraction macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(dataExtractionDataItem.getTenantId()).templateId(templateId).processId(dataExtractionDataItem.getProcessId()).templateName(dataExtractionDataItem.getTemplateName()).rootPipelineId(dataExtractionDataItem.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).batchId(dataExtractionDataItem.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(String.valueOf(endpoint)).build());
    }

    private void tritonRequestArgonExecutor(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                DataExtractionResponse modelResponse = objectMappers.readValue(responseBody, DataExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(s -> {
                            try {
                                extractedArgonOutputDataRequest(entity, s, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), jsonRequest, responseBody, endpoint.toString());
                            } catch (JsonProcessingException e) {
                                throw new HandymanException("Exception in extracted output Data request {}", e);
                            }
                        });
                    });
                }
            } else {
                parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : " + response.code() + " message : " + response.message());
                HandymanException.insertException("Agentic paper filter consumer failed for batch/group " + entity.getGroupId() + "origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo(), handymanException, this.action);

                log.error(aMarker, "The Exception occurred in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response("Error In getting Response").endpoint(String.valueOf(endpoint)).build());

            log.error(aMarker, "The Exception occurred ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Agentic paper filter extraction consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    private static String getTritonRequestPayload(String dataExtractionPayloadString, String name, ObjectMapper objectMapper) throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(name);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(dataExtractionPayloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return objectMapper.writeValueAsString(tritonInputRequest);
    }

    private DataExtractionData getArgonRequestPayloadFromEntity(AgenticPaperFilterInput entity) {
        DataExtractionData dataExtractionData = new DataExtractionData();
        dataExtractionData.setOriginId(entity.getOriginId());
        dataExtractionData.setGroupId(entity.getGroupId());
        dataExtractionData.setProcessId(entity.getProcessId());
        dataExtractionData.setTenantId(entity.getTenantId());
        dataExtractionData.setRootPipelineId(entity.getRootPipelineId());
        dataExtractionData.setActionId(action.getActionId());
        dataExtractionData.setPaperNumber(entity.getPaperNo());
        dataExtractionData.setTemplateName(entity.getTemplateName());
        dataExtractionData.setProcess(PROCESS_NAME);
        dataExtractionData.setInputFilePath(entity.getFilePath());
        dataExtractionData.setBatchId(entity.getBatchId());

        return dataExtractionData;
    }

    private void replicateResponseBuilder(URL endpoint, Request request, List<AgenticPaperFilterOutput> parentObj, AgenticPaperFilterInput entity, String replicateJsonRequest) {
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ReplicateResponse replicateResponse = mapper.readValue(responseBody, ReplicateResponse.class);
                if (!replicateResponse.getOutput().isEmpty() && !replicateResponse.getOutput().isNull()) {
                    extractedReplicateOutputResponse(endpoint, entity, replicateResponse, parentObj, replicateJsonRequest, responseBody);
                } else {
                    parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                    log.error(aMarker, "The replicate response has empty output {}", replicateResponse.getOutput());
                }
            } else {
                parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : " + response.code() + " message : " + response.message());
                HandymanException.insertException("Agentic paper filter consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper No " + entity.getPaperNo(), handymanException, this.action);
                log.error(aMarker, "The replicate response status {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(entity.getTenantId()).templateId(entity.getTemplateId()).processId(entity.getProcessId()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(entity.getRootPipelineId()).templateName(entity.getTemplateName()).request(encryptRequestResponse(request.toString())).response("Error In getting replicate Response").endpoint(String.valueOf(endpoint)).build());

            log.error(aMarker, "The Exception occurred in replicate {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Agentic paper Filter consumer failed for replicate batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }

    public String getBase64ForPath(String imagePath) throws IOException {
        String base64Image = new String();
        try {
            // Read the image file into a byte array
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));

            // Encode the byte array to Base64
            base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Print the Base64 encoded string
            log.info(aMarker, "base 64 created for this file {}", imagePath);

        } catch (Exception e) {
            log.error(aMarker, "error occurred in creating base 64 {}", ExceptionUtil.toString(e));
            throw new HandymanException("error occurred in creating base 64 {} ", e, action);
        }

        return base64Image;
    }

    private void extractedReplicateOutputResponse(URL endpoint, AgenticPaperFilterInput entity, ReplicateResponse replicateResponse, List<AgenticPaperFilterOutput> parentObj, String replicateJsonRequest, String replicateJsonResponse) throws JsonProcessingException {
        DataExtractionDataItem dataExtractionDataItem = mapper.treeToValue(replicateResponse.getOutput(), DataExtractionDataItem.class);
        final String contentString = Optional.of(dataExtractionDataItem.getPageContent()).map(String::valueOf).orElse(null);
        final String flag = (!Objects.isNull(contentString) && contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        Integer paperNo = entity.getPaperNo();
        String filePath = entity.getFilePath();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        String templateName = entity.getTemplateName();
        Long rootPipelineId = entity.getRootPipelineId();
        String batchId = entity.getBatchId();
        String encryptSotPageContent = action.getContext().get(ENCRYPT_AGENTIC_FILTER_OUTPUT);
        String extractedContentEnc;
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContentEnc = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContentEnc = contentString;
        }

        parentObj.add(AgenticPaperFilterOutput.builder().filePath(new File(filePath).getAbsolutePath()).extractedText(extractedContentEnc).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(paperNo).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Text extraction action api call completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(tenantId).templateId(templateId).processId(processId).templateName(templateName).rootPipelineId(rootPipelineId).modelName(replicateResponse.getModel()).modelVersion(replicateResponse.getVersion()).batchId(batchId).request(encryptRequestResponse(replicateJsonRequest)).response(encryptRequestResponse(replicateJsonResponse)).endpoint(endpoint.toString()).build());
    }

    private void coproRequestBuilder(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String originId, Integer groupId, String jsonInputRequest, URL endpoint) {
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        Long rootPipelineId = entity.getRootPipelineId();
        String templateName = entity.getTemplateName();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, responseBody, parentObj, originId, groupId, "", "", jsonInputRequest, responseBody, endpoint.toString());
            } else {
                parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(responseBody)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : " + response.code() + " message : " + response.message());
                HandymanException.insertException("Agentic paper filter consumer failed for batch/group " + entity.getGroupId(), handymanException, this.action);

                log.error(aMarker, "The Exception occurred in response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(ExceptionUtil.toString(e)).rootPipelineId(rootPipelineId).templateName(templateName).response("Error In Response").endpoint(String.valueOf(endpoint)).build());

            log.error(aMarker, "The Exception occurred in Copro {} ", e.toString());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Agentic paper filter extraction consumer failed for batch/group " + groupId + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
        }
    }


    private void extractedCoproOutputResponse(AgenticPaperFilterInput entity, String stringDataItem, List<AgenticPaperFilterOutput> parentObj, String originId, Integer groupId, String modelName, String modelVersion, String request, String response, String endpoint) {

        String parentResponseObject = extractPageContent(stringDataItem);
        final String contentString = Optional.of(parentResponseObject).map(String::valueOf).orElse(null);
        final String flag = (!Objects.isNull(contentString) && contentString.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;
        Integer paperNo = entity.getPaperNo();
        String filePath = entity.getFilePath();
        Long tenantId = entity.getTenantId();
        String templateId = entity.getTemplateId();
        Long processId = entity.getProcessId();
        String templateName = entity.getTemplateName();
        Long rootPipelineId = entity.getRootPipelineId();
        String batchId = entity.getBatchId();

        String encryptSotPageContent = action.getContext().get(ENCRYPT_AGENTIC_FILTER_OUTPUT);
        String extractedContentEnc;
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

        if (Objects.equals(encryptSotPageContent, "true")) {
            extractedContentEnc = encryption.encrypt(contentString, "AES256", "TEXT_DATA");
        } else {
            extractedContentEnc = contentString;
        }


        parentObj.add(AgenticPaperFilterOutput.builder().batchId(entity.getBatchId()).filePath(new File(filePath).getAbsolutePath()).extractedText(extractedContentEnc).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).paperNo(paperNo).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Data extraction macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).isBlankPage(flag).tenantId(tenantId).templateId(templateId).processId(processId).templateName(templateName).rootPipelineId(rootPipelineId).modelName(modelName).modelVersion(modelVersion).batchId(batchId).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());


    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                log.info("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");
                    // Convert the cleaned JSON string to a JsonNode

                    if (!jsonResponse.isEmpty()) {
                        return objectMapper.readTree(jsonResponse);
                    } else {
                        return null;
                    }
                } else {

                    return objectMapper.readTree(jsonResponse);
                }
            } else if (jsonResponse.contains("{")) {
                log.info("Input does not contain the required ```json``` markers. So processing it based on the indication of object literals.");

                return objectMapper.readTree(jsonResponse);
                //throw new IllegalArgumentException("Input does not contain the required ```json``` markers.");
            } else {
                log.info("Input does not contain the required ```json``` markers or any indication of object literals. So returning null.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String extractPageContent(String jsonString) {
        int startIndex = jsonString.indexOf("\"pageContent\":") + "\"pageContent\":".length();
        int endIndex = jsonString.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1) {
            String pageContent = jsonString.substring(startIndex, endIndex).trim();
            if (pageContent.startsWith("\"")) {
                pageContent = pageContent.substring(1);
            }
            if (pageContent.endsWith("\"")) {
                pageContent = pageContent.substring(0, pageContent.length() - 1);
            }
            return pageContent;
        } else {
            return "";
        }
    }


    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
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