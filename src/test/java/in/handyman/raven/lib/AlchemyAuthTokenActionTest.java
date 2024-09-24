package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class AlchemyAuthTokenActionTest {

    @Test
    void execute() throws Exception {

        AlchemyAuthToken alchemyAuthToken = AlchemyAuthToken.builder().build();
        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.login.url","http://localhost:8189/alchemy/api/v1/auth/login");
        actionExecutionAudit.getContext().put("alchemy.user","demo@intics.com");
        actionExecutionAudit.getContext().put("alchemy.password","Marvel@2026");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");

        AlchemyAuthTokenAction alchemyAuthTokenAction = new AlchemyAuthTokenAction(actionExecutionAudit, log, alchemyAuthToken);
        alchemyAuthTokenAction.execute();
    }
}