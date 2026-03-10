package in.handyman.raven.lib;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtraction;
import in.handyman.raven.lib.model.TableExtractionHeaders;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
class TableExtractionActionTest {

    @Test
    void tableExtractionTest() throws Exception {
        TableExtraction tableExtraction = TableExtraction.builder()
                .name("Text extraction macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet("SELECT  'ORIGIN-ss' as origin_id, 1 as group_id ,'/Users/sanjeeya.v/Desktop/Screenshot 2026-02-03 at 6.39.25 PM.png' as file_path," +
                        "1 as tenant_id,1 as template_id,1 as process_id,1 as root_pipeline_id")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.248:18888/copro/table-attribution"),
                Map.entry("read.batch.size", "1"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        TableExtractionAction tableExtractionAction = new TableExtractionAction(actionExecutionAudit, log, tableExtraction);
        tableExtractionAction.execute();
    }

    @Test
    void tableExtractionVersion1Test() throws Exception {
        TableExtractionHeaders tableExtraction = TableExtractionHeaders.builder()
                .name("Text extraction macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.245:18889/copro/table-attribution-with-header")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet("select * from macro.table_extraction_line_items_1234")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("mulipart.file.utpload.activator", "false"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        TableExtractionHeadersAction tableExtractionAction = new TableExtractionHeadersAction(actionExecutionAudit, log, tableExtraction);
        tableExtractionAction.execute();
    }

    @Test
    public void fileNameTest() {
        String input = "filename_2_2__121212_0_1.jpg";

        // Split the string by underscore
        String[] parts = input.split("_");

        // Check if there are at least two parts (0 and 1 after the first underscore)
        if (parts.length >= 3) {
            // Extract the second part (index 1 in the array after splitting)
            String number = parts[parts.length - 2];

            // Convert the extracted string to an integer if needed
            int extractedNumber = Integer.parseInt(number);

            // Print the extracted number
            System.out.println("Extracted number: " + extractedNumber);
        } else {
            System.out.println("Invalid input format");
        }
    }

    @org.junit.jupiter.api.Disabled("Old test - empty file path")
    @Test
    public void tableExtractionCsvRead(){

        String filePath = "";
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String removeFirstRow = "true";
            if (Objects.equals("true", removeFirstRow)) {
                reader.readNext();
            }

            String[] headers = reader.readNext(); // Read the headers

            JSONArray dataArray = new JSONArray(); // Array for data rows
            JSONArray headersArray = new JSONArray(); // Array for column headers

            // Convert headers to JSON
            for (String header : headers) {
                headersArray.put(header);
            }

            String[] row;

            while ((row = reader.readNext()) != null) {
                JSONArray rowArray = new JSONArray();

                // Convert data row to JSON
                for (int i = 0; i < headers.length; i++) {
                    rowArray.put(row[i]);
                }
                dataArray.put(rowArray);
            }

            // Create the main JSON object
            JSONObject json = new JSONObject();
            json.put("csvFilePath", filePath);
            json.put("data", dataArray);
            json.put("columnHeaders", headersArray);
            String outputResult = json.toString();


        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @org.junit.jupiter.api.Disabled("Old test - file path no longer valid")
    @Test
    public void readColumn(){

        // Path to your CSV file
        String filePath = "/home/anandh.andrews@zucisystems.com/intics-workspace/Demo/spendly/output/output/v2/2023-10-7T14_28_42 Payment Processing GenSales-4/Table/1/1_2023-10-7T14_28_42 Payment Processing GenSales-4_0.csv";

        // Column name to calculate sum
        String columnName = "Number of Sales";

        try {
            // Create a reader for the CSV file

            Long rowCount=extractRowCount(filePath);
            extractedFromFilepath(filePath, columnName,rowCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void extractedFromFilepath(String filePath, String columnName,Long rowCount) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(filePath));

        // Create a CSVParser object
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());


        double columnSum = 0.0;

        // Iterate through each record in the CSV file
        for (CSVRecord csvRecord : csvParser) {
            // Parse the value of the specified column as a double and add it to the sum

            Long totalRowCount = csvParser.getRecordNumber();

            String cellValue = csvRecord.get(columnName) != null & !csvRecord.get(columnName).isEmpty() ? csvRecord.get(columnName) : "0";
            double value = Double.parseDouble(cellValue);
            if (rowCount != totalRowCount) {

                String indexValue = csvRecord.get(1);
                columnSum += value;
            } else {
                if (columnSum == value) {
                    break;
                }

            }


        }


        // Calculate the sum of values in the specified column


        // Close the CSVParser
        csvParser.close();

        // Print the sum of the specified column
        System.out.println("Sum of values in column '" + columnName + "': " + columnSum);
    }

    public Long extractRowCount(String filePath) throws IOException {

        Reader reader = Files.newBufferedReader(Paths.get(filePath));

        // Create a CSVParser object
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

        Long rowCount = 0L;
        for (CSVRecord record : csvParser) {
            rowCount++;
        }

        // Close the CSVParser
        csvParser.close();

        return rowCount;

    }

//    @Test
//    void tableCsvTest() throws JsonProcessingException {
//        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
//        TableExtraction tableExtraction = new TableExtraction();
//
//        TableExtractionAction tableExtractionAction = new TableExtractionAction(actionExecutionAudit, log, tableExtraction);
//
//        String tableExtractionAction2 = tableExtractionAction.tableDataJson("", actionExecutionAudit);
//        System.out.println(tableExtractionAction2);
//    }

    /**
     * Test for new multi-page table extraction with Qwen VLM and markdown output
     * Tests the complete flow: input table → Copro API call → output table → DB verification
     */
    @Test
    void testMultiPageTableExtraction_withQwenVLM_shouldExtractMarkdownTables() throws Exception {
        final long timestamp = System.currentTimeMillis();
        final String TEST_PROCESS_ID = String.valueOf(timestamp % 10000000); // 7 digits
        final String TEST_BATCH_ID = "BATCH_" + timestamp;
        final Long TEST_TENANT_ID = 1L;
        final String TEST_ORIGIN_ID = "ORIGIN_BNP_GST_001";
        final String TEST_TABLE_GROUP_ID = "1001"; // numeric group_id
        final Long TEST_ROOT_PIPELINE_ID = 2001L;

        log.info("=== Starting Multi-Page Table Extraction Test ===");
        log.info("Process ID: {}, Batch ID: {}", TEST_PROCESS_ID, TEST_BATCH_ID);

        // NOTE: Update these paths to actual test image files
        final String PAGE5_IMAGE = "/Users/sanjeeya.v/Documents/intics/BNP/pages/page-5.png";
        final String PAGE6_IMAGE = "/Users/sanjeeya.v/Documents/intics/BNP/pages/page-6.png";

        // Check if files exist
        java.io.File file5 = new java.io.File(PAGE5_IMAGE);
        java.io.File file6 = new java.io.File(PAGE6_IMAGE);
        if (!file5.exists() || !file6.exists()) {
            log.warn("Test images not found! Page5: {}, Page6: {}", file5.exists(), file6.exists());
            log.warn("Test will create records but API calls will fail");
        }

        try {
            // 1. Setup test input table
            setupTestInputTable(TEST_PROCESS_ID, TEST_BATCH_ID, TEST_TENANT_ID, TEST_ORIGIN_ID,
                    TEST_TABLE_GROUP_ID, TEST_ROOT_PIPELINE_ID, PAGE5_IMAGE, PAGE6_IMAGE);

            // 2. Configure and execute TableExtraction action — uses real local copro
            TableExtraction tableExtraction = TableExtraction.builder()
                    .name("multipage_table_extraction_qwen_test")
                    .condition(true)
                    .resourceConn("intics_zio_db_conn")
                    .endpoint("http://localhost:7999/intics-copro/extract-table")  // local copro
                    .resultTable("table_extraction.table_extraction_result")
                    .outputDir("/data/output/")
                    .processId(TEST_PROCESS_ID)
                    .querySet("SELECT input_file_path, user_prompt, process, paper_no, paper_no AS page_number, origin_id, process_id, group_id, " +
                            "tenant_id, root_pipeline_id, model_registry, batch_id, category, " +
                            "created_on, sor_container_id, system_prompt " +
                            "FROM table_extraction_page_input_" + TEST_PROCESS_ID + " " +
                            "WHERE batch_id = '" + TEST_BATCH_ID + "' AND tenant_id = " + TEST_TENANT_ID +
                            " ORDER BY paper_no")
                    .build();

            ActionExecutionAudit actionAudit = new ActionExecutionAudit();
            actionAudit.setRootPipelineId(TEST_ROOT_PIPELINE_ID);
            actionAudit.setActionId(12345L);
            actionAudit.setProcessId(123L);
            actionAudit.getContext().put("copro.table-extraction.nextgen.url", "http://localhost:7999/intics-copro/extract-table");
            actionAudit.getContext().put("table.extraction.consumer.API.count", "1");
            actionAudit.getContext().put("write.batch.size", "2");
            actionAudit.getContext().put("read.batch.size", "2");

            TableExtractionAction action = new TableExtractionAction(actionAudit, log, tableExtraction);

            System.out.println("Executing TableExtractionAction for multi-page table...");
            log.info("Executing TableExtractionAction for multi-page table...");
            long startTime = System.currentTimeMillis();
            action.execute();
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("TableExtractionAction execution completed in " + executionTime + "ms");
            log.info("TableExtractionAction execution completed in {}ms", executionTime);

            // 3. Wait for async processing
            log.info("Waiting for async processing to complete...");
            Thread.sleep(10000);  // Wait longer for VLM processing

            // 4. Verify results go into the real result table
            long successRecords = verifyMultiPageResults(TEST_PROCESS_ID, TEST_BATCH_ID, TEST_TENANT_ID, TEST_ORIGIN_ID, TEST_TABLE_GROUP_ID);
            assert successRecords > 0 : "Should have at least one SUCCESS record";

            log.info("=== Multi-Page Table Extraction Test PASSED ===");

        } finally {
            log.info("Input table cleanup: DROP TABLE IF EXISTS table_extraction_page_input_{}", TEST_PROCESS_ID);
            try {
                in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle ->
                    handle.execute("DROP TABLE IF EXISTS table_extraction_page_input_" + TEST_PROCESS_ID)
                );
            } catch (Exception e) {
                log.warn("Cleanup failed: {}", e.getMessage());
            }
        }
    }

    private void setupTestInputTable(String processId, String batchId, Long tenantId, String originId,
                                      String tableGroupId, Long rootPipelineId,
                                      String page5Image, String page6Image) throws Exception {
        in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle -> {
            // Create input table matching real table_extraction_input_audit schema
            handle.execute("CREATE TABLE IF NOT EXISTS table_extraction_page_input_" + processId + " (" +
                    "input_file_path VARCHAR(500), " +
                    "user_prompt TEXT, " +
                    "process VARCHAR(255), " +
                    "paper_no INT, " +
                    "origin_id VARCHAR(255), " +
                    "process_id VARCHAR(255), " +
                    "group_id VARCHAR(255), " +
                    "tenant_id BIGINT, " +
                    "root_pipeline_id BIGINT, " +
                    "model_registry VARCHAR(255), " +
                    "batch_id VARCHAR(255), " +
                    "category VARCHAR(255), " +
                    "created_on TIMESTAMP, " +
                    "sor_container_id BIGINT, " +
                    "system_prompt TEXT)");

            String systemPrompt = "You are a table extraction expert. Extract tables from images and return them in markdown format.";
            String userPrompt = "Extract all tables from this image and format them as markdown tables. Preserve all rows and columns exactly as shown.";

            // Insert page 5
            handle.execute("INSERT INTO table_extraction_page_input_" + processId +
                    " (input_file_path, user_prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, model_registry, batch_id, category, created_on, sor_container_id, system_prompt) " +
                    "VALUES ('" + page5Image + "', '" + userPrompt + "', 'TABLE_EXTRACTION', 5, '" + originId + "', '" + processId + "', '" + tableGroupId + "', " + tenantId + ", " + rootPipelineId + ", 'default', '" + batchId + "', 'TEST', NOW(), 0, '" + systemPrompt + "')");

            // Insert page 6
            handle.execute("INSERT INTO table_extraction_page_input_" + processId +
                    " (input_file_path, user_prompt, process, paper_no, origin_id, process_id, group_id, tenant_id, root_pipeline_id, model_registry, batch_id, category, created_on, sor_container_id, system_prompt) " +
                    "VALUES ('" + page6Image + "', '" + userPrompt + "', 'TABLE_EXTRACTION', 6, '" + originId + "', '" + processId + "', '" + tableGroupId + "', " + tenantId + ", " + rootPipelineId + ", 'default', '" + batchId + "', 'TEST', NOW(), 0, '" + systemPrompt + "')");

            log.info("Created test input table with 2 pages for group {}", tableGroupId);
        });
    }

    private long verifyMultiPageResults(String processId, String batchId, Long tenantId,
                                        String originId, String tableGroupId) {
        return in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").withHandle(handle -> {
            log.info("=== Verifying DB Results in table_extraction.table_extraction_result ===");

            // Verify results in the REAL production table
            List<Map<String, Object>> results = handle.createQuery(
                    "SELECT origin_id, group_id, root_pipeline_id, tenant_id, page_number, " +
                    "markdown_table, status, model_name, error_message, duration_time, " +
                    "batch_id, process_id, created_on " +
                    "FROM table_extraction.table_extraction_result " +
                    "WHERE process_id = :processId ORDER BY page_number"
            ).bind("processId", processId).mapToMap().list();

            System.out.println("Verification query: SELECT ... FROM table_extraction.table_extraction_result WHERE process_id = '" + processId + "'");
            System.out.println("Found " + results.size() + " page-level results in table_extraction.table_extraction_result");
            log.info("Found {} page-level results in table_extraction.table_extraction_result", results.size());
            assert !results.isEmpty() : "Output table should not be empty";

            int successCount = 0;
            int failedCount = 0;

            for (Map<String, Object> row : results) {
                Integer pageNumber = (Integer) row.get("page_number");
                String status = (String) row.get("status");
                String markdownTable = (String) row.get("markdown_table");
                String errorMessage = (String) row.get("error_message");
                Double duration = (Double) row.get("duration_time");

                log.info("Page {}: status={}, markdown_length={}, duration={}s, error={}",
                        pageNumber, status, markdownTable != null ? markdownTable.length() : 0,
                        duration, errorMessage);

                if ("SUCCESS".equals(status)) {
                    successCount++;
                } else if ("FAILED".equals(status)) {
                    failedCount++;
                }
            }

            log.info("Results: {} success, {} failed", successCount, failedCount);
            log.info("=== DB Verification COMPLETED ===");
            return (long) successCount;
        });
    }

    private void cleanupTestTables(String processId) {
        try {
            in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn").useHandle(handle -> {
                handle.execute("DROP TABLE IF EXISTS table_extraction_page_input_" + processId);
                handle.execute("DROP TABLE IF EXISTS table_extraction_page_output_" + processId);
                log.info("Cleaned up test tables for process ID: {}", processId);
            });
        } catch (Exception e) {
            log.warn("Error during cleanup: {}", e.getMessage());
        }
    }

}