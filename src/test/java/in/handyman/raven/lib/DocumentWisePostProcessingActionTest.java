package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocumentWisePostProcessing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class DocumentWisePostProcessingActionTest {

    @Mock
    private ActionExecutionAudit action;

    @Mock
    private Logger log;

    private DocumentWisePostProcessing config;
    private DocumentWisePostProcessingAction actionInstance;
    private Map<String, String> context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = new HashMap<>();
        context.put("tenant_id", "1");
        context.put("group_id", "2014");
        context.put("batch_id", "BATCH-24_0");
        context.put("created_user_id", "1");
        context.put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        context.put("document.wise.post.processing.thread.count", "5");

        when(action.getContext()).thenReturn(context);
        when(action.getRootPipelineId()).thenReturn(10984L);

        config = DocumentWisePostProcessing.builder()
                .name("DocumentWisePostProcessing")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("test.document_wise_post_processing_output")
                .querySet("SELECT * FROM test.document_wise_post_processing_input")
                .build();

        actionInstance = new DocumentWisePostProcessingAction(action, log, config);
    }

    @Test
    void testExecuteIf_ReturnsTrue() throws Exception {
        assertTrue(actionInstance.executeIf());
    }

    @Test
    void testExecuteIf_ReturnsFalse() throws Exception {
        config.setCondition(false);
        actionInstance = new DocumentWisePostProcessingAction(action, log, config);
        assertFalse(actionInstance.executeIf());
    }

    @Test
    void testExecuteIf_WithNullCondition() throws Exception {
        // When condition is null, executeIf() will throw NullPointerException
        // This is expected behavior as condition should always be set
        config.setCondition(null);
        actionInstance = new DocumentWisePostProcessingAction(action, log, config);
        assertThrows(NullPointerException.class, () -> actionInstance.executeIf());
    }

    @Test
    void testConfigValues() {
        assertEquals("DocumentWisePostProcessing", config.getName());
        assertEquals("intics_zio_db_conn", config.getResourceConn());
        assertEquals("test.document_wise_post_processing_output", config.getOutputTable());
        assertEquals("SELECT * FROM test.document_wise_post_processing_input", config.getQuerySet());
        assertTrue(config.getCondition());
    }

    @Test
    void testContextValues() {
        assertEquals("1", context.get("tenant_id"));
        assertEquals("2014", context.get("group_id"));
        assertEquals("BATCH-24_0", context.get("batch_id"));
        assertEquals("true", context.get(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION));
        assertEquals("5", context.get("document.wise.post.processing.thread.count"));
    }

    @Test
    void testActionInstanceCreation() {
        assertNotNull(actionInstance);
        assertNotNull(action);
        assertNotNull(log);
        assertNotNull(config);
    }
}
