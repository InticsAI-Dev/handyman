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
                .coproUrl("http://192.168.10.248:9600/v2/models/argon-vqa-service/versions/1/infer")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_audit")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("select  ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path as file_path, ar.tenant_id\n" +
                        ",ar.template_id ,ar.process_id ,ar.root_pipeline_id , sq.model_registry_id,\n" +
                        "(jsonb_agg(json_build_object('question', (sq.question), 'questionId', sq.question_id, 'synonymId', st.synonym_id, 'sorItemName', 'template_name')))::varchar as questions, b.batch_id, now() as created_on\n" +
                        "from info.auto_rotation ar\n" +
                        "join sor_meta.sor_tsynonym st on 1=1\n" +
                        "join sor_meta.sor_question sq on st.synonym_id  =sq.synonym_id\n" +
                        "join preprocess.preprocess_payload_queue_archive b on ar.origin_id=b.origin_id and b.tenant_id=ar.tenant_id and b.batch_id = ar.batch_id\n" +
                        "where st.synonym ='Template Name' and ar.status ='COMPLETED' and ar.group_id='312' and sq.tenant_id=1 and sq.weights=150 and b.batch_id='BATCH-312_0'\n" +
                        "group by ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path , ar.tenant_id ,ar.template_id ,ar.process_id ,ar.root_pipeline_id,sq.model_registry_id,b.batch_id\n")
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
                .coproUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345999")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("select  ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path as file_path, ar.tenant_id \n" +
                        ",ar.template_id ,ar.process_id ,ar.root_pipeline_id , sq.model_registry_id, \n" +
                        "(jsonb_agg(json_build_object('question', (sq.question), 'questionId', sq.question_id, 'synonymId', st.synonym_id, 'sorItemName', 'template_name')))::varchar as questions, b.batch_id, now() as created_on \n" +
                        "from info.auto_rotation ar \n" +
                        "join sor_meta.sor_tsynonym st on 1=1 \n" +
                        "join sor_meta.sor_question sq on st.synonym_id  =sq.synonym_id \n" +
                        "join preprocess.preprocess_payload_queue_archive b on ar.origin_id=b.origin_id and b.tenant_id=ar.tenant_id and b.batch_id = ar.batch_id \n" +
                        "where st.synonym ='Template Name' and ar.status ='COMPLETED' and ar.group_id=312 and sq.tenant_id=1 and sq.weights=150 and b.batch_id='BATCH-312_0' \n" +
                        "group by ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path , ar.tenant_id ,ar.template_id ,ar.process_id ,ar.root_pipeline_id,sq.model_registry_id,b.batch_id;" )
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://192.168.10.24:10193/copro/attribution/kvp-printed");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","2"),
                Map.entry("consumer.API.count","2"),
                Map.entry("database.decryption.activator", "true"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("decryptionUrl","http://192.168.10.248:10045/copro-utils/data-security/decrypt"),
                Map.entry("actionId", "1"),
                Map.entry("write.batch.size","2")));

        TemplateDetectionAction templateDetectionAction=new TemplateDetectionAction(actionExecutionAudit,  log,templateDetection);
        templateDetectionAction.execute();
    }
    }