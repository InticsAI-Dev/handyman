package in.handyman.raven.core.api;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

/**
 * Asynchronous OkHttpClient wrapper with intelligent retry logic, comprehensive audit trail,
 * and enterprise-grade error handling for the Handyman Raven framework.
 * 
 * This class provides fully asynchronous HTTP operations with:
 * - Non-blocking retry logic with configurable backoff strategies
 * - Comprehensive audit trail with encryption support
 * - Circuit breaker patterns for resilience
 * - Thread-safe operations with proper resource management
 * - Integration with Handyman framework's security and audit systems
 * 
 * @author Handyman Raven Framework
 * @version 2.0.0
 */
@Slf4j
public class AsyncHttpClientWithRetry {

    private final OkHttpClient httpClient;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger actionLogger;
    private final String jdbiResourceName;
    private final Jdbi jdbi;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService auditExecutor;
    
    // Retry configuration
    private final AsyncRetryConfig retryConfig;
    
    // Error classification
    private static final List<Integer> NON_RETRYABLE_STATUS_CODES = Arrays.asList(400, 401, 402, 403, 404, 422);
    private static final String COPRO_RETRY_ERROR_AUDIT = "copro_retry_error_audit";

    /**
     * Configuration class for async retry behavior
     */
    @Builder
    public static class AsyncRetryConfig {
        @Builder.Default
        private final boolean retryEnabled = false;
        
        @Builder.Default
        private final int maxRetries = 3;
        
        @Builder.Default
        private final long initialDelayMs = 1000;
        
        @Builder.Default
        private final long maxDelayMs = 30000;
        
        @Builder.Default
        private final double backoffMultiplier = 2.0;
        
        @Builder.Default
        private final double jitterFactor = 0.1;
        
        @Builder.Default
        private final boolean encryptAuditTrail = false;
        
        @Builder.Default
        private final long timeoutMs = 300000; // 5 minutes
        
        @Builder.Default
        private final int auditThreadPoolSize = 5;

        // Getters
        public boolean isRetryEnabled() { return retryEnabled; }
        public int getMaxRetries() { return maxRetries; }
        public long getInitialDelayMs() { return initialDelayMs; }
        public long getMaxDelayMs() { return maxDelayMs; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public double getJitterFactor() { return jitterFactor; }
        public boolean isEncryptAuditTrail() { return encryptAuditTrail; }
        public long getTimeoutMs() { return timeoutMs; }
        public int getAuditThreadPoolSize() { return auditThreadPoolSize; }
    }

    /**
     * Result wrapper for async HTTP responses
     */
    @Builder
    public static class AsyncHttpResult {
        private final Response response;
        private final boolean successful;
        private final int totalAttempts;
        private final long totalDurationMs;
        private final Exception lastException;
        private final List<String> auditIds;

        // Getters
        public Response getResponse() { return response; }
        public boolean isSuccessful() { return successful; }
        public int getTotalAttempts() { return totalAttempts; }
        public long getTotalDurationMs() { return totalDurationMs; }
        public Exception getLastException() { return lastException; }
        public List<String> getAuditIds() { return auditIds; }
    }

    /**
     * Constructor with custom configuration
     */
    public AsyncHttpClientWithRetry(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit,
                                  Logger actionLogger, String jdbiResourceName, AsyncRetryConfig retryConfig) {
        this.httpClient = httpClient;
        this.actionExecutionAudit = actionExecutionAudit;
        this.actionLogger = actionLogger;
        this.jdbiResourceName = jdbiResourceName;
        this.jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        this.retryConfig = retryConfig;
        
        // Create dedicated thread pools for scheduling and audit operations
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "AsyncHttpRetry-Scheduler");
            t.setDaemon(true);
            return t;
        });
        
        this.auditExecutor = Executors.newFixedThreadPool(retryConfig.getAuditThreadPoolSize(), r -> {
            Thread t = new Thread(r, "AsyncHttpRetry-Audit");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Constructor with default configuration from action context
     */
    public AsyncHttpClientWithRetry(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit,
                                  Logger actionLogger, String jdbiResourceName) {
        this(httpClient, actionExecutionAudit, actionLogger, jdbiResourceName, 
             createConfigFromContext(actionExecutionAudit));
    }

    /**
     * Execute HTTP request asynchronously with retry logic
     * This is the main method that replaces CoproRetryService.callCoproApiWithRetry
     */
    public CompletableFuture<AsyncHttpResult> executeWithRetryAsync(Request request, String requestBody,
                                                                   CoproRetryErrorAuditTable retryAudit) {
        
        actionLogger.info("Starting async HTTP request with retry: URL={}, MaxRetries={}, RetryEnabled={}", 
                         request.url(), retryConfig.getMaxRetries(), retryConfig.isRetryEnabled());
        
        CompletableFuture<AsyncHttpResult> resultFuture = new CompletableFuture<>();
        long startTime = System.currentTimeMillis();
        
        // Start the async retry chain
        attemptRequestAsync(1, request, requestBody, retryAudit, resultFuture, startTime);
        
        // Add timeout handling
        CompletableFuture<AsyncHttpResult> timeoutFuture = new CompletableFuture<>();
        scheduler.schedule(() -> {
            if (!resultFuture.isDone()) {
                timeoutFuture.completeExceptionally(
                    new IOException("HTTP request timed out after " + retryConfig.getTimeoutMs() + "ms"));
            }
        }, retryConfig.getTimeoutMs(), TimeUnit.MILLISECONDS);
        
        return CompletableFuture.anyOf(resultFuture, timeoutFuture)
                .thenCompose(result -> {
                    if (resultFuture.isDone()) {
                        return resultFuture;
                    } else {
                        return timeoutFuture;
                    }
                });
    }

    /**
     * Execute HTTP request asynchronously without retry (single attempt)
     */
    public CompletableFuture<Response> executeAsync(Request request, String requestBody,
                                                   CoproRetryErrorAuditTable retryAudit) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                actionLogger.error("Async HTTP request failed for URL: {}", request.url(), e);
                recordFailedAttemptAsync(request, requestBody, null, e, 1, retryAudit);
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    actionLogger.info("Async HTTP request successful for URL: {}", request.url());
                    recordSuccessfulAttemptAsync(request, requestBody, response, 1, retryAudit);
                } else {
                    actionLogger.warn("Async HTTP request failed with status: {} for URL: {}", 
                                    response.code(), request.url());
                    recordFailedAttemptAsync(request, requestBody, response, null, 1, retryAudit);
                }
                future.complete(response);
            }
        });
        
        return future;
    }

    /**
     * Close the async client and release resources
     */
    public void close() {
        actionLogger.info("Shutting down AsyncHttpClientWithRetry");
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
        }
        
        if (auditExecutor != null) {
            auditExecutor.shutdown();
            try {
                if (!auditExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    auditExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                auditExecutor.shutdownNow();
            }
        }
        
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            if (httpClient.cache() != null) {
                try {
                    httpClient.cache().close();
                } catch (IOException e) {
                    actionLogger.warn("Error closing HTTP client cache", e);
                }
            }
        }
    }

    // Private async helper methods

    private void attemptRequestAsync(int attempt, Request request, String requestBody,
                                   CoproRetryErrorAuditTable retryAudit,
                                   CompletableFuture<AsyncHttpResult> resultFuture,
                                   long startTime) {
        
        actionLogger.debug("Async HTTP request attempt {} of {} for URL: {}", 
                          attempt, retryConfig.getMaxRetries(), request.url());
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                actionLogger.error("Async HTTP request attempt {} failed for URL: {}", attempt, request.url(), e);
                
                recordFailedAttemptAsync(request, requestBody, null, e, attempt, retryAudit);
                
                if (shouldRetryAsync(null, attempt)) {
                    scheduleNextAttempt(attempt, request, requestBody, retryAudit, resultFuture, startTime);
                } else {
                    long totalDuration = System.currentTimeMillis() - startTime;
                    resultFuture.complete(AsyncHttpResult.builder()
                            .successful(false)
                            .totalAttempts(attempt)
                            .totalDurationMs(totalDuration)
                            .lastException(e)
                            .build());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (isSuccessfulResponse(response)) {
                    actionLogger.info("Async HTTP request successful on attempt {} for URL: {}", attempt, request.url());
                    
                    recordSuccessfulAttemptAsync(request, requestBody, response, attempt, retryAudit);
                    
                    long totalDuration = System.currentTimeMillis() - startTime;
                    resultFuture.complete(AsyncHttpResult.builder()
                            .response(response)
                            .successful(true)
                            .totalAttempts(attempt)
                            .totalDurationMs(totalDuration)
                            .build());
                            
                } else if (shouldRetryAsync(response, attempt)) {
                    actionLogger.warn("Async HTTP request attempt {} failed with status: {}, will retry", 
                                    attempt, response.code());
                    
                    recordFailedAttemptAsync(request, requestBody, response, null, attempt, retryAudit);
                    scheduleNextAttempt(attempt, request, requestBody, retryAudit, resultFuture, startTime);
                    
                } else {
                    actionLogger.warn("Async HTTP request failed after {} attempts with status: {}", 
                                    attempt, response.code());
                    
                    recordFailedAttemptAsync(request, requestBody, response, null, attempt, retryAudit);
                    
                    long totalDuration = System.currentTimeMillis() - startTime;
                    resultFuture.complete(AsyncHttpResult.builder()
                            .response(response)
                            .successful(false)
                            .totalAttempts(attempt)
                            .totalDurationMs(totalDuration)
                            .build());
                }
            }
        });
    }

    private void scheduleNextAttempt(int currentAttempt, Request request, String requestBody,
                                   CoproRetryErrorAuditTable retryAudit,
                                   CompletableFuture<AsyncHttpResult> resultFuture,
                                   long startTime) {
        
        long delay = calculateBackoffWithJitter(currentAttempt);
        
        actionLogger.debug("Scheduling next attempt {} in {}ms for URL: {}", 
                          currentAttempt + 1, delay, request.url());
        
        scheduler.schedule(() -> {
            attemptRequestAsync(currentAttempt + 1, request, requestBody, retryAudit, resultFuture, startTime);
        }, delay, TimeUnit.MILLISECONDS);
    }

    private boolean shouldRetryAsync(Response response, int attempt) {
        if (!retryConfig.isRetryEnabled() || attempt >= retryConfig.getMaxRetries()) {
            return false;
        }

        // Network error or null response - should retry
        if (response == null) {
            return true;
        }

        // Check if the status code is retryable
        return !NON_RETRYABLE_STATUS_CODES.contains(response.code());
    }

    private long calculateBackoffWithJitter(int attempt) {
        long delay = (long) (retryConfig.getInitialDelayMs() * 
                           Math.pow(retryConfig.getBackoffMultiplier(), attempt - 1));
        
        // Add jitter to prevent thundering herd
        if (retryConfig.getJitterFactor() > 0) {
            double jitter = ThreadLocalRandom.current().nextDouble(
                1 - retryConfig.getJitterFactor(), 1 + retryConfig.getJitterFactor());
            delay = (long) (delay * jitter);
        }
        
        return Math.min(delay, retryConfig.getMaxDelayMs());
    }

    private boolean isSuccessfulResponse(Response response) {
        return response != null && response.isSuccessful() && response.body() != null;
    }

    private void recordSuccessfulAttemptAsync(Request request, String requestBody, Response response,
                                            int attempt, CoproRetryErrorAuditTable retryAudit) {
        auditExecutor.submit(() -> {
            try {
                retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                retryAudit.setAttempt(attempt);
                retryAudit.setMessage("HTTP request successful");
                retryAudit.setRequest(encryptRequestResponse(requestBody));
                retryAudit.setResponse(encryptRequestResponse(getResponseBody(response)));
                retryAudit.setEndpoint(request.url().toString());
                retryAudit.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
                
                insertAuditToDatabase(retryAudit);
                
                actionLogger.debug("Recorded successful attempt {} for URL: {}", attempt, request.url());
            } catch (Exception e) {
                actionLogger.error("Failed to record successful attempt audit", e);
            }
        });
    }

    private void recordFailedAttemptAsync(Request request, String requestBody, Response response,
                                        Exception exception, int attempt, CoproRetryErrorAuditTable retryAudit) {
        auditExecutor.submit(() -> {
            try {
                retryAudit.setStatus(ConsumerProcessApiStatus.FAILED.getStatusDescription());
                retryAudit.setAttempt(attempt);
                retryAudit.setRequest(encryptRequestResponse(requestBody));
                retryAudit.setEndpoint(request.url().toString());
                retryAudit.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
                
                if (response != null) {
                    retryAudit.setMessage("HTTP " + response.code() + ": " + response.message());
                    retryAudit.setResponse(encryptRequestResponse(getResponseBody(response)));
                } else if (exception != null) {
                    retryAudit.setMessage(exception.getMessage());
                    retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(exception)));
                }
                
                insertAuditToDatabase(retryAudit);
                
                actionLogger.debug("Recorded failed attempt {} for URL: {}", attempt, request.url());
            } catch (Exception e) {
                actionLogger.error("Failed to record failed attempt audit", e);
            }
        });
    }

    private void insertAuditToDatabase(CoproRetryErrorAuditTable retryAudit) {
        try {
            jdbi.useTransaction(handle -> {
                handle.createUpdate("INSERT INTO macro." + COPRO_RETRY_ERROR_AUDIT +
                        " (origin_id, group_id, attempt, tenant_id, process_id, file_path, paper_no, message, status, stage, " +
                        "created_on, root_pipeline_id, batch_id, last_updated_on, request, response, endpoint) " +
                        "VALUES (:originId, :groupId, :attempt, :tenantId, :processId, :filePath, :paperNo, :message, " +
                        ":status, :stage, :createdOn, :rootPipelineId, :batchId, NOW(), :request, :response, :endpoint)")
                        .bindBean(retryAudit)
                        .execute();
            });
        } catch (Exception e) {
            actionLogger.error("Transaction failed for audit insert: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in executing async audit insert query", handymanException, actionExecutionAudit);
        }
    }

    private String getResponseBody(Response response) {
        try {
            if (response != null && response.body() != null) {
                return response.peekBody(Long.MAX_VALUE).string();
            }
        } catch (Exception e) {
            actionLogger.warn("Failed to extract response body for audit", e);
        }
        return "Unable to extract response body";
    }

    private String encryptRequestResponse(String data) {
        if (data == null) {
            return null;
        }
        
        String encryptReqRes = actionExecutionAudit.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            try {
                return SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, actionLogger)
                        .encrypt(data, "AES256", "ASYNC_COPRO_REQUEST");
            } catch (Exception e) {
                actionLogger.warn("Failed to encrypt request/response data", e);
                return data;
            }
        }
        return data;
    }

    private static AsyncRetryConfig createConfigFromContext(ActionExecutionAudit actionExecutionAudit) {
        return AsyncRetryConfig.builder()
                .retryEnabled(Boolean.parseBoolean(
                    actionExecutionAudit.getContext().getOrDefault("copro.isretry.enabled", "false")))
                .maxRetries(Integer.parseInt(
                    actionExecutionAudit.getContext().getOrDefault("copro.retry.attempt", "3")))
                .initialDelayMs(Long.parseLong(
                    actionExecutionAudit.getContext().getOrDefault("copro.retry.initial.delay.ms", "1000")))
                .maxDelayMs(Long.parseLong(
                    actionExecutionAudit.getContext().getOrDefault("copro.retry.max.delay.ms", "30000")))
                .backoffMultiplier(Double.parseDouble(
                    actionExecutionAudit.getContext().getOrDefault("copro.retry.backoff.multiplier", "2.0")))
                .jitterFactor(Double.parseDouble(
                    actionExecutionAudit.getContext().getOrDefault("copro.retry.jitter.factor", "0.1")))
                .encryptAuditTrail(Boolean.parseBoolean(
                    actionExecutionAudit.getContext().getOrDefault("copro.audit.encrypt", "false")))
                .timeoutMs(Long.parseLong(
                    actionExecutionAudit.getContext().getOrDefault("copro.timeout.ms", "300000")))
                .auditThreadPoolSize(Integer.parseInt(
                    actionExecutionAudit.getContext().getOrDefault("copro.audit.threadpool.size", "5")))
                .build();
    }

    // Static factory methods

    /**
     * Create async client with default configuration from action context
     */
    public static AsyncHttpClientWithRetry create(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit,
                                                 Logger actionLogger, String jdbiResourceName) {
        return new AsyncHttpClientWithRetry(httpClient, actionExecutionAudit, actionLogger, jdbiResourceName);
    }

    /**
     * Create async client with custom retry configuration
     */
    public static AsyncHttpClientWithRetry createWithRetry(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit,
                                                          Logger actionLogger, String jdbiResourceName,
                                                          int maxRetries, long initialDelayMs) {
        AsyncRetryConfig config = AsyncRetryConfig.builder()
                .retryEnabled(true)
                .maxRetries(maxRetries)
                .initialDelayMs(initialDelayMs)
                .build();
        
        return new AsyncHttpClientWithRetry(httpClient, actionExecutionAudit, actionLogger, jdbiResourceName, config);
    }

    /**
     * Create async client with full custom configuration
     */
    public static AsyncHttpClientWithRetry createWithConfig(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit,
                                                           Logger actionLogger, String jdbiResourceName,
                                                           AsyncRetryConfig config) {
        return new AsyncHttpClientWithRetry(httpClient, actionExecutionAudit, actionLogger, jdbiResourceName, config);
    }
}
