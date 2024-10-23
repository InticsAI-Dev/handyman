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
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .resultTable("info.paper_itemizer")
                .endpoint("${copro.paper-itemizer.url}")
                .outputDir("/data/output/")
                .querySet("  SELECT 'ORIGIN-31' as origin_id, '31' as group_id ,'/data/input/agadia-synt-samples/BUILD-16-BATCH-May25/humana-2page/SYNT_166564144.pdf'  as file_path," +
                        "'1' as tenant_id,'temp-1' as template_id,'1234' as process_id \n" +
                        " from info.preprocess_payload_queue a \n" +
                        " join info.source_of_origin b on a.origin_id=b.origin_id  \n" +
                        " join info.asset c on b.file_id=c.file_id  \n" +
                        " where a.status='IN_PROGRESS'\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "http://192.168.10.240:8100/v2/models/paper-iterator-service/versions/1/infer"),
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
               .endpoint("http://triton.copro.paper.itemizer.instance1:8100/v2/models/paper-iterator-service/versions/1/infer")
                .resultTable("info.paper_itemizer")
                .outputDir("/data/output")
                .querySet("SELECT 'INT-1' AS origin_id, 1 AS group_id, '/data/input/agadia-synt-samples/BUILD-16-BATCH-May25/humana-2page/SYNT_166564144.pdf' AS file_path, 1 AS tenant_id, 'TMP-1' AS template_id, '138980184199100180' AS process_id, 1 as rootPipelineId \n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "http://192.168.10.240:8100/v2/models/paper-iterator-service/versions/1/infer"),
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

