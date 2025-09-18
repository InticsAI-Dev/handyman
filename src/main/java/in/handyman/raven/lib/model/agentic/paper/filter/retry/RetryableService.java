package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import java.util.concurrent.CompletableFuture;

/**
 * Generic retryable service interface.
 * Now returns HttpResult (safe, serializable response wrapper).
 */
public interface RetryableService {
    CompletableFuture<HttpResult> sendWithRetry(okhttp3.Request request, ServiceContext context);
}
