package in.handyman.raven.lib;

import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyKvpResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class AlchemyKvpResponseActionTest {

    @Test
    public void AlchemyKvpTest() throws Exception {

        AlchemyKvpResponse alchemyKvpResponse= AlchemyKvpResponse.builder()
                .name("alchemy kvp response")
                .processId("12345")
                .querySet(" SELECT  12345 as process_id, ap.group_id, ap.tenant_id, ap.root_pipeline_id,ap.alchemy_origin_id, ap.pipeline_origin_id,'/input/path' as file_name\n" +
                        "            FROM product_outbound.product_outbound_payload_queue ampq join alchemy_migration.alchemy_papers ap on ampq.origin_id = ap.pipeline_origin_id\n" +
                        "            where status = 'IN_PROGRESS' and ampq.group_id = 1 group by ap.alchemy_origin_id,ap.group_id, ap.tenant_id,ap.root_pipeline_id,ap.pipeline_origin_id;")
                .resourceConn("intics_zio_db_conn")
                .resultTable("alchemy_response.alchemy_kvp_response")
                .condition(true)
                .build();

        final ActionExecutionAudit actionExecutionAudit = ActionExecutionAudit.builder().build();
        actionExecutionAudit.setRootPipelineId(11011L);
        actionExecutionAudit.getContext().put("alchemy.kvp.consumer.API.count", "1");
        actionExecutionAudit.getContext().put("copro.product.outbound.url", "http://192.168.15.34:8189/alchemy/api/v1/transaction/response");
        actionExecutionAudit.getContext().put("", "http://192.168.15.34:8189/alchemy/api/v1/transaction/response");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk1NTgxNjA4LCJpYXQiOjE2OTU1Mzg0MDgsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.rNzD31_3AxBBx966Tb5IhIVp14f-3TKPB8NGT0usD4o");
        actionExecutionAudit.getContext().put("group_id","1");
        AlchemyKvpResponseAction alchemyKvpResponseAction= new AlchemyKvpResponseAction(actionExecutionAudit,log,alchemyKvpResponse,100);

        alchemyKvpResponseAction.execute();

    }

}