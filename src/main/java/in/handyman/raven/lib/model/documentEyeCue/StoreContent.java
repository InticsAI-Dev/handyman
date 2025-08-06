    package in.handyman.raven.lib.model.documentEyeCue;

    import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
    import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
    import com.anthem.acma.commonclient.storecontent.dto.StoreContentRequestDto;
    import com.anthem.acma.commonclient.storecontent.dto.Repository;
    import com.anthem.acma.commonclient.storecontent.logic.Acmastorecontentclient;
    import com.anthem.acma.commonclient.storecontent.logic.AcmastorecontentclientFactory;
    import lombok.extern.slf4j.Slf4j;

    import java.io.BufferedInputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.nio.file.Files;
    import java.util.HashMap;
    import java.util.Properties;

    @Slf4j
    public class StoreContent {

        /**
         * Uploads a file to StoreContent API using Anthem client JAR.
         * Automatically maps:
         * - documentId → contentkey
         * - fileName + "_updated" → FileName in metadata
         *
         * @param filePath Local file path (processed file)
         * @param repository Repository name (e.g., FilenetCE)
         * @param applicationId Application ID (e.g., CUE)
         * @param entity DocumentEyeCueInputTable containing documentId & fileName
         * @param action ActionExecutionAudit for API key and token retrieval
         * @return StoreContentResponseDto
         */
        public StoreContentResponseDto execute(String filePath,
                                               String repository,
                                               String applicationId,
                                               DocumentEyeCueInputTable entity,
                                               ActionExecutionAudit action) {

            StoreContentResponseDto responseDto = null;
            File file = new File(filePath);

            if (!file.exists() || !file.isFile()) {
                log.error("❌ File not found: {}", filePath);
                return null;
            }

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {

                // 1️⃣ Environment URLs from context
                String envUrlStream = action.getContext().get("storecontent.streaming.url");
                String envUrlNonStream = action.getContext().get("storecontent.nonstreaming.url");

                Properties clientProps = new Properties();
                clientProps.setProperty("BASE_URL_STORECONTENTSTREAM", envUrlStream);
                clientProps.setProperty("BASE_URL_STORECONTENTNONSTREAM", envUrlNonStream);
                clientProps.setProperty("isApigeeInvoked", "True");

                // 2️⃣ Request DTO
                StoreContentRequestDto requestDto = new StoreContentRequestDto();
                requestDto.setRepository(Repository.valueOf(repository));
                requestDto.setApplicationID(applicationId);

                // 3️⃣ Content Metadata (fileName + "_updated")
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

                // 4️⃣ Additional params (versioning + documentId)
                HashMap<String, String> additionalParams = new HashMap<>();
                additionalParams.put("versioning", "Y");
                if (entity != null && entity.getDocumentId() != null) {
                    additionalParams.put("contentkey", entity.getDocumentId());
                } else {
                    log.warn("⚠️ documentId not provided; contentkey will be empty");
                    additionalParams.put("contentkey", "");
                }
                additionalParams.put("contentkeytype", "DCN");
                requestDto.setAddtionalParams(additionalParams);

                // 5️⃣ Headers (dynamic from context)
                HashMap<String, String> headers = new HashMap<>();
                headers.put("apikey", ApiKeyProvider.getDecryptedApiKey(action));
                headers.put("Accept", "application/json;charset=UTF-8");
                String bearerToken = BearerTokenProvider.fetchBearerToken(action);
                if (bearerToken != null && !bearerToken.isBlank()) {
                    headers.put("Authorization", "Bearer " + bearerToken);
                }
                requestDto.setHeaderMap(headers);

                // 6️⃣ File stream
                requestDto.setContentData(bis);

                // 7️⃣ Execute client
                Acmastorecontentclient client = AcmastorecontentclientFactory.createInstance(clientProps);
                responseDto = client.storeContent(requestDto);

                // 8️⃣ Log response
                if (responseDto != null) {
                    log.info("✅ Upload Complete - Status: {}", responseDto.getStatus());
                    log.info("   Content ID: {}", responseDto.getContentID());
                    log.info("   Message: {}", responseDto.getMessage());
                } else {
                    log.warn("⚠️ Null response from StoreContent client.");
                }

            } catch (Exception e) {
                log.error("❌ Error while uploading to StoreContent", e);
            }

            return responseDto;
        }
    }
