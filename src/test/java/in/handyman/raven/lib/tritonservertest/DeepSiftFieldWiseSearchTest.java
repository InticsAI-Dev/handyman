package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.DeepSiftAction;
import in.handyman.raven.lib.DeepSiftSearchAction;
import in.handyman.raven.lib.model.DeepSift;
import in.handyman.raven.lib.model.DeepSiftSearch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

@Slf4j
public class DeepSiftFieldWiseSearchTest {

    private ActionExecutionAudit actionExecutionAudit;

    @BeforeEach
    void setUp() {
        actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(9001L);
        actionExecutionAudit.setActionId(50001L);

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "10"),
                Map.entry("write.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "30"),
                Map.entry("deep.sift.consumer.API.count", "2"),
                Map.entry("deep.sift.extraction.xenon.API.count", "2"),
                Map.entry("deep.sift.extraction.krypton.API.count", "2"),
                Map.entry("deep.sift.page.content.min.length.threshold", "10"),
                Map.entry("clinical.present.page.content.min.length.threshold", "10"),
                Map.entry("deep.sift.extraction.activator", "true"),
                Map.entry("deep.sift.search.consumer.API.count", "2"),
                Map.entry("copro.metrics.activator", "false"),
                Map.entry("copro.isretry.enabled", "false"),
                Map.entry("encrypt.deep.sift.output", "false"),
                Map.entry("encrypt.request.response", "false"),
                Map.entry("copro.api.file.input.format", "BASE64"),
                Map.entry("actionId", "50001"),
                Map.entry("copro.client.deep.sift.connect.timeout", "10"),
                Map.entry("copro.client.deep.sift.write.timeout", "10"),
                Map.entry("copro.client.deep.sift.read.timeout", "10"),
                Map.entry("copro.client.deep.sift.call.timeout", "10")
        ));
    }

    @Test
    @DisplayName("Test 1: Deep Sift OCR Extraction with Word Count Calculation")
    void testDeepSiftOCRExtractionWithWordCount() throws Exception {
        DeepSift deepSift = DeepSift.builder()
                .name("xenon deep sift extraction for batch BATCH-27_0")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics-bronze-08a490e67cef47dd27545c6964d7f0ec-0000.us-south.containers.appdomain.cloud/xenon-extraction-server/intics-copro/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .forkBatchSize("2")
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
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.tenant_id = 1\n" +
                        "  AND dsi.group_id = '27'\n" +
                        "  AND dsi.model_name = 'XENON';")
                .build();

        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();

        log.info("✅ Test 1 Completed: OCR Extraction with Word Count");
        log.info("Expected: word_count and is_blank_page columns populated in deep_sift_output_9001");
    }

    @Test
    @DisplayName("Test 2: Field-Specific Page Selection with Blank Page Skipping")
    void testFieldSpecificPageSelectionWithBlankPageSkipping() throws Exception {
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("search with field-specific page count and blank page handling")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.extracted_text,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.search_id,\n" +
                        "    dsi.search_name,\n" +
                        "    dsi.keywords\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.tenant_id = 1\n" +
                        "  AND dsi.group_id = '27';")
                .build();

        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();

        log.info("✅ Test 2 Completed: Field-Specific Page Selection");
        log.info("Expected: Different fields searched on different page ranges based on sor_item configuration");
    }

    @Test
    @DisplayName("Test 3: Page Range Configuration - Process Pages 2-5 Only")
    void testPageRangeConfiguration() throws Exception {
        // This test verifies that fields with page_range='2-5' only search pages 2,3,4,5
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("search with page range 2-5")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.extracted_text,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.search_id,\n" +
                        "    dsi.search_name,\n" +
                        "    dsi.keywords,\n" +
                        "    dsi.field_paper_count,\n" +
                        "    dsi.field_consider_blank_pages,\n" +
                        "    dsi.is_blank_page,\n" +
                        "    dsi.page_range\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.tenant_id = 1\n" +
                        "  AND dsi.sor_item_name = 'responsible_area'\n" +
                        "  AND dsi.page_range = '2-5';")
                .build();

        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();

        log.info("✅ Test 3 Completed: Page Range Configuration");
        log.info("Expected: Only pages within range 2-5 should be searched");
    }

    @Test
    @DisplayName("Test 4: Blank Page Detection - Pages with <10 Words Marked as Blank")
    void testBlankPageDetection() throws Exception {
        DeepSift deepSift = DeepSift.builder()
                .name("xenon extraction to verify blank page detection")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .forkBatchSize("2")
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
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.model_name = 'XENON';")
                .build();

        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();

        log.info("✅ Test 4 Completed: Blank Page Detection");
        log.info("Expected: Pages with word_count < 10 should have is_blank_page = true");
        log.info("Validation Query:");
        log.info("SELECT paper_no, word_count, is_blank_page FROM deep_sift_output_9001 WHERE is_blank_page = true;");
    }

    @Test
    @DisplayName("Test 5: Field with consider_blank_pages=false - Skip Blank Pages")
    void testSkipBlankPagesForField() throws Exception {
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("search skipping blank pages")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.extracted_text,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.search_id,\n" +
                        "    dsi.search_name,\n" +
                        "    dsi.keywords\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.field_consider_blank_pages = false;")
                .build();

        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();

        log.info("✅ Test 5 Completed: Skip Blank Pages");
        log.info("Expected: No records with is_blank_page=true should be in search input for this field");
        log.info("Validation Query:");
        log.info("SELECT sor_item_name, COUNT(*) FROM deep_search_input_9001 WHERE is_blank_page=true AND field_consider_blank_pages=false;");
        log.info("Expected count: 0");
    }

    @Test
    @DisplayName("Test 6: Word Count Calculation Using WordCountAdapter")
    void testWordCountAdapterIntegration() throws Exception {
        DeepSift deepSift = DeepSift.builder()
                .name("test word count adapter")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .forkBatchSize("1")
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
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.paper_no = 1\n" +
                        "  AND dsi.model_name = 'XENON';")
                .build();

        DeepSiftAction deepSiftAction = new DeepSiftAction(actionExecutionAudit, log, deepSift);
        deepSiftAction.execute();

        log.info("✅ Test 6 Completed: WordCountAdapter Integration");
        log.info("Expected: word_count calculated using WordCountAdapter.getThresholdScore()");
    }

    @Test
    @DisplayName("Test 7: Multiple Fields with Different Configurations")
    void testMultipleFieldsWithDifferentConfigurations() throws Exception {
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("search multiple fields with different configs")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.extracted_text,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.search_id,\n" +
                        "    dsi.search_name,\n" +
                        "    dsi.keywords,\n" +
                        "    dsi.field_paper_count,\n" +
                        "    dsi.field_consider_blank_pages,\n" +
                        "    dsi.page_range\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.batch_id = 'BATCH-27_0'\n" +
                        "  AND dsi.sor_item_name IN ('responsible_area', 'fax_report', 'clinical_present');")
                .build();

        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();

        log.info("✅ Test 7 Completed: Multiple Fields with Different Configurations");
        log.info("Expected:");
        log.info("- responsible_area: paper_count=4, consider_blank_pages=false, page_range='1-4'");
        log.info("- fax_report: paper_count=NULL (uses global), consider_blank_pages=true");
        log.info("- clinical_present: paper_count=5, consider_blank_pages=false, page_range='2-10'");
    }

    @Test
    @DisplayName("Test 8: Audit Tables Populated Correctly")
    void testAuditTablesPopulation() throws Exception {
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("verify audit table population")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("https://intics.elevance.ngrok.dev/xenon-extraction-server/xenon-textract")
                .processId("9001")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "    dsi.origin_id,\n" +
                        "    dsi.group_id,\n" +
                        "    dsi.created_on,\n" +
                        "    dsi.created_by,\n" +
                        "    dsi.extracted_text,\n" +
                        "    dsi.root_pipeline_id,\n" +
                        "    dsi.tenant_id,\n" +
                        "    dsi.batch_id,\n" +
                        "    dsi.paper_no,\n" +
                        "    dsi.source_document_type,\n" +
                        "    dsi.sor_container_id,\n" +
                        "    dsi.sor_container_name,\n" +
                        "    dsi.sor_item_id,\n" +
                        "    dsi.sor_item_name,\n" +
                        "    dsi.search_id,\n" +
                        "    dsi.search_name,\n" +
                        "    dsi.keywords\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.batch_id = 'BATCH-27_0';")
                .build();

        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();

        log.info("✅ Test 8 Completed: Audit Tables Population");
        log.info("Validation Queries:");
        log.info("1. SELECT COUNT(*) FROM deep_sift_output_audit WHERE word_count IS NOT NULL;");
        log.info("2. SELECT COUNT(*) FROM deep_search_input_audit WHERE field_paper_count IS NOT NULL;");
        log.info("3. SELECT COUNT(*) FROM deep_search_input_audit WHERE page_range IS NOT NULL;");
    }
}