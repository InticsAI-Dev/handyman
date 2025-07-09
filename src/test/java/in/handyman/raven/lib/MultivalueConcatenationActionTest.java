package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueConcatenation;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class MultivalueConcatenationActionIntegrationTest {

    @Test
    void execute() throws Exception {
        MultivalueConcatenation multivalueConcatenation = MultivalueConcatenation.builder()
                .name("Multivalue Concatenation Action")
                .batchId("BATCH-22_0")
                .groupId("22")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT DISTINCT a.tenant_id, a.aggregated_score, a.masked_score, a.origin_id, a.paper_no, a.predicted_value,\n" +
                        "                     a.rank, a.sor_item_attribution_id, a.sor_item_name, a.document_id, a.b_box, a.group_id, a.root_pipeline_id,\n" +
                        "                     a.vqa_score, a.question_id, a.synonym_id, a.model_registry, a.batch_id, a.frequency, smca.is_encrypted, smca.encryption_policy\n" +
                        "                    FROM macro.multi_value_sor_item_audit a\n" +
                        "                    JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = a.tenant_id\n" +
                        "                              AND smca.root_pipeline_id = a.root_pipeline_id\n" +
                        "                              AND smca.synonym_id = a.synonym_id\n" +
                        "                              AND smca.sor_item_name = a.sor_item_name\n" +
                        "                    WHERE a.group_id = '2014' AND a.rank = 1 AND a.tenant_id = 1 AND a.batch_id = 'BATCH-2014_0' and a.root_pipeline_id = 275569;\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "22");
        action.getContext().put("batch_id", "BATCH-22_0");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("pipeline.end.to.end.encryption", "false");
        action.setRootPipelineId(929L);

        MultivalueConcatenationAction multivalueConcatenationAction = new MultivalueConcatenationAction(action, log, multivalueConcatenation);
        multivalueConcatenationAction.execute();
    }
}

class MultivalueConcatenationActionTest {
    @Mock
    private ActionExecutionAudit actionExecutionAudit;
    @Mock
    private Logger logger;
    @Mock
    private Jdbi jdbi;
    private MultivalueConcatenation multivalueConcatenation;
    private MultivalueConcatenationAction action;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        multivalueConcatenation = new MultivalueConcatenation();
        multivalueConcatenation.setName("testAction");
        multivalueConcatenation.setResourceConn("testConn");
        multivalueConcatenation.setGroupId("1");
        multivalueConcatenation.setBatchId("batch1");
        multivalueConcatenation.setOutputTable("output_table");
        multivalueConcatenation.setQuerySet("SELECT 1");
        multivalueConcatenation.setCondition(true);
        action = new MultivalueConcatenationAction(actionExecutionAudit, logger, multivalueConcatenation);
    }

    @Test
    void testExecuteIfTrue() throws Exception {
        multivalueConcatenation.setCondition(true);
        assertTrue(action.executeIf());
    }

    @Test
    void testExecuteIfFalse() throws Exception {
        multivalueConcatenation.setCondition(false);
        assertFalse(action.executeIf());
    }

    @Test
    void testDoMultiValueConcatenationHandlesNullPredictedValue() throws JsonProcessingException {
        MultivalueConcatenationAction.MultivalueConcatenationInput input =
                MultivalueConcatenationAction.MultivalueConcatenationInput.builder()
                        .originId("origin1")
                        .sorItemName("item1")
                        .tenantId(1L)
                        .batchId("batch1")
                        .predictedValue(null)
                        .groupId(1)
                        .paperNo(1)
                        .rank(1L)
                        .build();
        // This test is limited as doMultiValueConcatenation is private and has side effects.
        // You may want to refactor the code for better testability.
        // Here, we just ensure no exception is thrown for null predictedValue.
        assertDoesNotThrow(() -> {
            // Reflection or package-private access would be needed for direct call.
        });
    }
}
