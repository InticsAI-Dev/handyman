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
    void paperItemizerInputFromLocal() throws Exception {
        PaperItemizer paperItemizer = PaperItemizer.builder()
                .name("paper itemizer macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .endpoint("https://9fc26c9f2d6f.ngrok.app/paper-iterator/v2/models/paper-iterator-service/versions/1/infer")
                .resultTable("info.paper_itemizer")
                .outputDir("/data/tenant/PI")
                .querySet(" SELECT 'ORIGIN-1' as origin_id, 1 as group_id ,'/home/anandh.andrews@zucisystems.com/intics-workspace/Asgard/ANTHEM-docs/AUMI/build/testing/paper-itemizer/merged_sample_dnu.pdf' as file_path,1 as tenant_id,'12345' as process_id, 1110 as root_pipeline_id, 'BATCH-1' as batch_id, now() as created_on;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "https://9fc26c9f2d6f.ngrok.app/paper-iterator/v2/models/paper-iterator-service/versions/1/infer"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("paper.itemizer.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("paper.itemizer.model.name", "XENON"),
                Map.entry("actionId", "1"),
                Map.entry("paper.itemization.using.app", "true"),
                Map.entry("paper.itemizer.resize.width", "1700"),
                Map.entry("paper.itemizer.resize.height", "2200"),
                Map.entry("read.batch.size", "5"),
                Map.entry("paper.itemizer.file.dpi", "200"),
                Map.entry("paper.itemizer.output.format", "png"),
                Map.entry("paper.itemizer.image.type.rgb", "true"),
                Map.entry("paper.itemization.resize.activator", "false"),
                Map.entry("write.batch.size", "5")));

        PaperItemizerAction paperItemizerAction = new PaperItemizerAction(actionExecutionAudit, log, paperItemizer);
        long startTime = System.currentTimeMillis();

        paperItemizerAction.execute();
        long endTime = System.currentTimeMillis();
        long totalTimeMillis = endTime - startTime;
        System.out.println("Execution time: " + totalTimeMillis + " ms");

    }


    @Test
    void paperItemizerInputFromDb() throws Exception {
        PaperItemizer paperItemizer = PaperItemizer.builder()
                .name("paper itemizer macro test after copro optimization")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .processId("138980184199100180")
                .endpoint("https://9fc26c9f2d6f.ngrok.app/paper-iterator/v2/models/paper-iterator-service/versions/1/infer")
                .resultTable("info.paper_itemizer")
                .outputDir("/data/tenant/PI")
                .querySet(" SELECT a.origin_id, a.group_id ,'/data/input/Hand Written_Final_8778811305.pdf' as file_path,b.tenant_id,a.producer_process_id as process_id, 1110 as root_pipeline_id, a.batch_id, now() as created_on\n" +
                        "from preprocess.preprocess_payload_queue_archive a\n" +
                        "join info.source_of_origin b on a.origin_id=b.origin_id and a.tenant_id=b.tenant_id\n" +
                        "join info.asset c on b.file_id=c.file_id\n" +
                        "where a.root_pipeline_id in (64255)")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "https://9fc26c9f2d6f.ngrok.app/paper-iterator/v2/models/paper-iterator-service/versions/1/infer"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("paper.itemizer.consumer.API.count", "1"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("paper.itemizer.model.name", "XENON"),
                Map.entry("actionId", "1"),
                Map.entry("paper.itemization.using.app", "true"),
                Map.entry("paper.itemizer.resize.width", "1700"),
                Map.entry("paper.itemizer.resize.height", "2200"),
                Map.entry("read.batch.size", "5"),
                Map.entry("paper.itemizer.file.dpi", "200"),
                Map.entry("paper.itemizer.output.format", "png"),
                Map.entry("paper.itemizer.image.type.rgb", "true"),
                Map.entry("extraction.preprocess.paper.itemizer.processing.paper.count", "5"),
                Map.entry("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "true"),
                Map.entry("paper.itemization.resize.activator", "false"),
                Map.entry("copro.processor.thread.creator", "FIXED_THREAD"),
                Map.entry("write.batch.size", "5")));


        PaperItemizerAction paperItemizerAction = new PaperItemizerAction(actionExecutionAudit, log, paperItemizer);
        long startTime = System.currentTimeMillis();

        paperItemizerAction.execute();
        long endTime = System.currentTimeMillis();
        long totalTimeMillis = endTime - startTime;
        System.out.println("Execution time: " + totalTimeMillis + " ms");

    }
}

