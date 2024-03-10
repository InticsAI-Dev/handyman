package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtractionHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TableExtractionHeadersActionTest {

    @Test
    void tableExtractionTest() throws Exception {
        TableExtractionHeaders tableExtraction = TableExtractionHeaders.builder()
                .name("Text extraction macro test after copro optimization")
                .endpoint("http://192.168.10.245:18889/copro/table-attribution-with-header")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet(" SELECT 1 as tenant_id,1 as root_pipeline_id, 1 as group_id,'ORIGIN-1' as origin_id,1 as paper_no," +
                        "'PRINTED' as document_type,'KRYPTON' as template_name," +
                        "'/data/output/86/preprocess/paper_itemizer/pdf_to_image/2023-10-7T14_28_42 Payment Processing GenSales-4/2023-10-7T14_28_42 Payment Processing GenSales-4_0.jpg' as file_path," +
                        "'[{\"columnHeaders\": [\"Number of Credits\", \"Amount of Credits\", \"Average Ticket\", \"Disc %\", \"Discount Due\", \"Disk P/I\", \"Net sales\", \"Plan code\", \"Amount of Sales\", \"Number of Sales\"], \"TruthEntity\": \"PLAN_SUMMARY\"}]' as table_headers,'KRYPTON' as model_name;")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url.v1", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("multipart.file.upload.activator", "false"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        actionExecutionAudit.setProcessId(1L);
        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.setActionId(1L);

        TableExtractionHeadersAction tableExtractionHeadersAction = new TableExtractionHeadersAction(actionExecutionAudit, log, tableExtraction);
        tableExtractionHeadersAction.execute();
    }


}