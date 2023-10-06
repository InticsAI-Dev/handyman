package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PhraseMatchPaperFilter;
import in.handyman.raven.lib.PhraseMatchPaperFilterAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;


@Slf4j
public class PharseMatchActionTest {
    @Test
    void tritonServer() throws Exception {

        final PhraseMatchPaperFilter build = PhraseMatchPaperFilter.builder()
                .condition(true)
                .name("Test PhraseMatch")
                .processID("12345")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .querySet("select 1 as paper_no, 'drug name, patient name,prescriber name' as page_content, 1 as group_id, 'INT-1' as origin_id, \n" +
                        "'1234' as process_id,1 as sor_container_id, 'Patient' as truth_entity, \n"
                +"jsonb_object_agg(t.truth_entity,t.keys_to_filter) as truth_placeholder\n" +
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
        action.getContext().put("copro.paper-filtering-phrase-match.url", "http://192.168.10.245:8500/v2/models/pm-service/versions/1/infer");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("okhttp.client.timeout","20"),
                Map.entry("write.batch.size","5")));

        final PhraseMatchPaperFilterAction zeroShotClassifierPaperFilterAction = new PhraseMatchPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }
}
