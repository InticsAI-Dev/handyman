package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LlmJsonParserAction;
import in.handyman.raven.lib.model.LlmJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;


@Slf4j
public class LlmJsonParserActionTest {
    @Test
    public void tritonTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transaction.llm_json_parser_output_audit")
                .querySet("SELECT a.response as response, a.paper_no, a.origin_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                        "a.model_registry, a.category, a.created_on,a.sor_container_id, a.sor_meta_detail,a.image_dpi, a.image_width, a.image_height\n" +
                        "from sor_transaction.llm_json_parser_input_audit a\n" +
                        "WHERE root_pipeline_id =17290;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("sor.transaction.bbox.activator.enable", "true");
        ac.getContext().put("sor.transaction.confidence.activator.enable", "true");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("llm.json.parser.consumer.API.count","10");
        ac.getContext().put("copro.processor.thread.creator", "FIXED_THREAD");
        ac.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");


        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();


    }
}
