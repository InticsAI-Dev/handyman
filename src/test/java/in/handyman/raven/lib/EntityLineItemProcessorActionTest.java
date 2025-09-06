package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EntityLineItemProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;

@Slf4j
class EntityLineItemProcessorActionTest {

    @Test
    void execute() throws Exception {

        EntityLineItemProcessor radonKvp = EntityLineItemProcessor.builder()
                .name("multi entity line item processor for sor transaction")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("https://intics.elevance.ngrok.dev/radon-vllm-server/predict")
                .outputTable("sor_transaction.radon_kvp_output_1")
                .querySet("SELECT a.input_file_path, a.user_prompt, a.process, a.paper_no, a.origin_id, a.process_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.system_prompt,a.model_name,\n" +
                        "a.batch_id, a.model_registry, a.category, now() as created_on, (CASE WHEN 'RADON' = 'RADON' then 'RADON START'\n" +
                        "WHEN 'RADON' = 'KRYPTON' then 'KRYPTON START'\n" +
                        "WHEN 'RADON' = 'NEON' then 'NEON START' end) as api_name,\n" +
                        "sc.sor_container_id, mep.container_name, entity, process_name as radon_process_name,\n" +
                        "priority_order, postprocessing_script\n" +
                        "FROM transit_data.radon_multi_section_kvp_input_1 a\n" +
                        "JOIN sor_meta.sor_container sc on a.sor_container_id=sc.sor_container_id\n" +
                        "join paper_filter.agentic_container_filter_base_table_audit acfbta\n" +
                        "on acfbta.sor_container_id = a.sor_container_id and a.origin_id = acfbta.origin_id and a.paper_no = acfbta.paper_no\n" +
                        "join sor_meta.multi_entity_processing mep\n" +
                        "on mep.sor_container_id= acfbta.sor_container_id and mep.status = true and acfbta.container_name = mep.entity\n" +
                        "WHERE a.model_registry = 'RADON'  and a.group_id ='6' and a.tenant_id='1' and a.batch_id ='BATCH-6_0';")
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
        ac.getContext().put("copro.client.socket.timeout", "10");
        ac.getContext().put("copro.client.api.sleeptime", "10");
        ac.getContext().put("pipeline.copro.api.process.file.format", "BASE64");
        ac.getContext().put("pipeline.encryption.default.holder", "");
        ac.getContext().put(ENCRYPT_TEXT_EXTRACTION_OUTPUT, "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("document_type", "MEDICAL_GBD");
        ac.getContext().put("pipeline.req.res.encryption", "true");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "TRITON");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("root-pipeline-name", "root.processor#6");


        EntityLineItemProcessorAction entityLineItemProcessorAction = new EntityLineItemProcessorAction(ac, log, radonKvp);

        entityLineItemProcessorAction.execute();
    }
}