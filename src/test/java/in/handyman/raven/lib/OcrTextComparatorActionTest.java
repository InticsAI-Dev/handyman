package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.ocr.OcrTextComparatorInput;
import in.handyman.raven.lib.model.OcrTextComparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class OcrTextComparatorActionTest {

    private ActionExecutionAudit actionExecutionAudit;
    private OcrTextComparator comparatorConfig;
    private OcrTextComparatorAction comparatorAction;


        @Test
        void execute() throws Exception {
            OcrTextComparator ocrTextComparator = OcrTextComparator.builder()
                    .name("Ocr Text Comparator Action")
                    .batchId("BATCH-71_0")
                    .condition(true)
                    .outputTable("macro.ocr_text_comparison_result")
                    .resourceConn("intics_zio_db_conn")
                    .querySet("SELECT\n" +
                            "    now(),\n" +
                            "    1,\n" +
                            "    now(),\n" +
                            "    1,\n" +
                            "    1,\n" +
                            "    vt.origin_id,\n" +
                            "    vt.group_id,\n" +
                            "    vt.paper_no,\n" +
                            "    vt.sor_question,\n" +
                            "    vt.answer,\n" +
                            "    vt.vqa_score,\n" +
                            "    vt.score,\n" +
                            "    vt.weight,\n" +
                            "    vt.sor_item_attribution_id,\n" +
                            "    vt.sor_item_name,\n" +
                            "    vt.document_id,\n" +
                            "    vt.b_box,\n" +
                            "    vt.root_pipeline_id,\n" +
                            "    vt.question_id,\n" +
                            "    vt.synonym_id,\n" +
                            "    vt.model_registry,\n" +
                            "    vt.category,\n" +
                            "    vt.batch_id,\n" +
                            "    si.is_ocr_field_comparable,\n" +
                            "    dsoa.extracted_text,\n" +
                            "    si.allowed_adapter,\n" +
                            "    ep.encryption_policy\n" +
                            "FROM sor_validation.sor_validation_payload_queue_archive sqv\n" +
                            "JOIN sor_transaction.vqa_transaction vt\n" +
                            "    ON vt.origin_id = sqv.origin_id\n" +
                            "   AND vt.batch_id = sqv.batch_id\n" +
                            "LEFT JOIN deep_sift.deep_sift_output_audit dsoa\n" +
                            "    ON dsoa.origin_id = vt.origin_id\n" +
                            "   AND dsoa.paper_no = vt.paper_no\n" +
                            "   AND vt.batch_id = dsoa.batch_id\n" +
                            "JOIN sor_meta.sor_container sc\n" +
                            "    ON vt.tenant_id = sc.tenant_id\n" +
                            "JOIN sor_meta.sor_item si\n" +
                            "    ON si.sor_container_id = sc.sor_container_id\n" +
                            "   AND si.tenant_id = sc.tenant_id\n" +
                            "   AND si.sor_item_name = vt.sor_item_name\n" +
                            "JOIN sor_meta.encryption_policies ep\n" +
                            "    ON si.encryption_policy_id = ep.encryption_policy_id\n" +
                            "WHERE vt.group_id = '79'\n" +
                            "  AND vt.tenant_id = 1\n" +
                            "  AND sqv.batch_id = 'BATCH-79_0'\n" +
                            "  AND sqv.status = 'COMPLETED'\n" +
                            "  AND sc.document_type = 'MEDICAL_GBD'\n" +
                            "  AND sc.status = 'ACTIVE'\n" +
                            "  AND si.status = 'ACTIVE';")
                    .build();

            final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
            action.getContext().put("tenant_id", "1");
            action.getContext().put("group_id", "71");
            action.getContext().put("batch_id", "BATCH-71_0");
            action.getContext().put("created_user_id", "1");
            action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
            action.getContext().put("ocr.comparison.fuzzy.match.threshold", "70");
            action.setRootPipelineId(929L);

            OcrTextComparatorAction ocrTextComparatorAction = new OcrTextComparatorAction(action, log, ocrTextComparator);
            ocrTextComparatorAction.execute();
        }

    @BeforeEach
    void setup() {
        actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("ocr.comparison.fuzzy.match.threshold", "90");
        actionExecutionAudit.getContext().put("pipeline.end.to.end.encryption", "true");
        actionExecutionAudit.getContext().put("pipeline.deep.sift.encryption", "false");
        actionExecutionAudit.getContext().put("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt");
        actionExecutionAudit.getContext().put("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt");
        actionExecutionAudit.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");

        OcrTextComparator comparatorConfig = new OcrTextComparator();
        comparatorConfig.setResourceConn("intics_zio_db_conn");
        comparatorConfig.setBatchId("BATCH-1213_0");
        comparatorConfig.setOutputTable("macro.ocr_text_comparison_result");
        comparatorConfig.setQuerySet("SELECT ROW_NUMBER() OVER (ORDER BY root_pipeline_id) AS id,'AES256' as encryption_policy,* FROM macro.ocr_text_comparison_input_audit where origin_id ='ORIGIN-15574' and is_ocr_field_comparable is true");
        comparatorConfig.setCondition(true);
        comparatorConfig.setName("ocr-text-comparator");
        comparatorAction = new OcrTextComparatorAction(actionExecutionAudit,log , comparatorConfig);


    }


    @Test
    void execute_shouldRunSuccessfully() throws Exception {
        comparatorAction.execute();


    }

    @Test
    void execute_shouldHandleExceptionGracefully() throws Exception {


    }

    @Test
    void executeIf_shouldReturnTrue() throws Exception {

    }

    @Test
    void executeIf_shouldReturnFalseWhenConditionNull() throws Exception {

    }

    @Test
    void testPrivateUtilityMethods_viaReflection() throws Exception {

    }
}
