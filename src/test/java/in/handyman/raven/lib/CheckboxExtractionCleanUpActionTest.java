package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.model.CheckboxExtractionCleanUp;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CheckboxExtractionCleanUpActionTest {

    @Mock
    private Logger log;

    private ActionExecutionAudit actionAudit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        actionAudit = ActionExecutionAudit.builder().build();
        actionAudit.getContext().put("created_user_id", "123");
    }

    @Test
    void execute_shouldReconcileUncheckedLabels_andUpdatePredictions() throws Exception {

        final String outputTable = "valuation.prediction";

        CheckboxExtractionCleanUp config = buildConfig(outputTable);

        CheckboxExtractionCleanUpAction action =
                new CheckboxExtractionCleanUpAction(actionAudit, log, config);

        // Mock DB
        Jdbi jdbi = mock(Jdbi.class);
        Handle handle = mock(Handle.class);
        Query query = mock(Query.class);
        ResultIterable<PredictionDTO> resultIterable = mock(ResultIterable.class);
        PreparedBatch batch = mock(PreparedBatch.class);

        mockBatch(batch);
        mockQuery(handle, query, resultIterable);
        mockTransaction(jdbi, handle);

        List<PredictionDTO> predictions = buildTestData();

        when(resultIterable.stream()).thenReturn(predictions.stream());
        when(handle.prepareBatch(anyString())).thenReturn(batch);

        try (MockedStatic<ResourceAccess> mock = Mockito.mockStatic(ResourceAccess.class)) {
            mock.when(() -> ResourceAccess.rdbmsJDBIConn(anyString()))
                    .thenReturn(jdbi);

            action.execute();

            verifyBatchExecution(batch);
        }
    }
    private void verifyBatchExecution(PreparedBatch batch) {

        // Capture predicted values
        ArgumentCaptor<String> predictedCaptor = ArgumentCaptor.forClass(String.class);
        verify(batch, atLeastOnce()).bind(eq("predictedValue"), predictedCaptor.capture());

        List<String> values = predictedCaptor.getAllValues();

        // Expected outcomes
        assertTrue(values.contains(""));
        assertTrue(values.contains("office visit"));
        assertTrue(values.contains("urgent"));

        // Capture prediction IDs
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(batch, atLeastOnce()).bind(eq("predictionId"), idCaptor.capture());

        List<Long> ids = idCaptor.getAllValues();

        assertTrue(ids.containsAll(List.of(2001L, 2002L, 2003L, 2004L, 2005L)));

        verify(batch, times(5)).add();
        verify(batch).execute();
    }

    private void mockBatch(PreparedBatch batch) {
        when(batch.bind(anyString(), Mockito.<Object>any())).thenReturn(batch);
        when(batch.add()).thenReturn(batch);
        when(batch.execute()).thenReturn(new int[]{1,1,1,1,1});
    }

    private void mockQuery(Handle handle, Query query, ResultIterable<PredictionDTO> resultIterable) {
        when(handle.createQuery(anyString())).thenReturn(query);
        when(query.mapToBean(PredictionDTO.class)).thenReturn(resultIterable);
    }

    private void mockTransaction(Jdbi jdbi, Handle handle) {
        doAnswer(invocation -> {
            HandleConsumer<?> consumer = invocation.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(any());
    }

    private List<PredictionDTO> buildTestData() {

        String checkboxJson =
                "[{\"label_value\":\"Outpatient\",\"attribution_status\":\"Unchecked\"}," +
                        "{\"label_value\":\"Maternity\",\"attribution_status\":\"Unchecked\"}," +
                        "{\"label_value\":\"Observation\",\"attribution_status\":\"Unchecked\"}]";

        return List.of(
                buildPrediction(1001L, "CHECKBOX_EXTRACTION", null, checkboxJson),
                buildPrediction(2001L, "KIE", "Outpatient", null),
                buildPrediction(2002L, "KIE", "Maternity", null),
                buildPrediction(2003L, "KIE", "office visit", null),
                buildPrediction(2004L, "KIE", "urgent", null),
                buildPrediction(2005L, "KIE", "Observation,urgent", null),
                buildPrediction(2005L, "KIE", "duplicate_should_skip", null) // duplicate
        );
    }

    private CheckboxExtractionCleanUp buildConfig(String outputTable) {
        return CheckboxExtractionCleanUp.builder()
                .name("checkbox_cleanup_test")
                .resourceConn("intics_zio_db_conn")
                .outputTable(outputTable)
                .batchId("BATCH-92_0")
                .querySet("SELECT * FROM prediction") // simplified
                .condition(true)
                .build();
    }

    private PredictionDTO buildPrediction(Long id, String feature,
                                          String value, String checkboxData) {
        PredictionDTO dto = new PredictionDTO();
        dto.setPredictionId(id);
        dto.setFeature(feature);
        dto.setPredictedValue(value);
        dto.setCheckboxData(checkboxData);
        dto.setOriginId("ORIGIN");
        dto.setTenantId(1L);
        dto.setSorItemId(11358L);
        dto.setBatchId("BATCH-92_0");
        dto.setGroupId(100L);
        dto.setTransactionId("TRX");
        return dto;
    }
}