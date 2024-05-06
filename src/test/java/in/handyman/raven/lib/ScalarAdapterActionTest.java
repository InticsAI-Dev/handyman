package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ScalarAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ScalarAdapterActionTest {

    @Test
    void execute() throws Exception {

        final ScalarAdapter build = ScalarAdapter.builder()
                .condition(true)
                .name("scalar adapter for docnut attribution")
                .processID("1234567")
                .resultSet("SELECT dp.sor_item_name as sor_key,si.sor_item_id, dp.sor_question as question, dp.answer as input_value, dp.weight,dp.vqa_score,\n" +
                        "si.allowed_adapter , si.restricted_adapter ,dp.synonym_id, dp.question_id,'${init_process_id.process_id}' as process_id,\n" +
                        "si.word_limit , si.word_threshold ,\n" +
                        "si.char_limit , si.char_threshold ,\n" +
                        "si.validator_threshold , si.allowed_characters ,\n" +
                        "si.comparable_characters, si.restricted_adapter_flag,\n" +
                        "dp.origin_id ,dp.paper_no ,dp.group_id,\n" +
                        "dp.created_user_id, dp.tenant_id,dp.b_box, dp.model_registry, dp.root_pipeline_id, dp.batch_id\n" +
                        "FROM sor_transaction.vqa_transaction dp\n" +
                        "JOIN sor_meta.sor_item si ON si.sor_item_name = dp.sor_item_name\n" +
                        "join sor_transaction.sor_transaction_payload_queue st on st.origin_id=dp.origin_id\n" +
                        "WHERE dp.group_id = '2298' AND si.allowed_adapter !='ner'\n" +
                        "AND dp.answer is not null and dp.tenant_id = 81;\n")

                .resourceConn("intics_zio_db_conn")

                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("validation.multiverse-mode","true");
        action.getContext().put("validation.restricted-answers","No,None of the above");
        action.getContext().put("validaiton.char-limit-count","1");
        action.getContext().put("copro.text-validation.url", "http://192.168.10.248:9100/v2/models/ner-service/versions/1/infer");
        final ScalarAdapterAction scalarAdapterAction = new ScalarAdapterAction(action, log, build);
        scalarAdapterAction.execute();

    }
}