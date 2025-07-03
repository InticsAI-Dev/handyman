package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import in.handyman.raven.lib.model.ErrorResponseJsonTrigger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ErrorResponseJsonTriggerActionTest {

    @Test
    void testingAssetInfo() throws Exception {
        ErrorResponseJsonTrigger errorResponseJsonTrigger= ErrorResponseJsonTrigger.builder()
                .name("ErrorResponseJsonTrigger")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT 'TRZ-01' as transaction_id,document_id, batch_id, origin_id, group_id, uploaded_on, process_failed_on, total_processed_duration,\n" +
                        "total_no_of_pages, failed_on_stage, stages, message, root_pipeline_id, tenant_id, 'FAILED' as pipelineStatus\n" +
                        "FROM outbound.error_document_detail\n" +
                        "WHERE group_id = '5' AND tenant_id = 1 AND batch_id = 'BATCH-5_0';")
                .tenantId("1")
                .endpoint("http://localhost:8189/alchemy/api/v1/product-outbound/error-details/create")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("process-id","1234567");
        action.getContext().put("read.batch.size","100");
        action.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNzUxNjQxMzI4LCJpYXQiOjE3NTE1NTQ5MjgsImVtYWlsIjoiYW50aGVtLWVsZXZhbmNlQGludGljcy5haSJ9.bzQujGG1GoblSMnN_WY2NKqPUity7KkjCCZV8g03p-c");
        ErrorResponseJsonTriggerAction errorResponseJsonTriggerAction=new ErrorResponseJsonTriggerAction(action ,log, errorResponseJsonTrigger);
        errorResponseJsonTriggerAction.execute();
    }

}