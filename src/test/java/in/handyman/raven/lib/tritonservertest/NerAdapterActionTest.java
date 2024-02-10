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
                .resultSet("SELECT 'prescriber_name' as sor_key, 'what is prescriber name' as question, 'anandh' as input_value,150 as weight,'50' as vqa_score,\n" +
                        "                    'ner' as allowed_adapter , 'numeric_reg' as restricted_adapter ,'123456' as process_id,\n" +
                        "                    4 as word_limit , 15 as word_threshold ,\n" +
                        "                    60 as char_limit , 15 as char_threshold ,\n" +
                        "                    70 as validator_threshold , '-' as allowed_characters ,\n" +
                        "                    1 as restricted_adapter_flag,\n" +
                        "                    'INT-1' as origin_id ,1 as paper_no ,1 as group_id,\n" +
                        "                    1 as created_user_id, 1 as tenant_id,1 as question_id,1 as synonym_id")
                .resultTable("sor_transaction.adapter_result_12345")

                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.text-validation.url","http://192.168.10.239:10190/copro/text-validation/patient");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.setRootPipelineId(138980079308730208L);
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
                .resultSet("SELECT 'patient_name' as sor_key, 'what is provider name' as question, 'Christopher' as input_value, 150 as weight, allowed_adapter,restricted_adapter, '12345' as process_id" +
                        " ,word_limit, word_threshold, char_limit, char_threshold ,validator_threshold, allowed_characters, comparable_characters, restricted_adapter_flag,'INT-1' as origin_id ,1 as paper_no ,1 as group_id" +
                        " ,1 as created_user_id, 1 as tenant_id,'{\"left\":\"1234\"}' as b_box from sor_meta.sor_item where sor_item_name='patient_name'")
                .resultTable("sor_transaction.adapter_result_12345")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.text-validation.url","http://192.168.10.239:10190/copro/text-validation/patient");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("validaiton.char-limit-count","1"),
                Map.entry("validation.multiverse-mode","true"),
                Map.entry("validation.restricted-answers","-"),
                Map.entry("ner.consumer.API.count","1"),
                Map.entry("write.batch.size","5"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("actionId","1"),
                Map.entry("RootPipelineId","1"),
                Map.entry("triton.request.activator", "false"),
                Map.entry("paper.itemizer.consumer.API.count", "1")
                ));

        NerAdapterAction action=new NerAdapterAction(actionExecutionAudit,log,nerAdapter);
        action.execute();
    }

}