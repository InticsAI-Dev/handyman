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
                .endPoint("http://192.168.10.248:10001/multipart-download")
                .querySet("SELECT '/data/output/processed_images/07-10-2024_03_10_41/tenant_1/group_18/preprocess/paper_itemizer/pdf_to_image/SYNT_166564144_c1/SYNT_166564144_c1_1.jpg' as filepath")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://192.168.10.248:10001/multipart-download"),
                Map.entry("write.batch.size", "5")));

        MultipartDownloadAction multipartDownloadAction = new MultipartDownloadAction(actionExecutionAudit, log, multipartDownload);
        multipartDownloadAction.execute();
    }
}