package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ValidationLlm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class ValidationLlmActionTest {
    @Test
    public void validationLlmActionTestCopro() throws Exception {
        ValidationLlm validationLlm = ValidationLlm.builder()
                .name("Validation llm macro")
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://192.168.10.240:10196/copro/mistral_llm_based_on_image")
                .outputTable("score.validation_llm_output_table")
                .querySet("select vt.root_pipeline_id , dara.action_id , 'VALIDATION' as process, '/data/input/blank.pdf' as input, '/data/' as output_dir, \n" +
                        "'As-is' as task, 'None' as section_header,vp.prompt_value as prompt, vt.origin_id , vt.paper_no ,\n" +
                        "vt.group_id , dara.process_id , si.sor_item_id , si.sor_item_name , vt.sor_question , vp.question_id, \n" +
                        "vt.synonym_id , vt.answer , vt.vqa_score , vt.weight , vt.created_user_id , vt.tenant_id , vt.created_on , \n" +
                        "'VALIDATION_LLM' as validation_name, vt.b_box , vt.model_registry, 'string' as text\n" +
                        "from macro.docnet_attribution_response_audit dara\n" +
                        "join sor_transaction.vqa_transaction vt on vt.sor_question = dara.question and dara.root_pipeline_id = vt.root_pipeline_id and dara.tenant_id = vt.tenant_id \n" +
                        "join sor_meta.sor_item si on si.sor_item_name = vt.sor_item_name \n" +
                        "join score.validation_llm_prompt_value_table vp on vp.origin_id = vt.origin_id and vp.tenant_id = dara.tenant_id and vp.paper_no = vt.paper_no \n" +
                        "and vp.sor_item_name  = vt.sor_item_name and vp.question_id = vt.question_id  \n" +
                        "where vt.root_pipeline_id = 20340 and vt.tenant_id = 1 and vt.origin_id ='ORIGIN-37'\n")
                .condition(true)
                .build();


        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(13898007L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "1"),
                Map.entry("validation.llm.consumer.API.count", "1"),
                Map.entry("root_pipeline_id", "1"),
                Map.entry("action_id", "100"),
                Map.entry("write.batch.size", "1"),
                Map.entry("triton.request.validation.llm.activator", "false"))
        );

        ValidationLlmAction validationLlmAction = new ValidationLlmAction(actionExecutionAudit, log, validationLlm);
        validationLlmAction.execute();


    }
}
