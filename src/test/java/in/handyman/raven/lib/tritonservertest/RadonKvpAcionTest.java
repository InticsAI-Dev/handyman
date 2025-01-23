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
                .endpoint("http://192.168.10.248:7700/predict")
                .outputTable("kvp_extraction.kvp_extraction_output_audit")
                .querySet("SELECT input_file_path, user_prompt, system_prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id,\n" +
                        "batch_id, model_registry, category, now() as created_on, 'KRYPTON START' as api_name\n" +
                        "FROM kvp_extraction.kvp_extraction_input_audit\n" +
                        "WHERE model_registry = 'KRYPTON'  and group_id ='521' and tenant_id='115' and batch_id ='BATCH-521_0' and process='KVP_EXTRACTION';")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put(" krypton.base64.activator", "false");
        ac.getContext().put("krypton.bbox.activator", "false");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }
}
