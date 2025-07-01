package in.handyman.raven.lib.model.agenticPaperFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.EncryptionConstants;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
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

public class AgenticPaperFilterConsumerProcess implements CoproProcessor.ConsumerProcess<AgenticPaperFilterInput, AgenticPaperFilterOutput> {

    public static final String AGENTIC_PAPER_FILTER_START = "AGENTIC PAPER FILTER START";
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String KRYPTON_START = "KRYPTON START";
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.agentic.paper.filter.extraction.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String AGENTIC_PAPER_FILTER_MODEL_NAME = "preprocess.agentic.paper.filter.model.name";
    public static final String PAGE_CONTENT_NO = "no";
    public static final String PAGE_CONTENT_YES = "yes";
    public static final String PROCESS_NAME = "AGENTIC PAPER FILTER";
    private static final String DEFAULT_SOCKET_TIMEOUT = "100";

    private static final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ActionExecutionAudit action;
    private final Logger log;
    private final Marker aMarker;
    private final int pageContentMinLength;
    private final String processBase64;
    private final FileProcessingUtils fileProcessingUtils;
    private final OkHttpClient httpclient;

    public AgenticPaperFilterConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.pageContentMinLength = pageContentMinLength;
        int timeOut = Integer.parseInt(action.getContext().getOrDefault("copro.client.socket.timeout", DEFAULT_SOCKET_TIMEOUT));
        this.httpclient = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES)
                .readTimeout(timeOut, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<AgenticPaperFilterOutput> process(URL endpoint, AgenticPaperFilterInput entity) throws IOException {
        List<AgenticPaperFilterOutput> parentObj = new ArrayList<>();

        try {
            String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
            String textExtractionModelName = action.getContext().get(AGENTIC_PAPER_FILTER_MODEL_NAME);

            log.info(aMarker, "Processing agentic paper filter for originId: {} paperNo: {} with handler: {} model: {}",
                    entity.getOriginId(), entity.getPaperNo(), coproHandlerName, textExtractionModelName);

            if (Objects.equals("COPRO", coproHandlerName)) {
                processCoproRequest(entity, parentObj, endpoint);
            } else if (Objects.equals("REPLICATE", coproHandlerName)) {
                processReplicateRequest(entity, parentObj, endpoint, textExtractionModelName);
            } else if (Objects.equals("TRITON", coproHandlerName)) {
                processTritonRequest(entity, parentObj, endpoint, textExtractionModelName);
            } else {
                log.warn(aMarker, "Unknown handler type: {} for originId: {} paperNo: {}", coproHandlerName, entity.getOriginId(), entity.getPaperNo());
                parentObj.add(buildFailedResponse(entity, "Unknown handler type: " + coproHandlerName, null, null, endpoint.toString()));
            }

            log.info(aMarker, "Agentic paper filter process completed for originId: {} paperNo: {} with {} outputs",
                    entity.getOriginId(), entity.getPaperNo(), parentObj.size());
            return parentObj;

        } catch (Exception e) {
            log.error(aMarker, "Unexpected error in AgenticPaperFilterConsumerProcess for originId: {} paperNo: {}: {}",
                    entity.getOriginId(), entity.getPaperNo(), e.getMessage());
            parentObj.add(buildFailedResponse(entity, "Unexpected error: " + e.getMessage(), null, null, endpoint.toString()));
            logErrorAndInsertException(entity, "Unexpected error: " + e.getMessage());
            return parentObj;
        }
    }

    private void processCoproRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint) throws Exception {
        log.info(aMarker, "Executing COPRO handler for endpoint: {} originId: {} paperNo: {}", endpoint, entity.getOriginId(), entity.getPaperNo());

        DataExtractionData dataExtractionData = buildDataExtractionData(entity);
        String jsonInputRequest = mapper.writeValueAsString(dataExtractionData);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonInputRequest, mediaType))
                .build();

        processCoproResponse(entity, request, parentObj, jsonInputRequest, endpoint);
    }

    private void processReplicateRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint, String textExtractionModelName) throws Exception {
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);

        if (textExtractionModelName.equals(ModelRegistry.ARGON.name())) {
            processReplicateArgonRequest(entity, parentObj, endpoint, replicateApiToken);
        } else if (textExtractionModelName.equals(ModelRegistry.KRYPTON.name())) {
            processReplicateKryptonRequest(entity, parentObj, endpoint, replicateApiToken);
        } else {
            log.warn(aMarker, "Unknown model type: {} for originId: {} paperNo: {}", textExtractionModelName, entity.getOriginId(), entity.getPaperNo());
            parentObj.add(buildFailedResponse(entity, "Unknown model type: " + textExtractionModelName, null, null, endpoint.toString()));
        }
    }

    private void processReplicateArgonRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint, String replicateApiToken) throws Exception {
        log.info(aMarker, "Executing REPLICATE ARGON handler for endpoint: {} originId: {} paperNo: {}", endpoint, entity.getOriginId(), entity.getPaperNo());

        DataExtractionData dataExtractionData = buildDataExtractionData(entity);
        String base64ForPath = getBase64ForPath(dataExtractionData.getInputFilePath());
        dataExtractionData.setBase64Img(base64ForPath);

        ReplicateRequest replicateRequest = new ReplicateRequest();
        replicateRequest.setInput(dataExtractionData);

        String replicateJsonRequest = mapper.writeValueAsString(replicateRequest);
        Request request = buildReplicateRequest(endpoint, replicateJsonRequest, replicateApiToken);

        processReplicateResponse(entity, request, parentObj, replicateJsonRequest, endpoint);
    }

    private void processReplicateKryptonRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint, String replicateApiToken) throws Exception {
        log.info(aMarker, "Executing REPLICATE KRYPTON handler for endpoint: {} originId: {} paperNo: {}", endpoint, entity.getOriginId(), entity.getPaperNo());

        String base64ForPath = getBase64ForPath(entity.getFilePath());
        RadonKvpExtractionRequest kryptonRequestPayload = buildKryptonRequestPayload(entity);
        kryptonRequestPayload.setBase64Img(base64ForPath);

        String textExtractionPayloadString = mapper.writeValueAsString(kryptonRequestPayload);
        ReplicateRequest replicateRequest = new ReplicateRequest();
        replicateRequest.setInput(textExtractionPayloadString);

        String replicateJsonRequest = mapper.writeValueAsString(replicateRequest);
        Request request = buildReplicateRequest(endpoint, replicateJsonRequest, replicateApiToken);

        processTritonKryptonResponse(entity, request, parentObj, textExtractionPayloadString, endpoint);
    }

    private void processTritonRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint, String textExtractionModelName) throws Exception {
        log.info(aMarker, "Executing TRITON handler for endpoint: {} model: {} originId: {} paperNo: {}", endpoint, textExtractionModelName, entity.getOriginId(), entity.getPaperNo());

        if (textExtractionModelName.equals(ModelRegistry.ARGON.name())) {
            processTritonArgonRequest(entity, parentObj, endpoint);
        } else if (textExtractionModelName.equals(ModelRegistry.KRYPTON.name())) {
            processTritonKryptonRequest(entity, parentObj, endpoint);
        } else {
            log.warn(aMarker, "Unknown model type: {} for originId: {} paperNo: {}", textExtractionModelName, entity.getOriginId(), entity.getPaperNo());
            parentObj.add(buildFailedResponse(entity, "Unknown model type: " + textExtractionModelName, null, null, endpoint.toString()));
        }
    }

    private void processTritonArgonRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint) throws Exception {
        DataExtractionData dataExtractionPayload = buildDataExtractionData(entity);
        setBase64ImageIfNeeded(dataExtractionPayload, entity.getFilePath());

        String dataExtractionPayloadString = mapper.writeValueAsString(dataExtractionPayload);
        String jsonRequestTritonArgon = buildTritonRequestPayload(dataExtractionPayloadString, AGENTIC_PAPER_FILTER_START);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestTritonArgon, mediaType))
                .build();

        processTritonArgonResponse(entity, request, parentObj, jsonRequestTritonArgon, endpoint);
    }

    private void processTritonKryptonRequest(AgenticPaperFilterInput entity, List<AgenticPaperFilterOutput> parentObj, URL endpoint) throws Exception {
        RadonKvpExtractionRequest kryptonRequestPayload = buildKryptonRequestPayload(entity);
        setBase64ImageIfNeeded(kryptonRequestPayload, entity.getFilePath());

        String textExtractionPayloadString = mapper.writeValueAsString(kryptonRequestPayload);
        String jsonRequestTritonKrypton = buildTritonRequestPayload(textExtractionPayloadString, KRYPTON_START);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestTritonKrypton, mediaType))
                .build();

        processTritonKryptonResponse(entity, request, parentObj, jsonRequestTritonKrypton, endpoint);
    }

    private DataExtractionData buildDataExtractionData(AgenticPaperFilterInput entity) {
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
        dataExtractionData.setBase64Img(entity.getBase64Img());
        return dataExtractionData;
    }

    private RadonKvpExtractionRequest buildKryptonRequestPayload(AgenticPaperFilterInput entity) {
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

    private void setBase64ImageIfNeeded(Object requestPayload, String filePath) throws Exception {
        if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
            if (requestPayload instanceof DataExtractionData) {
                ((DataExtractionData) requestPayload).setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
            } else if (requestPayload instanceof RadonKvpExtractionRequest) {
                ((RadonKvpExtractionRequest) requestPayload).setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
            }
        } else {
            if (requestPayload instanceof DataExtractionData) {
                ((DataExtractionData) requestPayload).setBase64Img("");
            } else if (requestPayload instanceof RadonKvpExtractionRequest) {
                ((RadonKvpExtractionRequest) requestPayload).setBase64Img("");
            }
        }
    }

    private Request buildReplicateRequest(URL endpoint, String replicateJsonRequest, String replicateApiToken) {
        return new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(replicateJsonRequest, mediaType))
                .addHeader("Authorization", "Bearer " + replicateApiToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "wait")
                .build();
    }

    private String buildTritonRequestPayload(String payloadString, String name) throws JsonProcessingException {
        TritonRequest tritonRequestPayload = new TritonRequest();
        tritonRequestPayload.setName(name);
        tritonRequestPayload.setShape(List.of(1, 1));
        tritonRequestPayload.setDatatype(TritonDataTypes.BYTES.name());
        tritonRequestPayload.setData(Collections.singletonList(payloadString));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(tritonRequestPayload));

        return mapper.writeValueAsString(tritonInputRequest);
    }

    private void processCoproResponse(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonInputRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulCoproResponse(entity, responseBody, parentObj, jsonInputRequest, responseBody, endpoint.toString());
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), jsonInputRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), jsonInputRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processReplicateResponse(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String replicateJsonRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulReplicateResponse(entity, responseBody, parentObj, replicateJsonRequest, responseBody, endpoint);
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), replicateJsonRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), replicateJsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processTritonArgonResponse(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulTritonArgonResponse(entity, responseBody, parentObj, jsonRequest, responseBody, endpoint.toString());
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), jsonRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processTritonKryptonResponse(AgenticPaperFilterInput entity, Request request, List<AgenticPaperFilterOutput> parentObj, String jsonRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulTritonKryptonResponse(entity, responseBody, parentObj, jsonRequest, responseBody, endpoint.toString());
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), jsonRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processSuccessfulCoproResponse(AgenticPaperFilterInput entity, String responseBody, List<AgenticPaperFilterOutput> parentObj, String request, String response, String endpoint) {
        String extractedContent = extractPageContent(responseBody);
        String flag = determinePageContentFlag(extractedContent);
        String encryptedContent = encryptContentIfNeeded(extractedContent);

        parentObj.add(buildSuccessResponse(entity, encryptedContent, flag, null, null, request, response, endpoint));
    }

    private void processSuccessfulReplicateResponse(AgenticPaperFilterInput entity, String responseBody, List<AgenticPaperFilterOutput> parentObj, String request, String response, URL endpoint) {
        try {
            ReplicateResponse replicateResponse = mapper.readValue(responseBody, ReplicateResponse.class);
            if (replicateResponse.getOutput() != null && !replicateResponse.getOutput().isEmpty() && !replicateResponse.getOutput().isNull()) {
                processReplicateOutput(entity, replicateResponse, parentObj, request, response, endpoint);
            } else {
                log.warn(aMarker, "Replicate response has empty output for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
                parentObj.add(buildFailedResponse(entity, "Replicate response has empty output", request, responseBody, endpoint.toString()));
            }
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Error parsing replicate response JSON for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
            parentObj.add(buildFailedResponse(entity, "Error parsing replicate response JSON: " + ExceptionUtil.toString(e), request, responseBody, endpoint.toString()));
            logErrorAndInsertException(entity, "Error parsing replicate response JSON: " + ExceptionUtil.toString(e));
        }
    }

    private void processSuccessfulTritonArgonResponse(AgenticPaperFilterInput entity, String responseBody, List<AgenticPaperFilterOutput> parentObj, String request, String response, String endpoint) {
        try {
            DataExtractionResponse modelResponse = mapper.readValue(responseBody, DataExtractionResponse.class);
            if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                modelResponse.getOutputs().forEach(o -> o.getData().forEach(dataItem -> {
                    try {
                        processArgonDataItem(entity, dataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), request, response, endpoint);
                    } catch (JsonProcessingException e) {
                        log.error(aMarker, "Error processing argon data item for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
                        parentObj.add(buildFailedResponse(entity, "Error processing argon data item: " + ExceptionUtil.toString(e), request, response, endpoint));
                        logErrorAndInsertException(entity, "Error processing argon data item: " + ExceptionUtil.toString(e));
                    }
                }));
            } else {
                log.warn(aMarker, "No outputs found in triton argon response for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
                parentObj.add(buildFailedResponse(entity, "No outputs found in triton argon response", request, response, endpoint));
            }
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Error parsing triton argon response JSON for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
            parentObj.add(buildFailedResponse(entity, "Error parsing triton argon response JSON: " + ExceptionUtil.toString(e), request, response, endpoint));
            logErrorAndInsertException(entity, "Error parsing triton argon response JSON: " + ExceptionUtil.toString(e));
        }
    }

    private void processSuccessfulTritonKryptonResponse(AgenticPaperFilterInput entity, String responseBody, List<AgenticPaperFilterOutput> parentObj, String request, String response, String endpoint) {
        try {
            RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
            if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                modelResponse.getOutputs().forEach(o -> o.getData().forEach(dataItem -> {
                    try {
                        processKryptonDataItem(entity, dataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion(), request, response, endpoint);
                    } catch (JsonProcessingException e) {
                        log.error(aMarker, "Error processing krypton data item for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
                        parentObj.add(buildFailedResponse(entity, "Error processing krypton data item: " + ExceptionUtil.toString(e), request, response, endpoint));
                        logErrorAndInsertException(entity, "Error processing krypton data item: " + ExceptionUtil.toString(e));
                    }
                }));
            } else {
                log.warn(aMarker, "No outputs found in triton krypton response for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
                parentObj.add(buildFailedResponse(entity, "No outputs found in triton krypton response", request, response, endpoint));
            }
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Error parsing triton krypton response JSON for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
            parentObj.add(buildFailedResponse(entity, "Error parsing triton krypton response JSON: " + ExceptionUtil.toString(e), request, response, endpoint));
            logErrorAndInsertException(entity, "Error parsing triton krypton response JSON: " + ExceptionUtil.toString(e));
        }
    }

    private void processReplicateOutput(AgenticPaperFilterInput entity, ReplicateResponse replicateResponse, List<AgenticPaperFilterOutput> parentObj, String request, String response, URL endpoint) throws JsonProcessingException {
        DataExtractionDataItem dataExtractionDataItem = mapper.treeToValue(replicateResponse.getOutput(), DataExtractionDataItem.class);
        String contentString = Optional.ofNullable(dataExtractionDataItem.getPageContent()).map(String::valueOf).orElse(null);
        String flag = determinePageContentFlag(contentString);
        String encryptedContent = encryptContentIfNeeded(contentString);

        parentObj.add(buildSuccessResponse(entity, encryptedContent, flag, replicateResponse.getModel(), replicateResponse.getVersion(), request, response, endpoint.toString()));
    }

    private void processArgonDataItem(AgenticPaperFilterInput entity, String dataItem, List<AgenticPaperFilterOutput> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        DataExtractionDataItem dataExtractionDataItem = mapper.readValue(dataItem, DataExtractionDataItem.class);
        String contentString = Optional.ofNullable(dataExtractionDataItem.getPageContent()).map(String::valueOf).orElse(null);
        String flag = determinePageContentFlag(contentString);
        String encryptedContent = encryptContentIfNeeded(contentString);

        parentObj.add(buildSuccessResponse(entity, encryptedContent, flag, modelName, modelVersion, request, response, endpoint));
    }

    private void processKryptonDataItem(AgenticPaperFilterInput entity, String dataItem, List<AgenticPaperFilterOutput> parentObj, String modelName, String modelVersion, String request, String response, String endpoint) throws JsonProcessingException {
        String cleanedJson = dataItem.replaceAll("```json", "").replaceAll("```", "").trim();
        RadonKvpLineItem dataExtractionDataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);

        String inferResponseJson = dataExtractionDataItem.getInferResponse();
        JsonNode inferResponseNode = mapper.readTree(inferResponseJson);
        String formattedInferResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inferResponseNode);
        String flag = determinePageContentFlag(inferResponseJson);
        String encryptedContent = encryptContentIfNeeded(formattedInferResponse);

        // Process each field in the response
        Iterator<Map.Entry<String, JsonNode>> fields = inferResponseNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String containerName = entry.getKey();
            String containerValue = entry.getValue().asText();

            parentObj.add(buildSuccessResponse(entity, encryptedContent, flag, modelName, modelVersion, request, response, endpoint, containerName, containerValue));
        }
    }

    private String determinePageContentFlag(String content) {
        if (content == null || content.length() <= pageContentMinLength) {
            return PAGE_CONTENT_YES;
        }
        return PAGE_CONTENT_NO;
    }

    private String encryptContentIfNeeded(String content) {
        String encryptSotPageContent = action.getContext().get(EncryptionConstants.ENCRYPT_AGENTIC_FILTER_OUTPUT);
        if (Objects.equals(encryptSotPageContent, "true")) {
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);
            return encryption.encrypt(content, "AES256", "TEXT_DATA");
        }
        return content;
    }

    private AgenticPaperFilterOutput buildSuccessResponse(AgenticPaperFilterInput entity, String extractedContent, String flag, String modelName, String modelVersion, String request, String response, String endpoint) {
        return buildSuccessResponse(entity, extractedContent, flag, modelName, modelVersion, request, response, endpoint, null, null);
    }

    private AgenticPaperFilterOutput buildSuccessResponse(AgenticPaperFilterInput entity, String extractedContent, String flag, String modelName, String modelVersion, String request, String response, String endpoint, String containerName, String containerValue) {
        return AgenticPaperFilterOutput.builder()
                .filePath(new File(entity.getFilePath()).getAbsolutePath())
                .extractedText(extractedContent)
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .groupId(entity.getGroupId())
                .paperNo(entity.getPaperNo())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Agentic Paper Filter macro completed")
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .isBlankPage(flag)
                .tenantId(entity.getTenantId())
                .templateId(entity.getTemplateId())
                .processId(entity.getProcessId())
                .templateName(entity.getTemplateName())
                .rootPipelineId(entity.getRootPipelineId())
                .modelName(modelName)
                .modelVersion(modelVersion)
                .batchId(entity.getBatchId())
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(response))
                .endpoint(endpoint)
                .containerName(containerName)
                .containerValue(containerValue)
                .build();
    }

    private AgenticPaperFilterOutput buildFailedResponse(AgenticPaperFilterInput entity, String errorMessage, String request, String response, String endpoint) {
        return AgenticPaperFilterOutput.builder()
                .batchId(entity.getBatchId())
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .groupId(entity.getGroupId())
                .paperNo(entity.getPaperNo())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .tenantId(entity.getTenantId())
                .templateId(entity.getTemplateId())
                .processId(entity.getProcessId())
                .message(encryptRequestResponse(errorMessage))
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .rootPipelineId(entity.getRootPipelineId())
                .templateName(entity.getTemplateName())
                .request(request != null ? encryptRequestResponse(request) : null)
                .response(response != null ? encryptRequestResponse(response) : null)
                .endpoint(endpoint)
                .build();
    }

    private void logErrorAndInsertException(AgenticPaperFilterInput entity, String errorMessage) {
        log.error(aMarker, "Agentic paper filter consumer failed for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), errorMessage);
        HandymanException handymanException = new HandymanException(errorMessage);
        HandymanException.insertException("Agentic paper filter consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
    }

    public String getBase64ForPath(String imagePath) throws IOException {
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            log.info(aMarker, "Base64 created for file: {}", imagePath);
            return base64Image;
        } catch (Exception e) {
            log.error(aMarker, "Error occurred in creating base64 for file {}: {}", imagePath, ExceptionUtil.toString(e));
            throw new HandymanException("Error occurred in creating base64", e, action);
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
        }
        return "";
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(EncryptionConstants.ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
        }
        return request;
    }
}