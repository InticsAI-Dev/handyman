package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.CustomResponseOutputTable;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomResponseGenerationConsumerProcessorTest {

    private final Logger log = LoggerFactory.getLogger(CustomResponseGenerationConsumerProcessorTest.class);
    private final Marker marker = MarkerFactory.getMarker("Test");
    private static final String INPUT_QUERY = "SELECT p.prediction_id, p.created_on, p.created_user_id, p.last_updated_on, " +
            "p.last_updated_user_id, p.status, p.version, p.encode, p.feature, p.label, " +
            "p.left_pos, p.lower_pos, p.origin_id, p.precision, p.predicted_value, " +
            "p.question_id, p.right_pos, p.root_pipeline_id, p.state, p.synonym_id, " +
            "p.table_data, p.tenant_id, p.transaction_id, p.upper_pos, p.workspace_id, " +
            "p.truth_id, p.channel_id, p.csv_file_path, p.sor_container_id, p.truth_entity_id, " +
            "p.currency_ascii_value, p.currency_value, p.aggregated_json, p.bulletin_points, " +
            "p.bulletin_section, p.paragraph_points, p.paragraph_section, p.sor_item_id, " +
            "si.line_item_type, si.sor_item_name, sc.sor_container_name AS container_name, " +
            "sot.paper_no, a.width as image_width, a.height as image_height, " +
            "m.meta_data AS metadata_json, soo.group_id, soo.batch_id ...";

    @Test
    void process_shouldMapTemplateWithPredictionDtoInputFromQueryShape() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        action.getContext().put("custom.response.generation.test.input.query", INPUT_QUERY);
        String template = "{\n" +
                "  \"root\": {\n" +
                "    \"requestTxnId\": \"\",\n" +
                "    \"status\": \"\",\n" +
                "    \"errorMessage\": null,\n" +
                "    \"errorMessageDetail\": null,\n" +
                "    \"errorCd\": null,\n" +
                "    \"documentId\": \"\",\n" +
                "    \"inboundTransactionId\": \"\",\n" +
                "    \"metadata\": {\n" +
                "      \"documentType\": \"\",\n" +
                "      \"documentExtension\": \"\",\n" +
                "      \"transactionId\": \"\",\n" +
                "      \"inboundDocumentName\": \"\",\n" +
                "      \"processStartTime\": \"\",\n" +
                "      \"processEndTime\": \"\",\n" +
                "      \"processingTimeMs\": 0,\n" +
                "      \"processedAt\": \"\",\n" +
                "      \"pageCount\": 0,\n" +
                "      \"candidatePaper\": [],\n" +
                "      \"overallConfidence\": 0\n" +
                "    },\n" +
                "    \"aumipayload\": {\n" +
                "      \"memberFirstName\": {\"value\": \"${member_first_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"provider\": [\n" +
                "        {\n" +
                "          \"providerNPI\": {\"value\": \"\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}}\n" +
                "        }\n" +
                "      ],\n" +
                "      \"memberAdditionalProperties\": [\n" +
                "        {\n" +
                "          \"propName\": {\"value\": \"MEMBER_INDICATOR\"},\n" +
                "          \"propValue\": \"\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": {\"value\": \"NEWBORN_REQUEST\"},\n" +
                "          \"propValue\": \"\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        action.getContext().put("custom.json.generation.structure", template);

        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        PredictionDTO p1 = PredictionDTO.builder()
                .originId("ORIGIN-1")
                .sorItemName("member_first_name")
                .predictedValue("Noichole")
                .paperNo(2)
                .precision(0.5)
                .leftPos(33.82)
                .rightPos(451.5)
                .upperPos(1669.4)
                .lowerPos(1786.87)
                .groupId(10L)
                .tenantId(1L)
                .batchId("BATCH-1")
                .rootPipelineId("100")
                .transactionId("TRZ-1")
                .metadataJson("{\"requestTxnId\":\"REQ-1\",\"documentId\":\"DOC-1\",\"inboundTransactionId\":\"INB-1\",\"transactionId\":\"TRZ-1\",\"inboundDocumentName\":\"DOC-NAME\",\"documentExtension\":\"pdf\",\"documentType\":\"MEDICAL_GBD\",\"processStartTime\":\"2026-04-03T02:13:06.478281\",\"processEndTime\":\"2026-04-03T02:14:16.110159\",\"processedAt\":\"2026-04-03T02:13:06.478281\",\"candidatePapers\":[1,2,3]}")
                .build();

        CustomResponseGenerationConsumerProcessor.CustomResponseGenerationInput input =
                CustomResponseGenerationConsumerProcessor.CustomResponseGenerationInput.builder()
                        .originId("ORIGIN-1")
                        .predictions(List.of(p1))
                        .build();
        List<CustomResponseOutputTable> output = consumer.process(new URL("http://localhost"), input);
        assertNotNull(output);
        assertEquals(1, output.size());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(output.get(0).getCustomResponse());
        JsonNode root = json.get("root");
        assertNotNull(root);
        assertEquals("TRZ-1", root.get("requestTxnId").asText());
        assertEquals("SUCCESS", root.get("status").asText());
        assertEquals("ORIGIN-1", root.get("documentId").asText());
        assertEquals("INB-1", root.get("inboundTransactionId").asText());
        assertEquals("MEDICAL_GBD", root.get("metadata").get("documentType").asText());
        assertEquals("pdf", root.get("metadata").get("documentExtension").asText());
        assertEquals("TRZ-1", root.get("metadata").get("transactionId").asText());
        assertEquals(3, root.get("metadata").get("candidatePaper").size());

        assertEquals("Noichole", root.get("aumipayload").get("memberFirstName").get("value").asText());
        assertEquals(2, root.get("aumipayload").get("memberFirstName").get("page").asInt());
        assertEquals(50, root.get("aumipayload").get("memberFirstName").get("confidence").asInt());
        assertEquals("INB-1", output.get(0).getInboundTransactionId());
    }

    @Test
    void generateCustomJson_shouldPrintFinalJsonForPredictionTableInputs() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{\n" +
                "  \"root\": {\n" +
                "    \"requestTxnId\": \"\",\n" +
                "    \"status\": \"\",\n" +
                "    \"errorMessage\": null,\n" +
                "    \"errorMessageDetail\": null,\n" +
                "    \"errorCd\": null,\n" +
                "    \"documentId\": \"\",\n" +
                "    \"inboundTransactionId\": \"\",\n" +
                "    \"metadata\": {\n" +
                "      \"documentType\": \"\",\n" +
                "      \"documentExtension\": \"\",\n" +
                "      \"transactionId\": \"\",\n" +
                "      \"inboundDocumentName\": \"\",\n" +
                "      \"processStartTime\": \"\",\n" +
                "      \"processEndTime\": \"\",\n" +
                "      \"processingTimeMs\": 0,\n" +
                "      \"processedAt\": \"\",\n" +
                "      \"pageCount\": 0,\n" +
                "      \"candidatePaper\": [],\n" +
                "      \"overallConfidence\": 0\n" +
                "    },\n" +
                "    \"aumipayload\": {\n" +
                "      \"memberLastName\": {\"value\": \"${member_last_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberFirstName\": {\"value\": \"${member_first_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberDOB\": {\"value\": \"${member_date_of_birth}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberGender\": {\"value\": \"${member_gender}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberAddressLine1\": {\"value\": \"${member_address_line1}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberCity\": {\"value\": \"${member_city}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberState\": {\"value\": \"${member_state}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"memberZipCode\": {\"value\": \"${member_zipcode}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"serviceFromDate\": {\"value\": \"${service_from_date}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"faxReceivedDate\": {\"value\": \"${fax_received_date}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\n" +
                "      \"diagnosis\": [{\"cd\": {\"value\": \"${diagnosis_code}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}}}],\n" +
                "      \"provider\": [{\"providerNPI\": {\"value\": \"${servicing_provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerFirstName\": {\"value\": \"${servicing_provider_first_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerLastName\": {\"value\": \"${servicing_provider_last_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerAddressLine1\": {\"value\": \"${servicing_provider_address_line1}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerCity\": {\"value\": \"${servicing_provider_city}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerState\": {\"value\": \"${servicing_provider_state}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}},\"providerZipCode\": {\"value\": \"${servicing_provider_zipcode}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}}}],\n" +
                "      \"memberAdditionalProperties\": [{\"propName\": {\"value\": \"NEWBORN_REQUEST\"}, \"propValue\": \"\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}}]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        action.getContext().put("custom.json.generation.structure", template);

        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);
        String metadata = "{\"requestTxnId\":\"1cf66141-c378-499a-9c72-3ad4d1028fa6\",\"documentId\":\"DUAL__P3_LA_NOA_CAID\",\"inboundTransactionId\":\"ITX-3279\",\"transactionId\":\"TRZ-758\",\"inboundDocumentName\":\"DUAL__P3_LA_NOA_CAID\",\"documentExtension\":\"pdf\",\"documentType\":\"MEDICAL_GBD\",\"uploadStatus\":\"COMPLETED\",\"processStartTime\":\"2026-04-08T16:11:06.904017\",\"processEndTime\":\"2026-04-08T16:11:51.4429\",\"processedAt\":\"2026-04-08T16:11:06.904017\",\"candidatePapers\":[1,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,1],\"errorMessage\":\"COMPLETED: Pipeline completed for the originId=ORIGIN-494\",\"errorMessageDetail\":\"\",\"errorCode\":\"200\"}";

        List<PredictionDTO> inputs = new ArrayList<>();
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_last_name").predictedValue("Exlsie").paperNo(2).precision(0.5).leftPos(20.0).rightPos(267.0).upperPos(739.0).lowerPos(791.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_first_name").predictedValue("Noichole").paperNo(2).precision(0.5).leftPos(20.0).rightPos(267.0).upperPos(739.0).lowerPos(791.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_date_of_birth").predictedValue("0943-12-21").paperNo(2).precision(0.0).leftPos(1163.0).rightPos(1282.0).upperPos(694.0).lowerPos(739.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_gender").predictedValue("M").paperNo(2).precision(0.0).leftPos(1505.0).rightPos(1573.0).upperPos(694.0).lowerPos(739.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_address_line1").predictedValue("89t121 De3 Mar Crossing,Pjano,...").paperNo(2).precision(0.0).leftPos(727.0).rightPos(964.0).upperPos(694.0).lowerPos(745.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_city").predictedValue("Pjano").paperNo(2).precision(0.0).leftPos(727.0).rightPos(846.0).upperPos(739.0).lowerPos(745.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_state").predictedValue("TX").paperNo(2).precision(0.0).leftPos(846.0).rightPos(896.0).upperPos(739.0).lowerPos(745.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("member_zipcode").predictedValue("75074").paperNo(2).precision(0.0).leftPos(896.0).rightPos(964.0).upperPos(739.0).lowerPos(745.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("service_from_date").predictedValue("2025-06-02").paperNo(2).precision(0.92).leftPos(470.0).rightPos(604.0).upperPos(1760.0).lowerPos(1807.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("diagnosis_code").predictedValue("F10250").paperNo(2).precision(0.95).leftPos(1485.0).rightPos(1635.0).upperPos(1789.0).lowerPos(1823.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_npi").predictedValue("1952628794").paperNo(2).precision(1.0).leftPos(643.0).rightPos(846.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_first_name").predictedValue("SAMANTHA B").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_last_name").predictedValue("ZERINGUE").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_address_line1").predictedValue("201 4TH STREET SUITE 5B").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_city").predictedValue("ALEXANDRIA").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_state").predictedValue("LA").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_zipcode").predictedValue("71301").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("fax_received_date").predictedValue("06-05-2025 17:19:30").paperNo(1).precision(0.95).leftPos(17.0).rightPos(372.0).upperPos(2152.0).lowerPos(2189.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("newborn_request").predictedValue("N").paperNo(1).precision(0.5).leftPos(0.0).rightPos(0.0).upperPos(0.0).lowerPos(0.0).transactionId("TRZ-758").metadataJson(metadata).build());

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        String finalGeneratedJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(finalJson);
        System.out.println("Final Generated JSON:\n" + finalGeneratedJson);

        assertNotNull(finalJson);
        JsonNode root = finalJson.get("root");
        assertNotNull(root);
        assertEquals("TRZ-758", root.get("requestTxnId").asText());
        assertEquals("Exlsie", root.get("aumipayload").get("memberLastName").get("value").asText());
        assertEquals("Noichole", root.get("aumipayload").get("memberFirstName").get("value").asText());
        assertEquals("1952628794", root.get("aumipayload").get("provider").get(0).get("providerNPI").get("value").asText());
        assertEquals("N", root.get("aumipayload").get("memberAdditionalProperties").get(0).get("propValue").asText());
    }
}
