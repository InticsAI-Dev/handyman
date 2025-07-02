package in.handyman.raven.lib.model.documentEyeCue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.model.DocumentEyeCue;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.utils.FileProcessingUtils;
import in.handyman.raven.lib.utils.ProcessFileFormatE;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.lib.encryption.EncryptionConstants.DOC_EYECUE_ENCRYPTION;

public class DocumentEyeCueConsumerProcess implements CoproProcessor.ConsumerProcess<DocumentEyeCueInputTable, DocumentEyeCueOutputTable> {

    public static final String PROCESS_NAME = "DOC_EYE_CUE";

    public final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    private static final MediaType mediaTypeJson = MediaType.parse("application/json; charset=utf-8");

    private final FileProcessingUtils fileProcessingUtils;

    private final DocumentEyeCue documentEyeCue;

    private final String processBase64;

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public DocumentEyeCueConsumerProcess(Logger log, Marker aMarker,
                                         FileProcessingUtils fileProcessingUtils, ActionExecutionAudit action,
                                         String processBase64, DocumentEyeCue documentEyeCue) {
        this.log = log;
        this.aMarker = aMarker;
        this.fileProcessingUtils = fileProcessingUtils;
        this.action = action;
        this.processBase64 = processBase64;
        this.documentEyeCue = documentEyeCue;
    }

    @Override
    public List<DocumentEyeCueOutputTable> process(URL endpoint, DocumentEyeCueInputTable entity) throws Exception {
        log.info(aMarker, "Document EyeCue consumer process started with endpoint {} and File path {}",
                endpoint, entity.getFilePath());

        return documentEyeCueApiCall(entity, action, endpoint, documentEyeCue.getOutputDir());
    }

    private List<DocumentEyeCueOutputTable> documentEyeCueApiCall(DocumentEyeCueInputTable entity,
                                                                  ActionExecutionAudit action,
                                                                  URL endpoint,
                                                                  String outputDir) throws IOException {

        List<DocumentEyeCueOutputTable> resultList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        // Build request payload
        DocumentEyeCueRequest documentEyeCueRequest = buildRequest(entity, action, outputDir);
        String jsonInputRequest = objectMapper.writeValueAsString(documentEyeCueRequest);

        // Create HTTP request
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonInputRequest, mediaTypeJson))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        // Execute API call
        executeApiCall(entity, request, objectMapper, resultList, jsonInputRequest, endpoint);

        return resultList;
    }

    private DocumentEyeCueRequest buildRequest(DocumentEyeCueInputTable entity, ActionExecutionAudit action, String outputDir) throws IOException {
        DocumentEyeCueRequest documentEyeCueRequest = new DocumentEyeCueRequest();
        documentEyeCueRequest.setOriginId(entity.getOriginId());
        documentEyeCueRequest.setBatchId(entity.getBatchId());
        documentEyeCueRequest.setProcessId(String.valueOf(entity.getProcessId()));
        documentEyeCueRequest.setGroupId(String.valueOf(entity.getGroupId()));
        documentEyeCueRequest.setTenantId(entity.getTenantId().intValue());
        documentEyeCueRequest.setRootPipelineId(entity.getRootPipelineId().intValue());
        documentEyeCueRequest.setProcess(PROCESS_NAME);
        documentEyeCueRequest.setActionId(action.getActionId().intValue());
        documentEyeCueRequest.setOutputDir(outputDir);

        // Set file path or base64 based on processing format
        if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
            String base64Content = fileProcessingUtils.convertFileToBase64(entity.getFilePath());
            documentEyeCueRequest.setBase64Pdf(base64Content);
            documentEyeCueRequest.setInputFilePath(entity.getFilePath());
        } else {
            documentEyeCueRequest.setInputFilePath(entity.getFilePath());
        }

        // Set optional parameters from context if available
        setOptionalParameters(documentEyeCueRequest);

        return documentEyeCueRequest;
    }

    private void setOptionalParameters(DocumentEyeCueRequest documentEyeCueRequest) {
        // Get optional parameters from action context if they exist
        String language = action.getContext().get("document.eye.cue.language");
        if (language != null) {
            documentEyeCueRequest.setLanguage(language);
        }

        String forceOcr = action.getContext().get("document.eye.cue.force.ocr");
        if (forceOcr != null) {
            documentEyeCueRequest.setForceOcr(Boolean.parseBoolean(forceOcr));
        }

        String skipTextPages = action.getContext().get("document.eye.cue.skip.text.pages");
        if (skipTextPages != null) {
            documentEyeCueRequest.setSkipTextPages(Boolean.parseBoolean(skipTextPages));
        }

        String maxPaperCount = action.getContext().get("document.eye.cue.max.paper.count");
        if (maxPaperCount != null) {
            documentEyeCueRequest.setMaxPaperCount(Integer.parseInt(maxPaperCount));
        }
    }

    private void executeApiCall(DocumentEyeCueInputTable entity, Request request, ObjectMapper objectMapper,
                                List<DocumentEyeCueOutputTable> resultList, String jsonInputRequest, URL endpoint) {
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                handleSuccessfulResponse(entity, response, objectMapper, resultList, jsonInputRequest, endpoint);
            } else {
                handleErrorResponse(entity, response, resultList, jsonInputRequest, endpoint);
            }
        } catch (Exception exception) {
            handleException(entity, exception, resultList, jsonInputRequest, endpoint);
        }
    }

    private void handleSuccessfulResponse(DocumentEyeCueInputTable entity, Response response,
                                          ObjectMapper objectMapper, List<DocumentEyeCueOutputTable> resultList,
                                          String jsonInputRequest, URL endpoint) throws IOException {
        log.info(aMarker, "Document EyeCue API call successful for origin_id {}", entity.getOriginId());

        String responseBody = Objects.requireNonNull(response.body()).string();

        try {
            DocumentEyeCueResponse documentEyeCueResponse = objectMapper.readValue(responseBody, DocumentEyeCueResponse.class);

            DocumentEyeCueOutputTable outputRecord = DocumentEyeCueOutputTable.builder()
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .processId(entity.getProcessId())
                    .templateId(entity.getTemplateId())
                    .processedFilePath(documentEyeCueResponse.getProcessedPdfPath())
                    .status("COMPLETED")
                    .stage(PROCESS_NAME)
                    .message(documentEyeCueResponse.getErrorMessage() != null ?
                            documentEyeCueResponse.getErrorMessage() : "Document EyeCue processing completed successfully")
                    .createdOn(entity.getCreatedOn())
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .request(encryptRequestResponse(jsonInputRequest))
                    .response(encryptRequestResponse(responseBody))
                    .endpoint(endpoint.toString())
                    .encodedFilePath(encryptDocEyeBase64(documentEyeCueResponse.getProcessedPdfBase64()))
                    .build();

            // Handle base64 response if needed
            if (processBase64.equals(ProcessFileFormatE.BASE64.name()) &&
                    documentEyeCueResponse.getProcessedPdfBase64() != null) {
                String outputFilePath = generateOutputFilePath(entity);
                fileProcessingUtils.convertBase64ToFile(documentEyeCueResponse.getProcessedPdfBase64(), outputFilePath);
                outputRecord.setProcessedFilePath(outputFilePath);
            }

            resultList.add(outputRecord);

        } catch (JsonProcessingException e) {
            handleJsonProcessingException(entity, e, resultList, jsonInputRequest, responseBody, endpoint);
        }
    }

    private String generateOutputFilePath(DocumentEyeCueInputTable entity) {
        // Generate output file path based on original file name and response data
        return documentEyeCue.getOutputDir() + "/" + entity.getOriginId() + "_processed.pdf";
    }

    private void handleErrorResponse(DocumentEyeCueInputTable entity, Response response,
                                     List<DocumentEyeCueOutputTable> resultList, String jsonInputRequest, URL endpoint) {
        String errorMessage = String.format("Document EyeCue API call failed for origin_id %s, code: %d, message: %s",
                entity.getOriginId(), response.code(), response.message());

        createErrorRecord(entity, errorMessage, resultList, jsonInputRequest, response.message(), endpoint);

        HandymanException handymanException = new HandymanException(errorMessage);
        HandymanException.insertException(errorMessage, handymanException, this.action);
        log.error(aMarker, errorMessage);
    }

    private void handleException(DocumentEyeCueInputTable entity, Exception exception,
                                 List<DocumentEyeCueOutputTable> resultList, String jsonInputRequest, URL endpoint) {
        String errorMessage = String.format("Exception occurred during Document EyeCue processing for origin_id %s: %s",
                entity.getOriginId(), exception.getMessage());

        createErrorRecord(entity, errorMessage, resultList, jsonInputRequest,
                "Exception: " + exception.getMessage(), endpoint);

        HandymanException handymanException = new HandymanException(exception);
        HandymanException.insertException(errorMessage, handymanException, this.action);
        log.error(aMarker, "Exception occurred in Document EyeCue processing", exception);
    }

    private void handleJsonProcessingException(DocumentEyeCueInputTable entity, JsonProcessingException e,
                                               List<DocumentEyeCueOutputTable> resultList, String jsonInputRequest,
                                               String responseBody, URL endpoint) {
        String errorMessage = "Failed to parse Document EyeCue response for origin_id " + entity.getOriginId();

        createErrorRecord(entity, errorMessage, resultList, jsonInputRequest, responseBody, endpoint);

        HandymanException handymanException = new HandymanException(e);
        HandymanException.insertException(errorMessage, handymanException, this.action);
        log.error(aMarker, "JSON processing exception in Document EyeCue response", e);
    }

    private void createErrorRecord(DocumentEyeCueInputTable entity, String errorMessage,
                                   List<DocumentEyeCueOutputTable> resultList, String request,
                                   String response, URL endpoint) {
        DocumentEyeCueOutputTable errorRecord = DocumentEyeCueOutputTable.builder()
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .templateId(entity.getTemplateId())
                .status("FAILED")
                .stage(PROCESS_NAME)
                .message(errorMessage)
                .createdOn(entity.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .rootPipelineId(entity.getRootPipelineId())
                .batchId(entity.getBatchId())
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(response))
                .endpoint(endpoint.toString())
                .build();

        resultList.add(errorRecord);
    }

    public String encryptRequestResponse(String data) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action).encrypt(data, "AES256", "DOCUMENT_EYE_CUE_REQUEST");
        }
        return data;
    }

    public String encryptDocEyeBase64(String data) {
        String encryptReqRes = action.getContext().get(DOC_EYECUE_ENCRYPTION);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action).encrypt(data, "AES256", "DOCUMENT_EYE_CUE_REQUEST");
        }
        return data;
    }


}