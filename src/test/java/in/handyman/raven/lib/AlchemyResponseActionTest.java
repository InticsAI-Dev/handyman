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
                .token("")
                .tenantId(1L)
                .querySet("select cr.b_box ,cr.origin_id ,cr.paper_no ,cr.tenant_id ,cr.root_pipeline_id , cr.confidence_score as confidence_score,cr.extracted_value as extracted_value,\n" +
                        "                cr.sor_item_name,cr.synonym_id,cr.question_id ,'KIE' as feature,null as state ,null as table_data, cr.batch_id\n" +
                        "                FROM voting.cummulative_result cr\n where origin_id ='ORIGIN-60'")
                .resourceConn("intics_zio_db_conn")
                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/predictions-list/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("alchemy.response.consumer.API.count","10");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("alchemy.response.output.table","alchemy_migration.alchemy_transform_output_table");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}