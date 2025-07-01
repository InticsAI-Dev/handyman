package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import bsh.EvalError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.EncryptionConstants;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.DatabaseUtility;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RadonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<RadonQueryInputTable, RadonQueryOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.RADON_KVP_ACTION.getProcessName();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final FileProcessingUtils fileProcessingUtils;
    private final String processBase64;
    private final Logger log;
    private final Marker aMarker;
    public final Jdbi jdbi;

    private final ProviderDataTransformer providerDataTransformer;

    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction, final String processBase64, final FileProcessingUtils fileProcessingUtils, Jdbi jdbi, ProviderDataTransformer providerDataTransformer) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.providerDataTransformer = providerDataTransformer;
        int timeOut = aAction.getTimeOut();
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
        this.jdbi = jdbi;
    }

    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        List<RadonQueryOutputTable> parentObj = new ArrayList<>();

        try {
            String userPrompt = processUserPrompt(entity);
            String jsonRequest = buildRequest(entity, userPrompt, endpoint);

            String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

            if (Objects.equals("false", tritonRequestActivator)) {
                log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode for originId: {} paperNo: {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, entity.getOriginId(), entity.getPaperNo());
                processLegacyRequest(entity, parentObj, jsonRequest, endpoint);
            } else {
                log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode for originId: {} paperNo: {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, entity.getOriginId(), entity.getPaperNo());
                processTritonRequest(entity, parentObj, jsonRequest, endpoint);
            }

            log.info(aMarker, "Radon kvp consumer process output parent object entities size {} for originId: {} paperNo: {}", parentObj.size(), entity.getOriginId(), entity.getPaperNo());
            return parentObj;

        } catch (Exception e) {
            log.error(aMarker, "Unexpected error in RadonKvpConsumerProcess for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), e.getMessage());
            parentObj.add(buildFailedResponse(entity, "Unexpected error: " + e.getMessage(), null, null, endpoint.toString()));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
            return parentObj;
        }
    }

    private String processUserPrompt(RadonQueryInputTable entity) throws Exception {
        if (!isBBoxActivatorEnabled(entity)) {
            log.info("BBox activator is disabled or process is not RADON_KVP_ACTION. Using original user prompt for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
            return entity.getUserPrompt();
        }

        log.info("RADON_KVP_ACTION process started. BBox activator is enabled for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());

        String inputResponseJson = processInputResponseJson(entity);
        return buildUserPrompt(entity, inputResponseJson);
    }

    private boolean isBBoxActivatorEnabled(RadonQueryInputTable entity) {
        return Objects.equals(action.getContext().get("bbox.radon_bbox_activator"), "true")
                && Objects.equals(entity.getProcess(), "RADON_KVP_ACTION");
    }

    private String processInputResponseJson(RadonQueryInputTable entity) throws Exception {
        String inputResponseJsonstr = entity.getInputResponseJson();
        String encryptOutputJsonContent = action.getContext().get(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION);

        log.info("Checking if end-to-end encryption is enabled: {} for originId: {} paperNo: {}", encryptOutputJsonContent, entity.getOriginId(), entity.getPaperNo());

        if (Objects.equals(encryptOutputJsonContent, "true")) {
            return decryptInputResponseJson(entity, inputResponseJsonstr);
        } else {
            log.info("Encryption is disabled. Using raw input response JSON for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
            return inputResponseJsonstr;
        }
    }

    private String decryptInputResponseJson(RadonQueryInputTable entity, String inputResponseJsonstr) throws Exception {
        log.info("Encrypting input response JSON for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);
        String decryptedJson = encryption.decrypt(inputResponseJsonstr, "AES256", "RADON_KVP_JSON");
        log.info("Encryption completed successfully for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());
        return decryptedJson;
    }

    private String buildUserPrompt(RadonQueryInputTable entity, String inputResponseJson) throws Exception {
        String base64Activator = action.getContext().get("sor.transaction.prompt.base64.activator");
        log.info("Checking if Base64 activator is enabled: {} for originId: {} paperNo: {}", base64Activator, entity.getOriginId(), entity.getPaperNo());

        if (Objects.equals(base64Activator, "true")) {
            return buildBase64UserPrompt(entity, inputResponseJson);
        } else {
            return buildPlainTextUserPrompt(entity, inputResponseJson);
        }
    }

    private String buildBase64UserPrompt(RadonQueryInputTable entity, String inputResponseJson) throws Exception {
        log.info("Base64 activator is turned ON for process: {} originId: {} paperNo: {}", entity.getProcess(), entity.getOriginId(), entity.getPaperNo());

        Base64toActualVaue base64Caller = new Base64toActualVaue();
        String base64Value = base64Caller.base64toActual(entity.getUserPrompt());
        log.info("Decoded Base64 value successfully for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());

        byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
        String decodedPrompt = new String(decodedBytes);
        log.info("Decoded prompt before replacing placeholder for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());

        String updatedPrompt = replacePlaceholder(decodedPrompt, inputResponseJson);
        return Base64.getEncoder().encodeToString(updatedPrompt.getBytes());
    }

    private String buildPlainTextUserPrompt(RadonQueryInputTable entity, String inputResponseJson) {
        log.info("Base64 activator is OFF. Using plain text prompt for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());

        String actualUserPrompt = entity.getUserPrompt();
        log.info("Original user prompt before replacing placeholder for originId: {} paperNo: {}", entity.getOriginId(), entity.getPaperNo());

        return replacePlaceholder(actualUserPrompt, inputResponseJson);
    }

    private String replacePlaceholder(String prompt, String inputResponseJson) {
        String placeholderName = action.getContext().get("prompt.bbox.json.placeholder.name");
        return prompt.replace(placeholderName, inputResponseJson);
    }

    private String buildRequest(RadonQueryInputTable entity, String userPrompt, URL endpoint) throws Exception {
        RadonKvpExtractionRequest radonKvpExtractionRequest = buildRadonKvpExtractionRequest(entity, userPrompt);
        String jsonInputRequest = mapper.writeValueAsString(radonKvpExtractionRequest);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);
        if (Objects.equals("false", tritonRequestActivator)) {
            return jsonInputRequest;
        } else {
            return buildTritonRequest(entity, jsonInputRequest);
        }
    }

    private RadonKvpExtractionRequest buildRadonKvpExtractionRequest(RadonQueryInputTable entity, String userPrompt) throws Exception {
        RadonKvpExtractionRequest request = new RadonKvpExtractionRequest();

        // Set basic properties
        request.setRootPipelineId(entity.getRootPipelineId());
        request.setActionId(action.getActionId());
        request.setProcess(entity.getProcess());
        request.setInputFilePath(entity.getInputFilePath());
        request.setGroupId(entity.getGroupId());
        request.setUserPrompt(userPrompt);
        request.setSystemPrompt(entity.getSystemPrompt());
        request.setProcessId(entity.getProcessId());
        request.setPaperNo(entity.getPaperNo());
        request.setTenantId(entity.getTenantId());
        request.setOriginId(entity.getOriginId());
        request.setBatchId(entity.getBatchId());

        // Set base64 image if needed
        request.setBase64Img(getBase64ImageIfNeeded(entity));

        return request;
    }

    private String getBase64ImageIfNeeded(RadonQueryInputTable entity) throws Exception {
        if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
            return fileProcessingUtils.convertFileToBase64(entity.getInputFilePath());
        }
        return "";
    }

    private String buildTritonRequest(RadonQueryInputTable entity, String jsonInputRequest) throws Exception {
        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(entity.getApiName());
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        return mapper.writeValueAsString(tritonInputRequest);
    }

    private void processLegacyRequest(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulResponse(entity, parentObj, jsonRequest, responseBody, endpoint.toString());
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), jsonRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (IOException e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "Unexpected error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "Unexpected Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processTritonRequest(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                processSuccessfulTritonResponse(entity, parentObj, jsonRequest, responseBody, endpoint.toString());
            } else {
                String errorMessage = response.body() != null ? response.body().string() : response.message();
                parentObj.add(buildFailedResponse(entity, "HTTP Error: " + response.code() + " - " + response.message(), jsonRequest, errorMessage, endpoint.toString()));
                logErrorAndInsertException(entity, "Unsuccessful response code: " + response.code() + " message: " + response.message());
            }
        } catch (IOException e) {
            parentObj.add(buildFailedResponse(entity, "IO Error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "IO Exception: " + ExceptionUtil.toString(e));
        } catch (Exception e) {
            parentObj.add(buildFailedResponse(entity, "Unexpected error: " + ExceptionUtil.toString(e), jsonRequest, null, endpoint.toString()));
            logErrorAndInsertException(entity, "Unexpected Exception: " + ExceptionUtil.toString(e));
        }
    }

    private void processSuccessfulResponse(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint) {
        try {
            RadonKvpExtractionResponse modelResponse = parseResponse(responseBody);
            if (hasValidOutputs(modelResponse)) {
                processOutputs(entity, parentObj, request, responseBody, endpoint, modelResponse, this::processRadonDataItem);
            } else {
                handleNoOutputs(entity, parentObj, request, responseBody, endpoint, "response");
            }
        } catch (JsonProcessingException e) {
            handleJsonParsingError(entity, parentObj, request, responseBody, endpoint, e, "response");
        }
    }

    private void processSuccessfulTritonResponse(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint) {
        try {
            RadonKvpExtractionResponse modelResponse = parseResponse(responseBody);
            if (hasValidOutputs(modelResponse)) {
                processOutputs(entity, parentObj, request, responseBody, endpoint, modelResponse, this::processTritonRadonDataItem);
            } else {
                handleNoOutputs(entity, parentObj, request, responseBody, endpoint, "triton response");
            }
        } catch (JsonProcessingException e) {
            handleJsonParsingError(entity, parentObj, request, responseBody, endpoint, e, "triton response");
        }
    }

    private RadonKvpExtractionResponse parseResponse(String responseBody) throws JsonProcessingException {
        return mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
    }

    private boolean hasValidOutputs(RadonKvpExtractionResponse modelResponse) {
        return modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty();
    }

    private void processOutputs(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint,
                                RadonKvpExtractionResponse modelResponse, DataItemProcessor processor) {
        modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
            try {
                processor.process(entity, radonDataItem, parentObj, request, responseBody, endpoint);
            } catch (Exception e) {
                handleDataItemProcessingError(entity, parentObj, request, responseBody, endpoint, e, processor.getClass().getSimpleName());
            }
        }));
    }

    private void handleNoOutputs(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint, String responseType) {
        log.warn(aMarker, "No outputs found in {} for originId: {} paperNo: {}", responseType, entity.getOriginId(), entity.getPaperNo());
        parentObj.add(buildFailedResponse(entity, "No outputs found in " + responseType, request, responseBody, endpoint));
    }

    private void handleJsonParsingError(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint,
                                        JsonProcessingException e, String responseType) {
        log.error(aMarker, "Error parsing {} JSON for originId: {} paperNo: {}: {}", responseType, entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
        parentObj.add(buildFailedResponse(entity, "Error parsing " + responseType + " JSON: " + ExceptionUtil.toString(e), request, responseBody, endpoint));
        logErrorAndInsertException(entity, "Error parsing " + responseType + " JSON: " + ExceptionUtil.toString(e));
    }

    private void handleDataItemProcessingError(RadonQueryInputTable entity, List<RadonQueryOutputTable> parentObj, String request, String responseBody, String endpoint,
                                               Exception e, String processorType) {
        log.error(aMarker, "Error processing {} for originId: {} paperNo: {}: {}", processorType, entity.getOriginId(), entity.getPaperNo(), ExceptionUtil.toString(e));
        parentObj.add(buildFailedResponse(entity, "Error processing " + processorType + ": " + ExceptionUtil.toString(e), request, responseBody, endpoint));
        logErrorAndInsertException(entity, "Error processing " + processorType + ": " + ExceptionUtil.toString(e));
    }

    @FunctionalInterface
    private interface DataItemProcessor {
        void process(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws Exception;
    }

    private void processRadonDataItem(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        if (Boolean.TRUE.equals(entity.getPostProcess())) {
            processProviderData(entity, modelResponse, parentObj, request, response, endpoint);
        } else {
            String extractedContent = processExtractedContent(modelResponse.getInferResponse());
            parentObj.add(buildSuccessResponse(entity, extractedContent, modelResponse.getBatchId(), request, response, endpoint));
        }
    }

    private void processTritonRadonDataItem(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws IOException, EvalError {
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        if (Boolean.TRUE.equals(entity.getPostProcess())) {
            processProviderData(entity, modelResponse, parentObj, request, response, endpoint);
        } else {
            String extractedContent = processExtractedContent(modelResponse.getInferResponse());
            parentObj.add(buildSuccessResponse(entity, extractedContent, modelResponse.getBatchId(), request, response, endpoint));
        }
    }

    private void processProviderData(RadonQueryInputTable entity, RadonKvpLineItem modelResponse, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) {
        log.info(aMarker, "Provider data found for the given input started the post process with value {} for originId: {} paperNo: {}", entity.getPostProcess(), entity.getOriginId(), entity.getPaperNo());
        String providerClassName = action.getContext().get(entity.getPostProcessClassName());
        Optional<String> sourceCode = DatabaseUtility.fetchBshResultByClassName(jdbi, providerClassName, entity.getTenantId());
        if (sourceCode.isPresent()) {
            List<RadonQueryOutputTable> providerParentObj = providerDataTransformer.processProviderData(sourceCode.get(), providerClassName, modelResponse.getInferResponse(), entity, request, response, endpoint);
            parentObj.addAll(providerParentObj);
        } else {
            log.warn(aMarker, "Provider source code not found for className: {} originId: {} paperNo: {}", providerClassName, entity.getOriginId(), entity.getPaperNo());
            parentObj.add(buildFailedResponse(entity, "Provider source code not found for className: " + providerClassName, request, response, endpoint));
        }
    }

    private String processExtractedContent(String inferResponse) {
        String encryptOutputJsonContent = action.getContext().get(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION);
        if (Objects.equals(encryptOutputJsonContent, "true")) {
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);
            return encryption.encrypt(inferResponse, "AES256", "RADON_KVP_JSON");
        } else {
            return inferResponse;
        }
    }

    private RadonQueryOutputTable buildSuccessResponse(RadonQueryInputTable entity, String extractedContent, String batchId, String request, String response, String endpoint) {
        return RadonQueryOutputTable.builder()
                .createdOn(CreateTimeStamp.currentTimestamp())
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getTenantId())
                .originId(entity.getOriginId())
                .paperNo(entity.getPaperNo())
                .totalResponseJson(extractedContent)
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .actionId(action.getActionId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .process(entity.getProcess())
                .batchId(batchId != null ? batchId : entity.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(response))
                .sorContainerId(entity.getSorContainerId())
                .endpoint(endpoint)
                .build();
    }

    private RadonQueryOutputTable buildFailedResponse(RadonQueryInputTable entity, String errorMessage, String request, String response, String endpoint) {
        return RadonQueryOutputTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .tenantId(entity.getTenantId())
                .actionId(action.getActionId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(entity.getApiName())
                .message(encryptRequestResponse(errorMessage))
                .batchId(entity.getBatchId())
                .createdOn(entity.getCreatedOn())
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getTenantId())
                .category(entity.getCategory())
                .request(request != null ? encryptRequestResponse(request) : null)
                .response(response != null ? encryptRequestResponse(response) : null)
                .endpoint(endpoint)
                .sorContainerId(entity.getSorContainerId())
                .build();
    }

    private void logErrorAndInsertException(RadonQueryInputTable entity, String errorMessage) {
        log.error(aMarker, "Radon kvp consumer failed for originId: {} paperNo: {}: {}", entity.getOriginId(), entity.getPaperNo(), errorMessage);
        HandymanException handymanException = new HandymanException(errorMessage);
        HandymanException.insertException("Radon kvp consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " paper no " + entity.getPaperNo(), handymanException, this.action);
    }

    public JsonNode getJsonNodeFromInferResponse(ObjectMapper objectMapper, String jsonString) throws JsonProcessingException {
        try {
            jsonString = jsonString.replace("\n", "");
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode;
        } catch (Exception e) {
            throw new IllegalArgumentException("Input does not have a json structure.");
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(EncryptionConstants.ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
        } else {
            return request;
        }
    }
}
