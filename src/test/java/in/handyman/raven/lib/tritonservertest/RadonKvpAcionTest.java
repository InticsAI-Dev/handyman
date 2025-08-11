package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;

@Slf4j
public class RadonKvpAcionTest {
    @Test
    public void tritonTest() throws Exception {
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("https://intics.elevance.ngrok.dev/v2/models/krypton-x-service/versions/1/infer")
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT DISTINCT\n" +
                        "    a.input_file_path,\n" +
                        "    a.input_response_json,\n" +
                        "    a.user_prompt,\n" +
                        "    a.process,\n" +
                        "    a.paper_no,\n" +
                        "    a.origin_id,\n" +
                        "    a.process_id,\n" +
                        "    a.group_id,\n" +
                        "    a.tenant_id,\n" +
                        "    a.root_pipeline_id,\n" +
                        "    a.system_prompt,\n" +
                        "    a.batch_id,\n" +
                        "    a.model_registry,\n" +
                        "    a.category,\n" +
                        "    NOW() AS created_on,\n" +
                        "    CASE\n" +
                        "        WHEN 'KRYPTON' = 'RADON' THEN 'RADON START'\n" +
                        "        WHEN 'KRYPTON' = 'KRYPTON' THEN 'KRYPTON START'\n" +
                        "        WHEN 'KRYPTON' = 'OPTIMUS' THEN 'OPTIMUS START'\n" +
                        "    END AS api_name,\n" +
                        "    sc.sor_container_id AS sor_container_id,\n" +
                        "    si.sor_item_id AS sor_item_id\n" +
                        "FROM transit_data.kvp_transformer_input_35339 a\n" +
                        "JOIN sor_meta.sor_container sc\n" +
                        "    ON a.sor_container_id = sc.sor_container_id\n" +
                        "JOIN sor_meta.sor_item si\n" +
                        "    ON si.sor_item_id = a.sor_item_id\n" +
                        "    AND si.sor_container_id = sc.sor_container_id\n" +
                        "WHERE\n" +
                        "    a.model_registry = 'OPTIMUS'\n" +
                        "    AND a.group_id = '154'\n" +
                        "    AND a.tenant_id = '1'\n" +
                        "    AND a.batch_id = 'BATCH-154_0';\n")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("text.to.replace.prompt", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("prompt.base64.activator", "false");
        ac.getContext().put("copro.client.socket.timeout", "10");
        ac.getContext().put("copro.client.api.sleeptime", "10");
        ac.getContext().put("pipeline.copro.api.process.file.format", "BASE64");
        ac.getContext().put("pipeline.encryption.default.holder", "");
        ac.getContext().put(ENCRYPT_TEXT_EXTRACTION_OUTPUT, "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("document_type", "HEALTH_CARE");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "RUNPOD");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("ProviderTransformerFinalBsh", "ProviderTransformerFinalBsh");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();
    }
}
