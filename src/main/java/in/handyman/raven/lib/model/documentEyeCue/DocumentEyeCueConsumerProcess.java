package in.handyman.raven.lib.model.documentEyeCue;

import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.EncryptionConstants;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.core.utils.ProcessFileFormatE;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.DocumentEyeCue;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterInput;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.model.common.CreateTimeStamp;

import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.exception.HandymanException.handymanRepo;

public class DocumentEyeCueConsumerProcess implements CoproProcessor.ConsumerProcess<DocumentEyeCueInputTable, DocumentEyeCueOutputTable> {

    public static final String PROCESS_NAME = "DOC_EYE_CUE";

    public final ActionExecutionAudit action;

    private final Logger log;

    private final Marker aMarker;

    private static final MediaType mediaTypeJson = MediaType.parse("application/json; charset=utf-8");

    private final FileProcessingUtils fileProcessingUtils;

    private final DocumentEyeCue documentEyeCue;

    private final String processBase64;

    private CoproRetryService coproRetryService;

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
        coproRetryService = new CoproRetryService(handymanRepo, httpclient);
    }

    private static final Marker MARKER = MarkerFactory.getMarker("DocumentEyeCue");

    private static final String KEY_REPOSITORY = "doc.eyecue.storecontent.repository";
    private static final String KEY_APPLICATION_ID = "doc.eyecue.storecontent.application.id";

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
        documentEyeCueRequest.setBase64Pdf("");//Confirm with Andrews
        String jsonInputRequest = objectMapper.writeValueAsString(documentEyeCueRequest);

        // Create HTTP request
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonInputRequest, mediaTypeJson))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        // Execute API call
        executeApiCall(entity, request, objectMapper, resultList, jsonInputRequest, endpoint, action);



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
        String base64Content = processBase64.equals(ProcessFileFormatE.BASE64.name())
                ? fileProcessingUtils.convertFileToBase64(entity.getFilePath())
                : "";
        documentEyeCueRequest.setBase64Pdf(base64Content);
        documentEyeCueRequest.setInputFilePath(entity.getFilePath());

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
                                List<DocumentEyeCueOutputTable> resultList, String jsonInputRequest, URL endpoint, ActionExecutionAudit action) {

        CoproRetryErrorAuditTable auditInput = setErrorAuditInputDetails(entity, endpoint);
        boolean isRetryEnabled = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"));
        Response response;
        try {
            if(isRetryEnabled) {
                response = coproRetryService.callCoproApiWithRetry(request, jsonInputRequest, auditInput, this.action);
            } else {
                response = httpclient.newCall(request).execute();
            }

            if (response == null) {
                String errorMessage = "No response received from API";
    //resultList.add(DocumentEyeCueOutputTable.builder().processId(entity.getBatchId()).originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null)).groupId(entity.getGroupId()).paperNo(entity.getPaperNo()).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).tenantId(tenantId).templateId(templateId).processId(processId).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).message(errorMessage).rootPipelineId(rootPipelineId).templateName(templateName).request(encryptRequestResponse(jsonRequest)).response(errorMessage).endpoint(String.valueOf(endpoint)).build());
                log.error(aMarker, errorMessage);
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);
                throw new IOException(errorMessage);
            }

            try (Response safeResponse = response) {
                if (safeResponse.isSuccessful()) {
                    handleSuccessfulResponse(entity, safeResponse, objectMapper, resultList, jsonInputRequest, endpoint);
                } else {
                    handleErrorResponse(entity, safeResponse, resultList, jsonInputRequest, endpoint);
                }
            }
        } catch (Exception exception) {
            handleException(entity, exception, resultList, jsonInputRequest, endpoint);
        }
    }


    private CoproRetryErrorAuditTable setErrorAuditInputDetails(DocumentEyeCueInputTable entity, URL endPoint) {
        CoproRetryErrorAuditTable retryAudit = CoproRetryErrorAuditTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .groupId(entity.getGroupId() != null ? Math.toIntExact(entity.getGroupId()) : null)
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .filePath(entity.getFilePath())
                .fileName(entity.getFileName())
                .templateId(entity.getTemplateId())
                .createdOn(entity.getCreatedOn())
                .rootPipelineId(entity.getRootPipelineId())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME + " - RETRY API CALL TO COPRO")
                .batchId(entity.getBatchId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .endpoint(String.valueOf(endPoint))
                .coproServiceId(1L)// Need to update service Id
                .build();
        //outputDir;
        //documentId;

        return retryAudit;
    }

    private void handleSuccessfulResponse(DocumentEyeCueInputTable entity,
                                          Response response,
                                          ObjectMapper objectMapper,
                                          List<DocumentEyeCueOutputTable> resultList,
                                          String jsonInputRequest,
                                          URL endpoint) throws IOException {
        log.info(aMarker, "Document EyeCue API call successful for origin_id {}", entity.getOriginId());

        String responseBody = Objects.requireNonNull(response.body()).string();

        try {
            DocumentEyeCueResponse documentEyeCueResponse = objectMapper.readValue(responseBody, DocumentEyeCueResponse.class);

            String outputFilePath = generateOutputFilePath(entity);

            if (processBase64.equals(ProcessFileFormatE.BASE64.name())
                    && documentEyeCueResponse.getProcessedPdfBase64() != null) {
                fileProcessingUtils.convertBase64ToFile(documentEyeCueResponse.getProcessedPdfBase64(), outputFilePath);
            } else {
                outputFilePath = documentEyeCueResponse.getProcessedPdfPath() != null
                        ? documentEyeCueResponse.getProcessedPdfPath()
                        : outputFilePath;
            }



            DocumentEyeCueOutputTable outputRecord = DocumentEyeCueOutputTable.builder()
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .processId(entity.getProcessId())
                    .templateId(entity.getTemplateId())
                    .processedFilePath(outputFilePath)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())

                    .stage(PROCESS_NAME)
                    .message(Optional.ofNullable(documentEyeCueResponse.getErrorMessage())
                            .orElse("Document EyeCue processing completed successfully"))
                    .createdOn(entity.getCreatedOn())
                    .rootPipelineId(entity.getRootPipelineId())
                    .batchId(entity.getBatchId())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .request(encryptRequestResponse(jsonInputRequest))
                    .response(encryptRequestResponse(responseBody))
                    .endpoint(endpoint.toString())
                    .encodedFilePath(encryptDocEyeBase64(documentEyeCueResponse.getProcessedPdfBase64()))
                    .docEyeCueDurationMs(documentEyeCueResponse.getDocEyeCueDurationMs())
                    .build();

            resultList.add(outputRecord);
            if(action.getContext().getOrDefault("doc.eyecue.storecontent.upload","false").equals("true")){
                log.info(MARKER, "StoreContent upload initiated for document_id: {} | origin_id: {}",
                        entity.getDocumentId(), entity.getOriginId());
                uploadToStoreContent(outputFilePath, documentEyeCueResponse.getProcessedPdfBase64(), entity);
            }

        } catch (JsonProcessingException e) {
            handleJsonProcessingException(entity, e, resultList, jsonInputRequest, responseBody, endpoint);
        }
    }

    private void uploadToStoreContent(String filePath, String processedPdfBase64, DocumentEyeCueInputTable entity) {
        try {
            log.info(MARKER, "StoreContent upload started for document_id: {} | filePath: {}",
                    entity.getDocumentId(), filePath);
            String repository = action.getContext().get(KEY_REPOSITORY);
            String applicationId = action.getContext().get(KEY_APPLICATION_ID);

            StoreContent storeContent = new StoreContent(log);
            StoreContentResponseDto response = storeContent.execute(
                    filePath,
                    processedPdfBase64,
                    repository,
                    applicationId,
                    entity,
                    action,
                    documentEyeCue
            );

            if (response != null) {
                log.info(MARKER, "StoreContent upload done for document_id: {} | contentId: {}",
                        entity.getDocumentId(), response.getContentID());
            } else {
                String warnMsg = "StoreContent upload returned null for document_id: " + entity.getDocumentId();
                HandymanException.insertException(warnMsg, new HandymanException(warnMsg), action);
                log.warn(MARKER, warnMsg);
            }
        } catch (Exception e) {
            String errorMessage = "StoreContent upload failed for document_id: " + entity.getDocumentId();
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
        }
    }


    private String generateOutputFilePath(DocumentEyeCueInputTable entity) {
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
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
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
        String encryptReqRes = action.getContext().get(EncryptionConstants.ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action,log).encrypt(data, "AES256", "DOCUMENT_EYE_CUE_REQUEST");
        }
        return data;
    }

    public String encryptDocEyeBase64(String data) {
        String encryptReqRes = action.getContext().get(EncryptionConstants.DOC_EYECUE_ENCRYPTION);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action,log).encrypt(data, "AES256", "DOCUMENT_EYE_CUE_REQUEST");
        }
        return data;
    }


}