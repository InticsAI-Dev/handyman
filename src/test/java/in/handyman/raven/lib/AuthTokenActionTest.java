package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AuthToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class AuthTokenActionTest {

    @Test
    public void testAuthToken() throws Exception {

        AuthToken authToken= AuthToken.builder()
                .name("auth_token")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.autorotation.url", "http://localhost:10181/copro/preprocess/autorotation");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("gatekeeper.url", "http://192.168.10.248:40002/"),
                Map.entry("gatekeeper.appid", "dj@intics.ai"),
                Map.entry("tenant_id", "8"),
                Map.entry("write.batch.size", "5")));


        AuthTokenAction authTokenAction=new AuthTokenAction(actionExecutionAudit,log,authToken);
        authTokenAction.execute();

    }

}