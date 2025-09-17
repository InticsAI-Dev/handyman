package in.handyman.raven.core.monitoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ConnectionMonitoringService {

    @Autowired
    private ThreadConnectionTracker threadTracker;

    @Autowired
    private TrackingDataSource trackingDataSource;

    @Autowired
    private TrackingHttpInterceptor trackingHttpInterceptor;

    @Scheduled(fixedDelay = 60000)
    public void monitor() {
        System.out.println("Active JDBC Connections:");
        trackingDataSource.getActiveConnections().forEach((conn, thread) ->
                System.out.println("Thread: " + thread + ", Connection: " + conn));

        System.out.println("Active HTTP Requests:");
        trackingHttpInterceptor.getActiveRequests().forEach((thread, uri) ->
                System.out.println("Thread: " + thread + ", URI: " + uri));

        threadTracker.trackThreads(); // logs suspicious threads
    }
}
