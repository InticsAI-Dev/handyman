package in.handyman.raven.lib.tritonservertest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpConsumerProcess;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RadonKvpAcionTest {

    private RadonKvpConsumerProcess radonKvpConsumerProcess;
    private ObjectMapper objectMapper;

    @Test
    public void tritonTest() throws Exception {
    RadonKvpConsumerProcess radonKvpConsumerProcess;
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.240:7700/v2/models/krypton-service/versions/1/infer")
                .outputTable("sor_transaction.radon_kvp_output_175654")
                .querySet("SELECT id, input_file_path, prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry\n" +
                        "FROM sor_transaction.radon_kvp_input_audit\n" +
                        "WHERE root_pipeline_id =175654;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }

    @Mock
    private Logger log;

    @Mock
    private Marker aMarker;

    @Mock
    private ActionExecutionAudit action;

    @Mock
    private RadonKvpAction aAction;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        when(aAction.getTimeOut()).thenReturn(1); // Set timeout to 1 minute for testing
        radonKvpConsumerProcess = new RadonKvpConsumerProcess(log, aMarker, action, aAction);
    }

    @Test
    public void testConvertFormattedJsonStringToJsonNodeWithMarkers() throws Exception {
        String jsonResponse = "Here is some text before\n```json\n{\"key\":\"\"}\n```\nAnd some text after";
        JsonNode expectedNode = objectMapper.readTree("{\"key\":\"\"}");
        JsonNode resultNode = radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
        assertEquals(expectedNode, resultNode);
    }

    @Test
    public void testConvertFormattedJsonStringToJsonNodeWithoutMarkers() throws Exception {
        String jsonResponse = "{\"key\":\"value\"}";
        JsonNode expectedNode = objectMapper.readTree("{\"key\":\"value\"}");
        JsonNode resultNode = radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
        assertEquals(expectedNode, resultNode);
    }

    @Test
    public void testConvertFormattedJsonStringToJsonNodeWithInvalidJson() throws Exception {
        String jsonResponse = "Here is some text before\n```json\n{\"key\":\"value\"\n```\nAnd some text after";
        JsonNode expectedNode = objectMapper.readTree("{\"key\":\"value\"}");
        JsonNode resultNode = radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
        assertEquals(expectedNode, resultNode);
    }

    @Test
    public void testConvertFormattedJsonStringToJsonNodeWithEmptyString() {
        String jsonResponse = "";
        JsonNode resultNode = radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
        assertNull(resultNode);
    }

    @Test
    public void testConvertFormattedJsonStringToJsonNodeWithOnlyMarkers() {
        String jsonResponse = "```json\n```\n";
        JsonNode resultNode = radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
        assertNull(resultNode);
    }

}