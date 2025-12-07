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
                .endpoint("http://172.202.112.23:8000/predict")
                .outputTable("transit_data.radon_kvp_output_3")
                .querySet("\n" +
                        "SELECT '/Users/anandh.andrews/intics-workspace/data/1/transaction/TRZ-283/1760546700213-b720e00f-e0f5-4134-a1d9-013c120d686c/processed_images/15-10-2025_10_10_01/tenant_1/group_274/preprocess/paper_itemizer/pdf_to_image/processed/MCD_P10_OB extension_overstay/MCD_P10_OB extension_overstay_4.png' as input_file_path, a.user_prompt, a.process, a.paper_no, a.origin_id, a.process_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.system_prompt,'KRYPTON' as model_name,\n" +
                        "                    a.batch_id, a.model_registry, a.category, now() as created_on, \n" +
                        "                    'KRYPTON' as api_name,sc.post_processing::bool as post_process,\n" +
                        "                    sc.post_process_class_name as post_process_class_name,sc.sor_container_id,sc.sor_container_name\n" +
                        "                    FROM sor_transaction.radon_kvp_input_audit a\n" +
                        "                    JOIN sor_meta.sor_container sc on a.sor_container_id=sc.sor_container_id\n" +
                        "WHERE a.model_registry = 'RADON' and a.tenant_id=1 and a.batch_id ='BATCH-15_0' and a.sor_container_id =1442;\n")
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
