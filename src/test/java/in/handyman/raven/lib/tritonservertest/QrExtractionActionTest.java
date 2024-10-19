package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lib.QrExtractionAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import in.handyman.raven.lib.model.QrExtraction;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import java.util.Map;

@Slf4j
public class QrExtractionActionTest {

    @Test
    void tritonServer() throws Exception {

         QrExtraction qrExtraction = QrExtraction.builder()

                .condition(true)
                .name("qr-extraction")
                .resourceConn("intics_zio_db_conn")
                .processId("1234")
                .endPoint("http://192.168.10.248:8700/v2/models/qr-extraction-service/versions/1/infer")
                .outputTable("qr_extraction.qr_extraction_result_audit")
                .querySet("SELECT '/data/output/processed_images/12-10-2024_10_10_42/tenant_1/group_67/preprocess/autorotation/auto_rotation/SYNT_166564144_c1_1.jpg' as file_path, 'ORIGIN-1' as origin_id, 1 as file_id, 1 as paper_no, 1 as group_id, 1 as root_pipeline_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.qr-attribution.url", "http://192.168.10.245:8700/v2/models/qr-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("qr.consumer.API.count", "1"),
                Map.entry("write.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "10"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("apiUrl", "http://0.0.0.0:10001/copro-utils/data-security/encrypt"),
                Map.entry("decryptApiUrl","http://192.168.10.239:10001/copro-utils/data-security/decrypt"),
                Map.entry("decryption.activator","true"),
                Map.entry("target_directory_path","/data/output/14575/qr-extraction"),
                Map.entry("database.decryption.activator", "true"),
                Map.entry("encryption.activator","true")));


      QrExtractionAction action1 = new QrExtractionAction(actionExecutionAudit, log, qrExtraction);
        action1.execute();

    }
}


