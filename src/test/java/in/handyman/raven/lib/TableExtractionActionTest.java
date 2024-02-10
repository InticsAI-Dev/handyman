package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtraction;
import in.handyman.raven.lib.model.TableExtractionHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

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
                .querySet("SELECT  'ORIGIN-ss' as origin_id, 1 as group_id ,'/data/output/186/preprocess/autorotation/auto_rotation/(Galvanized___Heavy_Duty)_Racks_Quotation_0.jpg' as file_path," +
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