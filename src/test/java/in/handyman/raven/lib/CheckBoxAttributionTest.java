package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxAttribution;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class CheckBoxAttributionTest {
    @Test
    public void tritonTest() throws Exception {
        CheckboxAttribution checkbox = CheckboxAttribution.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.241:7700/v2/models/krypton-service/versions/1/infer")
                .outputTable("checkbox_attribution.checkbox_attribution_output_audit")
                .querySet("\n" +
                        "SELECT '/data/input/radonApp/SYNTH_PR_Amerigroup_Template_2_10_1.jpg' as input_file_path , pv.base_prompt as prompt,  'CHECKBOX ATTRIBUTION' as process,\n" +
                        "'RADON' as model_registry, stpq.batch_id, 'Primary', now(),\n" +
                        "sot.paper_no ,stpq.origin_id , '35987' as process_id, stpq.group_id , stpq.tenant_id , \n" +
                        "stpq.root_pipeline_id ,\n" +
                        "pv.sor_container_id\n" +
                        "FROM checkbox_attribution.checkbox_attributiom_payload_queue_archive stpq\n" +
                        "join info.source_of_truth sot on sot.origin_id=stpq.origin_id and sot.tenant_id=stpq.tenant_id\n" +
                        "join paper.paper_level_score_audit pl on pl.origin_id = sot.origin_id\n" +
                        "and pl.tenant_id = sot.tenant_id and pl.batch_id = sot.batch_id and pl.paper_no = sot.paper_no\n" +
                        "join sor_meta.asset_info ai on ai.template_name = sot.template_name and ai.tenant_id = sot.tenant_id\n" +
                        "join info.asset a on a.asset_id=sot.asset_id and a.tenant_id= sot.tenant_id\n" +
                        "join sor_meta.truth_entity te on te.asset_id = ai.asset_id and ai.tenant_id = te.tenant_id\n" +
                        "join sor_meta.sor_container sc on sc.sor_container_id = te.sor_container_id and sc.tenant_id = te.tenant_id\n" +
                        "join sor_meta.radon_prompt_table pv on ai.tenant_id = pv.tenant_id and sc.sor_container_id = pv.sor_container_id\n" +
                        "where sot.root_pipeline_id = 138000\n")
                .build();



        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");


        CheckboxAttributionAction radonKvpAction = new CheckboxAttributionAction( ac, log, checkbox);
        radonKvpAction.execute();

    }
}
