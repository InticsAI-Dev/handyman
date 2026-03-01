package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
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
                .outputTable("sor_transform.vqa_transaction_post_processing_output")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT vtmui_field_id as vqa_id, transaction_id, created_on, created_user_id, last_updated_on, last_updated_user_id, root_pipeline_id, tenant_id,\n" +
                        "                    document_id, group_id, batch_id, origin_id, paper_no, truth_id, status, stage, message, version, extracted_image_unit,\n" +
                        "                    image_dpi, image_height, image_width, section_priority_after_filter, sor_container_id, sor_container_name, sor_container_instance,\n" +
                        "                    sor_item_name, sor_item_id, sor_item_attribution_id, model_id, model_info, model_registry, model_registry_id, answer, vqa_score,\n" +
                        "                    score, b_box, label, section_alias, synonym_id, sor_synonym, question_id, sor_question, weight, category, line_item_type,\n" +
                        "                    is_multi_entity_enabled, encryption_policy, is_encrypted::bool\n" +
                        "                    FROM sor_transform.vqa_transaction_multi_value_unifier_input a\n" +
                        "                    WHERE origin_id='ORIGIN-74';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-2014_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        action.setRootPipelineId(929L);

        MultivalueConcatenationAction multivalueConcatenationAction = new MultivalueConcatenationAction(action, log, multivalueConcatenation);
        multivalueConcatenationAction.execute();
    }
}
