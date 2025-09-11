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
            .querySet("SELECT tenant_id,\n" +
                    "                   origin_id,\n" +
                    "                   group_id,\n" +
                    "                   paper_no,\n" +
                    "                   sor_question,\n" +
                    "                   answer,\n" +
                    "                   vqa_score,\n" +
                    "                   score,\n" +
                    "                   weight,\n" +
                    "                   sor_item_attribution_id,\n" +
                    "                   sor_item_name,\n" +
                    "                   document_id,\n" +
                    "                   b_box,\n" +
                    "                   root_pipeline_id,\n" +
                    "                   question_id,\n" +
                    "                   synonym_id,\n" +
                    "                   model_registry,\n" +
                    "                   category,\n" +
                    "                   batch_id,\n" +
                    "                   is_ocr_field_comparable,\n" +
                    "                   extracted_text\n" +
                    "            FROM transit_data.ocr_text_comparison_input_11817")
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
