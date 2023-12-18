package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.IntegratedNoiseModelApiAction;
import in.handyman.raven.lib.model.IntegratedNoiseModelApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class NoiseModelActionTest {
    @Test
    void tritonServer() throws Exception {

        final IntegratedNoiseModelApi build = IntegratedNoiseModelApi.builder()
                .condition(true)
                .processId("123")
                .name("noise-detection-model")
                .resourceConn("intics_agadia_db_conn")
                .outputTable("integrated_noise_model.noise_model_output_table")
                .querySet("SELECT " +
                        "'/home/christopher.paulraj@zucisystems.com/Pictures/database.jpeg' as file_path, " +
                        "'INT-1' as origin_id, " +
                        "'/home/christopher.paulraj@zucisystems.com/Pictures' as output_dir ,"+
                        "1 as file_id, " +
                        "1 as group_id, " +
                        "1 as paper_no, " +
                        "1 as process_id, " +
                        "1 as tenant_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.noise-detection.url", "http://0.0.0.0:10193/copro/Noise-Detection/checkbox-noise-detection");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("noise.consumer.API.count", "1"),
                Map.entry("write.batch.size", "1"),
                Map.entry("actionId","1"),
                Map.entry("TRITON_REQUEST_ACTIVATOR","true"),

                Map.entry("okhttp.client.timeout", "10")));

        IntegratedNoiseModelApiAction action1 = new IntegratedNoiseModelApiAction(actionExecutionAudit,log,build);
        action1.execute();

    }
}