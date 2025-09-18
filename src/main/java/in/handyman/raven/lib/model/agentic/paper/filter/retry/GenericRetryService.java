//package in.handyman.raven.lib.model.agentic.paper.filter.retry;
//
//import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.SocketTimeoutException;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//
//public class GenericRetryService implements RetryableService<Request, Response> {
//
//    private static final Logger log = LoggerFactory.getLogger(GenericRetryService.class);
//    private static final List<Integer> NON_RETRY_CODES = List.of(400, 401, 402, 403, 404);
//
//    private final OkHttpClient httpClient;
//    private final int maxRetries;
//    private final long initialDelayMillis;
//
//    public GenericRetryService(OkHttpClient httpClient, int maxRetries, long initialDelayMillis) {
//        this.httpClient = Objects.requireNonNull(httpClient);
//        this.maxRetries = maxRetries;
//        this.initialDelayMillis = initialDelayMillis;
//    }
//
//    @Override
//    public CompletableFuture<Response> sendWithRetry(Request request, ServiceContext context) {
//        CompletableFuture<Response> future = new CompletableFuture<>();
//        attempt(request, context, future, 1);
//        return future;
//    }
//
//    private void attempt(Request request, ServiceContext context, CompletableFuture<Response> future, int attempt) {
//        if (future.isDone()) return;
//
//        httpClient.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                log.warn("Attempt {} failed: {}", attempt, e.getMessage());
//                if (attempt < maxRetries) {
//                    long delay = calculateDelay(attempt);
//                    CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
//                            .thenRun(() -> attempt(request, context, future, attempt + 1));
//                } else {
//                    future.completeExceptionally(e);
//                }
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) {
//                try (response) {
//                    if (isRetryRequired(response) && attempt < maxRetries) {
//                        log.warn("Attempt {} received retryable status {}.", attempt, response.code());
//                        long delay = calculateDelay(attempt);
//                        CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
//                                .thenRun(() -> attempt(request, context, future, attempt + 1));
//                    } else {
//                        future.complete(response);
//                    }
//                }
//            }
//        });
//    }
//
//    private boolean isRetryRequired(Response response) {
//        if (!response.isSuccessful()) {
//            return !NON_RETRY_CODES.contains(response.code());
//        }
//        return false;
//    }
//
//    private long calculateDelay(int attempt) {
//        long delay = initialDelayMillis * (attempt);
//        long jitter = ThreadLocalRandom.current().nextLong(100, 500);
//        return delay + jitter;
//    }
//    private void logFailure(int attempt, IOException e) {
//        if (e instanceof SocketTimeoutException) {
//            log.warn("Attempt {} failed due to SocketTimeoutException: {}", attempt, e.getMessage());
//        } else {
//            log.warn("Attempt {} failed with IOException: {}", attempt, e.getMessage());
//        }
//    }
//
//    private int resolveMaxRetries(ActionExecutionAudit action) {
//        if (action != null && action.getContext() != null) {
//            String enabled = action.getContext().getOrDefault("copro.isretry.enabled", "false");
//            if ("true".equalsIgnoreCase(enabled)) {
//                String cfg = action.getContext().get("copro.retry.attempt");
//                if (cfg != null) {
//                    try {
//                        int v = Integer.parseInt(cfg);
//                        return Math.max(1, v);
//                    } catch (NumberFormatException ignored) { }
//                }
//            }
//        }
//        return defaultMaxRetries;
//    }
//
////    private boolean isRetryRequired(Response response) {
////        return response == null || !response.isSuccessful()
////                || (response.body() == null && !NON_RETRY_CODES.contains(response.code()));
////    }
//
//    private void populateAudit(ServiceContext context, int attempt, String requestBody, Response response, Exception e) {
//        CoproRetryErrorAuditTable audit = context.getAudit();
//        audit.setAttempt(attempt);
//        audit.setRequest(EncryptionUtil.encryptRequestResponse(requestBody, context.getActionAudit()));
//
//        if (response != null) {null
//            audit.setMessage(response.message());
//            audit.setResponse(EncryptionUtil.encryptRequestResponse(response.body().string(), context.getActionAudit()));
//        } else if (e != null) {
//            audit.setMessage(e.getMessage());
//            audit.setResponse(EncryptionUtil.encryptRequestResponse(ExceptionUtil.toString(e), context.getActionAudit()));
//        }
//    }
//
//}

package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproAuditUtil;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproRetryErrorAuditTable;
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
 */
public class GenericRetryService implements RetryableService<Request, Response> {

    private static final Logger log = LoggerFactory.getLogger(GenericRetryService.class);
    private static final List<Integer> NON_RETRY_CODES = List.of(400, 401, 402, 403, 404);
    private static final int ENCRYPTION_TIMEOUT_SECONDS = 3;

    private final OkHttpClient httpClient;
    private final int maxRetries;
    private final long initialDelayMillis;

    public GenericRetryService(OkHttpClient httpClient, int maxRetries, long initialDelayMillis) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
        this.maxRetries = maxRetries;
        this.initialDelayMillis = initialDelayMillis;
    }

    @Override
    public CompletableFuture<Response> sendWithRetry(Request request, ServiceContext context) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        attempt(request, context, future, 1);
        return future;
    }

    private void attempt(Request request, ServiceContext context, CompletableFuture<Response> future, int attempt) {
        if (future.isDone()) return;

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logFailure(attempt, e);
                populateAudit(context, attempt, request, null, e);
                persistAuditAsync(context.getAudit(), context.getActionAudit());

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
                try (response) {
                    populateAudit(context, attempt, request, response, null);
                    persistAuditAsync(context.getAudit(), context.getActionAudit());

                    if (isRetryRequired(response) && attempt < maxRetries) {
                        long delay = calculateDelay(attempt);
                        CompletableFuture.runAsync(() -> {}, CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS))
                                .thenRun(() -> attempt(request, context, future, attempt + 1));
                    } else {
                        future.complete(response);
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });
    }

    private boolean isRetryRequired(Response response) {
        return !response.isSuccessful() && !NON_RETRY_CODES.contains(response.code());
    }

    private long calculateDelay(int attempt) {
        long delay = initialDelayMillis * attempt;
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
        CoproRetryErrorAuditTable audit = context.getAudit();
        audit.setAttempt(attempt);

        String requestBody = context.getRequestBody();
        audit.setRequest(encryptRequestResponse(requestBody, context.getActionAudit()));

        try {
            if (response != null && response.body() != null) {
                audit.setMessage(response.message());
                audit.setResponse(encryptRequestResponse(response.body().string(), context.getActionAudit()));
            } else if (e != null) {
                String msg = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
                audit.setMessage(msg);
                audit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), context.getActionAudit()));
            }
        } catch (IOException ex) {
            log.error("Error reading response body for audit: {}", ex.getMessage(), ex);
        }
    }

    private void persistAuditAsync(CoproRetryErrorAuditTable audit, ActionExecutionAudit action) {
        CompletableFuture.runAsync(() -> {
            try {
                if (audit.getCreatedOn() == null) {
                    audit.setCreatedOn(new java.sql.Timestamp(System.currentTimeMillis()));
                }
                CoproAuditUtil.truncateAuditFields(audit);
                // TODO: replace handymanRepo with your actual DB repository instance
                // handymanRepo.insertAuditToDb(audit, action);
            } catch (Exception ex) {
                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex), ex);
                HandymanException.insertException("Error inserting retry audit", new HandymanException(ex), action);
            }
        });
    }

    private String encryptRequestResponse(String data, ActionExecutionAudit action) {
        if (data == null) return null;
        String encrypt = action != null && action.getContext() != null
                ? action.getContext().get("copro.encrypt.audit")
                : null;

        if ("true".equalsIgnoreCase(encrypt)) {
            try {
                return EncryptionUtil.encryptRequestResponse(data, action);
            } catch (Exception ex) {
                log.warn("Encryption failed: {}, storing plaintext.", ex.getMessage());
                return "[ENCRYPTION_ERROR] " + CoproAuditUtil.truncateSafe(data);
            }
        }
        return data;
    }
}

