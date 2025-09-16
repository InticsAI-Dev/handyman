package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.api.OkHttpClientWrapperWithRetry;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Refactored CoproRetryService using the new OkHttpClientWrapperWithRetry.
 * This demonstrates how to replace the static retry logic with the new wrapper class.
 * 
 * Benefits of this refactoring:
 * - Thread-safe operations (no more static state)
 * - Better resource management
 * - Improved configuration management
 * - Enhanced error handling and logging
 * - Consistent with enterprise patterns
 * 
 * @author Handyman Raven Framework
 * @version 2.0.0
 */
public class CoproRetryServiceRefactored {
    
    private static final Logger log = LoggerFactory.getLogger(CoproRetryServiceRefactored.class);

    /**
     * New method signature that replaces the original static method.
     * This method is thread-safe and doesn't rely on static state.
     */
    public static Response callCoproApiWithRetry(Request request, String requestBody, 
                                               CoproRetryErrorAuditTable retryAudit,
                                               ActionExecutionAudit actionAudit, 
                                               String jdbiResourceName,
                                               OkHttpClient httpClient) throws IOException {
        
        // Create the wrapper with the existing HTTP client
        OkHttpClientWrapperWithRetry wrapper = OkHttpClientWrapperWithRetry.create(
            httpClient, actionAudit, log, jdbiResourceName);
        
        try {
            // Execute with retry logic and audit trail
            return wrapper.executeWithRetry(request, requestBody, retryAudit);
        } finally {
            // Clean up resources (optional, but good practice)
            wrapper.close();
        }
    }

    /**
     * Simplified method that creates its own HTTP client with default configuration
     */
    public static Response callCoproApiWithRetrySimple(Request request, String requestBody,
                                                      CoproRetryErrorAuditTable retryAudit,
                                                      ActionExecutionAudit actionAudit,
                                                      String jdbiResourceName,
                                                      int timeoutMinutes) throws IOException {
        
        // Create wrapper with default HTTP client
        OkHttpClientWrapperWithRetry wrapper = OkHttpClientWrapperWithRetry.create(
            actionAudit, log, jdbiResourceName, timeoutMinutes);
        
        try {
            return wrapper.executeWithRetry(request, requestBody, retryAudit);
        } finally {
            wrapper.close();
        }
    }

    /**
     * Advanced method with custom retry configuration
     */
    public static Response callCoproApiWithCustomRetry(Request request, String requestBody,
                                                      CoproRetryErrorAuditTable retryAudit,
                                                      ActionExecutionAudit actionAudit,
                                                      String jdbiResourceName,
                                                      int timeoutMinutes,
                                                      int maxRetries) throws IOException {
        
        // Create wrapper with custom retry configuration
        OkHttpClientWrapperWithRetry wrapper = OkHttpClientWrapperWithRetry.createWithRetry(
            actionAudit, log, jdbiResourceName, timeoutMinutes, maxRetries);
        
        try {
            log.info("Using custom retry configuration: {}", wrapper.getRetryConfiguration());
            return wrapper.executeWithRetry(request, requestBody, retryAudit);
        } finally {
            wrapper.close();
        }
    }

    /**
     * Method for creating a reusable wrapper instance (recommended for multiple calls)
     */
    public static OkHttpClientWrapperWithRetry createRetryWrapper(ActionExecutionAudit actionAudit,
                                                                 String jdbiResourceName,
                                                                 int timeoutMinutes) {
        return OkHttpClientWrapperWithRetry.create(actionAudit, log, jdbiResourceName, timeoutMinutes);
    }

    /**
     * Example usage in a service class
     */
    public static class ExampleUsage {
        
        private final OkHttpClientWrapperWithRetry httpWrapper;
        private final ActionExecutionAudit actionAudit;
        
        public ExampleUsage(ActionExecutionAudit actionAudit, String jdbiResourceName, int timeoutMinutes) {
            this.actionAudit = actionAudit;
            this.httpWrapper = OkHttpClientWrapperWithRetry.create(actionAudit, log, jdbiResourceName, timeoutMinutes);
        }
        
        public Response makeApiCall(Request request, String requestBody, CoproRetryErrorAuditTable auditTable) throws IOException {
            return httpWrapper.executeWithRetry(request, requestBody, auditTable);
        }
        
        public void shutdown() {
            httpWrapper.close();
        }
    }
}
