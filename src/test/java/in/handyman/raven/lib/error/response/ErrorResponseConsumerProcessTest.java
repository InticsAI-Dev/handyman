package in.handyman.raven.lib.error.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AlchemyKvpResponseAction;
import in.handyman.raven.lib.ErrorResponseAction;
import in.handyman.raven.lib.alchemy.common.AlchemyApiPayload;
import in.handyman.raven.lib.alchemy.error.ErrorResponseConsumerProcess;
import in.handyman.raven.lib.alchemy.error.ErrorResponseInputTable;
import in.handyman.raven.lib.alchemy.error.ErrorResponseOutputTable;
import in.handyman.raven.lib.model.AlchemyKvpResponse;
import in.handyman.raven.lib.model.ErrorResponse;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
class ErrorResponseConsumerProcessTest {

    public static final String URL = "http://localhost/alchemy/api/v1/";
    private ErrorResponseConsumerProcess consumerProcess;
    private Logger mockLogger;
    private Marker mockMarker;
    private ActionExecutionAudit mockAudit;

    private final String mockAuthToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzUyNjI0ODQ3LCJpYXQiOjE3NTI1Mzg0NDcsImVtYWlsIjoiU21hcnRJbnRha2UifQ.4SUmTehllyyMOBfUaf5R34NUmyWMQhgS9QiKYVqEPfw";
    private final Long mockTenantId = 1L;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        mockMarker = mock(Marker.class);
        mockAudit = mock(ActionExecutionAudit.class);

        Map<String, String> context = new HashMap<>();
        context.put("alchemyAuth.tenantId", mockTenantId.toString());
        context.put("alchemyAuth.token", mockAuthToken);

        when(mockAudit.getContext()).thenReturn(context);

        consumerProcess = new ErrorResponseConsumerProcess(mockLogger, mockMarker, mockAudit);
    }

    @Test
    void testProcess_successfulResponse() throws Exception {
        // Arrange
        URL mockUrl = new URL("http://localhost:8189/alchemy/api/v1/");
        ErrorResponseInputTable input = buildMockInput();

        AlchemyApiPayload payload = new AlchemyApiPayload();
        payload.setSuccess(true);
        payload.setPayload(new ObjectMapper().readTree("{\"key\":\"value\"}"));

        ResponseBody mockBody = ResponseBody.create(
                new ObjectMapper().writeValueAsString(payload),
                MediaType.get("application/json")
        );

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockUrl + "product-outbound/error-details/create").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(mockBody)
                .build();

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        // Inject mock client
        TestUtils.setPrivateField(consumerProcess, "httpClient", mockClient);

        // Act
        List<ErrorResponseOutputTable> result = consumerProcess.process(mockUrl, input);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ConsumerProcessApiStatus.COMPLETED.getStatusDescription(), result.get(0).getStatus());
        assertTrue(result.get(0).getMessage().contains("completed"));
    }

    @Test
    void testProcess_failedResponse() throws Exception {
        // Arrange
        URL mockUrl = new URL("http://localhost/");
        ErrorResponseInputTable input = buildMockInput();

        Response mockResponse = new Response.Builder()
                .request(new Request.Builder().url(mockUrl + "product-outbound/error-details/create").build())
                .protocol(Protocol.HTTP_1_1)
                .code(500)
                .message("Internal Server Error")
                .body(ResponseBody.create("", MediaType.get("application/json")))
                .build();

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        TestUtils.setPrivateField(consumerProcess, "httpClient", mockClient);

        // Act
        List<ErrorResponseOutputTable> result = consumerProcess.process(mockUrl, input);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ConsumerProcessApiStatus.FAILED.getStatusDescription(), result.get(0).getStatus());
        assertTrue(result.get(0).getMessage().contains("failed"));
    }

    @Test
    void testProcess_exceptionHandling() throws Exception {
        // Arrange
        URL mockUrl = new URL(URL);
        ErrorResponseInputTable input = buildMockInput();

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new RuntimeException("Connection error"));

        TestUtils.setPrivateField(consumerProcess, "httpClient", mockClient);

        // Act
        List<ErrorResponseOutputTable> result = consumerProcess.process(mockUrl, input);

        // Assert
        assertTrue(result.isEmpty());
//        verify(mockLogger).error(eq(mockMarker), contains("Exception occurred"), any());
    }

    // Helper to build input
    @NotNull
    private ErrorResponseInputTable buildMockInput() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = "{\"error\":\"test error\"}";
        JsonNode errorJsonNode = objectMapper.readTree(content);

        ErrorResponseInputTable input = new ErrorResponseInputTable();
        input.setProcessId(1);
        input.setGroupId(1L);
        input.setOriginId("origin-1");
        input.setRootPipelineId(100L);
        input.setFeature("feature-test");
        input.setOutboundStatus("FAILED");
        input.setErrorMessage("Some error");
        input.setErrorCode("500");
        input.setErrorJson(content);
        input.setDocumentId("doc-1");
        input.setTransactionId("txn-1");
        input.setBatchId("batch-1");
        input.setTenantId(mockTenantId);
        return input;
    }

    @Test
    void testExecute() throws Exception {

        ErrorResponse errorResponseAction= ErrorResponse.builder()
                .name("alchemy kvp response")
                .tenantId("1")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzUyNzU1NzMyLCJpYXQiOjE3NTI2NjkzMzIsImVtYWlsIjoiU21hcnRJbnRha2UifQ.KvXEtVMWjdwjiprxA2769ZcS1FwmwwgpHUwRwRXH72w")
                .querySet("SELECT ampq.origin_id, 'TRX-1' as transaction_id, ampq.producer_process_id as process_id, '1' as group_id,\n" +
                        "ampq.tenant_id, ampq.root_pipeline_id, 'Error' as feature, ampq.batch_id,\n" +
                        "edd.document_id as document_id,'ITX_ERR_500' as error_code,edd.failed_on_stage::varchar as error_message,\n" +
                        "edd.stages as error_json,'FAILED' as outbound_status\n" +
                        "FROM alchemy_migration.alchemy_migration_payload_error_queue ampq\n" +
                        "JOIN product_outbound.error_document_detail edd ON ampq.origin_id = edd.origin_id AND ampq.batch_id = edd.batch_id AND edd.tenant_id = ampq.tenant_id\n" +
                        "where status = 'FAILED' and ampq.group_id = '25' and ampq.tenant_id = 1 \n" +
                        "and ampq.batch_id = 'BATCH-25_0';")
                .resourceConn("intics_zio_db_conn")
                .resultTable("alchemy_response.alchemy_error_response")
                .condition(true)
                .build();

        final ActionExecutionAudit actionExecutionAudit = ActionExecutionAudit.builder().build();
        actionExecutionAudit.setRootPipelineId(11011L);
        actionExecutionAudit.getContext().put("alchemy.kvp.consumer.API.count", "1");
        actionExecutionAudit.getContext().put("alchemy.error.response.url", "http://localhost:8189/alchemy/api/v1/");
        actionExecutionAudit.getContext().put("", "http://localhost:8189/alchemy/api/");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzUyNzU1NzMyLCJpYXQiOjE3NTI2NjkzMzIsImVtYWlsIjoiU21hcnRJbnRha2UifQ.KvXEtVMWjdwjiprxA2769ZcS1FwmwwgpHUwRwRXH72w");
        actionExecutionAudit.getContext().put("group_id","1");
        actionExecutionAudit.getContext().put("copro.processor.thread.creator","FIXED_THREAD");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        ErrorResponseAction alchemyKvpResponseAction= new ErrorResponseAction(actionExecutionAudit,log,errorResponseAction);

        alchemyKvpResponseAction.execute();
    }
}
