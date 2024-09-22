package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CurrencyDetectionAction;
import in.handyman.raven.lib.model.CurrencyDetection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class CurrencyDetectionActionTest {

    @Test
    void currencyDetectionTest() throws Exception {

        CurrencyDetection action = CurrencyDetection.builder()
                .name("currency detection testing after copro optimization")
                .processId("138980")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .condition(true)
                .endPoint("http://192.168.10.245:10187/copro/currency-attribution")
                .querySet("select origin_id,\n" +
                        "paper_no,\n" +
                        "input_file_path as file_path,\n" +
                        "tenant_id,\n" +
                        "template_id,\n" +
                        "process_id,\n" +
                        "group_id,\n" +
                        "root_pipeline_id,\n" +
                        "batch_id\n" +
                        "from macro.currency_detection_payload_input_table_audit\n" +
                        "where group_id=1414 and batch_id ='BATCH-292_0';                           \n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.currency.detection.url", "http://0.0.0.0:10187/copro/currency-attribution");
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.setRootPipelineId(13898007L);
        actionExecutionAudit.setActionId(13898007L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("currency.detection.consumer.API.count", "1"),
                Map.entry("root_pipeline_id", "1"),
                Map.entry("action_id", "100"),
                Map.entry("triton.request.currency.detection.activator", "false"),
                Map.entry("write.batch.size", "1"))
        );

        CurrencyDetectionAction action1 = new CurrencyDetectionAction(actionExecutionAudit, log, action);
        action1.execute();


    }

    @Test
    void currencyDetectionTritonServer() throws Exception {
        CurrencyDetection action = CurrencyDetection.builder()
                .name("currency detection testing after copro optimization")
                .processId("138980")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .condition(true)
                .endPoint("http://0.0.0.0:10187/copro/currency-attribution")
                .querySet("select 'INT-1' as origin_id,1 as group_id,'/data/output/grey_scale_converted_output/2022-10-26T9_58_10 Dooliquor LLC_0.jpg' as file_path,1 as paper_no,1 as tenant_id,'TMP-1' as template_id,'138980744174170252' as process_id\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.currency.detection.url", "http://0.0.0.0:10187/copro/currency-attribution");
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.setRootPipelineId(13898007L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("currency.detection.consumer.API.count", "1"),
                Map.entry("root_pipeline_id", "1"),
                Map.entry("action_id", "100"),
                Map.entry("triton.request.currency.detection.activator", "false"),
                Map.entry("write.batch.size", "5"))
        );

        CurrencyDetectionAction action1 = new CurrencyDetectionAction(actionExecutionAudit, log, action);
        action1.execute();


    }
}