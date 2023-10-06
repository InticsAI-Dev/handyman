package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtraction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class TableExtractionActionTest {

    @Test
    void tableExtractionTest() throws Exception {
        TableExtraction tableExtraction= TableExtraction.builder()
                .name("Text extraction macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet(" \n" +
                        "                        SELECT a.origin_id, a.group_id ,c.file_path,b.tenant_id,b.document_id as template_id,a.producer_process_id as process_id,a.root_pipeline_id\n" +
                        "                        from table_extraction.table_extraction_payload_queue a\n" +
                        "                       join info.source_of_origin b on a.origin_id=b.origin_id\n" +
                        "                        join info.asset c on b.file_id=c.file_id\n" +
                        "                        where a.status='IN_PROGRESS' and a.group_id=7;")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url","http://192.168.10.245:10198/copro/table-attribution"),
                Map.entry("read.batch.size","1"),
                Map.entry("table.extraction.consumer.API.count","1"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","1")));

        TableExtractionAction tableExtractionAction=new TableExtractionAction(actionExecutionAudit,log,tableExtraction);
        tableExtractionAction.execute();
    }

    @Test
    public void fileNameTest(){
        String input = "filename_2_2__121212_0_1.jpg";

        // Split the string by underscore
        String[] parts = input.split("_");

        // Check if there are at least two parts (0 and 1 after the first underscore)
        if (parts.length >= 3) {
            // Extract the second part (index 1 in the array after splitting)
            String number = parts[parts.length-2];

            // Convert the extracted string to an integer if needed
            int extractedNumber = Integer.parseInt(number);

            // Print the extracted number
            System.out.println("Extracted number: " + extractedNumber);
        } else {
            System.out.println("Invalid input format");
        }
    }

    @Test
    void tableCsvTest() throws JsonProcessingException {
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        TableExtraction tableExtraction=new TableExtraction();

        TableExtractionAction tableExtractionAction=new TableExtractionAction(actionExecutionAudit,log,tableExtraction);

        String tableExtractionAction2=tableExtractionAction.tableDataJson("");
        System.out.println(tableExtractionAction2);
    }
}