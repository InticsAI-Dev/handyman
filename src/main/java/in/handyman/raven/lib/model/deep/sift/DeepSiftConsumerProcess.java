package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_DEEP_SIFT_OUTPUT;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class DeepSiftConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftInputTable, DeepSiftOutputTable> {
    private static final String PROCESS_NAME = "DATA_EXTRACTION";
    private static final String COPRO_SOCKET_TIMEOUT = "copro.client.socket.timeout";
    private static final String ENCRYPTION_ALGORITHM = "AES256";
    private static final String TEXT_DATA_TYPE = "TEXT_DATA";
    private static final String COPRO_REQUEST_TYPE = "COPRO_REQUEST";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Set<String> VALID_MODELS = Set.of("XENON", "ARGON", "KRYPTON", "OPTIMUS");

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final OkHttpClient httpClient;
    private final FileProcessingUtils fileProcessingUtils;
    private final ObjectMapper objectMapper;

    public DeepSiftConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.fileProcessingUtils = fileProcessingUtils;
        this.objectMapper = new ObjectMapper();

        int timeOut = Integer.parseInt(this.action.getContext().getOrDefault(COPRO_SOCKET_TIMEOUT, "100"));
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeOut, TimeUnit.MINUTES)
                .writeTimeout(timeOut, TimeUnit.MINUTES)
                .readTimeout(timeOut, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public List<DeepSiftOutputTable> process(URL endpoint, DeepSiftInputTable entity) throws IOException {
        List<DeepSiftOutputTable> parentObj = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        if (!VALID_MODELS.contains(entity.getModelName())) {
            log.error(aMarker, "Invalid model name: {} for originId: {}", entity.getModelName(), entity.getOriginId());
        }

        String inputFilePath = entity.getInputFilePath();
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            log.error(aMarker, "Input file path is null or empty for originId: {}", entity.getOriginId());
        }

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.canRead()) {
            log.error(aMarker, "Input file does not exist or is not readable: {}", inputFilePath);
        }

        log.info(aMarker, "Executing {} handler for endpoint: {}", entity.getModelName(), endpoint);
        DeepSiftRequest requestPayload = getRequestPayloadFromQuery(entity);
        String base64ForPath;
        try {
            base64ForPath = getBase64ForPath(inputFilePath);
            if (base64ForPath == null || base64ForPath.isEmpty()) {
                log.error(aMarker, "Base64 conversion returned null or empty for file: {}", inputFilePath);
            }
            requestPayload.setBase64Img(base64ForPath);
            log.info(aMarker, "Base64 image generated for file: {}", inputFilePath);
        } catch (IOException e) {
            log.error(aMarker, "Failed to convert file to Base64: {}", inputFilePath, e);
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            throw new HandymanException("Error converting file to Base64 for model " + entity.getModelName(), e, action);
        }

        String modelPayloadString;
        try {
            modelPayloadString = objectMapper.writeValueAsString(requestPayload);
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Failed to serialize payload for model {}", entity.getModelName(), e);
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            throw new HandymanException("Error serializing payload for model " + entity.getModelName(), e, action);
        }

        String jsonRequest = getXenonRequest(requestPayload);
        String dbJsonRequest = sanitizeRequestForDb(requestPayload);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequest, MEDIA_TYPE))
                .build();
        requestExecutor(entity, request, parentObj, jsonRequest, dbJsonRequest, endpoint, startTime);

        return parentObj;
    }

    private DeepSiftRequest getRequestPayloadFromQuery(DeepSiftInputTable entity) {
        DeepSiftRequest deepSiftRequest = new DeepSiftRequest();
        deepSiftRequest.setOriginId(entity.getOriginId());
        deepSiftRequest.setTenantId(entity.getTenantId());
        deepSiftRequest.setRootPipelineId(entity.getRootPipelineId());
        deepSiftRequest.setActionId(action.getActionId());
        deepSiftRequest.setProcess(PROCESS_NAME);
        deepSiftRequest.setInputFilePath(entity.getInputFilePath());
        deepSiftRequest.setBatchId(entity.getBatchId());
        deepSiftRequest.setProcessId(entity.getRootPipelineId());
        deepSiftRequest.setGroupId(Long.valueOf(entity.getGroupId()));
        deepSiftRequest.setModelName(entity.getModelName());
        return deepSiftRequest;
    }

    private String getXenonRequest(DeepSiftRequest deepSiftRequest) throws JsonProcessingException {
        XenonRequest customRequest = XenonRequest.builder()
                .originId(deepSiftRequest.getOriginId())
                .batchId(deepSiftRequest.getBatchId())
                .processId(deepSiftRequest.getProcessId())
                .groupId(deepSiftRequest.getGroupId())
                .tenantId(deepSiftRequest.getTenantId())
                .rootPipelineId(deepSiftRequest.getRootPipelineId())
                .process(deepSiftRequest.getProcess())
                .modelName(deepSiftRequest.getModelName())
                .actionId(deepSiftRequest.getActionId())
                .inputFilePath(deepSiftRequest.getInputFilePath())
                .base64Img(deepSiftRequest.getBase64Img())
                .build();
        return objectMapper.writeValueAsString(customRequest);
    }

    private String sanitizeRequestForDb(DeepSiftRequest deepSiftRequest) {
        try {
            XenonRequest sanitizedRequest = XenonRequest.builder()
                    .originId(deepSiftRequest.getOriginId())
                    .batchId(deepSiftRequest.getBatchId())
                    .processId(deepSiftRequest.getProcessId())
                    .groupId(deepSiftRequest.getGroupId())
                    .tenantId(deepSiftRequest.getTenantId())
                    .rootPipelineId(deepSiftRequest.getRootPipelineId())
                    .process(deepSiftRequest.getProcess())
                    .modelName(deepSiftRequest.getModelName())
                    .actionId(deepSiftRequest.getActionId())
                    .inputFilePath(deepSiftRequest.getInputFilePath())
                    .build();
            return objectMapper.writeValueAsString(sanitizedRequest);
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Failed to sanitize request for DB", e);
            return "{}";
        }
    }


    private void requestExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj,
                                 String jsonRequest,String dbJsonRequest, URL endpoint, long startTime) {
        try (Response response = httpClient.newCall(request).execute()) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            if (response.body() == null) {
                log.error(aMarker, "Response body is null for request to {} for model {}", endpoint, entity.getModelName());
                throw new HandymanException(
                        "Response body is null for request to " + endpoint + " for model " + entity.getModelName()
                );
            }
            String responseBody = response.body().string();
            log.info(aMarker, "{} response: code={}, message={}", entity.getModelName(), response.code(), response.message());

            if (response.isSuccessful()) {
                XenonResponse modelResponse = objectMapper.readValue(responseBody, XenonResponse.class);

                if (modelResponse.isSuccess() && modelResponse.hasInferResponse()) {
                    String extractedContent = modelResponse.getInferResponse();
                    String encryptSotPageContent = action.getContext().get(ENCRYPT_DEEP_SIFT_OUTPUT);
                    String finalExtractedContent = extractedContent;
                    if ("true".equals(encryptSotPageContent)) {
                        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
                        finalExtractedContent = encryption.encrypt(extractedContent, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
                    }

                    if (modelResponse.getOriginId() == null || modelResponse.getGroupId() == null ||
                            modelResponse.getTenantId() == null || modelResponse.getRootPipelineId() == null) {
                        log.error(aMarker, "Invalid response from model {}: missing required fields", entity.getModelName());
                        return;
                    }

                    parentObj.add(DeepSiftOutputTable.builder()
                            .inputFilePath(entity.getInputFilePath())
                            .extractedText(finalExtractedContent)
                            .originId(modelResponse.getOriginId())
                            .groupId(modelResponse.getGroupId().intValue())
                            .paperNo(entity.getPaperNo())
                            .createdOn(entity.getCreatedOn())
                            .createdBy(entity.getTenantId().toString())
                            .rootPipelineId(modelResponse.getRootPipelineId())
                            .tenantId(modelResponse.getTenantId())
                            .batchId(modelResponse.getBatchId())
                            .sourceDocumentType(entity.getSourceDocumentType())
                            .modelId(entity.getModelId())
                            .modelName(modelResponse.getModelName())
                            .timeTakenMS(elapsedTimeMs)
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .request(encryptRequestResponse(dbJsonRequest))
                            .response(encryptRequestResponse(responseBody))
                            .endpoint(String.valueOf(endpoint))
                            .build());
                }
            }
        } catch (Exception e) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            log.error(aMarker, "Exception occurred while processing request for originId: {} and model: {}",
                    entity.getOriginId(), entity.getModelName(), e);
            HandymanException handymanException = new HandymanException("Deep sift consumer failed for model " + entity.getModelName(), e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                    " paper no " + entity.getPaperNo() + " model " + entity.getModelName(), handymanException, action);
        }
    }

    public String getBase64ForPath(String imagePath) throws IOException {
        try {
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            log.info(aMarker, "Base64 created for file: {}", imagePath);
            return base64Image;
        } catch (Exception e) {
            log.error(aMarker, "Error creating base64: {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error creating base64", e);
        }
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
            return encryption.encrypt(request, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
        }
        return request;
    }

    private DeepSiftOutputTable buildOutputTable(DeepSiftInputTable entity, String status, String message,
                                                 String request, String response, String endpoint, long timeTakenMS) {
        return DeepSiftOutputTable.builder()
                .inputFilePath(entity.getInputFilePath())
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .paperNo(entity.getPaperNo())
                .createdBy(entity.getTenantId().toString())
                .rootPipelineId(entity.getRootPipelineId())
                .tenantId(entity.getTenantId())
                .batchId(entity.getBatchId())
                .sourceDocumentType(entity.getSourceDocumentType())
                .modelId(entity.getModelId())
                .modelName(entity.getModelName())
                .timeTakenMS(timeTakenMS)
                .status(status)
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(response))
                .endpoint(String.valueOf(endpoint))
                .build();
    }
}