package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PhraseMatchPaperFilterAction;
import in.handyman.raven.lib.model.PhraseMatchPaperFilter;
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
                .endPoint("http://192.168.10.248:8500/v2/models/pm-service/versions/1/infer")
                .readBatchSize("1")
                .threadCount("1")
                .writeBatchSize("1")
                .querySet("select a.origin_id,a.paper_no,\n" +
                        "sot.content as page_content, 123 as group_id ,a.root_pipeline_id as root_pipeline_id,\n" +
                        "'12345' as process_id,\n" +
                        " a.truth_placeholder,a.tenant_id as tenant_id ,a.batch_id, now() as created_on\n" +
                        "from paper.paper_filter_zsc_pm_input_aggregate_audit a\n" +
                        "join info.source_of_truth sot on a.origin_id = sot.origin_id and a.paper_no = sot.paper_no and sot.batch_id = a.batch_id and sot.tenant_id = a.tenant_id\n")

                .resourceConn("intics_zio_db_conn")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.paper-filtering-phrase-match.url", "http://192.168.10.239:10184/copro/filtering/phrase-match");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("encryption.pipeline.activator", "true"),
                Map.entry("database.decryption.activator", "false"),
                Map.entry("page.content.min.length.threshold", "5"),
                Map.entry("apiUrl", "http://192.168.10.248:10001/copro-utils/data-security/encrypt"),
                Map.entry("decryptApiUrl","http://192.168.10.248:10001/copro-utils/data-security/decrypt"),
                Map.entry("encryption.activator","false"),
                Map.entry("write.batch.size", "5")));

        final PhraseMatchPaperFilterAction pmPaperFilterAction = new PhraseMatchPaperFilterAction(action, log, build);
        pmPaperFilterAction.execute();
    }
}
