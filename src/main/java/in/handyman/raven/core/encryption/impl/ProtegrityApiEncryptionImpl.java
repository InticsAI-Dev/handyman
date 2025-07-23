package in.handyman.raven.core.encryption.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final OkHttpClient client;
    private final String protegrityEncApiUrl;
    private final String protegrityDecApiUrl;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger logger;
    private static final HandymanRepo REPO = new HandymanRepoImpl();
    private final int maxRetries;

    public ProtegrityApiEncryptionImpl(String encryptionUrl, String decryptionUrl, ActionExecutionAudit actionExecutionAudit, Logger logger) {
        this.protegrityEncApiUrl = encryptionUrl;
        this.protegrityDecApiUrl = decryptionUrl;
        this.actionExecutionAudit = actionExecutionAudit;
        this.logger = logger;
        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("protegrity.timeout.seconds", "60"));
        this.maxRetries = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("protegrity.retry.count", "3"));
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
        return callProtegrityApiWithRetry(inputToken, encryptionPolicy, sorItem, protegrityEncApiUrl);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (encryptedToken == null || encryptedToken.isBlank()) {
            logger.warn("Decryption skipped: encryptedToken is null or blank for sorItem: {}", sorItem);
            return encryptedToken;
        }
        logger.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApiWithRetry(encryptedToken, encryptionPolicy, sorItem, protegrityDecApiUrl);
    }

    @Override
    public String getEncryptionMethod() {
        logger.info("Returning encryption method: PROTEGRITY_API_ENC");
        return "PROTEGRITY_API_ENC";
    }

    private String callProtegrityApiWithRetry(String value, String policy, String key, String endpoint) throws HandymanException {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Attempt {} for key={} with endpoint={}", attempt, key, endpoint);
                return callProtegrityApi(value, policy, key, endpoint, attempt);
            } catch (HandymanException e) {
                logger.error("Attempt {} failed for key={} with error={}", attempt, key, e.getMessage());
                if (attempt == maxRetries) throw e;
            }
        }
        throw new HandymanException("Unexpected error: all retry attempts failed");
    }

    private String callProtegrityApi(String value, String policy, String key, String endpoint, int attempt) throws HandymanException {
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

            List<EncryptionRequest> encryptionPayload = Collections.singletonList(new EncryptionRequest(policy, value, key));
            String jsonPayload = objectMapper.writeValueAsString(encryptionPayload);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder().url(endpoint).post(body).build();

            try (Response response = client.newCall(request).execute()) {
                long tat = System.currentTimeMillis() - startTime;
                String responseBody = response.body().string();

                if (!response.isSuccessful()) {
                    String errorMsg = String.format("FAILED | attempt=%d | auditId=%d | key=%s | status=%d | message=%s | TAT=%dms", attempt, auditId, key, response.code(), response.message(), tat);
                    logger.error(errorMsg);
                    REPO.updateProtegrityAuditRecord(auditId, "FAILED", errorMsg);
                    throw new HandymanException(response.message());
                }

                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String encryptedValue = jsonResponse.get(0).get("value").asText();

                String successMsg = String.format("SUCCESS | attempt=%d | auditId=%d | key=%s | TAT=%dms", attempt, auditId, key, tat);
                logger.info(successMsg);
                REPO.updateProtegrityAuditRecord(auditId, "SUCCESS", successMsg);
                return encryptedValue;
            }

        } catch (IOException e) {
            long tat = System.currentTimeMillis() - startTime;
            String errorMsg = String.format("EXCEPTION | attempt=%d | auditId=%d | key=%s | message=%s | TAT=%dms", attempt, auditId, key, e.getMessage(), tat);
            logger.error(errorMsg, e);
            if (auditId != -1) REPO.updateProtegrityAuditRecord(auditId, "FAILED", errorMsg);
            throw new HandymanException("Protegrity API call failed", e);
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
