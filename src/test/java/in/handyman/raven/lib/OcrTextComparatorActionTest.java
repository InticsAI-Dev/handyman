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
            .querySet("SELECT \n" +
                    "    1 AS tenant_id,\n" +
                    "    'ORIGIN-12345' AS origin_id,\n" +
                    "    999 AS group_id,\n" +
                    "    123 AS paper_no,\n" +
                    "    'Dummy Question' AS sor_question,\n" +
                    "    'Bala Soundarya' AS answer,\n" +
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
                    "    'Jun. 10.2025 1:07PM\n" +
                    "No. 1167 P. 2\n" +
                    "INPATIENT REGISTRATION RECORD\n" +
                    "PATIENT INFORMATION:\n" +
                    "ACCT#: 115377509\n" +
                    "ADM DATE: 06/10/2025\n" +
                    "M.R.#: 91-31-76\n" +
                    "ADM TIME: 15:46\n" +
                    "FIRST NAME: Bala" +
                    "LAST NAME: Soundarya " +
                    "ADDR:\n" +
                    "DOB/PL: 08/24/1935 MEXIÇO\n" +
                    "0y7023 Del Sol Terrace\n" +
                    "AGE : 067Y\n" +
                    "CSZP: Vancouver, WA, 98664\n" +
                    "SEX : M\n" +
                    "RACE: O OTHER\n" +
                    "PHN#:\n" +
                    "MARTL : M\n" +
                    "HISP: Y\n" +
                    "SSNO:\n" +
                    "RELGN : NON VIP:\n" +
                    "AKA : BECERRAQUINTOR\n" +
                    "BRTBY : AMR\n" +
                    "ACCOUNT INFORMATION:\n" +
                    "ADM DX: STEMT INVOLVING RT CORONARY ARTERY\n" +
                    "ADM CLERK: ADTHERNS\n" +
                    "PAT TP: I\n" +
                    "MED SRV: ICU\n" +
                    "ROOM/BED: 426 B\n" +
                    "FIN CLASS: 6853\n" +
                    "ACCOM : DEFAULT AÇUITY CODE\n" +
                    "KAISER MR:\n" +
                    "PHYSICIAN INFORMATION:\n" +
                    "PATIENT EMPLOYMENT :\n" +
                    "ADM MD: SHAHI, ABHISHEK\n" +
                    "EMPLYR:\n" +
                    "ATT MD: SHAHI, ABHISHEK\n" +
                    "OCCUP : RETIRED/UNKNOWN\n" +
                    "PRI MD: DOCTOR, DOCTOR\n" +
                    "NEXT OF KIN:\n" +
                    "EMERGENCY CONTACT:\n" +
                    "LEGAL REPRESENTATIVE:\n" +
                    "BECERRA QUINTOR, IRNA\n" +
                    "9702 RUFF AVE\n" +
                    "STOCKTON, CA 95212\n" +
                    "REL : WIFE\n" +
                    "REL :\n" +
                    "REL :\n" +
                    "HOM#: 209-969-5832\n" +
                    "HOM# :\n" +
                    "HOM#: :\n" +
                    "WRK#:\n" +
                    "WRK#: :\n" +
                    "WRK#: :\n" +
                    "COMMENT: PCP:DR.GUZMAN/ER ÇONT: JASMIN (DHGT) . 209-471-4242\n" +
                    "GUARANTOR INFORMATION:\n" +
                    "NAME: Gopalley,Madgelyn ADDR:\n" +
                    "EOC: UM53219484\n" +
                    "Oy7023 Del Sol Terrace\n" +
                    "CSZP: Vancouver, WA, 98664\n" +
                    "PHN#:\n" +
                    "SSNO:\n" +
                    "AKA : BECERRAQUINTOR\n" +
                    "INSURANCE:\n" +
                    "PRIMARY:\n" +
                    "SECONDARY:\n" +
                    "TERTIARY:\n" +
                    "BLUE CROSS MEDIÇARE PREF\n" +
                    "ATTN: CLAIMS\n" +
                    "LOS ANGELES, CA 900600007\n" +
                    "833-848-8730\n" +
                    "POL:\n" +
                    "105J54006\n" +
                    "POL:\n" +
                    "PROCEDURE : 11920\n" +
                    "GRP: CAEGR001\n" +
                    "GRP :\n" +
                    "GRP:\n" +
                    "AUT:\n" +
                    "AUT:\n" +
                    "AUT:\n" +
                    "SELF\n" +
                    "COMMENTS:\n" +
                    "LANGUAGE: ENGLISH\n" +
                    "SENS ORG :\n" +
                    "72 HRS :\n" +
                    "ARMBAND : Y\n" +
                    "ADV DIR : N\n" +
                    "LEGAL REP: N NPP\n" +
                    ": Y\n" +
                    "INTERPRT: N\n" +
                    "VALUABLES: N RABY ID#:\n" +
                    "ICD10: I21.11 ER ADMTT\n" +
                    "HOSPITALIST NPI 1932418118\n" +
                    "LEVEL OF ASSIST SECOND REQUEST\n" +
                    "PG:2/2 * RCVD:06/10/2025 07:08:49 PM ET * JOBiat18669591537: -20250610160801717-328-2*CSID: *ANI:12099445550 * DUR:00-48 mm-ss' AS extracted_text\n")
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
