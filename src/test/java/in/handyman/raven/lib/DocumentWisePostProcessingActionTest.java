package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocumentWisePostProcessing;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_SELECT_READ_BATCH_SIZE;

class DocumentWisePostProcessingActionTest {

    @Mock
    private ActionExecutionAudit action;

    @Mock
    private Logger log;

    @Mock
    private InticsIntegrity crypt;

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
        context.put(DB_SELECT_READ_BATCH_SIZE, "10");
        context.put(DB_INSERT_WRITE_BATCH_SIZE, "10");

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
    @Disabled("Integration test: requires real DB tables, context substitutions, and encryption endpoints")
    void testDocumentWisePostProcessing_EndToEnd_InputToOutput() throws Exception {
        Assumptions.assumeTrue(
                System.getenv("JASYPT_ENCRYPTOR_PASSWORD") != null
                        && !System.getenv("JASYPT_ENCRYPTOR_PASSWORD").isBlank(),
                "Skipping integration test: JASYPT_ENCRYPTOR_PASSWORD is not set"
        );

        DocumentWisePostProcessing build = DocumentWisePostProcessing.builder()
                .condition(true)
                .name("transform into document-wise-post-processing output table")
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transform.document_wise_post_processing_output_audit")
                .batchId("${batch_id}")
                .querySet("""
                        SELECT
                          document_wise_post_processing_input_id, dwp.transaction_id, dwp.created_on,
                          dwp.created_user_id, dwp.last_updated_on, dwp.last_updated_user_id,
                          dwp.status, dwp.version, dwp.feature, dwp.label, dwp.left_pos,
                          dwp.lower_pos, dwp.right_pos, dwp.upper_pos, dwp.b_box, dwp.precision_val,
                          dwp.predicted_value, dwp.section_alias, dwp.sor_container_instance,
                          dwp.document_id, dwp.truth_id, dwp.channel_id, dwp.group_id,
                          dwp.origin_id, dwp.paper_no, dwp.question_id, dwp.root_pipeline_id,
                          dwp.score, dwp.sor_item_name, dwp.sor_question, dwp.synonym_id,
                          dwp.tenant_id, dwp.vqa_score, dwp.category, dwp.stage, dwp.batch_id,
                          dwp.line_item_type, dwp.is_encrypted, dwp.encryption_policy,
                          dwp.is_removed_after_filtering, dwp.message, dwp.sor_container_id,
                          dwp.truth_entity_id, dwp.sor_item_id, dwp.is_multi_entity_enabled
                        FROM sor_transform.document_wise_post_processing_input_audit dwp
                        WHERE dwp.tenant_id = 1
                          AND dwp.batch_id = 'BATCH-32_0'
                          AND dwp.group_id = 32
                        """)
                .build();

        ActionExecutionAudit integrationAction = ActionExecutionAudit.builder().build();
        integrationAction.setRootPipelineId(11011L);
        integrationAction.setProcessId(12345L);
        integrationAction.getContext().put("tenant_id", "1");
        integrationAction.getContext().put("group_id", "32");
        integrationAction.getContext().put("batch_id", "BATCH-32_0");
        integrationAction.getContext().put("temp_schema_name", "transit_data");
        integrationAction.getContext().put("document.wise.post.processing.activator", "true");
        integrationAction.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        integrationAction.getContext().put(DB_SELECT_READ_BATCH_SIZE, "180");
        integrationAction.getContext().put(DB_INSERT_WRITE_BATCH_SIZE, "100");
        integrationAction.getContext().put("document.wise.post.processing.thread.count", "10");
        integrationAction.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        integrationAction.getContext().put("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt");
        integrationAction.getContext().put("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt");

        DocumentWisePostProcessingAction integrationActionUnderTest =
                new DocumentWisePostProcessingAction(integrationAction, log, build);

        integrationActionUnderTest.execute();
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

    @Test
    void testDecryptLabels_UpdatesOnlyEncryptedLabeledRows() throws Exception {
        List<DocumentWisePostProcessingInput> inputs = new ArrayList<>();

        DocumentWisePostProcessingInput encryptedWithLabel = new DocumentWisePostProcessingInput();
        encryptedWithLabel.setIsEncrypted(true);
        encryptedWithLabel.setLabel("enc-label-1");
        inputs.add(encryptedWithLabel);

        DocumentWisePostProcessingInput notEncrypted = new DocumentWisePostProcessingInput();
        notEncrypted.setIsEncrypted(false);
        notEncrypted.setLabel("should-stay");
        inputs.add(notEncrypted);

        DocumentWisePostProcessingInput encryptedWithoutLabel = new DocumentWisePostProcessingInput();
        encryptedWithoutLabel.setIsEncrypted(true);
        encryptedWithoutLabel.setLabel(null);
        inputs.add(encryptedWithoutLabel);

        when(crypt.decrypt(anyList())).thenReturn(List.of(
                new EncryptionRequestClass("AES256", "dec-label-1", "0")
        ));

        invokePrivateListMethod("decryptLabels", inputs, crypt);

        assertEquals("dec-label-1", encryptedWithLabel.getLabel());
        assertEquals("should-stay", notEncrypted.getLabel());
        assertNull(encryptedWithoutLabel.getLabel());
        verify(crypt).decrypt(anyList());
    }

    @Test
    void testEncryptLabels_UpdatesOnlyEncryptedLabeledRows() throws Exception {
        List<DocumentWisePostProcessingInput> inputs = new ArrayList<>();

        DocumentWisePostProcessingInput encryptedWithLabel = new DocumentWisePostProcessingInput();
        encryptedWithLabel.setIsEncrypted(true);
        encryptedWithLabel.setLabel("plain-label-1");
        inputs.add(encryptedWithLabel);

        DocumentWisePostProcessingInput notEncrypted = new DocumentWisePostProcessingInput();
        notEncrypted.setIsEncrypted(false);
        notEncrypted.setLabel("should-stay");
        inputs.add(notEncrypted);

        when(crypt.encrypt(anyList())).thenReturn(List.of(
                new EncryptionRequestClass("AES256", "enc-label-1", "0")
        ));

        invokePrivateListMethod("encryptLabels", inputs, crypt);

        assertEquals("enc-label-1", encryptedWithLabel.getLabel());
        assertEquals("should-stay", notEncrypted.getLabel());
        verify(crypt).encrypt(anyList());
    }

    @Test
    void testFetchAndDecryptInputs_Flow_DecryptsPredictedAndLabels() throws Exception {
        Handle handle = org.mockito.Mockito.mock(Handle.class);
        Query query = org.mockito.Mockito.mock(Query.class);
        @SuppressWarnings("unchecked")
        ResultIterable<DocumentWisePostProcessingInput> resultIterable = org.mockito.Mockito.mock(ResultIterable.class);

        DocumentWisePostProcessingInput encryptedRow = new DocumentWisePostProcessingInput();
        encryptedRow.setIsEncrypted(true);
        encryptedRow.setPredictedValue("encPred");
        encryptedRow.setEncryptionPolicy("POLICY_A");
        encryptedRow.setSorItemName("member_id");
        encryptedRow.setLabel("encLabel");

        DocumentWisePostProcessingInput plainRow = new DocumentWisePostProcessingInput();
        plainRow.setIsEncrypted(false);
        plainRow.setPredictedValue("plainPred");
        plainRow.setLabel("plainLabel");

        when(handle.createQuery(anyString())).thenReturn(query);
        when(query.mapToBean(DocumentWisePostProcessingInput.class)).thenReturn(resultIterable);
        when(resultIterable.stream()).thenReturn(Stream.of(encryptedRow, plainRow));

        when(crypt.decrypt("encPred", "POLICY_A", "member_id")).thenReturn("decPred");
        when(crypt.decrypt(anyList())).thenReturn(List.of(
                new EncryptionRequestClass("AES256", "decLabel", "0")
        ));

        Method method = DocumentWisePostProcessingAction.class.getDeclaredMethod(
                "fetchAndDecryptInputs", Handle.class, InticsIntegrity.class, boolean.class
        );
        method.setAccessible(true);
        method.invoke(actionInstance, handle, crypt, true);

        assertEquals("decPred", encryptedRow.getPredictedValue());
        assertEquals("decLabel", encryptedRow.getLabel());
        assertEquals("plainPred", plainRow.getPredictedValue());
        assertEquals("plainLabel", plainRow.getLabel());
        verify(crypt).decrypt("encPred", "POLICY_A", "member_id");
        verify(crypt).decrypt(anyList());
    }

    @Test
    void testExecute_FullFlow_InputToOutputPath() throws Exception {
        Jdbi jdbi = org.mockito.Mockito.mock(Jdbi.class);
        Handle handle = org.mockito.Mockito.mock(Handle.class);
        Query query = org.mockito.Mockito.mock(Query.class);
        @SuppressWarnings("unchecked")
        ResultIterable<DocumentWisePostProcessingInput> resultIterable = org.mockito.Mockito.mock(ResultIterable.class);

        DocumentWisePostProcessingInput encryptedRow = new DocumentWisePostProcessingInput();
        encryptedRow.setIsEncrypted(true);
        encryptedRow.setPredictedValue("encPred");
        encryptedRow.setEncryptionPolicy("POLICY_A");
        encryptedRow.setSorItemName("member_id");
        encryptedRow.setSorItemId(101L);
        encryptedRow.setLabel("encLabel");
        encryptedRow.setOriginId("ORIGIN-1");

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            HandleConsumer<Exception> handleConsumer = invocation.getArgument(0);
            handleConsumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(org.mockito.ArgumentMatchers.<HandleConsumer<Exception>>any());

        when(handle.createQuery(anyString())).thenReturn(query);
        when(query.mapToBean(DocumentWisePostProcessingInput.class)).thenReturn(resultIterable);
        when(resultIterable.stream()).thenReturn(Stream.of(encryptedRow));

        when(crypt.decrypt("encPred", "POLICY_A", "member_id")).thenReturn("decPred");
        when(crypt.encrypt("decPred", "POLICY_A", "member_id")).thenReturn("encPredOut");
        when(crypt.decrypt(anyList())).thenReturn(List.of(new EncryptionRequestClass("AES256", "decLabel", "0")));
        when(crypt.encrypt(anyList())).thenReturn(List.of(new EncryptionRequestClass("AES256", "encLabelOut", "0")));

        try (MockedStatic<in.handyman.raven.lambda.access.ResourceAccess> resourceAccessMock =
                     org.mockito.Mockito.mockStatic(in.handyman.raven.lambda.access.ResourceAccess.class);
             MockedStatic<in.handyman.raven.core.encryption.SecurityEngine> securityEngineMock =
                     org.mockito.Mockito.mockStatic(in.handyman.raven.core.encryption.SecurityEngine.class);
             MockedConstruction<CoproProcessor> coproConstruction =
                     org.mockito.Mockito.mockConstruction(CoproProcessor.class,
                             (mock, context) -> {
                                 // no-op producer/consumer for flow-level execute test
                                 org.mockito.Mockito.doNothing().when(mock).startProducer(anyString(), anyInt());
                                 org.mockito.Mockito.doNothing().when(mock).startConsumer(anyString(), anyInt(), anyInt(), org.mockito.ArgumentMatchers.any());
                             })) {

            resourceAccessMock.when(() -> in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn(config.getResourceConn()))
                    .thenReturn(jdbi);
            securityEngineMock.when(() -> in.handyman.raven.core.encryption.SecurityEngine.getInticsIntegrityMethod(action, log))
                    .thenReturn(crypt);

            actionInstance.execute();

            assertEquals("encPredOut", encryptedRow.getPredictedValue());
            assertEquals("encLabelOut", encryptedRow.getLabel());
            verify(crypt).decrypt("encPred", "POLICY_A", "member_id");
            verify(crypt).encrypt("decPred", "POLICY_A", "member_id");
            verify(crypt).decrypt(anyList());
            verify(crypt).encrypt(anyList());
            assertFalse(coproConstruction.constructed().isEmpty());
            CoproProcessor<?, ?> constructedProcessor = coproConstruction.constructed().get(0);
            verify(constructedProcessor).startProducer(config.getQuerySet(), 10);
            verify(constructedProcessor).startConsumer(anyString(), anyInt(), anyInt(), org.mockito.ArgumentMatchers.any());
        }
    }

    private void invokePrivateListMethod(String methodName,
                                         List<DocumentWisePostProcessingInput> inputs,
                                         InticsIntegrity encryption) throws Exception {
        Method method = DocumentWisePostProcessingAction.class.getDeclaredMethod(
                methodName, List.class, InticsIntegrity.class
        );
        method.setAccessible(true);
        method.invoke(actionInstance, inputs, encryption);
    }
}
