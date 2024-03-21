package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.OutboundKvpResponse;
import in.handyman.raven.lib.model.OutboundTableResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class OutboundTableResponseActionTest {


    @Test
    void tableResponseFromAlchemy() throws Exception {

        OutboundTableResponse outboundTableResponse= OutboundTableResponse.builder()
                .name("alchemy table response ")
                .resourceConn("intics_zio_db_conn")
                .resultTable("alchemy_response.alchemy_table_response")
                .condition(true)
                .processId("12345")
                .querySet(" SELECT ampq.producer_process_id as process_id, ap.group_id, 2 as tenant_id, ap.root_pipeline_id,\n" +
                        "ap.origin_id  as alchemy_origin_id, ap.origin_id  as pipeline_origin_id ,ap.paper_no, 'batch_1' as batch_id\n" +
                        "FROM product_outbound.product_outbound_payload_queue ampq\n" +
                        "join info.source_of_truth ap on ampq.origin_id = ap.origin_id\n" +
                        "limit 1")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("alchemy.outbound.table.url","http://192.168.15.34:8189/alchemy/api/v1/response/table");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk1OTM2MjQzLCJpYXQiOjE2OTU4OTMwNDMsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.Ltwlm5CVDbR9EmHyLzjczRYC_Tn_etlWKBxISP6tbsE");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("alchemy.table.consumer.API.count","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");


        OutboundTableResponseAction outboundTableResponseAction=new OutboundTableResponseAction(actionExecutionAudit,log,outboundTableResponse);
        try {
            outboundTableResponseAction.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}