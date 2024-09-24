package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartDownload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class MultipartDownloadActionTest {

    @Test
    void execute() throws Exception {
        MultipartDownload multipartDownload = MultipartDownload.builder()
                .name("octet stream file download for paper itemization")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://localhost:8002/multipart-download")
                .querySet("SELECT '/home/manikandan.tm@zucisystems.com/Downloads/New_york_driver_license (1).jpg' as filepath")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://localhost:8002/multipart-download"),
                Map.entry("write.batch.size", "5")));

        MultipartDownloadAction multipartDownloadAction = new MultipartDownloadAction(actionExecutionAudit, log, multipartDownload);
        multipartDownloadAction.execute();
    }
}