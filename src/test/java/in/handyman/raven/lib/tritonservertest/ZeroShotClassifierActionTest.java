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
                .querySet("select origin_id, paper_no, page_content, group_id, root_pipeline_id, process_id, truth_placeholder, tenant_id, batch_id, created_on " +
                        "from zsc_input_test")
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
                .endPoint("http://192.168.10.248:8400/v2/models/zsc-service/versions/1/infer")
                .querySet("select origin_id, paper_no, page_content, group_id, root_pipeline_id, process_id, truth_placeholder, tenant_id, batch_id, created_on " +
                        "from zsc_input_test")
                .resourceConn("intics_zio_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-zero-shot-classifier.url", "http://192.168.10.248:8400/v2/models/zsc-service/versions/1/infer");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));


        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

    @Test
    void encryptedTritonServer() throws Exception {
        final ZeroShotClassifierPaperFilter build = ZeroShotClassifierPaperFilter.builder()
                .condition(true)
                .name("Test ZSC")
                .processID("12345")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .endPoint("http://192.168.10.240:8400/v2/models/zsc-service/versions/1/infer")
                .querySet("SELECT 'origin123' AS origin_id, " +
                        "101 AS paper_no, " +
                        "'Sample page content' AS page_content, " +
                        "'group789' AS group_id, " +
                        "11011 AS root_pipeline_id, " +
                        "'process567' AS process_id, " +
                        "'{\"key\": [\"value1\", \"value2\"]}'::jsonb AS truth_placeholder, " +
                        "1 AS tenant_id, " +
                        "'batch123' AS batch_id, " +
                        "NOW() AS created_on;")
                .resourceConn("intics_zio_db_conn")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-zero-shot-classifier.url", "http://192.168.10.240:8400/v2/models/zsc-service/versions/1/infer");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("encryption.pipeline.activator", "true"),
                Map.entry("write.batch.size", "5")));

        final ZeroShotClassifierPaperFilterAction zeroShotClassifierPaperFilterAction = new ZeroShotClassifierPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }

}

