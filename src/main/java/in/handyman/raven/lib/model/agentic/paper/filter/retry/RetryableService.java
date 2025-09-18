package in.handyman.raven.lib.model.agentic.paper.filter.retry;

import java.util.concurrent.CompletableFuture;

public interface RetryableService<RequestT, ResponseT> {

    /**
     * Sends the request asynchronously with retry logic.
     *
     * @param request the request object
     * @param context optional context or metadata
     * @return a CompletableFuture that completes with the response or an exception
     */
    CompletableFuture<ResponseT> sendWithRetry(RequestT request, ServiceContext context);

}
