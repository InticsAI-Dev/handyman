package in.handyman.raven.lib.custom.outbound.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.*;
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
 * Comprehensive test cases for MedicalPayloadGeneration covering ALL fields
 * Tests the complete integration flow with all possible prediction values
 */
@DisplayName("MedicalPayloadGeneration Comprehensive Test - All Fields Coverage")
class MedicalPayloadGenerationComprehensiveTest {

    private static final Logger log = LoggerFactory.getLogger(MedicalPayloadGenerationComprehensiveTest.class);
    private MedicalPayloadGeneration medicalPayloadGeneration;
    private ObjectMapper objectMapper;
    private Map<String, String> configMap;

    @BeforeEach
    void setUp() {
        medicalPayloadGeneration = new MedicalPayloadGeneration(log);
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        configMap = new HashMap<>();
        configMap.put("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100");
        configMap.put("AUMI_BBOX_SCALAR_WIDTH", "1000");
        configMap.put("AUMI_BBOX_SCALAR_HEIGHT", "1000");
        configMap.put("FLOAT_VALUE_ROUNDING_PRECISION", "2");
        configMap.put("aumi.reorder.paper.number", "false");
        configMap.put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false");
    }

    @Test
    @DisplayName("Test Complete MedicalPayloadGeneration with ALL Fields Populated")
    void testCompleteMedicalPayloadGenerationWithAllFields() throws Exception {
        // Given - Create predictions for ALL fields
        List<PredictionDTO> predictions = createAllFieldsPredictionList();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then - Verify complete response structure
        assertNotNull(response, "Response should not be null");
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getAumipayload(), "Payload should not be null");
        assertNotNull(response.getMetadata(), "Metadata should not be null");

        MedicalPayload payload = response.getAumipayload();

        // Verify Member Section - ALL fields
        verifyMemberSection(payload);

        // Verify Authorization Section - ALL fields
        verifyAuthorizationSection(payload);

        // Verify Services - Multiple entries
        verifyServiceSection(payload);

        // Verify Diagnosis - Multiple entries
        verifyDiagnosisSection(payload);

        // Verify Providers - All types
        verifyProviderSection(payload);

        // Verify Additional Properties
        verifyAdditionalProperties(payload);

        // Verify Member Additional Properties
        verifyMemberAdditionalProperties(payload);

        // Log the complete JSON for inspection
        logCompleteResponse(response);
    }

    @Test
    @DisplayName("Test MedicalPayloadGeneration with Clean Status Enabled - All Fields")
    void testMedicalPayloadGenerationWithCleanStatusAllFields() throws Exception {
        // Given
        configMap.put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "true");
        List<PredictionDTO> predictions = createAllFieldsPredictionList();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        
        MedicalPayload payload = response.getAumipayload();
        
        // With clean status, empty values should not be included
        // Verify that populated fields are present
        assertNotNull(payload.getHcid(), "HCID should be populated");
        assertNotNull(payload.getMemberFirstName(), "Member first name should be populated");
        assertNotNull(payload.getMemberLastName(), "Member last name should be populated");
    }

    @Test
    @DisplayName("Test Multiple Services with All Service Fields")
    void testMultipleServicesWithAllFields() throws Exception {
        // Given - Create multiple service entries with all fields
        List<PredictionDTO> predictions = createMultipleServicesWithAllFields();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getService());
        assertTrue(response.getAumipayload().getService().size() >= 2, 
                "Should have multiple service entries");

        // Verify each service has all fields
        for (ServiceModifier service : response.getAumipayload().getService()) {
            assertNotNull(service.getCd(), "Service code should not be null");
            assertNotNull(service.getCd().getValue(), "Service code value should not be null");
            assertNotNull(service.getServiceQuantity(), "Service quantities should not be null");
            assertFalse(service.getServiceQuantity().isEmpty(), "Service quantities should not be empty");
        }
    }

    @Test
    @DisplayName("Test Multiple Diagnosis with All Diagnosis Fields")
    void testMultipleDiagnosisWithAllFields() throws Exception {
        // Given - Create multiple diagnosis entries with all fields
        List<PredictionDTO> predictions = createMultipleDiagnosisWithAllFields();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getDiagnosis());
        assertTrue(response.getAumipayload().getDiagnosis().size() >= 2, 
                "Should have multiple diagnosis entries");

        // Verify each diagnosis has all fields
        for (Diagonsis diagnosis : response.getAumipayload().getDiagnosis()) {
            assertNotNull(diagnosis.getCd(), "Diagnosis code should not be null");
            assertNotNull(diagnosis.getCd().getValue(), "Diagnosis code value should not be null");
            assertNotNull(diagnosis.getDesc(), "Diagnosis description should not be null");
            assertNotNull(diagnosis.getCodePointer(), "Code pointer should not be null");
        }
    }

    @Test
    @DisplayName("Test All Provider Types with Complete Provider Information")
    void testAllProviderTypesWithCompleteInfo() throws Exception {
        // Given - Create all provider types with complete information
        List<PredictionDTO> predictions = createAllProviderTypesWithCompleteInfo();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getProvider());
        assertTrue(response.getAumipayload().getProvider().size() >= 4, 
                "Should have all provider types");

        // Verify each provider has complete information
        for (Provider provider : response.getAumipayload().getProvider()) {
            assertNotNull(provider.getProviderCategory(), "Provider category should not be null");
            assertNotNull(provider.getProviderCategory().getValue(), "Provider category value should not be null");
        }
    }

    @Test
    @DisplayName("Test All Additional Properties Types")
    void testAllAdditionalPropertiesTypes() throws Exception {
        // Given - Create all types of additional properties
        List<PredictionDTO> predictions = createAllAdditionalPropertiesTypes();
        String metadataJson = createCompleteMetadataJson();

        // When
        MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictions, configMap, metadataJson);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAumipayload());
        assertNotNull(response.getAumipayload().getAdditionalProperties());
        assertFalse(response.getAumipayload().getAdditionalProperties().isEmpty(), 
                "Additional properties should not be empty");

        // Verify different property types are present
        List<String> propNames = response.getAumipayload().getAdditionalProperties().stream()
                .map(AdditionalProperties::getPropName)
                .collect(Collectors.toList());

        assertTrue(propNames.contains("CLINICAL_PRESENT"), "Should have CLINICAL_PRESENT");
        assertTrue(propNames.contains("AUTH_KEYWORD"), "Should have AUTH_KEYWORD");
        assertTrue(propNames.contains("AUTH_ADDL_KEYWORD"), "Should have AUTH_ADDL_KEYWORD");
        assertTrue(propNames.contains("SORTING_KEYWORD"), "Should have SORTING_KEYWORD");
        assertTrue(propNames.contains("FAX_REPORT"), "Should have FAX_REPORT");
    }

    @Test
    @DisplayName("Test Complete Integration - Simulating CustomResponseAction Flow")
    void testCompleteIntegrationSimulatingCustomResponseAction() throws Exception {
        // Given - Simulate the flow from CustomResponseAction
        List<PredictionDTO> predictions = createAllFieldsPredictionList();
        
        // Group by originId (as CustomResponseAction does)
        Map<String, List<PredictionDTO>> groupedByOriginId = new HashMap<>();
        groupedByOriginId.put("ORIGIN-001", predictions);

        String metadataJson = createCompleteMetadataJson();

        // When - Process each originId group (simulating CustomResponseAction.forEach)
        List<MedicalOutboundResponse> responses = new ArrayList<>();
        for (Map.Entry<String, List<PredictionDTO>> entry : groupedByOriginId.entrySet()) {
            String originId = entry.getKey();
            List<PredictionDTO> predictionList = entry.getValue();
            
            if (!predictionList.isEmpty()) {
                MedicalOutboundResponse response = medicalPayloadGeneration.buildMedicalOutboundResponse(
                        predictionList, configMap, metadataJson);
                responses.add(response);
            }
        }

        // Then
        assertFalse(responses.isEmpty(), "Should have at least one response");
        MedicalOutboundResponse response = responses.get(0);
        
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getAumipayload());
        
        // Verify complete payload structure
        verifyCompletePayloadStructure(response.getAumipayload());
    }

    // ========== VERIFICATION METHODS ==========

    private void verifyMemberSection(MedicalPayload payload) {
        assertNotNull(payload.getHcid(), "HCID should not be null");
        assertNotNull(payload.getMedicaidId(), "Medicaid ID should not be null");
        assertNotNull(payload.getGroupId(), "Group ID should not be null");
        assertNotNull(payload.getMemberFirstName(), "Member first name should not be null");
        assertNotNull(payload.getMemberLastName(), "Member last name should not be null");
        assertNotNull(payload.getMemberDOB(), "Member DOB should not be null");
        assertNotNull(payload.getMemberGender(), "Member gender should not be null");
        assertNotNull(payload.getMemberAddressLine1(), "Member address line 1 should not be null");
        assertNotNull(payload.getMemberCity(), "Member city should not be null");
        assertNotNull(payload.getMemberState(), "Member state should not be null");
        assertNotNull(payload.getMemberZipCode(), "Member zip code should not be null");
    }

    private void verifyAuthorizationSection(MedicalPayload payload) {
        assertNotNull(payload.getAuthId(), "Auth ID should not be null");
        assertNotNull(payload.getLevelOfService(), "Level of service should not be null");
        assertNotNull(payload.getServiceFromDate(), "Service from date should not be null");
        assertNotNull(payload.getServiceToDate(), "Service to date should not be null");
        assertNotNull(payload.getAuthAdmitDate(), "Auth admit date should not be null");
        assertNotNull(payload.getAuthDischargeDate(), "Auth discharge date should not be null");
        assertNotNull(payload.getFaxReceivedDate(), "Fax received date should not be null");
        assertNotNull(payload.getTotalServiceDays(), "Total service days should not be null");
        assertNotNull(payload.getNotificationType(), "Notification type should not be null");
    }

    private void verifyServiceSection(MedicalPayload payload) {
        assertNotNull(payload.getService(), "Service list should not be null");
        assertFalse(payload.getService().isEmpty(), "Service list should not be empty");
        
        for (ServiceModifier service : payload.getService()) {
            assertNotNull(service.getCd(), "Service code should not be null");
            if (service.getModifier() != null) {
                assertFalse(service.getModifier().isEmpty(), "Service modifiers should not be empty");
            }
            if (service.getServiceQuantity() != null) {
                assertFalse(service.getServiceQuantity().isEmpty(), "Service quantities should not be empty");
            }
        }
    }

    private void verifyDiagnosisSection(MedicalPayload payload) {
        assertNotNull(payload.getDiagnosis(), "Diagnosis list should not be null");
        assertFalse(payload.getDiagnosis().isEmpty(), "Diagnosis list should not be empty");
        
        for (Diagonsis diagnosis : payload.getDiagnosis()) {
            assertNotNull(diagnosis.getCd(), "Diagnosis code should not be null");
            assertNotNull(diagnosis.getDesc(), "Diagnosis description should not be null");
            assertNotNull(diagnosis.getCodePointer(), "Code pointer should not be null");
        }
    }

    private void verifyProviderSection(MedicalPayload payload) {
        assertNotNull(payload.getProvider(), "Provider list should not be null");
        assertFalse(payload.getProvider().isEmpty(), "Provider list should not be empty");
        
        Set<String> providerCategories = new HashSet<>();
        for (Provider provider : payload.getProvider()) {
            assertNotNull(provider.getProviderCategory(), "Provider category should not be null");
            providerCategories.add(provider.getProviderCategory().getValue());
        }
        
        // Verify all provider types are present
        assertTrue(providerCategories.contains("Servicing Provider"), 
                "Should have Servicing Provider");
        assertTrue(providerCategories.contains("Servicing Facility"), 
                "Should have Servicing Facility");
        assertTrue(providerCategories.contains("Requesting Provider"), 
                "Should have Requesting Provider");
        assertTrue(providerCategories.contains("Ordering Provider"), 
                "Should have Ordering Provider");
    }

    private void verifyAdditionalProperties(MedicalPayload payload) {
        assertNotNull(payload.getAdditionalProperties(), 
                "Additional properties should not be null");
        assertFalse(payload.getAdditionalProperties().isEmpty(), 
                "Additional properties should not be empty");
    }

    private void verifyMemberAdditionalProperties(MedicalPayload payload) {
        assertNotNull(payload.getMemberAdditionalProperties(), 
                "Member additional properties should not be null");
        assertFalse(payload.getMemberAdditionalProperties().isEmpty(), 
                "Member additional properties should not be empty");
    }

    private void verifyCompletePayloadStructure(MedicalPayload payload) {
        verifyMemberSection(payload);
        verifyAuthorizationSection(payload);
        verifyServiceSection(payload);
        verifyDiagnosisSection(payload);
        verifyProviderSection(payload);
        verifyAdditionalProperties(payload);
        verifyMemberAdditionalProperties(payload);
    }

    // ========== HELPER METHODS TO CREATE PREDICTIONS ==========

    private List<PredictionDTO> createAllFieldsPredictionList() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // ========== MEMBER FIELDS (single_value) ==========
        predictions.add(createPrediction("member_id", "MEMBER-12345", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("medicaid_id", "MEDICAID-67890", "single_value", 1, 0.94, null));
        predictions.add(createPrediction("member_group_id", "GROUP-001", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("member_first_name", "John", "single_value", 1, 0.92, null));
        predictions.add(createPrediction("member_last_name", "Doe", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("member_date_of_birth", "1980-01-15", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("member_gender", "M", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("member_address_line1", "123 Main Street", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("member_city", "Springfield", "single_value", 1, 0.86, null));
        predictions.add(createPrediction("member_state", "IL", "single_value", 1, 0.85, null));
        predictions.add(createPrediction("member_zipcode", "62701", "single_value", 1, 0.84, null));

        // ========== AUTHORIZATION FIELDS (single_value) ==========
        predictions.add(createPrediction("auth_id", "AUTH-789", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("level_of_service", "Urgent", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("service_from_date", "2024-01-01", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("service_to_date", "2024-01-31", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("auth_admit_date", "2024-01-01", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("auth_discharge_date", "2024-01-31", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("fax_received_date", "2024-01-15", "single_value", 1, 0.86, null));
        predictions.add(createPrediction("total_service_days", "30", "single_value", 1, 0.85, null));
        predictions.add(createPrediction("notification_type", "Initial", "single_value", 1, 0.92, null));

        // ========== SERVICE FIELDS (multi_value) ==========
        // Service 1
        predictions.add(createPrediction("service_code", "99213", "multi_value", 1, 0.85, "1"));
        predictions.add(createPrediction("service_code_modifier", "25", "multi_value", 1, 0.87, "1"));
        predictions.add(createPrediction("service_quantity_units", "5", "multi_value", 1, 0.90, "1"));
        predictions.add(createPrediction("service_quantity_visits", "2", "multi_value", 1, 0.88, "1"));
        
        // Service 2
        predictions.add(createPrediction("service_code", "99214", "multi_value", 1, 0.86, "2"));
        predictions.add(createPrediction("service_code_modifier", "26", "multi_value", 1, 0.88, "2"));
        predictions.add(createPrediction("service_quantity_units", "3", "multi_value", 1, 0.91, "2"));

        // ========== DIAGNOSIS FIELDS (multi_value) ==========
        // Diagnosis 1
        predictions.add(createPrediction("diagnosis_code", "E11.9", "multi_value", 1, 0.91, "1"));
        predictions.add(createPrediction("diagnosis_description", "Type 2 diabetes mellitus without complications", "multi_value", 1, 0.89, "1"));
        predictions.add(createPrediction("code_pointer", "1", "multi_value", 1, 0.88, "1"));
        
        // Diagnosis 2
        predictions.add(createPrediction("diagnosis_code", "I10", "multi_value", 1, 0.92, "2"));
        predictions.add(createPrediction("diagnosis_description", "Essential hypertension", "multi_value", 1, 0.90, "2"));
        predictions.add(createPrediction("code_pointer", "2", "multi_value", 1, 0.89, "2"));

        // ========== PROVIDER FIELDS (single_value) ==========
        // Servicing Provider
        predictions.add(createPrediction("servicing_provider_npi", "1234567890", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("servicing_provider_tin", "12-3456789", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("servicing_provider_first_name", "Dr. Jane", "single_value", 1, 0.94, null));
        predictions.add(createPrediction("servicing_provider_last_name", "Smith", "single_value", 1, 0.92, null));
        predictions.add(createPrediction("servicing_provider_address_line1", "456 Provider St", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("servicing_provider_address_line2", "Suite 100", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("servicing_provider_city", "Chicago", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("servicing_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("servicing_provider_zipcode", "60601", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("servicing_provider_specialty", "Cardiology", "single_value", 1, 0.86, null));

        // Servicing Facility
        predictions.add(createPrediction("servicing_facility_npi", "9876543210", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("servicing_facility_tin", "98-7654321", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("servicing_facility_first_name", "General", "single_value", 1, 0.94, null));
        predictions.add(createPrediction("servicing_facility_last_name", "Hospital", "single_value", 1, 0.92, null));
        predictions.add(createPrediction("servicing_facility_address_line1", "789 Hospital Ave", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("servicing_facility_city", "Springfield", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("servicing_facility_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("servicing_facility_zipcode", "62701", "single_value", 1, 0.87, null));

        // Referring Provider
        predictions.add(createPrediction("referring_provider_npi", "1111111111", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("referring_provider_first_name", "Dr. John", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("referring_provider_last_name", "Doe", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("referring_provider_address_line1", "321 Referral St", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("referring_provider_city", "Chicago", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("referring_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("referring_provider_zipcode", "60601", "single_value", 1, 0.87, null));

        // Ordering Provider
        predictions.add(createPrediction("ordering_provider_npi", "2222222222", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("ordering_provider_first_name", "Dr. Bob", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("ordering_provider_last_name", "Johnson", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("ordering_provider_address_line1", "654 Order St", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("ordering_provider_city", "Springfield", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("ordering_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("ordering_provider_zipcode", "62701", "single_value", 1, 0.87, null));

        // ========== ADDITIONAL PROPERTIES (single_value) ==========
        predictions.add(createPrediction("clinical_present_1", "Y", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("clinical_present_2", "N", "single_value", 2, 0.94, null));
        predictions.add(createPrediction("level_of_care", "Urgent,Non-Urgent", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("fax_report", "Y", "single_value", 1, 0.85, null));
        predictions.add(createPrediction("additional_auth_properties", "keyword1,keyword2,keyword3", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("responsible_area_1", "Area1", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("responsible_area_2", "Area2", "single_value", 2, 0.86, null));

        // ========== MEMBER ADDITIONAL PROPERTIES (single_value) ==========
        predictions.add(createPrediction("multiple_member_indicator", "Y", "single_value", 1, 0.85, null));
        predictions.add(createPrediction("newborn_request", "Y", "single_value", 1, 0.84, null));
        predictions.add(createPrediction("newborn_first_name", "Baby", "single_value", 1, 0.83, null));
        predictions.add(createPrediction("newborn_last_name", "Doe", "single_value", 1, 0.82, null));
        predictions.add(createPrediction("newborn_date_of_birth", "2024-01-01", "single_value", 1, 0.81, null));
        predictions.add(createPrediction("newborn_gender", "M", "single_value", 1, 0.80, null));

        return predictions;
    }

    private List<PredictionDTO> createMultipleServicesWithAllFields() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // Service 1 - Complete
        predictions.add(createPrediction("service_code", "99213", "multi_value", 1, 0.85, "1"));
        predictions.add(createPrediction("service_code_modifier", "25", "multi_value", 1, 0.87, "1"));
        predictions.add(createPrediction("service_quantity_units", "5", "multi_value", 1, 0.90, "1"));
        predictions.add(createPrediction("service_quantity_visits", "2", "multi_value", 1, 0.88, "1"));

        // Service 2 - Complete
        predictions.add(createPrediction("service_code", "99214", "multi_value", 1, 0.86, "2"));
        predictions.add(createPrediction("service_code_modifier", "26", "multi_value", 1, 0.88, "2"));
        predictions.add(createPrediction("service_quantity_units", "3", "multi_value", 1, 0.91, "2"));
        predictions.add(createPrediction("service_quantity_visits", "1", "multi_value", 1, 0.89, "2"));

        // Service 3 - With modifier only
        predictions.add(createPrediction("service_code", "99215", "multi_value", 1, 0.87, "3"));
        predictions.add(createPrediction("service_code_modifier", "27", "multi_value", 1, 0.89, "3"));

        return predictions;
    }

    private List<PredictionDTO> createMultipleDiagnosisWithAllFields() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // Diagnosis 1
        predictions.add(createPrediction("diagnosis_code", "E11.9", "multi_value", 1, 0.91, "1"));
        predictions.add(createPrediction("diagnosis_description", "Type 2 diabetes", "multi_value", 1, 0.89, "1"));
        predictions.add(createPrediction("code_pointer", "1", "multi_value", 1, 0.88, "1"));

        // Diagnosis 2
        predictions.add(createPrediction("diagnosis_code", "I10", "multi_value", 1, 0.92, "2"));
        predictions.add(createPrediction("diagnosis_description", "Essential hypertension", "multi_value", 1, 0.90, "2"));
        predictions.add(createPrediction("code_pointer", "2", "multi_value", 1, 0.89, "2"));

        // Diagnosis 3
        predictions.add(createPrediction("diagnosis_code", "M54.5", "multi_value", 1, 0.93, "3"));
        predictions.add(createPrediction("diagnosis_description", "Low back pain", "multi_value", 1, 0.91, "3"));
        predictions.add(createPrediction("code_pointer", "3", "multi_value", 1, 0.90, "3"));

        return predictions;
    }

    private List<PredictionDTO> createAllProviderTypesWithCompleteInfo() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // Servicing Provider - Complete
        predictions.add(createPrediction("servicing_provider_npi", "1234567890", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("servicing_provider_tin", "12-3456789", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("servicing_provider_first_name", "Dr. Jane", "single_value", 1, 0.94, null));
        predictions.add(createPrediction("servicing_provider_last_name", "Smith", "single_value", 1, 0.92, null));
        predictions.add(createPrediction("servicing_provider_address_line1", "456 Provider St", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("servicing_provider_city", "Chicago", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("servicing_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("servicing_provider_zipcode", "60601", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("servicing_provider_specialty", "Cardiology", "single_value", 1, 0.86, null));

        // Servicing Facility - Complete
        predictions.add(createPrediction("servicing_facility_npi", "9876543210", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("servicing_facility_first_name", "General", "single_value", 1, 0.94, null));
        predictions.add(createPrediction("servicing_facility_last_name", "Hospital", "single_value", 1, 0.92, null));
        predictions.add(createPrediction("servicing_facility_address_line1", "789 Hospital Ave", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("servicing_facility_city", "Springfield", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("servicing_facility_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("servicing_facility_zipcode", "62701", "single_value", 1, 0.87, null));

        // Referring Provider - Complete
        predictions.add(createPrediction("referring_provider_npi", "1111111111", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("referring_provider_first_name", "Dr. John", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("referring_provider_last_name", "Doe", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("referring_provider_address_line1", "321 Referral St", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("referring_provider_city", "Chicago", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("referring_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("referring_provider_zipcode", "60601", "single_value", 1, 0.87, null));

        // Ordering Provider - Complete
        predictions.add(createPrediction("ordering_provider_npi", "2222222222", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("ordering_provider_first_name", "Dr. Bob", "single_value", 1, 0.93, null));
        predictions.add(createPrediction("ordering_provider_last_name", "Johnson", "single_value", 1, 0.91, null));
        predictions.add(createPrediction("ordering_provider_address_line1", "654 Order St", "single_value", 1, 0.90, null));
        predictions.add(createPrediction("ordering_provider_city", "Springfield", "single_value", 1, 0.89, null));
        predictions.add(createPrediction("ordering_provider_state", "IL", "single_value", 1, 0.88, null));
        predictions.add(createPrediction("ordering_provider_zipcode", "62701", "single_value", 1, 0.87, null));

        return predictions;
    }

    private List<PredictionDTO> createAllAdditionalPropertiesTypes() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createCompleteMetadataJson();

        // Clinical Present
        predictions.add(createPrediction("clinical_present_1", "Y", "single_value", 1, 0.95, null));
        predictions.add(createPrediction("clinical_present_2", "N", "single_value", 2, 0.94, null));

        // Level of Care (creates AUTH_KEYWORD)
        predictions.add(createPrediction("level_of_care", "Urgent,Non-Urgent", "single_value", 1, 0.90, null));

        // Fax Report
        predictions.add(createPrediction("fax_report", "Y", "single_value", 1, 0.85, null));

        // Additional Auth Properties (creates AUTH_ADDL_KEYWORD)
        predictions.add(createPrediction("additional_auth_properties", "keyword1,keyword2,keyword3", "single_value", 1, 0.88, null));

        // Responsible Area (creates SORTING_KEYWORD)
        predictions.add(createPrediction("responsible_area_1", "Area1", "single_value", 1, 0.87, null));
        predictions.add(createPrediction("responsible_area_2", "Area2", "single_value", 2, 0.86, null));

        return predictions;
    }

    private PredictionDTO createPrediction(String sorItemName, String predictedValue, 
                                           String lineItemType, int paperNo, 
                                           double precision, String sorContainerInstance) {
        return PredictionDTO.builder()
                .predictionId(System.currentTimeMillis() + (long)(Math.random() * 1000))
                .originId("ORIGIN-001")
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
                .metadataJson(createCompleteMetadataJson())
                .build();
    }

    private String createCompleteMetadataJson() {
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

    private void logCompleteResponse(MedicalOutboundResponse response) {
        try {
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response);
            log.info("==========================================");
            log.info("COMPLETE MEDICAL OUTBOUND RESPONSE");
            log.info("==========================================");
            log.info("\n{}", prettyJson);
            log.info("==========================================");
        } catch (Exception e) {
            log.error("Failed to serialize response to JSON", e);
        }
    }
}
