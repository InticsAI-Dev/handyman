package in.handyman.raven.core.encryption.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ProtegrityApiEncryptionImpl implements InticsDataEncryptionApi {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client ;
    private final String protegrityEncApiUrl;
    private final String protegrityDecApiUrl;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger logger;
    private static final HandymanRepo REPO = new HandymanRepoImpl();

    public ProtegrityApiEncryptionImpl(String encryptionUrl, String decryptionUrl, ActionExecutionAudit actionExecutionAudit, Logger logger) {
        this.protegrityEncApiUrl = encryptionUrl;
        this.protegrityDecApiUrl = decryptionUrl;
        this.actionExecutionAudit = actionExecutionAudit;
        this.logger = logger;
        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("protegrity.timeout.seconds", "60"));
        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (inputToken == null || inputToken.isBlank()) {
            logger.warn("Encryption skipped: inputToken is null or blank for sorItem: {}", sorItem);
            return inputToken;
        }

        logger.info("Encrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(inputToken, encryptionPolicy, sorItem, protegrityEncApiUrl);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            logger.warn("Decryption skipped: encryptedToken is null or blank for sorItem: {}", sorItem);
            return encryptedToken;
        }

        logger.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(encryptedToken, encryptionPolicy, sorItem, protegrityDecApiUrl);
    }

    @Override
    public List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) {
            //logger.warn("Encryption skipped: inputToken is null or blank for sorItem: {}", sorItem);
            return Collections.emptyList();
        }
        //logger.info("Encrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityListApi(requestList, protegrityEncApiUrl);
    }

    @Override
    public List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) {
            //logger.warn("Decryption skipped: encryptedToken is null or blank for sorItem: {}", sorItem);
            return Collections.emptyList();
        }

        //logger.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityListApi(requestList, protegrityDecApiUrl);
    }

    @Override
    public String getEncryptionMethod() {
        logger.info("Returning encryption method: PROTEGRITY_API_ENC");
        return "PROTEGRITY_API_ENC";
    }

    private String callProtegrityApi(String value, String policy, String key, String endpoint) throws HandymanException {
        long auditId = -1;
        String uuid = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            auditId = REPO.insertProtegrityAuditRecord(
                    key,
                    getEncryptionMethod(),
                    endpoint,
                    actionExecutionAudit.getRootPipelineId(),
                    actionExecutionAudit.getActionId(),
                    Thread.currentThread().getName(),
                    uuid
            );

            logger.info("Calling Protegrity API with auditId={}, uuid={}, endpoint={}, key={}", auditId, uuid, endpoint, key);

            List<EncryptionRequest> encryptionPayload = Collections.singletonList(
                    new EncryptionRequest(policy, value, key)
            );
            String jsonPayload = objectMapper.writeValueAsString(encryptionPayload);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                long tat = endTime - startTime;

                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    String errorMessage = String.format(
                            "FAILURE | uuid=%s | auditId=%d | key=%s | endpoint=%s | statusCode=%d | error=%s | TAT=%d ms",
                            uuid,
                            auditId,
                            key,
                            endpoint,
                            response.code(),
                            response.message(),
                            tat
                    );

                    logger.error(errorMessage);
                    REPO.updateProtegrityAuditRecord(auditId, "FAILED", errorMessage);
                    HandymanException exception = new HandymanException(response.message());
                    HandymanException.insertException("Protegrity API error [uuid=" + uuid + "]", exception, actionExecutionAudit);
                    throw exception;
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String encryptedValue = jsonResponse.get(0).get("value").asText();

                String successMessage = String.format(
                        "SUCCESS | uuid=%s | auditId=%d | key=%s | endpoint=%s | TAT=%d ms",
                        uuid,
                        auditId,
                        key,
                        endpoint,
                        tat
                );

                REPO.updateProtegrityAuditRecord(auditId, "SUCCESS", successMessage);
                logger.info("Protegrity API call SUCCESS [auditId={}, uuid={}, key={}, TAT={} ms]", auditId, uuid, key, tat);
                return encryptedValue;
            }

        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "EXCEPTION | uuid=%s | auditId=%d | key=%s | endpoint=%s | message=%s | TAT=%d ms",
                    uuid,
                    auditId,
                    key,
                    endpoint,
                    e.getMessage(),
                    tat
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);

            if (auditId != -1) {
                REPO.updateProtegrityAuditRecord(auditId, "FAILED", errMsg);
            }

            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            throw handymanException;
        }
    }



    //TODO to increase performance call this method
    private String callProtegrityApiList(List<EncryptionRequest> encryptionRequestLists, String endpoint) throws HandymanException {
        UUID uuid = UUID.randomUUID();
        try {
            logger.info("Calling Protegrity API with uuid {} at {}", uuid, endpoint);

            String jsonPayload = objectMapper.writeValueAsString(encryptionRequestLists);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Protegrity API error with uuid {} and message {} (Code: {})", uuid,
                            response.message(), response.code());
                    HandymanException.insertException("Protegrity API error: " + response.message(), new HandymanException(response.message()), actionExecutionAudit);
                }

                String responseBody = String.valueOf(response.body());

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String encryptedValue = jsonResponse.get(0).get("value").asText();
                logger.info("Protegrity API call successful with uuid {} and message : {}", uuid, response.message());
                return encryptedValue;
            }

        } catch (IOException e) {
            logger.error("Error calling Protegrity with uuid {} and message {}", uuid, e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error calling Protegrity API with uuid : " + uuid + " message : " + e.getMessage(), handymanException, actionExecutionAudit);
        }
        return "";
    }

    private List<EncryptionRequestClass> callProtegrityListApi(List<EncryptionRequestClass> protegrityList, String endpoint) throws HandymanException {
        long auditId = -1;
        String uuid = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        try {
            // Insert the Protegrity audit record into your database or repository
            auditId = REPO.insertProtegrityAuditRecord(
                    protegrityList.get(0).getKey(),
                    getEncryptionMethod(),
                    endpoint,
                    actionExecutionAudit.getRootPipelineId(),
                    actionExecutionAudit.getActionId(),
                    Thread.currentThread().getName(),
                    uuid
            );

            logger.info("Calling Protegrity API (BATCH) with auditId={}, uuid={}, endpoint={}, items={}",
                    auditId, uuid, endpoint, protegrityList.size());

            // Convert the entire list to JSON and send in one request
            String jsonPayload = objectMapper.writeValueAsString(protegrityList);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder().url(endpoint).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                long tat = endTime - startTime;

                String responseBody = response.body().string();

                List<EncryptionRequestClass> encryptedValues = objectMapper.readValue(responseBody, new TypeReference<List<EncryptionRequestClass>>(){});

                if (!response.isSuccessful()) {
                    String errorMessage = String.format(
                            "FAILURE | uuid=%s | auditId=%d | endpoint=%s | statusCode=%d | error=%s | TAT=%d ms | jsonSize=%d",
                            uuid, auditId, endpoint, response.code(), response.message(), tat, protegrityList.size()
                    );
                    logger.error(errorMessage);
                    REPO.updateProtegrityAuditRecord(auditId, "FAILED", errorMessage);
                    HandymanException exception = new HandymanException(response.message());
                    HandymanException.insertException("Protegrity API error [uuid=" + uuid + "]", exception, actionExecutionAudit);
                    throw exception;
                }

                String successMessage = String.format(
                        "SUCCESS | uuid=%s | auditId=%d | endpoint=%s | TAT=%d ms | jsonSize=%d",
                        uuid, auditId, endpoint, tat, protegrityList.size()
                );
                REPO.updateProtegrityAuditRecord(auditId, "SUCCESS", successMessage);
                logger.info("Protegrity API (BATCH) call SUCCESS [auditId={}, uuid={}, items={}, TAT={} ms]",
                        auditId, uuid, protegrityList.size(), tat);

                // Return the list of EncryptionRequestClass objects
            return encryptedValues;

            }

        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "EXCEPTION | uuid=%s | auditId=%d | endpoint=%s | message=%s | TAT=%d ms | jsonSize=%d",
                    uuid, auditId, endpoint, e.getMessage(), tat, protegrityList.size()
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);

            if (auditId != -1) {
                REPO.updateProtegrityAuditRecord(auditId, "FAILED", errMsg);
            }

            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            throw handymanException;
        }
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class EncryptionRequest {
        private String policy;
        private String value;
        private String key;
    }
}
