package in.handyman.raven.core.monitoring;

import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingHttpInterceptor implements ClientHttpRequestInterceptor {

    private final ConcurrentHashMap<String, String> requestTracker = new ConcurrentHashMap<>();

    @Override
    public ClientHttpResponse intercept(org.springframework.http.HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String threadName = Thread.currentThread().getName();
        String uri = request.getURI().toString();
        requestTracker.put(threadName, uri);
        try {
            return execution.execute(request, body);
        } finally {
            requestTracker.remove(threadName);
        }
    }

    public ConcurrentHashMap<String, String> getActiveRequests() {
        return requestTracker;
    }
}
