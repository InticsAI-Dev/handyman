package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.HttpsDownload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class HttpsDownloadActionTest {

    @Test
    public void httpsDownload() throws Exception {
        HttpsDownload httpsDownload= HttpsDownload.builder()
                .name("httpsDownload")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("onboard_wizard_info.https_download_info")
                .querySet("")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("url","https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-zip-file.zip"),
                Map.entry("savePath","/home/balasoundarya.thanga@zucisystems.com/Downloads/sample-zip-file.zip"),
                Map.entry("userName",""),
                Map.entry("tenant_Id","1"),
                Map.entry("rootPipelineId","1"),
                Map.entry("password",""),
                Map.entry("localDestination","/home/balasoundarya.thanga@zucisystems.com/Downloads/ftpFiles")));

        HttpsDownloadAction  httpsDownloadAction=new HttpsDownloadAction(actionExecutionAudit,log,httpsDownload);
        httpsDownloadAction.execute();

    }

}