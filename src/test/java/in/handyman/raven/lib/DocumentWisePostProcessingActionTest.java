package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocumentWisePostProcessing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_SELECT_READ_BATCH_SIZE;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DocumentWisePostProcessingActionTest {

    @Test
    void execute() throws Exception {

        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(true)
                .name("transform into document-wise-post-processing output table")
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transform.document_wise_post_processing_output_audit")
                .batchId("BATCH-16_0")
                .querySet("SELECT " +
                        "document_wise_post_processing_input_id, dwp.transaction_id, dwp.created_on, " +
                        "dwp.created_user_id, dwp.last_updated_on, dwp.last_updated_user_id, " +
                                "dwp.version, dwp.feature, dwp.label, dwp.left_pos, " +
                        "dwp.lower_pos, dwp.right_pos, dwp.upper_pos, dwp.b_box, dwp.precision_val, " +
                        "dwp.predicted_value, dwp.section_alias, dwp.sor_container_instance, " +
                        "dwp.document_id, dwp.truth_id, dwp.channel_id, dwp.group_id, " +
                        "dwp.origin_id, dwp.paper_no, dwp.question_id, dwp.root_pipeline_id, " +
                        "dwp.score, dwp.sor_item_name, dwp.sor_question, dwp.synonym_id, " +
                        "dwp.tenant_id, dwp.vqa_score, dwp.category, dwp.stage, dwp.batch_id, " +
                        "dwp.line_item_type, dwp.is_encrypted, dwp.encryption_policy, " +
                        "dwp.is_removed_after_filtering, dwp.message, dwp.sor_container_id, " +
                        "dwp.truth_entity_id, dwp.sor_item_id, dwp.is_multi_entity_enabled " +
                        "FROM sor_transform.document_wise_post_processing_input_audit dwp " +
                        "WHERE dwp.tenant_id = 1 " +
                        "AND dwp.batch_id = 'BATCH-16_0' " +
                        "AND dwp.group_id = 16 " +
                        "LIMIT 10")
                .build();

        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setProcessId(12345L);
        action.getContext().put("document.wise.post.processing.activator", "true");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        action.getContext().put("document.wise.post.processing.thread.count", "5");
        action.getContext().put(DB_SELECT_READ_BATCH_SIZE, "10");
        action.getContext().put(DB_INSERT_WRITE_BATCH_SIZE, "100");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("temp_schema_name", "transist_data");
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "16");
        action.getContext().put("batch_id", "BATCH-16_0");
        action.getContext().put("init_process_id.process_id", "12345");
        action.getContext().put("document.wise.executor.bsh.class.order", "TestValidator");
        action.getContext().put("TestValidator", "import in.handyman.raven.lib.model.DocumentWisePostProcessingInput; " +
                "import java.util.List; " +
                "public class TestValidator { " +
                "public List<DocumentWisePostProcessingInput> doCustomPredictionMapping(List<DocumentWisePostProcessingInput> inputs, Long rootPipelineId) { " +
                "return inputs; " +
                "} " +
                "}");
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url", encryptionUrl);
        action.getContext().put("protegrity.dec.api.url", decryptionUrl);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        documentWisePostProcessingAction.execute();
    }

    @Test
    void testExecuteIf_ReturnsTrue() throws Exception {
        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(true)
                .name("Test DocumentWisePostProcessing")
                .resourceConn("ibm_mar12")
                .outputTable("sor_transaction.document_wise_post_processing_output")
                .querySet("SELECT * FROM test.document_wise_post_processing_input LIMIT 1")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        assertTrue(documentWisePostProcessingAction.executeIf());
    }

    @Test
    void testExecuteIf_ReturnsFalse() throws Exception {
        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(false)
                .name("Test DocumentWisePostProcessing")
                .resourceConn("ibm_mar12")
                .outputTable("sor_transaction.document_wise_post_processing_output")
                .querySet("SELECT * FROM test.document_wise_post_processing_input LIMIT 1")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        assertFalse(documentWisePostProcessingAction.executeIf());
    }

    @Test
    void testExecuteIf_WithNullCondition() throws Exception {
        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(null)
                .name("Test DocumentWisePostProcessing")
                .resourceConn("ibm_mar12")
                .outputTable("sor_transaction.document_wise_post_processing_output")
                .querySet("SELECT * FROM test.document_wise_post_processing_input LIMIT 1")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        assertThrows(NullPointerException.class, documentWisePostProcessingAction::executeIf);
    }

    @Test
    void testExecute_WithEncryptionDisabled() throws Exception {
        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(true)
                .name("Test DocumentWisePostProcessing with encryption disabled")
                .resourceConn("ibm_mar12")
                .outputTable("sor_transform.document_wise_post_processing_output_audit")
                .batchId("BATCH-16_0")
                .querySet("SELECT " +
                        "document_wise_post_processing_input_id, dwp.transaction_id, dwp.created_on, " +
                        "dwp.created_user_id, dwp.last_updated_on, dwp.last_updated_user_id, " +
                        "dwp.status, dwp.version, dwp.feature, dwp.label, dwp.left_pos, " +
                        "dwp.lower_pos, dwp.right_pos, dwp.upper_pos, dwp.b_box, dwp.precision_val, " +
                        "dwp.predicted_value, dwp.section_alias, dwp.sor_container_instance, " +
                        "dwp.document_id, dwp.truth_id, dwp.channel_id, dwp.group_id, " +
                        "dwp.origin_id, dwp.paper_no, dwp.question_id, dwp.root_pipeline_id, " +
                        "dwp.score, dwp.sor_item_name, dwp.sor_question, dwp.synonym_id, " +
                        "dwp.tenant_id, dwp.vqa_score, dwp.category, dwp.stage, dwp.batch_id, " +
                        "dwp.line_item_type, dwp.is_encrypted, dwp.encryption_policy, " +
                        "dwp.is_removed_after_filtering, dwp.message, dwp.sor_container_id, " +
                        "dwp.truth_entity_id, dwp.sor_item_id, dwp.is_multi_entity_enabled " +
                        "FROM sor_transform.document_wise_post_processing_input_audit dwp " +
                        "WHERE dwp.tenant_id = 1 " +
                        "AND dwp.batch_id = 'BATCH-16_0' " +
                        "AND dwp.group_id = 16 " +
                        "LIMIT 10")
                .build();

        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setProcessId(12345L);
        action.getContext().put("document.wise.post.processing.activator", "true");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        action.getContext().put("document.wise.post.processing.thread.count", "5");
        action.getContext().put(DB_SELECT_READ_BATCH_SIZE, "10");
        action.getContext().put(DB_INSERT_WRITE_BATCH_SIZE, "100");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("temp_schema_name", "transist_data");
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "16");
        action.getContext().put("batch_id", "BATCH-16_0");
        action.getContext().put("init_process_id.process_id", "12345");
        action.getContext().put("document.wise.executor.bsh.class.order", "TestValidator");
        action.getContext().put("TestValidator", "import in.handyman.raven.lib.model.DocumentWisePostProcessingInput; " +
                "import java.util.List; " +
                "public class TestValidator { " +
                "public List<DocumentWisePostProcessingInput> doCustomPredictionMapping(List<DocumentWisePostProcessingInput> inputs, Long rootPipelineId) { " +
                "return inputs; " +
                "} " +
                "}");
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url", encryptionUrl);
        action.getContext().put("protegrity.dec.api.url", decryptionUrl);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        documentWisePostProcessingAction.execute();
    }

    @Test
    void testExecute_WithDifferentThreadCount() throws Exception {
        final DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(true)
                .name("Test DocumentWisePostProcessing with custom thread count")
                .resourceConn("ibm_mar12")
                .outputTable("sor_transform.document_wise_post_processing_output_audit")
                .batchId("BATCH-16_0")
                .querySet("SELECT " +
                        "document_wise_post_processing_input_id, dwp.transaction_id, dwp.created_on, " +
                        "dwp.created_user_id, dwp.last_updated_on, dwp.last_updated_user_id, " +
                        "dwp.status, dwp.version, dwp.feature, dwp.label, dwp.left_pos, " +
                        "dwp.lower_pos, dwp.right_pos, dwp.upper_pos, dwp.b_box, dwp.precision_val, " +
                        "dwp.predicted_value, dwp.section_alias, dwp.sor_container_instance, " +
                        "dwp.document_id, dwp.truth_id, dwp.channel_id, dwp.group_id, " +
                        "dwp.origin_id, dwp.paper_no, dwp.question_id, dwp.root_pipeline_id, " +
                        "dwp.score, dwp.sor_item_name, dwp.sor_question, dwp.synonym_id, " +
                        "dwp.tenant_id, dwp.vqa_score, dwp.category, dwp.stage, dwp.batch_id, " +
                        "dwp.line_item_type, dwp.is_encrypted, dwp.encryption_policy, " +
                        "dwp.is_removed_after_filtering, dwp.message, dwp.sor_container_id, " +
                        "dwp.truth_entity_id, dwp.sor_item_id, dwp.is_multi_entity_enabled " +
                        "FROM sor_transform.document_wise_post_processing_input_audit dwp " +
                        "WHERE dwp.tenant_id = 1 " +
                        "AND dwp.batch_id = 'BATCH-16_0' " +
                        "AND dwp.group_id = 16 " +
                        "LIMIT 10")
                .build();

        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setProcessId(12345L);
        action.getContext().put("document.wise.post.processing.activator", "true");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        action.getContext().put("document.wise.post.processing.thread.count", "10");
        action.getContext().put(DB_SELECT_READ_BATCH_SIZE, "20");
        action.getContext().put(DB_INSERT_WRITE_BATCH_SIZE, "200");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("temp_schema_name", "transist_data");
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "16");
        action.getContext().put("batch_id", "BATCH-16_0");
        action.getContext().put("init_process_id.process_id", "12345");
        action.getContext().put("document.wise.executor.bsh.class.order", "TestValidator");
        action.getContext().put("TestValidator", "import in.handyman.raven.lib.model.DocumentWisePostProcessingInput; " +
                "import java.util.List; " +
                "public class TestValidator { " +
                "public List<DocumentWisePostProcessingInput> doCustomPredictionMapping(List<DocumentWisePostProcessingInput> inputs, Long rootPipelineId) { " +
                "return inputs; " +
                "} " +
                "}");
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url", encryptionUrl);
        action.getContext().put("protegrity.dec.api.url", decryptionUrl);

        final DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, build);
        documentWisePostProcessingAction.execute();
    }
}
