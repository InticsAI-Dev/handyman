package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@Slf4j
class CheckboxExtractionActionTest {

    @Test
    void checkboxExtractionTest() throws Exception {
        final String TEST_PROCESS_ID = "TEST_CH_EX_" + System.currentTimeMillis();
        final String TEST_BATCH_ID = "BATCH_" + System.currentTimeMillis();
        final Long TEST_TENANT_ID = 1L;
        final String TEST_ORIGIN_ID = "ORIGIN_CH_EX_001";
        final String TEST_CHECKBOX_GROUP_ID = "CH_GRP_001";
        final Long TEST_ROOT_PIPELINE_ID = 1247L;

        // Path to test image (using the one from the curl test)
        final String TEST_IMAGE = "/Users/sanjeeya.v/Documents/intics/EH/Medical/QA Samples 1/QA Samples/NICU-Indicator-png/Inpatient23IndianaGBD_3.png";

        log.info("=== Starting Checkbox Extraction Test ===");
        log.info("Process ID: {}, Batch ID: {}", TEST_PROCESS_ID, TEST_BATCH_ID);

        try {
            // 1. Setup test input table
            setupTestInputTable(TEST_PROCESS_ID, TEST_BATCH_ID, TEST_TENANT_ID, TEST_ORIGIN_ID,
                    TEST_CHECKBOX_GROUP_ID, TEST_ROOT_PIPELINE_ID, TEST_IMAGE);

            // 2. Configure and execute CheckboxExtraction action
            CheckboxExtraction checkboxExtraction = CheckboxExtraction.builder()
                    .name("checkbox_extraction_test")
                    .condition(true)
                    .resourceConn("intics_zio_db_conn")
                    .endpoint("http://localhost:7999/extract-checkbox") // Local Copro server
                    .resultTable("checkbox_extraction_output_" + TEST_PROCESS_ID)
                    .querySet("SELECT origin_id, tenant_id, checkbox_group_id, page_number, " +
                            "input_file_path, system_prompt, user_prompt, process_id, batch_id, root_pipeline_id " +
                            "FROM checkbox_extraction_input_" + TEST_PROCESS_ID + " " +
                            "WHERE batch_id = '" + TEST_BATCH_ID + "' AND tenant_id = " + TEST_TENANT_ID +
                            " ORDER BY page_number")
                    .build();

            ActionExecutionAudit actionAudit = new ActionExecutionAudit();
            actionAudit.setRootPipelineId(TEST_ROOT_PIPELINE_ID);
            actionAudit.setActionId(14204L);
            actionAudit.setProcessId(1247L);
            actionAudit.getContext().put("checkbox.extraction.consumer.API.count", "1");
            actionAudit.getContext().put("write.batch.size", "1");
            actionAudit.getContext().put("read.batch.size", "1");

            CheckboxExtractionAction action = new CheckboxExtractionAction(actionAudit, log, checkboxExtraction);

            log.info("Executing CheckboxExtractionAction...");
            long startTime = System.currentTimeMillis();
            action.execute();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("CheckboxExtractionAction execution completed in {}ms", executionTime);

            // 3. Wait for async processing
            log.info("Waiting for copro processing to complete...");
            Thread.sleep(5000);

            // 4. Verify results
            verifyResults(TEST_PROCESS_ID, TEST_BATCH_ID, TEST_TENANT_ID, TEST_ORIGIN_ID);

            log.info("=== Checkbox Extraction Test PASSED ===");

        } catch (Exception e) {
            log.error("Test failed with exception: ", e);
            throw e;
        } finally {
            // Cleanup - usually better to keep for manual verification if it fails
            // cleanupTestTables(TEST_PROCESS_ID);
            log.info("Test tables: checkbox_extraction_input_{} and checkbox_extraction_output_{}", TEST_PROCESS_ID,
                    TEST_PROCESS_ID);
        }
    }

    private void setupTestInputTable(String processId, String batchId, Long tenantId, String originId,
            String checkboxGroupId, Long rootPipelineId, String imagePath) throws Exception {
        in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle -> {
            // Create input table
            handle.execute("CREATE TABLE IF NOT EXISTS checkbox_extraction_input_" + processId + " (" +
                    "origin_id VARCHAR(255), " +
                    "tenant_id BIGINT, " +
                    "checkbox_group_id VARCHAR(255), " +
                    "page_number INT, " +
                    "input_file_path VARCHAR(500), " +
                    "system_prompt TEXT, " +
                    "user_prompt TEXT, " +
                    "process_id VARCHAR(255), " +
                    "batch_id VARCHAR(255), " +
                    "root_pipeline_id BIGINT)");

            String systemPrompt = "You are a precise checkbox extraction assistant. Extract checkboxes from images and return labels with their checked status.";
            String userPrompt = "Extract all checkboxes from this form. For each checkbox, provide label and whether it is checked or unchecked. Return as JSON array.";

            // Insert test record
            handle.execute("INSERT INTO checkbox_extraction_input_" + processId +
                    " VALUES ('" + originId + "', " + tenantId + ", '" + checkboxGroupId + "', 1, " +
                    "'" + imagePath + "', '" + systemPrompt + "', '" + userPrompt + "', " +
                    "'" + processId + "', '" + batchId + "', " + rootPipelineId + ")");

            // Create output table
            handle.execute("CREATE TABLE IF NOT EXISTS checkbox_extraction_output_" + processId + " (" +
                    "origin_id VARCHAR(255), " +
                    "tenant_id BIGINT, " +
                    "checkbox_group_id VARCHAR(255), " +
                    "page_number INT, " +
                    "checkbox_data TEXT, " +
                    "status VARCHAR(50), " +
                    "model_name VARCHAR(255), " +
                    "error_message TEXT, " +
                    "duration_time DOUBLE PRECISION, " +
                    "batch_id VARCHAR(255), " +
                    "process_id VARCHAR(255), " +
                    "created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            log.info("Created test tables and inserted 1 record for checkbox extraction");
        });
    }

    private void verifyResults(String processId, String batchId, Long tenantId, String originId) {
        in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle -> {
            log.info("=== Verifying DB Results ===");

            List<Map<String, Object>> results = handle.createQuery(
                    "SELECT origin_id, tenant_id, page_number, checkbox_data, status, model_name, duration_time " +
                            "FROM checkbox_extraction_output_" + processId)
                    .mapToMap().list();

            log.info("Found {} results in output table", results.size());
            assert !results.isEmpty() : "Output table should not be empty";

            for (Map<String, Object> row : results) {
                String status = (String) row.get("status");
                String checkboxData = (String) row.get("checkbox_data");

                log.info("Page {}: status={}, data_length={}", row.get("page_number"), status,
                        checkboxData != null ? checkboxData.length() : 0);

                assert "SUCCESS".equals(status) : "Status should be SUCCESS but was " + status;
                assert checkboxData != null && !checkboxData.isEmpty() : "Checkbox data should not be empty";
            }
        });
    }

    private void cleanupTestTables(String processId) {
        in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle -> {
            handle.execute("DROP TABLE IF EXISTS checkbox_extraction_input_" + processId);
            handle.execute("DROP TABLE IF EXISTS checkbox_extraction_output_" + processId);
            log.info("Cleaned up test tables");
        });
    }
}
