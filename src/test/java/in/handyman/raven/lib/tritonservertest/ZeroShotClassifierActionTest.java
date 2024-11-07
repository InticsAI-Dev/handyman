package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PhraseMatchPaperFilterAction;
import in.handyman.raven.lib.ZeroShotClassifierPaperFilterAction;
import in.handyman.raven.lib.model.PhraseMatchPaperFilter;
import in.handyman.raven.lib.model.ZeroShotClassifierPaperFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class ZeroShotClassifierActionTest {

    @Test
    void execute() throws Exception {

        final ZeroShotClassifierPaperFilter build = ZeroShotClassifierPaperFilter.builder()
                .condition(true)
                .name("Test ZeroShotClassifier")
                .processID("1234")
                .querySet("select 1 as paper_no, 'drug name, patient name,prescriber name' as page_content, 1 as group_id, 'INT-1' as origin_id, \n" +
                        "'1234' as process_id,1 as sor_container_id, 'Patient' as truth_entity, \n"
                        + "jsonb_object_agg(t.truth_entity,t.keys_to_filter) as truth_placeholder\n" +
                        "                        from (select te.sor_container_id  as sor_container_id,\n" +
                        "                        te.truth_entity as truth_entity,te.sor_truth_entity_id,\n" +
                        "                        jsonb_agg(st.truth_entity) as keys_to_filter\n" +
                        "                        from sor_meta.sor_truth_entity_placeholder st\n" +
                        "                        join sor_meta.sor_truth_entity te on te.truth_entity= st.truth_entity\n" +
                        "                        group by te.sor_container_id,te.sor_truth_entity_id,te.truth_entity )t")
                .resourceConn("intics_agadia_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-zero-shot-classifier.url", "http://localhost:10189/copro/filtering/zero_shot_classifier");

        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }


    @Test
    void executePhraseMatch() throws Exception {

        final PhraseMatchPaperFilter build = PhraseMatchPaperFilter.builder()
                .condition(true)
                .name("Test PhraseMatch")
                .processID("1234")
                .querySet("select sot.paper_no, sot.content as page_content, sot.group_id, sot.origin_id, \n" +
                        "'1234' as process_id,t.sor_container_id,t.truth_entity, \n" +
                        "t.keys_to_filter from (select ste.sor_container_id as sor_container_id, \n" +
                        "ste.truth_entity as truth_entity, \n" +
                        "jsonb_agg(st.synonym) as keys_to_filter\n" +
                        "from sor_meta.sor_tsynonym st \n" +
                        "inner join sor_meta.sor_item_truth_entity_mapping sitem \n" +
                        "on sitem.sor_item_name = st.sor_item_name \n" +
                        "and sitem.sor_truth_mapping_id = st.sor_truth_mapping_id \n" +
                        "inner join sor_meta.sor_truth_entity ste \n" +
                        "on ste.truth_entity = sitem.truth_entity  \n" +
                        "where st.is_paper_filter_candidate ='True'\n" +
                        "group by ste.sor_container_id,ste.truth_entity  )t\n" +
                        "cross join info.source_of_truth sot\n" +
                        "where sot.origin_id ='INT-1' limit 2;")
                .resourceConn("intics_agadia_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-phrase-match.url", "http://localhost:10189/copro/filtering/phrase_match");

        final PhraseMatchPaperFilterAction zeroShotClassifierPaperFilterAction = new PhraseMatchPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

    @Test
    void tritonServer() throws Exception {
        final ZeroShotClassifierPaperFilter build = ZeroShotClassifierPaperFilter.builder()
                .condition(true)
                .name("Test ZSC")
                .processID("12345")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .endPoint("http://192.168.10.239:10183/copro/filtering/zero-shot-classifier")
                .querySet("SELECT " +
                        "1 AS paper_no, " +
                        "'drug name, patient name, prescriber name' AS page_content, " +
                        "1 AS group_id, " +
                        "'INT-1' AS origin_id, " +
                        "1 AS process_id, " +
                        "1 AS rootPipelineId, " +
                        "JSONB_BUILD_OBJECT(" +
                        "'Drug', ARRAY_TO_JSON(ARRAY['Strength', 'Quantity', 'Drug Requested', 'Drug', 'Medication', 'Drug name and Strength', 'Drug name', 'Dose', 'Directions', 'Diagnosis']), " +
                        "'Member', ARRAY_TO_JSON(ARRAY['Members Name', 'Member Name', 'Member Id', 'Member Optima', 'Member DOB', 'Members DOB'])" +
                        ") AS truthPlaceholder")
                .resourceConn("intics_zio_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-zero-shot-classifier.url", "http://192.168.10.239:10183/copro/filtering/zero-shot-classifier");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));


        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

    @Test
    void replicateServer() throws Exception {
        final ZeroShotClassifierPaperFilter build = ZeroShotClassifierPaperFilter.builder()
                .condition(true)
                .name("Test ZSC")
                .processID("12345")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .endPoint("https://api.replicate.com/v1/predictions")
                .querySet("SELECT 'batch-1' as batch_id," +
                        "1 AS paper_no, " +
                        "'14 May 2023 02:39 HP FaxZuci 04469178911 page 14 Other Q5. if other, please provide diagnosis: L50 .8 other uiticaria Q6. For IgE-Medicated allergic asthma has the patient had previous use of an Inhaled corticosterios (ICS) AND one of the following: Please check thos ethat apply: Long-acting muscarinic antagonist (LAMA) (Spiriva) or CSLAEBLAMA(eeg, Inhaled corticosteriod /Long- acting beta ( ICS/LABA) Leukotriene receptor antagonist (LTRA) corticosteriod in the past of 6 months? None of the above Q7. Has the patient had continuing symptomatic asthma requiring one or more fills of oral Q8. Has the patient experience contiuing symptomatic requiring 3 or more offices visit for asthma OR at least one ER visit or hospitilation due to an asthma exacerbation in the Pyes No past 6 months? yes yes Yes Yes Yes famotidine)? Lyes Yes Yes No No EANO No No Q9.F For asthma, is the patient 19E level equal to or greater than 30 10/ml? Q10. For asthma does the patient weigh less than or equal to 330 1bs (150 kgs)? Q11.F For CSU, does the patient have symptoms present most days of the week over a minimum of 3 months, resulting in a significant impact on quality of life? Q12. For CSU, has the patient failed tratment on twice daily HI-anti histamines (eg. Q13. For CSU, has the patient failed tratment on twice daily H2-blockers (eg.ranitdine, Q14. For CSU has the patient required atlest one recent cause of oral steroids? Cutirizine, hydroxyzine) over a minimum of weeks No No No Q15. For reauthorization for CSU, has the patient experienced a significant decrease in itch Q16. For chronic rhinosnusitis with nasal polyps, has the patient been complaint with and severity and hive counts? 32012 HBA74WS9PBR bRfransal corticosteroid therpahy and saline irrigation?' AS page_content, " +
                        "1 AS group_id, " +
                        "'INT-1' AS origin_id, " +
                        "1 as tenant_id," +
                        "1 AS process_id, " +
                        "1 AS rootPipelineId, " +
                        "JSONB_BUILD_OBJECT(" +
                        "'Drug', ARRAY_TO_JSON(ARRAY['Directions/SIG',          'Drug name and strength',          'Drug name and strength']), " +
                        "'Patient', ARRAY_TO_JSON(ARRAY['Patient Date of Birth',          'Patient IP',          'Patient Name',          'Patient Phone #'])," +
                        "'Servicing Provider', ARRAY_TO_JSON(ARRAY['Address',          'Name of Provider or facility',          'NPI/DEA',          'Phone Number'])," +
                        "'Requesting Provider', ARRAY_TO_JSON(ARRAY['Address',          'Fax#',          'Name',          'NPI/DEA',          'Phone #'])" +
                        ") AS truthPlaceholder")
                .resourceConn("intics_zio_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setActionId(1L);
        action.getContext().put("copro.paper-filtering-zero-shot-classifier.url", "https://api.replicate.com/v1/predictions");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("copro.request.activator.handler.name", "REPLICATE"),
                Map.entry("replicate.request.api.token", ""),
                Map.entry("replicate.zsc.version", "955a10253275aaaaed2ef9e61b032ed3776b2df2f7e876a45a5d84ed34adea94"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));


        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

}

