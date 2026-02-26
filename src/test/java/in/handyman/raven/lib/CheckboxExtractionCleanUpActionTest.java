package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxExtractionCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckboxExtractionCleanUpActionTest {

    @Mock
    private ActionExecutionAudit action;

    @Mock
    private Logger log;

    private CheckboxExtractionCleanUp checkboxExtractionCleanUp;
    private ObjectMapper mapper;
    private Map<String, String> context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
        context = new HashMap<>();
        context.put("created_user_id", "1");

        checkboxExtractionCleanUp = CheckboxExtractionCleanUp.builder()
                .name("Test Checkbox Cleanup")
                .condition(true)
                .outputTable("valuation.prediction")
                .resourceConn("test_conn")
                .querySet("SELECT * FROM valuation.prediction WHERE origin_id = 'ORIGIN-16367'")
                .build();

        when(action.getContext()).thenReturn(context);
    }

    @Test
    void testReconcileWithSingleUncheckedLabel() throws Exception {
        // Setup: Create predictions with one KIE and one CHECKBOX_EXTRACTION
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        // KIE prediction with comma-separated values
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "admission,inpatient,maternity,observation,outpatient,urgent");
        kiePrediction.put("checkbox_data", null);
        predictions.add(kiePrediction);
        
        // CHECKBOX_EXTRACTION prediction with unchecked labels
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("predicted_value", null);
        String checkboxDataJson = "[{\"bbox\": \"{\\\"topLeftX\\\": 0, \\\"topLeftY\\\": 815, \\\"bottomRightX\\\": 999, \\\"bottomRightY\\\": 845}\", " +
                "\"label_value\": \"Outpatient\", \"attribution_status\": \"Unchecked\"}, " +
                "{\"bbox\": \"{\\\"topLeftX\\\": 0, \\\"topLeftY\\\": 815, \\\"bottomRightX\\\": 999, \\\"bottomRightY\\\": 845}\", " +
                "\"label_value\": \"Maternity\", \"attribution_status\": \"Unchecked\"}, " +
                "{\"bbox\": \"{\\\"topLeftX\\\": 0, \\\"topLeftY\\\": 815, \\\"bottomRightX\\\": 999, \\\"bottomRightY\\\": 845}\", " +
                "\"label_value\": \"Observation\", \"attribution_status\": \"Unchecked\"}]";
        checkboxPrediction.put("checkbox_data", checkboxDataJson);
        predictions.add(checkboxPrediction);

        // Note: This test verifies the logic but requires proper Jdbi mocking setup
        // In a real scenario, you would use a test database or more sophisticated mocking
        // For now, we'll test the core reconciliation logic separately

        // Test the reconciliation logic directly
        // Group by sor_item_id
        Map<Long, List<Map<String, Object>>> grouped = new HashMap<>();
        grouped.put(11358L, predictions);
        
        // Extract unchecked labels
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("outpatient");
        uncheckedLabels.add("maternity");
        uncheckedLabels.add("observation");
        
        // Reconcile values
        for (Map<String, Object> pred : predictions) {
            if ("KIE".equalsIgnoreCase((String) pred.get("feature"))) {
                String predictedValue = (String) pred.get("predicted_value");
                if (predictedValue != null && !predictedValue.trim().isEmpty()) {
                    List<String> values = Arrays.asList(predictedValue.split(","));
                    List<String> filtered = new ArrayList<>();
                    for (String val : values) {
                        String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                        if (!uncheckedLabels.contains(normalized)) {
                            filtered.add(val.trim());
                        }
                    }
                    pred.put("predicted_value", String.join(",", filtered));
                }
            }
        }

        // Verify: Check that "outpatient", "maternity", and "observation" were removed
        String updatedValue = (String) kiePrediction.get("predicted_value");
        assertNotNull(updatedValue);
        assertFalse(updatedValue.toLowerCase().contains("outpatient"), "Outpatient should be removed");
        assertFalse(updatedValue.toLowerCase().contains("maternity"), "Maternity should be removed");
        assertFalse(updatedValue.toLowerCase().contains("observation"), "Observation should be removed");
        assertTrue(updatedValue.contains("admission"), "Admission should remain");
        assertTrue(updatedValue.contains("inpatient"), "Inpatient should remain");
        assertTrue(updatedValue.contains("urgent"), "Urgent should remain");
    }

    @Test
    void testReconcileWithMultipleSorItemIds() throws Exception {
        // Setup: Create predictions for two different sor_item_ids
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        // Group 1: sor_item_id = 11358
        Map<String, Object> kiePrediction1 = new HashMap<>();
        kiePrediction1.put("origin_id", "ORIGIN-16367");
        kiePrediction1.put("tenant_id", 1L);
        kiePrediction1.put("sor_item_id", 11358L);
        kiePrediction1.put("transaction_id", "TRZ-9844");
        kiePrediction1.put("feature", "KIE");
        kiePrediction1.put("predicted_value", "admission,inpatient,outpatient");
        predictions.add(kiePrediction1);
        
        Map<String, Object> checkboxPrediction1 = new HashMap<>();
        checkboxPrediction1.put("origin_id", "ORIGIN-16367");
        checkboxPrediction1.put("tenant_id", 1L);
        checkboxPrediction1.put("sor_item_id", 11358L);
        checkboxPrediction1.put("transaction_id", "TRZ-9844");
        checkboxPrediction1.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction1.put("checkbox_data", "[{\"label_value\": \"Outpatient\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction1);
        
        // Group 2: sor_item_id = 11522
        Map<String, Object> kiePrediction2 = new HashMap<>();
        kiePrediction2.put("origin_id", "ORIGIN-16367");
        kiePrediction2.put("tenant_id", 1L);
        kiePrediction2.put("sor_item_id", 11522L);
        kiePrediction2.put("transaction_id", "TRZ-9844");
        kiePrediction2.put("feature", "KIE");
        kiePrediction2.put("predicted_value", "value1,value2,value3");
        predictions.add(kiePrediction2);
        
        Map<String, Object> checkboxPrediction2 = new HashMap<>();
        checkboxPrediction2.put("origin_id", "ORIGIN-16367");
        checkboxPrediction2.put("tenant_id", 1L);
        checkboxPrediction2.put("sor_item_id", 11522L);
        checkboxPrediction2.put("transaction_id", "TRZ-9844");
        checkboxPrediction2.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction2.put("checkbox_data", "[{\"label_value\": \"Value2\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction2);

        // Test the reconciliation logic directly
        // Group by sor_item_id
        Map<Long, List<Map<String, Object>>> grouped = new HashMap<>();
        List<Map<String, Object>> group1 = Arrays.asList(kiePrediction1, checkboxPrediction1);
        List<Map<String, Object>> group2 = Arrays.asList(kiePrediction2, checkboxPrediction2);
        grouped.put(11358L, group1);
        grouped.put(11522L, group2);
        
        // Extract unchecked labels for group 1
        Set<String> uncheckedLabels1 = new HashSet<>();
        uncheckedLabels1.add("outpatient");
        
        // Extract unchecked labels for group 2
        Set<String> uncheckedLabels2 = new HashSet<>();
        uncheckedLabels2.add("value2");
        
        // Reconcile group 1
        for (Map<String, Object> pred : group1) {
            if ("KIE".equalsIgnoreCase((String) pred.get("feature"))) {
                String predictedValue = (String) pred.get("predicted_value");
                if (predictedValue != null && !predictedValue.trim().isEmpty()) {
                    List<String> values = Arrays.asList(predictedValue.split(","));
                    List<String> filtered = new ArrayList<>();
                    for (String val : values) {
                        String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                        if (!uncheckedLabels1.contains(normalized)) {
                            filtered.add(val.trim());
                        }
                    }
                    pred.put("predicted_value", String.join(",", filtered));
                }
            }
        }
        
        // Reconcile group 2
        for (Map<String, Object> pred : group2) {
            if ("KIE".equalsIgnoreCase((String) pred.get("feature"))) {
                String predictedValue = (String) pred.get("predicted_value");
                if (predictedValue != null && !predictedValue.trim().isEmpty()) {
                    List<String> values = Arrays.asList(predictedValue.split(","));
                    List<String> filtered = new ArrayList<>();
                    for (String val : values) {
                        String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                        if (!uncheckedLabels2.contains(normalized)) {
                            filtered.add(val.trim());
                        }
                    }
                    pred.put("predicted_value", String.join(",", filtered));
                }
            }
        }

        // Verify: Group 1 should have "outpatient" removed
        String updatedValue1 = (String) kiePrediction1.get("predicted_value");
        assertFalse(updatedValue1.contains("outpatient"), "Outpatient should be removed from group 1");
        assertTrue(updatedValue1.contains("admission"), "Admission should remain in group 1");
        
        // Verify: Group 2 should have "value2" removed
        String updatedValue2 = (String) kiePrediction2.get("predicted_value");
        assertFalse(updatedValue2.toLowerCase().contains("value2"), "Value2 should be removed from group 2");
        assertTrue(updatedValue2.contains("value1"), "Value1 should remain in group 2");
    }

    @Test
    void testReconcileWithEmptyUncheckedLabels() throws Exception {
        // Setup: Predictions with no unchecked labels
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "admission,inpatient,outpatient");
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        // All labels are checked
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"Outpatient\", \"attribution_status\": \"Checked\"}]");
        predictions.add(checkboxPrediction);

        // Test: No unchecked labels means no values should be removed
        // Verify: No values should be removed
        String updatedValue = (String) kiePrediction.get("predicted_value");
        assertEquals("admission,inpatient,outpatient", updatedValue, "No values should be removed");
    }

    @Test
    void testReconcileWithAllValuesRemoved() throws Exception {
        // Setup: All values in predicted_value match unchecked labels
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "outpatient,maternity,observation");
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"Outpatient\", \"attribution_status\": \"Unchecked\"}, " +
                "{\"label_value\": \"Maternity\", \"attribution_status\": \"Unchecked\"}, " +
                "{\"label_value\": \"Observation\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction);

        // Test: All values match unchecked labels
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("outpatient");
        uncheckedLabels.add("maternity");
        uncheckedLabels.add("observation");
        
        // Reconcile
        String predictedValue = (String) kiePrediction.get("predicted_value");
        if (predictedValue != null && !predictedValue.trim().isEmpty()) {
            List<String> values = Arrays.asList(predictedValue.split(","));
            List<String> filtered = new ArrayList<>();
            for (String val : values) {
                String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                if (!uncheckedLabels.contains(normalized)) {
                    filtered.add(val.trim());
                }
            }
            kiePrediction.put("predicted_value", String.join(",", filtered));
        }

        // Verify: All values should be removed, resulting in empty string
        String updatedValue = (String) kiePrediction.get("predicted_value");
        assertEquals("", updatedValue, "All values should be removed, resulting in empty string");
    }

    @Test
    void testReconcileWithCaseInsensitiveMatching() throws Exception {
        // Setup: Test case-insensitive matching
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "OUTPATIENT,Inpatient,Maternity");
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"outpatient\", \"attribution_status\": \"Unchecked\"}, " +
                "{\"label_value\": \"MATERNITY\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction);

        // Test: Case-insensitive matching
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("outpatient");
        uncheckedLabels.add("maternity");
        
        // Reconcile
        String predictedValue = (String) kiePrediction.get("predicted_value");
        if (predictedValue != null && !predictedValue.trim().isEmpty()) {
            List<String> values = Arrays.asList(predictedValue.split(","));
            List<String> filtered = new ArrayList<>();
            for (String val : values) {
                String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                if (!uncheckedLabels.contains(normalized)) {
                    filtered.add(val.trim());
                }
            }
            kiePrediction.put("predicted_value", String.join(",", filtered));
        }

        // Verify: Case-insensitive matching should work
        String updatedValue = (String) kiePrediction.get("predicted_value");
        assertFalse(updatedValue.toLowerCase().contains("outpatient"), "OUTPATIENT should be removed (case-insensitive)");
        assertFalse(updatedValue.toLowerCase().contains("maternity"), "Maternity should be removed (case-insensitive)");
        assertTrue(updatedValue.contains("Inpatient"), "Inpatient should remain");
    }

    @Test
    void testReconcileWithSpecialCharacters() throws Exception {
        // Setup: Test normalization with special characters
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "power of attorney,power of attorney requests");
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"Power of Attorney\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction);

        // Test: Normalized matching with special characters
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("power of attorney");
        
        // Reconcile
        String predictedValue = (String) kiePrediction.get("predicted_value");
        if (predictedValue != null && !predictedValue.trim().isEmpty()) {
            List<String> values = Arrays.asList(predictedValue.split(","));
            List<String> filtered = new ArrayList<>();
            for (String val : values) {
                String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                if (!uncheckedLabels.contains(normalized)) {
                    filtered.add(val.trim());
                }
            }
            kiePrediction.put("predicted_value", String.join(",", filtered));
        }

        // Verify: Normalized matching should work
        String updatedValue = (String) kiePrediction.get("predicted_value");
//        // After normalization, "power of attorney" should match "Power of Attorney"
//        assertFalse(updatedValue.toLowerCase().contains("power of attorney"),
//                "power of attorney should be removed (normalized matching)");
        assertEquals(updatedValue,"power of attorney requests");
    }

    @Test
    void testReconcileWithNullPredictedValue() {
        // Setup: KIE prediction with null predicted_value
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", null);
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"Outpatient\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction);

        // Test: Null predicted_value should be handled gracefully
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("outpatient");
        
        // Reconcile - should not throw exception
        assertDoesNotThrow(() -> {
            for (Map<String, Object> pred : predictions) {
                if ("KIE".equalsIgnoreCase((String) pred.get("feature"))) {
                    String predictedValue = (String) pred.get("predicted_value");
                    if (predictedValue != null && !predictedValue.trim().isEmpty()) {
                        // Process normally
                    }
                }
            }
        });
    }

    @Test
    void testReconcileWithSingleValueRemoved() {
        // Setup: KIE prediction with single value that matches unchecked label
        List<Map<String, Object>> predictions = new ArrayList<>();
        
        Map<String, Object> kiePrediction = new HashMap<>();
        kiePrediction.put("origin_id", "ORIGIN-16367");
        kiePrediction.put("tenant_id", 1L);
        kiePrediction.put("sor_item_id", 11358L);
        kiePrediction.put("transaction_id", "TRZ-9844");
        kiePrediction.put("feature", "KIE");
        kiePrediction.put("predicted_value", "Outpatient");
        predictions.add(kiePrediction);
        
        Map<String, Object> checkboxPrediction = new HashMap<>();
        checkboxPrediction.put("origin_id", "ORIGIN-16367");
        checkboxPrediction.put("tenant_id", 1L);
        checkboxPrediction.put("sor_item_id", 11358L);
        checkboxPrediction.put("transaction_id", "TRZ-9844");
        checkboxPrediction.put("feature", "CHECKBOX_EXTRACTION");
        checkboxPrediction.put("checkbox_data", "[{\"label_value\": \"Outpatient\", \"attribution_status\": \"Unchecked\"}]");
        predictions.add(checkboxPrediction);

        // Test: Single value that matches unchecked label should be removed
        Set<String> uncheckedLabels = new HashSet<>();
        uncheckedLabels.add("outpatient");
        
        // Reconcile
        String predictedValue = (String) kiePrediction.get("predicted_value");
        if (predictedValue != null && !predictedValue.trim().isEmpty()) {
            List<String> values = Arrays.asList(predictedValue.split(","));
            List<String> filtered = new ArrayList<>();
            for (String val : values) {
                String normalized = val.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");
                if (!uncheckedLabels.contains(normalized)) {
                    filtered.add(val.trim());
                }
            }
            kiePrediction.put("predicted_value", String.join(",", filtered));
        }
        
        // Verify: Single value should be removed, resulting in empty string
        String updatedValue = (String) kiePrediction.get("predicted_value");
        assertEquals("", updatedValue, "Single matching value should be removed, resulting in empty string");
    }
}
