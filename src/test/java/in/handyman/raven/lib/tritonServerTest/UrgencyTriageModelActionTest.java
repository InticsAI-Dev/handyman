package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.UrgencyTriageModelAction;
import in.handyman.raven.lib.model.UrgencyTriage;
import in.handyman.raven.lib.model.UrgencyTriageModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class UrgencyTriageModelActionTest {

    @Test
    void tritonServer() throws Exception {

        final UrgencyTriageModel urgencyTriageModel=UrgencyTriageModel.builder()
                .condition(true)
                .name("urgency triage")
                .outputDir("/data/output/")
                .querySet("SELECT 'Int-1' as originId, '1234567' as preprocessedFileId,1 as paperNo, '/data/input/test_qr.jpg' as inputFilePath,\n" +
                        "                1 as createdUserId, 1 as lastUpdatedUserId,\n" +
                        "                1 as tenantId,'TMP-1' as templateId, 12345 as processId,123 as modelId, 1 as groupId,\n" +
                        "               12345 as root_pipeline_id")
                .resourceConn("intics_agadia_db_conn")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.urgency-triage-model.url", "http://192.168.10.245:8800/v2/models/ut-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("ut.consumer.API.count", "1"),
                Map.entry("write.batch.size", "5")));

     UrgencyTriageModelAction action1 = new UrgencyTriageModelAction(actionExecutionAudit, log, urgencyTriageModel);
        action1.execute();



    }
}
