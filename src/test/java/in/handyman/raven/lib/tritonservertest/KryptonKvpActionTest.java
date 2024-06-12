package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.KryptonJsonParserAction;
import in.handyman.raven.lib.KryptonKvpAction;
import in.handyman.raven.lib.model.KryptonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class KryptonKvpActionTest {
    @Test
    public void tritonTest() throws Exception {
        KryptonKvp kryptonKvp = KryptonKvp.builder()
                .name("krypton kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.248:9700/v2/models/krypton-service/versions/1/infer")
                .outputTable("krypton_kvp_output")
                .querySet("select '/data/output/174/preprocess/autorotation/auto_rotation/0d2d1665-224_1_1_1.png' as input_file_path, 'Please extract all key-value pairs from the document and provide the results in a JSON format. \n" +
                        "Ensure that the keys and values are clearly identified and represented. \n" +
                        "Group them into sections based upon the information. \n" +
                        "Note: Maintain the proper JSON format in the response' as prompt,\n" +
                        "'XENON' as text_model, 'KVP_ARGON' as process, 1 as root_pipeline_id, 1 as action_id, 'Handwritten' as paper_type, 'KRYPTON' as model_registry,\n" +
                        "1 as paper_no, 'ORIGIN-1' as origin_id, 1 as process_id, 1 as group_id, 1 as tenant_id, 'JSON' as response_format, 1 as action_id, 'BATCH_1' as batch_id;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("krypton.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.krypton.kvp.activator", "true");


        KryptonKvpAction kryptonKvpAction = new KryptonKvpAction(ac, log, kryptonKvp);

        kryptonKvpAction.execute();

    }
}
