package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.RadonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;
import static in.handyman.raven.core.enums.NetworkHandlerConstants.COPRO_CLIENT_SOCKET_TIMEOUT;

@Slf4j
public class RadonKvpAcionTest {
    @Test
    public void tritonTest() throws Exception {
        RadonKvp radonKvp = RadonKvp.builder()
                .name("radon kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("http://172.202.112.23/predict")
                .outputTable("sor_transaction.radon_kvp_output_audit")
                .querySet("SELECT\n" +
                        "    pl.file_path AS input_file_path,\n" +
                        "    pv.base_prompt AS user_prompt,\n" +
                        "    CASE\n" +
                        "        WHEN 'false' = 'false' THEN 'RADON_KVP_ACTION'\n" +
                        "        ELSE 'RADON_BBOX_ACTION'\n" +
                        "    END AS process,\n" +
                        "    pl.paper_no,\n" +
                        "    stpq.origin_id,\n" +
                        "    stpq.root_pipeline_id AS process_id,\n" +
                        "    stpq.group_id,\n" +
                        "    stpq.tenant_id,\n" +
                        "    stpq.root_pipeline_id,\n" +
                        "    'RADON' AS model_registry,\n" +
                        "    stpq.batch_id,\n" +
                        "    'PRIMARY' AS category,\n" +
                        "    NOW() AS created_on,\n" +
                        "    pv.system_prompt AS system_prompt,\n" +
                        "     (CASE WHEN '${sor.kvp.service.name.activator}' = 'RADON' then 'RADON START'\n" +
                        "     WHEN '${sor.kvp.service.name.activator}' = 'KRYPTON' then 'KRYPTON START'\n" +
                        "     WHEN '${sor.kvp.service.name.activator}' = 'NEON' then 'NEON START' end) as api_name,sc.post_processing::bool as post_process,sc.post_process_class_name as post_process_class_name,sc.sor_container_id\n" +
                        "FROM sor_transaction.sor_transaction_payload_queue_archive stpq\n" +
                        "JOIN paper_filter.agentic_entity_level_score_audit pl\n" +
                        "    ON pl.origin_id = stpq.origin_id\n" +
                        "    AND pl.tenant_id = stpq.tenant_id\n" +
                        "    AND pl.batch_id = stpq.batch_id\n" +
                        "  JOIN sor_meta.sor_container sc on \n" +
                        "  pl.sor_container_id=sc.sor_container_id\n" +
                        "JOIN sor_meta.radon_prompt_table pv\n" +
                        "    ON pl.tenant_id = pv.tenant_id\n" +
                        "    AND pl.sor_container_id = pv.sor_container_id\n" +
                        "WHERE stpq.group_id = '4'\n" +
                        "  AND stpq.tenant_id = '1'\n" +
                        "  AND pv.document_type = 'MEDICAL_COMMERCIAL'\n" +
                        "  AND pv.status = 'ACTIVE'\n" +
                        "  AND pv.version = '1'\n" +
                        "  AND pv.process = CASE\n" +
                        "                       WHEN 'false' = 'false' THEN 'RADON_KVP_BBOX'\n" +
                        "                       ELSE 'RADON_KVP'\n" +
                        "                   END\n" +
                        "  AND stpq.batch_id = 'BATCH-4_0'\n" +
                        "  and pl.paper_no =3\n" +
                        "  AND pl.is_candidate_paper = 'yes';\n")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("text.to.replace.prompt", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("prompt.base64.activator", "false");
        ac.getContext().put(COPRO_CLIENT_SOCKET_TIMEOUT, "10");
        ac.getContext().put("copro.client.api.sleeptime", "10");
        ac.getContext().put("pipeline.copro.api.process.file.format", "BASE64");
        ac.getContext().put("pipeline.encryption.default.holder", "");
        ac.getContext().put(ENCRYPT_TEXT_EXTRACTION_OUTPUT, "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("document_type", "MEDICAL_GBD");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "TRITON");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("ProviderTransformerFinalBsh", "ProviderTransformerFinalBsh");
        ac.getContext().put("MemberTransformerFinalBsh", "MemberTransformerFinalBsh");



        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();
    }
}
