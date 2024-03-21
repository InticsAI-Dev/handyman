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
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzEwOTE0OTczLCJpYXQiOjE3MTA4Mjg1NzMsImVtYWlsIjoiZGVtb0BpbnRpY3MuYWkifQ.e6NFu4qPgVxH76b42LBEvneR5zrL02Itcwv70ae-cH4")
                .tenantId(1L)
                .querySet("SELECT ar.bbox, ar.alchemy_origin_id as origin_id, ar.paper_no, \n" +
                        "ar.tenant_id, ampq.root_pipeline_id, ar.confidence_score, ar.extracted_value, ar.sor_item_name, ar.synonym_id, 'batch_1' as batch_id\n" +
                        "FROM alchemy_migration.alchemy_response ar \n" +
                        "join alchemy_migration.alchemy_migration_payload_queue ampq on ampq.origin_id = ar.pipeline_origin_id;")
                .resourceConn("intics_zio_db_conn")
                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url", "http://192.168.10.248:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzEwOTYxMDI2LCJpYXQiOjE3MTA4NzQ2MjYsImVtYWlsIjoiZGVtb0BpbnRpY3MuYWkifQ.TL37Ng2ajwVq-YL9wYevLPKmtGlaG8oUZxME3J4q8BA");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId", "1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id", "1");
        actionExecutionAudit.getContext().put("read.batch.size", "1");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}