package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FolderDeleteByProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class FolderDeleteByProcessActionTest {

    @Test
    void execute() throws Exception {
        FolderDeleteByProcess multipartDelete = FolderDeleteByProcess.builder()
                .name("multipart file delete for paper itemization")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .cleanupProcessName("FILE_PROCESS,PREPROCESS,DENOISE,RESPONSE_ARTIFACT")
                .querySet("SELECT ar.current_directory as current_directory_path, ar.group_id as group_id,\n" +
                        "ar.tenant_id as tenant_id, ar.created_on,\n" +
                        "ar.root_pipeline_id as root_pipeline_id, ar.process_name\n" +
                        "from ingestion.pipeline_directory_info ar\n" +
                        "where ar.tenant_id = 1 and status = 'STAGED' and ar.root_pipeline_id IN (86)")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.delete.url", "http://127.0.0.1:8002/multipart-delete"),
                Map.entry("write.batch.size", "5")));

        FolderDeleteByProcessAction folderDeleteByProcessAction = new FolderDeleteByProcessAction(actionExecutionAudit, log, multipartDelete);
        folderDeleteByProcessAction.execute();
    }
}