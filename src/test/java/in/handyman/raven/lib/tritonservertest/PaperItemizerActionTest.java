package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PaperItemizerAction;
import in.handyman.raven.lib.model.PaperItemizer;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerInputTable;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerOutputTable;
import in.handyman.raven.lib.model.paperitemizer.PdfToPaperItemizer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

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
                .outputDir("/home/manikandan.tm@zucisystems.com/data/Anthem/bucketing/112-files-buketing/pdf-2-img-source")
                .querySet(" SELECT 'ORIGIN-1' as origin_id, 1 as group_id ,'/home/manikandan.tm@zucisystems.com/data/Anthem/bucketing/112-files-buketing/pdf-2-img-source/20250603T123635573.pdf' as file_path,1 as tenant_id,'12345' as process_id, 1110 as root_pipeline_id, 'BATCH-1' as batch_id, now() as created_on;")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("copro.paper-itemizer.url", "https://9fc26c9f2d6f.ngrok.app/paper-iterator/v2/models/paper-iterator-service/versions/1/infer"),
                Map.entry("gen_group_id.group_id", "10"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("paper.itemizer.consumer.API.count", "30"),
                Map.entry("pipeline.copro.api.process.file.format", "BASE64"),
                Map.entry("paper.itemizer.model.name", "XENON"),
                Map.entry("actionId", "1"),
                Map.entry("paper.itemization.using.app", "true"),
                Map.entry("paper.itemizer.resize.width", "1700"),
                Map.entry("paper.itemizer.resize.height", "2200"),
                Map.entry("read.batch.size", "5"),
                Map.entry("paper.itemizer.file.dpi", "200"),
                Map.entry("paper.itemizer.output.format", "jpg"),
                Map.entry("paper.itemizer.image.type.rgb", "true"),
                Map.entry("extraction.preprocess.paper.itemizer.processing.paper.count", "5"),
                Map.entry("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "true"),
                Map.entry("paper.itemization.resize.activator", "false"),
                Map.entry("copro.processor.thread.creator", "VIRTUAL_THREAD"),
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

    @Test
    void testPdfToPaperItemizerWithExecutorService() {
        String inputFilePath = "/home/manikandan.tm@zucisystems.com/data/Anthem/bucketing/194-files";
        int nThreads = 20;
        List<PaperItemizerOutputTable> paperItemizerOutputTables = new ArrayList<>();
        List<PaperItemizerInputTable> paperItemizerInputTables = generateInputTablesFromFolder(inputFilePath);

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("paper.itemizer.image.type.rgb", "true");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.count", "100");
        actionExecutionAudit.getContext().put("extraction.preprocess.paper.itemizer.processing.paper.limiter.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.width", "2550");
        actionExecutionAudit.getContext().put("paper.itemizer.resize.height", "3301");
        actionExecutionAudit.getContext().put("paper.itemizer.output.format", "jpg");
        actionExecutionAudit.getContext().put("paper.itemizer.file.dpi", "300");
        actionExecutionAudit.getContext().put("paper.itemization.resize.activator", "false");
        actionExecutionAudit.getContext().put("paper.itemizer.consumer.API.count", "10");
        actionExecutionAudit.getContext().put("document_type", "UM_FAX");

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads); // Adjust the pool size as needed
        List<Future<List<PaperItemizerOutputTable>>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (PaperItemizerInputTable paperItemizerInputTable : paperItemizerInputTables) {
            futures.add(executorService.submit(() -> {
                PdfToPaperItemizer pdfToPaperItemizer = new PdfToPaperItemizer(actionExecutionAudit, log);
                return pdfToPaperItemizer.paperItemizer(paperItemizerInputTable.getFilePath(), paperItemizerInputTable.getOutputDir(), paperItemizerInputTable);
            }));
        }

        futures.forEach(future -> {
            try {
                List<PaperItemizerOutputTable> pdfItemizedOutputs = future.get();
                paperItemizerOutputTables.addAll(pdfItemizedOutputs);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Convert milliseconds to minutes and seconds
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTime) - TimeUnit.MINUTES.toSeconds(minutes);

        // Print results
        System.out.println("Total input files: " + paperItemizerInputTables.size());
        System.out.println("Total output files: " + paperItemizerOutputTables.size());
        System.out.println("Total execution time: " + minutes + " mins " + seconds + " secs");
    }


    public static List<PaperItemizerInputTable> generateInputTablesFromFolder(String folderPath) {
        List<PaperItemizerInputTable> inputTables = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        long processId = System.currentTimeMillis();
        int groupId = 1;
        long tenantId = 100L;
        String templateId = UUID.randomUUID().toString();
        String outputDir = folderPath + File.separator + "output";
        long rootPipelineId = processId + 1000;
        String batchId = "batch-1";

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    PaperItemizerInputTable table = PaperItemizerInputTable.builder()
                            .originId(UUID.randomUUID().toString())
                            .processId(processId)
                            .groupId(groupId)
                            .tenantId(tenantId)
                            .templateId(templateId)
                            .filePath(file.getAbsolutePath())
                            .outputDir(outputDir)
                            .rootPipelineId(rootPipelineId)
                            .batchId(batchId)
                            .createdOn(new Timestamp(System.currentTimeMillis()))
                            .build();
                    inputTables.add(table);
                    groupId++;
                }
            }
        }
        return inputTables;
    }
}

