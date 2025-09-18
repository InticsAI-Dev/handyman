package in.handyman.raven.lib.model.agentic.paper.filter.copro;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.retry.GenericRetryService;
import in.handyman.raven.lib.model.agentic.paper.filter.retry.ServiceContext;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
//
//public class CoproService {
//
//    private static final Logger log = LoggerFactory.getLogger(CoproService.class);
//
//    private final GenericRetryService retryService;
//    private final CoproProperties coproProperties;
//    private final int maxAuditLength = 6000;
//
//    public CoproService(GenericRetryService retryService, CoproProperties coproProperties) {
//        this.retryService = retryService;
//        this.coproProperties = coproProperties;
//    }
//
//    public CompletableFuture<CoproResponse> callCoproApi(Request request, String requestBody) {
//        ServiceContext context = new ServiceContext(Map.of("encrypt.audit", coproProperties.getEncryptAudit()));
//        ErrorAudit audit = new ErrorAudit();
//        audit.setCreatedOn(new Timestamp(System.currentTimeMillis()));
//        audit.setRequest(requestBody);
//
//        return retryService.sendWithRetry(request, context)
//                .thenApply(response -> {
//                    audit.setResponse(response.message());
//                    audit.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
//                    audit.setStatus("Completed with status " + response.code());
//                    auditRepository.save(audit);
//                    return new CoproResponse(response.code(), response.message());
//                })
//                .exceptionally(ex -> {
//                    audit.setStatus("Failed: " + ex.getMessage());
//                    audit.setLastUpdatedOn(new Timestamp(System.currentTimeMillis()));
//                    auditRepository.save(audit);
//                    log.error("Copro API failed", ex);
//                    throw new RuntimeException(ex);
//                });
//    }
//
//
//
//    private void populateAudit(CoproRetryErrorAuditTable retryAudit, int attempt, String requestBody, Response response, Exception e, ActionExecutionAudit action) {
//        retryAudit.setAttempt(attempt);
//        retryAudit.setRequest(encryptRequestResponse(requestBody, action));
//        if (response != null) {
//            retryAudit.setMessage(response.message());
//            retryAudit.setResponse(encryptRequestResponse(response.toString(), action));
//        } else if (e != null) {
//            String msg = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
//            retryAudit.setMessage(msg);
//            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), action));
//        }
//    }
//
//    private void persistAuditAsync(CoproRetryErrorAuditTable auditCopy, ActionExecutionAudit action) {
//        CompletableFuture.runAsync(() -> {
//            try {
//                if (auditCopy.getCreatedOn() == null) {
//                    auditCopy.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
//                }
//                truncateAuditFields(auditCopy);
//                handymanRepo.insertAuditToDb(auditCopy, action);
//            } catch (Exception ex) {
//                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex));
//                HandymanException.insertException("Error inserting retry audit", new HandymanException(ex), action);
//            }
//        }, dbExecutor);
//    }
//}


public class CoproService {

    private static final Logger log = LoggerFactory.getLogger(CoproService.class);
    private static final int MAX_BODY_LENGTH = 6000;

    private final GenericRetryService retryService;
    private final CoproProperties coproProperties;

    public CoproService(GenericRetryService retryService, CoproProperties coproProperties) {
        this.retryService = retryService;
        this.coproProperties = coproProperties;
    }

    public CompletableFuture<CoproResponse> callCoproApi(Request request, String requestBody, ActionExecutionAudit actionAudit) {

        // Create ServiceContext for audit & encryption settings
        ServiceContext context = new ServiceContext(Map.of(
                "encrypt.audit", coproProperties.isEncryptAudit()+""
        ));

        context.withActionAudit(actionAudit);

        // Initialize audit record
        CoproRetryErrorAuditTable audit = new CoproRetryErrorAuditTable();
        audit.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
        audit.setRequest(truncate(requestBody));
        context.withAudit(audit);

        // Call GenericRetryService
        return retryService.sendWithRetry(request, context)
                .thenApply(response -> {
                    try {
                        audit.setResponse(truncate(response.body() != null ? response.body().string() : null));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    audit.setStatus("Completed with status " + response.code());
                    persistAudit(audit, actionAudit);
                    try {
                        return new CoproResponse(response.code(), response.message(), response.body() != null ? response.body().bytes() : null);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .exceptionally(ex -> {
                    audit.setStatus("Failed: " + ex.getMessage());
                    persistAudit(audit, actionAudit);
                    log.error("Copro API failed", ex);
                    throw new RuntimeException(ex);
                });
    }

    private void persistAudit(CoproRetryErrorAuditTable audit, ActionExecutionAudit action) {
        CompletableFuture.runAsync(() -> {
            try {
                truncateAuditFields(audit);
                // Replace with actual repo call to persist audit
                // handymanRepo.insertAuditToDb(audit, action);
            } catch (Exception ex) {
                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex));
            }
        });
    }

    private void truncateAuditFields(CoproRetryErrorAuditTable audit) {
        if (audit.getRequest() != null && audit.getRequest().length() > MAX_BODY_LENGTH) {
            audit.setRequest(audit.getRequest().substring(0, MAX_BODY_LENGTH));
        }
        if (audit.getResponse() != null && audit.getResponse().length() > MAX_BODY_LENGTH) {
            audit.setResponse(audit.getResponse().substring(0, MAX_BODY_LENGTH));
        }
    }

    private String truncate(String input) {
        if (input == null) return null;
        return input.length() > MAX_BODY_LENGTH ? input.substring(0, MAX_BODY_LENGTH) + "...[TRUNCATED]" : input;
    }
}