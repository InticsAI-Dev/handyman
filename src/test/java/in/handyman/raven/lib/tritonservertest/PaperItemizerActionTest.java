package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PaperItemizerAction;
import in.handyman.raven.lib.model.PaperItemizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class PaperItemizerActionTest {

    @Test
    void paperItemizerTest() throws Exception {
        PaperItemizer paperItemizer = PaperItemizer.builder()
                .name("Data extraction macro test after copro optimization")
                .resourceConn("intics_agadia_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("info.")
                .endpoint("${copro.paper-itemizer.url}")
                .outputDir("/home/anandh.andrews@zucisystems.com/W-space/pr1-lambdas/agadia/output/")
                .querySet("  SELECT a.origin_id, a.group_id ,c.file_path,b.tenant_id,b.document_id as template_id,a.producer_process_id as process_id \n" +
                        " from info.preprocess_payload_queue a \n" +
                        " join info.source_of_origin b on a.origin_id=b.origin_id  \n" +
                        " join info.asset c on b.file_id=c.file_id  \n" +
                        " where a.status='IN_PROGRESS'\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "http://localhost:10181/copro/denoise/paperiterator"),
                Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "5")));

        PaperItemizerAction paperItemizerAction = new PaperItemizerAction(actionExecutionAudit, log, paperItemizer);
        paperItemizerAction.execute();
    }


    @Test
    void tritonServer() throws Exception {
        PaperItemizer paperItemizer = PaperItemizer.builder()
                .name("paper itemizer macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("138980184199100180")
               .endpoint("http://192.168.10.248:8100/v2/models/paper-iterator-service/versions/1/infer")
                .resultTable("info.paper_itemizer")
                .outputDir("/data/output")
                .querySet("SELECT 'INT-1' AS origin_id, 1 AS group_id," +
                        " '/data/multipart-files/data/1/transaction/TRZ-211/139465056086000223/SYNT_166730538_c6.pdf' AS file_path" +
                        ", 1 AS tenant_id, 'TMP-1' AS template_id, '138980184199100180' AS process_id, 1 as rootPipelineId \n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "http://192.168.10.248:8100/v2/models/paper-iterator-service/versions/1/infer"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("paper.itemizer.consumer.API.count", "1"),
                Map.entry("actionId", "1"),
                Map.entry("read.batch.size", "5"),
                Map.entry("write.batch.size", "5")));

        PaperItemizerAction paperItemizerAction = new PaperItemizerAction(actionExecutionAudit, log, paperItemizer);
        paperItemizerAction.execute();
    }
}

