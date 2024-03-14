package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.UrgencyTriageModelAction;
import in.handyman.raven.lib.model.UrgencyTriageModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class UrgencyTriageModelActionTest {

    @Test
    void tritonServer() throws Exception {

        final UrgencyTriageModel urgencyTriageModel = UrgencyTriageModel.builder()
                .condition(true)
                .name("urgency triage")
                .outputDir("/data/output/")
                .outputTable("urgency_triage.ut_model_result")
                .endPoint("http://192.168.10.248:8800/v2/models/ut-service/versions/1/infer")
                .querySet("SELECT 'INT-3' as originId, '1234567' as preprocessedFileId, 1 as paperNo, '/data/input/test.jpg' as inputFilePath,\n" +
                        "                1 as createdUserId, 1 as lastUpdatedUserId,\n" +
                        "                1 as tenantId,'TMP-1' as templateId, 12345 as processId,123 as modelId, 1 as groupId, '1' as batchId,\n" +
                        "               12345 as root_pipeline_id")
                .resourceConn("intics_zio_db_conn")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.urgency-triage-model.url", "http://192.168.10.239:10194/copro/urgency-triage");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("ut.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));

        UrgencyTriageModelAction action1 = new UrgencyTriageModelAction(actionExecutionAudit, log, urgencyTriageModel);
        action1.execute();


    }
}
