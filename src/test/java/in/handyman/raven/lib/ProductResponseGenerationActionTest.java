package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ProductResponseGeneration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ProductResponseGenerationActionTest {

    @Test
    void execute() throws Exception {
        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("read.batch.size", "5");
        action.getContext().put("write.batch.size", "5");
        action.getContext().put("product.response.generation.consumer.API.count", "1");
        action.getContext().put("product.response.generation.consumer.url", "http://localhost:8189/");
        action.getContext().put("alchemy.base.url", "http://localhost:8189");
        action.getContext().put("outbound.file.uri.format.specifer", "api");

        ProductResponseGeneration productResponseGeneration = ProductResponseGeneration.builder()
                .name("Product Response Generation Action")
                .tenantId("1")
                .token("sample-token")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("sor_transform.product_response_generation_output")
                .querySet("SELECT " +
                        "1::bigint as prediction_id, " +
                        "now() as created_on, " +
                        "'KIE' as feature, " +
                        "'ORIGIN-1' as origin_id, " +
                        "0.95::double precision as precision, " +
                        "'sample value' as predicted_value, " +
                        "'101' as root_pipeline_id, " +
                        "1::bigint as tenant_id, " +
                        "'TXN-1' as transaction_id, " +
                        "1 as paper_no, " +
                        "'{}' as metadata_json, " +
                        "101::bigint as group_id, " +
                        "'BATCH-1' as batch_id, " +
                        "'ContainerA' as container_name, " +
                        "'ItemA' as sor_item_name")
                .build();

        ProductResponseGenerationAction actionObj = new ProductResponseGenerationAction(action, log, productResponseGeneration);
        actionObj.execute();
    }
}
