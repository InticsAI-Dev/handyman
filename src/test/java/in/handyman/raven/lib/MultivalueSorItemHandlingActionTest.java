package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueSorItemHandling;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultivalueSorItemHandlingActionTest {

    private static final Logger log = LoggerFactory.getLogger(MultivalueSorItemHandlingActionTest.class);

    @Test
    void testMultivalueSorItemHandlingExecution() throws Exception {
        // 1. Build the Configuration Model using your provided SQL query
        MultivalueSorItemHandling config = MultivalueSorItemHandling.builder()
                .name("Multivalue SOR Handling for Service Codes")
                .resourceConn("intics_zio_db_conn")
                .condition(true)
                .outputTable("sor_transform.sor_item_multivalue_filtering_output")
                .querySet("SELECT\n" +
                        "    vqa.simfi_id as multi_entity_filtering_id,\n" +
                        "    vqa.answer,\n" +
                        "    vqa.origin_id,\n" +
                        "    vqa.paper_no,\n" +
                        "    vqa.vqa_score AS score,\n" +
                        "    vqa.sor_item_name,\n" +
                        "    vqa.batch_id,\n" +
                        "    vqa.line_item_type,\n" +
                        "    vqa.is_encrypted,\n" +
                        "    vqa.sor_container_instance,\n" +
                        "    vqa.is_multi_entity_enabled::varchar as is_multi_entity_enabled,\n" +
                        "    vqa.sor_container_name,\n" +
                        "    vqa.section_alias,\n" +
                        "    vqa.whitelisted_sections\n" +
                        "FROM sor_transform.sor_item_multivalue_filtering_input vqa\n" +
                        "WHERE vqa.batch_id='BATCH-42_0' AND vqa.group_id='42' AND vqa.tenant_id=1;")
                .build();

        // 2. Setup ActionExecutionAudit context with required environment flags
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(5443L);
        actionExecutionAudit.setActionId(21352L);

        // Map required context variables used inside the Action
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("batch_id", "BATCH-42_0"),
                Map.entry("gen_group_id.group_id", "42"),
                Map.entry("tenant_id", "1"),
                Map.entry("pipeline.item.wise.encryption", "false"), // Set to false to bypass SecurityEngine in test
                Map.entry("kvp.json.parser.encryption", "false"),
                Map.entry("write.batch.size", "5")
        ));

        // 3. Initialize the Action
        // This assumes that ResourceAccess has been initialized with "intics_zio_db_conn"
        // in your test environment (e.g., via an H2 database or Mockito)
        MultivalueSorItemHandlingAction action = new MultivalueSorItemHandlingAction(
                actionExecutionAudit,
                log,
                config
        );

        // 4. Validate query integrity before execution
        String query = config.getQuerySet();
        assertTrue(query.toUpperCase().contains("FROM SOR_TRANSFORM.SOR_ITEM_MULTIVALUE_FILTERING_INPUT"),
                "Query must reference the correct multi-entity input table");
        assertTrue(query.contains("vqa.sor_container_instance"),
                "Query must include container instance for deduplication logic to work");

        // 5. Execute Action
        // This will trigger the handleMultiEntitySingleValueLineItems logic
        assertDoesNotThrow(() -> {
            if (action.executeIf()) {
                action.execute();
                log.info("MultivalueSorItemHandlingAction executed successfully.");
            } else {
                log.warn("Action condition was false; execution skipped.");
            }
        }, "Execution should not throw exceptions. Ensure DB connection is mocked if no real DB is available.");
    }
}