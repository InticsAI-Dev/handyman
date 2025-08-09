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
                .querySet("SELECT\n" +
                        "                    b.total_response_json AS response,\n" +
                        "                    b.paper_no,\n" +
                        "                    a.origin_id,\n" +
                        "                    a.group_id,\n" +
                        "                    a.tenant_id,\n" +
                        "                    a.root_pipeline_id,\n" +
                        "                    a.batch_id,\n" +
                        "                    b.model_registry,\n" +
                        "                    b.category,\n" +
                        "                    a.created_on,\n" +
                        "                    si.sor_container_id,\n" +
                        "                    si.sor_item_name,\n" +
                        "                    jsonb_agg(\n" +
                        "                        jsonb_build_object(\n" +
                        "                            'sorItemName', si.sor_item_name,\n" +
                        "                            'isEncrypted', si.is_encrypted,\n" +
                        "                            'encryptionPolicy', ep.encryption_policy\n" +
                        "                        )\n" +
                        "                    )::varchar AS sor_meta_detail\n" +
                        "                FROM sor_transaction.sor_transaction_payload_queue_archive  a\n" +
                        "                LEFT JOIN transit_data.kvp_transformer_output_36549 b\n" +
                        "                    ON a.origin_id = b.origin_id\n" +
                        "                    AND a.batch_id = b.batch_id\n" +
                        "                    AND a.tenant_id = b.tenant_id\n" +
                        "                LEFT JOIN sor_meta.sor_item si\n" +
                        "                    ON si.sor_item_id = b.sor_item_id\n" +
                        "                JOIN sor_meta.encryption_policies ep\n" +
                        "                    ON ep.encryption_policy_id = si.encryption_policy_id\n" +
                        "                WHERE a.tenant_id = 1 and a.group_id ='183' and a.batch_id ='BATCH-183_0'\n" +
                        "                group by b.total_response_json, b.paper_no,a.origin_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                        "                                b.model_registry, b.category, a.created_on,si.sor_container_id,si.sor_item_name;")
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
        ac.getContext().put("double.parser.string","true");


        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();


    }
}
