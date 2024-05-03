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
                .coproUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("\tselect  'INT-1' as origin_id , 1 as paper_no ,1 as group_id , '/data/output/244/preprocess/autorotation/auto_rotation/SYNT_166838894_c6_1.jpg' as file_path,\"1 as tenant_id\\n ,\n" +
                        "                        TMP-1' as template_id ,\n" +
                        "                        134 as process_id ,12435 as root_pipeline_id , \n" +
                        "                        '[{\"question\": \"Find out the template name\", \"synonym_id\": 114, \"question_id\": 244, \"sor_item_name\": \"template_name\"},\n" +
                        "                         {\"question\": \"what is the biggest word in the document\", \"synonym_id\": 114, \"question_id\": 248, \"sor_item_name\": \"template_name\"},\n" +
                        "                         {\"question\": \"what is pharmacy\", \"synonym_id\": 114, \"question_id\": 627, \"sor_item_name\": \"template_name\"}]'  as questions"
                )
                                                                                        .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer");
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
                .coproUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("\tselect  'INT-1' as origin_id , 1 as paper_no ,1 as group_id , '/data/output/244/preprocess/autorotation/auto_rotation/SYNT_166838894_c6_1.jpg' as file_path, 1 as tenant_id,\n" +
                                "                        'TMP-1' as template_id ,\n" +
                                "                        134 as process_id ,12435 as root_pipeline_id , \n" +
                                "                        '[{\"question\": \"Find out the template name\", \"synonym_id\": 114, \"question_id\": 244, \"sor_item_name\": \"template_name\"},\n" +
                                "                         {\"question\": \"what is the biggest word in the document\", \"synonym_id\": 114, \"question_id\": 248, \"sor_item_name\": \"template_name\"},\n" +
                                "                         {\"question\": \"what is pharmacy\", \"synonym_id\": 114, \"question_id\": 627, \"sor_item_name\": \"template_name\"}]'  as questions"
                        )
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size","5")));

        TemplateDetectionAction templateDetectionAction=new TemplateDetectionAction(actionExecutionAudit,  log,templateDetection);
        templateDetectionAction.execute();
    }
    }