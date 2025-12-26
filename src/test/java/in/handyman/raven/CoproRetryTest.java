package in.handyman.raven;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.access.repo.HandymanRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.lib.model.retry.CoproRetryService;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CoproRetryTest {

    private CoproRetryService coproRetryService;
    private HandymanRepo handymanRepo;
    private OkHttpClient httpClient;
    private Logger log;
    private ActionExecutionAudit actionAudit;

    @BeforeEach
    void setup() {
        handymanRepo = mock(HandymanRepo.class);
        httpClient = mock(OkHttpClient.class);
        log = mock(Logger.class);

        coproRetryService = new CoproRetryService(handymanRepo, httpClient, log);

        actionAudit = new ActionExecutionAudit();
        Map<String, String> ctx = new HashMap<>();
        ctx.put("encrypt.request.response", "false");
        actionAudit.setContext(ctx);
    }

    @Test
    void testPopulateAudit_successResponse() throws Exception {

        String responseJson =
                "{\"originId\":\"ORIGIN-28\",\"batchId\":\"BATCH-28_0\",\"processId\":5325,\"groupId\":28,\"tenantId\":1,\"rootPipelineId\":5325,\"process\":\"DATA_EXTRACTION\",\"actionId\":38668,\"status\":\"SUCCESS\",\"inferResponse\":\"BCBS & NYC HEALTHL:2068031\\n#\\n2\\n08-02-25:12:31PM:\\nJAMAICA HOSPITAL MEDICAL CENTER\\n8900 Van Wyck Expressway\\nJamaica, NY 11418\\nPHONE:718-206-6022 BAX 718-206-6031\\nFax Message. from Emergency Department\\nDate: ffocas\\nDeliver to: BLUE CROSS/ BLUE SHIELD\\nFax:\\n(800,241-5308\\nde\\nE\\nNumber of Pages (include cover sheet):\\n-\\nFrom; JHMC/ ER DEPARTMENT\\nFOR YOUR INFORMATION\\nORIGINAL TO FOLLOW\\nAS WE DISCUSSED\\nPLEASE CALL TO DISCUSS\\nPLEASE REVIEW & COMMENT\\nORIGINAL WILL NOT\\nFOLLOW\\nAS REQUESTED\\nX OTHER: ER/ ADMIT\\nNOTIFICATION And\\nAUTHORIZATION\\nPrivileged and Confidential Information\\n\\n5\\nThis message is intended only for the use ofi individual or entity to whom or which it is addressed and may contain\\ninformation Chat is privileged, confidential and sxempt from disclosure under applicable law. lthe reader of the\\nmessage is not intended recipient, or the intended recipient, you are hereby notified that any dissemination,\\ndistribution, or copying oft this information is strictly prohibited. lfyou have received this communication in error,\\nplease notify the sender immediately by telephone and retum the original message to the sender at the above\\naddress via u.s, Pastal Service.\\nFA12-RCVD.802ZG2S 12:41:28 PM ET * KBAIEAISREEBHESOATIISAr-CNPAHEA-AAAFREgANON *DUR:00-48 m-SS\",\"modelName\":\"XENON\",\"detail\":\"\",\"durationTime\":5.861518,\"metricsData\":{\"beforeMetricsData\":{\"cpuUsage\":33.8,\"totalCores\":8,\"availableCores\":4.0,\"usedCores\":1.35,\"coreUtilizationPercent\":33.8,\"ramUsage\":74.4,\"ramUsedMb\":\"16502.61 MB\",\"ramTotalMb\":\"23880.92 MB\",\"ramAvailableMb\":\"6109.71 MB\",\"diskUsage\":25.6,\"diskTotalGb\":\"449.51 GB\",\"diskFreeGb\":\"317.34 GB\",\"diskTotalMb\":\"460295.23 MB\",\"diskFreeMb\":\"324955.47 MB\",\"gpus\":[],\"source\":\"psutil\"},\"afterMetricsData\":{\"cpuUsage\":9.0,\"totalCores\":8,\"availableCores\":4.0,\"usedCores\":0.36,\"coreUtilizationPercent\":9.0,\"ramUsage\":78.5,\"ramUsedMb\":\"17474.09 MB\",\"ramTotalMb\":\"23880.92 MB\",\"ramAvailableMb\":\"5141.78 MB\",\"diskUsage\":25.6,\"diskTotalGb\":\"449.51 GB\",\"diskFreeGb\":\"317.34 GB\",\"diskTotalMb\":\"460295.23 MB\",\"diskFreeMb\":\"324951.85 MB\",\"gpus\":[],\"source\":\"psutil\"}},\"statusCode\":200,\"requestId\":\"4773844c-b919-4d98-8252-435a866bf619\"}";

        Response response = new Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_2)
                .request(new Request.Builder().url("http://test.com").build())
                .body(ResponseBody.create(MediaType.get("application/json"), responseJson))
                .build();

        CoproRetryErrorAuditTable audit = new CoproRetryErrorAuditTable();
        audit.setStage("TEST_STAGE");

        Method method = CoproRetryService.class.getDeclaredMethod(
                "populateAudit",
                int.class,
                CoproRetryErrorAuditTable.class,
                String.class,
                Response.class,
                Exception.class,
                ActionExecutionAudit.class
        );
        method.setAccessible(true);

//        method.invoke(
//                coproRetryService,
//                1,                       // attempt
//                audit,
//                "{requestBody}",         // request
//                response,
//                null,
//                actionAudit
//        );
//        int attempt,
//        CoproRetryErrorAuditTable retryAudit,
//        String requestBody,
//        Response response,
//        Exception e,
//        ActionExecutionAudit action
//        coproRetryService.populateAudit(1, audit, "{requestBody}", response, null, actionAudit);

        // Verify populated fields
        assertEquals("200 -> OK", audit.getMessage());
        assertNotNull(audit.getResponse());
        assertEquals(200, audit.getCoproStatusCode());
        assertEquals("none", audit.getCoproLog());
        assertEquals("\"COMP-123\"", audit.getComputationDetails());
        assertEquals("OK", audit.getCoproDetails());
        assertEquals("REQ-789", audit.getRequestId());
    }

    @Test
    public void testPopulateAudit_successfulJson() throws Exception {

        // Simulated Copro JSON Response (must match expected structure)
        String responseJson =
                "{\n" +
                        "  \"outputs\": [\n" +
                        "    {\n" +
                        "      \"data\": [\n" +
                        "        \"{  \\\"statusCode\\\": 200, \\\"errorMessage\\\": \\\"none\\\", \\\"detail\\\": \\\"OK\\\", \\\"requestId\\\": \\\"REQ-789\\\" }\"\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

        Response response = new Response.Builder()
                .code(200)
                .message("OK")
                .request(new Request.Builder().url("http://test.com").build())
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.create(
                        MediaType.parse("application/json"),
                        responseJson
                ))
                .build();

        CoproRetryErrorAuditTable audit = new CoproRetryErrorAuditTable();
        audit.setStage("UNIT_TEST_STAGE");

        // Use reflection to call private method
        Method m = CoproRetryService.class.getDeclaredMethod(
                "populateAudit",
                int.class,
                CoproRetryErrorAuditTable.class,
                String.class,
                Response.class,
                Exception.class,
                ActionExecutionAudit.class
        );
        m.setAccessible(true);

        m.invoke(
                coproRetryService,
                1,                     // attempt
                audit,
                "{requestBody}",       // request
                response,
                null,                  // no exception
                actionAudit
        );

        // VALIDATION
        assertEquals("200 -> OK", audit.getMessage());
        assertNotNull(audit.getResponse());
        assertEquals("\"COMP-123\"", audit.getComputationDetails());
        assertEquals("none", audit.getCoproLog());
        assertEquals("OK", audit.getCoproDetails());
        assertEquals("REQ-789", audit.getRequestId());
        assertEquals(200, audit.getCoproStatusCode());
        assertEquals(1, audit.getAttempt());
        assertEquals("{requestBody}", audit.getRequest());
    }
}
