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
                .resultTable("doc_eyecue.doc_eyecue_pipeline_result_final")
                .endpoint("document.eye.cue.api.url")
                .outputDir("/data/output/")
                .querySet("SELECT 'ORIGIN-50' AS origin_id,\n" +
                        "       '3382' AS process_id,\n" +
                        "       '44' AS group_id,\n" +
                        "       NOW() AS created_on,\n" +
                        "       'BATCH-44_0' AS batch_id,\n" +
                        "       '3382' AS root_pipeline_id,\n" +
                        "       '/data/allkeywords.pdf' AS file_path,\n" +
                        "       1 AS tenant_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("document.eye.cue.api.url", "http://192.168.10.239:7999/intics-copro/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("document.eye.cue.write.batch.size", "5"),
                Map.entry("document.eye.cue.language", "eng"),
                Map.entry("document.eye.cue.force.ocr", "false"),
                Map.entry("document.eye.cue.skip.text.pages", "true"),
                Map.entry("document.eye.cue.max.paper.count", "5"),
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
                .resultTable("doc_eyecue.doc_eyecue_pipeline_result_final")
                .endpoint("document.eye.cue.api.url")
                .outputDir("/data/output/")
                .querySet("SELECT\n" +
                        "                        i.origin_id,\n" +
                        "                        i.batch_id,\n" +
                        "                        i.process_id,\n" +
                        "                        i.group_id,\n" +
                        "                        i.tenant_id,\n" +
                        "                        i.root_pipeline_id,\n" +
                        "                       '/data/allkeywords.pdf' AS file_path,\n" +
                        "                        i.created_on,\n" +
                        "                        d.document_id,\n" +
                        "                        d.file_name\n" +
                        "                    FROM transit_data.doc_eyecue_input_table_3382 i\n" +
                        "                    join info.source_of_origin soo on i.origin_id = soo.origin_id\n" +
                        "                    LEFT JOIN inbound_config.ingestion_downloaded_file_details d\n" +
                        "                        ON soo.transaction_id  = d.transaction_id \n" +
                        "                       AND i.tenant_id = d.tenant_id\n" +
                        "                    WHERE i.group_id = 44\n" +
                        "                      AND i.batch_id = 'BATCH-44_0'\n" +
                        "                      AND i.root_pipeline_id = 3382\n" +
                        "                      AND i.tenant_id = 1;\n" +
                        "\n" +
                        "\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("document.eye.cue.api.url", "http://192.168.10.239:7999/intics-copro/document-eye-cue"),
                Map.entry("document.eye.cue.read.batch.size", "5"),
                Map.entry("document.eye.cue.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("document.eye.cue.write.batch.size", "5"),
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
                Map.entry("storecontent.streaming.url", "http://localhost:8080/storecontent/upload/streaming"),
                Map.entry("storecontent.nonstreaming.url", "http://localhost:8080/storecontent/upload"),
                Map.entry("storecontent.api.key", "dummy_api_key"),
                Map.entry("doc.eyecue.storecontent.repository", "FilenetCE"),
                Map.entry("doc.eyecue.storecontent.application.id", "CUE"),
                Map.entry("doc.eyecue.storecontent.upload", "true"),

                // Metadata (storecontent meta headers)
                Map.entry("storecontent.meta.SYSID", "SYSTEM_TEST_CUE"),
                Map.entry("storecontent.meta.MEMBERCERTNUM", "MEMNUM_CUE"),
                Map.entry("storecontent.meta.ORIGRCPTDATE", "10/28/2019"),
                Map.entry("storecontent.meta.BATCHNAME", "BNAME_CUE"),
                Map.entry("storecontent.meta.STATUSDATE", "10/29/2019"),
                Map.entry("storecontent.meta.DOCUMENTTYPE", "DOCTYPE_CUE"),
                Map.entry("storecontent.meta.SRCID", "SOURCEID"),

                // For backward compatibility (if required by your API)
                Map.entry("authorization_header",
                        "MTY4ZTVjNjQxMWZlNDZlZThhYjRhMDVmZjVhOGEzMmM6N2I5ZmVmMDcwZmMyNTJlZjdhODczYjc2YzIwY2UyMmI4MTRjODUxZTUzNjQxNTVhMTAwZjFhOTU2YWYxZDQwYg==")
        ));
        actionExecutionAudit.setActionId(1L);

        DocumentEyeCueAction documentEyeCueAction = new DocumentEyeCueAction(actionExecutionAudit, log, documentEyeCue);
        documentEyeCueAction.execute();
    }
}