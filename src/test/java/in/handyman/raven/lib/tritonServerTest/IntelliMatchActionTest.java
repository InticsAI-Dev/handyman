package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.Intellimatch;
import in.handyman.raven.lib.IntellimatchAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class IntelliMatchActionTest {

    @Test
    void tritonServer() throws Exception {

        final Intellimatch action = Intellimatch.builder()
                .condition(true)
                .name("intellimatch")
                .resourceConn("intics_agadia_db_conn")
                .matchResult("")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.intelli-match.url", "http://localhost:9200/v2/models/cos-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.intellimatch.API.count", "1"),
                Map.entry("write.batch.size", "5")));

       IntellimatchAction action1 = new IntellimatchAction(actionExecutionAudit, log, action);
        action1.execute();
    }
}