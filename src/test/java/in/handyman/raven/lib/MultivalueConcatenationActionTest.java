package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueConcatenation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class MultivalueConcatenationActionTest {

    @Test
    void execute() throws Exception {
        MultivalueConcatenation multivalueConcatenation = MultivalueConcatenation.builder()
                .name("Multivalue Concatenation Action")
                .batchId("BATCH-2014_0_new")
                .groupId("2014")
                .condition(true)
                .outputTable("macro.aggregate_cleaned_result")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT DISTINCT a.tenant_id, a.aggregated_score, a.masked_score, a.origin_id, a.paper_no, a.predicted_value,\n" +
                        "                     a.rank, a.sor_item_attribution_id, a.sor_item_name, a.document_id, a.b_box, a.group_id, a.root_pipeline_id,\n" +
                        "                     a.vqa_score, a.question_id, a.synonym_id, a.model_registry, a.batch_id, a.frequency, smca.is_encrypted, smca.encryption_policy\n" +
                        "                    FROM macro.multi_value_sor_item_audit a\n" +
                        "                    JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = a.tenant_id\n" +
                        "                              AND smca.root_pipeline_id = a.root_pipeline_id\n" +
                        "                              AND smca.synonym_id = a.synonym_id\n" +
                        "                              AND smca.sor_item_name = a.sor_item_name\n" +
                        "                    WHERE a.group_id = '2014' AND a.rank = 1 AND a.tenant_id = 1 AND a.batch_id = 'BATCH-2014_0' and a.root_pipeline_id = 275569;\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-2014_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("pipeline.end.to.end.encryption", "false");
        action.setRootPipelineId(929L);

        MultivalueConcatenationAction multivalueConcatenationAction = new MultivalueConcatenationAction(action, log, multivalueConcatenation);
        multivalueConcatenationAction.execute();
    }
}
