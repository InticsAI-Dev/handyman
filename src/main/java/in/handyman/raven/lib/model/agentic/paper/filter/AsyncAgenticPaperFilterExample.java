package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.api.AsyncHttpClientWithRetry;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating how to integrate AsyncHttpClientWithRetry
 * into the AgenticPaperFilterConsumerProcess for fully asynchronous operations.
 * 
 * This example shows:
 * - Non-blocking HTTP requests with retry logic
 * - Asynchronous response processing
 * - Proper resource management
 * - Error handling in async context
 * - Multiple concurrent requests
 */
public class AsyncAgenticPaperFilterExample {
    
    private final ActionExecutionAudit action;
    private final Logger log;
    private final Marker aMarker;
    private final String jdbiResourceName;
    private final AsyncHttpClientWithRetry asyncHttpClient;
    
    public AsyncAgenticPaperFilterExample(Logger log, Marker aMarker, ActionExecutionAudit action,
                                        int timeoutMinutes, String jdbiResourceName) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        
        // Create HTTP client with custom timeout
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .build();
        
        // Create async client with retry capabilities
        this.asyncHttpClient = AsyncHttpClientWithRetry.create(httpClient, action, log, jdbiResourceName);
        
        log.info(aMarker, "Async HTTP client initialized for Agentic Paper Filter processing");
    }
    
    /**
     * Process a single document asynchronously with retry logic
     * This replaces the synchronous tritonRequestKryptonExecutor method
     */
    public CompletableFuture<List<AgenticPaperFilterOutput>> processDocumentAsync(
            URL endpoint, AgenticPaperFilterInput entity, String jsonRequestPayload) {
        
        log.info(aMarker, "Starting async document processing for paper: {} at endpoint: {}", 
                entity.getPaperNo(), endpoint);
        
        // Create audit table for this request
        CoproRetryErrorAuditTable retryAudit = createRetryAuditTable(entity, endpoint);
        
        // Build HTTP request
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestPayload, mediaType))
                .build();
        
        // Execute async request with retry logic
        return asyncHttpClient.executeWithRetryAsync(request, jsonRequestPayload, retryAudit)
                .thenCompose(result -> {
                    if (result.isSuccessful()) {
                        log.info(aMarker, "Async request successful for paper: {} after {} attempts", 
                                entity.getPaperNo(), result.getTotalAttempts());
                        return processSuccessfulResponseAsync(result.getResponse(), entity, 
                                                            jsonRequestPayload, endpoint.toString());
                    } else {
                        log.error(aMarker, "Async request failed for paper: {} after {} attempts", 
                                 entity.getPaperNo(), result.getTotalAttempts());
                        return processFailedResponseAsync(result.getResponse(), entity, 
                                                        jsonRequestPayload, endpoint.toString(), 
                                                        result.getLastException());
                    }
                })
                .exceptionally(throwable -> {
                    log.error(aMarker, "Async request exception for paper: {}", entity.getPaperNo(), throwable);
                    return processExceptionResponseAsync(entity, jsonRequestPayload, endpoint.toString(), throwable);
                });
    }
    
    /**
     * Process multiple documents concurrently
     */
    public CompletableFuture<List<AgenticPaperFilterOutput>> processDocumentsBatch(
            URL endpoint, List<AgenticPaperFilterInput> entities, String baseJsonPayload) {
        
        log.info(aMarker, "Starting async batch processing for {} documents", entities.size());
        
        // Create list of futures for concurrent processing
        List<CompletableFuture<List<AgenticPaperFilterOutput>>> futures = new ArrayList<>();
        
        for (AgenticPaperFilterInput entity : entities) {
            // Customize payload for each entity (this would be your actual payload generation logic)
            String entityJsonPayload = customizePayloadForEntity(baseJsonPayload, entity);
            
            CompletableFuture<List<AgenticPaperFilterOutput>> future = 
                processDocumentAsync(endpoint, entity, entityJsonPayload);
            futures.add(future);
        }
        
        // Combine all futures into a single result
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<AgenticPaperFilterOutput> allResults = new ArrayList<>();
                    for (CompletableFuture<List<AgenticPaperFilterOutput>> future : futures) {
                        try {
                            allResults.addAll(future.get());
                        } catch (Exception e) {
                            log.error(aMarker, "Error getting result from batch future", e);
                        }
                    }
                    log.info(aMarker, "Completed async batch processing with {} total results", allResults.size());
                    return allResults;
                });
    }
    
    /**
     * Process document with custom retry configuration
     */
    public CompletableFuture<List<AgenticPaperFilterOutput>> processDocumentWithCustomRetry(
            URL endpoint, AgenticPaperFilterInput entity, String jsonRequestPayload,
            int maxRetries, long initialDelayMs) {
        
        // Create HTTP client
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();
        
        // Create async client with custom retry config
        AsyncHttpClientWithRetry customAsyncClient = AsyncHttpClientWithRetry.createWithRetry(
            httpClient, action, log, jdbiResourceName, maxRetries, initialDelayMs);
        
        try {
            CoproRetryErrorAuditTable retryAudit = createRetryAuditTable(entity, endpoint);
            
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(jsonRequestPayload, mediaType))
                    .build();
            
            return customAsyncClient.executeWithRetryAsync(request, jsonRequestPayload, retryAudit)
                    .thenCompose(result -> {
                        if (result.isSuccessful()) {
                            return processSuccessfulResponseAsync(result.getResponse(), entity, 
                                                                jsonRequestPayload, endpoint.toString());
                        } else {
                            return processFailedResponseAsync(result.getResponse(), entity, 
                                                            jsonRequestPayload, endpoint.toString(), 
                                                            result.getLastException());
                        }
                    })
                    .whenComplete((result, throwable) -> {
                        // Clean up custom client
                        customAsyncClient.close();
                    });
        } catch (Exception e) {
            customAsyncClient.close();
            return CompletableFuture.failedFuture(e);
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        log.info(aMarker, "Shutting down AsyncAgenticPaperFilterExample");
        if (asyncHttpClient != null) {
            asyncHttpClient.close();
        }
    }
    
    // Private helper methods for async response processing
    
    private CompletableFuture<List<AgenticPaperFilterOutput>> processSuccessfulResponseAsync(
            Response response, AgenticPaperFilterInput entity, String request, String endpoint) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseBody = response.body() != null ? response.body().string() : "";
                log.debug(aMarker, "Processing successful async response for paper: {}", entity.getPaperNo());
                
                // Here you would implement your actual response processing logic
                // This is a simplified example
                List<AgenticPaperFilterOutput> results = new ArrayList<>();
                
                AgenticPaperFilterOutput output = AgenticPaperFilterOutput.builder()
                        .filePath(entity.getFilePath())
                        .originId(entity.getOriginId())
                        .groupId(entity.getGroupId())
                        .paperNo(entity.getPaperNo())
                        .status("COMPLETED")
                        .stage("AGENTIC PAPER FILTER")
                        .message("Async processing completed successfully")
                        .request(request)
                        .response(responseBody)
                        .endpoint(endpoint)
                        .build();
                
                results.add(output);
                return results;
                
            } catch (Exception e) {
                log.error(aMarker, "Error processing successful async response for paper: {}", 
                         entity.getPaperNo(), e);
                throw new RuntimeException("Failed to process successful response", e);
            }
        });
    }
    
    private CompletableFuture<List<AgenticPaperFilterOutput>> processFailedResponseAsync(
            Response response, AgenticPaperFilterInput entity, String request, String endpoint, Exception exception) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String responseBody = "";
                String errorMessage = "Unknown error";
                
                if (response != null) {
                    responseBody = response.body() != null ? response.body().string() : "";
                    errorMessage = "HTTP " + response.code() + ": " + response.message();
                } else if (exception != null) {
                    errorMessage = exception.getMessage();
                    responseBody = "Exception: " + exception.getClass().getSimpleName();
                }
                
                log.warn(aMarker, "Processing failed async response for paper: {} - {}", 
                        entity.getPaperNo(), errorMessage);
                
                List<AgenticPaperFilterOutput> results = new ArrayList<>();
                
                AgenticPaperFilterOutput output = AgenticPaperFilterOutput.builder()
                        .filePath(entity.getFilePath())
                        .originId(entity.getOriginId())
                        .groupId(entity.getGroupId())
                        .paperNo(entity.getPaperNo())
                        .status("FAILED")
                        .stage("AGENTIC PAPER FILTER")
                        .message("Async processing failed: " + errorMessage)
                        .request(request)
                        .response(responseBody)
                        .endpoint(endpoint)
                        .build();
                
                results.add(output);
                return results;
                
            } catch (Exception e) {
                log.error(aMarker, "Error processing failed async response for paper: {}", 
                         entity.getPaperNo(), e);
                throw new RuntimeException("Failed to process failed response", e);
            }
        });
    }
    
    private List<AgenticPaperFilterOutput> processExceptionResponseAsync(
            AgenticPaperFilterInput entity, String request, String endpoint, Throwable throwable) {
        
        log.error(aMarker, "Processing exception async response for paper: {}", entity.getPaperNo(), throwable);
        
        List<AgenticPaperFilterOutput> results = new ArrayList<>();
        
        AgenticPaperFilterOutput output = AgenticPaperFilterOutput.builder()
                .filePath(entity.getFilePath())
                .originId(entity.getOriginId())
                .groupId(entity.getGroupId())
                .paperNo(entity.getPaperNo())
                .status("FAILED")
                .stage("AGENTIC PAPER FILTER")
                .message("Async processing exception: " + throwable.getMessage())
                .request(request)
                .response("Exception: " + throwable.getClass().getSimpleName())
                .endpoint(endpoint)
                .build();
        
        results.add(output);
        return results;
    }
    
    private CoproRetryErrorAuditTable createRetryAuditTable(AgenticPaperFilterInput entity, URL endpoint) {
        CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
        auditTable.setOriginId(entity.getOriginId());
        auditTable.setGroupId(entity.getGroupId());
        auditTable.setTenantId(entity.getTenantId());
        auditTable.setProcessId(entity.getProcessId());
        auditTable.setFilePath(entity.getFilePath());
        auditTable.setPaperNo(entity.getPaperNo());
        auditTable.setRootPipelineId(entity.getRootPipelineId());
        auditTable.setBatchId(entity.getBatchId());
        auditTable.setStage("AGENTIC PAPER FILTER");
        auditTable.setEndpoint(endpoint.toString());
        return auditTable;
    }
    
    private String customizePayloadForEntity(String basePayload, AgenticPaperFilterInput entity) {
        // This would be your actual payload customization logic
        // For now, just return the base payload
        return basePayload;
    }
    
    /**
     * Factory method for creating async processor with retry enabled
     */
    public static AsyncAgenticPaperFilterExample createWithAsyncRetry(
            Logger log, Marker aMarker, ActionExecutionAudit action, String jdbiResourceName,
            int timeoutMinutes, int maxRetries) {
        
        // Enable async retry in the action context
        action.getContext().put("copro.isretry.enabled", "true");
        action.getContext().put("copro.retry.attempt", String.valueOf(maxRetries));
        
        return new AsyncAgenticPaperFilterExample(log, aMarker, action, timeoutMinutes, jdbiResourceName);
    }
}
