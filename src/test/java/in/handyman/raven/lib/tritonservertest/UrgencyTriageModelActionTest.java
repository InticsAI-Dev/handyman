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
                .endPoint("http://192.168.10.241:7500/v2/models/radon-service/versions/1/infer")
                .querySet("SELECT a.origin_id, a.preprocessed_file_id, a.paper_no, a.input_file_path, a.created_user_id, a.last_updated_user_id, a.tenant_id, a.template_id, a.process_id, a.model_registry_id, a.group_id, a.root_pipeline_id, a.batch_id,\n" +
                        "b.base_prompt as prompt\n" +
                        "FROM macro.ut_payload_input_table_audit a\n" +
                        "left join sor_meta.radon_prompt_table b on a.tenant_id = b.tenant_id and b.process='RADON_UT'\n" +
                        "WHERE a.group_id='52' AND  a.root_pipeline_id='82012' and a.batch_id = 'BATCH-52_0'\n")
                .resourceConn("intics_zio_db_conn")
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.urgency-triage-model.url", "http://192.168.10.239:10194/copro/urgency-triage");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("ut.consumer.API.count", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("ut.legacy.api.call.configs", "false"),
                Map.entry("write.batch.size", "5")));

        UrgencyTriageModelAction action1 = new UrgencyTriageModelAction(actionExecutionAudit, log, urgencyTriageModel);
        action1.execute();


    }
}
