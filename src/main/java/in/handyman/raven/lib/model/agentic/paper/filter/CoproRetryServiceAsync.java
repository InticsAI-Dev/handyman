package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import lombok.Getter;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryServiceAsync {

    private static final Logger log = LoggerFactory.getLogger(CoproRetryServiceAsync.class);
    private static final List<Integer> NON_RETRY_CODES = List.of(400, 401, 402, 403, 404);
    private static final int MAX_BODY_LENGTH = 6000; // truncate long request/response bodies
    private static final int ENCRYPTION_TIMEOUT_SECONDS = 3;

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2);
    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(4);

    private final HandymanRepo handymanRepo;
    private final OkHttpClient httpClient;

    /**
     * Main constructor. Provide executors if you manage lifecycle outside this class.
     *
     * @param handymanRepo repo implementation (required)
     * @param httpClient   OkHttpClient (required)
     */
    public CoproRetryServiceAsync(
            HandymanRepo handymanRepo,
            OkHttpClient httpClient
    ) {
        this.handymanRepo = Objects.requireNonNull(handymanRepo, "handymanRepo");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /**
     * DTO returned to caller (body read into memory to avoid leaking okhttp Response).
     */
    @Getter
    public static class CoproResponse {
        private final int httpCode;
        private final String message;
        private final byte[] body;

        public CoproResponse(int httpCode, String message, byte[] body) {
            this.httpCode = httpCode;
            this.message = message;
            this.body = body;
        }

        public String getBodyAsString() {
            return body == null ? null : new String(body, StandardCharsets.UTF_8);
        }
    }

    /**
     * Public API: returns future that completes with CoproResponse or exceptionally if all retries fail.
     */
    public CompletableFuture<CoproResponse> callCoproApiWithRetryAsync(
            Request request,
            String requestBody,
            CoproRetryErrorAuditTable retryAudit,
            ActionExecutionAudit actionAudit
    ) {
        int resolvedMaxRetries = resolveMaxRetries(actionAudit);
        CompletableFuture<CoproResponse> resultFuture = new CompletableFuture<>();
        attemptAsync(request, requestBody, retryAudit, actionAudit, resultFuture, resolvedMaxRetries);
        return resultFuture;
    }

    private void attemptAsync(
            Request request,
            String requestBody,
            CoproRetryErrorAuditTable originalAudit,
            ActionExecutionAudit action,
            CompletableFuture<CoproResponse> resultFuture,
            int maxAttempts) {

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (resultFuture.isDone()) return;

            int finalAttempt = attempt;
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logFailure(finalAttempt, e);
                    handleFailure(request, originalAudit, finalAttempt, requestBody, e, resultFuture, action, maxAttempts);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    handleResponse(request, response, originalAudit, action, requestBody, resultFuture, finalAttempt, maxAttempts);
                }
            });

            // Optional: Add delay before retrying
            try {
                Thread.sleep(1000); // Optional retry delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                resultFuture.completeExceptionally(e);
                return;
            }
        }
    }

    private void logFailure(int attempt, IOException e) {
        if (e instanceof SocketTimeoutException) {
            log.warn("Attempt {} failed due to SocketTimeoutException: {}", attempt, e.getMessage());
        } else {
            log.warn("Attempt {} failed with IOException: {}", attempt, e.getMessage());
        }
    }

    private void handleResponse(Request request, Response response, CoproRetryErrorAuditTable originalAudit, ActionExecutionAudit action,
                                String requestBody, CompletableFuture<CoproResponse> resultFuture, int attempt, int maxAttempts) {
        try (Response r = response) {
            byte[] bodyBytes = r.body() != null ? r.body().bytes() : null;

            boolean retryRequired = isRetryRequired(r);
            CoproRetryErrorAuditTable auditCopy = cloneFullAuditForAttemptIfNeeded(originalAudit);

            if (retryRequired) {
                log.warn("Attempt {}: Unsuccessful response {} - {}", attempt, response.code(), response.message());

                populateAudit(auditCopy, attempt, requestBody, r, null, action);
                persistAuditAsync(auditCopy, action);

                if (attempt < maxAttempts) {
                    attemptAsync(request, requestBody, originalAudit, action, resultFuture, maxAttempts);
                } else {
                    resultFuture.complete(new CoproResponse(r.code(), r.message(), bodyBytes));
                }
            } else {
                auditCopy.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                populateAudit(auditCopy, attempt, requestBody, r, null, action);
                persistAuditAsync(auditCopy, action);
                resultFuture.complete(new CoproResponse(r.code(), r.message(), bodyBytes));
            }
        } catch (IOException ex) {
            log.error("IOException processing response on attempt {}: {}", attempt, ex.getMessage(), ex);
            resultFuture.completeExceptionally(ex);
        } catch (Exception ex) {
            log.error("Unexpected error on attempt {}: {}", attempt, ex.getMessage(), ex);
            resultFuture.completeExceptionally(ex);
        }
    }

    private void handleFailure(Request request, CoproRetryErrorAuditTable originalAudit, int attempt, String requestBody, IOException e,
                               CompletableFuture<CoproResponse> resultFuture, ActionExecutionAudit action, int maxAttempts) {
        CoproRetryErrorAuditTable auditCopy = cloneFullAuditForAttemptIfNeeded(originalAudit);
        populateAudit(auditCopy, attempt, requestBody, null, e, action);
        persistAuditAsync(auditCopy, action);

        if (attempt < maxAttempts) {
            attemptAsync(request, requestBody, originalAudit, action, resultFuture, maxAttempts);
        } else {
            resultFuture.completeExceptionally(e);
        }
    }

    private boolean isRetryRequired(Response response) {
        return response == null || !response.isSuccessful() || response.body() == null
                && !NON_RETRY_CODES.contains(response.code());
    }

    /**
     * Resolve max retries: prefer action context, fallback to injected default.
     */
    private int resolveMaxRetries(ActionExecutionAudit action) {
        int defaultMaxRetries = 3;
        if (action != null && action.getContext() != null) {
            String cfg = action.getContext().get("copro.retry.attempt");
            String enabled = action.getContext().getOrDefault("copro.isretry.enabled", "false");
            if ("true".equalsIgnoreCase(enabled) && cfg != null) {
                try {
                    int v = Integer.parseInt(cfg);
                    return Math.max(1, v);
                } catch (NumberFormatException ignored) { /* fallthrough to default */ }
            }
        }
        return defaultMaxRetries;
    }

    private void persistAuditAsync(CoproRetryErrorAuditTable auditCopy, ActionExecutionAudit action) {
        DB_EXECUTOR.submit(() -> {
            try {
                if (auditCopy.getCreatedOn() == null) {
                    auditCopy.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
                }
                // Truncate long fields to avoid DB/IO pressure
                truncateAuditFields(auditCopy);
                handymanRepo.insertAuditToDb(auditCopy, action);
            } catch (Exception ex) {
                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex));
                HandymanException.insertException("Error inserting retry audit", new HandymanException(ex), action);
            }
        });
    }

    private void truncateAuditFields(CoproRetryErrorAuditTable auditCopy) {
        if (auditCopy.getRequest() != null && auditCopy.getRequest().length() > MAX_BODY_LENGTH) {
            auditCopy.setRequest(auditCopy.getRequest().substring(0, MAX_BODY_LENGTH));
        }
        if (auditCopy.getResponse() != null && auditCopy.getResponse().length() > MAX_BODY_LENGTH) {
            auditCopy.setResponse(auditCopy.getResponse().substring(0, MAX_BODY_LENGTH));
        }
    }

    /**
     * Defensive full copy of the audit object. Copies Timestamp instances to avoid shared mutable TS objects.
     * Intentionally not copying 'attempt' so the per-attempt value can be set in populateAudit.
     */
    private CoproRetryErrorAuditTable cloneFullAuditForAttemptIfNeeded(CoproRetryErrorAuditTable original) {
        if (original == null) {
            return new CoproRetryErrorAuditTable();
        }
        Timestamp createdOnCopy = (original.getCreatedOn() != null) ? new Timestamp(original.getCreatedOn().getTime()) : null;
        Timestamp lastUpdatedOnCopy = (original.getLastUpdatedOn() != null) ? new Timestamp(original.getLastUpdatedOn().getTime()) : null;

        return CoproRetryErrorAuditTable.builder()
                .originId(original.getOriginId())
                .groupId(original.getGroupId())
                .tenantId(original.getTenantId())
                .templateId(original.getTemplateId())
                .processId(original.getProcessId())
                .filePath(original.getFilePath())
                .containerName(original.getContainerName())
                .containerValue(original.getContainerValue())
                .fileName(original.getFileName())
                .paperNo(original.getPaperNo())
                .status(original.getStatus())
                .stage(original.getStage())
                .message(original.getMessage())
                .createdOn(createdOnCopy)
                .rootPipelineId(original.getRootPipelineId())
                .batchId(original.getBatchId())
                .lastUpdatedOn(lastUpdatedOnCopy)
                .request(original.getRequest())
                .response(original.getResponse())
                .endpoint(original.getEndpoint())
                // intentionally not copying attempt
                .build();
    }

    private void populateAudit(CoproRetryErrorAuditTable retryAudit, int attempt, String requestBody, Response response, Exception e, ActionExecutionAudit action) {
        retryAudit.setAttempt(attempt);
        retryAudit.setRequest(encryptRequestResponse(requestBody, action));

        if (response != null) {
            retryAudit.setMessage(response.message());
            retryAudit.setResponse(encryptRequestResponse(response.toString(), action));
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            retryAudit.setMessage(message);
            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), action));
        }
    }

    private String encryptRequestResponse(String data, ActionExecutionAudit action) {
        if (data == null) return null;

        String encrypt = action != null && action.getContext() != null
                ? action.getContext().get(ENCRYPT_REQUEST_RESPONSE)
                : null;

        if ("true".equalsIgnoreCase(encrypt)) {
            return CompletableFuture.supplyAsync(() -> {
                        try {
                            return SecurityEngine
                                    .getInticsIntegrityMethod(action, log)
                                    .encrypt(data, "AES256", "COPRO_REQUEST");
                        } catch (Exception ex) {
                            throw new CompletionException(ex);
                        }
                    }).orTimeout(ENCRYPTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .exceptionally(ex -> handleEncryptionFailure(ex, data)).join();
        }
        return data;
    }

    private String handleEncryptionFailure(Throwable ex, String data) {
        if (ex instanceof TimeoutException || ex.getCause() instanceof SocketTimeoutException) {
            log.warn("Encryption service timed out after {}s; storing plaintext for audit.", ENCRYPTION_TIMEOUT_SECONDS);
            return "[ENCRYPTION_TIMEOUT] " + truncateSafe(data);
        } else {
            log.error("Encryption failed: {}; storing plaintext for audit.", ex.getMessage());
            return "[ENCRYPTION_ERROR] " + truncateSafe(data);
        }
    }

    /**
     * Utility to safely truncate very large strings for audit fallback
     */
    private String truncateSafe(String input) {
        if (input == null) return null;
        return input.length() > MAX_BODY_LENGTH ? input.substring(0, MAX_BODY_LENGTH) + "...[TRUNCATED]" : input;
    }

    /**
     * Call to shut down the scheduler/DB executor if this service created them.
     */
    @PreDestroy
    public CompletableFuture<Void> shutdown() {
        // Ensure both executors are properly shut down before completing the shutdown
        CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> shutdownExecutor("dbExecutor", DB_EXECUTOR)),
                CompletableFuture.runAsync(() -> shutdownExecutor("scheduler", SCHEDULER))
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                shutdownFuture.completeExceptionally(ex);
            } else {
                shutdownFuture.complete(null);
            }
        });

        return shutdownFuture;
    }

    private void shutdownExecutor(String name, ExecutorService executor) {
        if (executor == null || executor.isShutdown()) return;

        try {
            log.info("Shutting down {} gracefully...", name);
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("{} did not terminate in {}s; forcing shutdownNow()", name, 5);
                executor.shutdownNow();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
            log.info("{} shutdown complete", name);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during {} shutdown; forcing shutdownNow()", name);
            executor.shutdownNow();
        } catch (Exception e) {
            log.error("Error shutting down {}: {}", name, e.getMessage(), e);
        }
    }

}
