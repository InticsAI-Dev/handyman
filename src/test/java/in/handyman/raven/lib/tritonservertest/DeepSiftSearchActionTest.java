package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.DeepSiftSearchAction;
import in.handyman.raven.lib.model.DeepSiftSearch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class DeepSiftSearchActionTest {

    @Test
    void tritonServer() throws Exception {
        // Ensure the class name matches exactly with your model class
        DeepSiftSearch deepSiftSearch = DeepSiftSearch.builder()
                .name("search filteration for group_id 104 for batch id BATCH-104_0")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .endPoint("http://localhost:8001/xenon-textract")
                .processId("8037")
                .resultTable("deep_sift.deep_search_output_audit")
                .querySet("SELECT\n" +
                        "dsi.origin_id,\n" +
                        "dsi.group_id,\n" +
                        "dsi.created_on,\n" +
                        "dsi.created_by,\n" +
                        "dsi.extracted_text,\n" +
                        "dsi.root_pipeline_id,\n" +
                        "dsi.tenant_id,\n" +
                        "dsi.batch_id,\n" +
                        "dsi.paper_no,\n" +
                        "dsi.source_document_type,\n" +
                        "dsi.sor_container_id,\n" +
                        "dsi.sor_container_name,\n" +
                        "dsi.sor_item_id,\n" +
                        "dsi.sor_item_name,\n" +
                        "dsi.search_id,\n" +
                        "dsi.search_name,\n" +
                        "dsi.keywords\n" +
                        "FROM deep_sift.deep_search_input_audit dsi\n" +
                        "WHERE dsi.root_pipeline_id=1455;")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.data-extraction.url", "http://localhost:8001/xenon-textract");
        actionExecutionAudit.setProcessId(8037L);
        actionExecutionAudit.setActionId(42182L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("okhttp.client.timeout", "20"),
                Map.entry("text.extraction.consumer.API.count", "1"),
                Map.entry("deep.sift.consumer.API.count", "1"),
                Map.entry("deep.sift.page.content.min.length.threshold", "1"),
                Map.entry("triton.request.activator", "true"),
                Map.entry("pipeline.deep.sift.encryption", "false"),
                Map.entry("copro.request.deep.sift.handler.name", "TRITON"),
                Map.entry("replicate.request.api.token", ""),
                Map.entry("actionId", "42182"),
                Map.entry("write.batch.size", "1"),
                Map.entry("page.content.min.length.threshold", "1"),
                Map.entry("deep.sift.extraction.activator", "true")
        ));
        DeepSiftSearchAction deepSiftSearchAction = new DeepSiftSearchAction(actionExecutionAudit, log, deepSiftSearch);
        deepSiftSearchAction.execute();
    }
}