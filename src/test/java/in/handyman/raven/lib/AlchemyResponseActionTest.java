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
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzMyMTAzNTcyLCJpYXQiOjE3MzIwMTcxNzIsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.2rMw2OpyfLu38HpXSZelmz0mjMYGPmeqresonxuXcUY")
                .tenantId(74L)
                .querySet(" select id, paper_no, origin_id, process, group_id, tenant_id, root_pipeline_id, batch_id,\n" +
                        "model_registry, 'CHECKBOX_EXTRACTION' as feature, container_name as sor_item_name,\n" +
                        "predicted_value as extracted_value, bbox, confidence_score, state from \n" +
                        "checkbox_attribution.checkbox_attribution_output_audit caoa\n" +
                        "where id = 27;")
                .resourceConn("intics_zio_db_conn")

                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzMyMDY5MjY3LCJpYXQiOjE3MzE5ODI4NjcsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.IuVzA-yP8C9752bGFv7ZqejSovumm4N6DfbTuvF7fhc");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","116");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}