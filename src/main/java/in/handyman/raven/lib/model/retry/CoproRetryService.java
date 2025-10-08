package in.handyman.raven.lib.model.retry;

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
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryService {
    private static final Logger log = LoggerFactory.getLogger(CoproRetryService.class);

    private final HandymanRepo handymanRepo;
    private final OkHttpClient httpClient;

    // HTTP/2 specific error patterns
    private static final List<String> HTTP2_RETRYABLE_ERRORS = Arrays.asList(
            "REFUSED_STREAM",
            "PROTOCOL_ERROR",
            "stream was reset",
            "INTERNAL_ERROR",
            "CANCEL",
            "ENHANCE_YOUR_CALM"
    );

    public CoproRetryService(HandymanRepo handymanRepo, OkHttpClient httpClient) {
        this.handymanRepo = Objects.requireNonNull(handymanRepo, "handymanRepo");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public Response callCoproApiWithRetry(Request request,
                                          String requestBody,
                                          CoproRetryErrorAuditTable retryAudit,
                                          ActionExecutionAudit actionAudit) throws IOException {
        int maxRetries = Integer.parseInt(actionAudit.getContext().getOrDefault("copro.retry.attempt", "1"));
        IOException lastException = null;
        retryAudit.setCoproServiceId(UUID.randomUUID().toString());

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Response response = null;
            try {
                response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // ✅ return without auto-closing
                }

                if (!isRetryRequired(response)) {
                    retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // non-retryable → exit early
                }

                logRetryAttempt(attempt, response);
                retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
                insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                safeClose(response); // free resources before retry

            } catch (IOException e) {
                lastException = e;
                safeClose(response); // ensure cleanup on exception

                // Check if this is a retryable error
                if (!isRetryableException(e)) {
                    log.error("Non-retryable exception encountered: {}", e.getClass().getName());
                    handleIOException(attempt, retryAudit, requestBody, e, actionAudit);
                    throw e; // Don't retry non-retryable exceptions
                }

                // Log HTTP/2 specific errors with more context
                if (isHttp2Error(e)) {
                    log.warn("HTTP/2 protocol error detected on attempt {}: {}",
                            attempt, e.getMessage());
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
                : new IOException("Copro API call failed: no response and no exception.");
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

    private void logRetryAttempt(int attempt, Response response) {
        log.error("Attempt {}: Unsuccessful response {} - {}",
                attempt, response.code(), response.message());
    }

    private void handleIOException(int attempt,
                                   CoproRetryErrorAuditTable retryAudit,
                                   String requestBody,
                                   IOException e,
                                   ActionExecutionAudit action) {
        log.error("Attempt {}: IOException - {}", attempt, ExceptionUtil.toString(e));
        retryAudit.setLastUpdatedOn(CreateTimeStamp.currentTimestamp());
        insertAudit(attempt, retryAudit, requestBody, null, e, action);
        HandymanException.insertException("Error during copro API call",
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
            handymanRepo.insertAuditToDb(retryAudit, action);
        } catch (Exception exception) {
            log.error("Error inserting into retry audit {}", ExceptionUtil.toString(exception));
            HandymanException.insertException("Error inserting into copro retry audit",
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
            retryAudit.setMessage(response.message());
            try {
                retryAudit.setResponse(encryptRequestResponse(response.peekBody(Long.MAX_VALUE).string(), action));
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

    public static String encryptRequestResponse(String request, ActionExecutionAudit action) {
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

        log.info("Waiting {} ms before retry attempt {}", backoffMillis, attempt + 1);

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