package in.handyman.raven.lib;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FtpUpload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import java.util.Map;
@Slf4j
class FtpUploadActionTest {
    @Test
    public void FtpUploadTest() throws Exception {
        FtpUpload ftpUpload= FtpUpload.builder()
                .name("FtpUpload")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("onboard_wizard_info.ftp_upload_info")
                .querySet("SELECT 'deploy' as username, '192.168.10.245' as serverAddress, 'deploy@123' as password, '/Downloads' as folderPath")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("localDir","/home/balasoundarya.thanga@zucisystems.com/Downloads/remdir"),
                Map.entry("ftpPort","21"),
                Map.entry("uploadTime","2023-1-31 11:32:10.906"),
                Map.entry("tenant_id","1")));

        FtpUploadAction  ftpUploadAction=new FtpUploadAction(actionExecutionAudit,log,ftpUpload);
        ftpUploadAction.execute();
    }
}