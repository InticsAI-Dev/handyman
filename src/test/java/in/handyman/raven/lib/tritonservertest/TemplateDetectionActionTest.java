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
                .ouputTable("macro.template_detection_response_12345999")
                .resourceConn("intics_agadia_db_conn")
                .processId("12345")
                .querySet(" select  ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path as file_path, ar.tenant_id\n" +
                        ",ar.template_id ,ar.process_id ,ar.root_pipeline_id , sq.model_registry_id,\n" +
                        "(jsonb_agg(json_build_object('question', (sq.question), 'questionId', sq.question_id, 'synonymId', st.synonym_id, 'sorItemName', 'template_name')))::varchar as questions, b.batch_id\n" +
                        "from info.auto_rotation ar\n" +
                        "join sor_meta.sor_tsynonym st on 1=1\n" +
                        "join sor_meta.sor_question sq on st.synonym_id  =sq.synonym_id\n" +
                        "join preprocess.preprocess_payload_queue b on ar.origin_id=b.origin_id and b.tenant_id=ar.tenant_id and b.batch_id = ar.batch_id\n" +
                        "where st.synonym ='Template Name' and ar.status ='COMPLETED' and ar.group_id='6' and sq.tenant_id=1 and sq.weights=150 and b.batch_id='BATCH-6_0'\n" +
                        "group by ar.origin_id ,ar.paper_no ,ar.group_id ,ar.processed_file_path , ar.tenant_id ,ar.template_id ,ar.process_id ,ar.root_pipeline_id,sq.question_id,st.synonym_id,b.batch_id\n")
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
                .coproUrl("http://192.168.10.248:9600/v2/models/argon-vqa-service/versions/1/infer")
                .inputTable("info.auto_rotation")
                .ouputTable("macro.template_detection_response_12345999")
                .resourceConn("intics_zio_db_conn")
                .processId("12345")
                .querySet("SELECT \n" +
                        "    ar.origin_id,\n" +
                        "    ar.paper_no,\n" +
                        "    ar.group_id,\n" +
                        "    ar.processed_file_path AS file_path,\n" +
                        "    ar.tenant_id,\n" +
                        "    ar.template_id,\n" +
                        "    ar.process_id,\n" +
                        "    ar.root_pipeline_id,\n" +
                        "    sq.model_registry_id,\n" +
                        "    (\n" +
                        "        jsonb_agg(\n" +
                        "            json_build_object(\n" +
                        "                'question', sq.question, \n" +
                        "                'questionId', sq.question_id, \n" +
                        "                'synonymId', st.synonym_id, \n" +
                        "                'sorItemName', 'template_name'\n" +
                        "            )\n" +
                        "        )\n" +
                        "    )::varchar AS questions, \n" +
                        "    b.batch_id\n" +
                        "FROM \n" +
                        "    info.auto_rotation ar\n" +
                        "JOIN \n" +
                        "    sor_meta.sor_tsynonym st ON 1=1\n" +
                        "JOIN \n" +
                        "    sor_meta.sor_question sq ON st.synonym_id = sq.synonym_id\n" +
                        "JOIN \n" +
                        "    preprocess.preprocess_payload_queue b ON ar.origin_id = b.origin_id \n" +
                        "        AND b.tenant_id = ar.tenant_id \n" +
                        "        AND b.batch_id = ar.batch_id\n" +
                        "WHERE \n" +
                        "    st.synonym = 'Template Name' \n" +
                        "    AND ar.status = 'COMPLETED' \n" +
                        "    AND ar.group_id = '6' \n" +
                        "    AND sq.tenant_id = 1 \n" +
                        "    AND sq.weights = 150 \n" +
                        "    AND b.batch_id = 'BATCH-6_0'\n" +
                        "GROUP BY \n" +
                        "    ar.origin_id, \n" +
                        "    ar.paper_no, \n" +
                        "    ar.group_id, \n" +
                        "    ar.processed_file_path, \n" +
                        "    ar.tenant_id, \n" +
                        "    ar.template_id, \n" +
                        "    ar.process_id, \n" +
                        "    ar.root_pipeline_id, \n" +
                        "    sq.model_registry_id, \n" +
                        "    b.batch_id;\n" )
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.template.detection.url","http://192.168.10.239:10193/copro/attribution/kvp-printed");
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