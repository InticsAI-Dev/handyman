package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueSorItemHandling;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class multiValueSorItemHandlingTest {

    @Test
    void execute() throws Exception {
        MultivalueSorItemHandling multivalueSorItemHandling = MultivalueSorItemHandling.builder()
                .name("Multivalue Concatenation Action")
                .condition(true)
                .outputTable("entity_voting.sor_item_multivalue_filtering_output")
                .resourceConn("intics_zio_db_conn")
                .querySet("select vqa.created_on, vqa.created_user_id, vqa.last_updated_on, vqa.last_updated_user_id, vqa.status, \n" +
                        "vqa.version, vqa.answer, vqa.b_box, vqa.document_id, vqa.extracted_image_unit, vqa.group_id, vqa.image_dpi,\n" +
                        "vqa.image_height, vqa.image_width, vqa.model_id, vqa.model_info, vqa.origin_id, vqa.paper_no,\n" +
                        "vqa.question_id, vqa.root_pipeline_id, vqa.score, vqa.sor_item_attribution_id, vqa.sor_item_name, \n" +
                        "vqa.sor_question, vqa.synonym_id, vqa.tenant_id, vqa.vqa_score, vqa.weight, vqa.model_registry,\n" +
                        "vqa.category, vqa.model_registry_id, vqa.stage, vqa.batch_id, vqa.line_item_type, vqa.is_encrypted,\n" +
                        "vqa.encryption_policy_id\n" +
                        "from sor_transaction.vqa_transaction vqa\n" +
                        "where vqa.line_item_type = 'multi_value';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-2014_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
//        action.setRootPipelineId(929L);

        MultivalueSorItemHandlingAction multivalueSorItemHandlingAction = new MultivalueSorItemHandlingAction(action, log, multivalueSorItemHandling);
        multivalueSorItemHandlingAction.execute();
    }
}
