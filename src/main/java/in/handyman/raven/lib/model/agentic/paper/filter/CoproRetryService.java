package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryService {
    private static final Logger log = LoggerFactory.getLogger(CoproRetryService.class);
    private static final String COPRO_RETRY_ERROR_AUDIT = "copro_retry_error_audit";
    private static ActionExecutionAudit action;
    private static int maxRetries;

    public static Response callCoproApiWithRetry(Request request, String requestBody, CoproRetryErrorAuditTable retryAudit,
                                                 ActionExecutionAudit actionAudit, Jdbi jdbi,
                                                 OkHttpClient httpClient) throws IOException {
        action = actionAudit;
        maxRetries = resolveMaxRetries();
        int attempt = 1;
        Response response = null;
        IOException lastException = null;

        while (attempt <= maxRetries) {
            response = executeRequestWithHandling(request, requestBody,retryAudit, jdbi, httpClient, attempt);
            if (response != null && response.isSuccessful() && response.body() != null) {
                retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                insertAudit(attempt,retryAudit, request, requestBody,response, null, jdbi);
                return response;
            }
            attempt++;
        }

        if (response != null) return response;
        throw lastException != null ? lastException : new IOException("Copro API call failed with no response and no exception.");
    }

    private static int resolveMaxRetries() {
        boolean isRetryActive = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"));
        return isRetryActive
                ? Integer.parseInt(action.getContext().getOrDefault("copro.retry.attempt", "1"))
                : 1;
    }

    private static Response executeRequestWithHandling(Request request,String requestBody, CoproRetryErrorAuditTable retryAudit,
                                                       Jdbi jdbi, OkHttpClient httpClient, int attempt) {
        Response response = null;

        try {
            response = httpClient.newCall(request).execute();
            if (isRetryRequired(response)) {
                logRetryAttempt(attempt, response);
                insertAudit(attempt,retryAudit, request,requestBody, response, null, jdbi);
            }
        } catch (IOException e) {
            handleIOException(attempt, retryAudit, request,requestBody, e, jdbi);
        }
        return response;
    }

    private static boolean isRetryRequired(Response response) {
        List<Integer> nonRetractableErrors = Arrays.asList(400, 401, 402, 403, 404);
        return response == null || (!response.isSuccessful() || response.body() == null)
                && !nonRetractableErrors.contains(response.code());
    }

    private static void logRetryAttempt(int attempt, Response response) {
        log.error("Attempt {}: Unsuccessful response {} - {}", attempt + 1, response.code(), response.message());
    }

    private static void handleIOException(int attempt, CoproRetryErrorAuditTable retryAudit, Request request,String requestBody,
                                          IOException e, Jdbi jdbi) {
        log.error("Attempt {}: IOException - {}", attempt + 1, ExceptionUtil.toString(e));
        insertAudit(attempt,retryAudit, request,requestBody, null, e, jdbi);
        HandymanException.insertException("Error inserting into copro retry audit", new HandymanException(e), action);
    }

    private static void insertAudit(int attempt,CoproRetryErrorAuditTable retryAudit, Request request,String requestBody, Response response,
                                    Exception e, Jdbi jdbi) {
        try {
            populateAudit(attempt,retryAudit,requestBody,response, e);
            insertAuditToDb(retryAudit, jdbi);
        } catch (Exception exception) {
            log.error("Error inserting into retry audit  {}", ExceptionUtil.toString(exception));
            HandymanException.insertException("Error inserting into copro retry audit", new HandymanException(exception), action);
        }
    }

    private static void populateAudit(int attempt,CoproRetryErrorAuditTable retryAudit,String requestBody,Response response,
                                      Exception e) {
        retryAudit.setRequest(encryptRequestResponse(requestBody));
        retryAudit.setAttempt(attempt);
        if (response != null) {
            retryAudit.setMessage(response.message());
            retryAudit.setResponse(encryptRequestResponse(response.toString()));
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            retryAudit.setMessage(message);
            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e)));
        }
    }

    private static void insertAuditToDb(CoproRetryErrorAuditTable retryAudit, Jdbi jdbi) {
        jdbi.useTransaction(handle -> {
            Update update = handle.createUpdate("  INSERT INTO macro." + COPRO_RETRY_ERROR_AUDIT +
                    "( origin_id, group_id, attempt,tenant_id,process_id, file_path,paper_no,message,status,stage,created_on," +
                    "root_pipeline_id,batch_id,last_updated_on,request,response,endpoint) " +
                    " VALUES(:originId, :groupId,:attempt,:tenantId, :processId, :filePath,:paperNo,:message,:status,:stage," +
                    ":createdOn,:rootPipelineId,:batchId,NOW(),:request,:response,:endpoint);");
            update.bindBean(retryAudit).execute();
        });
    }

    public static String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            return SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
        }
        return request;
    }
}