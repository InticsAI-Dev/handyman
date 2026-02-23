package in.handyman.raven.lib.custom.outbound.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.MedicalOutboundResponse;
import in.handyman.raven.lib.custom.outbound.model.ServiceModifier;
import in.handyman.raven.lib.custom.outbound.model.ServiceQuantity;
import in.handyman.raven.lib.custom.outbound.model.Diagonsis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicalPayloadGenerationTest {

    private MedicalPayloadGeneration payloadGeneration;
    private Logger logger;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        logger = LoggerFactory.getLogger(MedicalPayloadGenerationTest.class);
        payloadGeneration = new MedicalPayloadGeneration(logger);
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    private PredictionDTO buildSingleValuePrediction(String sorItem, String value) {
        PredictionDTO dto = new PredictionDTO();
        dto.setSorItemName(sorItem);
        dto.setPredictedValue(value);
        dto.setPrecision(0.95);
        dto.setPaperNo(1);
        dto.setLeftPos(10.0);
        dto.setUpperPos(10.0);
        dto.setRightPos(50.0);
        dto.setLowerPos(20.0);
        dto.setImageWidth(1000);
        dto.setImageHeight(1000);
        dto.setIsMultiEntityEnabled(false);
        dto.setLineItemType("single_value");
        return dto;
    }

    private PredictionDTO buildMultiValuePrediction(String sorItem, String value, String instance) {
        PredictionDTO dto = buildSingleValuePrediction(sorItem, value);
        dto.setIsMultiEntityEnabled(false);
        dto.setLineItemType("multi_value");
        dto.setSorContainerInstance(instance);
        return dto;
    }

    private String buildMetadataContextJson() throws Exception {
        MetadataContext context = new MetadataContext();
        context.setRequestTxnId("REQ123");
        context.setDocumentId("DOC1");
        context.setInboundTransactionId("INB1");
        context.setUploadStatus("SUCCESS");
        context.setErrorCode(200);

        return new ObjectMapper().writeValueAsString(context);
    }

    private Map<String, String> defaultConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100");
        config.put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "true");
        return config;
    }

    @Test
    void testBuildMedicalOutboundResponse_Success() throws Exception {

        List<PredictionDTO> predictions = List.of(
                buildSingleValuePrediction("member_id", "12345"),
                buildSingleValuePrediction("member_first_name", "John"),
                buildSingleValuePrediction("member_last_name", "Doe")
        );

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        buildMetadataContextJson()
                );

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("REQ123", response.getRequestTxnId());
        assertEquals("DOC1", response.getDocumentId());
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getMetadata());
    }

    @Test
    void testStatusMapping_Failure() throws Exception {

        MetadataContext context = new MetadataContext();
        context.setRequestTxnId("REQ1");
        context.setUploadStatus("ERROR");
        context.setErrorCode(500);
        context.setErrorMessage("Failure occurred");

        String metadataJson = new ObjectMapper().writeValueAsString(context);

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        Collections.emptyList(),
                        defaultConfig(),
                        metadataJson
                );

        assertEquals("FAILURE", response.getStatus());
        assertEquals("Failure occurred", response.getErrorMessage());
        assertEquals(500, response.getErrorCd());
    }

    @Test
    void testDiagnosisCreation() throws Exception {

        List<PredictionDTO> predictions = List.of(
                buildMultiValuePrediction("diagnosis_code", "A123", "1"),
                buildMultiValuePrediction("diagnosis_description", "Flu", "1")
        );

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        buildMetadataContextJson()
                );

        assertNotNull(response.getAumipayload().getDiagnosis());
        assertFalse(response.getAumipayload().getDiagnosis().isEmpty());
    }

    @Test
    @DisplayName("Test Diagnosis Creation with Comma-Separated Values")
    void testDiagnosisCreationWithCommaSeparatedValues() throws Exception {
        // Given - Create predictions with comma-separated values
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = buildMetadataContextJson();

        // Diagnosis codes: 3 diagnoses
        predictions.add(createPrediction("diagnosis_code", "E11.9,I10,M54.5", "multi_value", 1, 0.91, "1", metadataJson));
        // Diagnosis descriptions: 3 descriptions (matching diagnosis codes)
        predictions.add(createPrediction("diagnosis_description", "Type 2 diabetes,Essential hypertension,Low back pain", "multi_value", 1, 0.89, "1", metadataJson));
        // Code pointers: 3 pointers (matching diagnosis codes)
        predictions.add(createPrediction("code_pointer", "1,2,3", "multi_value", 1, 0.88, "1", metadataJson));

        // When
        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        metadataJson
                );

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getDiagnosis());
        assertFalse(response.getAumipayload().getDiagnosis().isEmpty(), 
                "Diagnosis list should not be empty");
        
        // Should have 3 diagnoses (one for each comma-separated value)
        assertTrue(response.getAumipayload().getDiagnosis().size() >= 3, 
                "Should have at least 3 diagnoses from comma-separated values");
        
        // Verify each diagnosis has the correct structure
        for (Diagonsis diagnosis : response.getAumipayload().getDiagnosis()) {
            assertNotNull(diagnosis.getCd(), "Diagnosis code should not be null");
            assertNotNull(diagnosis.getCd().getValue(), "Diagnosis code value should not be null");
            assertFalse(diagnosis.getCd().getValue().isEmpty(), "Diagnosis code value should not be empty");
            assertNotNull(diagnosis.getDesc(), "Diagnosis description should not be null");
            assertNotNull(diagnosis.getCodePointer(), "Code pointer should not be null");
        }
        
        System.out.println("\n==========================================");
        System.out.println("DIAGNOSIS WITH COMMA-SEPARATED VALUES");
        System.out.println("==========================================");
        System.out.println("Total Diagnoses: " + response.getAumipayload().getDiagnosis().size());
        for (int i = 0; i < response.getAumipayload().getDiagnosis().size(); i++) {
            Diagonsis diag = response.getAumipayload().getDiagnosis().get(i);
            System.out.println("Diagnosis " + (i + 1) + ":");
            System.out.println("  Code: " + diag.getCd().getValue());
            System.out.println("  Description: " + diag.getDesc().getValue());
            System.out.println("  Code Pointer: " + diag.getCodePointer().getValue());
        }
        System.out.println("==========================================\n");
    }

    @Test
    void testServiceModifierCreation() throws Exception {

        List<PredictionDTO> predictions = List.of(
                buildMultiValuePrediction("service_code", "SVC1", "1"),
                buildMultiValuePrediction("service_code_modifier", "M1", "1"),
                buildMultiValuePrediction("service_quantity_units", "5", "1")
        );

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        buildMetadataContextJson()
                );

        assertNotNull(response.getAumipayload().getService());
        assertFalse(response.getAumipayload().getService().isEmpty());
    }

    @Test
    @DisplayName("Test Service Modifier Creation with Comma-Separated Values")
    void testServiceModifierCreationWithCommaSeparatedValues() throws Exception {
        // Given - Create predictions with comma-separated values
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = buildMetadataContextJson();

        // Service codes: 3 services
        predictions.add(createPrediction("service_code", "99213,99214,99215", "multi_value", 1, 0.85, "1", metadataJson));
        // Service modifiers: 3 modifiers (matching service codes)
        predictions.add(createPrediction("service_code_modifier", "25,26,27", "multi_value", 1, 0.87, "1", metadataJson));
        // Service quantity units: 3 units (matching service codes)
        predictions.add(createPrediction("service_quantity_units", "5,3,4", "multi_value", 1, 0.90, "1", metadataJson));
        // Service quantity visits: 3 visits (matching service codes)
        predictions.add(createPrediction("service_quantity_visits", "2,1,3", "multi_value", 1, 0.88, "1", metadataJson));

        // When
        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        metadataJson
                );

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getService());
        assertFalse(response.getAumipayload().getService().isEmpty(), 
                "Service list should not be empty");
        
        // Should have 3 services (one for each comma-separated value)
        assertTrue(response.getAumipayload().getService().size() >= 3, 
                "Should have at least 3 services from comma-separated values");
        
        // Verify each service has the correct structure
        for (ServiceModifier service : response.getAumipayload().getService()) {
            assertNotNull(service.getCd(), "Service code should not be null");
            assertNotNull(service.getCd().getValue(), "Service code value should not be null");
            assertFalse(service.getCd().getValue().isEmpty(), "Service code value should not be empty");
            assertNotNull(service.getServiceQuantity(), "Service quantities should not be null");
        }
        
        System.out.println("\n==========================================");
        System.out.println("SERVICE MODIFIERS WITH COMMA-SEPARATED VALUES");
        System.out.println("==========================================");
        System.out.println("Total Services: " + response.getAumipayload().getService().size());
        for (int i = 0; i < response.getAumipayload().getService().size(); i++) {
            ServiceModifier svc = response.getAumipayload().getService().get(i);
            System.out.println("Service " + (i + 1) + ":");
            System.out.println("  Code: " + svc.getCd().getValue());
            if (svc.getModifier() != null && !svc.getModifier().isEmpty()) {
                System.out.println("  Modifier: " + svc.getModifier().get(0).getCd().getValue());
            }
            if (svc.getServiceQuantity() != null && !svc.getServiceQuantity().isEmpty()) {
                for (ServiceQuantity qty : svc.getServiceQuantity()) {
                    System.out.println("  " + qty.getQuantityType().getValue() + ": " + qty.getQuantityUnits().getValue());
                }
            }
        }
        System.out.println("==========================================\n");
    }

    @Test
    void testProviderCreation() throws Exception {

        List<PredictionDTO> predictions = List.of(
                buildSingleValuePrediction("servicing_provider_npi", "9999999999"),
                buildSingleValuePrediction("servicing_provider_first_name", "Alice"),
                buildSingleValuePrediction("servicing_provider_last_name", "Smith")
        );

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        buildMetadataContextJson()
                );

        assertNotNull(response.getAumipayload().getProvider());
        assertFalse(response.getAumipayload().getProvider().isEmpty());
    }

    @Test
    void testAdditionalPropertiesCreation() throws Exception {

        List<PredictionDTO> predictions = List.of(
                buildSingleValuePrediction("clinical_present_1", "YES"),
                buildSingleValuePrediction("fax_report", "received")
        );

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        predictions,
                        defaultConfig(),
                        buildMetadataContextJson()
                );
        System.out.println(response);
        assertNotNull(response.getAumipayload().getAdditionalProperties());
        assertFalse(response.getAumipayload().getAdditionalProperties().isEmpty());
    }

    @Test
    void testEmptyPredictions() throws Exception {

        MedicalOutboundResponse response =
                payloadGeneration.buildMedicalOutboundResponse(
                        Collections.emptyList(),
                        defaultConfig(),
                        buildMetadataContextJson()
                );

        assertNotNull(response);
        assertNotNull(response.getAumipayload());
    }

    @Test
    @DisplayName("Test Complete JSON Generation with ALL SOR Items - Generate and Verify Complete JSON")
    void testCompleteJsonGenerationWithAllSorItems() throws Exception {
        // Given - Create predictions for ALL SOR items
        List<PredictionDTO> allSorItemPredictions = createAllSorItemPredictions();
        String metadataJson = createCompleteMetadataJson();
        Map<String, String> config = createCompleteConfig();

        // When - Generate the complete medical outbound response
        MedicalOutboundResponse response = payloadGeneration.buildMedicalOutboundResponse(
                allSorItemPredictions,
                config,
                metadataJson
        );

        // Then - Serialize to JSON and verify
        String generatedJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(response);

        // Assertions
        assertNotNull(response, "Response should not be null");
        assertNotNull(generatedJson, "Generated JSON should not be null");
        assertFalse(generatedJson.isEmpty(), "Generated JSON should not be empty");
        assertTrue(generatedJson.length() > 0, "Generated JSON should have content");

        // Log the complete JSON to both logger and System.out for visibility
        System.out.println("\n==========================================");
        System.out.println("COMPLETE MEDICAL OUTBOUND JSON");
        System.out.println("==========================================");
        System.out.println(generatedJson);
        System.out.println("==========================================");
        System.out.println("JSON Length: " + generatedJson.length() + " characters");
        System.out.println("Total Predictions Processed: " + allSorItemPredictions.size());
        System.out.println("==========================================\n");
        
        // Also log using logger (if configured)
        logger.info("==========================================");
        logger.info("COMPLETE MEDICAL OUTBOUND JSON");
        logger.info("==========================================");
        logger.info("\n{}", generatedJson);
        logger.info("==========================================");
        logger.info("JSON Length: {} characters", generatedJson.length());
        logger.info("Total Predictions Processed: {}", allSorItemPredictions.size());
        logger.info("==========================================");

        // Verify response structure
        assertNotNull(response.getAumipayload(), "Payload should not be null");
        assertNotNull(response.getMetadata(), "Metadata should not be null");
        assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");

        // Verify JSON contains expected keys
        assertTrue(generatedJson.contains("\"requestTxnId\""), "JSON should contain requestTxnId");
        assertTrue(generatedJson.contains("\"status\""), "JSON should contain status");
        assertTrue(generatedJson.contains("\"aumipayload\""), "JSON should contain aumipayload");
        assertTrue(generatedJson.contains("\"metadata\""), "JSON should contain metadata");
        assertTrue(generatedJson.contains("\"hcid\""), "JSON should contain hcid");
        assertTrue(generatedJson.contains("\"service\""), "JSON should contain service");
        assertTrue(generatedJson.contains("\"diagnosis\""), "JSON should contain diagnosis");
        assertTrue(generatedJson.contains("\"provider\""), "JSON should contain provider");
    }

    private List<PredictionDTO> createAllSorItemPredictions() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // ========== MEMBER FIELDS (single_value) ==========
        predictions.add(createPrediction("member_id", "MEMBER-12345", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("medicaid_id", "MEDICAID-67890", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction("member_group_id", "GROUP-001", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("member_first_name", "John", "single_value", 1, 0.92, null, metadataJson));
        predictions.add(createPrediction("member_last_name", "Doe", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("member_date_of_birth", "1980-01-15", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("member_gender", "M", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("member_address_line1", "123 Main Street", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction("member_city", "Springfield", "single_value", 1, 0.86, null, metadataJson));
        predictions.add(createPrediction("member_state", "IL", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction("member_zipcode", "62701", "single_value", 1, 0.84, null, metadataJson));

        // ========== AUTHORIZATION FIELDS (single_value) ==========
        predictions.add(createPrediction("auth_id", "AUTH-789", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("level_of_service", "Urgent", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction("service_from_date", "2024-01-01", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("service_to_date", "2024-01-31", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction("auth_admit_date", "2024-01-01", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("auth_discharge_date", "2024-01-31", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction("fax_received_date", "2024-01-15", "single_value", 1, 0.86, null, metadataJson));
        predictions.add(createPrediction("total_service_days", "30", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction("notification_type", "Initial", "single_value", 1, 0.92, null, metadataJson));

        // ========== SERVICE FIELDS (multi_value) - Comma-separated values ==========
        // Service codes with comma-separated values (will be split into service_code_1, service_code_2, service_code_3)
        predictions.add(createPrediction("service_code", "99213,99214,99215", "multi_value", 1, 0.85, "1", metadataJson));
        // Service modifiers with comma-separated values (will be split and matched by index)
        predictions.add(createPrediction("service_code_modifier", "25,*,27", "multi_value", 1, 0.87, "1", metadataJson));
        // Service quantity units with comma-separated values (will be split and matched by index)
        predictions.add(createPrediction("service_quantity_units", "5,3,4", "multi_value", 1, 0.90, "1", metadataJson));
        // Service quantity visits with comma-separated values (will be split and matched by index)
        predictions.add(createPrediction("service_quantity_visits", "*,1,3", "multi_value", 1, 0.88, "1", metadataJson));

        // ========== DIAGNOSIS FIELDS (multi_value) - Comma-separated values ==========
        // Diagnosis codes with comma-separated values (will be split into diagnosis_code_1, diagnosis_code_2, diagnosis_code_3)
        predictions.add(createPrediction("diagnosis_code", "E11.9,I10,M54.5", "multi_value", 1, 0.91, "1", metadataJson));
        // Diagnosis descriptions with comma-separated values (will be split and matched by index)
        predictions.add(createPrediction("diagnosis_description", "Type 2 diabetes,Essential hypertension,Low back pain", "multi_value", 1, 0.89, "1", metadataJson));
        // Code pointers with comma-separated values (will be split and matched by index)
        predictions.add(createPrediction("code_pointer", "1,2,3", "multi_value", 1, 0.88, "1", metadataJson));

        // ========== PROVIDER FIELDS (single_value) - All Provider Types ==========
        // Servicing Provider
        predictions.add(createPrediction("servicing_provider_npi", "1234567890", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_tin", "12-3456789", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_first_name", "Dr. Jane", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_last_name", "Smith", "single_value", 1, 0.92, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_address_line1", "456 Provider St", "single_value", 1, 0.91, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_address_line2", "Suite 100", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_city", "Chicago", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_state", "IL", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_zipcode", "60601", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction("servicing_provider_specialty", "Cardiology", "single_value", 1, 0.86, null, metadataJson));

        // Servicing Facility
        predictions.add(createPrediction("servicing_facility_npi", "9876543210", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_tin", "98-7654321", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_first_name", "General", "single_value", 1, 0.94, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_last_name", "Hospital", "single_value", 1, 0.92, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_address_line1", "789 Hospital Ave", "single_value", 1, 0.91, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_city", "Springfield", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_state", "IL", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("servicing_facility_zipcode", "62701", "single_value", 1, 0.87, null, metadataJson));

        // Referring Provider
        predictions.add(createPrediction("referring_provider_npi", "1111111111", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("referring_provider_tin", "11-1111111", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("referring_provider_first_name", "Dr. John", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("referring_provider_last_name", "Doe", "single_value", 1, 0.91, null, metadataJson));
        predictions.add(createPrediction("referring_provider_address_line1", "321 Referral St", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("referring_provider_city", "Chicago", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction("referring_provider_state", "IL", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("referring_provider_zipcode", "60601", "single_value", 1, 0.87, null, metadataJson));

        // Ordering Provider
        predictions.add(createPrediction("ordering_provider_npi", "2222222222", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_tin", "22-2222222", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_first_name", "Dr. Bob", "single_value", 1, 0.93, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_last_name", "Johnson", "single_value", 1, 0.91, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_address_line1", "654 Order St", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_city", "Springfield", "single_value", 1, 0.89, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_state", "IL", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("ordering_provider_zipcode", "62701", "single_value", 1, 0.87, null, metadataJson));

        // ========== ADDITIONAL PROPERTIES (single_value) ==========
        predictions.add(createPrediction("clinical_present_1", "Y", "single_value", 1, 0.95, null, metadataJson));
        predictions.add(createPrediction("clinical_present_2", "N", "single_value", 2, 0.94, null, metadataJson));
        predictions.add(createPrediction("level_of_care", "Urgent,Non-Urgent", "single_value", 1, 0.90, null, metadataJson));
        predictions.add(createPrediction("fax_report", "Y", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction("additional_auth_properties", "keyword1,keyword2,keyword3", "single_value", 1, 0.88, null, metadataJson));
        predictions.add(createPrediction("responsible_area_1", "Area1", "single_value", 1, 0.87, null, metadataJson));
        predictions.add(createPrediction("responsible_area_2", "Area2", "single_value", 2, 0.86, null, metadataJson));

        // ========== MEMBER ADDITIONAL PROPERTIES (single_value) ==========
        predictions.add(createPrediction("multiple_member_indicator", "Y", "single_value", 1, 0.85, null, metadataJson));
        predictions.add(createPrediction("newborn_request", "Y", "single_value", 1, 0.84, null, metadataJson));
        predictions.add(createPrediction("newborn_first_name", "Baby", "single_value", 1, 0.83, null, metadataJson));
        predictions.add(createPrediction("newborn_last_name", "Doe", "single_value", 1, 0.82, null, metadataJson));
        predictions.add(createPrediction("newborn_date_of_birth", "2024-01-01", "single_value", 1, 0.81, null, metadataJson));
        predictions.add(createPrediction("newborn_gender", "M", "single_value", 1, 0.80, null, metadataJson));

        return predictions;
    }

    private PredictionDTO createPrediction(String sorItemName, String predictedValue, 
                                           String lineItemType, int paperNo, 
                                           double precision, String sorContainerInstance,
                                           String metadataJson) {
        PredictionDTO dto = new PredictionDTO();
        dto.setPredictionId(System.currentTimeMillis() + (long)(Math.random() * 1000));
        dto.setOriginId("ORIGIN-001");
        dto.setTenantId(1L);
        dto.setGroupId(100L);
        dto.setBatchId("BATCH-001");
        dto.setRootPipelineId("ROOT-001");
        dto.setTransactionId("TXN-001");
        dto.setFeature("KIE");
        dto.setPaperNo(paperNo);
        dto.setLineItemType(lineItemType);
        dto.setSorItemName(sorItemName);
        dto.setPredictedValue(predictedValue);
        dto.setPrecision(precision);
        dto.setLeftPos(100.0);
        dto.setRightPos(200.0);
        dto.setUpperPos(50.0);
        dto.setLowerPos(80.0);
        dto.setImageWidth(1000);
        dto.setImageHeight(1000);
        dto.setIsMultiEntityEnabled(false);
        dto.setSorContainerInstance(sorContainerInstance);
        dto.setMetadataJson(metadataJson);
        return dto;
    }

    private String createCompleteMetadataJson() {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusSeconds(5);
            LocalDateTime endTime = LocalDateTime.now();

            MetadataContext metadataContext = new MetadataContext();
            metadataContext.setRequestTxnId("REQ-TXN-001");
            metadataContext.setUploadStatus("SUCCESS");
            metadataContext.setErrorMessage(null);
            metadataContext.setErrorMessageDetail(null);
            metadataContext.setErrorCode(200);
            metadataContext.setDocumentId("DOC-001");
            metadataContext.setInboundTransactionId("INBOUND-TXN-001");
            metadataContext.setTransactionId("TXN-001");
            metadataContext.setDocumentType("Medical");
            metadataContext.setInboundDocumentName("test_document.pdf");
            metadataContext.setDocumentExtension("pdf");
            metadataContext.setProcessStartTime(startTime.toString());
            metadataContext.setProcessEndTime(endTime.toString());
            metadataContext.setProcessedAt(endTime.toString());
            metadataContext.setCandidatePapers(Arrays.asList(1, 2, 3));

            return objectMapper.writeValueAsString(metadataContext);
        } catch (Exception e) {
            logger.error("Failed to create metadata JSON", e);
            return "{}";
        }
    }

    private Map<String, String> createCompleteConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100");
        config.put("AUMI_BBOX_SCALAR_WIDTH", "1000");
        config.put("AUMI_BBOX_SCALAR_HEIGHT", "1000");
        config.put("FLOAT_VALUE_ROUNDING_PRECISION", "2");
        config.put("aumi.reorder.paper.number", "false");
        config.put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false");
        return config;
    }
}
