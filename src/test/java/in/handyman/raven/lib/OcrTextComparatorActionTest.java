package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueConcatenation;
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
            .querySet("SELECT  \n" +
                    "                        1, \n" +
                    "                        vt.origin_id, \n" +
                    "                        vt.group_id, \n" +
                    "                        vt.paper_no, \n" +
                    "                        vt.sor_question, \n" +
                    "                        vt.answer, \n" +
                    "                        vt.vqa_score, \n" +
                    "                        vt.score, \n" +
                    "                        vt.weight, \n" +
                    "                        vt.sor_item_attribution_id, \n" +
                    "                        vt.sor_item_name, \n" +
                    "                        vt.document_id, \n" +
                    "                        vt.b_box, \n" +
                    "                        vt.root_pipeline_id, \n" +
                    "                        vt.question_id, \n" +
                    "                        vt.synonym_id, \n" +
                    "                        vt.model_registry, \n" +
                    "                        vt.category, \n" +
                    "                        vt.batch_id, \n" +
                    "                        si.is_ocr_field_comparable, \n" +
                    "                        dsoa.extracted_text\n" +
                    "                      FROM sor_transaction.vqa_transaction vt \n" +
                    "                    JOIN deep_sift.deep_sift_output_audit dsoa  \n" +
                    "                        ON dsoa.origin_id = vt.origin_id  \n" +
                    "                       AND dsoa.paper_no = vt.paper_no \n" +
                    "                    JOIN sor_meta.sor_container sc  \n" +
                    "                        ON sc.document_type = 'MEDICAL_GBD' \n" +
                    "                       AND vt.tenant_id = sc.tenant_id \n" +
                    "                    JOIN sor_meta.sor_item si  \n" +
                    "                        ON si.sor_container_id = sc.sor_container_id   \n" +
                    "                        AND si.tenant_id = sc.tenant_id  \n" +
                    "                        and si.sor_item_name =vt.sor_item_name      \n" +
                    "                    WHERE vt.group_id =71 \n" +
                    "                      AND vt.tenant_id =1  \n" +
                    "                      AND vt.batch_id = 'BATCH-71_0' \n" +
                    "                      and sc.status ='ACTIVE' and si.status ='ACTIVE'")
            .build();

    final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
    action.getContext().put("tenant_id", "1");
    action.getContext().put("group_id", "71");
    action.getContext().put("batch_id", "BATCH-71_0");
    action.getContext().put("created_user_id", "1");
    action.getContext().put("pipeline.end.to.end.encryption", "false");
    action.getContext().put("fuzzy.match.threshold", "70");
    action.setRootPipelineId(929L);

    OcrTextComparatorAction ocrTextComparatorAction = new OcrTextComparatorAction(action, log, ocrTextComparator);
    ocrTextComparatorAction.execute();
}
}
