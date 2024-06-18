package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.UrgencyTriageBetaAction;
import in.handyman.raven.lib.model.UrgencyTriageBeta;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class UrgencyTriageBetaActionTest {
    @Test
    void tritonServer() throws Exception {

        final UrgencyTriageBeta urgencyTriageModel = UrgencyTriageBeta.builder()
                .condition(true)
                .name("urgency triage")
                .outputDir("/data/output/")
                .outputTable("urgency_triage_beta.urgency_triage_beta_pipeline_result")
                .endPoint("http://192.168.10.248:9700/v2/models/ut-service/versions/1/infer")
                .querySet("SELECT 'ORIGIN-3' as originId, '1234567' as preprocessedFileId, 1 as paperNo, '/data/output/1/preprocess/autorotation/auto_rotation/SYNT_166730538_c1_2.jpg' as inputFilePath,\n" +
                        "                1 as createdUserId, 1 as lastUpdatedUserId,\n" +
                        "                1 as tenantId,'TMP-1' as templateId, 12345 as processId,123 as modelId, 1 as groupId,\n" +
                        "               12345 as root_pipeline_id, 'BATCH_0_1' as batch_id, '/data/output/' as outputDir")
                .resourceConn("intics_zio_db_conn")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.urgency-triage-model.url", "http://192.168.10.248:9700/v2/models/ut-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("ut.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));

        UrgencyTriageBetaAction action1 = new UrgencyTriageBetaAction(actionExecutionAudit, log, urgencyTriageModel);
        action1.execute();


    }
}