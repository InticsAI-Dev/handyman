package in.handyman.raven.lib.tritonServerTest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.IntellimatchAction;
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
                .resourceConn("intics_agadia_db_conn")
                .matchResult("")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.masterdata-comparison.url", "http://localhost:9200/v2/models/cos-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.masterdata.API.count", "1"),
                Map.entry("write.batch.size", "5")));

        IntellimatchAction action1 = new IntellimatchAction(actionExecutionAudit, log, build);
        action1.execute();
    }
}


