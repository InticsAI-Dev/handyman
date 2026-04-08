package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CustomResponseGeneration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class CustomResponseGenerationActionTest {

    @Test
    void execute() throws Exception {
        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("read.batch.size", "5");
        action.getContext().put("write.batch.size", "5");
        action.getContext().put("custom.response.generation.consumer.API.count", "1");
        action.getContext().put("custom.response.generation.consumer.url", "http://localhost:8189/");
        action.getContext().put("custom.json.generation.structure",
                "{ \"aumipayload\": { " +
                        "\"memberFirstName\": {\"value\":\"\", \"page\":0, \"confidence\":0, \"boundingBox\":{\"x\":0,\"width\":0,\"y\":0,\"height\":0}}," +
                        "\"memberAdditionalProperties\": [" +
                        "{\"propName\":{\"value\":\"MEMBER_INDICATOR\"}, \"propValue\":{\"value\":\"\", \"page\":0, \"confidence\":0, \"boundingBox\":{\"x\":0,\"width\":0,\"y\":0,\"height\":0}}}," +
                        "{\"propName\":{\"value\":\"NEWBORN_REQUEST\"}, \"propValue\":{\"value\":\"\", \"page\":0, \"confidence\":0, \"boundingBox\":{\"x\":0,\"width\":0,\"y\":0,\"height\":0}}}" +
                        "] } }");

        CustomResponseGeneration customResponseGeneration = CustomResponseGeneration.builder()
                .name("Custom Response Generation Action")
                .tenantId("1")
                .token("sample-token")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("sor_transform.custom_response_generation_output")
                .querySet("SELECT " +
                        "1::bigint as prediction_id, " +
                        "'ORIGIN-1' as origin_id, " +
                        "'memberFirstName' as sor_item_name, " +
                        "'Noichole' as predicted_value, " +
                        "2 as paper_no, " +
                        "0.50::double precision as precision, " +
                        "33.82::double precision as left_pos, " +
                        "451.5::double precision as right_pos, " +
                        "1669.4::double precision as upper_pos, " +
                        "1786.87::double precision as lower_pos, " +
                        "'100' as root_pipeline_id, " +
                        "1::bigint as tenant_id, " +
                        "10::bigint as group_id, " +
                        "'BATCH-1' as batch_id, " +
                        "'{\"inboundTransactionId\":\"INB-1\"}' as metadata_json")
                .build();

        CustomResponseGenerationAction actionObj = new CustomResponseGenerationAction(action, log, customResponseGeneration);
        actionObj.execute();
    }
}
