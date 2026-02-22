package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class CheckboxJsonParserActionTest {

    private static final Logger log = LoggerFactory.getLogger(CheckboxJsonParserActionTest.class);

    @Test
    public void tritonTest() throws Exception {
        CheckboxJsonParser checkboxJsonParser = CheckboxJsonParser.builder()
                .name("checkbox json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("checkbox_extraction.checkbox_extraction_llm_json_parser_output_audit")
                .querySet(
                        "SELECT\n" +
                                "    a.response,\n" +
                                "    a.paper_no,\n" +
                                "    a.origin_id,\n" +
                                "    a.group_id,\n" +
                                "    a.tenant_id,\n" +
                                "    a.root_pipeline_id,\n" +
                                "    a.batch_id,\n" +
                                "    a.model_registry,\n" +
                                "    a.created_on,\n" +
                                "    a.sor_container_id,\n" +
                                "    a.image_dpi,\n" +
                                "    a.image_width,\n" +
                                "    a.image_height,\n" +
                                "    a.sor_item_name,\n" +
                                "    a.checkbox_keywords,\n" +
                                "    a.sor_meta_detail\n" +
                                "FROM checkbox_extraction.checkbox_extraction_llm_json_parser_input_audit a\n" +
                                "WHERE a.tenant_id = 1\n" +
                                "  AND a.group_id  = '31'")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("checkbox.json.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");

        CheckboxJsonParserAction checkboxJsonParserAction = new CheckboxJsonParserAction(ac, log, checkboxJsonParser);
        checkboxJsonParserAction.execute();
    }
}
