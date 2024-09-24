package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TableExtractionHeadersAction;
import in.handyman.raven.lib.TableExtractionOutboundAction;
import in.handyman.raven.lib.model.TableExtractionOutbound;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class TableExtractionOutboundTest {


    @Test
    public void tableExtractionTest() throws Exception {

        TableExtractionOutbound tableExtractionOutbound = TableExtractionOutbound.builder()
                .name("table extraction outbound")
                .condition(true)
                .resultTable("table_extraction.table_extraction_outbound_output_audit")
                .processId(String.valueOf(1))
                .querySet(" SELECT 1 as process_id,1 as group_id,1 as tenant_id,1 as template_id, 'ORIGIN-1' as origin_id,1 as paper_no,\n" +
                        "                    '/data/output/76/table_extraction-v2/table_extraction/2022-10-26T9_58_10 Dooliquor LLC_0/csv/2_2022-10-26T9_58_10 Dooliquor LLC_0.csv' as processed_file_path," +
                        "'' as table_response,'' as status,'' as stage, '' as message, '2024-02-19 21:10:45.828' as created_on,\n" +
                        "                    1 as root_pipeline_id, '' as bboxes, '' as croppedimage, 'Number of Sales' as column_header, 'PLAN_SUMMARY' as truth_entity_name, 'KRYPTON' as model_name,1 as sor_item_id \n" +
                        "                     ")
                .resourceConn("intics_zio_db_conn")
                .inputAttribution("csv")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.setProcessId(1L);

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("multipart.file.upload.activator", "false"),
                Map.entry("last.row.value.total.check", "false"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        TableExtractionOutboundAction tableExtractionAction = new TableExtractionOutboundAction(actionExecutionAudit, log, tableExtractionOutbound);
        tableExtractionAction.execute();

    }

    @Test
    public void tableExtractionTest2() throws Exception {

        TableExtractionOutbound tableExtractionOutbound = TableExtractionOutbound.builder()
                .name("table extraction outbound")
                .condition(true)
                .resultTable("table_extraction.table_extraction_outbound_output_audit")
                .processId(String.valueOf(1))
                .querySet(" SELECT 1 as process_id,1 as group_id,1 as tenant_id,1 as template_id, 'ORIGIN-1' as origin_id,1 as paper_no,\n" +
                        "                    '/data/output/106/table_extraction-v2/table_extraction/2023-5-2T21_49_22 Payment Processing Master Tax Services LI LLC_0/csv/1_2023-5-2T21_49_22 Payment Processing Master Tax Services LI LLC_0.csv' as processed_file_path," +
                        "'' as table_response,'' as status,'' as stage, '' as message, '2024-02-19 21:10:45.828' as created_on,\n" +
                        "                    1 as root_pipeline_id, '' as bboxes, '' as croppedimage, '# SALES' as column_header, '# SALES' as truth_entity_name, 'KRYPTON' as model_name,1 as sor_item_id \n" +
                        "                     ")
                .resourceConn("intics_zio_db_conn")
                .inputAttribution("csv")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.setProcessId(1L);

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("multipart.file.upload.activator", "false"),
                Map.entry("last.row.value.total.check", "false"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        TableExtractionOutboundAction tableExtractionAction = new TableExtractionOutboundAction(actionExecutionAudit, log, tableExtractionOutbound);
        tableExtractionAction.execute();

    }

}
