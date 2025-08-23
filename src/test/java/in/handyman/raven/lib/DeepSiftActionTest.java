package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DeepSift;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class DeepSiftActionTest {

    @Test
    void tritonServer() throws Exception {
        DeepSift deepSift = DeepSift.builder()
                .name("deep sift extraction for group_id 579 for batch id BATCH-579_1")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://localhost:5432/xenon-textract")
                .processId("5443")
                .resultTable("deep_sift.deep_sift_output_audit")
                .forkBatchSize("8")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.input_file_path,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.model_id,\n" +
                        "    dsi.model_name,\n" +
                        "    dsi.base_prompt,\n" +
                        "    dsi.system_prompt\n" +
                        "FROM deep_sift.deep_sift_input_audit dsi\n" +
                        "JOIN deep_sift.deep_sift_payload_queue_archive dspq\n" +
                        "ON dspq.origin_id = dsi.origin_id\n" +
                        "WHERE\n" +
                        "    dsi.batch_id = 'BATCH-129_0'\n" +
                        "    AND dsi.tenant_id = 1\n" +
                        "    AND dsi.group_id = '129'")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://192.168.10.241:10100/xenon-textract");
        actionExecutionAudit.setProcessId(5443L);
        actionExecutionAudit.setActionId(21352L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("deep.sift.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("copro.request.deep.sift.handler.name", "TRITON"),
                Map.entry("pipeline.deep.sift.encryption", "true"),
                Map.entry("actionId", "21352"),
                Map.entry("write.batch.size", "5"),
                Map.entry("deep.sift.page.content.min.length.threshold", "1"),
                Map.entry("deep.sift.extraction.activator", "true")
        ));
        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();
    }

    @Test
    void tritonKryptonServer() throws Exception {
        String filePath = "/data/multipart-files/vulcan_data/output/1/transaction/TRZ-1321/a22eba72-856f-4046-91db-15e5a62c6fb2/139737009524220116/processed_images/05-08-2025_09_08_53/tenant_1/group_52/preprocess/paper_itemizer/pdf_to_image/FM202505091418000/FM202505091418000_3.jpg";
        DeepSift deepSift = DeepSift.builder()
                .name("deep sift extraction for group_id 579 for batch id BATCH-579_1")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://localhost:5432/xenon-textract")
                .processId("138980184199100180")
                .resultTable("transit_data.deep_sift_output_2036")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    '" + filePath + "' as input_file_path,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.model_id,\n" +
                        "    dsi.model_name,\n" +
                        "    dsi.search_name,\n" +
                        "    'Extract all the page content and return as text and dont add preambles' as base_prompt,\n" +
                        "    dsi.system_prompt\n" +
                        "FROM transit_data.deep_sift_input_2036 dsi\n" +
                        "JOIN deep_sift.deep_sift_payload_queue dspq\n" +
                        "ON dspq.origin_id = dsi.origin_id\n" +
                        "WHERE\n" +
                        "    dsi.batch_id = 'BATCH-52_0'\n" +
                        "    AND dsi.tenant_id = 1\n" +
                        "    AND dsi.group_id = '52';")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://localhost:5432/xenon-textract");
        actionExecutionAudit.setProcessId(2036L);
        actionExecutionAudit.setActionId(21352L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("replicate.request.api.token", "API_TOKEN"),
                Map.entry("replicate.deep.sift.version", "1"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("copro.request.deep.sift.handler.name", "TRITON"),
                Map.entry("deep.sift.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("preprocess.deep.sift.model.name", "KRYPTON"),
                Map.entry("deep.sift.page.content.min.length.threshold", "1"),
                Map.entry("tesseract.data.path", ""),
                Map.entry("write.batch.size", "5"),
                Map.entry("deep.sift.extraction.activator", "true")
        ));
        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();
    }
}