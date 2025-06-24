package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PostProcessingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class PostProcessingExecutorActionTest {

    @Test
    void execute() throws Exception {

        PostProcessingExecutor postProcessingExecutor = PostProcessingExecutor.builder()
                .name("Post Processing executor")
                .batchId("BATCH-22_0")
                .groupId("22")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn_hera")
                .querySet("SELECT\n" +
                        "                                            a.tenant_id as tenant_id,\n" +
                        "                                            a.confidence_score as aggregated_score,\n" +
                        "                                            a.weight_score as masked_score,\n" +
                        "                                            a.origin_id,\n" +
                        "                                            a.paper_no,\n" +
                        "                                            a.extracted_value,\n" +
                        "                                            b.vqa_score,\n" +
                        "                                            a.weight_rank as rank,\n" +
                        "                                            smca.sor_item_attribution_id as sor_item_attribution_id,\n" +
                        "                                            a.sor_item_name,\n" +
                        "                                            '1' as documentId,\n" +
                        "                                            1 as acc_transaction_id,\n" +
                        "                                            b.b_box,\n" +
                        "                                            a.root_pipeline_id as root_pipeline_id,\n" +
                        "                                            a.freq as frequency,\n" +
                        "                                            b.question_id,\n" +
                        "                                            b.synonym_id,\n" +
                        "                                            a.model_registry,\n" +
                        "                                            smca.is_encrypted,\n" +
                        "                                            smca.encryption_policy\n" +
                        "                                          FROM\n" +
                        "                                            voting.weighted_value a\n" +
                        "                                            JOIN score.scalar_validation b ON a.validation_id = b.validation_id\n" +
                        "                                            JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = b.tenant_id\n" +
                        "                                                            AND smca.root_pipeline_id = b.root_pipeline_id\n" +
                        "                                                            AND smca.synonym_id = b.synonym_id\n" +
                        "                                                            AND smca.sor_item_name = b.sor_item_name\n" +
                        "                                            JOIN sor_transaction.sor_transaction_payload_queue_archive st ON st.origin_id = a.origin_id\n" +
                        "                                                            AND st.batch_id = a.batch_id\n" +
                        "                                                            AND st.tenant_id = b.tenant_id\n" +
                        "                        WHERE a.origin_id ='ORIGIN-8736';\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "22");
        action.getContext().put("batch_id", "BATCH-22_0");
        action.getContext().put("created_user_id", "1");
                action.getContext().put("outbound.mapper.bsh.class.order", "MedicaidMemberIdValidator");
        action.getContext().put("pipeline.end.to.end.encryption", "false");
        action.getContext().put("MedicaidMemberIdValidator", "");

        action.setRootPipelineId(929L);

        PostProcessingExecutorAction postProcessingExecutorAction = new PostProcessingExecutorAction(action, log, postProcessingExecutor);
        postProcessingExecutorAction.execute();
    }
}