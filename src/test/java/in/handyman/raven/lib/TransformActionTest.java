package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.Transform;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class TransformActionTest {
//
//
//    @Test
//    void testSelect(){
//        Transform transform=Transform.builder()
//                .name("transform")
//                .on("intics_agadia_db_conn")
//                .format(false)
//                .value(Collections.singletonList("INSERT INTO ref_data.validation_results (\n" +
//                        "                        root_pipeline_id,\n" +
//                        "                        po_number,\n" +
//                        "                        tax_amount,\n" +
//                        "                        tax_amount_status,\n" +
//                        "                        invoice_amount,\n" +
//                        "                        description,\n" +
//                        "                        file_name\n" +
//                        "                    )\n" +
//                        "                    SELECT\n" +
//                        "                        ${gen_id.root_pipeline_id} AS root_pipeline_id,\n" +
//                        "                        (\n" +
//                        "                            SELECT dara.predicted_attribution_value\n" +
//                        "                            FROM macro.docnet_attribution_response_audit dara\n" +
//                        "                            INNER JOIN sor_meta.sor_question sq ON dara.question = sq.question\n" +
//                        "                            INNER JOIN sor_meta.sor_tsynonym st ON st.synonym_id = sq.synonym_id\n" +
//                        "                            WHERE st.sor_item_id = 1\n" +
//                        "                            AND dara.root_pipeline_id = '${gen_id.root_pipeline_id}'\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as po_number,\n" +
//                        "                        (\n" +
//                        "                            SELECT COALESCE(CAST(dara.predicted_attribution_value AS NUMERIC), 0.0)\n" +
//                        "                            FROM macro.docnet_attribution_response_audit dara\n" +
//                        "                            INNER JOIN sor_meta.sor_question sq ON dara.question = sq.question\n" +
//                        "                            INNER JOIN sor_meta.sor_tsynonym st ON st.synonym_id = sq.synonym_id\n" +
//                        "                            WHERE st.sor_item_id = 5\n" +
//                        "                            AND dara.root_pipeline_id = '${gen_id.root_pipeline_id}'\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as tax_amount,\n" +
//                        "                        (\n" +
//                        "                            SELECT\n" +
//                        "                                CASE\n" +
//                        "                                    WHEN COALESCE(CAST(dara.predicted_attribution_value AS NUMERIC), 0.0) > 0 THEN 'yes'\n" +
//                        "                                    ELSE 'no'\n" +
//                        "                                END\n" +
//                        "                            FROM macro.docnet_attribution_response_audit dara\n" +
//                        "                            INNER JOIN sor_meta.sor_question sq ON dara.question = sq.question\n" +
//                        "                            INNER JOIN sor_meta.sor_tsynonym st ON st.synonym_id = sq.synonym_id\n" +
//                        "                            WHERE st.sor_item_id = 5\n" +
//                        "                            AND dara.root_pipeline_id = '${gen_id.root_pipeline_id}'\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as tax_amount_status,\n" +
//                        "                        (\n" +
//                        "                            SELECT COALESCE(CAST(NULLIF(dara.predicted_attribution_value, '') AS NUMERIC), 0.0)\n" +
//                        "                            FROM macro.docnet_attribution_response_audit dara\n" +
//                        "                            INNER JOIN sor_meta.sor_question sq ON dara.question = sq.question\n" +
//                        "                            INNER JOIN sor_meta.sor_tsynonym st ON st.synonym_id = sq.synonym_id\n" +
//                        "                            WHERE st.sor_item_id = 2\n" +
//                        "                            AND dara.root_pipeline_id = '${gen_id.root_pipeline_id}'\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as invoice_amount,\n" +
//                        "                        (\n" +
//                        "                            SELECT c.description\n" +
//                        "                            FROM rcnn_padd.cm1882524_0_0 c\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as description,\n" +
//                        "                        (\n" +
//                        "                            SELECT a.file_path\n" +
//                        "                            FROM info.asset a\n" +
//                        "                            WHERE file_extension = 'pdf' and root_pipeline_id='${gen_id.root_pipeline_id}'\n" +
//                        "                            LIMIT 1\n" +
//                        "                        ) as file_name;"))
//                .condition(true)
//                .build();
//
//
//        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
//        actionExecutionAudit.getContext().put("copro.text-validation.url","http://localhost:10190/copro/text-validation/patient");
//        actionExecutionAudit.setProcessId(138980079308730208L);
//        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
//                Map.entry("gen_group_id.group_id","5"),
//                Map.entry("consumer.API.count","1"),
//                Map.entry("write.batch.size","5"),
//                Map.entry("gen_id.root_pipeline_id","2533")));
//
//        TransformAction transformAction=new TransformAction(actionExecutionAudit,log,transform);
//        transformAction.execute();
//    }
//


}