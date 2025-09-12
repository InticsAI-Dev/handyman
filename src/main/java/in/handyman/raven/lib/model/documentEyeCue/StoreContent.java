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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

public class StoreContent {
    public DocumentEyeCue documentEyeCue;
    private static Logger log;

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

    private static final String SQL_INSERT_AUDIT =
            "INSERT INTO doc_eyecue.storecontent_upload_audit " +
                    "(origin_id, document_id, group_id, tenant_id, processed_file_path, " +
                    "storecontent_status, storecontent_message, storecontent_content_id, " +
                    "created_on, process_id, batch_id, last_updated_on, endpoint) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, now(), ?)";

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
            String envUrlStream = action.getContext().get(KEY_STREAMING_URL);
            String envUrlNonStream = action.getContext().get(KEY_NONSTREAMING_URL);
            String storeContentApiKey = action.getContext().get(KEY_STORECONTENT_API_KEY);

            Properties clientProps = new Properties();
            clientProps.setProperty(BASE_URL_STREAM, envUrlStream);
            clientProps.setProperty(BASE_URL_NONSTREAM, envUrlNonStream);
            clientProps.setProperty(PROP_IS_APIGEE_INVOKED, "True");

            StoreContentRequestDto requestDto = new StoreContentRequestDto();
            requestDto.setRepository(Repository.valueOf(repository));
            requestDto.setApplicationID(applicationId);

            HashMap<String, String> contentMetadata = new HashMap<>();
            String baseFileName;

            if (entity != null && entity.getFileName() != null && !entity.getFileName().isBlank()) {
                baseFileName = entity.getFileName();
            } else {
                baseFileName = file.getName();
            }

            String updatedFileName;
            if (baseFileName.toLowerCase().endsWith(".pdf")) {
                updatedFileName = baseFileName.substring(0, baseFileName.length() - 4) + "_updated.pdf";
            } else {
                updatedFileName = baseFileName + "_updated.pdf";
            }
            contentMetadata.put("FileName", updatedFileName);
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
            String bearerToken = BearerTokenProvider.fetchBearerToken(action);
            if (bearerToken != null && !bearerToken.isBlank()) {
                headers.put("Authorization", "Bearer " + bearerToken);
            }
            requestDto.setHeaderMap(headers);

            if (processedPdfBase64 != null && !processedPdfBase64.isBlank()) {
                byte[] decodedBytes = Base64.getDecoder().decode(processedPdfBase64);
                requestDto.setContentData(new ByteArrayInputStream(decodedBytes));
                requestDto.setSize(decodedBytes.length);

                log.info(MARKER, "Using processedPdfBase64 for StoreContent upload.");
            } else if (file.exists() && file.isFile()) {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                requestDto.setContentData(new ByteArrayInputStream(fileBytes));
                requestDto.setSize(fileBytes.length);

                log.info(MARKER, "Using file content for StoreContent upload.");
            } else {
                String errorMessage = "Neither processedPdfBase64 nor valid file available for StoreContent upload.";
                HandymanException.insertException(errorMessage, new HandymanException(errorMessage), action);
                log.error(MARKER, errorMessage);
            }

            Acmastorecontentclient client = AcmastorecontentclientFactory.createInstance(clientProps);
            responseDto = client.storeContent(requestDto);
            log.info("Invoking Acmastorecontentclient.storeContent() with repo and applicationId");

            if (responseDto != null) {
                log.info(MARKER, "Upload Complete - Status: {}", responseDto.getStatus());
                log.info(MARKER, "Content ID: {}", responseDto.getContentID());
                log.info(MARKER, "Message: {}", responseDto.getMessage());

                saveStoreContentAudit(entity, filePath, responseDto, documentEyeCue, action);
                log.info(MARKER, "StoreContent upload SUCCESS | document_id: {} | contentId: {} | file: {}",
                        entity != null ? entity.getDocumentId() : "N/A",
                        responseDto.getContentID(),
                        filePath);
            } else {
                String warnMsg = "Null response from StoreContent client.";
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

    private void saveStoreContentAudit(DocumentEyeCueInputTable entity,
                                       String filePath,
                                       StoreContentResponseDto responseDto,
                                       DocumentEyeCue documentEyeCue,
                                       ActionExecutionAudit action) {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(documentEyeCue.getResourceConn());
            String endpointUrl = documentEyeCue.getEndpoint();

            jdbi.useHandle(handle -> handle.execute(SQL_INSERT_AUDIT,
                    entity.getOriginId(),
                    entity.getDocumentId(),
                    entity.getGroupId(),
                    entity.getTenantId(),
                    filePath,
                    responseDto.getStatus(),
                    responseDto.getMessage(),
                    responseDto.getContentID(),
                    LocalDateTime.now(),
                    entity.getProcessId(),
                    entity.getRootPipelineId(),
                    entity.getBatchId(),
                    LocalDateTime.now(),
                    endpointUrl
            ));

            log.info(MARKER, "StoreContent upload audit inserted for origin_id {}", entity.getOriginId());
        } catch (Exception e) {
            String errorMessage = "Failed to insert StoreContent upload audit for origin_id " + entity.getOriginId();
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(errorMessage, handymanException, action);
            log.error(MARKER, errorMessage, e);
        }
    }
}
