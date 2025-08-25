package in.handyman.raven.core.api;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.lambda.doa.audit.PipelineApiAudit;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class HandymanOkHttpClient {
    private final OkHttpClient httpClient;
    private final boolean logRequestBody;
    private final boolean logResponseBody;
    private final int maxBodyLogSize;
    private final int timeoutSeconds;

    public HandymanOkHttpClient() {
        this(true, true, 10000, 30);
    }
    public HandymanOkHttpClient(boolean logRequestBody,
                                boolean logResponseBody, int maxBodyLogSize, int timeoutSeconds) {
        this.logRequestBody = logRequestBody;
        this.logResponseBody = logResponseBody;
        this.maxBodyLogSize = maxBodyLogSize;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(this.timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(this.timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(this.timeoutSeconds, TimeUnit.SECONDS)
                .build();

    }
    // Synchronous request execution
    public Response execute(Request request, ApiRequestContext context) throws IOException {
        return executeInternal(request, context);
    }
    // Asynchronous request execution
    public CompletableFuture<Response> executeAsync(Request request, ApiRequestContext context) {
        CompletableFuture<Response> future = new CompletableFuture<>();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    auditFailure(request, context, e);
                    future.completeExceptionally(e);
                } catch (Exception auditException) {
                    future.completeExceptionally(auditException);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    auditSuccess(request, response, context);
                    future.complete(response);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        });

        return future;
    }
    // Execute with retry mechanism
    public Response executeWithRetry(Request request, ApiRequestContext context, int maxRetries) throws IOException {
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            context.incrementRetryCount();

            try {
                return executeInternal(request, context);
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    auditRetry(request, context, e, attempt + 1);
                    // Optional: add delay between retries
                    try {
                        Thread.sleep(1000 * (attempt + 1)); // Progressive delay
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        // Final failure after all retries
        auditFailure(request, context, lastException);
        throw lastException;
    }

    private Response executeInternal(Request request, ApiRequestContext context) throws IOException {
        long startTime = System.currentTimeMillis();

        try {
            Response response = httpClient.newCall(request).execute();
            long duration = (System.currentTimeMillis() - startTime) / 1000; // Convert to seconds

            auditSuccess(request, response, context, duration);
            return response;

        } catch (IOException e) {
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            auditFailure(request, context, e, duration);
            throw e;
        }
    }

    private void auditSuccess(Request request, Response response, ApiRequestContext context) {
        auditSuccess(request, response, context, null);
    }

    private void auditSuccess(Request request, Response response, ApiRequestContext context, Long duration) {
        try {
            String responseBodyString = "";
            if (logResponseBody && response.body() != null) {
                responseBodyString = getResponseBodyString(response);
            }
            PipelineApiAudit audit = PipelineApiAudit.builder()
                    .requestId(context.getRequestId())
                    .tenantId(context.getTenantId())
                    .rootPipelineId(context.getRootPipelineId())
                    .batchId(context.getBatchId())
                    .actionId(context.getActionId())
                    .endpoint(request.url().toString())
                    .originId(context.getOriginId())
                    .paperNo(context.getPaperNo())
                    .request(logRequestBody ? getRequestBodyString(request) : null)
                    .response(responseBodyString)
                    .stage(context.getStage())
                    .status("SUCCESS")
                    .message("HTTP " + response.code() + " - " + response.message())
                    .retryCount(context.getRetryCount())
                    .build();

            if (duration != null) {
                audit.setTimeTakenSec(duration);
            }

            HandymanActorSystemAccess.insertApiAudit(audit);

        } catch (Exception e) {
            // Log audit failure but don't throw to avoid breaking the main flow
            System.err.println("Failed to audit successful request: " + e.getMessage());
        }
    }

    private void auditFailure(Request request, ApiRequestContext context, IOException exception) {
        auditFailure(request, context, exception, null);
    }

    private void auditFailure(Request request, ApiRequestContext context, IOException exception, Long duration) {
        try {
            PipelineApiAudit audit = PipelineApiAudit.builder()
                    .requestId(context.getRequestId())
                    .tenantId(context.getTenantId())
                    .rootPipelineId(context.getRootPipelineId())
                    .batchId(context.getBatchId())
                    .actionId(context.getActionId())
                    .endpoint(request.url().toString())
                    .originId(context.getOriginId())
                    .paperNo(context.getPaperNo())
                    .request(logRequestBody ? getRequestBodyString(request) : null)
                    .response(null)
                    .stage(context.getStage())
                    .status("FAILURE")
                    .message("Error: " + exception.getMessage())
                    .retryCount(context.getRetryCount())
                    .build();

            if (duration != null) {
                audit.setTimeTakenSec(duration);
            }

            HandymanActorSystemAccess.insertApiAudit(audit);

        } catch (Exception e) {
            System.err.println("Failed to audit failed request: " + e.getMessage());
        }
    }

    private void auditRetry(Request request, ApiRequestContext context, IOException exception, int nextAttempt) {
        try {
            PipelineApiAudit audit = PipelineApiAudit.builder()
                    .requestId(context.getRequestId())
                    .tenantId(context.getTenantId())
                    .rootPipelineId(context.getRootPipelineId())
                    .batchId(context.getBatchId())
                    .actionId(context.getActionId())
                    .endpoint(request.url().toString())
                    .originId(context.getOriginId())
                    .paperNo(context.getPaperNo())
                    .request(logRequestBody ? getRequestBodyString(request) : null)
                    .response(null)
                    .stage(context.getStage())
                    .status("RETRY")
                    .message("Attempt " + context.getRetryCount() + " failed: " + exception.getMessage() +
                            ". Will retry (attempt " + nextAttempt + ")")
                    .retryCount(context.getRetryCount())
                    .build();

            HandymanActorSystemAccess.insertApiAudit(audit);

        } catch (Exception e) {
            System.err.println("Failed to audit retry: " + e.getMessage());
        }
    }

    private String getRequestBodyString(Request request) {
        try {
            if (request.body() == null) {
                return null;
            }

            RequestBody body = request.body();
            if (body.contentLength() > maxBodyLogSize) {
                return "[Large request body: " + body.contentLength() + " bytes]";
            }

            okio.Buffer buffer = new okio.Buffer();
            body.writeTo(buffer);
            String bodyString = buffer.readUtf8();

            return bodyString.length() > maxBodyLogSize ?
                    bodyString.substring(0, maxBodyLogSize) + "[Truncated...]" : bodyString;

        } catch (Exception e) {
            return "[Error reading request body: " + e.getMessage() + "]";
        }
    }

    private String getResponseBodyString(Response response) {
        try {
            if (response.body() == null) {
                return null;
            }
            String bodyString = response.peekBody(Long.MAX_VALUE).string();


            return bodyString;

        } catch (Exception e) {
            return "[Error reading response body: " + e.getMessage() + "]";
        }
    }

    public void shutdown() {
        httpClient.dispatcher().executorService().shutdown();
    }
}
