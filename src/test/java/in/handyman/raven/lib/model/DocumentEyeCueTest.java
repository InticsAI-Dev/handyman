package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.DocumentEyeCueAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class DocumentEyeCueTest {

    @Test
    void documentEyeCueTest() throws Exception {
        DocumentEyeCue documentEyeCue = DocumentEyeCue.builder()
                .name("Document eye cue")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("info.paper_itemizer")
                .endpoint("document.eye.cue.api.url")
                .outputDir("/data/output/")
                .querySet("  SELECT 'ORIGIN-31' as origin_id, '1' as process_id, '31' as group_id, now() as created_on, 'BTH-1' as batch_id, " +
                        "'1' as template_id, '1' as root_pipeline_id, " +
                        "'/home/dineshkumar.anandan@zucisystems.com/Downloads/110_Samples/DCN_Files/20250603T123305744.pdf' as file_path," +
                        "'1' as tenant_id\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("document.eye.cue.api.url", "http://localhost:7999/intics-copro/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("document.eye.cue.write.batch.size", "5")));

        DocumentEyeCueAction documentEyeCueAction = new DocumentEyeCueAction(actionExecutionAudit, log, documentEyeCue);
        documentEyeCueAction.execute();
    }

}