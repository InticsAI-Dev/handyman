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
        action.getContext().put("copro.paper-filtering-phrase-match.url", "http://192.168.10.248:8500/v2/models/pm-service/versions/1/infer");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("okhttp.client.timeout","20"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size","5")));

        final PhraseMatchPaperFilterAction zeroShotClassifierPaperFilterAction = new PhraseMatchPaperFilterAction(action, log, build);
        zeroShotClassifierPaperFilterAction.execute();
    }
}
