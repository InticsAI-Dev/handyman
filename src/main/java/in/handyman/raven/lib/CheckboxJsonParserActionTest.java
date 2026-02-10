package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

@Slf4j
public class CheckboxJsonParserActionTest {

    @Test
    public void tritonTest() throws Exception {
        CheckboxJsonParser checkboxJsonParser = CheckboxJsonParser.builder()
                .name("checkbox json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transaction.llm_json_parser_output_audit")
                .querySet(
                        "SELECT '{\"chbq_grps\": [{\"grid\": \"q_01\", \"sec_hdr\": \"Section Header\", \"qns_txt\": \"Question?\", \"grp_bbox\": [137, 433, 794, 457], \"opts\": [{\"l\": \"Urgent\", \"s\": \"C\"}, {\"l\": \"For Review\", \"s\": \"U\"}]}]}' as response, "
                                +
                                "1 as paper_no, 'origin_1' as origin_id, 12345::bigint as group_id, 1::bigint as tenant_id, 17290::bigint as root_pipeline_id, 'batch_1' as batch_id, "
                                +
                                "'registry_1' as model_registry, 'pixel' as extracted_image_unit, 300::bigint as image_dpi, 1000::bigint as image_width, 1000::bigint as image_height, "
                                +
                                "now()::timestamp as created_on, 'process_1' as process, '[]' as sor_meta_detail, 1::bigint as sor_container_id, 'item_label' as sor_item_label")
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
