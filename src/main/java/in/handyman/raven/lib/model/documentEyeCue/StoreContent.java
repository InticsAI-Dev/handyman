package in.handyman.raven.lib.model.documentEyeCue;

import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.exception.HandymanException;
import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import com.anthem.acma.commonclient.storecontent.dto.StoreContentRequestDto;
import com.anthem.acma.commonclient.storecontent.dto.Repository;
import com.anthem.acma.commonclient.storecontent.logic.Acmastorecontentclient;
import com.anthem.acma.commonclient.storecontent.logic.AcmastorecontentclientFactory;
import in.handyman.raven.lib.model.DocumentEyeCue;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoreContent {
    public DocumentEyeCue documentEyeCue;
    private static Logger log;

    private static final long STREAMING_THRESHOLD = 7_864_320; // 7.5 * 1024 * 1024
    private static final String KEY_FILE_RENAME_ACTIVATOR = "doc.eyecue.file.rename.activator";

    public StoreContent(Logger log) {
        this.log = log;
    }

    private static final Marker MARKER = MarkerFactory.getMarker("DocumentEyeCue");

    private static final String KEY_STREAMING_URL = "storecontent.streaming.url";
    private static final String KEY_NONSTREAMING_URL = "storecontent.nonstreaming.url";

    private static final String BASE_URL_STREAM = "BASE_URL_STORECONTENTSTREAM";
    private static final String BASE_URL_NONSTREAM = "BASE_URL_STORECONTENTNONSTREAM";
    private static final String PROP_IS_APIGEE_INVOKED = "isApigeeInvoked";

    private static final String DEFAULT_MIME_TYPE = "Application/pdf";
    private static final String VERSIONING_FLAG = "Y";
    private static final String CONTENT_KEY_TYPE = "DCN";
    private static final String CHANNEL_TYPE_VALUE = "SMRTINT";
    private static final String DOC_TYPE_VALUE = "LETTER";
    private static final String PLAN_VALUE = "SMARTINTAKE_WC_CLAIMS";
    private static final String KEY_STORECONTENT_API_KEY = "storecontent.api.key";
    private static final Pattern UPDATED_SUFFIX_PATTERN = Pattern.compile("(?i)(?:_updated)(\\d+)?$");

    private static final String SQL_INSERT_AUDIT =
            "INSERT INTO doc_eyecue.storecontent_upload_audit " +
                    "(origin_id, document_id, group_id, tenant_id, processed_file_path, " +
                    "storecontent_status, storecontent_message, storecontent_content_id, " +
                    "created_on, process_id, root_pipeline_id, batch_id, last_updated_on, endpoint, upload_type, file_name, file_size) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, now(), ?, ?, ?, ?)";

    public StoreContentResponseDto execute(String filePath,
                                           String processedPdfBase64,
                                           String repository,
                                           String applicationId,
                                           DocumentEyeCueInputTable entity,
                                           ActionExecutionAudit action,
                                           DocumentEyeCue documentEyeCue) {
        log.info("{} - Initiating StoreContent upload for file: {}", MARKER, filePath);

        StoreContentResponseDto responseDto = null;
        File file = new File(filePath);

        if (!file.exists() || !file.isFile()) {
            String errorMessage = "File not found: " + filePath;
            HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
            log.error(MARKER, errorMessage);
            return null;
        }
        log.info("{} - File found: {}", MARKER, filePath);

        try {
            long fileSize = getFileSize(file, processedPdfBase64);
            boolean isStreaming = fileSize > STREAMING_THRESHOLD;
            String uploadType = isStreaming ? "STREAMING" : "NON_STREAMING";

            log.info("{} - File size: {} bytes, Upload type: {}", MARKER, fileSize, uploadType);

            String envUrlStream = action.getContext().get(KEY_STREAMING_URL);
            String envUrlNonStream = action.getContext().get(KEY_NONSTREAMING_URL);
            String storeContentApiKey = action.getContext().get(KEY_STORECONTENT_API_KEY);

            Properties clientProps = new Properties();
            clientProps.setProperty(BASE_URL_STREAM, envUrlStream);
            clientProps.setProperty(BASE_URL_NONSTREAM, envUrlNonStream);
            clientProps.setProperty(PROP_IS_APIGEE_INVOKED, "True");

            StoreContentRequestDto requestDto;
            if (isStreaming) {
                requestDto = createStreamingRequest(file, repository, applicationId,
                        entity, action, storeContentApiKey);
            } else {
                requestDto = createNonStreamingRequest(file, processedPdfBase64, repository, applicationId,
                        entity, action, storeContentApiKey);
            }

            if (requestDto == null) {
                log.error(MARKER, "Failed to create request DTO for upload type: {}", uploadType);
                return null;
            }

            Acmastorecontentclient client = AcmastorecontentclientFactory.createInstance(clientProps);
            responseDto = client.storeContent(requestDto);
            log.info("Invoking Acmastorecontentclient.storeContent() with repo and applicationId - Upload type: {}", uploadType);

            if (responseDto != null) {
                log.info(MARKER, "Upload Complete - Status: {}, Upload type: {}", responseDto.getStatus(), uploadType);
                log.info(MARKER, "Content ID: {}", responseDto.getContentID());
                log.info(MARKER, "Message: {}", responseDto.getMessage());

                saveStoreContentAudit(entity, filePath, responseDto, documentEyeCue, action, uploadType);
                log.info(MARKER, "StoreContent upload SUCCESS | document_id: {} | contentId: {} | file: {} | type: {}",
                        entity != null ? entity.getDocumentId() : "N/A",
                        responseDto.getContentID(),
                        filePath,
                        uploadType);
            } else {
                String warnMsg = "Null response from StoreContent client for upload type: " + uploadType;
                HandymanException.insertException(warnMsg, new HandymanException(warnMsg), action);
                log.warn(MARKER, warnMsg);
            }

        } catch (Exception e) {
            String errorMessage = "Error while uploading to StoreContent";
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
        }

        return responseDto;
    }

    private long getFileSize(File file, String processedPdfBase64) {
        try {
            if (processedPdfBase64 != null && !processedPdfBase64.isBlank()) {
                byte[] decodedBytes = Base64.getDecoder().decode(processedPdfBase64);
                return decodedBytes.length;
            } else if (file.exists() && file.isFile()) {
                return file.length();
            }
        } catch (Exception e) {
            log.warn(MARKER, "Error calculating file size, defaulting to 0: {}", e.getMessage());
        }
        return 0L;
    }

    private String incrementUpdatedFileName(String baseFileName) {
        if (baseFileName == null || baseFileName.isBlank()) return baseFileName;

        int lastDot = baseFileName.lastIndexOf('.');
        String namePart = (lastDot >= 0) ? baseFileName.substring(0, lastDot) : baseFileName;
        String extPart = (lastDot >= 0) ? baseFileName.substring(lastDot) : "";

        if (extPart.isBlank() || extPart.equals(".")) {
            extPart = ".pdf";
        }

        Matcher m = UPDATED_SUFFIX_PATTERN.matcher(namePart);
        if (m.find()) {
            String numberGroup = m.group(1);
            if (numberGroup == null || numberGroup.isEmpty()) {
                String newName = namePart.replaceAll("(?i)(_updated)$", "_updated2");
                return newName + extPart;
            } else {
                int n;
                try {
                    n = Integer.parseInt(numberGroup);
                } catch (NumberFormatException e) {
                    n = 1;
                }
                int next = n + 1;
                String newName = namePart.replaceAll("(?i)(_updated)\\d+$", "$1" + next);
                return newName + extPart;
            }
        } else {
            return namePart + "_updated" + extPart;
        }
    }

    private String getFileName(String baseFileName, ActionExecutionAudit action, String context) {
        String fileRenameActivator = action.getContext().get(KEY_FILE_RENAME_ACTIVATOR);
        boolean shouldRename = "true".equalsIgnoreCase(fileRenameActivator);

        if (shouldRename) {
            String updatedFileName = incrementUpdatedFileName(baseFileName);
            log.info(MARKER, "{} - File rename activator is enabled. Using renamed filename: {}",
                    context, updatedFileName);
            return updatedFileName;
        } else {
            log.info(MARKER, "{} - File rename activator is disabled. Using original filename: {}",
                    context, baseFileName);
            return baseFileName;
        }
    }

    private StoreContentRequestDto createNonStreamingRequest(File file,
                                                             String processedPdfBase64,
                                                             String repository,
                                                             String applicationId,
                                                             DocumentEyeCueInputTable entity,
                                                             ActionExecutionAudit action,
                                                             String storeContentApiKey) {
        try {
            StoreContentRequestDto requestDto = new StoreContentRequestDto();
            requestDto.setRepository(Repository.valueOf(repository));
            requestDto.setApplicationID(applicationId);

            HashMap<String, String> contentMetadata = new HashMap<>();
            String baseFileName = (entity != null && entity.getFileName() != null && !entity.getFileName().isBlank())
                    ? entity.getFileName()
                    : file.getName();

            String fileName = getFileName(baseFileName, action, "NON-STREAMING");
            contentMetadata.put("FileName", fileName);
            contentMetadata.put("MimeType",
                    Files.probeContentType(file.toPath()) != null
                            ? Files.probeContentType(file.toPath())
                            : DEFAULT_MIME_TYPE);
            contentMetadata.put("CHANNEL_TYPE", CHANNEL_TYPE_VALUE);
            contentMetadata.put("DocType", DOC_TYPE_VALUE);
            requestDto.setContentMetaData(contentMetadata);

            HashMap<String, String> additionalParams = new HashMap<>();
            additionalParams.put("versioning", VERSIONING_FLAG);

            if (entity != null && entity.getDocumentId() != null) {
                additionalParams.put("contentkey", entity.getDocumentId());
            } else {
                String warnMsg = "documentId not provided; contentkey will be empty";
                HandymanException.insertException(warnMsg, new HandymanException(warnMsg), action);
                log.warn(MARKER, warnMsg);
                additionalParams.put("contentkey", "");
            }

            additionalParams.put("contentkeytype", CONTENT_KEY_TYPE);
            additionalParams.put("plan", PLAN_VALUE);
            requestDto.setAddtionalParams(additionalParams);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("apikey", storeContentApiKey);
            headers.put("Content-Type", "application/json");
            String bearerToken = BearerTokenProvider.fetchBearerToken(action, log, MARKER);
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.put("Authorization", "Bearer " + bearerToken);
            }
            requestDto.setHeaderMap(headers);

            if (processedPdfBase64 != null && !processedPdfBase64.isBlank()) {
                byte[] decodedBytes = Base64.getDecoder().decode(processedPdfBase64);
                requestDto.setContentData(new ByteArrayInputStream(decodedBytes));
                requestDto.setSize(decodedBytes.length);
                log.info(MARKER, "Using processedPdfBase64 for NON-STREAMING upload.");
            } else if (file.exists() && file.isFile()) {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                requestDto.setContentData(new ByteArrayInputStream(fileBytes));
                requestDto.setSize(fileBytes.length);
                log.info(MARKER, "Using file content for NON-STREAMING upload.");
            } else {
                String errorMessage = "Neither processedPdfBase64 nor valid file available for NON-STREAMING upload.";
                HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
                log.error(MARKER, errorMessage);
            }

            return requestDto;

        } catch (Exception e) {
            String errorMessage = "Error creating non-streaming request";
            HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
            log.error(MARKER, errorMessage, e);
            return null;
        }
    }

    private StoreContentRequestDto createStreamingRequest(File file,
                                                          String repository,
                                                          String applicationId,
                                                          DocumentEyeCueInputTable entity,
                                                          ActionExecutionAudit action,
                                                          String storeContentApiKey) {
        try {
            StoreContentRequestDto requestDto = new StoreContentRequestDto();
            requestDto.setRepository(Repository.valueOf(repository));
            requestDto.setApplicationID(applicationId);

            HashMap<String, String> contentMetadata = new HashMap<>();
            String baseFileName = (entity != null && entity.getFileName() != null && !entity.getFileName().isBlank())
                    ? entity.getFileName()
                    : file.getName();

            String fileName = getFileName(baseFileName, action, "STREAMING");
            contentMetadata.put("FileName", fileName);
            contentMetadata.put("MimeType", DEFAULT_MIME_TYPE);
            requestDto.setContentMetaData(contentMetadata);

            HashMap<String, String> additionalParams = new HashMap<>();
            additionalParams.put("versioning", VERSIONING_FLAG);
            if (entity != null && entity.getDocumentId() != null) {
                additionalParams.put("contentkey", entity.getDocumentId());
            } else {
                String warnMsg = "documentId not provided; contentkey will be empty";
                HandymanException.insertException(warnMsg, new HandymanException(warnMsg), action);
                log.warn(MARKER, warnMsg);
                additionalParams.put("contentkey", "");
            }
            additionalParams.put("contentkeytype", CONTENT_KEY_TYPE);
            additionalParams.put("plan", PLAN_VALUE);
            requestDto.setAddtionalParams(additionalParams);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("apikey", storeContentApiKey);
            String bearerToken = BearerTokenProvider.fetchBearerToken(action, log, MARKER);
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.put("Authorization", "Bearer " + bearerToken);
            }
            requestDto.setHeaderMap(headers);

            if (file.exists() && file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                requestDto.setContentData(fis);
                requestDto.setSize(file.length());
                log.info(MARKER, "Using file for STREAMING multipart upload: {}", file.getAbsolutePath());
            } else {
                String errorMessage = "Valid file not available for STREAMING upload.";
                HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
                log.error(MARKER, errorMessage);
            }

            return requestDto;

        } catch (Exception e) {
            String errorMessage = "Error creating streaming multipart request";
            HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
            log.error(MARKER, errorMessage, e);
            return null;
        }
    }

    private void saveStoreContentAudit(DocumentEyeCueInputTable entity,
                                       String filePath,
                                       StoreContentResponseDto responseDto,
                                       DocumentEyeCue documentEyeCue,
                                       ActionExecutionAudit action,
                                       String uploadType) {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(documentEyeCue.getResourceConn());
            String endpointUrl = documentEyeCue.getEndpoint();

            File file = new File(filePath);
            String fileName = file.getName();
            long fileSize = file.exists() ? file.length() : 0L;

            jdbi.useHandle(handle -> handle.execute(SQL_INSERT_AUDIT,
                    entity.getOriginId(),
                    entity.getDocumentId(),
                    entity.getGroupId(),
                    entity.getTenantId(),
                    filePath,
                    responseDto.getStatus(),
                    responseDto.getMessage(),
                    responseDto.getContentID(),
                    entity.getProcessId(),
                    entity.getRootPipelineId(),
                    entity.getBatchId(),
                    endpointUrl,
                    uploadType,
                    fileName,
                    fileSize
            ));

            log.info(MARKER, "StoreContent upload audit inserted for origin_id {} with upload type {}",
                    entity.getOriginId(), uploadType);
        } catch (Exception e) {
            String errorMessage = "Failed to insert StoreContent upload audit for origin_id " + entity.getOriginId();
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
        }
    }
}
