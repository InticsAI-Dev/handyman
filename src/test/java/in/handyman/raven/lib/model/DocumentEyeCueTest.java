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
                .processId("13109")
                .resultTable("doc_eyecue.doc_eyecue_pipeline_result_final")
                .endpoint("document.eye.cue.api.url")
                .outputDir("/data/output/")
                .querySet("SELECT 'ORIGIN-117' AS origin_id,\n" +
                        "       '13109' AS process_id,\n" +
                        "       '74' AS group_id,\n" +
                        "       NOW() AS created_on,\n" +
                        "       'BATCH-74_0' AS batch_id,\n" +
                        "       '13109' AS root_pipeline_id,\n" +
                        "       '/data/allkeywords.pdf' AS file_path,\n" +
                        "       1 AS tenant_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("document.eye.cue.api.url", "http://192.168.10.238:7999/intics-copro/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("document.eye.cue.write.batch.size", "5"),
                Map.entry("document.eye.cue.language", "eng"),
                Map.entry("document.eye.cue.force.ocr", "false"),
                Map.entry("document.eye.cue.skip.text.pages", "true"),
                Map.entry("document.eye.cue.max.paper.count", "5")
        ));
        actionExecutionAudit.setActionId(1L);

        DocumentEyeCueAction documentEyeCueAction = new DocumentEyeCueAction(actionExecutionAudit, log, documentEyeCue);
        documentEyeCueAction.execute();
    }
}