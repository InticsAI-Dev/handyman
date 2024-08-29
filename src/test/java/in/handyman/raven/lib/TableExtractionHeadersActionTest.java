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
                .endpoint("http://192.168.10.240:18889/copro/table-attribution-with-header")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("999")
                .resultTable("table_extraction.table_extraction_result")
                .outputDir("/data/output/")
                .querySet(" SELECT a.tenant_id, a.root_pipeline_id, a.group_id, a.origin_id, a.paper_no, a.document_type, a.template_name, a.file_path,a.table_headers,a.model_name as model_name,a.truth_entity_id,a.sor_container_id,a.channel_id, a.batch_id\n" +
                        "from macro.table_extraction_line_items_audit a\n" +
                        "join table_extraction.table_extraction_payload_queue b on a.origin_id=b.origin_id and a.batch_id = b.batch_id\n" +
                        "where b.status='IN_PROGRESS' and a.group_id='92' and a.tenant_id =81 and a.model_name='XENON' and b.batch_id ='BATCH-92_0' limit 1;")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url.v1", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("multipart.file.upload.activator", "false"),
                Map.entry("triton.request.table.headers.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        actionExecutionAudit.setProcessId(1L);
        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.setActionId(1L);

        TableExtractionHeadersAction tableExtractionHeadersAction = new TableExtractionHeadersAction(actionExecutionAudit, log, tableExtraction);
        tableExtractionHeadersAction.execute();
    }


}