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
                    .endpoint("http://0.0.0.0:7999/predict")
                .outputTable("sor_transaction.radon_kvp_output_audit_1")
                .querySet("SELECT '/data/processed_images/11-11-2025_04_11_40/tenant_1/group_91/preprocess/paper_itemizer/pdf_to_image/processed/COMM_P2_INREQ_3/COMM_P2_INREQ_3_2.png' as input_file_path,\n" +
                        "a.user_prompt, a.process, a.paper_no, a.origin_id, a.process_id, 898 as group_id, a.tenant_id,\n" +
                        "a.root_pipeline_id, a.system_prompt,a.model_name,   'batch-id-01' as  batch_id, a.model_registry, a.category, now() as created_on,\n" +
                        "(CASE WHEN 'KRYPTON' = 'RADON' then 'RADON START'    \n" +
                        "WHEN 'KRYPTON' = 'KRYPTON' then 'KRYPTON START'    \n" +
                        "WHEN 'KRYPTON' = 'NEON' then 'NEON START' end) as api_name,sc.post_processing::bool as post_process,sc.post_process_class_name as post_process_class_name,sc.sor_container_id, request_id    \n" +
                        "FROM transit_data.radon_kvp_input_6708 a    \n" +
                        "JOIN sor_meta.sor_container sc on a.sor_container_id=sc.sor_container_id    \n" +
                        "WHERE a.model_registry = 'RADON'  and a.group_id ='89' and a.tenant_id='1' and a.batch_id ='BATCH-89_0';")
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
        ac.getContext().put("pipeline.encryption.default.holder", "INBUILT_AES");
        ac.getContext().put(ENCRYPT_TEXT_EXTRACTION_OUTPUT, "false");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("document_type", "HEALTH_CARE");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "RUNPOD");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("ProviderTransformerFinalBsh", "ProviderTransformerFinalBsh");
        ac.getContext().put("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt");
        ac.getContext().put("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt");
        ac.getContext().put("legacy.resource.connection.type", "LEGACY");
        ac.getContext().put("pipeline.req.res.encryption", "false");
        ac.getContext().put("copro.isretry.enabled", "true");




        RadonKvpAction radonKvpAction = new RadonKvpAction(ac, log, radonKvp);

        radonKvpAction.execute();
    }
}
