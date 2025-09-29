package in.handyman.raven.lib.model.retry;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryService {
    private static final Logger log = LoggerFactory.getLogger(CoproRetryService.class);

    private static final HandymanRepo REPO = new HandymanRepoImpl();
    public static final String COPRO_RETRY_ATTEMPT = "copro.retry.attempt";
    private final OkHttpClient httpclient;
    private final ActionExecutionAudit actionAudit;

    public CoproRetryService(ActionExecutionAudit actionExecutionAudit) {
        int timeout = Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("copro.client.socket.timeout", "5").trim());
        this.actionAudit = actionExecutionAudit;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.MINUTES).writeTimeout(timeout, TimeUnit.MINUTES).readTimeout(timeout, TimeUnit.MINUTES).build();

    }

    public Response callCoproApiWithRetry(Request request,CoproRetryErrorAuditTable retryAudit) throws IOException {
        String coproServiceId = String.valueOf(UUID.randomUUID());
        retryAudit.setCoproServiceId(coproServiceId);
        int maxRetries =  Integer.parseInt(actionAudit.getContext().getOrDefault(COPRO_RETRY_ATTEMPT, "1"));
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Response response =this.httpclient.newCall(request).execute()){


                if (response.isSuccessful() && response.body() != null) {
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    insertAudit(attempt, retryAudit, retryAudit.getRequest(), response, null, actionAudit);
                    return response; // ✅ return without auto-closing
                }

                if (!isRetryRequired(response)) {
                    insertAudit(attempt, retryAudit, retryAudit.getRequest(), response, null, actionAudit);
                    return response; // non-retryable → exit early
                }

                logRetryAttempt(attempt, response);
                insertAudit(attempt, retryAudit, retryAudit.getRequest(), response, null, actionAudit);
                safeClose(response); // free resources before retry

            } catch (IOException e) {
                lastException = e;
                handleIOException(attempt, retryAudit, retryAudit.getRequest(), e, actionAudit);
            }
            sleepBackoff(actionAudit);
        }

        throw lastException != null
                ? lastException
                : new IOException("Copro API call failed: no response and no exception.");
    }

    private boolean isRetryRequired(Response response) {
        List<Integer> nonRetractableErrors = Arrays.asList(400, 401, 402, 403, 404, 422);
        return response == null
                || ((!response.isSuccessful() || response.body() == null)
                && !nonRetractableErrors.contains(response.code()));
    }

    private void logRetryAttempt(int attempt, Response response) {
        log.error("Attempt {}: Unsuccessful response {} - {}",
                attempt + 1, response.code(), response.message());
    }

    private void handleIOException(int attempt,
                                   CoproRetryErrorAuditTable retryAudit,
                                   String requestBody,
                                   IOException e,
                                   ActionExecutionAudit action) {
        log.error("Attempt {}: IOException - {}", attempt + 1, ExceptionUtil.toString(e));
        insertAudit(attempt, retryAudit, requestBody, null, e, action);
        HandymanException.insertException("Error inserting into copro retry audit",
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
            REPO.insertAuditToDb(retryAudit, action);
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

    private void sleepBackoff(ActionExecutionAudit actionAudit) {
        try {
            // Get the delay in seconds from the context, defaulting to 5 if not found
            String delayStr = actionAudit.getContext().getOrDefault("copro.retry.delay", "5");
            long backoffMillis = 5; // Default delay in milliseconds

            // Attempt to parse the delay, defaulting to 5 seconds if parsing fails
            try {
                backoffMillis = TimeUnit.SECONDS.toMillis(Integer.parseInt(delayStr));
            } catch (NumberFormatException e) {
                // Log the invalid value for transparency, if needed
                System.err.println("Invalid delay value, defaulting to 5 seconds. Error: " + e.getMessage());
            }
            Thread.sleep(backoffMillis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted during sleep: {}", ignored.getMessage());

        }
    }

    private void safeClose(Response response) {
        if (response != null) {
            response.close();
        }
    }
}
