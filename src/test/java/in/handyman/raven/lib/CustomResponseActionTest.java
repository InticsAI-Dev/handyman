package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.mapper.MedicalPayloadGeneration;
import in.handyman.raven.lib.custom.outbound.model.MedicalOutboundResponse;
import in.handyman.raven.lib.model.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CustomResponseActionTest {

    private ObjectMapper objectMapper;
    private ActionExecutionAudit actionExecutionAudit;
    private CustomResponse customResponse;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("CONFIDENCE_SCORE_MULTIPLY_VARIABLE", "100");
        actionExecutionAudit.getContext().put("AUMI_BBOX_SCALAR_WIDTH", "1000");
        actionExecutionAudit.getContext().put("AUMI_BBOX_SCALAR_HEIGHT", "1000");
        actionExecutionAudit.getContext().put("FLOAT_VALUE_ROUNDING_PRECISION", "2");
        actionExecutionAudit.getContext().put("aumi.reorder.paper.number", "false");
        actionExecutionAudit.getContext().put("CUSTOM_MEDICAL_OUTBOUND_CLEANER", "false");
        actionExecutionAudit.getContext().put("NEWBORN_REQUEST_MEMBER_ENABLER", "false");

        customResponse = CustomResponse.builder()
                .tenantId("1")
                .condition(true)
                .name("test custom response")
                .resultTable("test_custom_response_table")
                .resourceConn("test_connection")
                .querySet("SELECT * FROM test_table")
                .build();
    }

    @Test
    @DisplayName("Test MedicalOutboundResponse generation with PredictionDTO list")
    void testBuildMedicalOutboundResponse() throws Exception {
        // Create test PredictionDTO list
        List<PredictionDTO> predictionDTOList = createMedicalPredictionDTOList();

        // Create metadata JSON
        String metadataJson = createMetadataJson();

        // Create MedicalPayloadGeneration instance
        MedicalPayloadGeneration medicalPayloadGeneration = new MedicalPayloadGeneration(log);

        // Build MedicalOutboundResponse
        MedicalOutboundResponse medicalOutboundResponse = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictionDTOList,
                actionExecutionAudit.getContext(),
                metadataJson
        );

        // Verify response structure
        assertNotNull(medicalOutboundResponse, "MedicalOutboundResponse should not be null");
        assertEquals("REQ-TXN-001", medicalOutboundResponse.getRequestTxnId(), "RequestTxnId should match");
        assertEquals("COMPLETED", medicalOutboundResponse.getStatus(), "Status should match");
        assertEquals("DOC-001", medicalOutboundResponse.getDocumentId(), "DocumentId should match");
        assertEquals("INBOUND-TXN-001", medicalOutboundResponse.getInboundTransactionId(), "InboundTransactionId should match");

        // Verify metadata
        assertNotNull(medicalOutboundResponse.getMetadata(), "Metadata should not be null");
        assertNotNull(medicalOutboundResponse.getMetadata().getTransactionId(), "TransactionId should not be null");
        assertNotNull(medicalOutboundResponse.getMetadata().getDocumentType(), "DocumentType should not be null");

        // Verify payload
        assertNotNull(medicalOutboundResponse.getAumipayload(), "AUMI payload should not be null");

        // Serialize to JSON and verify
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(medicalOutboundResponse);
        assertNotNull(jsonString, "JSON string should not be null");
        assertFalse(jsonString.isEmpty(), "JSON string should not be empty");

        JsonNode jsonNode = objectMapper.readTree(jsonString);
        assertTrue(jsonNode.has("requestTxnId"), "Should have requestTxnId");
        assertTrue(jsonNode.has("status"), "Should have status");
        assertTrue(jsonNode.has("documentId"), "Should have documentId");
        assertTrue(jsonNode.has("inboundTransactionId"), "Should have inboundTransactionId");
        assertTrue(jsonNode.has("metadata"), "Should have metadata");
        assertTrue(jsonNode.has("aumipayload"), "Should have aumipayload");

        log.info("Generated MedicalOutboundResponse JSON:\n{}", jsonString);
    }

    @Test
    @DisplayName("Test CustomResponseAction with mock database and PredictionDTO list")
    void testCustomResponseActionExecute() throws Exception {
        // Create test PredictionDTO list
        List<PredictionDTO> predictionDTOList = createMedicalPredictionDTOList();

        // Create CustomResponseAction instance
        CustomResponseAction customResponseAction = new CustomResponseAction(actionExecutionAudit, log, customResponse);

        // Verify executeIf returns true when condition is true
        assertTrue(customResponseAction.executeIf(), "executeIf should return true when condition is true");

        // Test with condition false
        customResponse.setCondition(false);
        CustomResponseAction customResponseActionFalse = new CustomResponseAction(actionExecutionAudit, log, customResponse);
        assertFalse(customResponseActionFalse.executeIf(), "executeIf should return false when condition is false");
    }

    @Test
    @DisplayName("Test MedicalOutboundResponse JSON serialization")
    void testMedicalOutboundResponseSerialization() throws Exception {
        // Create test PredictionDTO list
        List<PredictionDTO> predictionDTOList = createMedicalPredictionDTOList();
        String metadataJson = createMetadataJson();

        MedicalPayloadGeneration medicalPayloadGeneration = new MedicalPayloadGeneration(log);
        MedicalOutboundResponse medicalOutboundResponse = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictionDTOList,
                actionExecutionAudit.getContext(),
                metadataJson
        );

        // Serialize to JSON
        String jsonString = objectMapper.writeValueAsString(medicalOutboundResponse);
        assertNotNull(jsonString, "JSON string should not be null");

        // Deserialize back and verify
        MedicalOutboundResponse deserialized = objectMapper.readValue(jsonString, MedicalOutboundResponse.class);
        assertNotNull(deserialized, "Deserialized response should not be null");
        assertEquals(medicalOutboundResponse.getRequestTxnId(), deserialized.getRequestTxnId(), "RequestTxnId should match after deserialization");
        assertEquals(medicalOutboundResponse.getStatus(), deserialized.getStatus(), "Status should match after deserialization");
        assertEquals(medicalOutboundResponse.getDocumentId(), deserialized.getDocumentId(), "DocumentId should match after deserialization");
    }

    @Test
    @DisplayName("Test with empty PredictionDTO list")
    void testWithEmptyPredictionDTOList() throws Exception {
        List<PredictionDTO> emptyList = new ArrayList<>();
        String metadataJson = createMetadataJson();

        MedicalPayloadGeneration medicalPayloadGeneration = new MedicalPayloadGeneration(log);
        MedicalOutboundResponse medicalOutboundResponse = medicalPayloadGeneration.buildMedicalOutboundResponse(
                emptyList,
                actionExecutionAudit.getContext(),
                metadataJson
        );

        assertNotNull(medicalOutboundResponse, "Response should not be null even with empty list");
        assertNotNull(medicalOutboundResponse.getAumipayload(), "Payload should not be null");
    }

    @Test
    @DisplayName("Test with multiple originIds grouping")
    void testMultipleOriginIdsGrouping() throws Exception {
        List<PredictionDTO> predictionDTOList = createMedicalPredictionDTOList();

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
                .metadataJson(createMetadataJson())
                .build();

        predictionDTOList.add(differentOrigin);

        // Group by originId
        Map<String, List<PredictionDTO>> groupedByOriginId = predictionDTOList.stream()
                .collect(java.util.stream.Collectors.groupingBy(PredictionDTO::getOriginId));

        assertEquals(2, groupedByOriginId.size(), "Should have 2 originIds");
        assertTrue(groupedByOriginId.containsKey("TEST-ORIGIN-001"), "Should contain TEST-ORIGIN-001");
        assertTrue(groupedByOriginId.containsKey("TEST-ORIGIN-002"), "Should contain TEST-ORIGIN-002");
    }

    @Test
    @DisplayName("Test CustomResponseOutputTable builder")
    void testCustomResponseOutputTableBuilder() {
        LocalDateTime now = LocalDateTime.now();
        CustomResponseAction.CustomResponseOutputTable outputTable = CustomResponseAction.CustomResponseOutputTable.builder()
                .processId(123)
                .groupId(100L)
                .customResponse("{\"test\":\"response\"}")
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .rootPipelineId(456L)
                .status("COMPLETED")
                .stage("Product Response Generation")
                .message("Product Response generated successfully")
                .triggeredUrl("")
                .feature("Product")
                .batchId("BATCH-001")
                .inboundTransactionId("INBOUND-TXN-001")
                .createdOn(now)
                .lastUpdatedOn(now)
                .build();

        assertNotNull(outputTable, "OutputTable should not be null");
        assertEquals(123, outputTable.getProcessId(), "ProcessId should match");
        assertEquals(100L, outputTable.getGroupId(), "GroupId should match");
        assertEquals("TEST-ORIGIN-001", outputTable.getOriginId(), "OriginId should match");
        assertEquals("COMPLETED", outputTable.getStatus(), "Status should match");
        assertEquals("INBOUND-TXN-001", outputTable.getInboundTransactionId(), "InboundTransactionId should match");
    }

    /**
     * Creates a comprehensive list of PredictionDTO objects for medical payload testing
     */
    private List<PredictionDTO> createMedicalPredictionDTOList() {
        List<PredictionDTO> predictions = new ArrayList<>();
        String metadataJson = createMetadataJson();

        // Member Information - Single Value
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
                .lineItemType("single_value")
                .sorItemName("member_id")
                .predictedValue("MEMBER-12345")
                .precision(0.95)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(50.0)
                .lowerPos(80.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
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
                .lineItemType("single_value")
                .sorItemName("member_first_name")
                .predictedValue("John")
                .precision(0.92)
                .leftPos(100.0)
                .rightPos(150.0)
                .upperPos(100.0)
                .lowerPos(130.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
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
                .lineItemType("single_value")
                .sorItemName("member_last_name")
                .predictedValue("Doe")
                .precision(0.93)
                .leftPos(160.0)
                .rightPos(200.0)
                .upperPos(100.0)
                .lowerPos(130.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        // Authorization Information
        predictions.add(PredictionDTO.builder()
                .predictionId(4L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("single_value")
                .sorItemName("auth_id")
                .predictedValue("AUTH-789")
                .precision(0.88)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(200.0)
                .lowerPos(230.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(5L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("single_value")
                .sorItemName("service_from_date")
                .predictedValue("2024-01-01")
                .precision(0.90)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(250.0)
                .lowerPos(280.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        // Multi-value - Service Modifiers
        predictions.add(PredictionDTO.builder()
                .predictionId(6L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("multi_value")
                .sorItemName("service_code")
                .predictedValue("99213")
                .precision(0.85)
                .sorContainerInstance("1")
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(300.0)
                .lowerPos(330.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(7L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("multi_value")
                .sorItemName("service_code_modifier")
                .predictedValue("25")
                .precision(0.87)
                .sorContainerInstance("1")
                .leftPos(210.0)
                .rightPos(250.0)
                .upperPos(300.0)
                .lowerPos(330.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        // Multi-value - Diagnosis
        predictions.add(PredictionDTO.builder()
                .predictionId(8L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("multi_value")
                .sorItemName("diagnosis_code")
                .predictedValue("E11.9")
                .precision(0.91)
                .sorContainerInstance("2")
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(350.0)
                .lowerPos(380.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(9L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("multi_value")
                .sorItemName("diagnosis_description")
                .predictedValue("Type 2 diabetes mellitus without complications")
                .precision(0.89)
                .sorContainerInstance("2")
                .leftPos(210.0)
                .rightPos(500.0)
                .upperPos(350.0)
                .lowerPos(380.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        // Provider Information
        predictions.add(PredictionDTO.builder()
                .predictionId(10L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("single_value")
                .sorItemName("servicing_provider_first_name")
                .predictedValue("Dr. Jane")
                .precision(0.94)
                .leftPos(100.0)
                .rightPos(200.0)
                .upperPos(400.0)
                .lowerPos(430.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        predictions.add(PredictionDTO.builder()
                .predictionId(11L)
                .originId("TEST-ORIGIN-001")
                .tenantId(1L)
                .groupId(100L)
                .batchId("BATCH-001")
                .rootPipelineId("ROOT-001")
                .transactionId("TXN-001")
                .feature("KIE")
                .paperNo(1)
                .lineItemType("single_value")
                .sorItemName("servicing_provider_last_name")
                .predictedValue("Smith")
                .precision(0.92)
                .leftPos(210.0)
                .rightPos(300.0)
                .upperPos(400.0)
                .lowerPos(430.0)
                .imageWidth(1000)
                .imageHeight(1000)
                .isMultiEntityEnabled(false)
                .metadataJson(metadataJson)
                .build());

        return predictions;
    }

    /**
     * Creates metadata JSON string for testing
     */
    private String createMetadataJson() {
        try {
            MetadataContext metadataContext = MetadataContext.builder()
                    .requestTxnId("REQ-TXN-001")
                    .uploadStatus("COMPLETED")
                    .errorMessage(null)
                    .errorMessageDetail(null)
                    .errorCode(null)
                    .documentId("DOC-001")
                    .inboundTransactionId("INBOUND-TXN-001")
                    .transactionId("TXN-001")
                    .documentType("Medical")
                    .inboundDocumentName("test_document.pdf")
                    .documentExtension("pdf")
                    .processStartTime(LocalDateTime.now().toString())
                    .processEndTime(LocalDateTime.now().toString())
                    .processedAt(LocalDateTime.now().toString())
                    .candidatePapers(Arrays.asList(1))
                    .build();

            return objectMapper.writeValueAsString(metadataContext);
        } catch (JsonProcessingException e) {
            log.error("Failed to create metadata JSON", e);
            return "{}";
        }
    }
}
