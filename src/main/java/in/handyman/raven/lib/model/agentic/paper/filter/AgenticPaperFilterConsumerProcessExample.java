package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.api.OkHttpClientWrapperWithRetry;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Example showing how to integrate the new OkHttpClientWrapperWithRetry
 * into the AgenticPaperFilterConsumerProcess.
 * 
 * This demonstrates how to replace the direct OkHttpClient usage
 * with the new wrapper for improved retry logic and audit trail.
 */
public class AgenticPaperFilterConsumerProcessExample {
    
    private final ActionExecutionAudit action;
    private final Logger log;
    private final Marker aMarker;
    private final String jdbiResourceName;
    private final OkHttpClientWrapperWithRetry httpWrapper;
    
    public AgenticPaperFilterConsumerProcessExample(Logger log, Marker aMarker, 
                                                   ActionExecutionAudit action, 
                                                   int timeOut, String jdbiResourceName) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        
        // Create the HTTP wrapper with retry capabilities
        this.httpWrapper = OkHttpClientWrapperWithRetry.create(
            action, log, jdbiResourceName, timeOut);
        
        log.info(aMarker, "HTTP client wrapper initialized with config: {}", 
                httpWrapper.getRetryConfiguration());
    }
    
    /**
     * Example of making an HTTP request with the new wrapper
     * This replaces the direct httpclient.newCall(request).execute() pattern
     */
    public void makeTritonRequestWithRetry(URL endpoint, String jsonRequestPayload, 
                                         AgenticPaperFilterInput entity) throws IOException {
        
        // Create the audit table for tracking retry attempts
        CoproRetryErrorAuditTable retryAudit = createRetryAuditTable(entity, endpoint);
        
        // Build the HTTP request
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestPayload, mediaType))
                .build();
        
        log.info(aMarker, "Making HTTP request to {} with retry logic", endpoint);
        
        try {
            // Execute with automatic retry logic and audit trail
            Response response = httpWrapper.executeWithRetry(request, jsonRequestPayload, retryAudit);
            
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info(aMarker, "HTTP request successful for endpoint: {}", endpoint);
                processSuccessfulResponse(responseBody, entity);
            } else {
                log.warn(aMarker, "HTTP request failed with status: {} for endpoint: {}", 
                        response.code(), endpoint);
                processFailedResponse(response, entity);
            }
            
        } catch (IOException e) {
            log.error(aMarker, "HTTP request failed after all retry attempts for endpoint: {}", endpoint, e);
            processExceptionResponse(e, entity);
            throw e;
        }
    }
    
    /**
     * Example of making a simple HTTP request without retry
     */
    public void makeSimpleRequest(URL endpoint, String jsonRequestPayload) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestPayload, mediaType))
                .build();
        
        // Use the wrapper for single attempt (no retry)
        Response response = httpWrapper.execute(request);
        
        if (response.isSuccessful()) {
            String responseBody = response.body().string();
            log.info(aMarker, "Simple HTTP request successful: {}", endpoint);
            // Process response...
        }
    }
    
    /**
     * Example of using different timeout configuration
     */
    public void makeRequestWithCustomTimeout(URL endpoint, String jsonRequestPayload, 
                                           int customTimeoutMinutes) throws IOException {
        
        // Create a wrapper with different timeout
        OkHttpClientWrapperWithRetry customWrapper = httpWrapper.withTimeout(customTimeoutMinutes);
        
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(jsonRequestPayload, mediaType))
                .build();
        
        try {
            Response response = customWrapper.execute(request);
            // Process response...
            
        } finally {
            // Clean up the custom wrapper
            customWrapper.close();
        }
    }
    
    /**
     * Clean up resources when done
     */
    public void shutdown() {
        if (httpWrapper != null) {
            httpWrapper.close();
            log.info(aMarker, "HTTP client wrapper closed");
        }
    }
    
    // Helper methods
    
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
    
    private void processSuccessfulResponse(String responseBody, AgenticPaperFilterInput entity) {
        // Process the successful response
        log.debug(aMarker, "Processing successful response for paper: {}", entity.getPaperNo());
        // Add your response processing logic here
    }
    
    private void processFailedResponse(Response response, AgenticPaperFilterInput entity) {
        // Process the failed response
        log.warn(aMarker, "Processing failed response for paper: {}, status: {}", 
                entity.getPaperNo(), response.code());
        // Add your error handling logic here
    }
    
    private void processExceptionResponse(IOException exception, AgenticPaperFilterInput entity) {
        // Process the exception
        log.error(aMarker, "Processing exception response for paper: {}", entity.getPaperNo(), exception);
        // Add your exception handling logic here
    }
    
    /**
     * Factory method for creating instances with common configurations
     */
    public static AgenticPaperFilterConsumerProcessExample createWithRetry(Logger log, Marker aMarker,
                                                                          ActionExecutionAudit action,
                                                                          String jdbiResourceName,
                                                                          int timeoutMinutes,
                                                                          int maxRetries) {
        // Enable retry in the action context
        action.getContext().put("copro.isretry.enabled", "true");
        action.getContext().put("copro.retry.attempt", String.valueOf(maxRetries));
        
        return new AgenticPaperFilterConsumerProcessExample(log, aMarker, action, timeoutMinutes, jdbiResourceName);
    }
}
