package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class RadonKvpAcionTest {
    @Test
    public void tritonTest() throws Exception {
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.248:7800/v2/models/krypton-x-service/versions/1/infer")
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT input_file_path, user_prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, system_prompt,\n" +
                        "                batch_id, model_registry, category, now() as created_on, 'KRYPTON START' as api_name,sor_container_id, " +
                        "'KRYPTON_DOUBLE_PASS_MODE' as krypton_inference_mode, transformation_user_prompts as transformation_user_prompts, " +
                        "transformation_system_prompts as transformation_system_prompts\n" +
                        "                FROM cleanup_schema.radon_kvp_input_3427;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("legacy.resource.connection.type", "LEGACY");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("text.to.replace.prompt", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("sor.transaction.prompt.base64.activator", "false");
        ac.getContext().put("prompt.base64.activator", "false");
        ac.getContext().put("copro.client.socket.timeout", "10");
        ac.getContext().put("copro.client.api.sleeptime", "10");
        ac.getContext().put("pipeline.copro.api.process.file.format", "BASE64");
        ac.getContext().put("pipeline.encryption.default.holder", "");
        ac.getContext().put("pipeline.text.extraction.encryption", "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put("pipeline.end.to.end.encryption", "false");
        ac.getContext().put("kvp.double.pass.batch.size", "2");
        ac.getContext().put("kvp.double.pass.mode", "KRYPTON_DOUBLE_PASS_MODE");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }
}
