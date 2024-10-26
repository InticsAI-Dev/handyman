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
                .querySet("SELECT '/data/input/2.jpg' as file_path,sot.origin_id,asset.file_id,sot.paper_no,sot.group_id,\n" +
                        "'1234' as root_pipeline_id,sot.tenant_id, sot.batch_id, now() as created_on\n" +
                        "FROM info.source_of_truth sot\n" +
                        "JOIN info.asset asset\n" +
                        "ON asset.file_id=sot.preprocessed_file_id\n" +
                        "join qr_extraction.qr_extraction_payload_queue_archive b on b.origin_id=sot.origin_id\n" +
                        "WHERE sot.group_id=285 and sot.tenant_id = 1 and sot.batch_id ='BATCH-285_0';\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.qr-attribution.url", "http://192.168.10.245:8700/v2/models/qr-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("qr.consumer.API.count", "1"),
                Map.entry("write.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "10"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("encryptionUrl", "http://192.168.10.248:10001/copro-utils/data-security/encrypt"),
                Map.entry("decryptionUrl","http://192.168.10.248:10001/copro-utils/data-security/decrypt"),
                Map.entry("decryption.activator","true"),
                Map.entry("target_directory_path","/data/output/14575/qr-extraction"),
                Map.entry("database.decryption.activator", "true"),
                Map.entry("encryption.activator","true")));


      QrExtractionAction action1 = new QrExtractionAction(actionExecutionAudit, log, qrExtraction);
        action1.execute();

    }
}


