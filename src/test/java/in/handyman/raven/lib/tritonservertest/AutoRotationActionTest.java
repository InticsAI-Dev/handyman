package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AutoRotationAction;
import in.handyman.raven.lib.model.AutoRotation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class AutoRotationActionTest {

    @Test
    void autoRotationTest() throws Exception {

        AutoRotation action = AutoRotation.builder()
                .name("auto rotation testing after copro optimization")
                .processId("138980")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .condition(true)
                .querySet("select 'INT-1' as origin_id,1 as group_id,'/data/input/2_0.jpg' as file_path,1 as paper_no,1 as tenant_id,'TMP-1' as template_id,'138980744174170252' as process_id, '1' as batch_id \n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.autorotation.url", "http://192.168.10.248:8200/v2/models/auto-rotator-service/versions/1/infer");
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("root_pipeline_id","1"),
                Map.entry("action_id","100"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("write.batch.size", "5"))
        );

        AutoRotationAction action1 = new AutoRotationAction(actionExecutionAudit, log, action);
        action1.execute();


    }

    @Test
    void tritonServer() throws Exception {
        AutoRotation action = AutoRotation.builder()
                .name("auto rotation testing after copro optimization")
                .endPoint("http://192.168.10.248:8200/v2/models/auto-rotator-service/versions/1/infer")
                .processId("138980184199100180")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .condition(true)
                .querySet(" SELECT a.origin_id,a.group_id,a.processed_file_path as file_path,a.paper_no,a.tenant_id,a.template_id,a.process_id, 1 as root_pipeline_id , '1' as batch_id\n" +
                        "\t\t            FROM info.paper_itemizer a\n" +
                        "\t\t             where origin_id ='INT-1'")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.autorotation.url", "http://192.168.10.239:10181/copro/preprocess/auto-rotation");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("auto.rotation.consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        AutoRotationAction action1 = new AutoRotationAction(actionExecutionAudit, log, action);
        action1.execute();


    }

}