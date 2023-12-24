package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.IntellimatchAction;
import in.handyman.raven.lib.MasterdataComparisonAction;
import in.handyman.raven.lib.model.MasterdataComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class MasterDataComparisonActionTest {

    @Test
    void tritonServer() throws Exception {
        final MasterdataComparison build = MasterdataComparison.builder()
                .condition(true)
                .name("MasterDataComparison")
                .matchResult("md_lookup_systemKey.provider_npi_match")
                .inputSet("SELECT 'INT' AS originId, 1 AS paperNo, 1 AS groupId, 1 AS eocIdentifier, ARRAY['dinesh kumar', 'dinesh', 'kumar'] AS extractedValue,'dinesh' AS actualValue, 1 AS rootPipelineId")
                .resourceConn("intics_zio_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.masterdata-comparison.url", "http://192.168.10.239:10192/copro/similarity/cos-sim");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.masterdata.API.count", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size", "5")));

        MasterdataComparisonAction action1 = new MasterdataComparisonAction(actionExecutionAudit, log, build);
        action1.execute();
    }
}


