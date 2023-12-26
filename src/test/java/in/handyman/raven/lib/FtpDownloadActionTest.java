package in.handyman.raven.lib;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FtpDownload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

    @Slf4j
    class FtpDownloadActionTest {
        @Test
        public void FtpDownload() throws Exception {
            FtpDownload ftpDownload= FtpDownload.builder()
                    .name("FtpDownload")
                    .condition(true)
                    .resourceConn("intics_zio_db_conn")
                    .resultTable("onboard_wizard_info.ftp_download_info")
                    .querySet("SELECT 'deploy' as username, '192.168.10.245' as serverAddress, 'deploy@123' as password, '/Public/Upload' as folderPath")
                    .build();

            ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

            actionExecutionAudit.getContext().putAll(Map.ofEntries(
                    Map.entry("read.batch.size","1"),
                    Map.entry("ftp.upload.url","https://192.168.10.245:21/home/ftpuser/test.pdf"),
                    Map.entry("destinationPath","/home/balasoundarya.thanga@zucisystems.com/Downloads/remdir"),
                    Map.entry("ftp.download.API.count","1"),
                    Map.entry("username","deploy"),
                    Map.entry("serverAddress","192.168.10.245"),
                    Map.entry("password","deploy@123"),
                    Map.entry("folderPath","/Public/Upload/Grant Making Final Review Demo-1.pdf"),


                    Map.entry("ftpPort","21"),

                    Map.entry("uploadTime","null"),
                    Map.entry("tenant_id","1"),
                    Map.entry("write.batch.size","1")));

            FtpDownloadAction  ftpDownloadAction=new FtpDownloadAction(actionExecutionAudit,log,ftpDownload);
            ftpDownloadAction.execute();


        }

    }
