package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.lib.model.FtpConnectionTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class FtpConnectionTestActionTest {
    private final FtpConnectionTestAction ftpConnectionCheckAction;
    {
        var actionExecutionAudit = ActionExecutionAudit.builder().actionName("FTP UPLOAD").build();

        ftpConnectionCheckAction = new FtpConnectionTestAction(actionExecutionAudit,
                LambdaEngine.getLogger(actionExecutionAudit),
                FtpConnectionTest.builder()
                        .name("FTP Connection Test")
                        .resultTable("onboard_wizard_info.ftp_connection_check")
                        .resourceConn("intics_zio_db_conn")
                        .condition(true)
                        .build());

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("ftpHost","192.168.10.245"),
                Map.entry("tenant_id","1"),
                Map.entry("ftpUserName","deploy"),
                Map.entry("ftpPassword","deploy@123"),
                Map.entry("ftpSourceDir","/Downloads"),
                Map.entry("ftpPort","21")));
    }
    @Test
    void execute() throws Exception {
        ftpConnectionCheckAction.execute();
    }
}
