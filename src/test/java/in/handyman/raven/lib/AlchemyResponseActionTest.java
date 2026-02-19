package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class AlchemyResponseActionTest {

    @Test
    void execute() throws Exception {

        AlchemyResponse alchemyResponse = AlchemyResponse.builder()
                .name("alchemy response action")
                .token("")
                .tenantId(1L)
                .querySet("SELECT\n" +
                        "    cr.origin_id,\n" +
                        "    cr.paper_no,\n" +
                        "    cr.tenant_id,\n" +
                        "    cr.root_pipeline_id,\n" +
                        "    cr.confidence AS confidence_score,\n" +
                        "    NULL AS extracted_value,\n" +
                        "    cr.sor_item_name,\n" +
                        "    st.synonym_id,\n" +
                        "    NULL AS question_id,\n" +
                        "    'CHECKBOX_EXTRACTION' AS feature,\n" +
                        "    NULL AS state,\n" +
                        "    json_agg(\n" +
                        "        json_build_object(\n" +
                        "            'label_value', cr.sor_item_label,\n" +
                        "            'attribution_status', cr.answer,\n" +
                        "            'bbox', cr.bbox\n" +
                        "        )\n" +
                        "    ) AS checkbox_data,\n" +
                        "    ampq.batch_id\n" +
                        "\n" +
                        "FROM checkbox_extraction.checkbox_extraction_llm_json_parser_output_audit cr\n" +
                        "\n" +
                        "JOIN alchemy_migration.alchemy_migration_payload_queue_archive ampq\n" +
                        "    ON ampq.origin_id = cr.origin_id\n" +
                        "   AND ampq.batch_id = cr.batch_id\n" +
                        "\n" +
                        "JOIN sor_meta.sor_item si\n" +
                        "    ON si.sor_item_name = cr.sor_item_name\n" +
                        "   AND si.sor_container_id = cr.sor_container_id\n" +
                        "\n" +
                        "JOIN sor_meta.sor_tsynonym st\n" +
                        "    ON st.feature = 'CHECKBOX_EXTRACTION'\n" +
                        "   AND st.sor_item_id = si.sor_item_id\n" +
                        "   AND si.sor_container_id = st.sor_container_id\n" +
                        "\n" +
                        "WHERE ampq.group_id = 83\n" +
                        "  AND ampq.tenant_id = 1\n" +
                        "  AND ampq.batch_id = 'BATCH-83_0'\n" +
                        "  AND si.status = 'ACTIVE'\n" +
                        "\n" +
                        "GROUP BY\n" +
                        "    cr.origin_id,\n" +
                        "    cr.paper_no,\n" +
                        "    cr.tenant_id,\n" +
                        "    cr.root_pipeline_id,\n" +
                        "    cr.confidence,\n" +
                        "    cr.sor_item_name,\n" +
                        "    st.synonym_id,\n" +
                        "    ampq.batch_id;")
                .resourceConn("intics_zio_db_conn")
                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setRootPipelineId(1L);
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/predictions-list/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("alchemy.response.consumer.API.count","10");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("alchemy.response.output.table","alchemy_migration.alchemy_transform_output_table");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}