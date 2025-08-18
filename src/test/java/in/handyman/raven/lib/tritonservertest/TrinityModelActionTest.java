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
                .requestUrl("http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT \n" +
                        "    (jsonb_agg(\n" +
                        "        json_build_object(\n" +
                        "            'question', (a.questions), \n" +
                        "            'questionId', a.question_id, \n" +
                        "            'synonymId', a.synonym_id, \n" +
                        "            'sorItemName', a.sor_item_name)\n" +
                        "        )\n" +
                        "    )::varchar as attributes,\n" +
                        "    a.file_path,\n" +
                        "    a.paper_type, \n" +
                        "    a.model_registry,  \n" +
                        "    a.origin_id,\n" +
                        "    a.paper_no,\n" +
                        "    a.tenant_id, \n" +
                        "    st.group_id, \n" +
                        "    a.root_pipeline_id, \n" +
                        "    a.model_registry_id, \n" +
                        "    '${process-id}' as process_id, \n" +
                        "    a.batch_id\n" +
                        "FROM macro.sor_transaction_final_tqa_audit a\n" +
                        "JOIN sor_transaction.sor_transaction_payload_queue_archive st \n" +
                        "ON st.origin_id = a.origin_id\n" +
                        "WHERE a.model_registry = 'ARGON'\n" +
                        "AND a.tenant_id = 1 \n" +
                        "AND st.group_id = '72' \n" +
                        "AND a.batch_id = 'BATCH-72_3'\n" +
                        "GROUP BY \n" +
                        "    a.file_path, \n" +
                        "    a.paper_type, \n" +
                        "    a.model_registry,\n" +
                        "    a.origin_id, \n" +
                        "    a.paper_no, \n" +
                        "    a.tenant_id,\n" +
                        "    st.group_id, \n" +
                        "    a.root_pipeline_id, \n" +
                        "    a.model_registry_id, \n" +
                        "    a.batch_id;")
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
                .requestUrl("http://triton.copro.valuation.printed:8900/v2/models/argon-vqa-service/versions/1/infer")
                .resourceConn("intics_zio_db_conn")
                .forkBatchSize("1")
                .questionSql("SELECT 'what is name' as question, '/data/resized.jpg' as file_path,'Printed' as paperType;")
                .responseAs("sor_transaction_tqa_123456")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.trinity-attribution.printed.url","http://triton.copro.valuation.printed:8900/v2/models/argon-vqa-service/versions/1/infer");
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
}