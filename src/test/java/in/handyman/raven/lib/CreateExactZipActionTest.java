package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateExactZip;
import in.handyman.raven.lib.model.CreateZip;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CreateExactZipActionTest {



    @Test
    void execute() throws Exception {
        CreateExactZip createZipAction = CreateExactZip.builder()
                .name("test")
                .fileName("pdf_to_image.zip")
                .destination("/home/anandh.andrews@zucisystems.com/intics-workspace/testing/database-backup")
                .source("/home/anandh.andrews@zucisystems.com/intics-workspace/Demo/BUILD-16-BATCH-May25/5-samples/SYNT_166522063.pdf")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://localhost:8002/multipart-download"),
                Map.entry("write.batch.size", "5")));

        CreateExactZipAction createExactZipAction = new CreateExactZipAction(actionExecutionAudit, log, createZipAction);
        createExactZipAction.execute();

    }
}