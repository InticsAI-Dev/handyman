package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import in.handyman.raven.lib.QrExtractionAction;
import in.handyman.raven.lib.model.QrExtraction;

import java.util.Map;

@Slf4j
public class QrExtractionActionTest {

    @Test
    void tritonServer() throws Exception {

        final QrExtraction build = QrExtraction.builder()

                .condition(true)
                .name("qr-extraction")
                .resourceConn("intics_zio_db_conn")
                .processId("1234356524675")
                .endPoint("http://192.168.10.248:8700/v2/models/qr-extraction-service/versions/1/infer")
                .outputTable("qr_extraction.qr_extraction_result_")
                .querySet("SELECT '/data/input/test.jpg' as file_path, '/data/output/' as outputDir, 'ORIGIN-1' as origin_id, 1 as file_id, 1 as tenant_id, 1 as paper_no, 1 as group_id, 1 as root_pipeline_id, '1' as batchId")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.qr-attribution.url", "http://192.168.10.245:8700/v2/models/qr-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("qr.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("write.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "10")));


      QrExtractionAction action1 = new QrExtractionAction(actionExecutionAudit, log, build);
        action1.execute();

    }
}


