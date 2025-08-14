package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.lambda.access.repo.HandymanRepo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoproRetryServiceAsyncTest {

    private MockWebServer mockWebServer;
    private HandymanRepo handymanRepo;
    private OkHttpClient okHttpClient;
    private ScheduledExecutorService scheduler;
    private ExecutorService dbExecutor;
    private CoproRetryServiceAsync service;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        handymanRepo = Mockito.mock(HandymanRepo.class);

        okHttpClient = new OkHttpClient.Builder().build();

        // small executors for testing
        scheduler = Executors.newScheduledThreadPool(2);
        dbExecutor = Executors.newFixedThreadPool(2);

        // defaultMaxRetries = 2, baseDelayMillis small for faster tests
        service = new CoproRetryServiceAsync(
                handymanRepo,
                okHttpClient
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        // Shut down executors and wait for termination before proceeding
        shutdownExecutor(scheduler, "scheduler");
        shutdownExecutor(dbExecutor, "dbExecutor");

        // Shut down MockWebServer after the test completes
        mockWebServer.shutdown();
    }

    /**
     * Helper to build a basic audit object (minimal fields). Adjust if your builder requires other non-null fields.
     */
    private CoproRetryErrorAuditTable makeAuditInput() {
        return CoproRetryErrorAuditTable.builder()
                .originId("orig-1")
                .groupId(1)
                .tenantId(10L)
                .processId(100L)
                .filePath("/tmp/x")
                .paperNo(1)
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .rootPipelineId(999L)
                .batchId("batch-1")
                .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                .endpoint("http://example")
                .build();
    }

    private void shutdownExecutor(ExecutorService executor, String name) throws InterruptedException {
        if (executor == null || executor.isShutdown()) return;

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            // Force shutdown if termination is not complete in time
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void successOnFirstTry_shouldCompleteSuccessfullyAndPersistAudit() throws Exception {
        String body = "{\"outputs\":[{\"data\":[{\"k\":\"v\"}]}],\"modelName\":\"m\",\"modelVersion\":\"v1\"}";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        URL url = mockWebServer.url("/test").url();
        Request request = new Request.Builder().url(url).get().build();

        CompletableFuture<CoproRetryServiceAsync.CoproResponse> future =
                service.callCoproApiWithRetryAsync(request, "{\"req\":1}", makeAuditInput(), null);

        CoproRetryServiceAsync.CoproResponse resp = future.get(5, TimeUnit.SECONDS);
        assertNotNull(resp);
        assertEquals(200, resp.getHttpCode());
        assertNotNull(resp.getBodyAsString());
        assertTrue(resp.getBodyAsString().contains("\"modelName\""));

        // Verify handymanRepo was called at least once to insert audit (attempt persisted)
        // Because persistAuditAsync runs asynchronously we wait a bit for invocation
        verify(handymanRepo, timeout(2000).atLeastOnce()).insertAuditToDb(any(CoproRetryErrorAuditTable.class), any());
    }

    @Test
    void retryThenSuccess_shouldRetryAndSucceed_andPersistMultipleAudits() throws Exception {
        // First response: 500 (causes retry)
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("server-error"));
        // Second response: 200 success
        String body = "{\"outputs\":[{\"data\":[{\"k\":\"v\"}]}],\"modelName\":\"m\",\"modelVersion\":\"v1\"}";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(body));

        URL url = mockWebServer.url("/test2").url();
        Request request = new Request.Builder().url(url).get().build();

        // Use defaultMaxRetries=2 (from constructor) so one retry will be attempted
        CompletableFuture<CoproRetryServiceAsync.CoproResponse> future =
                service.callCoproApiWithRetryAsync(request, "{\"req\":2}", makeAuditInput(), null);

        CoproRetryServiceAsync.CoproResponse resp = future.get(10, TimeUnit.SECONDS);
        assertNotNull(resp);
        assertEquals(200, resp.getHttpCode());

        // Expect at least two audit inserts (one for failed attempt, one for success)
        verify(handymanRepo, timeout(5000).atLeast(2)).insertAuditToDb(any(CoproRetryErrorAuditTable.class), any());
    }
}
