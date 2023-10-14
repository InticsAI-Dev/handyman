package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.lib.model.FtpConnectionCheck;
import org.junit.jupiter.api.Test;


public class FtpConnectionCheckActionTest {

    private final FtpConnectionCheckAction ftpConnectionCheckAction;
    {
        var actionExecutionAudit = ActionExecutionAudit.builder().actionName("FTP UPLOAD").build();
        ftpConnectionCheckAction = new FtpConnectionCheckAction(actionExecutionAudit,
                LambdaEngine.getLogger(actionExecutionAudit),
                FtpConnectionCheck.builder()
                        .name("FTP Connection Test")
                        .resultTable("onboard_wizard_info.ftp_connection_check")
                        .resourceConn("intics_zio_db_conn")
                        .condition(true)
                        .build());
    }
    @Test
    void execute() throws Exception {
        ftpConnectionCheckAction.execute();
    }
    @Test
    void checkFtpConnection() {
    }

    @Test
    void checkDirectoryAccess() {
    }
}


