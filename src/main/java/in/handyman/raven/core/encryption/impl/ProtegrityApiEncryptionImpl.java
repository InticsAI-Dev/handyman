package in.handyman.raven.core.encryption.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.InticsDataEncryptionApi;
import in.handyman.raven.core.encryption.ProtegrityApiAudit;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static in.handyman.raven.core.enums.EncryptionConstants.*;

public class ProtegrityApiEncryptionImpl implements InticsDataEncryptionApi {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient client;
    private final String protegrityEncApiUrl;
    private final String protegrityDecApiUrl;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger logger;
    private static final HandymanRepo REPO = new HandymanRepoImpl();

    // Retry configuration
    private final int maxRetryAttempts;
    private final long retryIntervalSecs;
    private final Set<Integer> retryableStatusCodes;
    private final boolean retryActivated;


    public ProtegrityApiEncryptionImpl(String encryptionUrl, String decryptionUrl, ActionExecutionAudit actionExecutionAudit, Logger logger) {
        this.protegrityEncApiUrl = encryptionUrl;
        this.protegrityDecApiUrl = decryptionUrl;
        this.actionExecutionAudit = actionExecutionAudit;
        this.logger = logger;

        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault(PROTEGRITY_API_TIMEOUT, "60"));
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .build();

        // Initialize retry configuration from context
        this.maxRetryAttempts = Integer.parseInt(
                actionExecutionAudit.getContext().getOrDefault(PROTEGRITY_API_RETRY_COUNT, "3")
        );

        this.retryIntervalSecs = Long.parseLong(
                actionExecutionAudit.getContext().getOrDefault(PROTEGRITY_API_RETRY_INTERVAL_SEC, "1")
        )*1000;

        // Parse retryable status codes from comma-separated string
        String retryableCodesStr = actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_STATUS_CODES, "404,400,408,429,500,502,503,504");
        this.retryableStatusCodes = Stream.of(retryableCodesStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        this.retryActivated = Boolean.parseBoolean(
                actionExecutionAudit.getContext().getOrDefault(PROTEGRITY_API_RETRY_ACTIVATE, "true")
        );

        logger.info("Retry configuration initialized - maxAttempts: {}, intervalSec: {}, retryableCodes: {}",
                maxRetryAttempts, retryIntervalSecs, retryableStatusCodes);
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

    public List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }
        return callProtegrityListApi(requestList, protegrityEncApiUrl);
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
    public List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) {
            return Collections.emptyList();
        }
        return callProtegrityListApi(requestList, protegrityDecApiUrl);
    }

    @Override
    public String getEncryptionMethod() {
        logger.info("Returning encryption method: PROTEGRITY_API_ENC");
        return "PROTEGRITY_API_ENC";
    }

    private String callProtegrityApi(String value, String policy, String key, String endpoint) {
        String uuid = UUID.randomUUID().toString() + "-" + System.currentTimeMillis(); // add more uniqueness
        int attempt = 0;

        try {
            // Build request payload
            List<EncryptionRequest> payload = Collections.singletonList(new EncryptionRequest(policy, value, key));
            String jsonPayload = objectMapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder().url(endpoint).post(body).build();

            // Retry loop
            while (attempt < maxRetryAttempts) {
                attempt++;
                long attemptStart = System.currentTimeMillis();
                ProtegrityApiAudit audit = null;

                try {
                    audit = ProtegrityApiAudit.builder()
                            .key(actionExecutionAudit.getActionName())
                            .encryptionType(getEncryptionMethod())
                            .endpoint(endpoint)
                            .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                            .actionId(actionExecutionAudit.getActionId())
                            .threadName(Thread.currentThread().getName())
                            .uuid(uuid)
                            .startedOn(Timestamp.valueOf(LocalDateTime.now()))
                            .build();

                    logger.info("Protegrity API CALL START | uuid={} | endpoint={} | key={} | attempt={}/{}",
                            uuid, endpoint, key, attempt, maxRetryAttempts);

                    try (Response response = client.newCall(request).execute()) {
                        long attemptEnd = System.currentTimeMillis();
                        long tat = attemptEnd - attemptStart;

                        String responseBody = response.body() != null ? response.body().string() : null;

                        // Retry if response is empty/null
                        if (responseBody == null || responseBody.isBlank()) {
                            boolean canRetry = retryActivated && attempt < maxRetryAttempts;

                            String msg = String.format(
                                    "EMPTY RESPONSE | uuid=%s | endpoint=%s | key=%s | attempt=%d/%d | retryable=%s",
                                    uuid, endpoint, key, attempt, maxRetryAttempts, canRetry
                            );

                            logger.error("Protegrity API {}", msg);
                            if (audit != null) {
                                audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                                audit.setStatus("FAILED");
                                audit.setMessage(msg);
                                REPO.insertProtegrityAudit(audit);
                            }

                            if (canRetry) {
                                logger.warn("Retrying due to empty response after {} ms | uuid={} | attempt={}/{}",
                                        retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            }

                            HandymanException ex = new HandymanException("Protegrity API returned empty or null response");
                            HandymanException.insertException(msg, ex, actionExecutionAudit);
                            return null;
                        }

                        // Retry if response is not successful
                        if (!response.isSuccessful()) {
                            boolean isRetryable = retryableStatusCodes.contains(response.code());
                            boolean canRetry = retryActivated && isRetryable && attempt < maxRetryAttempts;

                            String msg = String.format(
                                    "FAILURE | uuid=%s | endpoint=%s | key=%s | code=%d | error=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                                    uuid, endpoint, key, response.code(), response.message(),
                                    attempt, maxRetryAttempts, canRetry, tat
                            );

                            logger.error("Protegrity API {}", msg);
                            if (audit != null) {
                                audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                                audit.setStatus("FAILED");
                                audit.setMessage(msg);
                                REPO.insertProtegrityAudit(audit);
                            }

                            if (canRetry) {
                                logger.warn("Retrying in {} ms | uuid={} | attempt={}/{}", retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            }

                            HandymanException ex = new HandymanException("Protegrity API error: " + response.message());
                            HandymanException.insertException(msg, ex, actionExecutionAudit);
                            return null;
                        }

                        // SUCCESS
                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        String encryptedValue = jsonResponse.get(0).get("value").asText();

                        String successMsg = String.format(
                                "SUCCESS | uuid=%s | endpoint=%s | key=%s | attempt=%d/%d | TAT=%d ms",
                                uuid, endpoint, key, attempt, maxRetryAttempts, tat
                        );

                        logger.info("Protegrity API {}", successMsg);
                        if (audit != null) {
                            audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                            audit.setStatus("SUCCESS");
                            audit.setMessage(successMsg);
                            REPO.insertProtegrityAudit(audit);
                        }

                        return encryptedValue;
                    }

                } catch (IOException e) {
                    long attemptEnd = System.currentTimeMillis();
                    long tat = attemptEnd - attemptStart;
                    boolean canRetry = retryActivated && attempt < maxRetryAttempts;

                    String errMsg = String.format(
                            "EXCEPTION | uuid=%s | endpoint=%s | key=%s | msg=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                            uuid, endpoint, key, e.getMessage(), attempt, maxRetryAttempts, canRetry, tat
                    );

                    logger.error("Protegrity API {}", errMsg, e);
                    if (audit != null) {
                        audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                        audit.setStatus("FAILED");
                        audit.setMessage(errMsg);
                        REPO.insertProtegrityAudit(audit);
                    }

                    if (canRetry) {
                        logger.warn("Retrying after {} ms due to IOException | uuid={} | attempt={}/{}",
                                retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                        Thread.sleep(retryIntervalSecs);
                        continue;
                    }

                    HandymanException ex = new HandymanException("IOException during Protegrity API", e, actionExecutionAudit);
                    HandymanException.insertException(errMsg, ex, actionExecutionAudit);
                    return null;
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Protegrity API INTERRUPTED | uuid={} | endpoint={} | key={} | msg={}", uuid, endpoint, key, e.getMessage(), e);
            HandymanException ex = new HandymanException("Retry interrupted", e, actionExecutionAudit);
            HandymanException.insertException("Protegrity API interrupted [uuid=" + uuid + "]", ex, actionExecutionAudit);
        } catch (Exception e) {
            logger.error("Protegrity API UNEXPECTED EXCEPTION | uuid={} | endpoint={} | key={} | msg={}", uuid, endpoint, key, e.getMessage(), e);
            HandymanException ex = new HandymanException("Unexpected Protegrity API error", e, actionExecutionAudit);
            HandymanException.insertException("Unexpected error [uuid=" + uuid + "]", ex, actionExecutionAudit);
        }

        logger.error("Protegrity API returning NULL result | uuid={} | endpoint={}", uuid, endpoint);
        return null;
    }

    private List<EncryptionRequestClass> callProtegrityListApi(
            List<EncryptionRequestClass> protegrityList,
            String endpoint
    ) {
        String uuid = UUID.randomUUID().toString() + "-" + System.currentTimeMillis(); // added more uniqueness
        int attempt = 0;

        try {
            String jsonPayload = objectMapper.writeValueAsString(protegrityList);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder().url(endpoint).post(body).build();

            while (attempt < maxRetryAttempts) {
                attempt++;
                long attemptStart = System.currentTimeMillis();
                ProtegrityApiAudit audit = null;

                try {
                    audit = ProtegrityApiAudit.builder()
                            .key(actionExecutionAudit.getActionName())
                            .encryptionType(getEncryptionMethod())
                            .endpoint(endpoint)
                            .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                            .actionId(actionExecutionAudit.getActionId())
                            .threadName(Thread.currentThread().getName())
                            .uuid(uuid)
                            .startedOn(Timestamp.valueOf(LocalDateTime.now()))
                            .build();

                    logger.info(
                            "Protegrity API [BATCH] CALL START | uuid={} | endpoint={} | items={} | attempt={}/{}",
                            uuid, endpoint, protegrityList.size(), attempt, maxRetryAttempts
                    );

                    try (Response response = client.newCall(request).execute()) {
                        long attemptEnd = System.currentTimeMillis();
                        long tat = attemptEnd - attemptStart;

                        String responseBody = response.body() != null ? response.body().string() : null;

                        // Retry on empty/null response body
                        if (responseBody == null || responseBody.isBlank()) {
                            boolean canRetry = retryActivated && attempt < maxRetryAttempts;

                            String msg = String.format(
                                    "EMPTY RESPONSE | uuid=%s | endpoint=%s | status=%d | attempt=%d/%d | retryable=%s",
                                    uuid, endpoint, response.code(), attempt, maxRetryAttempts, canRetry
                            );

                            logger.error("Protegrity API [BATCH] {}", msg);
                            if (audit != null) {
                                audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                                audit.setStatus("FAILED");
                                audit.setMessage(msg);
                                REPO.insertProtegrityAudit(audit);
                            }

                            if (canRetry) {
                                logger.warn("Retrying due to empty response after {} ms | uuid={} | attempt={}/{}",
                                        retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            }

                            HandymanException ex = new HandymanException("Protegrity API returned empty or null response");
                            HandymanException.insertException(msg, ex, actionExecutionAudit);
                            logger.error("All retry attempts exhausted due to empty response | uuid={} | endpoint={}", uuid, endpoint);
                            return Collections.emptyList();
                        }

                        // Retry if response is not successful
                        if (!response.isSuccessful()) {
                            boolean isRetryable = retryableStatusCodes.contains(response.code());
                            boolean canRetry = retryActivated && isRetryable && attempt < maxRetryAttempts;

                            String msg = String.format(
                                    "FAILURE | uuid=%s | endpoint=%s | status=%d | error=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                                    uuid, endpoint, response.code(), response.message(), attempt, maxRetryAttempts, canRetry, tat
                            );

                            logger.error("Protegrity API [BATCH] {}", msg);
                            if (audit != null) {
                                audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                                audit.setStatus("FAILED");
                                audit.setMessage(msg);
                                REPO.insertProtegrityAudit(audit);
                            }

                            if (canRetry) {
                                logger.warn("Retrying in {} ms | uuid={} | attempt={}/{}", retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            }

                            HandymanException ex = new HandymanException("Protegrity API error: " + response.message());
                            HandymanException.insertException(msg, ex, actionExecutionAudit);
                            logger.error("All retry attempts exhausted | uuid={} | endpoint={}", uuid, endpoint);
                            return Collections.emptyList();
                        }

                        // SUCCESS
                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        List<EncryptionRequestClass> encryptedList = new ArrayList<>();
                        for (JsonNode item : jsonResponse) {
                            encryptedList.add(new EncryptionRequestClass(
                                    item.get("policy").asText(),
                                    item.get("value").asText(),
                                    item.get("key").asText()
                            ));
                        }

                        String successMsg = String.format(
                                "SUCCESS | uuid=%s | endpoint=%s | jsonSize=%d | attempt=%d/%d | TAT=%d ms",
                                uuid, endpoint, protegrityList.size(), attempt, maxRetryAttempts, tat
                        );

                        logger.info("Protegrity API [BATCH] {}", successMsg);
                        if (audit != null) {
                            audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                            audit.setStatus("SUCCESS");
                            audit.setMessage(successMsg);
                            REPO.insertProtegrityAudit(audit);
                        }

                        return encryptedList;
                    }

                } catch (IOException e) {
                    long attemptEnd = System.currentTimeMillis();
                    long tat = attemptEnd - attemptStart;
                    boolean canRetry = retryActivated && attempt < maxRetryAttempts;

                    String errMsg = String.format(
                            "EXCEPTION | uuid=%s | endpoint=%s | msg=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                            uuid, endpoint, e.getMessage(), attempt, maxRetryAttempts, canRetry, tat
                    );

                    logger.error("Protegrity API [BATCH] {}", errMsg, e);
                    if (audit != null) {
                        audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                        audit.setStatus("FAILED");
                        audit.setMessage(errMsg);
                        REPO.insertProtegrityAudit(audit);
                    }

                    if (canRetry) {
                        logger.warn("Retrying due to IOException after {} ms | uuid={} | attempt={}/{}",
                                retryIntervalSecs, uuid, attempt, maxRetryAttempts);
                        Thread.sleep(retryIntervalSecs);
                        continue;
                    }

                    HandymanException ex = new HandymanException("IOException during Protegrity API", e, actionExecutionAudit);
                    HandymanException.insertException(errMsg, ex, actionExecutionAudit);
                    logger.error("All retry attempts exhausted due to IOException | uuid={} | endpoint={}", uuid, endpoint);
                    return Collections.emptyList();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Protegrity API [BATCH] INTERRUPTED | uuid={} | endpoint={} | msg={}", uuid, endpoint, e.getMessage(), e);
            HandymanException ex = new HandymanException("Retry interrupted", e, actionExecutionAudit);
            HandymanException.insertException("Protegrity API interrupted [uuid=" + uuid + "]", ex, actionExecutionAudit);
        } catch (Exception e) {
            logger.error("Protegrity API [BATCH] UNEXPECTED EXCEPTION | uuid={} | endpoint={} | msg={}", uuid, endpoint, e.getMessage(), e);
            HandymanException ex = new HandymanException("Unexpected Protegrity API error", e, actionExecutionAudit);
            HandymanException.insertException("Unexpected error [uuid=" + uuid + "]", ex, actionExecutionAudit);
        }

        logger.error("Protegrity API [BATCH] returning EMPTY result | uuid={} | endpoint={}", uuid, endpoint);
        return Collections.emptyList();
    }



    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class EncryptionRequest {
        private String policy;
        private String value;
        private String key;
    }
}