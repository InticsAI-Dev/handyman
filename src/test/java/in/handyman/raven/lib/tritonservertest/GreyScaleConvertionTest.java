package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AutoRotationAction;
import in.handyman.raven.lib.GreyScaleConversionAction;
import in.handyman.raven.lib.model.AutoRotation;
import in.handyman.raven.lib.model.GreyScaleConversion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class GreyScaleConvertionTest {

    @Test
    void greyScaleConversion() throws Exception {

        GreyScaleConversion greyScaleConversion = GreyScaleConversion.builder()
                .name("grey scale conversion after copro optimization")
                .processId("138980")
                .resourceConn("intics_zio_db_conn")
                .endPoint("http://192.168.10.245:10197/copro/preprocess/grey-scale-conversion")
                .outputTable("info.grey_Scale_conversion")
                .outputDir("/data/")
                .condition(true)
                .querySet(" SELECT  distinct a.origin_id, 300 as group_id,a.processed_file_path as file_path,a.paper_no,a.tenant_id,a.template_id,a.process_id, 1 as root_pipeline_id\n" +
                        "                                    FROM info.paper_itemizer a\n" +
                        "                                     where origin_id in ('ORIGIN-514', 'ORIGIN-515', \n" +
                        "                                     'ORIGIN-516')")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.setRootPipelineId(12345L);
        actionExecutionAudit.setActionId(2345L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("greyscale.conversion.consumer.API.count", "10"),
                Map.entry("triton.grey.scale.conversion.request.activator", "false"),
                Map.entry("write.batch.size", "5"))
        );

        GreyScaleConversionAction action1 = new GreyScaleConversionAction(actionExecutionAudit, log, greyScaleConversion);
        action1.execute();
    }

        @Test
    void tritonServer() throws Exception {
        AutoRotation action = AutoRotation.builder()
                .name("auto rotation testing after copro optimization")
                .processId("138980184199100180")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/output/")
                .condition(true)
                .querySet(" SELECT a.origin_id,a.group_id,a.processed_file_path as file_path,a.paper_no,a.tenant_id,a.template_id,a.process_id, 1 as root_pipeline_id\n" +
                        "\t\t            FROM info.paper_itemizer a\n" +
                        "\t\t             where origin_id ='INT-1'")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.autorotation.url", "http://192.168.10.239:10181/copro/preprocess/auto-rotation");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("auto.rotation.consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        AutoRotationAction action1 = new AutoRotationAction(actionExecutionAudit, log, action);
        action1.execute();


    }

}