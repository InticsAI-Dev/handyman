package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartDelete;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class MultipartDeleteActionTest {

    @Test
    void execute() throws Exception {
        MultipartDelete multipartDelete = MultipartDelete.builder()
                .name("multipart file delete for paper itemization")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://localhost:7999/file-operations/multipart-delete")
                .querySet("SELECT '/home/dineshkumar.anandan@zucisystems.com/Downloads/test/Weather API.pdf' as filepath, " +
                        "'ORIGIN-1' as originId, 1 as paperNo, 1 as groupId, 1 as tenantId, " +
                        "'tmp-1' as templateId, 1112 as processId, 52732 as rootPipelineId, 'batch_1' as batch_id;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.delete.url", "http://localhost:7999/file-operations/multipart-delete"),
                Map.entry("write.batch.size", "5")));

        MultipartDeleteAction multipartUploadAction = new MultipartDeleteAction(actionExecutionAudit, log, multipartDelete);
        multipartUploadAction.execute();
    }

}