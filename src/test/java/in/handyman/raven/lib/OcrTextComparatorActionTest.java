package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.OcrTextComparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class OcrTextComparatorActionTest {

@Test
void execute() throws Exception {
    OcrTextComparator ocrTextComparator = OcrTextComparator.builder()
            .name("Ocr Text Comparator Action")
            .batchId("BATCH-71_0")
            .condition(true)
            .outputTable("macro.ocr_text_comparison_result")
            .resourceConn("intics_zio_db_conn")
            .querySet("SELECT\n" +
                    "    now(),1, now(), 1, 1,\n" +
                    "    vt.origin_id, vt.group_id, vt.paper_no, vt.sor_question, vt.answer,\n" +
                    "    vt.vqa_score, vt.score, vt.weight, vt.sor_item_attribution_id, vt.sor_item_name,\n" +
                    "    vt.document_id, vt.b_box, vt.root_pipeline_id, vt.question_id, vt.synonym_id,\n" +
                    "    vt.model_registry, vt.category, vt.batch_id,\n" +
                    "    si.is_ocr_field_comparable,\n" +
                    "    dsoa.extracted_text,      \n" +
                    "    si.allowed_adapter\n" +
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
                    "WHERE vt.group_id =203\n" +
                    "  AND vt.tenant_id = 1\n" +
                    "  AND sqv.batch_id = 'BATCH-203_0'\n" +
                    "  AND sqv.status = 'COMPLETED'\n" +
                    "  AND sc.document_type = 'MEDICAL_GBD'\n" +
                    "  AND sc.status = 'ACTIVE'\n" +
                    "  and vt.origin_id ='ORIGIN-2274'\n" +
                    "  AND si.status = 'ACTIVE';\n")
            .build();

    final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
    action.getContext().put("tenant_id", "1");
    action.getContext().put("group_id", "71");
    action.getContext().put("batch_id", "BATCH-71_0");
    action.getContext().put("created_user_id", "1");
    action.getContext().put("pipeline.end.to.end.encryption", "false");
    action.getContext().put("ocr.comparison.fuzzy.match.threshold", "70");
    action.setRootPipelineId(929L);

    OcrTextComparatorAction ocrTextComparatorAction = new OcrTextComparatorAction(action, log, ocrTextComparator);
    ocrTextComparatorAction.execute();
}
}
