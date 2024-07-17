package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.P2pNameValidation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class P2pNameValidationActionTest {

    @Test
    void execute() throws Exception {

        P2pNameValidation p2pNameValidation = P2pNameValidation.builder()
                .name("p2p name concatenation action")
                .processId("123456")
                .querySet("SELECT 'test bbox' as p2p_first_Name_bBox , 'test bbox' as p2p_last_name_bBox," +
                        "                    90 as p2p_first_name_confidence_score, 80 as p2p_last_name_confidence_score," +
                        "                    'micky' as p2p_first_name, 'mouse' as p2p_last_name, 0 as p2p_first_name_filter_score, 0 as p2p_last_name_filter_score," +
                        "                    1 as group_id, 80 as p2p_first_name_maximum_score, 70 as p2p_last_name_maximum_score," +
                        "                    'ORIGIN-123' as origin_id, 1 as paper_no, 50884 as root_pipeline_id, 1 as tenant_id, 'patient_name' as sor_item_name")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.getContext().put("p2p.url", "http://localhost:10181/copro/preprocess/autorotation");
        actionExecutionAudit.getContext().put("p2p.output.table", "cumulative_result_p2p_result_table_1");
        actionExecutionAudit.getContext().put("p2p.schema.name", "voting");
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("p2p.name.concat.consumer.API.count", "1"),
                Map.entry("root_pipeline_id", "1"),
                Map.entry("action_id", "100"),
                Map.entry("write.batch.size", "1"))
        );

        P2pNameValidationAction p2pNameValidationAction = new P2pNameValidationAction(actionExecutionAudit, log, p2pNameValidation);
        p2pNameValidationAction.execute();


    }
}