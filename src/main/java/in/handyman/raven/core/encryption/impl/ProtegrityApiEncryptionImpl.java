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

        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("protegrity.timeout.seconds", "60"));
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
        );

        // Parse retryable status codes from comma-separated string
        String retryableCodesStr = actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_STATUS_CODES, "400,408,429,500,502,503,504");
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

    private String callProtegrityApi(String value, String policy, String key, String endpoint) throws HandymanException {
        String uuid = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        int attempt = 0;

        try {
            List<EncryptionRequest> encryptionPayload = Collections.singletonList(
                    new EncryptionRequest(policy, value, key)
            );
            String jsonPayload = objectMapper.writeValueAsString(encryptionPayload);

            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build();

            while (attempt <= maxRetryAttempts) {
                attempt++;
                long auditId = -1;
                long attemptStartTime = System.currentTimeMillis();

                try {
                    // Create separate audit record for each attempt
                    auditId = REPO.insertProtegrityAuditRecord(
                            key,
                            getEncryptionMethod(),
                            endpoint,
                            actionExecutionAudit.getRootPipelineId(),
                            actionExecutionAudit.getActionId(),
                            Thread.currentThread().getName(),
                            uuid
                    );

                    logger.info("Calling Protegrity API with auditId={}, uuid={}, endpoint={}, key={}, attempt={}/{}",
                            auditId, uuid, endpoint, key, attempt, maxRetryAttempts + 1);

                    try (Response response = client.newCall(request).execute()) {
                        long endTime = System.currentTimeMillis();
                        long tat = endTime - startTime;
                        long attemptTat = endTime - attemptStartTime;

                        String responseBody = response.body().string();

                        if (!response.isSuccessful()) {
                            boolean isRetryable = retryableStatusCodes.contains(response.code());
                            boolean canRetry = retryActivated && isRetryable && attempt <= maxRetryAttempts;

                            String errorMessage = String.format(
                                    "FAILURE | uuid=%s | auditId=%d | key=%s | endpoint=%s | statusCode=%d | error=%s | TAT=%d ms | attempt=%d/%d | retryable=%s",
                                    uuid,
                                    auditId,
                                    key,
                                    endpoint,
                                    response.code(),
                                    response.message(),
                                    attemptTat,
                                    attempt,
                                    maxRetryAttempts + 1,
                                    canRetry
                            );

                            logger.error(errorMessage);
                            REPO.updateProtegrityAuditRecord(auditId, "FAILED", errorMessage);

                            if (canRetry) {
                                logger.warn("Retrying API call after {} ms [uuid={}, attempt={}/{}]",
                                        retryIntervalSecs, uuid, attempt, maxRetryAttempts + 1);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            } else {
                                HandymanException exception = new HandymanException(response.message());
                                HandymanException.insertException("Protegrity API error [uuid=" + uuid + "]", exception, actionExecutionAudit);
                                logger.error("All retry attempts exhausted for uuid={}, returning null", uuid);
                                return null;
                            }
                        }

                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        String encryptedValue = jsonResponse.get(0).get("value").asText();

                        String successMessage = String.format(
                                "SUCCESS | uuid=%s | auditId=%d | key=%s | endpoint=%s | TAT=%d ms | attempt=%d/%d",
                                uuid,
                                auditId,
                                key,
                                endpoint,
                                tat,
                                attempt,
                                maxRetryAttempts + 1
                        );

                        REPO.updateProtegrityAuditRecord(auditId, "SUCCESS", successMessage);
                        logger.info("Protegrity API call SUCCESS [auditId={}, uuid={}, key={}, TAT={} ms, attempt={}/{}]",
                                auditId, uuid, key, tat, attempt, maxRetryAttempts + 1);
                        return encryptedValue;
                    }

                } catch (IOException e) {
                    boolean canRetry = retryActivated && attempt <= maxRetryAttempts;

                    long endTime = System.currentTimeMillis();
                    long tat = endTime - startTime;
                    long attemptTat = endTime - attemptStartTime;

                    String errMsg = String.format(
                            "EXCEPTION | uuid=%s | auditId=%d | key=%s | endpoint=%s | message=%s | TAT=%d ms | attempt=%d/%d | retryable=%s",
                            uuid,
                            auditId,
                            key,
                            endpoint,
                            e.getMessage(),
                            attemptTat,
                            attempt,
                            maxRetryAttempts + 1,
                            canRetry
                    );

                    logger.error(errMsg, e);

                    if (auditId != -1) {
                        REPO.updateProtegrityAuditRecord(auditId, "FAILED", errMsg);
                    }

                    if (canRetry) {
                        logger.warn("Retrying API call after {} ms due to IOException [uuid={}, attempt={}/{}]",
                                retryIntervalSecs, uuid, attempt, maxRetryAttempts + 1);
                        Thread.sleep(retryIntervalSecs);
                        continue;
                    } else {
                        HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);
                        HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
                        logger.error("All retry attempts exhausted for uuid={}, returning null", uuid);
                        return null;
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "INTERRUPTED | uuid=%s | key=%s | endpoint=%s | message=%s | TAT=%d ms | attempt=%d/%d",
                    uuid,
                    key,
                    endpoint,
                    e.getMessage(),
                    tat,
                    attempt,
                    maxRetryAttempts + 1
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Retry interrupted", e, actionExecutionAudit);
            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            return null;

        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "EXCEPTION | uuid=%s | key=%s | endpoint=%s | message=%s | TAT=%d ms | attempt=%d/%d",
                    uuid,
                    key,
                    endpoint,
                    e.getMessage(),
                    tat,
                    attempt,
                    maxRetryAttempts + 1
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);
            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            return null;
        }

        // This should never be reached due to the logic above, but added for safety
        return null;
    }

    private List<EncryptionRequestClass> callProtegrityListApi(List<EncryptionRequestClass> protegrityList, String endpoint) throws HandymanException {
        String uuid = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        int attempt = 0;

        try {
            String jsonPayload = objectMapper.writeValueAsString(protegrityList);
            RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
            Request request = new Request.Builder().url(endpoint).post(body).build();

            while (attempt <= maxRetryAttempts) {
                attempt++;
                long auditId = -1;
                long attemptStartTime = System.currentTimeMillis();

                try {

                    ProtegrityApiAudit auditRecord = ProtegrityApiAudit.builder()
                            .key(actionExecutionAudit.getActionName())
                            .encryptionType(getEncryptionMethod())
                            .endpoint(endpoint)
                            .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                            .actionId(actionExecutionAudit.getActionId())
                            .threadName(Thread.currentThread().getName())
                            .uuid(uuid)
                            .startedOn(Timestamp.valueOf(LocalDateTime.now()))
                            .build();

                    logger.info("Calling Protegrity API (BATCH) with auditId={}, uuid={}, endpoint={}, items={}, attempt={}/{}",
                            auditId, uuid, endpoint, protegrityList.size(), attempt, maxRetryAttempts + 1);

                    try (Response response = client.newCall(request).execute()) {
                        long endTime = System.currentTimeMillis();
                        long tat = endTime - startTime;
                        long attemptTat = endTime - attemptStartTime;

                        String responseBody = response.body().string();

                        if (!response.isSuccessful()) {
                            boolean isRetryable = retryableStatusCodes.contains(response.code());
                            boolean canRetry = retryActivated && isRetryable && attempt <= maxRetryAttempts;

                            String errorMessage = String.format(
                                    "FAILURE | uuid=%s | auditId=%d | endpoint=%s | statusCode=%d | error=%s | TAT=%d ms | jsonSize=%d | attempt=%d/%d | retryable=%s",
                                    uuid, auditId, endpoint, response.code(), response.message(), attemptTat,
                                    protegrityList.size(), attempt, maxRetryAttempts + 1, canRetry
                            );

                            logger.error(errorMessage);
                            auditRecord.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                            auditRecord.setMessage(errorMessage);
                            auditRecord.setStatus("FAILED");
                            REPO.insertProtegrityAudit(auditRecord);

                            if (canRetry) {
                                logger.warn("Retrying BATCH API call after {} ms [uuid={}, attempt={}/{}]",
                                        retryIntervalSecs, uuid, attempt, maxRetryAttempts + 1);
                                Thread.sleep(retryIntervalSecs);
                                continue;
                            } else {
                                HandymanException exception = new HandymanException(response.message());
                                HandymanException.insertException("Protegrity API error [uuid=" + uuid + "]", exception, actionExecutionAudit);
                                logger.error("All retry attempts exhausted for BATCH uuid={}, returning empty list", uuid);
                                return Collections.emptyList();
                            }
                        }

                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        List<EncryptionRequestClass> encryptedValues = new ArrayList<>();

                        for (int i = 0; i < jsonResponse.size(); i++) {
                            JsonNode responseItem = jsonResponse.get(i);
                            String encryptedValue = responseItem.get("value").asText();

                            EncryptionRequestClass encryptedRequest = new EncryptionRequestClass(
                                    responseItem.get("policy").asText(),
                                    encryptedValue,
                                    responseItem.get("key").asText()
                            );

                            encryptedValues.add(encryptedRequest);
                        }

                        String successMessage = String.format(
                                "SUCCESS | uuid=%s | auditId=%d | endpoint=%s | TAT=%d ms | jsonSize=%d | attempt=%d/%d",
                                uuid, auditId, endpoint, tat, protegrityList.size(), attempt, maxRetryAttempts + 1
                        );
                        auditRecord.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
                        auditRecord.setMessage(successMessage);
                        auditRecord.setStatus("SUCCESS");
                        REPO.insertProtegrityAudit(auditRecord);

                        logger.info("Protegrity API (BATCH) call SUCCESS [auditId={}, uuid={}, items={}, TAT={} ms, attempt={}/{}]",
                                auditId, uuid, protegrityList.size(), tat, attempt, maxRetryAttempts + 1);

                        return encryptedValues;
                    }

                } catch (IOException e) {
                    boolean canRetry = retryActivated && attempt <= maxRetryAttempts;
                    long endTime = System.currentTimeMillis();
                    long tat = endTime - startTime;
                    long attemptTat = endTime - attemptStartTime;

                    String errMsg = String.format(
                            "EXCEPTION | uuid=%s | auditId=%d | endpoint=%s | message=%s | TAT=%d ms | jsonSize=%d | attempt=%d/%d | retryable=%s",
                            uuid, auditId, endpoint, e.getMessage(), attemptTat, protegrityList.size(),
                            attempt, maxRetryAttempts + 1, canRetry
                    );

                    logger.error(errMsg, e);

                    if (auditId != -1) {

                        ProtegrityApiAudit auditRecord = ProtegrityApiAudit.builder()
                                .key(actionExecutionAudit.getActionName())
                                .encryptionType(getEncryptionMethod())
                                .endpoint(endpoint)
                                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                                .actionId(actionExecutionAudit.getActionId())
                                .threadName(Thread.currentThread().getName())
                                .uuid(uuid)
                                .startedOn(Timestamp.valueOf(LocalDateTime.now()))
                                .build();


                        REPO.updateProtegrityAuditRecord(auditId, "FAILED", errMsg);
                    }

                    if (canRetry) {
                        logger.warn("Retrying BATCH API call after {} ms due to IOException [uuid={}, attempt={}/{}]",
                                retryIntervalSecs, uuid, attempt, maxRetryAttempts + 1);
                        Thread.sleep(retryIntervalSecs);
                        continue;
                    } else {
                        HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);
                        HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
                        logger.error("All retry attempts exhausted for BATCH uuid={}, returning empty list", uuid);
                        return Collections.emptyList();
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "INTERRUPTED | uuid=%s | endpoint=%s | message=%s | TAT=%d ms | jsonSize=%d | attempt=%d/%d",
                    uuid, endpoint, e.getMessage(), tat, protegrityList.size(), attempt, maxRetryAttempts + 1
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Retry interrupted", e, actionExecutionAudit);
            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            return Collections.emptyList();

        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            long tat = endTime - startTime;

            String errMsg = String.format(
                    "EXCEPTION | uuid=%s | endpoint=%s | message=%s | TAT=%d ms | jsonSize=%d | attempt=%d/%d",
                    uuid, endpoint, e.getMessage(), tat, protegrityList.size(), attempt, maxRetryAttempts + 1
            );

            logger.error(errMsg, e);
            HandymanException handymanException = new HandymanException("Error calling Protegrity API", e, actionExecutionAudit);
            HandymanException.insertException(errMsg, handymanException, actionExecutionAudit);
            return Collections.emptyList();
        }

        // This should never be reached due to the logic above, but added for safety
        return Collections.emptyList();
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