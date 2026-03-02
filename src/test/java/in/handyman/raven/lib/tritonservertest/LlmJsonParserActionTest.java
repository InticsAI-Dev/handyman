package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LlmJsonParserAction;
import in.handyman.raven.lib.model.LlmJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;


@Slf4j
public class LlmJsonParserActionTest {
    @Test
    public void tritonTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn_tsar")
                .outputTable("public.llm_json_parser_output_audit")
                .querySet("SELECT row_number() over(order by id) as id,a.response as response, a.paper_no, a.origin_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                        "                    a.model_registry, a.category, a.created_on,a.sor_container_id, a.sor_meta_detail,a.image_dpi, a.image_width, a.image_height,a.sor_container_instance\n" +
                        "                    from extraction.llm_json_parser_input_audit a\n" +
                        "                    WHERE sor_container_id =1444 and origin_id ='ORIGIN-1060' AND a.response IS NOT NULL and id not in (10045,9989)")
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
        ac.getContext().put("llm.json.parser.consumer.API.count","1");
        ac.getContext().put("copro.processor.thread.creator", "FIXED_THREAD");
        ac.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        ac.getContext().put("llm.json.parser.label.encryption", "false");
        ac.getContext().put("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt");
        ac.getContext().put("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt");

        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();


    }
}
