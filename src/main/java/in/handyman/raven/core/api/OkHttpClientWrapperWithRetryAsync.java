package in.handyman.raven.core.api;

import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.*;

public class OkHttpClientWrapperWithRetryAsync {

    private final OkHttpClient client;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService dbExecutor;
    private final int maxRetries;
    private final long baseDelayMillis;
    private final double jitterFactor;
    private final long maxBackoffMillis;

    public OkHttpClientWrapperWithRetryAsync(
            OkHttpClient client,
            HandymanRepo handymanRepo,
            ScheduledExecutorService scheduler,
            ExecutorService dbExecutor,
            int maxRetries,
            long baseDelayMillis,
            double jitterFactor,
            long maxBackoffMillis
    ) {
        this.client = client;
        this.scheduler = scheduler;
        this.dbExecutor = dbExecutor;
        this.maxRetries = maxRetries;
        this.baseDelayMillis = baseDelayMillis;
        this.jitterFactor = jitterFactor;
        this.maxBackoffMillis = maxBackoffMillis;
    }

    public CompletableFuture<Response> executeWithRetryAsync(
            Request request,
            String requestPayload,
            CoproRetryErrorAuditTable baseAudit
    ) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        attemptRequest(0, request, requestPayload, baseAudit, future);
        return future;
    }

    private void attemptRequest(int attempt, Request request, String requestPayload,
                                CoproRetryErrorAuditTable baseAudit,
                                CompletableFuture<Response> future) {
        long startTime = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                logAndAudit(attempt, request, requestPayload, null, e.getMessage(), false, startTime);
                if (attempt < maxRetries) {
                    long delay = computeBackoffWithJitter(attempt);
                    scheduler.schedule(() -> attemptRequest(attempt + 1, request, requestPayload, baseAudit, future),
                            delay, TimeUnit.MILLISECONDS);
                } else {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String body = response.body() != null ? response.peekBody(10_000).string() : "";
                    logAndAudit(attempt, request, requestPayload, body, null, response.isSuccessful(), startTime);

                    if (response.isSuccessful()) {
                        future.complete(response);
                    } else if (attempt < maxRetries) {
                        long delay = computeBackoffWithJitter(attempt);
                        scheduler.schedule(() -> attemptRequest(attempt + 1, request, requestPayload, baseAudit, future),
                                delay, TimeUnit.MILLISECONDS);
                    } else {
                        future.complete(response);
                    }
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            private void logAndAudit(int attempt, Request req, String payload,
                                     String responseBody, String errorMessage, boolean success, long startTime) {
//                CoproRetryErrorAuditTable audit = baseAudit.tobuilder().
//                        .attemptNo(attempt + 1)
//                        .endpoint(req.url().toString())
//                        .requestPayload(payload)
//                        .responsePayload(responseBody != null ? responseBody : errorMessage)
//                        .statusCode(success ? 200 : 500)
//                        .success(success)
//                        .lastUpdatedOn(new Timestamp(System.currentTimeMillis()))
//                        .timeTakenMs(System.currentTimeMillis() - startTime)
//                        .build();
//
//                dbExecutor.submit(() -> insertAuditToDb(audit, dbExecutor));
            }

            private long computeBackoffWithJitter(int attempt) {
                long delay = (long) (baseDelayMillis * Math.pow(2, attempt));
                if (jitterFactor > 0) {
                    double jitter = ThreadLocalRandom.current().nextDouble(1 - jitterFactor, 1 + jitterFactor);
                    delay = (long) (delay * jitter);
                }
                return Math.min(delay, maxBackoffMillis);
            }
        });
    }
}
