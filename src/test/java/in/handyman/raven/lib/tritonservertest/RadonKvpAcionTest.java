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
                .endpoint("https://api.runpod.ai/v2/mds27mhmolybf0/runsync")
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT '/data/tenant/PI/pdf_to_image/cpt/cpt_0.png' as input_file_path, a.user_prompt, a.process, a.paper_no, a.origin_id, a.process_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.system_prompt,\n" +
                        "a.batch_id, a.model_registry, a.category, now() as created_on, (CASE WHEN 'KRYPTON' = 'RADON' then 'RADON START'\n" +
                        "WHEN 'KRYPTON' = 'KRYPTON' then 'KRYPTON START'\n" +
                        "WHEN 'KRYPTON' = 'NEON' then 'NEON START' end) as api_name,sc.post_processing::bool as post_process,sc.post_process_class_name as post_process_class_name,sc.sor_container_id\n" +
                        "FROM sor_transaction.radon_kvp_input_audit a\n" +
                        "JOIN sor_meta.sor_container sc on a.sor_container_id=sc.sor_container_id\n" +
                        "WHERE a.model_registry = 'RADON' and a.tenant_id='1' and a.root_pipeline_id =76995")
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
        ac.getContext().put("pipeline.text.extraction.encryption", "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put("pipeline.end.to.end.encryption", "false");
        ac.getContext().put("document_type", "HEALTH_CARE");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "RUNPOD");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("ProviderTransformerFinalBsh", "ProviderTransformerFinalBsh");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();
    }
}
