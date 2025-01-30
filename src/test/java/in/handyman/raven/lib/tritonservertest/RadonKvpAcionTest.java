package in.handyman.raven.lib.tritonservertest;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpConsumerProcess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.Marker;



import static org.junit.jupiter.api.Assertions.*;

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
                .endpoint("https://d276d05a6764.ngrok.app/v2/models/krypton-x-service/versions/1/infer")
                .outputTable("sor_transaction.radon_bbox_output_audit")
                .querySet("sELECT input_file_path, user_prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, system_prompt,\n" +
                        "batch_id, model_registry, category, now() as created_on, (CASE WHEN 'KRYPTON' = 'RADON' then 'RADON START'\n" +
                        "WHEN 'KRYPTON' = 'KRYPTON' then 'KRYPTON START'\n" +
                        "WHEN 'KRYPTON' = 'NEON' then 'NEON START' end) as api_name\n" +
                        "from sor_transaction.radon_kvp_input_audit rbia \n" +
                        "where root_pipeline_id =1807;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("bbox.radon_bbox_activator", "true");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("text.to.replace.prompt", "{%sreplaceable_value_of_the_previous_json}");
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
        String jsonResponse = "Here is some text before\n```json\n[{\"key\":\"auth_id\",\"value\":\"\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,\"bottomRightY\":120}},{\"key\":\"auth_admit_date\",\"value\":\"\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,\"bottomRightY\":120}},{\"key\":\"auth_discharge_date\",\"value\":\"\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,\"bottomRightY\":120}},{\"key\":\"auth_discharge_disposition\",\"value\":\"\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,\"bottomRightY\":120}},{\"key\":\"level_of_service\",\"value\":\"\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,\"bottomRightY\":120}},{\"key\":\"length_of_stay\",\"value\":\"06/12/2024 08:35:27+00:00 GMT\",\"confidence\":1.0,\"boundingBox\":{\"topLeftX\":500,\"topLeftY\":100,\"bottomRightX\":900,bottomRightY\":120}}```\nAnd some text after";
        JsonNode expectedNode = objectMapper.readTree("[{\"key\":\"auth_id\",\"value\":\"\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}},{\"key\":\"auth_admit_date\",\"value\":\"\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}},{\"key\":\"auth_discharge_date\",\"value\":\"\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}},{\"key\":\"auth_discharge_disposition\",\"value\":\"\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}},{\"key\":\"level_of_service\",\"value\":\"\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}},{\"key\":\"length_of_stay\",\"value\":\"06/12/2024 08:35:27+00:00 GMT\",\"confidence\":\"1.0\",\"boundingBox\":{\"topLeftX\":\"500\",\"topLeftY\":\"100\",\"bottomRightX\":\"900\",\"bottomRightY\":\"120\"}}]");
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