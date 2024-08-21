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
                .endpoint("http://192.168.10.245:18887/copro/table-attribution-with-header")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet(" SELECT 'ORIGIN-2093' as origin_id, 1 as paper_no, 1503 as group_id, '/data/output/81/1503/42793/autorotation/auto_rotation/QTN3864 - BOQ for the Fit-Out Works in Polo Ralph Lauren at MOE_1.jpg' as file_path, 81 as tenant_id, 1 as process_id, '/data/output/1503/paragraph_extraction/' as output_dir, 42793 as root_pipeline_id, 'PARAGRAPH_EXTRACTION' as process, 13658 as synonym_id, 'You are an intelligent bullet point extractor specialized in extracting payment terms from quotations. Payment terms typically include percentages of payments split into four bullet points. Your task is to extract payment terms from the provided quotations. Payment terms are usually found in the second part and at the end of the quotation content.     Your output should be in the array of string not array of dict or others like this Please ensure that your response starts with, ###response_Start and ends with ###response_End. Make sure to read the prompt thoroughly for complete understanding.' as prompt, 'Payment terms' as section_header, 'batch_id' as batch_id\n")
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