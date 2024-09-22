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
                .endPoint("http://paper.itemizer.multipart.instance1:10001/multipart-download")
                .querySet("SELECT a.processed_file_path as file_path\n" +
                        "                FROM info.paper_itemizer a\n" +
                        "                WHERE a.group_id='11' and a.tenant_id = 1 and a.batch_id ='BATCH-11_0'")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("batch.processing.split.count", "2"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://paper.itemizer.multipart.instance1:10001/multipart-download"),
                Map.entry("write.batch.size", "5")));

        MultipartDownloadAction multipartDownloadAction = new MultipartDownloadAction(actionExecutionAudit, log, multipartDownload);
        multipartDownloadAction.execute();
    }
}