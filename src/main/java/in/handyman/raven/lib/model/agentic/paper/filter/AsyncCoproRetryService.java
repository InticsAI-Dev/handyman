package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.api.AsyncHttpClientWithRetry;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Async version of CoproRetryService that provides both synchronous and asynchronous
 * HTTP request execution with retry logic.
 * 
 * This service provides:
 * - Fully asynchronous HTTP operations with retry logic
 * - Backward compatibility with synchronous API
 * - Non-blocking operations for better scalability
 * - Comprehensive audit trail and error handling
 * - Thread-safe operations without static state
 * 
 * Migration Benefits:
 * - Better resource utilization (non-blocking)
 * - Improved scalability for high-volume processing
 * - Thread-safe operations
 * - Better error handling and monitoring
 * - Configurable timeout and retry policies
 * 
 * @author Handyman Raven Framework
 * @version 2.0.0
 */
public class AsyncCoproRetryService {
    
    private static final Logger log = LoggerFactory.getLogger(AsyncCoproRetryService.class);

    /**
     * Async version of the original callCoproApiWithRetry method.
     * Returns a CompletableFuture for non-blocking operations.
     */
    public static CompletableFuture<Response> callCoproApiWithRetryAsync(
            Request request, String requestBody, CoproRetryErrorAuditTable retryAudit,
            ActionExecutionAudit actionAudit, String jdbiResourceName, OkHttpClient httpClient) {
        
        log.info("Starting async HTTP request with retry for URL: {}", request.url());
        
        // Create async HTTP client wrapper
        AsyncHttpClientWithRetry asyncClient = AsyncHttpClientWithRetry.create(
            httpClient, actionAudit, log, jdbiResourceName);
        
        // Execute async request with retry logic
        return asyncClient.executeWithRetryAsync(request, requestBody, retryAudit)
                .thenApply(result -> {
                    if (result.isSuccessful()) {
                        log.info("Async HTTP request successful after {} attempts for URL: {}", 
                                result.getTotalAttempts(), request.url());
                        return result.getResponse();
                    } else {
                        log.error("Async HTTP request failed after {} attempts for URL: {}", 
                                result.getTotalAttempts(), request.url());
                        if (result.getLastException() != null) {
                            throw new RuntimeException("HTTP request failed", result.getLastException());
                        }
                        return result.getResponse();
                    }
                })
                .whenComplete((response, throwable) -> {
                    // Clean up resources
                    asyncClient.close();
                    
                    if (throwable != null) {
                        log.error("Async HTTP request completed with error for URL: {}", request.url(), throwable);
                    } else {
                        log.debug("Async HTTP request completed successfully for URL: {}", request.url());
                    }
                });
    }

    /**
     * Synchronous wrapper around the async method for backward compatibility.
     * This method blocks until the async operation completes.
     */
    public static Response callCoproApiWithRetry(Request request, String requestBody,
                                               CoproRetryErrorAuditTable retryAudit,
                                               ActionExecutionAudit actionAudit,
                                               String jdbiResourceName,
                                               OkHttpClient httpClient) throws Exception {
        
        log.info("Starting sync HTTP request (async wrapper) with retry for URL: {}", request.url());
        
        try {
            // Get timeout from context or use default
            long timeoutSeconds = Long.parseLong(
                actionAudit.getContext().getOrDefault("copro.sync.timeout.seconds", "300"));
            
            // Execute async and wait for completion
            CompletableFuture<Response> future = callCoproApiWithRetryAsync(
                request, requestBody, retryAudit, actionAudit, jdbiResourceName, httpClient);
            
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
            
        } catch (TimeoutException e) {
            log.error("Sync HTTP request timed out after waiting for async operation: {}", request.url());
            throw new Exception("HTTP request timed out", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Sync HTTP request interrupted while waiting for async operation: {}", request.url());
            throw new Exception("HTTP request interrupted", e);
        } catch (ExecutionException e) {
            log.error("Sync HTTP request failed during async execution: {}", request.url(), e.getCause());
            throw new Exception("HTTP request failed", e.getCause());
        }
    }

    /**
     * Simplified async method that creates its own HTTP client
     */
    public static CompletableFuture<Response> callCoproApiWithRetryAsyncSimple(
            Request request, String requestBody, CoproRetryErrorAuditTable retryAudit,
            ActionExecutionAudit actionAudit, String jdbiResourceName, int timeoutMinutes) {
        
        // Create HTTP client with specified timeout
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .build();
        
        return callCoproApiWithRetryAsync(request, requestBody, retryAudit, actionAudit, jdbiResourceName, httpClient);
    }

    /**
     * Batch async method for processing multiple requests concurrently
     */
    public static CompletableFuture<Response[]> callCoproApiWithRetryAsyncBatch(
            Request[] requests, String[] requestBodies, CoproRetryErrorAuditTable[] retryAudits,
            ActionExecutionAudit actionAudit, String jdbiResourceName, OkHttpClient httpClient) {
        
        log.info("Starting async batch HTTP requests with retry for {} requests", requests.length);
        
        if (requests.length != requestBodies.length || requests.length != retryAudits.length) {
            throw new IllegalArgumentException("Array lengths must match");
        }
        
        // Create futures for all requests
        CompletableFuture<Response>[] futures = new CompletableFuture[requests.length];
        
        for (int i = 0; i < requests.length; i++) {
            futures[i] = callCoproApiWithRetryAsync(
                requests[i], requestBodies[i], retryAudits[i], actionAudit, jdbiResourceName, httpClient);
        }
        
        // Combine all futures into a single result
        return CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    Response[] responses = new Response[futures.length];
                    for (int i = 0; i < futures.length; i++) {
                        try {
                            responses[i] = futures[i].get();
                        } catch (Exception e) {
                            log.error("Error getting response from batch future {}", i, e);
                            responses[i] = null;
                        }
                    }
                    log.info("Completed async batch HTTP requests");
                    return responses;
                });
    }

    /**
     * Async method with custom retry configuration
     */
    public static CompletableFuture<Response> callCoproApiWithRetryAsyncCustom(
            Request request, String requestBody, CoproRetryErrorAuditTable retryAudit,
            ActionExecutionAudit actionAudit, String jdbiResourceName, OkHttpClient httpClient,
            int maxRetries, long initialDelayMs) {
        
        log.info("Starting async HTTP request with custom retry config for URL: {}", request.url());
        
        // Create async client with custom retry config
        AsyncHttpClientWithRetry asyncClient = AsyncHttpClientWithRetry.createWithRetry(
            httpClient, actionAudit, log, jdbiResourceName, maxRetries, initialDelayMs);
        
        return asyncClient.executeWithRetryAsync(request, requestBody, retryAudit)
                .thenApply(result -> {
                    if (result.isSuccessful()) {
                        log.info("Async HTTP request with custom config successful after {} attempts", 
                                result.getTotalAttempts());
                        return result.getResponse();
                    } else {
                        log.error("Async HTTP request with custom config failed after {} attempts", 
                                result.getTotalAttempts());
                        if (result.getLastException() != null) {
                            throw new RuntimeException("HTTP request failed", result.getLastException());
                        }
                        return result.getResponse();
                    }
                })
                .whenComplete((response, throwable) -> {
                    asyncClient.close();
                });
    }

    /**
     * Utility method to create a reusable async client for multiple requests
     */
    public static AsyncHttpClientWithRetry createAsyncClient(ActionExecutionAudit actionAudit,
                                                            String jdbiResourceName,
                                                            int timeoutMinutes) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .build();
        
        return AsyncHttpClientWithRetry.create(httpClient, actionAudit, log, jdbiResourceName);
    }

    /**
     * Example usage class demonstrating different async patterns
     */
    public static class AsyncUsageExamples {
        
        private final ActionExecutionAudit actionAudit;
        private final String jdbiResourceName;
        private final AsyncHttpClientWithRetry asyncClient;
        
        public AsyncUsageExamples(ActionExecutionAudit actionAudit, String jdbiResourceName, int timeoutMinutes) {
            this.actionAudit = actionAudit;
            this.jdbiResourceName = jdbiResourceName;
            this.asyncClient = createAsyncClient(actionAudit, jdbiResourceName, timeoutMinutes);
        }
        
        /**
         * Example: Single async request with callback
         */
        public void singleAsyncRequestWithCallback(Request request, String requestBody, 
                                                  CoproRetryErrorAuditTable retryAudit) {
            callCoproApiWithRetryAsync(request, requestBody, retryAudit, actionAudit, jdbiResourceName, null)
                    .thenAccept(response -> {
                        if (response.isSuccessful()) {
                            log.info("Request completed successfully: {}", response.code());
                            // Process successful response
                        } else {
                            log.warn("Request failed: {}", response.code());
                            // Handle failed response
                        }
                    })
                    .exceptionally(throwable -> {
                        log.error("Request failed with exception", throwable);
                        // Handle exception
                        return null;
                    });
        }
        
        /**
         * Example: Multiple concurrent requests
         */
        public CompletableFuture<Void> multipleConcurrentRequests(Request[] requests, String[] requestBodies,
                                                                  CoproRetryErrorAuditTable[] retryAudits) {
            return callCoproApiWithRetryAsyncBatch(requests, requestBodies, retryAudits, 
                                                  actionAudit, jdbiResourceName, null)
                    .thenAccept(responses -> {
                        for (int i = 0; i < responses.length; i++) {
                            if (responses[i] != null && responses[i].isSuccessful()) {
                                log.info("Request {} completed successfully", i);
                            } else {
                                log.warn("Request {} failed", i);
                            }
                        }
                    });
        }
        
        /**
         * Clean up resources
         */
        public void shutdown() {
            if (asyncClient != null) {
                asyncClient.close();
            }
        }
    }
}
