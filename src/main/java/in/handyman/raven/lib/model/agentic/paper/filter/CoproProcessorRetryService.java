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
import java.util.List;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproProcessorRetryService {

    private static final Logger log = LoggerFactory.getLogger(CoproProcessorRetryService.class);

    private final OkHttpClient httpClient;
    private final Jdbi jdbi;
    private final ActionExecutionAudit action;

    private final int maxRetries;
    private final boolean auditEncryptionEnabled;

    public CoproProcessorRetryService(OkHttpClient httpClient, Jdbi jdbi, ActionExecutionAudit action) {
        this.httpClient = httpClient;
        this.jdbi = jdbi;
        this.action = action;
        this.maxRetries = resolveMaxRetries(action);
        this.auditEncryptionEnabled = Boolean.parseBoolean(action.getContext().getOrDefault(ENCRYPT_REQUEST_RESPONSE, "false"));
    }

    public Response callCoproApiWithRetry(Request request, String requestBody, CoproRetryErrorAuditTable retryAudit) {
        IOException lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Response response = httpClient.newCall(request).execute();
                if (shouldRetry(response)) {
                    log.warn("Attempt {}: Unsuccessful response {} - {}", attempt, response.code(), response.message());
                    insertAudit(attempt, retryAudit, requestBody, response, null);
                } else if (response != null && response.isSuccessful() && response.body() != null) {
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    insertAudit(attempt, retryAudit, requestBody, response, null);
                    return response;
                }
            } catch (IOException e) {
                log.error("Attempt {}: IOException - {}", attempt, ExceptionUtil.toString(e));
                insertAudit(attempt, retryAudit, requestBody, null, e);
                HandymanException.insertException("Error inserting into copro retry audit", new HandymanException(e), action);
                lastException = e;
            }
        }
        HandymanException.insertException("Copro API call failed after all retries", lastException != null ? new HandymanException(lastException) : new HandymanException("No response and no exception"), action);
        return null;
    }

    private int resolveMaxRetries(ActionExecutionAudit action) {
        boolean isRetryActive = Boolean.parseBoolean(action.getContext().getOrDefault("copro.isretry.enabled", "false"));
        return isRetryActive ?  Integer.parseInt(action.getContext().getOrDefault("copro.retry.attempt", "1")) : 1;
    }

    private boolean shouldRetry(Response response) {
        List<Integer> nonRetriable = List.of(400, 401, 402, 403, 404);
        return response == null
                || (!response.isSuccessful() || response.body() == null)
                && !nonRetriable.contains(response.code());
    }

    private void insertAudit(int attempt, CoproRetryErrorAuditTable audit, String requestBody, Response response, Exception e) {
        try {
            populateAudit(audit, attempt, requestBody, response, e);
            jdbi.useTransaction(handle -> {
                Update update = handle.createUpdate(getInsertSql());
                update.bindBean(audit).execute();
            });
        } catch (Exception ex) {
            log.error("Error inserting into retry audit: {}", ExceptionUtil.toString(ex));
            HandymanException.insertException("Error inserting into copro retry audit", new HandymanException(ex), action);
        }
    }

    private void populateAudit(CoproRetryErrorAuditTable audit, int attempt, String requestBody, Response response, Exception e) {
        audit.setRequest(encryptIfRequired(requestBody));
        audit.setAttemptInitiated(attempt);
        if (response != null) {
            audit.setMessage(response.message());
            audit.setResponse(encryptIfRequired(response.toString()));
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            audit.setMessage(message);
            audit.setResponse(encryptIfRequired(ExceptionUtil.toString(e)));
        }
    }

    private String encryptIfRequired(String str) {
        if (auditEncryptionEnabled) {
            return SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(str, "AES256", "COPRO_REQUEST");
        }
        return str;
    }

    private String getInsertSql() {
        return "INSERT INTO macro.copro_retry_error_audit (" +
                "origin_id, group_id, attempt, tenant_id, process_id, file_path, paper_no, message, status, stage, created_on, " +
                "root_pipeline_id, batch_id, last_updated_on, request, response, endpoint) " +
                "VALUES (:originId, :groupId, :attempt, :tenantId, :processId, :filePath, :paperNo, :message, :status, :stage, " +
                ":createdOn, :rootPipelineId, :batchId, NOW(), :request, :response, :endpoint)";
    }
}
