package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.model.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ProductResponseActionTest {

    @Test
    @DisplayName("Test executeIf method - should return condition value")
    void testExecuteIf() throws Exception {
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        
        // Test with condition true
        ProductResponse productResponseTrue = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .name("test product response")
                .resultTable("test_table")
                .resourceConn("test_conn")
                .querySet("SELECT * FROM test_table")
                .build();
        
        ProductResponseAction productResponseActionTrue = new ProductResponseAction(actionExecutionAudit, log, productResponseTrue);
        assertTrue(productResponseActionTrue.executeIf(), "executeIf should return true when condition is true");
        
        // Test with condition false
        ProductResponse productResponseFalse = ProductResponse.builder()
                .tenantId(1L)
                .condition(false)
                .name("test product response")
                .resultTable("test_table")
                .resourceConn("test_conn")
                .querySet("SELECT * FROM test_table")
                .build();
        
        ProductResponseAction productResponseActionFalse = new ProductResponseAction(actionExecutionAudit, log, productResponseFalse);
        assertFalse(productResponseActionFalse.executeIf(), "executeIf should return false when condition is false");
    }

    @Test
    @DisplayName("Test extractInboundTransactionId method")
    void testExtractInboundTransactionId() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        ProductResponse productResponse = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .name("test product response")
                .build();
        ProductResponseAction productResponseAction = new ProductResponseAction(actionExecutionAudit, log, productResponse);

        // Use reflection to access private method
        Method extractInboundTransactionIdMethod = ProductResponseAction.class.getDeclaredMethod(
                "extractInboundTransactionId",
                String.class, ObjectMapper.class
        );
        extractInboundTransactionIdMethod.setAccessible(true);

        // Test with valid metadata JSON
        String metadataJson = "{\"documentId\":\"DOC-001\",\"inboundTransactionId\":\"INBOUND-TXN-001\",\"requestTxnId\":\"REQ-TXN-001\"}";
        String inboundTransactionId = (String) extractInboundTransactionIdMethod.invoke(
                productResponseAction,
                metadataJson,
                objectMapper
        );
        assertEquals("INBOUND-TXN-001", inboundTransactionId, "Should extract inboundTransactionId from metadata");

        // Test with null metadata
        String nullResult = (String) extractInboundTransactionIdMethod.invoke(
                productResponseAction,
                null,
                objectMapper
        );
        assertNull(nullResult, "Should return null for null metadata");

        // Test with empty metadata
        String emptyResult = (String) extractInboundTransactionIdMethod.invoke(
                productResponseAction,
                "",
                objectMapper
        );
        assertNull(emptyResult, "Should return null for empty metadata");

        // Test with invalid JSON
        String invalidJson = "{invalid json}";
        String invalidResult = (String) extractInboundTransactionIdMethod.invoke(
                productResponseAction,
                invalidJson,
                objectMapper
        );
        assertNull(invalidResult, "Should return null for invalid JSON");
    }

    @Test
    @DisplayName("Test grouping by originId")
    void testGroupingByOriginId() {
        List<PredictionDTO> predictionDTOList = createTestPredictionDTOList();

        // Add predictions with different originId
        PredictionDTO differentOrigin = PredictionDTO.builder()
                .predictionId(100L)
                .originId("TEST-ORIGIN-002")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .sorItemName("member_id")
                .predictedValue("MEMBER-99999")
                .precision(0.90)
                .imageWidth(1000)
                .imageHeight(1000)
                .metadataJson("{\"documentId\":\"DOC-002\",\"inboundTransactionId\":\"INBOUND-TXN-002\"}")
                .build();

        predictionDTOList.add(differentOrigin);

        // Group by originId
        Map<String, List<PredictionDTO>> groupedByOriginId = predictionDTOList.stream()
                .collect(java.util.stream.Collectors.groupingBy(PredictionDTO::getOriginId));

        assertEquals(2, groupedByOriginId.size(), "Should have 2 originIds");
        assertTrue(groupedByOriginId.containsKey("TEST-ORIGIN-001"), "Should contain TEST-ORIGIN-001");
        assertTrue(groupedByOriginId.containsKey("TEST-ORIGIN-002"), "Should contain TEST-ORIGIN-002");
        
        // Verify each group has correct data
        List<PredictionDTO> group1 = groupedByOriginId.get("TEST-ORIGIN-001");
        assertNotNull(group1, "Group 1 should not be null");
        assertTrue(group1.size() > 0, "Group 1 should have predictions");
        assertEquals("TEST-ORIGIN-001", group1.get(0).getOriginId(), "First group should have correct originId");

        List<PredictionDTO> group2 = groupedByOriginId.get("TEST-ORIGIN-002");
        assertNotNull(group2, "Group 2 should not be null");
        assertEquals(1, group2.size(), "Group 2 should have 1 prediction");
        assertEquals("TEST-ORIGIN-002", group2.get(0).getOriginId(), "Second group should have correct originId");
    }

    @Test
    @DisplayName("Test JSON generation with real-world data structure")
    void testBuildProductResponseJsonWithRealWorldData() throws Exception {
        // Create ObjectMapper
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // Create PredictionDTO list based on the provided JSON structure
        List<PredictionDTO> predictionDTOList = createRealWorldPredictionDTOList();

        // Create ProductResponseAction instance
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        ProductResponse productResponse = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .name("test product response")
                .build();
        ProductResponseAction productResponseAction = new ProductResponseAction(actionExecutionAudit, log, productResponse);

        // Use reflection to access private method
        Method buildProductResponseJsonMethod = ProductResponseAction.class.getDeclaredMethod(
                "buildProductResponseJson",
                List.class, String.class, Long.class, Long.class, String.class, String.class, String.class, String.class, ObjectMapper.class
        );
        buildProductResponseJsonMethod.setAccessible(true);

        // Call the method with real-world data
        String originId = "ORIGIN-521";
        Long tenantId = 1L;
        Long groupId = 100L;
        String batchId = "BATCH-001";
        String rootPipelineId = "ROOT-001";
        String transactionId = "TRZ-8425";
        String metadataJson = createRealWorldMetadataJson();

        JsonNode result = (JsonNode) buildProductResponseJsonMethod.invoke(
                productResponseAction,
                predictionDTOList,
                originId,
                tenantId,
                groupId,
                batchId,
                rootPipelineId,
                transactionId,
                metadataJson,
                objectMapper
        );

        // Log the generated JSON
        try {
            String generatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            log.info("Generated JSON for testBuildProductResponseJsonWithRealWorldData:\n{}", generatedJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON for logging", e);
        }

        // Verify basic document info matches expected structure
        assertNotNull(result, "Result should not be null");
        assertEquals(originId, result.get("originId").asText(), "OriginId should match");
        
        // Verify documentId if present
        if (result.has("documentId") && !result.get("documentId").isNull()) {
            assertEquals("MCR_P3_Notice_Of_Admission", result.get("documentId").asText(), "DocumentId should match");
        }
        
        // Verify metadata fields are present (may be empty strings if not in metadata)
        assertTrue(result.has("extension"), "Should have extension field");
        assertTrue(result.has("totalProcessedDuration"), "Should have totalProcessedDuration field");
        assertTrue(result.has("sourceFileURI"), "Should have sourceFileURI field");
        assertTrue(result.has("preprocessedFileURI"), "Should have preprocessedFileURI field");
        
        // Verify process times if present in metadata
        if (result.has("processStartedOn") && !result.get("processStartedOn").isNull()) {
            assertNotNull(result.get("processStartedOn").asText(), "processStartedOn should not be null");
        }
        if (result.has("processCompletedOn") && !result.get("processCompletedOn").isNull()) {
            assertNotNull(result.get("processCompletedOn").asText(), "processCompletedOn should not be null");
        }

        // Verify totalPages
        assertTrue(result.has("totalPages"), "Should have totalPages field");
        assertTrue(result.get("totalPages").asInt() >= 2, "TotalPages should be at least 2");

        // Verify paperInfo array
        assertTrue(result.has("paperInfo"), "Should have paperInfo array");
        JsonNode paperInfoArray = result.get("paperInfo");
        assertTrue(paperInfoArray.isArray(), "PaperInfo should be an array");
        assertTrue(paperInfoArray.size() >= 2, "Should have at least 2 pages");

        // Verify first page structure (matching provided JSON structure)
        JsonNode firstPage = paperInfoArray.get(0);
        assertTrue(firstPage.has("pageNo"), "Page should have pageNo");
        assertTrue(firstPage.has("paperType"), "Page should have paperType");
        assertTrue(firstPage.has("featureInfo"), "Page should have featureInfo");

        // Verify featureInfo structure
        JsonNode featureInfo = firstPage.get("featureInfo");
        assertNotNull(featureInfo, "FeatureInfo should not be null");
        
        // Verify that feature-specific objects (like kie) have originId, tenantId, pageNo
        if (featureInfo.has("kie") && !featureInfo.get("kie").isNull()) {
            JsonNode kie = featureInfo.get("kie");
            assertTrue(kie.has("originId"), "KIE feature should have originId");
            assertTrue(kie.has("tenantId"), "KIE feature should have tenantId");
            assertTrue(kie.has("pageNo"), "KIE feature should have pageNo");
        }

        // Verify KIE feature exists
        if (featureInfo.has("kie")) {
            JsonNode kie = featureInfo.get("kie");
            assertTrue(kie.has("entityDetails"), "KIE should have entityDetails");
            JsonNode entityDetails = kie.get("entityDetails");
            assertTrue(entityDetails.has("sorContainerDetails"), "EntityDetails should have sorContainerDetails");
        }

        // Verify summary sections exist
        assertTrue(result.has("kvpSummary"), "Should have kvpSummary");
        assertTrue(result.has("currencySummary"), "Should have currencySummary");
        assertTrue(result.has("bulletinSummary"), "Should have bulletinSummary");
        assertTrue(result.has("paragraphSummary"), "Should have paragraphSummary");

        // Verify kvpSummary structure matches expected containers
        JsonNode kvpSummary = result.get("kvpSummary");
        assertTrue(kvpSummary.has("MEMBER_DETAILS") || kvpSummary.has("SERVICING_PROVIDER_DETAILS") || 
                   kvpSummary.has("SERVICE_CODE") || kvpSummary.has("AUTH_ID"), 
                   "KVP Summary should have expected containers");

        // Print the generated JSON for comparison
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        log.info("Generated Product Response JSON:\n{}", jsonString);
    }

    /**
     * Creates PredictionDTO list based on the real-world JSON structure provided
     */
    private List<PredictionDTO> createRealWorldPredictionDTOList() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createRealWorldMetadataJson();

        // Page 1 Predictions
        // SERVICING_PROVIDER_DETAILS - Page 1
        predictions.add(createPredictionDTO(1L, "ORIGIN-521", 1, "KIE", "servicing_provider_tin", "", 0.50, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, null, null, null, null));

        // FAX_DETAILS - Page 1
        predictions.add(createPredictionDTO(2L, "ORIGIN-521", 1, "KIE", "fax_received_date", "", 0.50, 
                "FAX_DETAILS", metadataJson, null, null, null, null));

        // ORDERING_PROVIDER_DETAILS - Page 1 (multiple fields)
        predictions.add(createPredictionDTO(3L, "ORIGIN-521", 1, "KIE", "ordering_provider_tin", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(4L, "ORIGIN-521", 1, "KIE", "ordering_provider_city", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(5L, "ORIGIN-521", 1, "KIE", "ordering_provider_npi", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(6L, "ORIGIN-521", 1, "KIE", "ordering_provider_zipcode", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(7L, "ORIGIN-521", 1, "KIE", "ordering_provider_state", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(8L, "ORIGIN-521", 1, "KIE", "ordering_provider_last_name", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(9L, "ORIGIN-521", 1, "KIE", "ordering_provider_full_name", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(10L, "ORIGIN-521", 1, "KIE", "ordering_provider_address_line1", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(11L, "ORIGIN-521", 1, "KIE", "ordering_provider_first_name", "", 0.50, 
                "ORDERING_PROVIDER_DETAILS", metadataJson, null, null, null, null));

        // ADDITIONAL_PROPERTIES - Page 1
        predictions.add(createPredictionDTO(12L, "ORIGIN-521", 1, "KIE", "additional_properties", "", 0.50, 
                "ADDITIONAL_PROPERTIES", metadataJson, null, null, null, null));

        // REFERRING_PROVIDER_DETAILS - Page 1 (multiple fields)
        predictions.add(createPredictionDTO(13L, "ORIGIN-521", 1, "KIE", "referring_provider_last_name", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(14L, "ORIGIN-521", 1, "KIE", "referring_provider_city", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(15L, "ORIGIN-521", 1, "KIE", "referring_provider_first_name", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(16L, "ORIGIN-521", 1, "KIE", "referring_provider_state", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(17L, "ORIGIN-521", 1, "KIE", "referring_provider_zipcode", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(18L, "ORIGIN-521", 1, "KIE", "referring_provider_address_line1", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(19L, "ORIGIN-521", 1, "KIE", "referring_provider_full_name", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(20L, "ORIGIN-521", 1, "KIE", "referring_provider_tin", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(21L, "ORIGIN-521", 1, "KIE", "referring_provider_npi", "", 0.50, 
                "REFERRING_PROVIDER_DETAILS", metadataJson, null, null, null, null));

        // SERVICING_FACILITY_DETAILS - Page 1
        predictions.add(createPredictionDTO(22L, "ORIGIN-521", 1, "KIE", "servicing_facility_full_name", "", 0.50, 
                "SERVICING_FACILITY_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(23L, "ORIGIN-521", 1, "KIE", "servicing_facility_first_name", "", 0.50, 
                "SERVICING_FACILITY_DETAILS", metadataJson, null, null, null, null));

        // AUTH_DISCHARGE_DATE - Page 1
        predictions.add(createPredictionDTO(24L, "ORIGIN-521", 1, "KIE", "auth_discharge_date", "", 0.50, 
                "AUTH_DISCHARGE_DATE", metadataJson, null, null, null, null));

        // FAX_REPORT - Page 1
        predictions.add(createPredictionDTO(25L, "ORIGIN-521", 1, "KIE", "fax_report", "N", 0.50, 
                "FAX_REPORT", metadataJson, null, null, null, null));

        // NOTIFICATION_TYPE - Page 1
        predictions.add(createPredictionDTO(26L, "ORIGIN-521", 1, "KIE", "notification_type", "Notification of Admission", 0.92, 
                "NOTIFICATION_TYPE", metadataJson, 0.0, 0.0, 0.0, 0.0));

        // Page 2 Predictions - SERVICING_PROVIDER_DETAILS
        predictions.add(createPredictionDTO(27L, "ORIGIN-521", 2, "KIE", "servicing_provider_city", "KINGWOOD", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(28L, "ORIGIN-521", 2, "KIE", "servicing_provider_first_name", "JAMES T.", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(29L, "ORIGIN-521", 2, "KIE", "servicing_provider_zipcode", "77338", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(30L, "ORIGIN-521", 2, "KIE", "servicing_provider_last_name", "CARSON", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(31L, "ORIGIN-521", 2, "KIE", "servicing_provider_npi", "1780978775", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(32L, "ORIGIN-521", 2, "KIE", "servicing_provider_state", "TX", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(33L, "ORIGIN-521", 2, "KIE", "servicing_provider_address_line1", "1485 FM 1960 BYPASS RD E SUITE 260 KINGWOOD TX", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(34L, "ORIGIN-521", 2, "KIE", "servicing_provider_full_name", "CARSON, JAMES T.", 1.0, 
                "SERVICING_PROVIDER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));

        // Page 2 - SERVICE_CODE
        predictions.add(createPredictionDTO(35L, "ORIGIN-521", 2, "KIE", "service_code", "11921,11920,33786", 0.97, 
                "SERVICE_CODE", metadataJson, 0.0, 0.0, 0.0, 0.0));

        // Page 2 - DIAGNOSIS_CODE
        predictions.add(createPredictionDTO(36L, "ORIGIN-521", 2, "KIE", "diagnosis_code", "M16.12", 0.93, 
                "DIAGNOSIS_CODE", metadataJson, 0.0, 0.0, 0.0, 0.0));

        // Page 2 - SERVICE_FROM_DATE
        predictions.add(createPredictionDTO(37L, "ORIGIN-521", 2, "KIE", "service_from_date", "2025-06-10", 0.92, 
                "SERVICE_FROM_DATE", metadataJson, 888.0, 2630.0, 1040.0, 2681.0));

        // Page 2 - MEMBER_DETAILS
        predictions.add(createPredictionDTO(38L, "ORIGIN-521", 2, "KIE", "member_date_of_birth", "0954-08-23", 0.98, 
                "MEMBER_DETAILS", metadataJson, 507.0, 0.0, 634.0, 67.0));
        predictions.add(createPredictionDTO(39L, "ORIGIN-521", 2, "KIE", "member_id", "354A50426", 0.95, 
                "MEMBER_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(40L, "ORIGIN-521", 2, "KIE", "member_address_line1", "71 c Florence Junction", 0.96, 
                "MEMBER_DETAILS", metadataJson, 1014.0, 0.0, 1217.0, 67.0));
        predictions.add(createPredictionDTO(41L, "ORIGIN-521", 2, "KIE", "member_state", "TX", 0.99, 
                "MEMBER_DETAILS", metadataJson, 1420.0, 0.0, 1471.0, 67.0));
        predictions.add(createPredictionDTO(42L, "ORIGIN-521", 2, "KIE", "member_city", "Plano", 0.99, 
                "MEMBER_DETAILS", metadataJson, 1268.0, 0.0, 1395.0, 67.0));
        predictions.add(createPredictionDTO(43L, "ORIGIN-521", 2, "KIE", "member_zipcode", "75074", 0.99, 
                "MEMBER_DETAILS", metadataJson, 1496.0, 0.0, 1598.0, 67.0));
        predictions.add(createPredictionDTO(44L, "ORIGIN-521", 2, "KIE", "member_first_name", "Lamwmond", 0.50, 
                "MEMBER_DETAILS", metadataJson, null, null, null, null));
        predictions.add(createPredictionDTO(45L, "ORIGIN-521", 2, "KIE", "member_full_name", "Lamwmond Chgaffen", 0.96, 
                "MEMBER_DETAILS", metadataJson, 0.0, 0.0, 254.0, 67.0));
        predictions.add(createPredictionDTO(46L, "ORIGIN-521", 2, "KIE", "member_gender", "M", 0.94, 
                "MEMBER_DETAILS", metadataJson, 761.0, 0.0, 888.0, 67.0));
        predictions.add(createPredictionDTO(47L, "ORIGIN-521", 2, "KIE", "member_last_name", "Chgaffen", 0.50, 
                "MEMBER_DETAILS", metadataJson, null, null, null, null));

        // Page 2 - AUTH_ID
        predictions.add(createPredictionDTO(48L, "ORIGIN-521", 2, "KIE", "auth_id", "UM53647888", 0.98, 
                "AUTH_ID", metadataJson, 1141.0, 1281.0, 1395.0, 1349.0));

        // Page 2 - LEVEL_OF_SERVICE
        predictions.add(createPredictionDTO(49L, "ORIGIN-521", 2, "KIE", "level_of_service", "Urgent", 0.98, 
                "LEVEL_OF_SERVICE", metadataJson, 0.0, 0.0, 0.0, 0.0));

        // Page 2 - SERVICING_FACILITY_DETAILS
        predictions.add(createPredictionDTO(50L, "ORIGIN-521", 2, "KIE", "servicing_facility_last_name", "HCA HOUSTON KINGWOOD", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(51L, "ORIGIN-521", 2, "KIE", "servicing_facility_state", "TX", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(52L, "ORIGIN-521", 2, "KIE", "servicing_facility_npi", "1811942238", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(53L, "ORIGIN-521", 2, "KIE", "servicing_facility_city", "KINGWOOD", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(54L, "ORIGIN-521", 2, "KIE", "servicing_facility_tin", "621619857", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(55L, "ORIGIN-521", 2, "KIE", "servicing_facility_address_line1", "22999 U.S.HIGHWAY 59 N.,KINGWOOD,TX", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));
        predictions.add(createPredictionDTO(56L, "ORIGIN-521", 2, "KIE", "servicing_facility_zipcode", "77339", 1.0, 
                "SERVICING_FACILITY_DETAILS", metadataJson, 0.0, 0.0, 0.0, 0.0));

        return predictions;
    }

    /**
     * Helper method to create PredictionDTO with common fields
     */
    private PredictionDTO createPredictionDTO(Long predictionId, String originId, Integer paperNo, String feature,
                                               String sorItemName, String predictedValue, Double precision,
                                               String containerName, String metadataJson,
                                               Double leftPos, Double rightPos, Double upperPos, Double lowerPos) {
        return PredictionDTO.builder()
                .predictionId(predictionId)
                .originId(originId)
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TRZ-8425")
                .feature(feature)
                .paperNo(paperNo)
                .lineItemType("single_value")
                .sorItemName(sorItemName)
                .predictedValue(predictedValue)
                .precision(precision)
                .containerName(containerName)
                .leftPos(leftPos)
                .rightPos(rightPos)
                .upperPos(upperPos)
                .lowerPos(lowerPos)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build();
    }

    /**
     * Creates metadata JSON string matching the real-world structure
     */
    private String createRealWorldMetadataJson() {
        try {
            MetadataContext metadataContext = MetadataContext.builder()
                    .requestTxnId("REQ-TXN-001")
                    .uploadStatus("COMPLETED")
                    .errorMessage(null)
                    .errorMessageDetail(null)
                    .errorCode(null)
                    .documentId("MCR_P3_Notice_Of_Admission")
                    .inboundTransactionId("INBOUND-TXN-001")
                    .transactionId("TRZ-8425")
                    .documentType("Medical")
                    .inboundDocumentName("MCR_P3_Notice_Of_Admission.pdf")
                    .documentExtension("pdf")
                    .processStartTime("2025-11-10T06:28:00.122773")
                    .processEndTime("2025-11-10T06:35:41.922025")
                    .processedAt("2025-11-10T06:35:41.922025")
                    .candidatePapers(Arrays.asList(1, 2, 3))
                    .build();

            ObjectMapper objectMapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build();
            return objectMapper.writeValueAsString(metadataContext);
        } catch (JsonProcessingException e) {
            log.error("Failed to create metadata JSON", e);
            return "{}";
        }
    }

    @Test
    void jsonNodeTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JSONObject parentResponse = new JSONObject("{\"csvTablesPath\": [{\"rcnn_padd.cm1882524_0_0\": \"/home/logesh.b@zucisystems.com/workspace/dev/intics-agadia/pipeline/data/output/2/table_extraction/1392/INT-3/139147003665780118/tabel-extraction/CM1882524/rcnn_padd/CM1882524_0_0.csv\"}], \"tableResponse\": {\"payload\": [{\"encode\": \"\", \"tableData\": {\"columns\": [0, 1, 2, 3, 4, 5], \"data\": [[\"ITEMNUMBER\", \"DESCRIPTION\", \"QTY\", \"U/M\", \"UNITPRICE\", \"EXTPRICE\"], [\"SAN1735790\", \"MARKER,SHARPIE,UF,RT,BK refused. no paperwork.\", \"-1\", \"DZ\", \"8.65\", \"-8.65\"]]}}]}}");
        JSONArray filePathArray = new JSONArray(parentResponse.get("csvTablesPath").toString());
        JsonNode jsonNode = mapper.readTree(parentResponse.toString());
        JsonNode tableResponse = jsonNode.get("tableResponse").get("payload").get(0);
        System.out.println(filePathArray);
        System.out.println(tableResponse);

    }

    @Test
    void tableData() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree("{\"payload\":[{\"encode\":\"\",\"tableData\":{\"data\":[[\"ITEMNUMBER\",\"DESCRIPTION\",\"QTY\",\"U/M\",\"UNITPRICE\",\"EXTPRICE\"],[\"SAN1735790\",\"MARKER,SHARPIE,UF,RT,BK refused. no paperwork.\",\"-1\",\"DZ\",\"8.65\",\"-8.65\"]],\"columns\":[0,1,2,3,4,5]}}]}");
        System.out.println(mapper.writeValueAsString(jsonNode.get("payload").get(0).get("tableData")));
    }

    @Test
    @DisplayName("Test JSON generation with various PredictionDTO features")
    void testBuildProductResponseJson() throws Exception {
        // Create ObjectMapper
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        // Create test PredictionDTO list with various features
        List<PredictionDTO> predictionDTOList = createTestPredictionDTOList();

        // Create ProductResponseAction instance
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        ProductResponse productResponse = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .name("test product response")
                .build();
        ProductResponseAction productResponseAction = new ProductResponseAction(actionExecutionAudit, log, productResponse);

        // Use reflection to access private method
        Method buildProductResponseJsonMethod = ProductResponseAction.class.getDeclaredMethod(
                "buildProductResponseJson",
                List.class, String.class, Long.class, Long.class, String.class, String.class, String.class, String.class, ObjectMapper.class
        );
        buildProductResponseJsonMethod.setAccessible(true);

        // Call the method
        String originId = "TEST-ORIGIN-001";
        Long tenantId = 1L;
        Long groupId = 100L;
        String batchId = "BATCH-001";
        String rootPipelineId = "ROOT-001";
        String transactionId = "TXN-001";
        String metadataJson = "{\"documentId\":\"DOC-001\",\"inboundTransactionId\":\"INBOUND-TXN-001\"}";

        JsonNode result = (JsonNode) buildProductResponseJsonMethod.invoke(
                productResponseAction,
                predictionDTOList,
                originId,
                tenantId,
                groupId,
                batchId,
                rootPipelineId,
                transactionId,
                metadataJson,
                objectMapper
        );

        // Log the generated JSON
        try {
            String generatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            log.info("Generated JSON for testBuildProductResponseJson:\n{}", generatedJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON for logging", e);
        }

        // Verify basic document info
        assertNotNull(result, "Result should not be null");
        assertEquals(originId, result.get("originId").asText(), "OriginId should match");
        
        // Verify documentId if metadata was provided
        if (result.has("documentId") && !result.get("documentId").isNull()) {
            assertEquals("DOC-001", result.get("documentId").asText(), "DocumentId from metadata should match");
        }
        
        // Verify metadata fields are present (may be empty strings if not in metadata)
        assertTrue(result.has("extension"), "Should have extension field");
        assertTrue(result.has("totalProcessedDuration"), "Should have totalProcessedDuration field");
        assertTrue(result.has("sourceFileURI"), "Should have sourceFileURI field");
        assertTrue(result.has("preprocessedFileURI"), "Should have preprocessedFileURI field");

        // Verify totalPages
        assertTrue(result.has("totalPages"), "Should have totalPages field");
        assertTrue(result.get("totalPages").asInt() > 0, "TotalPages should be greater than 0");

        // Verify paperInfo array
        assertTrue(result.has("paperInfo"), "Should have paperInfo array");
        ArrayNode paperInfoArray = (ArrayNode) result.get("paperInfo");
        assertNotNull(paperInfoArray, "PaperInfo array should not be null");
        assertTrue(paperInfoArray.size() > 0, "PaperInfo array should have entries");

        // Verify first paperInfo structure (matching provided JSON structure)
        ObjectNode firstPaperInfo = (ObjectNode) paperInfoArray.get(0);
        assertTrue(firstPaperInfo.has("pageNo"), "PaperInfo should have pageNo");
        assertTrue(firstPaperInfo.has("paperType"), "PaperInfo should have paperType");
        assertTrue(firstPaperInfo.has("featureInfo"), "PaperInfo should have featureInfo");

        // Verify featureInfo structure
        ObjectNode featureInfo = (ObjectNode) firstPaperInfo.get("featureInfo");
        assertNotNull(featureInfo, "FeatureInfo should not be null");

        // Verify KIE feature
        if (featureInfo.has("kie")) {
            ObjectNode kie = (ObjectNode) featureInfo.get("kie");
            assertTrue(kie.has("entityDetails"), "KIE should have entityDetails");
            ObjectNode entityDetails = (ObjectNode) kie.get("entityDetails");
            assertTrue(entityDetails.has("sorContainerDetails"), "EntityDetails should have sorContainerDetails");
        }

        // Verify Checkbox feature
        if (featureInfo.has("checkbox")) {
            ObjectNode checkbox = (ObjectNode) featureInfo.get("checkbox");
            assertTrue(checkbox.has("checkboxItems"), "Checkbox should have checkboxItems");
            ArrayNode checkboxItems = (ArrayNode) checkbox.get("checkboxItems");
            assertTrue(checkboxItems.size() > 0, "CheckboxItems should have entries");
        }

        // Verify Table feature
        if (featureInfo.has("table")) {
            ObjectNode table = (ObjectNode) featureInfo.get("table");
            assertTrue(table.has("tableData"), "Table should have tableData");
        }

        // Verify Currency feature
        if (featureInfo.has("currency")) {
            ArrayNode currency = (ArrayNode) featureInfo.get("currency");
            assertTrue(currency.size() > 0, "Currency array should have entries");
        }

        // Verify Bulletin feature
        if (featureInfo.has("bulletin")) {
            ArrayNode bulletin = (ArrayNode) featureInfo.get("bulletin");
            assertTrue(bulletin.size() > 0, "Bulletin array should have entries");
        }

        // Verify Paragraph feature
        if (featureInfo.has("paragraph")) {
            ArrayNode paragraph = (ArrayNode) featureInfo.get("paragraph");
            assertTrue(paragraph.size() > 0, "Paragraph array should have entries");
        }

        // Verify summary sections
        assertTrue(result.has("kvpSummary"), "Should have kvpSummary");
        assertTrue(result.has("currencySummary"), "Should have currencySummary");
        assertTrue(result.has("bulletinSummary"), "Should have bulletinSummary");
        assertTrue(result.has("paragraphSummary"), "Should have paragraphSummary");

        // Print the generated JSON for debugging
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        log.info("Generated Product Response JSON:\n{}", jsonString);
    }

    /**
     * Creates a comprehensive list of PredictionDTO objects with various features for testing
     */
    private List<PredictionDTO> createTestPredictionDTOList() {
        List<PredictionDTO> predictions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // KIE Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(1L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .containerName("Member Information")
                .sorItemName("member_id")
                .predictedValue("MEMBER-12345")
                .precision(0.95)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(50.0)
                .lowerPos(80.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(2L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .containerName("Member Information")
                .sorItemName("member_name")
                .predictedValue("John Doe")
                .precision(0.92)
                .leftPos(100.0)
                .rightPos(250.0)
                .upperPos(100.0)
                .lowerPos(130.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(3L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .containerName("Authorization Details")
                .sorItemName("auth_id")
                .predictedValue("AUTH-789")
                .precision(0.88)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(200.0)
                .lowerPos(230.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .build());

        // Checkbox Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(4L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("CHECKBOX_EXTRACTION")
                .paperNo(1)
                .predictedValue("Yes")
                .precision(0.90)
                .state("CHECKED")
                .leftPos(300.0)
                .rightPos(320.0)
                .upperPos(150.0)
                .lowerPos(170.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .build());

        // Table Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(5L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("TABLE_EXTRACT")
                .paperNo(1)
                .csvFilePath("/path/to/table1.csv")
                .tableData("{\"columnHeaders\":[\"Item\",\"Quantity\",\"Price\"],\"data\":[[\"Item1\",\"10\",\"$100\"],[\"Item2\",\"20\",\"$200\"]]}")
                .build());

        // Currency Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(6L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("CURRENCY_DETECTION")
                .paperNo(1)
                .currencyValue("$1,234.56")
                .currencyAsciiValue("1234.56")
                .precision(0.95)
                .build());

        // Bulletin Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(7L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("BULLETIN_EXTRACTION")
                .paperNo(1)
                .bulletinSection("Important Notice")
                .bulletinPoints("Point 1: Important information\nPoint 2: Additional details")
                .build());

        // Paragraph Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(8L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("PARAGRAPH_EXTRACTION")
                .paperNo(1)
                .sorItemName("policy_details")
                .paragraphSection("Policy Information")
                .paragraphPoints("This is a detailed paragraph about policy information.")
                .build());

        // Table Aggregate Feature Predictions - Page 1
        predictions.add(PredictionDTO.builder()
                .predictionId(9L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("TABLE_EXTRACT_AGGREGATE")
                .paperNo(1)
                .sorItemName("total_amount")
                .containerName("Financial Summary")
                .aggregatedJson("{\"total\":\"$3,000.00\",\"count\":3}")
                .build());

        // Page 2 Predictions
        predictions.add(PredictionDTO.builder()
                .predictionId(10L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(2)
                .containerName("Provider Information")
                .sorItemName("provider_name")
                .predictedValue("Dr. Smith")
                .precision(0.93)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(50.0)
                .lowerPos(80.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(11L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("CURRENCY_DETECTION")
                .paperNo(2)
                .currencyValue("$500.00")
                .currencyAsciiValue("500.00")
                .precision(0.91)
                .build());

        return predictions;
    }
}