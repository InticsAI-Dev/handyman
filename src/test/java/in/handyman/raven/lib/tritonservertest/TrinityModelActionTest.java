package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.TrinityModelAction;
import in.handyman.raven.lib.model.TrinityModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class TrinityModelActionTest {

    @Test
    void executeHandwritten() throws Exception {

        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("/data/output/")
                .requestUrl("http://triton.copro.valuation.handwritten:9000/v2/models/xenon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT 'what is group number' as question, '/data/output/749/preprocess/autorotation/auto_rotation/Rachel_David_1.jpg' as file_path,'Handwritten' as paperType;")
                .responseAs("sor_transaction_tqa_123456")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://triton.copro.valuation.handwritten:9000/v2/models/xenon-vqa-service/versions/1/infer");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setRootPipelineId(12345678L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("tenant_id","1"),
                Map.entry("triton.request.activator","true"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }



    @Test
    void executePrinted() throws Exception {

        TrinityModel trinityModel=TrinityModel.builder()
                .name("DIE model testing")
                .condition(true)
                .outputDir("/data/output/")
                .requestUrl("http://192.168.10.248:8900/v2/models/argon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT (jsonb_agg(json_build_object('question', (a.questions), 'questionId', a.question_id, 'synonymId', a.synonym_id, 'sorItemName', a.sor_item_name)))::varchar as attributes, \n" +
                        "a.file_path,a.paper_type, a.model_registry,  a.origin_id,a.paper_no,a.tenant_id, a.group_id, a.category as qn_category , a.root_pipeline_id, 1 as process_id , 1 as model_registry_id\n" +
                        "FROM macro.sor_transaction_final_tqa_audit a\n" +
                        "join sor_transaction.sor_transaction_payload_queue_archive st on st.origin_id=a.origin_id\n" +
                        "where a.model_registry = 'ARGON' and a.category = 'PRIMARY'\n" +
                        "and a.tenant_id = 1 and st.group_id='297'\n" +
                        "group by a.file_path, a.paper_type, a.model_registry,a.origin_id, a.paper_no , a.tenant_id,a.group_id ,a.category, a.root_pipeline_id LIMIT 1;\n")
                .responseAs("sor_transaction.vqa_transaction")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://triton.copro.valuation.printed:8900/v2/models/argon-vqa-service/versions/1/infer");
        actionExecutionAudit.getContext().put("okhttp.client.timeout","20");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("action_id", "1");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setRootPipelineId(12345678L);
        actionExecutionAudit.setActionId(1L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("tenant_id","1"),
                Map.entry("triton.request.activator","true"),
                Map.entry("write.batch.size","5")));

        TrinityModelAction trinityModelAction=new TrinityModelAction(actionExecutionAudit,log,trinityModel);
        trinityModelAction.execute();
    }
}