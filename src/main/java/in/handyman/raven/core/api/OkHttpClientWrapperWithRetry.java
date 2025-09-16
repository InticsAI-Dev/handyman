package in.handyman.raven.core.api;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.agentic.paper.filter.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

/**
 * Enhanced OkHttpClient wrapper with built-in retry logic, audit trail, and security features.
 * This class provides a drop-in replacement for the static retry logic in CoproRetryService
 * with improved thread safety, configuration management, and error handling.
 * 
 * Key Features:
 * - Thread-safe retry logic with configurable parameters
 * - Comprehensive audit trail with encryption support
 * - Intelligent error classification (retryable vs non-retryable)
 * - Exponential backoff with jitter
 * - Resource management and proper cleanup
 * - Integration with Handyman framework's security and audit systems
 * 
 * @author Handyman Raven Framework
 * @version 2.0.0
 */
@Slf4j
public class OkHttpClientWrapperWithRetry {

    private final OkHttpClient httpClient;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger actionLogger;
    private final String jdbiResourceName;
    private final Jdbi jdbi;
    
    // Retry configuration
    private final int maxRetries;
    private final boolean retryEnabled;
    private final long initialDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    
    // Error classification
    private static final List<Integer> NON_RETRYABLE_STATUS_CODES = Arrays.asList(400, 401, 402, 403, 404, 422);
    private static final String COPRO_RETRY_ERROR_AUDIT = "copro_retry_error_audit";

    /**
     * Constructor with custom HTTP client and configuration
     */
    public OkHttpClientWrapperWithRetry(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit, 
                                      Logger actionLogger, String jdbiResourceName) {
        this.httpClient = httpClient;
        this.actionExecutionAudit = actionExecutionAudit;
        this.actionLogger = actionLogger;
        this.jdbiResourceName = jdbiResourceName;
        this.jdbi = ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        
        // Load configuration from action context
        this.retryEnabled = Boolean.parseBoolean(
            actionExecutionAudit.getContext().getOrDefault("copro.isretry.enabled", "false"));
        this.maxRetries = retryEnabled ? 
            Integer.parseInt(actionExecutionAudit.getContext().getOrDefault("copro.retry.attempt", "3")) : 1;
        this.initialDelayMs = Long.parseLong(
            actionExecutionAudit.getContext().getOrDefault("copro.retry.initial.delay.ms", "1000"));
        this.maxDelayMs = Long.parseLong(
            actionExecutionAudit.getContext().getOrDefault("copro.retry.max.delay.ms", "30000"));
        this.backoffMultiplier = Double.parseDouble(
            actionExecutionAudit.getContext().getOrDefault("copro.retry.backoff.multiplier", "2.0"));
    }

    /**
     * Constructor with default HTTP client configuration
     */
    public OkHttpClientWrapperWithRetry(ActionExecutionAudit actionExecutionAudit, Logger actionLogger, 
                                      String jdbiResourceName, int timeoutMinutes) {
        this(createDefaultHttpClient(timeoutMinutes), actionExecutionAudit, actionLogger, jdbiResourceName);
    }

    /**
     * Execute HTTP request with retry logic and comprehensive audit trail
     * This method replaces the static callCoproApiWithRetry method
     */
    public Response executeWithRetry(Request request, String requestBody, 
                                   CoproRetryErrorAuditTable retryAudit) throws IOException {
        
        actionLogger.info("Starting HTTP request with retry: URL={}, MaxRetries={}, RetryEnabled={}", 
                         request.url(), maxRetries, retryEnabled);
        
        int attempt = 1;
        Response response = null;
        IOException lastException = null;

        while (attempt <= maxRetries) {
            try {
                actionLogger.debug("HTTP request attempt {} of {} for URL: {}", attempt, maxRetries, request.url());
                
                response = httpClient.newCall(request).execute();
                
                if (isSuccessfulResponse(response)) {
                    actionLogger.info("HTTP request successful on attempt {} for URL: {}", attempt, request.url());
                    retryAudit.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                    insertAuditRecord(attempt, retryAudit, request, requestBody, response, null);
                    return response;
                }
                
                if (!shouldRetry(response, attempt)) {
                    actionLogger.warn("HTTP request will not be retried. Response code: {}, Attempt: {}/{}", 
                                    response != null ? response.code() : "null", attempt, maxRetries);
                    break;
                }
                
                logRetryAttempt(attempt, response, null);
                insertAuditRecord(attempt, retryAudit, request, requestBody, response, null);
                
            } catch (IOException e) {
                lastException = e;
                actionLogger.error("HTTP request attempt {} failed with IOException for URL: {}", 
                                 attempt, request.url(), e);
                
                if (!shouldRetry(null, attempt)) {
                    insertAuditRecord(attempt, retryAudit, request, requestBody, null, e);
                    break;
                }
                
                logRetryAttempt(attempt, null, e);
                insertAuditRecord(attempt, retryAudit, request, requestBody, null, e);
            }
            
            // Wait before next attempt (except for the last attempt)
            if (attempt < maxRetries) {
                waitBeforeRetry(attempt);
            }
            attempt++;
        }
        
        retryAudit.setStatus(ConsumerProcessApiStatus.FAILED.getStatusDescription());
        
        if (response != null) {
            return response;
        }
        
        throw lastException != null ? lastException : 
            new IOException("HTTP request failed after " + (attempt - 1) + " attempts with no response and no exception");
    }

    /**
     * Execute HTTP request without retry (single attempt)
     */
    public Response execute(Request request) throws IOException {
        actionLogger.debug("Executing single HTTP request for URL: {}", request.url());
        return httpClient.newCall(request).execute();
    }

    /**
     * Create a new instance with different timeout configuration
     */
    public OkHttpClientWrapperWithRetry withTimeout(int timeoutMinutes) {
        OkHttpClient newClient = httpClient.newBuilder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .build();
        
        return new OkHttpClientWrapperWithRetry(newClient, actionExecutionAudit, actionLogger, jdbiResourceName);
    }

    /**
     * Get retry configuration information
     */
    public String getRetryConfiguration() {
        return String.format("RetryEnabled=%s, MaxRetries=%d, InitialDelay=%dms, MaxDelay=%dms, BackoffMultiplier=%.1f",
                           retryEnabled, maxRetries, initialDelayMs, maxDelayMs, backoffMultiplier);
    }

    /**
     * Close the HTTP client and release resources
     */
    public void close() {
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

    // Private helper methods

    private static OkHttpClient createDefaultHttpClient(int timeoutMinutes) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .writeTimeout(timeoutMinutes, TimeUnit.MINUTES)
                .retryOnConnectionFailure(false) // We handle retries manually
                .build();
    }

    private boolean isSuccessfulResponse(Response response) {
        return response != null && response.isSuccessful() && response.body() != null;
    }

    private boolean shouldRetry(Response response, int attempt) {
        if (!retryEnabled || attempt >= maxRetries) {
            return false;
        }

        // Network error or null response - should retry
        if (response == null) {
            return true;
        }

        // Check if the status code is retryable
        return !NON_RETRYABLE_STATUS_CODES.contains(response.code());
    }

    private void waitBeforeRetry(int attempt) {
        long delay = Math.min(
            (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1)),
            maxDelayMs
        );
        
        // Add jitter to prevent thundering herd
        long jitter = (long) (delay * 0.1 * Math.random());
        long finalDelay = delay + jitter;
        
        try {
            actionLogger.debug("Waiting {}ms before retry attempt {} (base: {}ms, jitter: {}ms)", 
                             finalDelay, attempt + 1, delay, jitter);
            Thread.sleep(finalDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HandymanException("Retry wait interrupted", e);
        }
    }

    private void logRetryAttempt(int attempt, Response response, Exception exception) {
        if (response != null) {
            actionLogger.warn("Attempt {}: Unsuccessful response {} - {}", 
                            attempt, response.code(), response.message());
        } else if (exception != null) {
            actionLogger.warn("Attempt {}: Exception - {}", attempt, exception.getMessage());
        }
    }

    private void insertAuditRecord(int attempt, CoproRetryErrorAuditTable retryAudit, Request request, 
                                 String requestBody, Response response, Exception exception) {
        try {
            populateAuditRecord(attempt, retryAudit, requestBody, response, exception, request);
            insertAuditToDatabase(retryAudit);
        } catch (Exception e) {
            actionLogger.error("Error inserting audit record for attempt {}: {}", attempt, e.getMessage(), e);
            HandymanException.insertException("Error inserting HTTP retry audit", new HandymanException(e), actionExecutionAudit);
        }
    }

    private void populateAuditRecord(int attempt, CoproRetryErrorAuditTable retryAudit, String requestBody, 
                                   Response response, Exception exception, Request request) {
        retryAudit.setRequest(encryptRequestResponse(requestBody));
        retryAudit.setAttempt(attempt);
        retryAudit.setEndpoint(request.url().toString());
        retryAudit.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
        
        if (response != null) {
            retryAudit.setMessage(String.format("HTTP %d: %s", response.code(), response.message()));
            try {
                String responseBody = response.body() != null ? response.body().string() : "Empty response body";
                retryAudit.setResponse(encryptRequestResponse(responseBody));
            } catch (IOException e) {
                retryAudit.setResponse(encryptRequestResponse("Error reading response body: " + e.getMessage()));
            }
        } else if (exception != null) {
            String message = exception.getMessage() != null ? exception.getMessage() : ExceptionUtil.toString(exception);
            retryAudit.setMessage(message);
            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(exception)));
        }
    }

    private void insertAuditToDatabase(CoproRetryErrorAuditTable retryAudit) {
        try {
            jdbi.useTransaction(handle -> {
                try {
                    handle.createUpdate("INSERT INTO macro." + COPRO_RETRY_ERROR_AUDIT +
                            " (origin_id, group_id, attempt, tenant_id, process_id, file_path, paper_no, message, status, stage, " +
                            "created_on, root_pipeline_id, batch_id, last_updated_on, request, response, endpoint) " +
                            "VALUES (:originId, :groupId, :attempt, :tenantId, :processId, :filePath, :paperNo, :message, " +
                            ":status, :stage, :createdOn, :rootPipelineId, :batchId, NOW(), :request, :response, :endpoint)")
                            .bindBean(retryAudit)
                            .execute();
                } catch (Exception e) {
                    actionLogger.error("Error executing audit insert: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to insert audit record", e);
                }
            });
        } catch (Exception e) {
            actionLogger.error("Transaction failed for audit insert: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in executing audit insert query", handymanException, actionExecutionAudit);
            throw handymanException;
        }
    }

    private String encryptRequestResponse(String data) {
        if (data == null) {
            return null;
        }
        
        String encryptReqRes = actionExecutionAudit.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes)) {
            try {
                return SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, actionLogger)
                        .encrypt(data, "AES256", "COPRO_REQUEST");
            } catch (Exception e) {
                actionLogger.warn("Failed to encrypt request/response data", e);
                return data;
            }
        }
        return data;
    }

    // Static factory methods for easy creation

    /**
     * Create a wrapper with default configuration from action context
     */
    public static OkHttpClientWrapperWithRetry create(ActionExecutionAudit actionExecutionAudit, 
                                                     Logger actionLogger, String jdbiResourceName, 
                                                     int timeoutMinutes) {
        return new OkHttpClientWrapperWithRetry(actionExecutionAudit, actionLogger, jdbiResourceName, timeoutMinutes);
    }

    /**
     * Create a wrapper with custom HTTP client
     */
    public static OkHttpClientWrapperWithRetry create(OkHttpClient httpClient, ActionExecutionAudit actionExecutionAudit, 
                                                     Logger actionLogger, String jdbiResourceName) {
        return new OkHttpClientWrapperWithRetry(httpClient, actionExecutionAudit, actionLogger, jdbiResourceName);
    }

    /**
     * Create a wrapper with retry enabled and custom max retries
     */
    public static OkHttpClientWrapperWithRetry createWithRetry(ActionExecutionAudit actionExecutionAudit, 
                                                              Logger actionLogger, String jdbiResourceName, 
                                                              int timeoutMinutes, int maxRetries) {
        // Temporarily override context for this instance
        actionExecutionAudit.getContext().put("copro.isretry.enabled", "true");
        actionExecutionAudit.getContext().put("copro.retry.attempt", String.valueOf(maxRetries));
        
        return new OkHttpClientWrapperWithRetry(actionExecutionAudit, actionLogger, jdbiResourceName, timeoutMinutes);
    }
}
