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
                .endPoint("http://0.0.0.0:10001/copro-utils/file-operations/multipart-upload")
                .querySet("SELECT '/data/input/batch_id/docker_bind/SYNT_166564144.pdf' as file_path, ar.template_id as template_id,\n" +
                        "ar.origin_id as origin_id, ar.paper_no as paper_no, ar.group_id as group_id,\n" +
                        "ar.tenant_id as tenant_id, ar.process_id as process_id,\n" +
                        "ar.root_pipeline_id as root_pipeline_id, ar.batch_id, '/data/input/batch_id/docker_bind' as output_dir\n" +
                        "FROM info.auto_rotation ar\n" +
                        "WHERE ar.status ='COMPLETED' AND ar.group_id='312' AND ar.tenant_id=1 and ar.batch_id='BATCH-312_0' limit 1;")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","5"),
                Map.entry("gen_group_id.group_id","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("batch.processing.split.count","1"),
                Map.entry("paper.itemizer.multipart.upload.url","http://copro.paper.itemizer:10001/multipart-upload"),
                Map.entry("write.batch.size","5")));

        MultipartUploadAction multipartUploadAction = new MultipartUploadAction(actionExecutionAudit, log, multipartUpload);
        multipartUploadAction.execute();
    }
}