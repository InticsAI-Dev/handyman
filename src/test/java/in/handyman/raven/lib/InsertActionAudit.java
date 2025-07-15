package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.InsertActionAudit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class InsertActionAuditTest {

    @Test
    void testInsertActionAuditExecution() throws Exception {
        // Step 1: Build InsertActionAudit object
        InsertActionAudit insertActionAudit = InsertActionAudit.builder()
                .name("InsertAction")
                .resourceConn("intics_zio_db_conn")
                .querySet(
                        "SELECT transaction_id, process_load_type, pipeline_name, " +
                                "start_time, end_time, context, tenant_id FROM audit.pipeline_plugin_audit"
                )
                .condition(true)
                .build();

        // Step 2: Build ActionExecutionAudit with required context
        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("transactionId", "txn123");
        action.getContext().put("processLoadType", "FULL");
        action.getContext().put("pipelineName", "DemoPipeline");
        action.getContext().put("tenantId", "1001");
        action.getContext().put("process-id", "1234567"); // optional additional key

        // Step 3: Create the action class instance
        ActionInsertActionAuditAction actionInsertActionAuditAction =
                new ActionInsertActionAuditAction(action, log, insertActionAudit);

        // Optional: Log or print objects for debugging
        System.out.println("ActionExecutionAudit: " + action);
        System.out.println("InsertActionAudit: " + insertActionAudit);

        // Step 4: Execute the logic
        actionInsertActionAuditAction.execute();
    }

}