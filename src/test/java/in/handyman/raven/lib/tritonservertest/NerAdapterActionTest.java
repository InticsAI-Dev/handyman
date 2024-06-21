package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.NerAdapter;
import in.handyman.raven.lib.NerAdapterAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class NerAdapterActionTest {

    @Test
    void testCase() throws Exception {
        NerAdapter nerAdapter= NerAdapter.builder()
                .name("ner")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultSet("\n" +
                        "SELECT dp.sor_item_name as sor_key,si.sor_item_id, dp.sor_question as question, dp.answer as input_value, dp.weight,dp.vqa_score,\n" +
                        "si.allowed_adapter , si.restricted_adapter,dp.synonym_id,dp.question_id, '123' as process_id,\n" +
                        "si.word_limit , si.word_threshold ,\n" +
                        "si.char_limit , si.char_threshold ,\n" +
                        "si.validator_threshold , si.allowed_characters ,\n" +
                        "si.comparable_characters, si.restricted_adapter_flag,\n" +
                        "dp.origin_id ,dp.paper_no ,dp.group_id,\n" +
                        "dp.created_user_id, dp.tenant_id,dp.b_box, dp.model_registry, dp.root_pipeline_id, dp.batch_id\n" +
                        "FROM sor_transaction.vqa_transaction dp\n" +
                        "JOIN sor_meta.sor_item si ON si.sor_item_name = dp.sor_item_name\t\n" +
                        "join sor_transaction.sor_transaction_payload_queue st on st.origin_id=dp.origin_id\n" +
                        "WHERE dp.group_id = '10' AND si.allowed_adapter ='ner'\n" +
                        "AND dp.answer is not null AND dp.tenant_id = 1 and dp.batch_id = 'BATCH-10_0'\n")
                .resultTable("sor_transaction.adapter_result_12345")

                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.text-validation.url","http://192..168.10.248:9100/v2/models/ner-service/versions/1/infer");
        actionExecutionAudit.setProcessId(12345L);
        actionExecutionAudit.setRootPipelineId(12345L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("ner.consumer.API.count","1"),
                Map.entry("validaiton.char-limit-count","10"),
                Map.entry("validation.multiverse-mode","true"),
                Map.entry("validation.restricted-answers","true"),
                Map.entry("write.batch.size","5")));

        NerAdapterAction action=new NerAdapterAction(actionExecutionAudit,log,nerAdapter);
        action.execute();
    }



    @Test
    void tritonServer() throws Exception {
        NerAdapter nerAdapter= NerAdapter.builder()
                .name("ner")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultSet(" SELECT distinct dp.sor_item_name as sor_key,si.sor_item_id, dp.sor_question as question, dp.answer as input_value, dp.weight,dp.vqa_score,\n" +
                        "                     si.allowed_adapter , si.restricted_adapter ,dp.synonym_id, dp.question_id,'12345' as process_id,\n" +
                        "                     si.word_limit , si.word_threshold ,\n" +
                        "                     si.char_limit , si.char_threshold ,\n" +
                        "                     si.validator_threshold , si.allowed_characters ,\n" +
                        "                     si.comparable_characters, si.restricted_adapter_flag,\n" +
                        "                     dp.origin_id ,dp.paper_no ,dp.group_id,\n" +
                        "                     dp.created_user_id, dp.root_pipeline_id, dp.tenant_id,dp.b_box,dp.model_registry\n" +
                        "                     FROM sor_transaction.vqa_transaction dp\n" +
                        "                     JOIN sor_meta.sor_item si ON si.sor_item_name = dp.sor_item_name\n" +
                        "                     WHERE dp.group_id = '23' AND si.allowed_adapter ='ner' AND dp.answer is not null" +
                        " AND dp.sor_item_name ='patient_name';\n" +
                        "   ")
                .resultTable("sor_transaction.adapter_result_12345")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.text-validation.url","http://192.168.10.248:9100/v2/models/ner-service/versions/1/infer");
        actionExecutionAudit.setProcessId(12345L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("validaiton.char-limit-count","1"),
                Map.entry("validation.multiverse-mode","true"),
                Map.entry("validation.restricted-answers","-"),
                Map.entry("ner.consumer.API.count","1"),
                Map.entry("write.batch.size","5"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("actionId","1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("paper.itemizer.consumer.API.count", "1")
                ));

        NerAdapterAction action=new NerAdapterAction(actionExecutionAudit,log,nerAdapter);
        action.execute();
    }

}