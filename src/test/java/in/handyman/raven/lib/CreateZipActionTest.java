package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateZip;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class CreateZipActionTest {

    @Test
    void execute() throws Exception {
        CreateZip createZipAction = CreateZip.builder().name("test")
                .fileName("SYNT_166730538_c3")
                .destination("/home/dineshkumar.anandan@zucisystems.com/Documents/evaluation_output/47747/SYNT_166730538_c3")
                .source("/home/dineshkumar.anandan@zucisystems.com/Documents/evaluation_output/47747/SYNT_166730538_c3/SYNT_166730538_c3_29022024")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://localhost:8002/multipart-download"),
                Map.entry("write.batch.size", "5")));

        CreateZipAction createZipAction1 = new CreateZipAction(actionExecutionAudit, log, createZipAction);
        createZipAction1.execute();

    }
}