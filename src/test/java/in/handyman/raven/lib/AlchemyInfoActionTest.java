package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
class AlchemyInfoActionTest {

    @Test
    void execute() throws Exception {


        AlchemyInfo alchemyInfo = AlchemyInfo.builder()
                .tenantId(1L)
                .condition(true)
                .name("alchemy info action")
                .querySet("SELECT 1 as tenant_id, 'ORIGIN-1' as origin_id, '1' as root_pipeline_id, 1 as group_id, '/home/anandh.andrews@zucisystems.com/a.txt' as file_path ")
                .resourceConn("intics_zio_db_conn")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk0ODA4MDEzLCJpYXQiOjE2OTQ3NjQ4MTMsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.2IQEewH-cQ5TK3L8wKpk_UAXrojAnWqSTi9ORwQJyOU")
                .build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("alchemy.origin.upload.url","http://localhost:8189/alchemy/api/v1/info/origin/upload");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk0ODA4MDEzLCJpYXQiOjE2OTQ3NjQ4MTMsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.2IQEewH-cQ5TK3L8wKpk_UAXrojAnWqSTi9ORwQJyOU");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");


        AlchemyInfoAction alchemyInfoAction = new AlchemyInfoAction(actionExecutionAudit, log, alchemyInfo);
        alchemyInfoAction.execute();
    }
}