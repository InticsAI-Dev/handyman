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
                .originId("ORIGIN-81")
                .eocId("H97276489")
                .authtoken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJHYXRlS2VlcGVyIiwiZXhwIjoxNzQ2MTA3NjkzLCJpYXQiOjE3NDYwMjEyOTMsImVtYWlsIjoiYWdhZGlhLWRldkBpbnRpY3MuYWkifQ.1mK7QvS7riiV67UT98erEfUSi8B0fSQQwvqJ9C8WKJ4")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .groupId("84")
                .documentId("SYNTH_10_PAGE_1").build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("tenant_id","6");
        actionExecutionAudit.getContext().put("batch_id", "BATCH-84_0");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("gatekeeper.url", "http://localhost:40002/");

        EocJsonGeneratorAction eocJsonGeneratorAction = new EocJsonGeneratorAction(actionExecutionAudit, log, eocJsonGenerator);
        eocJsonGeneratorAction.execute();
    }
}
