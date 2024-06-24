package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class AlchemyResponseActionTest {

    @Test
    void execute() throws Exception {

        AlchemyResponse alchemyResponse = AlchemyResponse.builder()
                .name("alchemy response action")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzE3ODQ4NDAzLCJpYXQiOjE3MTc3NjIwMDMsImVtYWlsIjoibml2YXJhX2RlbW9AaW50aWNzLmFpIn0.iKZtp1SyCEWX934YP6xKGSGlSgy6SMWeE6ur16W_Q_Y")
                .tenantId(74L)
                .querySet("select cr.b_box ,cr.origin_id ,cr.paper_no ,cr.tenant_id ,cr.root_pipeline_id , cr.confidence_score,cr.extracted_value,\n" +
                        "                cr.sor_item_name,cr.synonym_id,cr.question_id ,'KIE' as feature,null as state ,null as table_data\n" +
                        "                FROM voting.cummulative_result cr\n where tenant_id =105")
                .resourceConn("intics_zio_db_conn_bl")
                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzE3ODQ4NDAzLCJpYXQiOjE3MTc3NjIwMDMsImVtYWlsIjoibml2YXJhX2RlbW9AaW50aWNzLmFpIn0.iKZtp1SyCEWX934YP6xKGSGlSgy6SMWeE6ur16W_Q_Y");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","74");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}