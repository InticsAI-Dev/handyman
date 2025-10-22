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

    private final int maxRetryAttempts;
    private final long retryIntervalMillis;
    private final Set<Integer> retryableStatusCodes;
    private final boolean retryActivated;

    public ProtegrityApiEncryptionImpl(String encryptionUrl, String decryptionUrl,
                                       ActionExecutionAudit actionExecutionAudit, Logger logger) {
        this.protegrityEncApiUrl = encryptionUrl;
        this.protegrityDecApiUrl = decryptionUrl;
        this.actionExecutionAudit = actionExecutionAudit;
        this.logger = logger;

        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault(PROTEGRITY_API_TIMEOUT, "60"));
        this.client = buildHttpClient(timeout);

        this.maxRetryAttempts = Integer.parseInt(actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_COUNT, "3"));

        this.retryIntervalMillis = Long.parseLong(actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_INTERVAL_SEC, "1")) * 1000;

        this.retryableStatusCodes = parseRetryableStatusCodes(actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_STATUS_CODES, "404,400,408,429,500,502,503,504"));

        this.retryActivated = Boolean.parseBoolean(actionExecutionAudit.getContext()
                .getOrDefault(PROTEGRITY_API_RETRY_ACTIVATE, "true"));

        logger.info("Retry configuration initialized - maxAttempts: {}, intervalMillis: {}, retryableCodes: {}",
                maxRetryAttempts, retryIntervalMillis, retryableStatusCodes);
    }

    // ----------------------- Public API Methods -----------------------

    @Override
    public String encrypt(String inputToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (isBlank(inputToken)) return inputToken;
        logger.info("Encrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(inputToken, encryptionPolicy, sorItem, protegrityEncApiUrl);
    }

    @Override
    public List<EncryptionRequestClass> encrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) return Collections.emptyList();
        return callProtegrityListApi(requestList, protegrityEncApiUrl);
    }

    @Override
    public String decrypt(String encryptedToken, String encryptionPolicy, String sorItem) throws HandymanException {
        if (isBlank(encryptedToken)) return encryptedToken;
        logger.info("Decrypting data for sorItem: {}, encryptionPolicy: {}", sorItem, encryptionPolicy);
        return callProtegrityApi(encryptedToken, encryptionPolicy, sorItem, protegrityDecApiUrl);
    }

    @Override
    public List<EncryptionRequestClass> decrypt(List<EncryptionRequestClass> requestList) throws HandymanException {
        if (requestList == null || requestList.isEmpty()) return Collections.emptyList();
        return callProtegrityListApi(requestList, protegrityDecApiUrl);
    }

    @Override
    public String getEncryptionMethod() {
        logger.info("Returning encryption method: PROTEGRITY_API_ENC");
        return "PROTEGRITY_API_ENC";
    }

    // ----------------------- Private Helper Methods -----------------------

    private OkHttpClient buildHttpClient(int timeoutSecs) {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .connectTimeout(timeoutSecs, TimeUnit.SECONDS)
                .writeTimeout(timeoutSecs, TimeUnit.SECONDS)
                .readTimeout(timeoutSecs, TimeUnit.SECONDS)
                .build();
    }

    private Set<Integer> parseRetryableStatusCodes(String codesStr) {
        return Stream.of(codesStr.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    private ProtegrityApiAudit buildAudit(String endpoint, String key) {
        return ProtegrityApiAudit.builder()
                .key(actionExecutionAudit.getActionName())
                .encryptionType(getEncryptionMethod())
                .endpoint(endpoint)
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .actionId(actionExecutionAudit.getActionId())
                .threadName(Thread.currentThread().getName())
                .uuid(UUID.randomUUID().toString() + "-" + System.currentTimeMillis())
                .startedOn(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    private String callProtegrityApi(String value, String policy, String key, String endpoint) {
        int attempt = 0;
        String uuid = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        while (attempt < maxRetryAttempts) {
            attempt++;
            long attemptStart = System.currentTimeMillis();
            ProtegrityApiAudit audit = buildAudit(endpoint, key);

            try {
                Request request = buildSingleRequest(value, policy, key, endpoint);

                try (Response response = client.newCall(request).execute()) {
                    long tat = System.currentTimeMillis() - attemptStart;
                    return handleSingleResponse(response, uuid, endpoint, key, attempt, tat, audit);
                }

            } catch (IOException e) {
                if (handleIOException(e, uuid, endpoint, key, attempt, audit)) continue;
                return "";
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logInterrupted(uuid, endpoint, key, e);
                return "";
            }
        }

        logger.error("Protegrity API returning NULL result | uuid={} | endpoint={}", uuid, endpoint);
        return "";
    }

    private Request buildSingleRequest(String value, String policy, String key, String endpoint) throws IOException {
        List<EncryptionRequest> payload = Collections.singletonList(new EncryptionRequest(policy, value, key));
        String jsonPayload = objectMapper.writeValueAsString(payload);
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
        return new Request.Builder().url(endpoint).post(body).build();
    }

    private String handleSingleResponse(Response response, String uuid, String endpoint, String key,
                                        int attempt, long tat, ProtegrityApiAudit audit) throws IOException, InterruptedException {
        String responseBody = response.body() != null ? response.body().string() : null;

        if (responseBody == null || responseBody.isBlank()) {
            return handleFailedSingleResponse(uuid, endpoint, key, attempt, audit, "EMPTY RESPONSE");
        }

        if (!response.isSuccessful()) {
            boolean isRetryable = retryableStatusCodes.contains(response.code());
            boolean canRetry = retryActivated && isRetryable && attempt < maxRetryAttempts;

            String msg = String.format(
                    "FAILURE | uuid=%s | endpoint=%s | code=%d | error=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                    uuid, endpoint, response.code(), response.message(), attempt, maxRetryAttempts, canRetry, tat
            );

            logger.error("Protegrity API {}", msg);
            updateAudit(audit, "FAILED", msg);

            if (canRetry) {
                Thread.sleep(retryIntervalMillis);
                return null;
            }

            HandymanException ex = new HandymanException("Protegrity API error: " + response.message());
            HandymanException.insertException(msg, ex, actionExecutionAudit);
            return "";
        }

        // SUCCESS
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        String encryptedValue = jsonResponse.get(0).get("value").asText();
        String successMsg = String.format(
                "SUCCESS | uuid=%s | endpoint=%s | key=%s | attempt=%d/%d | TAT=%d ms",
                uuid, endpoint, key, attempt, maxRetryAttempts, tat
        );
        updateAudit(audit, "SUCCESS", successMsg);

        return encryptedValue;
    }

    private String handleFailedSingleResponse(String uuid, String endpoint, String key,
                                              int attempt, ProtegrityApiAudit audit, String failureReason) throws InterruptedException {
        boolean canRetry = retryActivated && attempt < maxRetryAttempts;
        String msg = String.format("%s | uuid=%s | endpoint=%s | attempt=%d/%d | retryable=%s",
                failureReason, uuid, endpoint, attempt, maxRetryAttempts, canRetry);

        logger.error("Protegrity API {}", msg);
        updateAudit(audit, "FAILED", msg);

        if (canRetry) {
            Thread.sleep(retryIntervalMillis);
            return null;
        }

        HandymanException ex = new HandymanException("Protegrity API returned empty or null response");
        HandymanException.insertException(msg, ex, actionExecutionAudit);
        return "";
    }
    private boolean handleIOException(IOException e, String uuid, String endpoint, String key,
                                      int attempt, ProtegrityApiAudit audit) {
        boolean canRetry = retryActivated && attempt < maxRetryAttempts;
        String errMsg = String.format(
                "EXCEPTION | uuid=%s | endpoint=%s | msg=%s | attempt=%d/%d | retryable=%s",
                uuid, endpoint, e.getMessage(), attempt, maxRetryAttempts, canRetry
        );

        logger.error("Protegrity API {}", errMsg, e);
        updateAudit(audit, "FAILED", errMsg);

        if (canRetry) {
            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                logger.error("Retry interrupted during IOException handling | uuid={} | endpoint={}", uuid, endpoint, ie);
                HandymanException ex = new HandymanException("Retry interrupted during IOException handling", ie, actionExecutionAudit);
                HandymanException.insertException(errMsg + " | Interrupted", ex, actionExecutionAudit);
                return false; // Stop retrying if interrupted
            }
            return true; // Can retry after sleep
        }

        HandymanException ex = new HandymanException("IOException during Protegrity API", e, actionExecutionAudit);
        HandymanException.insertException(errMsg, ex, actionExecutionAudit);
        return false;
    }

    private void logInterrupted(String uuid, String endpoint, String key, InterruptedException e) {
        logger.error("Protegrity API INTERRUPTED | uuid={} | endpoint={} | key={} | msg={}", uuid, endpoint, key, e.getMessage(), e);
        HandymanException ex = new HandymanException("Retry interrupted", e, actionExecutionAudit);
        HandymanException.insertException("Protegrity API interrupted [uuid=" + uuid + "]", ex, actionExecutionAudit);
    }

    private void updateAudit(ProtegrityApiAudit audit, String status, String message) {
        audit.setCompletedOn(Timestamp.valueOf(LocalDateTime.now()));
        audit.setStatus(status);
        audit.setMessage(message);
        REPO.insertProtegrityAudit(audit);
    }

    private List<EncryptionRequestClass> callProtegrityListApi(List<EncryptionRequestClass> protegrityList, String endpoint) {
        int attempt = 0;
        String uuid = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        while (attempt < maxRetryAttempts) {
            attempt++;
            long attemptStart = System.currentTimeMillis();
            ProtegrityApiAudit audit = buildAudit(endpoint, null);

            try {
                Request request = buildListRequest(protegrityList, endpoint);

                try (Response response = client.newCall(request).execute()) {
                    long tat = System.currentTimeMillis() - attemptStart;
                    List<EncryptionRequestClass> result = handleBatchResponse(response, uuid, endpoint, protegrityList, attempt, tat, audit);
                    if (result != null) return result;
                }

            } catch (IOException e) {
                if (handleBatchIOException(e, uuid, endpoint, protegrityList, attempt, audit)) continue;
                return emptyValueList(protegrityList);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logInterrupted(uuid, endpoint, null, e);
                return emptyValueList(protegrityList);
            }
        }

        logger.error("Protegrity API [BATCH] returning EMPTY result | uuid={} | endpoint={}", uuid, endpoint);
        return emptyValueList(protegrityList);
    }

    private Request buildListRequest(List<EncryptionRequestClass> list, String endpoint) throws IOException {
        String jsonPayload = objectMapper.writeValueAsString(list);
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
        return new Request.Builder().url(endpoint).post(body).build();
    }

    private List<EncryptionRequestClass> handleBatchResponse(Response response, String uuid, String endpoint,
                                                             List<EncryptionRequestClass> requestList, int attempt,
                                                             long tat, ProtegrityApiAudit audit) throws IOException, InterruptedException {
        String responseBody = response.body() != null ? response.body().string() : null;

        if (responseBody == null || responseBody.isBlank()) {
            return handleFailedBatchResponse(uuid, endpoint, requestList, attempt, audit, "EMPTY RESPONSE");
        }

        if (!response.isSuccessful()) {
            boolean isRetryable = retryableStatusCodes.contains(response.code());
            boolean canRetry = retryActivated && isRetryable && attempt < maxRetryAttempts;

            String msg = String.format(
                    "FAILURE | uuid=%s | endpoint=%s | status=%d | error=%s | attempt=%d/%d | retryable=%s | TAT=%d ms",
                    uuid, endpoint, response.code(), response.message(), attempt, maxRetryAttempts, canRetry, tat
            );

            logger.error("Protegrity API [BATCH] {}", msg);
            updateAudit(audit, "FAILED", msg);

            if (canRetry) {
                Thread.sleep(retryIntervalMillis);
                return null;
            }

            HandymanException ex = new HandymanException("Protegrity API error: " + response.message());
            HandymanException.insertException(msg, ex, actionExecutionAudit);
            return emptyValueList(requestList);
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
                uuid, endpoint, requestList.size(), attempt, maxRetryAttempts, tat
        );
        updateAudit(audit, "SUCCESS", successMsg);

        return encryptedList;
    }

    private List<EncryptionRequestClass> handleFailedBatchResponse(String uuid, String endpoint,
                                                                   List<EncryptionRequestClass> requestList, int attempt,
                                                                   ProtegrityApiAudit audit, String failureReason) throws InterruptedException {
        boolean canRetry = retryActivated && attempt < maxRetryAttempts;
        String msg = String.format("%s | uuid=%s | endpoint=%s | attempt=%d/%d | retryable=%s",
                failureReason, uuid, endpoint, attempt, maxRetryAttempts, canRetry);

        logger.error("Protegrity API [BATCH] {}", msg);
        updateAudit(audit, "FAILED", msg);

        if (canRetry) {
            Thread.sleep(retryIntervalMillis);
            return null;
        }

        HandymanException ex = new HandymanException("Protegrity API returned empty or null response");
        HandymanException.insertException(msg, ex, actionExecutionAudit);

        return emptyValueList(requestList);
    }

    private boolean handleBatchIOException(IOException e, String uuid, String endpoint,
                                           List<EncryptionRequestClass> requestList, int attempt,
                                           ProtegrityApiAudit audit) {
        boolean canRetry = retryActivated && attempt < maxRetryAttempts;
        String errMsg = String.format(
                "EXCEPTION | uuid=%s | endpoint=%s | msg=%s | attempt=%d/%d | retryable=%s",
                uuid, endpoint, e.getMessage(), attempt, maxRetryAttempts, canRetry
        );

        logger.error("Protegrity API [BATCH] {}", errMsg, e);
        updateAudit(audit, "FAILED", errMsg);

        if (canRetry) {
            try {
                Thread.sleep(retryIntervalMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                logger.error("Retry interrupted during batch IOException handling | uuid={} | endpoint={}", uuid, endpoint, ie);
                HandymanException ex = new HandymanException("Retry interrupted during batch IOException handling", ie, actionExecutionAudit);
                HandymanException.insertException(errMsg + " | Interrupted", ex, actionExecutionAudit);
                return false; // Stop retrying if interrupted
            }
            return true; // Can retry after sleep
        }

        HandymanException ex = new HandymanException("IOException during Protegrity API", e, actionExecutionAudit);
        HandymanException.insertException(errMsg, ex, actionExecutionAudit);
        return false;
    }


    private List<EncryptionRequestClass> emptyValueList(List<EncryptionRequestClass> list) {
        return list.stream()
                .map(req -> {
                    req.setValue("");
                    return req;
                })
                .collect(Collectors.toList());
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
