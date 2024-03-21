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
                .endpoint("http://192.168.10.245:18890/copro/table-attribution-with-header-v4")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet("")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18890/copro/table-attribution-with-header-v4"),
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
                .endpoint("http://192.168.10.245:18890/copro/table-attribution-with-header-v4")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet("\n" +
                        "    SELECT tenant_id, root_pipeline_id, group_id, origin_id, paper_no, document_type, template_name, file_path, table_headers,'ARGON' as model_name,truth_entity_id,sor_container_id,channel_id, 'batch_1' as batch_id \n" +
                        "           from macro.table_extraction_line_items_123\n")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("mulipart.file.upload.activator", "false"),
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

}