package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TemplateDetection;
import in.handyman.raven.lib.TemplateDetectionAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class TemplateDetectionActionTest {

    @Test
    public void detectionAction() throws Exception {

        TemplateDetection templateDetection=TemplateDetection.builder()
                .condition(true)
                .name("template detection")
                .coproUrl("http://localhost:10193/copro/attribution/kvp-printed-old")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345")
                .resourceConn("intics_agadia_db_conn")
                .processId("12345")
                .querySet("select  'INT-1' as origin_id , 1 as paper_no ,1 as group_id , '/data/output/pdf_to_image/SYNT_166838894_c1/SYNT_166838894_c1_0.jpg' as file_path,'TNT-1' as tenant_id\n" +
                        ",'TMP-1' as template_id ,134 as process_id ,12435 as root_pipeline_id , '{\"what is patient name\"}'  as questions\n"
                )
                                                                                        .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://localhost:10182/copro/preprocess/text_extraction");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        TemplateDetectionAction templateDetectionAction=new TemplateDetectionAction(actionExecutionAudit,  log,templateDetection);
        templateDetectionAction.execute();
    }


    @Test
    void tritonServer() throws Exception {
        TemplateDetection templateDetection=TemplateDetection.builder()
                .condition(true)
                .name("template detection")
                .coproUrl("http://192.168.10.239:10193/copro/attribution/kvp-printed")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("select  'INT-1' as origin_id , 1 as paper_no ,1 as group_id , '/data/output/auto_rotation/h_hart_packet_0.jpg' as file_path,1 as tenant_id\n" +
                        ",'TMP-1' as template_id ,134 as process_id ,12435 as root_pipeline_id , ARRAY['what is logo']  as questions" )
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://192.168.10.239:10193/copro/attribution/kvp-printed");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size","5")));

        TemplateDetectionAction templateDetectionAction=new TemplateDetectionAction(actionExecutionAudit,  log,templateDetection);
        templateDetectionAction.execute();
    }
    }