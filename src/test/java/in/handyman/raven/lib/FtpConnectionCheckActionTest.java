package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.process.LambdaEngine;
import in.handyman.raven.lib.model.FtpConnectionCheck;
import org.junit.jupiter.api.Test;


public class FtpConnectionCheckActionTest {

    @Test
    void execute() throws Exception {

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("tenant_id", "1");
//        action.getContext().put("ftpHost", "ftp://ftp.dlptest.com");
        action.getContext().put("ftpHost", "ftp.dlptest.com");
        action.getContext().put("ftpUserName", "dlpuser");
        action.getContext().put("ftpPassword", "rNrKYTX9g7z3RgJRmxWuGHbeu");
        action.getContext().put("ftpPort", "21");
        action.getContext().put("ftpSourceDir", "/v1");

        final FtpConnectionCheckAction ftpConnectionCheckAction = new FtpConnectionCheckAction(action,
                LambdaEngine.getLogger(action),
                FtpConnectionCheck.builder()
                        .name("FTP Connection Test")
                        .resultTable("onboard_wizard_info.ftp_connection_check")
                        .resourceConn("intics_zio_db_conn_dev")
                        .condition(true)
                        .build());

        ftpConnectionCheckAction.execute();

    }

    @Test
    void checkFtpConnection() {
    }

    @Test
    void checkDirectoryAccess() {
    }
}


