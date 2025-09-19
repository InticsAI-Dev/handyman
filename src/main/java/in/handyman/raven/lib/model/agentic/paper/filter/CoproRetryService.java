package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
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

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryService {
    private static final Logger log = LoggerFactory.getLogger(CoproRetryService.class);

    private final HandymanRepo handymanRepo;
    private final OkHttpClient httpClient;

    public CoproRetryService(HandymanRepo handymanRepo, OkHttpClient httpClient) {
        this.handymanRepo = Objects.requireNonNull(handymanRepo, "handymanRepo");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public Response callCoproApiWithRetry(Request request,
                                          String requestBody,
                                          CoproRetryErrorAuditTable retryAudit,
                                          ActionExecutionAudit actionAudit) throws IOException {
        int maxRetries = resolveMaxRetries(actionAudit);
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Response response = null;
            try {
                response = httpClient.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // ✅ return without auto-closing
                }

                if (!isRetryRequired(response)) {
                    insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                    return response; // non-retryable → exit early
                }

                logRetryAttempt(attempt, response);
                insertAudit(attempt, retryAudit, requestBody, response, null, actionAudit);
                safeClose(response); // free resources before retry

            } catch (IOException e) {
                lastException = e;
                handleIOException(attempt, retryAudit, requestBody, e, actionAudit);
            }
            sleepBackoff(attempt);
        }

        throw lastException != null
                ? lastException
                : new IOException("Copro API call failed: no response and no exception.");
    }

    private int resolveMaxRetries(ActionExecutionAudit action) {
        boolean isRetryActive = Boolean.parseBoolean(
                action.getContext().getOrDefault("copro.isretry.enabled", "false")
        );
        return isRetryActive
                ? Integer.parseInt(action.getContext().getOrDefault("copro.retry.attempt", "1"))
                : 1;
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

    private void sleepBackoff(int attempt) {
        try {
            long backoffMillis = (long) Math.min(1000 * Math.pow(2, attempt - 1), 8000);
            Thread.sleep(backoffMillis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void safeClose(Response response) {
        if (response != null) {
            response.close();
        }
    }
}
