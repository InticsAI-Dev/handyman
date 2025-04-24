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
                .processID("audit")
                .querySet("select a.origin_id,a.paper_no,\n" +
                        "sot.content as page_content, 'Humana' as template_name, 3033 as group_id ,a.root_pipeline_id as root_pipeline_id,\n" +
                        "'3033' as process_id,\n" +
                        "a.truth_placeholder,a.tenant_id as tenant_id ,a.batch_id, now() as created_on\n" +
                        "from paper.paper_filter_zsc_pm_input_aggregate_audit a\n" +
                        "join info.source_of_truth sot on a.origin_id = sot.origin_id and a.paper_no = sot.paper_no and sot.batch_id = a.batch_id and sot.tenant_id = a.tenant_id\n" +
                        "where a.root_pipeline_id = 3033;")
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
                .processID("audit")
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
                .resourceConn("intics_zio_db_conn")
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
                .processID("audit")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .endPoint("https://api.runpod.ai/v2/oq74j3k9oxclog/runsync")
                .querySet("select a.origin_id,a.paper_no,\n" +
                        "sot.content as page_content, 3033 as group_id ,a.root_pipeline_id as root_pipeline_id,\n" +
                        "'3033' as process_id,\n" +
                        "a.truth_placeholder,a.tenant_id as tenant_id ,a.batch_id, now() as created_on\n" +
                        "from paper.paper_filter_zsc_pm_input_aggregate_audit a\n" +
                        "join info.source_of_truth sot on a.origin_id = sot.origin_id and a.paper_no = sot.paper_no and sot.batch_id = a.batch_id and sot.tenant_id = a.tenant_id\n" +
                        "where a.root_pipeline_id = 3033;")
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
                Map.entry("copro.request.activator.handler.name", "RUNPOD"),
                Map.entry("filter.zsc.page.content.lower", "true"),
                Map.entry("temp_schema_name", "paper"),
                Map.entry("write.batch.size", "5")));


        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

}

