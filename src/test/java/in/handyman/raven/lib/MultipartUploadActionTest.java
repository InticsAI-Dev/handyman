package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartUpload;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class MultipartUploadActionTest {

    @Test
    void execute() throws Exception {

        MultipartUpload multipartUpload = MultipartUpload.builder()
                .name("multipart file upload for paper itemization")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endPoint("paper.itemizer.multipart.upload.url")
                .querySet("SELECT '/home/manikandan.tm@zucisystems.com/data/full_full_0_default.jpg' as filepath, " +
                        "'/data/output' as outputDir;")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("paper.itemizer.multipart.upload.url","http://copro.paper.itemizer:10001/multipart-upload"),
                Map.entry("write.batch.size","5")));

        MultipartUploadAction multipartUploadAction = new MultipartUploadAction(actionExecutionAudit, log, multipartUpload);
        multipartUploadAction.execute();
    }
}