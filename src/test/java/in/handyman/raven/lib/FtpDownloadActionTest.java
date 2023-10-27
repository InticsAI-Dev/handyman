package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ftpdownload.FtpDownloadAction;
import in.handyman.raven.lib.model.FtpDownload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class FtpDownloadActionTest {
    @Test
    void ftpDownloadTest() throws Exception {

        FtpDownload ftpAction = FtpDownload.builder()
                .condition(true)
                .name("ftp-download")
                .resourceConn("intics_zio_db_conn")
                .resultTable("onboard_wizard_info.ftp_download_info")
                .querySet(" SELECT  'deploy@123' as password, 'ftpuser' as username, '192.168.10.245' as serverAddress,  '/home/balasoundarya.thanga@zucisystems.com/Downloads/remdir' as folderPath;")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("ftp.download.url", "https://192.168.10.245:21/home/ftpuser/test.pdf"),
                Map.entry("ftp.download.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        FtpDownloadAction ftpDownloadAction = new FtpDownloadAction(actionExecutionAudit, log, ftpAction);
        ftpDownloadAction.execute();
    }
}