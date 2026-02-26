package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.mapper.MedicalPayloadGeneration;
import in.handyman.raven.lib.custom.outbound.model.MedicalOutboundResponse;
import in.handyman.raven.lib.custom.outbound.model.MedicalPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for CustomResponseAction and MedicalPayloadGeneration
 * Simulates the complete flow from CustomResponseAction to MedicalPayloadGeneration
 * with ALL fields populated to verify end-to-end functionality
 */
@DisplayName("CustomResponseAction Integration Test - Complete Flow with All Fields")
class CustomResponseActionIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CustomResponseActionIntegrationTest.class);
    private MedicalPayloadGeneration medicalPayloadGeneration;
    private ObjectMapper objectMapper;
    private Map<String, String> configMap;

    @BeforeEach
    void setUp() {
        medicalPayloadGeneration = new MedicalPayloadGeneration(log);
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        // Simulate the context map from ActionExecutionAudit
        configMap = new HashMap<>();
        configMap.put("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100");
        configMap.put("AUMI_BBOX_SCALAR_WIDTH", "1000");
        configMap.put("AUMI_BBOX_SCALAR_HEIGHT", "1000");
        configMap.put("FLOAT_VALUE_ROUNDING_PRECISION", "2");
        configMap.put("aumi.reorder.paper.number", "false");
        configMap.put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false");
    }

    @Test
    @DisplayName("Test CustomResponseAction Flow - Group by OriginId with All Fields")
    void testCustomResponseActionFlowGroupedByOriginId() throws Exception {
        // Given - Simulate multiple originIds with complete prediction data
        List<PredictionDTO> allPredictions = createCompletePredictionsForMultipleOrigins();

        // When - Group by originId (as CustomResponseAction does)
        Map<String, List<PredictionDTO>> groupedByOriginId = allPredictions.stream()
                .collect(Collectors.groupingBy(PredictionDTO::getOriginId));

        log.info("Grouped predictions by {} originIds", groupedByOriginId.size());

        // Process each originId group (simulating CustomResponseAction.forEach)
        List<MedicalOutboundResponse> responses = new ArrayList<>();
        for (Map.Entry<String, List<PredictionDTO>> entry : groupedByOriginId.entrySet()) {
            String originId = entry.getKey();
            List<PredictionDTO> predictionDTOList = entry.getValue();

            if (!predictionDTOList.isEmpty()) {
                // Extract metadata from first prediction (as CustomResponseAction does)
                String metadata = predictionDTOList.get(0).getMetadataJson();
                Long groupId = predictionDTOList.get(0).getGroupId();
                Long tenantId = predictionDTOList.get(0).getTenantId();
                String batchId = predictionDTOList.get(0).getBatchId();
                String rootPipelineId = predictionDTOList.get(0).getRootPipelineId();

                log.info("Processing originId: {}, groupId: {}, tenantId: {}, batchId: {}, rootPipelineId: {}", 
                        originId, groupId, tenantId, batchId, rootPipelineId);

                // Build medical outbound response (as CustomResponseAction does)
                MedicalOutboundResponse medicalOutboundResponse = medicalPayloadGeneration.buildMedicalOutboundResponse(
                        predictionDTOList, configMap, metadata);

                // Verify response structure
                assertNotNull(medicalOutboundResponse, "Response should not be null for originId: " + originId);
                assertEquals("SUCCESS", medicalOutboundResponse.getStatus());
                assertNotNull(medicalOutboundResponse.getAumipayload(), "Payload should not be null");
                assertNotNull(medicalOutboundResponse.getMetadata(), "Metadata should not be null");

                // Verify complete payload structure
                verifyCompletePayloadStructure(medicalOutboundResponse.getAumipayload());

                // Serialize to JSON (as CustomResponseAction does)
                String customResponseStr = objectMapper.writeValueAsString(medicalOutboundResponse);
                assertNotNull(customResponseStr, "Serialized response should not be null");
                assertFalse(customResponseStr.isEmpty(), "Serialized response should not be empty");

                responses.add(medicalOutboundResponse);

                log.info("Successfully processed originId: {}, response size: {} bytes", 
                        originId, customResponseStr.length());
            }
        }

        // Then
        assertFalse(responses.isEmpty(), "Should have processed at least one response");
        assertEquals(groupedByOriginId.size(), responses.size(), 
                "Should have one response per originId");

        // Log all responses for inspection
        logAllResponses(responses);
    }

    @Test
    @DisplayName("Test CustomResponseAction Flow - Single OriginId with All Fields")
    void testCustomResponseActionFlowSingleOriginId() throws Exception {
        // Given - Create complete predictions for single originId
        List<PredictionDTO> predictions = createCompletePredictionsForAllFields("ORIGIN-001");
        String metadata = predictions.get(0).getMetadataJson();

        // When - Build response (simulating CustomResponseAction flow)
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadata);

        // Then - Verify complete structure
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getAumipayload());

        // Verify all sections are populated
        MedicalPayload payload = response.getAumipayload();
        
        // Member section
        assertNotNull(payload.getHcid(), "HCID should be populated");
        assertNotNull(payload.getMedicaidId(), "Medicaid ID should be populated");
        assertNotNull(payload.getMemberFirstName(), "Member first name should be populated");
        assertNotNull(payload.getMemberLastName(), "Member last name should be populated");

        // Authorization section
        assertNotNull(payload.getAuthId(), "Auth ID should be populated");
        assertNotNull(payload.getLevelOfService(), "Level of service should be populated");
        assertNotNull(payload.getServiceFromDate(), "Service from date should be populated");
        assertNotNull(payload.getServiceToDate(), "Service to date should be populated");

        // Services
        assertNotNull(payload.getService(), "Services should be populated");
        assertFalse(payload.getService().isEmpty(), "Services should not be empty");

        // Diagnosis
        assertNotNull(payload.getDiagnosis(), "Diagnosis should be populated");
        assertFalse(payload.getDiagnosis().isEmpty(), "Diagnosis should not be empty");

        // Providers
        assertNotNull(payload.getProvider(), "Providers should be populated");
        assertFalse(payload.getProvider().isEmpty(), "Providers should not be empty");

        // Additional Properties
        assertNotNull(payload.getAdditionalProperties(), "Additional properties should be populated");
        assertFalse(payload.getAdditionalProperties().isEmpty(), "Additional properties should not be empty");

        // Member Additional Properties
        assertNotNull(payload.getMemberAdditionalProperties(), "Member additional properties should be populated");
        assertFalse(payload.getMemberAdditionalProperties().isEmpty(), "Member additional properties should not be empty");

        // Serialize and verify JSON structure
        String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response);
        assertNotNull(jsonResponse);
        assertTrue(jsonResponse.contains("\"hcid\""), "JSON should contain hcid");
        assertTrue(jsonResponse.contains("\"service\""), "JSON should contain service");
        assertTrue(jsonResponse.contains("\"diagnosis\""), "JSON should contain diagnosis");
        assertTrue(jsonResponse.contains("\"provider\""), "JSON should contain provider");

        log.info("Complete JSON Response:\n{}", jsonResponse);
    }

    @Test
    @DisplayName("Test CustomResponseAction Flow - Multiple OriginIds with Different Data")
    void testCustomResponseActionFlowMultipleOriginIds() throws Exception {
        // Given - Create predictions for multiple originIds
        List<PredictionDTO> origin1Predictions = createCompletePredictionsForAllFields("ORIGIN-001");
        List<PredictionDTO> origin2Predictions = createCompletePredictionsForAllFields("ORIGIN-002");
        List<PredictionDTO> origin3Predictions = createCompletePredictionsForAllFields("ORIGIN-003");

        List<PredictionDTO> allPredictions = new ArrayList<>();
        allPredictions.addAll(origin1Predictions);
        allPredictions.addAll(origin2Predictions);
        allPredictions.addAll(origin3Predictions);

        // When - Group by originId
        Map<String, List<PredictionDTO>> groupedByOriginId = allPredictions.stream()
                .collect(Collectors.groupingBy(PredictionDTO::getOriginId));

        assertEquals(3, groupedByOriginId.size(), "Should have 3 originIds");

        // Process each group
        Map<String, MedicalOutboundResponse> responsesByOrigin = new HashMap<>();
        for (Map.Entry<String, List<PredictionDTO>> entry : groupedByOriginId.entrySet()) {
            String originId = entry.getKey();
            List<PredictionDTO> predictionList = entry.getValue();

            String metadata = predictionList.get(0).getMetadataJson();
            MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                    predictionList, configMap, metadata);

            responsesByOrigin.put(originId, response);
        }

        // Then
        assertEquals(3, responsesByOrigin.size(), "Should have 3 responses");
        
        for (Map.Entry<String, MedicalOutboundResponse> entry : responsesByOrigin.entrySet()) {
            String originId = entry.getKey();
            MedicalOutboundResponse response = entry.getValue();
            
            assertNotNull(response, "Response should not be null for originId: " + originId);
            assertEquals("SUCCESS", response.getStatus());
            assertNotNull(response.getAumipayload());
            verifyCompletePayloadStructure(response.getAumipayload());
        }
    }

    // ========== HELPER METHODS ==========

    private List<PredictionDTO> createCompletePredictionsForMultipleOrigins() {
        List<PredictionDTO> predictions = new ArrayList<>();
        
        // Origin 1
        predictions.addAll(createCompletePredictionsForAllFields("ORIGIN-001"));
        
        // Origin 2
        predictions.addAll(createCompletePredictionsForAllFields("ORIGIN-002"));
        
        return predictions;
    }

    private List<PredictionDTO> createCompletePredictionsForAllFields(String originId) {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createMetadataJson();

        // Member fields
        predictions.add(createPrediction(originId, "member_id", "MEMBER-12345", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "medicaid_id", "MEDICAID-67890", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction(originId, "member_group_id", "GROUP-001", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction(originId, "member_first_name", "John", "single_value", 1, 0.92, null, metadataJson));
        predictions.add(createPrediction(originId, "member_last_name", "Doe", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction(originId, "member_date_of_birth", "1980-01-15", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction(originId, "member_gender", "M", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction(originId, "member_address_line1", "123 Main Street", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction(originId, "member_city", "Springfield", "single_value", 1, 0.86, null, metadataJson));
        predictions.add(createPrediction(originId, "member_state", "IL", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction(originId, "member_zipcode", "62701", "single_value", 1, 0.84, null, metadataJson));

        // Authorization fields
        predictions.add(createPrediction(originId, "auth_id", "AUTH-789", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction(originId, "level_of_service", "Urgent", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction(originId, "service_from_date", "2024-01-01", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction(originId, "service_to_date", "2024-01-31", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction(originId, "auth_admit_date", "2024-01-01", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction(originId, "auth_discharge_date", "2024-01-31", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction(originId, "fax_received_date", "2024-01-15", "single_value", 1, 0.86, null, metadataJson));
        predictions.add(createPrediction(originId, "total_service_days", "30", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction(originId, "notification_type", "Initial", "single_value", 1, 0.92, null, metadataJson));

        // Service fields - Multiple services
        predictions.add(createPrediction(originId, "service_code", "99213", "multi_value", 1, 0.85, "1", metadataJson));
        predictions.add(createPrediction(originId, "service_code_modifier", "25", "multi_value", 1, 0.87, "1", metadataJson));
        predictions.add(createPrediction(originId, "service_quantity_units", "5", "multi_value", 1, 0.90, "1", metadataJson));
        predictions.add(createPrediction(originId, "service_quantity_visits", "2", "multi_value", 1, 0.88, "1", metadataJson));
        
        predictions.add(createPrediction(originId, "service_code", "99214", "multi_value", 1, 0.86, "2", metadataJson));
        predictions.add(createPrediction(originId, "service_code_modifier", "26", "multi_value", 1, 0.88, "2", metadataJson));
        predictions.add(createPrediction(originId, "service_quantity_units", "3", "multi_value", 1, 0.91, "2", metadataJson));

        // Diagnosis fields - Multiple diagnosis
        predictions.add(createPrediction(originId, "diagnosis_code", "E11.9", "multi_value", 1, 0.91, "1", metadataJson));
        predictions.add(createPrediction(originId, "diagnosis_description", "Type 2 diabetes", "multi_value", 1, 0.89, "1", metadataJson));
        predictions.add(createPrediction(originId, "code_pointer", "1", "multi_value", 1, 0.88, "1", metadataJson));
        
        predictions.add(createPrediction(originId, "diagnosis_code", "I10", "multi_value", 1, 0.92, "2", metadataJson));
        predictions.add(createPrediction(originId, "diagnosis_description", "Essential hypertension", "multi_value", 1, 0.90, "2", metadataJson));
        predictions.add(createPrediction(originId, "code_pointer", "2", "multi_value", 1, 0.89, "2", metadataJson));

        // Provider fields - All provider types
        predictions.add(createPrediction(originId, "servicing_provider_npi", "1234567890", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "servicing_provider_first_name", "Dr. Jane", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction(originId, "servicing_provider_last_name", "Smith", "single_value", 1, 0.92, null, metadataJson));
        
        predictions.add(createPrediction(originId, "servicing_facility_npi", "9876543210", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "servicing_facility_first_name", "General", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction(originId, "servicing_facility_last_name", "Hospital", "single_value", 1, 0.92, null, metadataJson));
        
        predictions.add(createPrediction(originId, "referring_provider_npi", "1111111111", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "referring_provider_first_name", "Dr. John", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction(originId, "referring_provider_last_name", "Doe", "single_value", 1, 0.91, null, metadataJson));
        
        predictions.add(createPrediction(originId, "ordering_provider_npi", "2222222222", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "ordering_provider_first_name", "Dr. Bob", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction(originId, "ordering_provider_last_name", "Johnson", "single_value", 1, 0.91, null, metadataJson));

        // Additional properties
        predictions.add(createPrediction(originId, "clinical_present_1", "Y", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction(originId, "level_of_care", "Urgent,Non-Urgent", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction(originId, "fax_report", "Y", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction(originId, "additional_auth_properties", "keyword1,keyword2", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction(originId, "responsible_area_1", "Area1", "single_value", 1, 0.87, null, metadataJson));

        // Member additional properties
        predictions.add(createPrediction(originId, "multiple_member_indicator", "Y", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction(originId, "newborn_request", "Y", "single_value", 1, 0.84, null, metadataJson));
        predictions.add(createPrediction(originId, "newborn_first_name", "Baby", "single_value", 1, 0.83, null, metadataJson));
        predictions.add(createPrediction(originId, "newborn_last_name", "Doe", "single_value", 1, 0.82, null, metadataJson));
        predictions.add(createPrediction(originId, "newborn_date_of_birth", "2024-01-01", "single_value", 1, 0.81, null, metadataJson));
        predictions.add(createPrediction(originId, "newborn_gender", "M", "single_value", 1, 0.80, null, metadataJson));

        return predictions;
    }

    private PredictionDTO createPrediction(String originId, String sorItemName, String predictedValue, 
                                           String lineItemType, int paperNo, double precision, 
                                           String sorContainerInstance, String metadataJson) {
        return PredictionDTO.builder()
                .predictionId(System.currentTimeMillis() + (long)(Math.random() * 1000))
                .originId(originId)
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(paperNo)
                .lineItemType(lineItemType)
                .sorItemName(sorItemName)
                .predictedValue(predictedValue)
                .precision(precision)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(50.0)
                .lowerPos(80.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .sorContainerInstance(sorContainerInstance)
                .metadataJson(metadataJson)
                .build();
    }

    private String createMetadataJson() {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusSeconds(5);
            LocalDateTime endTime = LocalDateTime.now();

            MetadataContext metadataContext = MetadataContext.builder()
                    .requestTxnId("REQ-TXN-001")
                    .uploadStatus("SUCCESS")
                    .errorMessage(null)
                    .errorMessageDetail(null)
                    .errorCode(200)
                    .documentId("DOC-001")
                    .inboundTransactionId("INBOUND-TXN-001")
                    .transactionId("TXN-001")
                    .documentType("Medical")
                    .inboundDocumentName("test_document.pdf")
                    .documentExtension("pdf")
                    .processStartTime(startTime.toString())
                    .processEndTime(endTime.toString())
                    .processedAt(endTime.toString())
                    .candidatePapers(Arrays.asList(1, 2, 3))
                    .build();

            return objectMapper.writeValueAsString(metadataContext);
        } catch (Exception e) {
            log.error("Failed to create metadata JSON", e);
            return "{}";
        }
    }

    private void verifyCompletePayloadStructure(MedicalPayload payload) {
        // Member section
        assertNotNull(payload.getHcid(), "HCID should not be null");
        assertNotNull(payload.getMemberFirstName(), "Member first name should not be null");
        assertNotNull(payload.getMemberLastName(), "Member last name should not be null");

        // Authorization section
        assertNotNull(payload.getAuthId(), "Auth ID should not be null");
        assertNotNull(payload.getLevelOfService(), "Level of service should not be null");

        // Services
        assertNotNull(payload.getService(), "Services should not be null");
        assertFalse(payload.getService().isEmpty(), "Services should not be empty");

        // Diagnosis
        assertNotNull(payload.getDiagnosis(), "Diagnosis should not be null");
        assertFalse(payload.getDiagnosis().isEmpty(), "Diagnosis should not be empty");

        // Providers
        assertNotNull(payload.getProvider(), "Providers should not be null");
        assertFalse(payload.getProvider().isEmpty(), "Providers should not be empty");

        // Additional Properties
        assertNotNull(payload.getAdditionalProperties(), "Additional properties should not be null");
        assertFalse(payload.getAdditionalProperties().isEmpty(), "Additional properties should not be empty");

        // Member Additional Properties
        assertNotNull(payload.getMemberAdditionalProperties(), "Member additional properties should not be null");
        assertFalse(payload.getMemberAdditionalProperties().isEmpty(), "Member additional properties should not be empty");
    }

    private void logAllResponses(List<MedicalOutboundResponse> responses) {
        try {
            log.info("==========================================");
            log.info("ALL RESPONSES SUMMARY");
            log.info("==========================================");
            log.info("Total responses: {}", responses.size());
            
            for (int i = 0; i < responses.size(); i++) {
                MedicalOutboundResponse response = responses.get(i);
                String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(response);
                log.info("Response {}:\n{}", i + 1, jsonResponse);
            }
            log.info("==========================================");
        } catch (Exception e) {
            log.error("Failed to log responses", e);
        }
    }
}
