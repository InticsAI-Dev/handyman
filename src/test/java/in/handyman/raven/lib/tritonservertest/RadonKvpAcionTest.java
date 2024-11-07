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
                .endpoint("http://192.168.10.241:7500/v2/models/radon-service/versions/1/infer")
                .outputTable("sor_transaction.radon_kvp_output_30197")
                .querySet("SELECT id, input_file_path, prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry\n" +
                        "FROM sor_transaction.radon_kvp_input_audit\n" +
                        "WHERE root_pipeline_id =32789;")
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

    @Test
    public void replicateTest() throws Exception {
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("https://api.replicate.com/v1/predictions")
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT 1 as id, '' as input_file_path,'extract kvp and return as json' prompt,'RADON_KVP' process,1 as paper_no,'ORIGIN-1' origin_id,'1' as process_id,1 as group_id,1 as tenant_id,1 as root_pipeline_id,'BATCH-1' as batch_id,'KRYPTON' model_registry, now() as created_on,'KRYPTON START' as api_name,encode as base64img\n" +
                        "from base64img;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("copro.request.activator.handler.name", "REPLICATE");
        ac.getContext().put("replicate.request.api.token", "");
        ac.getContext().put("replicate.radon.version", "459adf971f85d8200cb6555a0245f1d14da43a6cc757000a26bd4bf7f95feb2e");


        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }
}
