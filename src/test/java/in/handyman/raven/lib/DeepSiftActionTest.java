package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DeepSift;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DeepSiftActionTest {

    @Test
    void testTritonServerExecution() throws Exception {
        // Build the DeepSift configuration
        DeepSift deepSift = DeepSift.builder()
                .name("deep sift extraction for group_id 579 for batch id BATCH-579_1")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://172.202.112.23/xenon-textract")
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
                        "WHERE origin_id = 'ORIGIN-1160';")
                .build();

        // Assert query correctness
        String query = deepSift.getQuerySet();
        assertTrue(query.toUpperCase().contains("FROM DEEP_SIFT.DEEP_SIFT_INPUT_AUDIT"), "Query must reference the correct table");
        assertTrue(query.contains("WHERE origin_id = 'ORIGIN-1160'"), "Query must filter by origin_id");

        // Setup ActionExecutionAudit context
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://localhost:5432/xenon-textract");
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
                Map.entry("copro.isretry.enabled", "true"),
                Map.entry("deep.sift.extraction.activator", "true"),
                Map.entry("deep.sift.route.tess4j", "true")
        ));

        // Execute DeepSift action
        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);

        assertDoesNotThrow(deepSiftAction::execute, "Execution should not throw any exceptions");
    }

    @Test
    void tritonKryptonServer() throws Exception {
        String filePath = "/data/multipart-files/vulcan_data/output/1/transaction/TRZ-1321/a22eba72-856f-4046-91db-15e5a62c6fb2/139737009524220116/processed_images/05-08-2025_09_08_53/tenant_1/group_52/preprocess/paper_itemizer/pdf_to_image/FM202505091418000/FM202505091418000_3.jpg";
        DeepSift deepSift = DeepSift.builder()
                .name("deep sift extraction for group_id 579 for batch id BATCH-579_1")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://localhost:8001/xenon-textract")
                .processId("138980184199100180")
                .resultTable("deep_sift.deep_sift_output_audit")
                .querySet("SELECT\n" +
                        "dsi.origin_id,\n" +
                        "dsi.group_id,\n" +
                        "dsi.created_on,\n" +
                        "dsi.created_by,\n" +
                        "dsi.input_file_path,\n" +
                        "dsi.root_pipeline_id,\n" +
                        "dsi.tenant_id,\n" +
                        "dsi.batch_id,\n" +
                        "dsi.paper_no,\n" +
                        "dsi.source_document_type,\n" +
                        "dsi.model_id,\n" +
                        "dsi.model_name,\n" +
                        "dsi.base_prompt,\n" +
                        "dsi.system_prompt\n" +
                        "FROM deep_sift.deep_sift_input_audit dsi\n" +
                        "WHERE\n" +
                        "dsi.origin_id = 'ORIGIN-32'\n" +
                        "AND dsi.tenant_id = 1\n" +
                        "AND dsi.group_id = '35'\n" +
                        "AND dsi.model_name = 'XENON' limit 5;")
                .forkBatchSize("5")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://localhost:8001/xenon-textract");
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
                Map.entry("write.batch.size", "1"),
                Map.entry("deep.sift.extraction.activator", "true"),
                Map.entry("copro.isretry.enabled", "true"),
                Map.entry("copro.processor.consumer.route.type", "MODERN"),
                Map.entry("copro.metrics.activator", "true")
        ));
        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();
    }
}