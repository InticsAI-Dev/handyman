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
                .querySet("\n" +
                        "select tenant_id, root_pipeline_id, group_id, origin_id, paper_no, document_type, template_name, file_path,\n" +
                        "  jsonb_agg(\n" +
                        "  jsonb_build_object(\n" +
                        "      'TruthEntity', truth_entity_name,\n" +
                        "  'columnHeaders', synonym_array\n" +
                        "          )\n" +
                        "      ) AS table_headers,\n" +
                        "      model_name,\n" +
                        "      truth_entity_id as truth_entity_id,\n" +
                        "      sor_container_id as sor_container_id,\n" +
                        "      channel_id,\n" +
                        "      batch_id\n" +
                        "   from   macro.table_extraction_temp_table_audit\n" +
                        "   where batch_id ='BATCH-96_0'\n" +
                        "  group by\n" +
                        "      tenant_id,\n" +
                        "      root_pipeline_id,\n" +
                        "      group_id,\n" +
                        "      origin_id,\n" +
                        "      paper_no,\n" +
                        "      document_type,\n" +
                        "      template_name,\n" +
                        "      file_path,\n" +
                        "      model_name,\n" +
                        "      truth_entity_id,\n" +
                        "      sor_container_id,\n" +
                        "      channel_id,\n" +
                        "      batch_id;\n" +
                        "\n")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url.v1", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("multipart.file.upload.activator", "true"),
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