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
                .endPoint("http://127.0.0.1:8002/multipart-upload")
                .querySet("SELECT '/home/dineshkumar.anandan@zucisystems.com/Downloads/54.jpg' as filepath, " +
                        "'/data/output' as outputDir, 'ORIGIN-1' as originId, 1 as paperNo, 1 as groupId, 1 as tenantId, " +
                        "'tmp-1' as templateId, 1112 as processId, 52731 as rootPipelineId;")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("paper.itemizer.multipart.upload.url","http://127.0.0.1:8002/multipart-upload"),
                Map.entry("write.batch.size","5")));

        MultipartUploadAction multipartUploadAction = new MultipartUploadAction(actionExecutionAudit, log, multipartUpload);
        multipartUploadAction.execute();
    }
}