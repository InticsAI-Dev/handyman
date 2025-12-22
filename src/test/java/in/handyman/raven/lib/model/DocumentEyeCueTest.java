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
                .processId("3382")
                .resultTable("doc_eyecue.doc_eyecue_pipeline_result_audit_1")
                .endpoint("document.eye.cue.api.url")
                .outputDir("/data/output/")
                .querySet("SELECT i.origin_id,\n" +
                        "i.batch_id,\n" +
                        "i.process_id,\n" +
                        "i.group_id,\n" +
                        "i.tenant_id,\n" +
                        "i.root_pipeline_id,\n" +
                        "i.input_file_path AS file_path,\n" +
                        "i.created_on,\n" +
                        "i.document_id,\n" +
                        "i.file_name\n" +
                        "FROM doc_eyecue.doc_eyecue_input_table_audit i\n" +
                        "WHERE i.group_id = 35\n" +
                        "AND i.batch_id = 'BATCH-35_0'\n" +
                        "AND i.tenant_id = 1;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("document.eye.cue.api.url", "http://localhost:8000/intics-copro/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("document.eye.cue.write.batch.size", "1"),
                Map.entry("document.eye.cue.language", "eng"),
                Map.entry("document.eye.cue.force.ocr", "false"),
                Map.entry("document.eye.cue.skip.text.pages", "true"),
                Map.entry("document.eye.cue.max.paper.count", "5"),
                Map.entry("copro.isretry.enabled", "true"),
                Map.entry("doc.eyecue.encryption", "true")
        ));
        actionExecutionAudit.setActionId(1L);

        DocumentEyeCueAction documentEyeCueAction = new DocumentEyeCueAction(actionExecutionAudit, log, documentEyeCue);
        documentEyeCueAction.execute();
    }


    @Test
    void documentEyeCueStoreContentTest() throws Exception {
        DocumentEyeCue documentEyeCue = DocumentEyeCue.builder()
                .name("Document eye cue")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("13109")
                .resultTable("doc_eyecue.doc_eyecue_pipeline_result_audit")
                .endpoint("http://localhost:8000/document-eye-cue")
                .outputDir("/data/output/")
                .querySet("SELECT i.origin_id,\n" +
                        "i.batch_id,\n" +
                        "i.process_id,\n" +
                        "i.group_id,\n" +
                        "i.tenant_id,\n" +
                        "i.root_pipeline_id,\n" +
                        "i.input_file_path AS file_path,\n" +
                        "i.created_on,\n" +
                        "i.document_id,\n" +
                        "i.file_name, gen_random_uuid() as request_id\n" +
                        "FROM doc_eyecue.doc_eyecue_input_table_audit i\n" +
                        "WHERE i.group_id = 68\n" +
                        "AND i.batch_id = 'BATCH-68_0'\n" +
                        "AND i.root_pipeline_id = 219\n" +
                        "AND i.tenant_id = 1;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("document.eye.cue.api.url", "http://localhost:8000/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("document.eye.cue.write.batch.size", "1"),
                Map.entry("document.eye.cue.language", "eng"),
                Map.entry("document.eye.cue.force.ocr", "false"),
                Map.entry("document.eye.cue.skip.text.pages", "true"),
                Map.entry("document.eye.cue.max.paper.count", "5"),
                Map.entry("doc.eyecue.encryption", "true"),

                // Apigee token configs
                Map.entry("apigee.client.id", "CLIENT_ID_HERE"),
                Map.entry("apigee.client.secret", "CLIENT_SECRET_HERE"),
                Map.entry("apigee.token.url", "http://localhost:5000/oauth/accesstoken"),

                // StoreContent configs
                Map.entry("storecontent.base.url", "http://localhost:8081"),
                Map.entry("storecontent.streaming.url", "http://localhost:8190/vulcan/mock-storecontent-controller"),
                Map.entry("storecontent.nonstreaming.url", "http://localhost:8190/vulcan/mock-storecontent-controller"),
                Map.entry("storecontent.api.key", "dummy_api_key"),
                Map.entry("doc.eyecue.storecontent.repository", "FilenetCE"),
                Map.entry("doc.eyecue.storecontent.application.id", "CUE"),
                Map.entry("doc.eyecue.storecontent.upload", "true"),
                Map.entry("doc.eyecue.file.rename.activator", "true"),

                // Metadata (storecontent meta headers)
                Map.entry("storecontent.meta.SYSID", "SYSTEM_TEST_CUE"),
                Map.entry("storecontent.meta.MEMBERCERTNUM", "MEMNUM_CUE"),
                Map.entry("storecontent.meta.ORIGRCPTDATE", "10/28/2019"),
                Map.entry("storecontent.meta.BATCHNAME", "BNAME_CUE"),
                Map.entry("storecontent.meta.STATUSDATE", "10/29/2019"),
                Map.entry("storecontent.meta.DOCUMENTTYPE", "DOCTYPE_CUE"),
                Map.entry("storecontent.meta.SRCID", "SOURCEID"),
                Map.entry("copro.isretry.enabled","true"),
                Map.entry("legacy.resource.connection.type","LEGACY"),
                Map.entry("copro.metrics.activator", "true"),
                // For backward compatibility (if required by your API)
                Map.entry("storecontent.authorization.header",
                        "")
        ));
        actionExecutionAudit.setActionId(1L);

        DocumentEyeCueAction documentEyeCueAction = new DocumentEyeCueAction(actionExecutionAudit, log, documentEyeCue);
        documentEyeCueAction.execute();
    }
}