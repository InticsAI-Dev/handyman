package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.FileProcessingUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_DEEP_SIFT_OUTPUT;
import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.core.enums.NetworkHandlerConstants.COPRO_CLIENT_SOCKET_TIMEOUT;
import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class DeepSiftConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftInputTable, DeepSiftOutputTable> {
    private static final String PROCESS_NAME = "DATA_EXTRACTION";
    private static final String ENCRYPTION_ALGORITHM = "AES256";
    private static final String TEXT_DATA_TYPE = "TEXT_DATA";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final Set<String> VALID_MODELS = Set.of("XENON", "ARGON", "KRYPTON", "OPTIMUS");

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final OkHttpClient httpClient;
    private final FileProcessingUtils fileProcessingUtils;
    private final ObjectMapper objectMapper;
    private final CoproRetryService coproRetryService;
    private final String processBase64;

    public DeepSiftConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, Integer pageContentMinLength, FileProcessingUtils fileProcessingUtils, String processBase64) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.fileProcessingUtils = fileProcessingUtils;
        this.objectMapper = new ObjectMapper();
        this.processBase64 = processBase64;
        int timeOut = Integer.parseInt(this.action.getContext().getOrDefault(COPRO_CLIENT_SOCKET_TIMEOUT, "100"));
        this.httpClient = new OkHttpClient.Builder()
                    .connectTimeout(timeOut, TimeUnit.MINUTES)
                    .writeTimeout(timeOut, TimeUnit.MINUTES)
                    .readTimeout(timeOut, TimeUnit.MINUTES)
                    .callTimeout(timeOut, TimeUnit.MINUTES)
                    .build();
        coproRetryService = new CoproRetryService(handymanRepo, httpClient, log);
    }

    @Override
    public List<DeepSiftOutputTable> process(URL endpoint, DeepSiftInputTable entity) throws IOException {
        List<DeepSiftOutputTable> parentObj = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        if (!VALID_MODELS.contains(entity.getModelName())) {
            String errorMessage = "Invalid model name: " + entity.getModelName() + " for originId: " + entity.getOriginId();
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        String inputFilePath = entity.getInputFilePath();
        if (inputFilePath == null || inputFilePath.trim().isEmpty()) {
            String errorMessage = "Input file path is null or empty for originId: " + entity.getOriginId();
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.canRead()) {
            String errorMessage = "Input file does not exist or is not readable";
            log.error(aMarker, errorMessage);
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, action);
        }

        log.info(aMarker, "Executing {} handler for endpoint: {}", entity.getModelName(), endpoint);
        DeepSiftRequest requestPayload = getRequestPayloadFromQuery(entity);

        // Set file path or base64 based on processing format
        String base64Content = processBase64.equals(ProcessFileFormatE.BASE64.name())
                ? fileProcessingUtils.convertFileToBase64(inputFilePath)
                : "";
        requestPayload.setBase64Img(base64Content);

        String jsonRequest = getXenonRequest(requestPayload);
        requestPayload.setBase64Img("");//To confirm with Andrews
        String dbJsonRequest = sanitizeRequestForDb(requestPayload);

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequest, MEDIA_TYPE))
                .build();
        requestExecutor(entity, request, parentObj, dbJsonRequest, endpoint, startTime);

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
        deepSiftRequest.setPaperNo(entity.getPaperNo());
        return deepSiftRequest;
    }

    private String getXenonRequest(DeepSiftRequest deepSiftRequest) throws JsonProcessingException {
        XenonRequest customRequest = XenonRequest.builder()
                .originId(deepSiftRequest.getOriginId())
                .batchId(deepSiftRequest.getBatchId())
                .paperNo(deepSiftRequest.getPaperNo())
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
                    .paperNo(deepSiftRequest.getPaperNo())
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
            String errorMessage = "Failed to sanitize DeepSiftRequest for DB";
            HandymanException handymanException = new HandymanException(errorMessage, e);
            HandymanException.insertException(errorMessage, handymanException, action);
            throw handymanException;
        }
    }


    private void requestExecutor(DeepSiftInputTable entity, Request request, List<DeepSiftOutputTable> parentObj,
                                 String dbJsonRequest, URL endpoint, long startTime) {

        CoproRetryErrorAuditTable auditInput = setErrorAuditInputDetails(entity, endpoint);
        Response response;
        try {
            response = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"))
                    ? coproRetryService.callCoproApiWithRetry(request, dbJsonRequest, auditInput, this.action)
                    : httpClient.newCall(request).execute();
            if (response == null) {
                String errorMessage = "No response received from API";
                parentObj.add(DeepSiftOutputTable.builder().batchId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).tenantId(entity.getTenantId()).createdOn(entity.getCreatedOn()).rootPipelineId(entity.getRootPipelineId()).request(encryptRequestResponse(dbJsonRequest)).response(errorMessage).endpoint(String.valueOf(endpoint)).build());
                log.error(aMarker, errorMessage);
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                throw new IOException(errorMessage);
            }

            try (Response safeResponse = response) {
                long elapsedTimeMs = System.currentTimeMillis() - startTime;
                if (safeResponse.body() == null) {
                    log.error(aMarker, "Response body is null for request to {} for model {}", endpoint, entity.getModelName());
                    HandymanException handymanException = new HandymanException("Deep sift consumer failed for model " + entity.getModelName());
                    HandymanException.insertException("Response body is null for request to " + endpoint + " for model " + entity.getModelName(), handymanException, action);
                }

                if (safeResponse.code() != 200) {
                    String errorMessage = "Response code is " + safeResponse.code() + " for request to " + endpoint + " for model " + entity.getModelName();
                    log.error(aMarker, errorMessage);
                    HandymanException handymanException = new HandymanException(errorMessage);
                    HandymanException.insertException(errorMessage, handymanException, action);
                }

                String responseBody = safeResponse.body().string();
                log.info(aMarker, "{} response: code={}, message={}", entity.getModelName(), safeResponse.code(), safeResponse.message());

                if (safeResponse.isSuccessful()) {
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
            }
        } catch (Exception e) {
            log.error(aMarker, "Exception occurred while processing request for originId: {} and model: {}",
                    entity.getOriginId(), entity.getModelName(), e);
            HandymanException handymanException = new HandymanException("Deep sift consumer failed for model " + entity.getModelName(), e);
            HandymanException.insertException("Deep sift consumer failed for origin Id " + entity.getOriginId() +
                    " paper no " + entity.getPaperNo() + " model " + entity.getModelName(), handymanException, action);
        }
    }

    private CoproRetryErrorAuditTable setErrorAuditInputDetails(DeepSiftInputTable entity, URL endPoint) {
        return  CoproRetryErrorAuditTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                .tenantId(entity.getTenantId())
                .processId(entity.getRootPipelineId())
                .filePath(entity.getInputFilePath())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .batchId(entity.getBatchId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .endpoint(String.valueOf(endPoint))
                .build();

    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
            return encryption.encrypt(request, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
        }
        return request;
    }

}