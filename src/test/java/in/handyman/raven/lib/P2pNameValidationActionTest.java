package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.P2pNameValidation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class P2pNameValidationActionTest {

    @Test
    void execute() throws Exception {

        P2pNameValidation p2pNameValidation = P2pNameValidation.builder()
                .name("p2p name concatenation action")
                .processId("123456")
                .outputTable("voting.p2p_result_temp_table_123")
                .querySet("SELECT\n" +
                        "patient_first_name_b_box AS p2p_first_name_bBox,\n" +
                        "patient_last_name_b_box AS p2p_last_name_bBox,\n" +
                        "patient_name_b_box AS p2p_full_name_bBox,\n" +
                        "patient_first_name_confidence_score AS p2p_first_name_confidence_score,\n" +
                        "patient_last_name_confidence_score AS p2p_last_name_confidence_score,\n" +
                        "patient_name_confidence_score AS p2p_full_name_confidence_score,\n" +
                        "patient_first_name AS p2p_first_name,\n" +
                        "patient_last_name AS p2p_last_name,\n" +
                        "patient_name AS p2p_full_name,\n" +
                        "group_id AS group_id,\n" +
                        "patient_first_name_maximum_score AS p2p_first_name_maximum_score,\n" +
                        "patient_last_name_maximum_score AS p2p_last_name_maximum_score,\n" +
                        "patient_name_maximum_score AS p2p_full_name_maximum_score,\n" +
                        "origin_id AS origin_id,\n" +
                        "paper_no AS paper_no,\n" +
                        "root_pipeline_id AS root_pipeline_id,\n" +
                        "tenant_id AS tenant_id,\n" +
                        "'patient_name' AS sor_item_name,\n" +
                        "    question_id AS question_id,\n" +
                        "    first_name_question_id AS first_name_question_id,\n" +
                        "    last_name_question_id AS last_name_question_id,\n" +
                        "    synonym_id AS synonym_id,\n" +
                        "    first_name_synonym_id AS first_name_synonym_id,\n" +
                        "    last_name_synonym_id AS last_name_synonym_id,\n" +
                        "    model_registry AS model_registry,\n" +
                        "    batch_id\n" +
                        "FROM\n" +
                        "    voting.cumulative_patient_name_62539\n" +
                        "WHERE\n" +
                        "    origin_id = 'ORIGIN-58' and batch_id='BATCH-39_0';")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.getContext().put("p2p.url", "http://localhost:10181/copro/preprocess/autorotation");
        actionExecutionAudit.getContext().put("p2p.output.table", "cumulative_result_p2p_result_table_1");
        actionExecutionAudit.getContext().put("p2p.schema.name", "voting");
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("p2p.name.concat.consumer.API.count", "1"),
                Map.entry("root_pipeline_id", "1"),
                Map.entry("action_id", "100"),
                Map.entry("write.batch.size", "1"))
        );
        P2pNameValidationAction p2pNameValidationAction = new P2pNameValidationAction(actionExecutionAudit, log, p2pNameValidation);
        p2pNameValidationAction.execute();
    }

    @Test
    public void testExactMatch() {
        String finalConcatenatedName = "dinesh kumar";
        String p2pFullName = "dinesh kumar";
        boolean isContains = finalConcatenatedName.equalsIgnoreCase(p2pFullName) && finalConcatenatedName.contains(p2pFullName);
        System.out.println(isContains);
        assertTrue(isContains);
    }
}