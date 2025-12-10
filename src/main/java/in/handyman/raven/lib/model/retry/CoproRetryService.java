package in.handyman.raven.lib.model.retry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryService {

    private final HandymanRepo handymanRepo;
    private final OkHttpClient httpClient;
    private final Logger log;

    // HTTP/2 specific error patterns
    private static final List<String> HTTP2_RETRYABLE_ERRORS = Arrays.asList(
            "REFUSED_STREAM",
            "PROTOCOL_ERROR",
            "stream was reset",
            "INTERNAL_ERROR",
            "CANCEL",
            "ENHANCE_YOUR_CALM"
    );

    public CoproRetryService(HandymanRepo handymanRepo, OkHttpClient httpClient,Logger log) {
        this.handymanRepo = Objects.requireNonNull(handymanRepo, "handymanRepo");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.log=log;
    }

    public Response callCoproApiWithRetry(Request request,
                                          String requestBody,
                                          CoproRetryErrorAuditTable retryAudit,
                                          ActionExecutionAudit actionAudit) throws IOException {
        int maxRetries = Integer.parseInt(actionAudit.getContext().getOrDefault("copro.retry.attempt", "1"));
        IOException lastException = null;
        retryAudit.setCoproServiceId(UUID.randomUUID().toString());
        log.info("Starting Copro API call with up to {} retries for stage {} with id {}",
                maxRetries,retryAudit.getStage(),retryAudit.getCoproServiceId());

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Response response = null;
            retryAudit.setCreatedOn(CreateTimeStamp.currentTimestamp());

            try {
                log.info("Copro API call retry attempt {} for stage {} with id {}",
                        attempt,retryAudit.getStage(),retryAudit.getCoproServiceId());
                response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    log.info("Copro API call successful for stage {} with id {} on attempt {}: {} - {}",
                            retryAudit.getStage(),retryAudit.getCoproServiceId(),attempt, response.code(), response.message());
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    retryAudit.setMessage(response.code() +" -> "+ response.message());
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // ✅ return without auto-closing
                }

                if (!isRetryRequired(response)) {
                    log.info("Copro API call unsuccessful for stage {}  with id {} on attempt {}: {} - {} ",
                            retryAudit.getStage(),retryAudit.getCoproServiceId(),attempt, response.code(), response.message());
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    retryAudit.setMessage(response.code()  +" -> "+ response.message());
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // non-retryable → exit early
                }

                logRetryAttempt(attempt, response, retryAudit.getCoproServiceId());
                retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                safeClose(response); // free resources before retry

            } catch (IOException e) {
                lastException = e;
                safeClose(response); // ensure cleanup on exception

                // Check if this is a retryable error
                if (!isRetryableException(e)) {
                    log.error("Non-retryable exception encountered: {} for copro service id {}", e.getClass().getName(), retryAudit.getCoproServiceId());
                    handleIOException(attempt, retryAudit, requestBody, e, actionAudit);
                    throw e; // Don't retry non-retryable exceptions
                }

                // Log HTTP/2 specific errors with more context
                if (isHttp2Error(e)) {
                    log.warn("HTTP/2 protocol error detected on attempt {}: {} for copro service id {}",
                            attempt, e.getMessage(), retryAudit.getCoproServiceId());
                }

                handleIOException(attempt, retryAudit, requestBody, e, actionAudit);

                // If this was the last attempt, throw the exception
                if (attempt == maxRetries) {
                    throw lastException;
                }
            }

            // Only sleep if we're going to retry
            if (attempt < maxRetries) {
                sleepBackoff(actionAudit, attempt);
            }
        }

        throw lastException != null
                ? lastException
                : new IOException("Copro API call failed: no response and no exception for copro service id " + retryAudit.getCoproServiceId());
    }

    /**
     * Determines if an exception is retryable
     */
    private boolean isRetryableException(IOException e) {
        // Check for HTTP/2 errors
        if (isHttp2Error(e)) {
            return true;
        }

        // Network-level retryable errors
        String message = e.getMessage();
        if (message != null) {
            message = message.toLowerCase();
            if (message.contains("connection reset") ||
                    message.contains("broken pipe") ||
                    message.contains("connection timed out") ||
                    message.contains("read timed out") ||
                    message.contains("unexpected end of stream") ||
                    message.contains("socket closed")) {
                return true;
            }
        }

        // Specific exception types that are retryable
        if (e instanceof SocketException) {
            return true;
        }

        // Default to retryable for IOExceptions
        return true;
    }

    /**
     * Checks if the exception is an HTTP/2 protocol error
     */
    private boolean isHttp2Error(IOException e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }

        // Check exception message for HTTP/2 error patterns
        for (String pattern : HTTP2_RETRYABLE_ERRORS) {
            if (message.contains(pattern)) {
                return true;
            }
        }

        // Check for ProtocolException which often indicates HTTP/2 issues
        if (e instanceof ProtocolException) {
            return true;
        }

        // Check the exception chain
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ProtocolException) {
                return true;
            }
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                for (String pattern : HTTP2_RETRYABLE_ERRORS) {
                    if (causeMessage.contains(pattern)) {
                        return true;
                    }
                }
            }
            cause = cause.getCause();
        }

        return false;
    }

    private boolean isRetryRequired(Response response) {
        List<Integer> nonRetractableErrors = Arrays.asList(400, 401, 402, 403, 404, 422);
        return response == null
                || ((!response.isSuccessful() || response.body() == null)
                && !nonRetractableErrors.contains(response.code()));
    }

    private void logRetryAttempt(int attempt, Response response, String coproServiceId) {
        log.error("Attempt {}: Unsuccessful response {} - {} for copro service id {}",
                attempt, response.code(), response.message(),coproServiceId);
    }

    private void handleIOException(int attempt,
                                   CoproRetryErrorAuditTable retryAudit,
                                   String requestBody,
                                   IOException e,
                                   ActionExecutionAudit action) {
        log.error("Attempt {}: for copro service ID {} : IOException - {} ", attempt, retryAudit.getCoproServiceId(),ExceptionUtil.toString(e));
        retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
        insertAudit(attempt, retryAudit, requestBody, null, e, action);
        HandymanException.insertException("Error during copro API call for id " + retryAudit.getCoproServiceId(),
                new HandymanException(e), action);
    }

    private void insertAudit(int attempt,
                             CoproRetryErrorAuditTable retryAudit,
                             String requestBody,
                             Response response,
                             Exception e,
                             ActionExecutionAudit action) {
        try {
            populateAudit(attempt, retryAudit, requestBody, response, e, action);
            retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
            handymanRepo.insertAuditToDb(retryAudit, action);
        } catch (Exception exception) {
            log.error("Error inserting into retry audit {} for id {} ", ExceptionUtil.toString(exception),retryAudit.getCoproServiceId());
            HandymanException.insertException("Error inserting into copro retry audit for id " + retryAudit.getCoproServiceId(),
                    new HandymanException(exception), action);
        }
    }

    private void populateAudit(int attempt,
                               CoproRetryErrorAuditTable retryAudit,
                               String requestBody,
                               Response response,
                               Exception e,
                               ActionExecutionAudit action) {
        retryAudit.setRequest(encryptRequestResponse(requestBody, action));
        retryAudit.setAttempt(attempt);

        if (response != null) {
            retryAudit.setMessage(response.code()  +" -> "+ response.message());
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(encryptRequestResponse(response.peekBody(Long.MAX_VALUE).string(), action));
                JsonNode outputs = root.path("outputs");

                String computationDetails = null;
                Integer coproStatusCode = null;
                String coproLog = null;
                String coproDetails = null;
                String requestId = null;
                final String peekResponseBody = response.peekBody(Long.MAX_VALUE).string();
                if (!outputs.isEmpty()) {

                    final JsonNode innerJson = setParsedResponseValue(action, peekResponseBody);

                    // computationDetails
                    JsonNode compNode = innerJson.get("computationDetails");
                    computationDetails = compNode != null && !compNode.isNull() ? compNode.toString() : null;

                    // statusCode
                    JsonNode statusNode = innerJson.get("statusCode");
                    coproStatusCode = statusNode != null && !statusNode.isNull() ? statusNode.asInt() : null;

                    // errorMessage
                    JsonNode errorNode = innerJson.get("errorMessage");
                    coproLog = errorNode != null && !errorNode.isNull() ? errorNode.asText() : null;

                    // detail
                    JsonNode detailNode = innerJson.get("detail");
                    coproDetails = detailNode != null && !detailNode.isNull() ? detailNode.asText() : null;

                    // requestId
                    JsonNode reqNode = innerJson.get("requestId");
                    requestId = reqNode != null && !reqNode.isNull() ? reqNode.asText() : null;

                } else if (
                        root.has("process")
                                && ("DATA_EXTRACTION".equals(root.get("process").asText())
                                || "DOC_EYE_CUE".equals(root.get("process").asText()))
                ) {

                    // computationDetails = metricsData JSON or null
                    JsonNode metricsNode = root.get("metricsData");
                    computationDetails = metricsNode != null && !metricsNode.isNull()
                            ? mapper.writeValueAsString(metricsNode)
                            : null;

                    // statusCode
                    JsonNode statusNode = root.get("statusCode");
                    coproStatusCode = statusNode != null && !statusNode.isNull() ? statusNode.asInt() : null;

                    // errorMessage
                    JsonNode errorNode = root.get("errorMessage");
                    coproLog = errorNode != null && !errorNode.isNull() ? errorNode.asText() : null;

                    // detail
                    JsonNode detailNode = root.get("detail");
                    coproDetails = detailNode != null && !detailNode.isNull() ? detailNode.asText() : null;

                    // requestId
                    JsonNode reqNode = root.get("requestId");
                    requestId = reqNode != null && !reqNode.isNull() ? reqNode.asText() : null;
                }


                retryAudit.setResponse(encryptRequestResponse(peekResponseBody, action));
                retryAudit.setCoproLog(coproLog);

                retryAudit.setComputationDetails(computationDetails);
                retryAudit.setCoproDetails(coproDetails);
                retryAudit.setRequestId(requestId);
                retryAudit.setCoproStatusCode(coproStatusCode);

            } catch (IOException ex) {
                retryAudit.setResponse("peek-failed");
            }
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            retryAudit.setMessage(message);
            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), action));

            // Add HTTP/2 error flag if applicable
            if (isHttp2Error((IOException) e)) {
                retryAudit.setMessage("HTTP/2_ERROR: " + message);
            }
        }
    }

    private JsonNode setParsedResponseValue(ActionExecutionAudit action, String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(encryptRequestResponse(responseBody, action));
        JsonNode outputs = root.path("outputs");
        JsonNode dataNode = outputs.get(0).path("data").get(0);
        return mapper.readTree(dataNode.asText());
    }

    public String encryptRequestResponse(String request, ActionExecutionAudit action) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action, log)
                    .encrypt(request, "AES256", "COPRO_REQUEST");
        }
        return request;
    }

    /**
     * Enhanced backoff with exponential delay for HTTP/2 errors
     */
    private void sleepBackoff(ActionExecutionAudit actionAudit, int attempt) {
        String delayStr = actionAudit.getContext().getOrDefault("copro.retry.delay.inSeconds", "3");
        long baseBackoffMillis = TimeUnit.SECONDS.toMillis(5);

        try {
            baseBackoffMillis = TimeUnit.SECONDS.toMillis(Long.parseLong(delayStr));
        } catch (NumberFormatException e) {
            log.error("Invalid delay value, defaulting to 5 seconds. Error: " + e.getMessage());
        }

        // Apply exponential backoff: delay * (2 ^ (attempt - 1))
        // For attempt 1: delay * 1, attempt 2: delay * 2, attempt 3: delay * 4, etc.
        long backoffMillis = baseBackoffMillis * (long) Math.pow(2, attempt - 1);

        // Cap the maximum delay at 60 seconds
        backoffMillis = Math.min(backoffMillis, TimeUnit.SECONDS.toMillis(60));

        log.info("Waiting {} ms before retry attempt {}", backoffMillis, attempt + 1    );

        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted during sleep: {}", ex.getMessage(), ex);
        }
    }

    private void safeClose(Response response) {
        if (response != null) {
            try {
                response.close();
            } catch (Exception e) {
                log.warn("Error closing response: {}", e.getMessage());
            }
        }
    }
}