package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproAuditUtil;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.filemergerpdf.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Generic async retry service for HTTP requests with audit & encryption support.
 * Returns HttpResult wrapper so callers don't need to handle okhttp Response lifecycle.
 */
public class GenericRetryService implements RetryableService {

    private static final Logger log = LoggerFactory.getLogger(GenericRetryService.class);
    private static final List<Integer> NON_RETRY_CODES = List.of(400, 401, 402, 403, 404);

    private final OkHttpClient httpClient;
    private final int maxRetries;
    private final long initialDelayMillis;
    private final HandymanRepo handymanRepo;
    private final CoproAuditUtil auditUtil;

    public GenericRetryService(OkHttpClient httpClient, int maxRetries, long initialDelayMillis, HandymanRepo handymanRepo) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.maxRetries = Math.max(1, maxRetries);
        this.initialDelayMillis = initialDelayMillis;
        this.handymanRepo = handymanRepo;
        this.auditUtil = new CoproAuditUtil();
    }

    @Override
    public CompletableFuture<HttpResult> sendWithRetry(Request request, ServiceContext context) {
        CompletableFuture<HttpResult> future = new CompletableFuture<>();
        attempt(request, context, future, 1);
        return future;
    }

    private void attempt(Request request, ServiceContext context, CompletableFuture<HttpResult> future, int attempt) {
        if (future.isDone()) return;

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logFailure(attempt, e);

                // populate audit & persist
                try {
                    populateAudit(context, attempt, request, null, e);
                    persistAuditAsync(context.getAudit(), context.getActionAudit());
                } catch (Exception ex) {
                    log.error("Error while populating or persisting audit: {}", ExceptionUtil.toString(ex));
                }

                if (attempt < maxRetries) {
                    long delay = calculateDelay(attempt);
                    CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
                            .thenRun(() -> attempt(request, context, future, attempt + 1));
                } else {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response r = response) {
                    byte[] bodyBytes = r.body() != null ? r.body().bytes() : null;
                    // set responseBody in context for audit helpers
                    context.withResponseBody(bodyBytes == null ? null : new String(bodyBytes));

                    populateAudit(context, attempt, request, r, null);
                    persistAuditAsync(context.getAudit(), context.getActionAudit());

                    if (isRetryRequired(r) && attempt < maxRetries) {
                        long delay = calculateDelay(attempt);
                        CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
                                .thenRun(() -> attempt(request, context, future, attempt + 1));
                    } else {
                        future.complete(new HttpResult(r.code(), r.message(), bodyBytes));
                    }
                } catch (IOException ex) {
                    future.completeExceptionally(ex);
                }
            }
        });
    }

    private boolean isRetryRequired(Response response) {
        if (response == null) return true;
        if (!response.isSuccessful()) {
            return !NON_RETRY_CODES.contains(response.code());
        }
        return false;
    }

    private long calculateDelay(int attempt) {
        long delay = initialDelayMillis * (long) attempt;
        long jitter = ThreadLocalRandom.current().nextLong(100, 500);
        return delay + jitter;
    }

    private void logFailure(int attempt, IOException e) {
        if (e instanceof SocketTimeoutException) {
            log.warn("Attempt {} failed due to SocketTimeoutException: {}", attempt, e.getMessage());
        } else {
            log.warn("Attempt {} failed with IOException: {}", attempt, e.getMessage());
        }
    }

    private void populateAudit(ServiceContext context, int attempt, Request request, Response response, Exception e) {
        if (!context.hasAudit()) return;
        CoproRetryErrorAuditTable audit = context.getAudit().cloneForRetry();
        audit.setAttempt(attempt);

        // request body from context (caller should set)
        String reqBody = context.getRequestBody();
        audit.setRequest(EncryptionUtil.encryptRequestResponse(reqBody, context.getActionAudit()));

        try {
            if (response != null) {
                String respStr = context.getResponseBody(); // we set earlier
                audit.setMessage(response.message());
                audit.setResponse(EncryptionUtil.encryptRequestResponse(respStr, context.getActionAudit()));
                audit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());

            } else if (e != null) {
                String msg = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
                audit.setMessage(msg);
                audit.setResponse(EncryptionUtil.encryptRequestResponse(ExceptionUtil.toString(e), context.getActionAudit()));
                audit.setStatus(ConsumerProcessApiStatus.FAILED.getStatusDescription());

            }
            auditUtil.truncateAuditFields(audit);
            // put back the clone into context so persistAuditAsync gets the incremented attempt clone
            context.withAudit(audit);
        } catch (Exception ex) {
            log.error("Error populating audit: {}", ExceptionUtil.toString(ex), ex);
        }
    }

    private void persistAuditAsync(CoproRetryErrorAuditTable audit, ActionExecutionAudit action) {
        if (audit == null || handymanRepo == null) return;
        CompletableFuture.runAsync(() -> {
            try {
                if (audit.getCreatedOn() == null) {
                    audit.setCreatedOn(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                auditUtil.truncateAuditFields(audit);
                handymanRepo.insertAuditToDb(audit, action);
            } catch (Exception ex) {
                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex), ex);
                HandymanException.insertException("Error inserting retry audit", new HandymanException(ex), action);
            }
        });
    }
}
