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
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT '/data/input/input_files/26_1.jpg' as input_file_path, \n" +
                        "'**Instruction:** Extract all key-value pairs (KVP) from the document, including both printed and handwritten data. For each KVP, provide the key, value. Ensure that the output is in JSON format.\n" +
                        " \n" +
                        "**Guidelines:**\n" +
                        " \n" +
                        "1. **Key-Value Pairs Extraction:** Accurately identify and extract all key-value pairs from the document. The key and value should be captured as text.\n" +
                        " \n" +
                        "4. **Printed and Handwritten Text:** Ensure that the extraction process accounts for both printed and handwritten text, accurately capturing each key-value pair regardless of the format.\n" +
                        " \n" +
                        "5. **Comprehensive Coverage:** Ensure all key-value pairs in the document are captured.' as prompt,\n" +
                        "'RADON KVP' as process,1 paper_no,'ORIGIN-3933' as origin_id,'104080' as process_id,'3218' as group_id, '115' as tenant_id,'104080' as root_pipeline_id,\n" +
                        "'batch_id' as batch_id, 'RADON' as model_registry, 'PRIMARY' as category")
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
}