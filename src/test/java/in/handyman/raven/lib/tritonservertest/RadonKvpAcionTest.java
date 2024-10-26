package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class RadonKvpAcionTest {
    @Test
    public void tritonTest() throws Exception {
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .outputTable("sor_transaction.radon_kvp_output_12345")
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.248:7500/v2/models/radon-service/versions/1/infer")
                .querySet("SELECT id, '/data/output/processed_images/24-08-2024_07_08_25/tenant_1/group_287/preprocess/autorotation/auto_rotation/SYNT_167047724_C4_1.jpg' as input_file_path, prompt, process, paper_no, origin_id, process_id, group_id,\n" +
                        "tenant_id, root_pipeline_id, batch_id, model_registry\n" +
                        "    FROM sor_transaction.radon_kvp_input_audit \n" +
                        "    WHERE id =3;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
            Map.entry("okhttp.client.timeout", "20"),
            Map.entry("text.extraction.consumer.API.count", "1"),
            Map.entry("triton.request.activator", "true"),
            Map.entry("actionId", "1"),
            Map.entry("write.batch.size", "5"),
            Map.entry("database.decryption.activator", "true"),
            Map.entry("page.content.min.length.threshold", "5"),
            Map.entry("apiUrl", "http://192.168.10.248:10001/copro-utils/data-security/encrypt"),
            Map.entry("decryptApiUrl","http://192.168.10.248:10001/copro-utils/data-security/decrypt"),
            Map.entry("encryption.activator","false")));

        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();

    }
}
