package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EocJsonGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class EocJsonGeneratorActionTest {
    @Test
    void execute() throws Exception {
        EocJsonGenerator eocJsonGenerator = EocJsonGenerator.builder()
                .name("eoc generation")
                .originId("ORIGIN-51")
                .eocId("H00928567")
                .authtoken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJHYXRlS2VlcGVyIiwiZXhwIjoxNzI4NTM1MTY5LCJpYXQiOjE3Mjg0NDg3NjksImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.f24Fk7ZLT3Z6nEkqHKl5xTMN1OmAnClAii_gdnuU54E")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .groupId("51")
                .documentId("SYNT_166564144_c1").build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("group_id","51");
        actionExecutionAudit.getContext().put("tenant_id","1");
        actionExecutionAudit.getContext().put("batch_id", "BATCH-1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("gatekeeper.url", "http://localhost:40002/");

        EocJsonGeneratorAction eocJsonGeneratorAction = new EocJsonGeneratorAction(actionExecutionAudit, log, eocJsonGenerator);
        eocJsonGeneratorAction.execute();
    }
}
