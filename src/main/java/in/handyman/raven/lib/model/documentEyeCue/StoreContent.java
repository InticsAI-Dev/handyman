    package in.handyman.raven.lib.model.documentEyeCue;

    import in.handyman.raven.lambda.access.ResourceAccess;
    import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
    import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
    import com.anthem.acma.commonclient.storecontent.dto.StoreContentRequestDto;
    import com.anthem.acma.commonclient.storecontent.dto.Repository;
    import com.anthem.acma.commonclient.storecontent.logic.Acmastorecontentclient;
    import com.anthem.acma.commonclient.storecontent.logic.AcmastorecontentclientFactory;
    import in.handyman.raven.lib.model.DocumentEyeCue;
    import org.jdbi.v3.core.Jdbi;
    import org.slf4j.Logger;

    import java.io.BufferedInputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.nio.file.Files;
    import java.util.HashMap;
    import java.util.Properties;

    public class StoreContent {
        public DocumentEyeCue documentEyeCue;
        public Logger log;

        public StoreContentResponseDto execute(String filePath,
                                               String repository,
                                               String applicationId,
                                               DocumentEyeCueInputTable entity,
                                               ActionExecutionAudit action,
                                               DocumentEyeCue documentEyeCue) {

            StoreContentResponseDto responseDto = null;
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                log.error("File not found: {}", filePath);
                return null;
            }

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {

                // Environment URLs from context
                String envUrlStream = action.getContext().get("storecontent.streaming.url");
                String envUrlNonStream = action.getContext().get("storecontent.nonstreaming.url");

                Properties clientProps = new Properties();
                clientProps.setProperty("BASE_URL_STORECONTENTSTREAM", envUrlStream);
                clientProps.setProperty("BASE_URL_STORECONTENTNONSTREAM", envUrlNonStream);
                clientProps.setProperty("isApigeeInvoked", "True");

                // Request DTO
                StoreContentRequestDto requestDto = new StoreContentRequestDto();
                requestDto.setRepository(Repository.valueOf(repository));
                requestDto.setApplicationID(applicationId);

                // Content Metadata (fileName + "_updated")
                HashMap<String, String> contentMetadata = new HashMap<>();
                String updatedFileName;
                if (entity != null && entity.getFileName() != null && !entity.getFileName().isBlank()) {
                    updatedFileName = entity.getFileName() + "_updated";
                } else {
                    updatedFileName = file.getName() + "_updated";
                }
                contentMetadata.put("FileName", updatedFileName);
                contentMetadata.put("MimeType",
                        Files.probeContentType(file.toPath()) != null
                                ? Files.probeContentType(file.toPath())
                                : "application/pdf");
                requestDto.setContentMetaData(contentMetadata);

                // Additional params (versioning + documentId)
                HashMap<String, String> additionalParams = new HashMap<>();
                additionalParams.put("versioning", "Y");
                if (entity != null && entity.getDocumentId() != null) {
                    additionalParams.put("contentkey", entity.getDocumentId());
                } else {
                    log.warn("documentId not provided; contentkey will be empty");
                    additionalParams.put("contentkey", "");
                }
                additionalParams.put("contentkeytype", "DCN");
                requestDto.setAddtionalParams(additionalParams);

                // Headers (dynamic from context)
                HashMap<String, String> headers = new HashMap<>();
                headers.put("apikey", ApiKeyProvider.getDecryptedApiKey(action));
                headers.put("Accept", "application/json;charset=UTF-8");
                String bearerToken = BearerTokenProvider.fetchBearerToken(action);
                if (bearerToken != null && !bearerToken.isBlank()) {
                    headers.put("Authorization", "Bearer " + bearerToken);
                }
                requestDto.setHeaderMap(headers);

                // File stream
                requestDto.setContentData(bis);

                // Execute client
                Acmastorecontentclient client = AcmastorecontentclientFactory.createInstance(clientProps);
                responseDto = client.storeContent(requestDto);

                // Log response
                if (responseDto != null) {
                    log.info("Upload Complete - Status: {}", responseDto.getStatus());
                    log.info("Content ID: {}", responseDto.getContentID());
                    log.info("Message: {}", responseDto.getMessage());

                    saveStoreContentAudit(entity, filePath, responseDto, documentEyeCue);
                }else {
                    log.warn("Null response from StoreContent client.");
                }

            } catch (Exception e) {
                log.error("Error while uploading to StoreContent", e);
            }

            return responseDto;
        }

        private void saveStoreContentAudit(DocumentEyeCueInputTable entity,
                                           String filePath,
                                           StoreContentResponseDto responseDto,
                                           DocumentEyeCue documentEyeCue) {
            String sql = "INSERT INTO doc_eyecue.storecontent_upload_audit " +
                    "(origin_id, document_id, group_id, tenant_id, processed_file_path, " +
                    "storecontent_status, storecontent_message, storecontent_content_id, " +
                    "created_on, process_id, batch_id, last_updated_on, endpoint) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, now(), ?)";

            try {
                final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(documentEyeCue.getResourceConn());

                jdbi.useHandle(handle -> handle.execute(sql,
                        entity.getOriginId(),
                        entity.getDocumentId(),
                        entity.getGroupId(),
                        entity.getTenantId(),
                        filePath,
                        responseDto.getStatus(),
                        responseDto.getMessage(),
                        responseDto.getContentID(),
                        entity.getProcessId(),
                        entity.getBatchId(),
                        documentEyeCue.getEndpoint()
                ));

                log.info("StoreContent upload audit inserted for origin_id {}", entity.getOriginId());
            } catch (Exception e) {
                log.error("Failed to insert StoreContent upload audit for origin_id {}", entity.getOriginId(), e);
            }
        }

    }
