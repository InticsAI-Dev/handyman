package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.IntegratedNoiseModelApiAction;
import in.handyman.raven.lib.model.IntegratedNoiseModelApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class NoiseModelsActionTest {
    @Test
    void tritonServer() throws Exception {

        final IntegratedNoiseModelApi build = IntegratedNoiseModelApi.builder()
                .condition(true)
                .processId("1235472754635346527")
                .name("noise-detection-model")
                .endPoint("http://192.168.10.248:9400/v2/models/noise-detection-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .outputTable("noise_model.noise_model_output_table")
                .querySet("SELECT " +
                        "'/data/input/test.jpg' as inputFilePath, " +
                        "'INT-1' as origin_id, " +
                        "'/data/output/' as output_dir ,"+
                        "1 as file_id, " +
                        "1 as group_id, " +
                        "1 as paper_no, '1' as batch_id, " +
                        "1 as process_id, " +
                        "1 as tenant_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.noise-detection.url", "http://0.0.0.0:10199/copro/Noise-Detection");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("noise.consumer.API.count", "1"),
                Map.entry("write.batch.size", "1"),
                Map.entry("actionId","1"),
                Map.entry("triton.request.activator","true"),
                Map.entry("okhttp.client.timeout", "10")));

        IntegratedNoiseModelApiAction action1 = new IntegratedNoiseModelApiAction(actionExecutionAudit,log,build);
        action1.execute();

    }
}