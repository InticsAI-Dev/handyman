package in.handyman.raven.lib.bsh;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.ProviderContainerParserAction;
import in.handyman.raven.lib.ZeroShotClassifierPaperFilterAction;
import in.handyman.raven.lib.model.ProviderContainerParser;
import in.handyman.raven.lib.model.ZeroShotClassifierPaperFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProviderContainerParserActionTest {

    @Test
    void execute() throws Exception {

        final ProviderContainerParser build = ProviderContainerParser.builder()
                .condition(true)
                .name("Test ZeroShotClassifier")
                .resourceConn("intics_zio_db_conn")
                .nameMappingDetails("select document_type,tenant_id,jsonb_object_agg(sor_item_name,sor_item_ref)::varchar AS sor_item_mappings_json \n" +
                        "from sor_meta_mapper.radon_prompt_json_sor_meta_mapper\n" +
                        "group by document_type,tenant_id;")
                .containerItemDetails("SELECT sc.document_type, sc.sor_container_name ,sc.tenant_id,  jsonb_agg(si.sor_item_name)::varchar AS sor_item_names_json\n" +
                        "from sor_meta.sor_container sc \n" +
                        "join sor_meta.sor_item si on sc.sor_container_id =si.sor_container_id\n" +
                        "where sc.document_type ='HEALTH_CARE' and sc.tenant_id =1 and sc.status ='ACTIVE' and si.status ='ACTIVE'\n" +
                        "group by sc.document_type,sc.sor_container_name ,sc.tenant_id;")
                .metaContainerEntityDetails("SELECT sc.document_type,sc.tenant_id,sc.sor_container_name , jsonb_agg(te.truth_entity_name) AS truth_entity_names_json\n" +
                        "from sor_meta.sor_container sc \n" +
                        "join sor_meta.truth_entity te on sc.sor_container_id =te.sor_container_id\n" +
                        "where sc.document_type ='HEALTH_CARE' and sc.tenant_id =1 and sc.status ='ACTIVE' and te.status ='ACTIVE'\n" +
                        "group by sc.document_type,sc.sor_container_name,sc.tenant_id ;")
                .querySet("select  response, paper_no, origin_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, category, created_on, sor_container_id, sor_meta_detail\n" +
                        "from sor_transaction.llm_json_parser_input_audit ljpoa \n" +
                        "where ljpoa.tenant_id =1 and ljpoa.id =235;")
                .paperFilterDetails("")
                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);

        action.getContext().put("gen_output_table.name","sor_transaction.llm_json_provider_parser_12345");

        final ProviderContainerParserAction providerContainerParserAction = new ProviderContainerParserAction(action, log, build);
        providerContainerParserAction.execute();
    }

}
