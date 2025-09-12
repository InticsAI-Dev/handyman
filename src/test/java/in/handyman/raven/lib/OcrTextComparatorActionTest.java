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
            .querySet("SELECT \n" +
                    "    1 AS tenant_id,\n" +
                    "    'ORIGIN-12345' AS origin_id,\n" +
                    "    999 AS group_id,\n" +
                    "    123 AS paper_no,\n" +
                    "    'Dummy Question' AS sor_question,\n" +
                    "    '89121 De3 Mar Crossing' AS answer,\n" +
                    "    0.95 AS vqa_score,\n" +
                    "    85 AS score,\n" +
                    "    1.0 AS weight,\n" +
                    "    111 AS sor_item_attribution_id,\n" +
                    "    'member_last_name' AS sor_item_name,\n" +
                    "    'DOC-12345' AS document_id,\n" +
                    "    '{\"x\":10,\"y\":20,\"w\":100,\"h\":50}'::json AS b_box,\n" +
                    "    '123456' AS root_pipeline_id,\n" +
                    "    222 AS question_id,\n" +
                    "    333 AS synonym_id,\n" +
                    "    'MODEL-ABC' AS model_registry,\n" +
                    "    'Dummy Category' AS category,\n" +
                    "    'BATCH-DUMMY' AS batch_id,\n" +
                    "    TRUE AS is_ocr_field_comparable,\n" +
                    "     'ADDR_ALPHANUMERIC' AS adaptor_code,\n" +

                    "    'wertyu rtyuio 89t121 Del Mar Crossing' AS extracted_text\n")
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
