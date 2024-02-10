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

        TableExtractionOutbound tableExtractionOutbound= TableExtractionOutbound.builder()
                .name("table extraction outbound")
                .condition(true)
                .resultTable("table_extraction.table_extraction_outbound_output_audit")
                .processId(String.valueOf(1))
                .querySet(" SELECT a.process_id, a.group_id, a.tenant_id, a.template_id, a.origin_id, a.paper_no,\n" +
                        "                    a.processed_file_path, a.table_response, a.status, a.stage, a.message, a.created_on,\n" +
                        "                    a.root_pipeline_id, a.bboxes, a.croppedimage, st.synonym as column_header, a.truth_entity_name, a.model_name,si.sor_item_id \n" +
                        "                    FROM table_extraction.table_extraction_result a\n" +
                        "                    join info.source_of_truth sot on sot.origin_id = a.origin_id and sot.paper_no =a.paper_no \n" +
                        "                    join sor_meta.asset_info ai on lower(ai.template_name)=lower(sot.template_name) and a.tenant_id  = ai.tenant_id\n" +
                        "                    join sor_meta.truth_entity te on ai.asset_id = te.asset_id and te.tenant_id  = a.tenant_id\n" +
                        "                    join sor_meta.sor_tsynonym st on st.truth_entity_id =te.truth_entity_id \n" +
                        "                    join sor_meta.sor_item si on si.sor_item_id =st.sor_item_id \n" +
                        "                    where si.table_aggregate_function = 'SUM' and a.tenant_id =69 and te.sip_type ='TABLE_SIP'; ")
                .resourceConn("intics_zio_db_conn")
                .inputAttribution("csv")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.setProcessId(1L);

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.table-extraction.url", "http://192.168.10.245:18889/copro/table-attribution-with-header"),
                Map.entry("read.batch.size", "1"),
                Map.entry("mulipart.file.utpload.activator", "false"),
                Map.entry("table.extraction.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        TableExtractionOutboundAction tableExtractionAction = new TableExtractionOutboundAction(actionExecutionAudit, log, tableExtractionOutbound);
        tableExtractionAction.execute();

    }




}
