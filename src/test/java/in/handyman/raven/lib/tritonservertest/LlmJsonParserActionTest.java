package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.core.encryption.impl.ProtegrityApiEncryptionImpl;
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
                        "                   b.total_response_json as response, b.paper_no,a.origin_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                        "                  b.model_registry, b.category, a.created_on, si.sor_container_id,\n" +
                        "                                     jsonb_agg(jsonb_build_object('sorItemName',si.sor_item_name,'isEncrypted',si.is_encrypted,'encryptionPolicy',ep.encryption_policy))::varchar as sor_meta_detail\n" +
                        "                from sor_transaction.sor_transaction_payload_queue_archive a\n" +
                        "                left join sor_transaction.radon_kvp_output_audit b\n" +
                        "                    on a.origin_id = b.origin_id and a.batch_id = b.batch_id and a.tenant_id =b.tenant_id\n" +
                        "                left join sor_meta.sor_item si on si.sor_container_id =b.sor_container_id\n" +
                        "                join sor_meta.encryption_policies ep on ep.encryption_policy_id =si.encryption_policy_id\n" +
                        "                WHERE a.tenant_id = 1 and a.group_id ='12' and a.batch_id ='BATCH-12_0'\n" +
                        "                group by b.total_response_json, b.paper_no,a.origin_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.batch_id,\n" +
                        "                                b.model_registry, b.category, a.created_on,si.sor_container_id")
                .build();
        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("sor.transaction.bbox.activator.enable", "true");
        ac.getContext().put("sor.transaction.confidence.activator.enable", "true");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        ac.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        ac.getContext().put("protegrity.enc.api.url",encryptionUrl);
        ac.getContext().put("protegrity.dec.api.url",decryptionUrl);
        ac.getContext().put("llm.json.parser.trim.extracted.value","true");

        //ProtegrityApiEncryptionImpl protegrityApiEncryption=new ProtegrityApiEncryptionImpl(encryptionUrl,decryptionUrl, ac, log);
        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);
        llmJsonParserAction.execute();


    }
}
