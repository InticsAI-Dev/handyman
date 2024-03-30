package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EpisodeOfCoverage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class EpisodeOfCoverageActionTest {

    //This test case checked for the splitting is done by splitting , now change the
    @Test
    public void episodeOfCoverageTestEID() throws Exception {

        EpisodeOfCoverage episodeOfCoverage = EpisodeOfCoverage.builder()
                .name("Episode of coverage")
                .resourceConn("intics_zio_db_conn")
                .originId("INT-1")
                .totalPages("6")
                .outputTable("sor_grouping.eoc_result_1234567")
                .eocIdCount("1")
                .eocGroupingItem("EOC_ID")
                .groupId("1")
                .filepath(" SELECT asset.file_path,sot.origin_id,asset.file_id,sot.paper_no\n" +
                        "                             FROM info.source_of_truth sot\n" +
                        "                             JOIN info.asset asset\n" +
                        "                             ON asset.file_id=sot.preprocessed_file_id\n" +
                        "                             WHERE sot.origin_id='INT-1' ;")
                .value("    SELECT '${group_id}' AS group_id,predicted_value as answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'6' AS total_pages\n" +
                        "                            FROM score.aggregation_evaluator\n" +
                        "                            WHERE origin_id='INT-1'  and predicted_value !='' and predicted_value is not null ;")
                .qrInput(" SELECT '1' AS group_id,extracted_value AS answer, min(paper_no) AS start_no, max(paper_no) AS end_no,'${file_count.total_page}' AS total_pages\n" +
                        "                           FROM qr_extraction.qr_extraction_details\n" +
                        "                           WHERE origin_id='INT-1' AND qr_format='DATAMATRIX' AND is_candidate_qr='YES'  and extracted_value is not null and extracted_value!='' group by extracted_value ;")
                .pndValue("SELECT '1' AS group_id,string_agg(predicted_value,'-') AS answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'6' AS total_pages\n" +
                        "                            FROM score.aggregation_evaluator\n" +
                        "                            WHERE origin_id='INT-1'  and predicted_value !='' and predicted_value is not null;")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.qr-attribution.url", "http://localhost:10189/copro/qr-reader");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");
        action.getContext().put("sor.grouping.priority.selector", "true");
        action.getContext().put("gen_group_id.group_id", "1");
        action.getContext().put("sor.grouping.eid.validator", "true");
        action.getContext().put("sor.grouping.qid.validator", "true");
        action.getContext().put("sor.grouping.pid.validator", "true");
        action.getContext().put("sor.grouping.pnd.validator", "true");


        EpisodeOfCoverageAction episodeOfCoverageAction = new EpisodeOfCoverageAction(action, log, episodeOfCoverage);
        episodeOfCoverageAction.execute();

    }
            //non eoc id priority test
    @Test
    public void episodeOfCoverageTestQid() throws Exception {

        EpisodeOfCoverage episodeOfCoverage = EpisodeOfCoverage.builder()
                .name("Episode of coverage")
                .resourceConn("intics_zio_db_conn")
                .originId("ORIGIN-1")
                .totalPages("6")
                .outputTable("sor_grouping.eoc_result_1234567")
                .eocIdCount("1")
                .eocGroupingItem("EOC_ID")
                .groupId("1")
                .filepath(" SELECT asset.file_path,sot.origin_id,asset.file_id,sot.paper_no\n" +
                        "                             FROM info.source_of_truth sot\n" +
                        "                             JOIN info.asset asset\n" +
                        "                             ON asset.file_id=sot.preprocessed_file_id\n" +
                        "                             WHERE sot.origin_id='ORIGIN-1';")
                .value("    SELECT '${group_id}' AS group_id,predicted_value as answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'6' AS total_pages, batch_id\n" +
                        "                            FROM score.aggregation_evaluator\n" +
                        "                            WHERE origin_id='ORIGIN-1'  and predicted_value !='' and predicted_value is not null and batch_id = 'BATCH-1_0' ;")
                .qrInput(" SELECT '1' AS group_id,extracted_value AS answer, min(paper_no) AS start_no, max(paper_no) AS end_no,'${file_count.total_page}' AS total_pages\n" +
                        "                           FROM qr_extraction.qr_extraction_details\n" +
                        "                           WHERE origin_id='ORIGIN-1' AND qr_format='DATAMATRIX' AND is_candidate_qr='YES'  and extracted_value is not null and extracted_value!='' group by extracted_value ;")
                .pndValue("SELECT '1' AS group_id,string_agg(predicted_value,'-') AS answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'6' AS total_pages\n" +
                        "                            FROM score.aggregation_evaluator\n" +
                        "                            WHERE origin_id='ORIGIN-1'  and predicted_value !='' and predicted_value is not null;")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.qr-attribution.url", "http://localhost:10189/copro/qr-reader");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");
        action.getContext().put("sor.grouping.priority.selector", "false");
        action.getContext().put("gen_group_id.group_id", "1");
        EpisodeOfCoverageAction episodeOfCoverageAction = new EpisodeOfCoverageAction(action, log, episodeOfCoverage);
        episodeOfCoverageAction.execute();

    }


    //this test case
    @Test
    public void eocActivatorIdTest() throws Exception {

        EpisodeOfCoverage episodeOfCoverage = EpisodeOfCoverage.builder()
                .name("Episode of coverage")
                .resourceConn("intics_zio_db_conn")
                .originId("ORIGIN-1")
                .totalPages("6")
                .outputTable("sor_grouping.eoc_result_2176")
                .eocIdCount("1")
                .eocGroupingItem("EOC_ID")
                .groupId("1")
                .filepath(" SELECT asset.file_path,sot.origin_id,asset.file_id,sot.paper_no\n" +
                        "                             FROM info.source_of_truth sot\n" +
                        "                             JOIN info.asset asset\n" +
                        "                             ON asset.file_id=sot.preprocessed_file_id\n" +
                        "                             WHERE sot.origin_id='ORIGIN-1' ;")
                .value("      SELECT '1' AS group_id,predicted_value as answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'2' AS total_pages\n" +
                        "        FROM score.aggregation_evaluator\n" +
                        "        WHERE origin_id='ORIGIN-1'  and predicted_value !=''\n" +
                        "        and predicted_value is not null and tenant_id = 1;")

                .qrInput("   SELECT '1' AS group_id,extracted_value AS answer, min(paper_no) AS start_no, max(paper_no) AS end_no,'2' AS total_pages\n" +
                        "                           FROM qr_extraction.qr_extraction_details\n" +
                        "                           WHERE origin_id='ORIGIN-1'\n" +
                        "                           AND qr_format='DATAMATRIX' AND is_candidate_qr='YES'\n" +
                        "                           and extracted_value is not null and extracted_value!='' and tenant_id = 1\n" +
                        "                           group by extracted_value ;\n" +
                        "\t\t")

                .pndValue(" SELECT '1' AS group_id,string_agg(predicted_value,'-') AS answer,min(paper_no) AS start_no,max(paper_no) AS end_no ,'2' AS total_pages\n" +
                        "  FROM score.aggregation_evaluator\n" +
                        "  WHERE origin_id='ORIGIN-1'  and predicted_value !=''\n" +
                        "  and predicted_value is not null and tenant_id = 1 ;\n" +
                        "  ")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.qr-attribution.url", "http://localhost:10189/copro/qr-reader");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");
        action.getContext().put("sor.grouping.priority.selector", "true");
        action.getContext().put("gen_group_id.group_id", "1");
        action.getContext().put("sor.grouping.eid.validator", "false");
        action.getContext().put("sor.grouping.qid.validator", "false");
        action.getContext().put("sor.grouping.pid.validator", "false");
        action.getContext().put("sor.grouping.pnd.validator", "false");


        EpisodeOfCoverageAction episodeOfCoverageAction = new EpisodeOfCoverageAction(action, log, episodeOfCoverage);
        episodeOfCoverageAction.execute();

    }

}