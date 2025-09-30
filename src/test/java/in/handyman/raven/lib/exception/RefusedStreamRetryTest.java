package in.handyman.raven.lib.exception;


import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.access.repo.HandymanRepoImpl;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Test client to validate retry logic against the Python error simulation server
 */
public class RefusedStreamRetryTest {
    private static final Logger log = LoggerFactory.getLogger(RefusedStreamRetryTest.class);
    private static final String BASE_URL = "http://localhost:8080";
    private static RefusedStreamRetryTest testClient;

    public static void main(String[] args) {
        RefusedStreamRetryTest testClient = new RefusedStreamRetryTest();

        System.out.println("=".repeat(60));
        System.out.println("Starting Copro Retry Implementation Tests");
        System.out.println("=".repeat(60));

        // Run all test scenarios
        testClient.testRefusedStream();
        testClient.testProtocolError();
        testClient.testEnhanceYourCalm();
        testClient.testInternalError();
        testClient.testSuccessAfterRetries();
        testClient.testIntermittentFailures();
        testClient.testTimeout();
        testClient.testImmediateSuccess();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("All tests completed!");
        System.out.println("=".repeat(60));
    }

    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .protocols(java.util.Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();
    }

    private ActionExecutionAudit createMockAudit(int maxRetries, int delaySeconds) {
        ActionExecutionAudit audit = new ActionExecutionAudit();
        Map<String, String> context = new HashMap<>();
        context.put("copro.retry.attempt", String.valueOf(maxRetries));
        context.put("copro.retry.delay.inSeconds", String.valueOf(delaySeconds));
        context.put("copro.retry.calm.delay.inSeconds", "10");
        audit.setContext(context);
        return audit;
    }

    private void testRefusedStream() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 1: REFUSED_STREAM Error");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=refused_stream";
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{\"test\": \"data\"}",
                            MediaType.parse("application/json")))
                    .addHeader("X-Client-ID", "test-client-1")
                    .addHeader("X-Session-ID", UUID.randomUUID().toString())
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(3, 2);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "{\"test\": \"data\"}", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Response Body: " + response.body().string());
            System.out.println("✅ Total Duration: " + duration + "ms");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Exception caught (expected for retry exhaustion): "
                    + e.getMessage());
        }
    }

    private void testProtocolError() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 2: PROTOCOL_ERROR");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=protocol_error";
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{\"test\": \"protocol\"}",
                            MediaType.parse("application/json")))
                    .addHeader("X-Client-ID", "test-client-2")
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(3, 2);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "{\"test\": \"protocol\"}", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Total Duration: " + duration + "ms");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Exception: " + e.getMessage());
        }
    }

    private void testEnhanceYourCalm() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 3: ENHANCE_YOUR_CALM (Rate Limiting)");
        System.out.println("-".repeat(60));

        try {
            // Reset server state first
            resetServerState();

            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String clientId = "rate-limit-test-" + System.currentTimeMillis();

            // Send multiple requests to trigger rate limiting
            for (int i = 1; i <= 8; i++) {
                System.out.println("\nRequest #" + i);

                String url = BASE_URL + "/api/test?mode=enhance_your_calm";
                Request request = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create("{\"request\": " + i + "}",
                                MediaType.parse("application/json")))
                        .addHeader("X-Client-ID", clientId)
                        .build();

                CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
                ActionExecutionAudit actionAudit = createMockAudit(5, 2);

                try {
                    Response response = retryService.callCoproApiWithRetry(
                            request, "{\"request\": " + i + "}", auditTable, actionAudit);
                    System.out.println("  ✅ Response: " + response.code() +
                            " | Remaining: " + response.header("X-Rate-Limit-Remaining", "N/A"));
                    response.close();
                } catch (IOException e) {
                    System.out.println("  ❌ Failed: " + e.getMessage());
                }

                // Small delay between requests
                Thread.sleep(500);
            }

        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
    }

    private void testInternalError() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 4: INTERNAL_ERROR");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=internal_error";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-Client-ID", "test-client-4")
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(3, 2);

            Response response = retryService.callCoproApiWithRetry(
                    request, "", auditTable, actionAudit);
            System.out.println("✅ Response Code: " + response.code());
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Exception: " + e.getMessage());
        }
    }

    private void testSuccessAfterRetries() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 5: Success After N Retries");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String sessionId = UUID.randomUUID().toString();
            String url = BASE_URL + "/api/test?mode=success_after_n&attempts=3";
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{\"test\": \"retry-success\"}",
                            MediaType.parse("application/json")))
                    .addHeader("X-Session-ID", sessionId)
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(5, 2);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "{\"test\": \"retry-success\"}", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Success after retries!");
            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Response Body: " + response.body().string());
            System.out.println("✅ Total Duration: " + duration + "ms");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Exception: " + e.getMessage());
        }
    }

    private void testIntermittentFailures() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 6: Intermittent Failures (70% failure rate)");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=intermittent&failure_rate=0.7";
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{\"test\": \"intermittent\"}",
                            MediaType.parse("application/json")))
                    .addHeader("X-Client-ID", "test-client-6")
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(5, 1);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "{\"test\": \"intermittent\"}", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Eventually succeeded!");
            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Total Duration: " + duration + "ms");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Failed after all retries: " + e.getMessage());
        }
    }

    private void testTimeout() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 7: Timeout Scenario");
        System.out.println("-".repeat(60));
        System.out.println("⚠️  This test will take ~40+ seconds...");

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=timeout&delay=35";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-Client-ID", "test-client-7")
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(2, 2);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Total Duration: " + duration + "ms");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Timeout exception (expected): " + e.getMessage());
        }
    }

    private void testImmediateSuccess() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("TEST 8: Immediate Success (No Retry Needed)");
        System.out.println("-".repeat(60));

        try {
            OkHttpClient client = createHttpClient();
            CoproRetryService retryService = new CoproRetryService(
                    createMockRepo(), client);

            String url = BASE_URL + "/api/test?mode=success";
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create("{\"test\": \"immediate-success\"}",
                            MediaType.parse("application/json")))
                    .addHeader("X-Client-ID", "test-client-8")
                    .build();

            CoproRetryErrorAuditTable auditTable = new CoproRetryErrorAuditTable();
            ActionExecutionAudit actionAudit = createMockAudit(3, 2);

            long startTime = System.currentTimeMillis();
            Response response = retryService.callCoproApiWithRetry(
                    request, "{\"test\": \"immediate-success\"}", auditTable, actionAudit);
            long duration = System.currentTimeMillis() - startTime;

            System.out.println("✅ Immediate success (no retries)!");
            System.out.println("✅ Response Code: " + response.code());
            System.out.println("✅ Response Body: " + response.body().string());
            System.out.println("✅ Duration: " + duration + "ms (should be < 1000ms)");
            response.close();

        } catch (IOException e) {
            System.out.println("❌ Unexpected exception: " + e.getMessage());
        }
    }

    private void resetServerState() throws IOException {
        OkHttpClient client = createHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/reset")
                .post(RequestBody.create("", null))
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("🔄 Server state reset: " + response.code());
        }
    }

    private HandymanRepo createMockRepo() {
        // Return a mock repository that logs instead of actually persisting
        return new HandymanRepoImpl();
    }
}