package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.IntellimatchAction;
import in.handyman.raven.lib.MasterdataComparisonAction;
import in.handyman.raven.lib.model.Intellimatch;
import in.handyman.raven.lib.model.MasterdataComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class IntellimatchTest {

    @Test
    void tritonServer() throws Exception {
        final Intellimatch build = Intellimatch.builder()
                .condition(true)
                .name("IntelliMatch")
                .matchResult("control_data_macro.eval_prescriber_name")
                .inputSet("select  a.actual_value,a.origin_id,a.group_id,a.root_pipeline_id, a.extracted_value,a.confidence_score,a.file_name,a.tenant_id, a.batch_id\n" +
                        "FROM control_data_macro.prescriber_name_similarity_input a\n" +
                        "join control_data.control_data_payload_queue_archive b on a.origin_id=b.origin_id")
                .resourceConn("intics_zio_db_conn")

                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.masterdata.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("database.decryption.activator", "false"),
                Map.entry("copro.intelli-match.url","http://192.168.10.248:9200/v2/models/cos-service/versions/1/infer"),
                Map.entry("decryptApiUrl","http://192.168.10.248:10001/copro-utils/data-security/decrypt"),
                Map.entry("apiUrl", "http://192.168.10.248:10001/copro-utils/data-security/encrypt"),
                Map.entry("encryption.activator","true"),
                Map.entry("consumer.intellimatch.API.count","1"),
                Map.entry("write.batch.size", "5")));

        IntellimatchAction action1 = new IntellimatchAction(actionExecutionAudit, log, build);
        action1.execute();
    }
}
