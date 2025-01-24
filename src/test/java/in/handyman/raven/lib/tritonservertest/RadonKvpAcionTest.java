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
                .endpoint("http://localhost:7800/v2/models/krypton-x-service/versions/1/infer")
                .outputTable("table_extraction_info.table_extraction_output_109661")
                .querySet("SELECT input_file_path, user_prompt, system_prompt, process, paper_no, origin_id, process_id, group_id,\n" +
                        "tenant_id, root_pipeline_id,batch_id, model_registry, category, now() as created_on, 'KRYPTON START' as api_name\n" +
                        "from table_extraction_info.table_extraction_input_audit teoa \n" +
                        "where root_pipeline_id =206963;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("pipeline.copro.api.process.file.format","FILE");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }
}
