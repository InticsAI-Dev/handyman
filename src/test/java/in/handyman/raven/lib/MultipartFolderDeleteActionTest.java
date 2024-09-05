package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartDelete;
import in.handyman.raven.lib.model.MultipartFolderDelete;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class MultipartFolderDeleteActionTest {

    @Test
    void execute() throws Exception {
        MultipartFolderDelete multipartDelete = MultipartFolderDelete.builder()
                .name("multipart file delete for paper itemization")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://192.168.10.248:10002/multipart-delete")
                .querySet("SELECT  as file_path, ar.group_id as group_id,\n" +
                        "            ar.tenant_id as tenant_id, ar.root_pipeline_id as process_id,\n" +
                        "            ar.root_pipeline_id as root_pipeline_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.delete.url", "http://127.0.0.1:8002/multipart-delete"),
                Map.entry("write.batch.size", "5")));

        MultipartFolderDeleteAction multipartFolderDeleteAction = new MultipartFolderDeleteAction(actionExecutionAudit, log, multipartDelete);
        multipartFolderDeleteAction.execute();
    }

}