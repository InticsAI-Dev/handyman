package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class CoproRetryServiceAsync {

    private static final Logger log = LoggerFactory.getLogger(CoproRetryServiceAsync.class);
    private static final List<Integer> NON_RETRY_CODES = List.of(400, 401, 402, 403, 404);
    private static final Random RANDOM = new Random();
    private static final int DEFAULT_DB_THREADS = 4;
    private static final int MAX_BODY_LENGTH = 4000; // truncate long request/response bodies

    private final HandymanRepo handymanRepo;        // DAO (injected)
    private final Jdbi jdbi;                       // optional - injected for other uses
    private final OkHttpClient httpClient;         // injected
    private final ScheduledExecutorService scheduler; // injected or created
    private final ExecutorService dbExecutor;      // injected or created (bounded)
    private final boolean ownExecutors;            // whether this instance owns the executors and should shutdown them

    private final long baseDelayMillis;
    private final int defaultMaxRetries;
    private final double jitterFactor; // e.g. 0.2 = +-20% jitter
    private final long maxBackoffMillis;

    /**
     * Main constructor. Provide executors if you manage lifecycle outside this class.
     *
     * @param handymanRepo     repo implementation (required)
     * @param jdbi             jdbi (optional but recommended)
     * @param httpClient       OkHttpClient (required)
     * @param scheduler        ScheduledExecutorService (if null, one will be created and owned by this class)
     * @param dbExecutor       ExecutorService for DB work (if null, a bounded pool will be created and owned by this class)
     * @param defaultMaxRetries fallback max retries if not provided in ActionExecutionAudit
     * @param baseDelayMillis  base delay for exponential backoff (ms)
     * @param jitterFactor     jitter factor [0..1]
     * @param maxBackoffMillis cap for backoff (ms)
     */
    public CoproRetryServiceAsync(
            HandymanRepo handymanRepo,
            Jdbi jdbi,
            OkHttpClient httpClient,
            ScheduledExecutorService scheduler,
            ExecutorService dbExecutor,
            int defaultMaxRetries,
            long baseDelayMillis,
            double jitterFactor,
            long maxBackoffMillis
    ) {
        this.handymanRepo = Objects.requireNonNull(handymanRepo, "handymanRepo");
        this.jdbi = jdbi; // optional
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");

        boolean createdScheduler = false;
        boolean createdDbExecutor = false;

        if (scheduler == null) {
            this.scheduler = Executors.newScheduledThreadPool(4, r -> {
                Thread t = new Thread(r, "copro-retry-scheduler");
                t.setDaemon(true);
                return t;
            });
            createdScheduler = true;
        } else {
            this.scheduler = scheduler;
        }

        if (dbExecutor == null) {
            this.dbExecutor = new ThreadPoolExecutor(
                    DEFAULT_DB_THREADS,
                    DEFAULT_DB_THREADS,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(1000),
                    r -> {
                        Thread t = new Thread(r, "copro-db-worker");
                        t.setDaemon(true);
                        return t;
                    }
            );
            createdDbExecutor = true;
        } else {
            this.dbExecutor = dbExecutor;
        }

        this.ownExecutors = createdScheduler || createdDbExecutor;

        this.defaultMaxRetries = Math.max(1, defaultMaxRetries);
        this.baseDelayMillis = Math.max(10, baseDelayMillis);
        this.jitterFactor = Double.isFinite(jitterFactor) ? Math.max(0.0, Math.min(1.0, jitterFactor)) : 0.2;
        this.maxBackoffMillis = Math.max(100, maxBackoffMillis);
    }


    /**
     * DTO returned to caller (body read into memory to avoid leaking okhttp Response).
     */
    public static class CoproResponse {
        private final int httpCode;
        private final String message;
        private final byte[] body;

        public CoproResponse(int httpCode, String message, byte[] body) {
            this.httpCode = httpCode;
            this.message = message;
            this.body = body;
        }

        public int getHttpCode() { return httpCode; }
        public String getMessage() { return message; }
        public byte[] getBody() { return body; }
        public String getBodyAsString() { return body == null ? null : new String(body, StandardCharsets.UTF_8); }
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
        attemptAsync(request, requestBody, retryAudit, actionAudit, resultFuture, 1, resolvedMaxRetries);
        return resultFuture;
    }

    private void attemptAsync(
            Request request,
            String requestBody,
            CoproRetryErrorAuditTable originalAudit,
            ActionExecutionAudit action,
            CompletableFuture<CoproResponse> resultFuture,
            int attempt,
            int maxAttempts) {

        if (resultFuture.isDone()) return;

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn("Attempt {} failed with IOException: {}", attempt, e.getMessage());
                // create a separate audit copy for this attempt (avoid shared mutable state)
                CoproRetryErrorAuditTable auditCopy = cloneFullAuditForAttemptIfNeeded(originalAudit);
                populateAudit(auditCopy, attempt, requestBody, null, e, action);
                persistAuditAsync(auditCopy, action);

                if (attempt < maxAttempts) {
                    scheduler.execute(() ->
                            attemptAsync(request, requestBody, originalAudit, action, resultFuture, attempt + 1, maxAttempts));
                } else {
                    resultFuture.completeExceptionally(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response r = response) { // ensure body/response closed here
                    boolean retryRequired = isRetryRequired(r);
                    // create per-attempt audit copy
                    CoproRetryErrorAuditTable auditCopy = cloneFullAuditForAttemptIfNeeded(originalAudit);
                    byte[] bodyBytes = null;
                    try {
                        if (r.body() != null) {
                            bodyBytes = r.body().bytes(); // read fully while we have the response
                        }
                    } catch (IOException ioe) {
                        log.warn("Failed to read response body on attempt {}: {}", attempt, ioe.getMessage());
                    }

                    if (retryRequired) {
                        logRetryAttempt(attempt, r);
                        populateAudit(auditCopy, attempt, requestBody, r, null, action);
                        persistAuditAsync(auditCopy, action);

                        if (attempt < maxAttempts) {
                            scheduler.execute(() ->
                                    attemptAsync(request, requestBody, originalAudit, action, resultFuture, attempt + 1, maxAttempts));
                        } else {
                            resultFuture.complete(new CoproResponse(r.code(), r.message(), bodyBytes));
                        }
                    } else {
                        auditCopy.setStatus(ConsumerProcessApiStatus.COMPLETED.getStatusDescription());
                        populateAudit(auditCopy, attempt, requestBody, r, null, action);
                        persistAuditAsync(auditCopy, action);
                        resultFuture.complete(new CoproResponse(r.code(), r.message(), bodyBytes));
                    }
                } catch (Exception ex) {
                    log.error("Unexpected error while processing response on attempt {}: {}", attempt, ex.getMessage(), ex);
                    if (!resultFuture.isDone()) {
                        resultFuture.completeExceptionally(ex);
                    }
                }
            }
        });
    }

    private boolean isRetryRequired(Response response) {
        if (response == null) return true;
        if (!response.isSuccessful() || response.body() == null) {
            return !NON_RETRY_CODES.contains(response.code());
        }
        return false;
    }

    private void logRetryAttempt(int attempt, Response response) {
        log.warn("Attempt {}: Unsuccessful response {} - {}", attempt, response.code(), response.message());
    }

    /**
     * Resolve max retries: prefer action context, fallback to injected default.
     */
    private int resolveMaxRetries(ActionExecutionAudit action) {
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
        return this.defaultMaxRetries;
    }

    private long nextBackoffMillis(int attempt) {
        long exp = baseDelayMillis * (1L << (Math.max(0, attempt - 1))); // exponential
        long capped = Math.min(exp, maxBackoffMillis);
        double jitter = 1.0 + (RANDOM.nextDouble() * 2 - 1) * jitterFactor;
        long result = (long) (capped * jitter);
        return Math.max(0L, result);
    }

    private void persistAuditAsync(CoproRetryErrorAuditTable auditCopy, ActionExecutionAudit action) {
        dbExecutor.submit(() -> {
            try {
                if (auditCopy.getCreatedOn() == null) {
                    auditCopy.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
                }

                // Truncate long fields to avoid DB/IO pressure
                if (auditCopy.getRequest() != null && auditCopy.getRequest().length() > MAX_BODY_LENGTH) {
                    auditCopy.setRequest(auditCopy.getRequest().substring(0, MAX_BODY_LENGTH));
                }
                if (auditCopy.getResponse() != null && auditCopy.getResponse().length() > MAX_BODY_LENGTH) {
                    auditCopy.setResponse(auditCopy.getResponse().substring(0, MAX_BODY_LENGTH));
                }

                // Use the repo method you implemented (bindBean variant).
                // NOTE: method name should match your HandymanRepo interface.
                handymanRepo.insertAuditToDb(auditCopy, action);

            } catch (Exception ex) {
                log.error("Failed to persist audit: {}", ExceptionUtil.toString(ex));
                HandymanException.insertException("Error inserting retry audit", new HandymanException(ex), action);
            }
        });
    }

    /**
     * Defensive full copy of the audit object. Copies Timestamp instances to avoid shared mutable TS objects.
     * Intentionally not copying 'attempt' so the per-attempt value can be set in populateAudit.
     */
    private CoproRetryErrorAuditTable cloneFullAuditForAttemptIfNeeded(CoproRetryErrorAuditTable original) {
        if (original == null) {
            return new CoproRetryErrorAuditTable();
        }

        // defensive copy of mutable Timestamp fields
        Timestamp createdOnCopy = null;
        if (original.getCreatedOn() != null) {
            createdOnCopy = new Timestamp(original.getCreatedOn().getTime());
        }
        Timestamp lastUpdatedOnCopy = null;
        if (original.getLastUpdatedOn() != null) {
            lastUpdatedOnCopy = new Timestamp(original.getLastUpdatedOn().getTime());
        }

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
            // For response content consider using a hash or truncated content; using toString() may not include body.
            retryAudit.setResponse(encryptRequestResponse(response.toString(), action));
        } else if (e != null) {
            String message = e.getMessage() != null ? e.getMessage() : ExceptionUtil.toString(e);
            retryAudit.setMessage(message);
            retryAudit.setResponse(encryptRequestResponse(ExceptionUtil.toString(e), action));
        }
    }

    private String encryptRequestResponse(String data, ActionExecutionAudit action) {
        if (data == null) return null;
        String encrypt = action != null && action.getContext() != null ? action.getContext().get(ENCRYPT_REQUEST_RESPONSE) : null;
        if ("true".equalsIgnoreCase(encrypt)) {
            return SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(data, "AES256", "COPRO_REQUEST");
        }
        return data;
    }

    /**
     * Call to shut down the scheduler/DB executor if this service created them.
     */
    @PreDestroy
    public void shutdown() {
        if (!ownExecutors) {
            log.info("Not shutting down executors; lifecycle managed externally.");
            return;
        }

        shutdownExecutor("scheduler", scheduler, 5, 5);
        shutdownExecutor("dbExecutor", dbExecutor, 5, 5);
    }

    private void shutdownExecutor(String name, ExecutorService executor, long quietSeconds, long timeoutSeconds) {
        if (executor == null || executor.isShutdown()) return;

        try {
            log.info("Shutting down {} gracefully...", name);
            executor.shutdown();
            if (!executor.awaitTermination(quietSeconds, TimeUnit.SECONDS)) {
                log.warn("{} did not terminate in {}s; forcing shutdownNow()", name, quietSeconds);
                var dropped = executor.shutdownNow();
                if (!dropped.isEmpty()) {
                    log.warn("{}: {} tasks dropped on shutdown", name, dropped.size());
                }
                executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
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
