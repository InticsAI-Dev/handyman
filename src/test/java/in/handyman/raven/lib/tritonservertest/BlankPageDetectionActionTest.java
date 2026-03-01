package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.BlankPageDetectionAction;
import in.handyman.raven.lib.model.BlankPageDetection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class BlankPageDetectionActionTest {

        @Test
        void blankPageDetectionInputFromLocalPdf() throws Exception {

                BlankPageDetection blankPageDetection = BlankPageDetection.builder()
                                .name("Blank Page Detection OpenCV Test")
                                .resourceConn("intics_zio_db_conn")
                                .condition(true)
                                .processId("138980184199100180")
                                .resultTable("info.blank_page_detection")

                                .querySet(
                                                "SELECT 'ORIGIN-1' as origin_id, " +
                                                                "       1 as group_id, " +
                                                                "       '/home/prathapan.chinnan@zucisystems.com/Downloads/SmartIntake_AUMI_163SeparatedFiles/Newborn-Yes-png/20260106T102638023_39D7D755-FD69-45E2-94E9-6B8D05C8A132_20251212T191026947_DB2F4F30-4B6A-4C9E-98EA-26AC8B284C2A_ct_8882460226_COMM_202507300837046_NICU1_2.png' as file_path, "
                                                                +
                                                                "       1 as tenant_id, " +
                                                                "       '12345' as process_id, " +
                                                                "       1110 as root_pipeline_id, " +
                                                                "       'BATCH-1' as batch_id, " +
                                                                "       'png' as file_extension, " +
                                                                "       now() as created_on;")
                                .build();

                ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
                actionExecutionAudit.setActionId(1L);

                actionExecutionAudit.getContext().putAll(
                                Map.ofEntries(
                                                Map.entry("gen_group_id.group_id", "1"),
                                                Map.entry("write.batch.size", "5"),
                                                Map.entry("read.batch.size", "5"),
                                                Map.entry("pipeline.copro.api.process.file.format", "FILE"),
                                                Map.entry("actionId", "1")));

                BlankPageDetectionAction blankPageDetectionAction = new BlankPageDetectionAction(actionExecutionAudit,
                                log, blankPageDetection);

                long startTime = System.currentTimeMillis();

                blankPageDetectionAction.execute();

                long endTime = System.currentTimeMillis();

                System.out.println(
                                "BlankPageDetection Execution Time: " + (endTime - startTime) + " ms");
        }

        @Test
        void blankPageDetectionInputFromDb() throws Exception {

                BlankPageDetection blankPageDetection = BlankPageDetection.builder()
                                .name("Blank Page Detection Input From DB")
                                .resourceConn("intics_zio_db_conn")
                                .condition(true)
                                .processId("138980184199100180")
                                .resultTable("transit_data.blank_page_detection_12001")

                                .querySet(
                                                "SELECT a.origin_id, a.group_id ,p.processed_file_path,b.tenant_id,a.producer_process_id as process_id,a.root_pipeline_id, a.batch_id, now() as created_on, c.file_extension\n"
                                                                +
                                                                "from preprocess.preprocess_payload_queue_archive a\n" +
                                                                "join info.source_of_origin b on a.origin_id=b.origin_id and a.tenant_id=b.tenant_id\n"
                                                                +
                                                                "join info.asset c on b.file_id=c.file_id\n" +
                                                                "join info.paper_itemizer p on a.origin_id=p.origin_id and a.tenant_id=p.tenant_id\n"
                                                                +
                                                                "where a.batch_id ='BATCH-78_0' and a.group_id='78' and  a.tenant_id = 1;\n")
                                .build();

                ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
                actionExecutionAudit.setActionId(2L);

                actionExecutionAudit.getContext().putAll(
                                Map.ofEntries(
                                                Map.entry("gen_group_id.group_id", "1"),
                                                Map.entry("write.batch.size", "5"),
                                                Map.entry("read.batch.size", "5"),
                                                Map.entry("pipeline.copro.api.process.file.format", "FILE"),
                                                Map.entry("actionId", "1")));

                BlankPageDetectionAction blankPageDetectionAction = new BlankPageDetectionAction(actionExecutionAudit,
                                log, blankPageDetection);

                long startTime = System.currentTimeMillis();

                blankPageDetectionAction.execute();

                long endTime = System.currentTimeMillis();

                System.out.println(
                                "BlankPageDetection DB Execution Time: " + (endTime - startTime) + " ms");
        }
}