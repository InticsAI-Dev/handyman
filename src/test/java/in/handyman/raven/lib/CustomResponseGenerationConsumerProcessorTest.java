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
import java.util.Iterator;
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
        assertEquals("REQ-1", root.get("requestTxnId").asText());
        assertEquals("SUCCESS", root.get("status").asText());
        assertEquals("DOC-1", root.get("documentId").asText());
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
                "      \"authId\": {\n" +
                "        \"value\": \"${auth_id}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"hcid\": {\n" +
                "        \"value\": \"${member_id}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberLastName\": {\n" +
                "        \"value\": \"${member_last_name}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberFirstName\": {\n" +
                "        \"value\": \"${member_first_name}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberDOB\": {\n" +
                "        \"value\": \"${member_date_of_birth}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberGender\": {\n" +
                "        \"value\": \"${member_gender}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberAddressLine1\": {\n" +
                "        \"value\": \"${member_address_line_1}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberCity\": {\n" +
                "        \"value\": \"${member_city}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberState\": {\n" +
                "        \"value\": \"${member_state}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"memberZipCode\": {\n" +
                "        \"value\": \"${member_zip_code}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"serviceFromDate\": {\n" +
                "        \"value\": \"${service_from_date}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"serviceToDate\": {\n" +
                "        \"value\": \"${service_to_date}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"diagnosis\": [\n" +
                "        {\n" +
                "          \"cd\": {\n" +
                "            \"value\": \"${diagnosis_code}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"desc\": {\n" +
                "            \"value\": \"${desc}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"codePointer\": {\n" +
                "            \"value\": \"${code_pointer}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"service\": [\n" +
                "        {\n" +
                "          \"cd\": {\n" +
                "            \"value\": \"${service_code}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"modifier\": [\n" +
                "            {\n" +
                "              \"cd\": {\n" +
                "                \"value\": \"${service_modifier}\",\n" +
                "                \"page\": 0,\n" +
                "                \"confidence\": 0,\n" +
                "                \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "              }\n" +
                "            }\n" +
                "          ],\n" +
                "          \"serviceQuantity\": [\n" +
                "            {\n" +
                "              \"quantityType\": { \"value\": \"Units\" },\n" +
                "              \"quantityUnits\": {\n" +
                "                \"value\": \"${service_unit}\",\n" +
                "                \"page\": 0,\n" +
                "                \"confidence\": 0,\n" +
                "                \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"quantityType\": { \"value\": \"Visits\" },\n" +
                "              \"quantityUnits\": {\n" +
                "                \"value\": \"${service_visit}\",\n" +
                "                \"page\": 0,\n" +
                "                \"confidence\": 0,\n" +
                "                \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "              }\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"levelOfService\": {\n" +
                "        \"value\": \"${level_of_service}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"provider\": [\n" +
                "        {\n" +
                "          \"providerCategory\": {\n" +
                "            \"value\": \"${provider_category}\"\n" +
                "          },\n" +
                "          \"providerNPI\": {\n" +
                "            \"value\": \"${provider_npi}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerTIN\": {\n" +
                "            \"value\": \"${provider_tin}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerFirstName\": {\n" +
                "            \"value\": \"${provider_first_name}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerLastName\": {\n" +
                "            \"value\": \"${provider_last_name}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerAddressLine1\": {\n" +
                "            \"value\": \"${provider_address_line_1}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerCity\": {\n" +
                "            \"value\": \"${provider_city}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerState\": {\n" +
                "            \"value\": \"${provider_state}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          },\n" +
                "          \"providerZipCode\": {\n" +
                "            \"value\": \"${provider_zip_code}\",\n" +
                "            \"page\": 0,\n" +
                "            \"confidence\": 0,\n" +
                "            \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"authorizationIndicators\": [\n" +
                "        {\n" +
                "          \"propName\": { \"value\": \"ADDL_MMS_ID\" },\n" +
                "          \"propValue\": \"${auth_id}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"faxReceivedDate\": {\n" +
                "        \"value\": \"${fax_received_date}\",\n" +
                "        \"page\": 0,\n" +
                "        \"confidence\": 0,\n" +
                "        \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "      },\n" +
                "      \"additionalProperties\": [\n" +
                "        {\n" +
                "          \"propName\": \"AUTH_ADDL_KEYWORD\",\n" +
                "          \"propValue\": \"${additional_auth_properties}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"SORTING_KEY\",\n" +
                "          \"propValue\": \"${responsible_area}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"memberAdditionalProperties\": [\n" +
                "        {\n" +
                "          \"propName\": \"MEMBER_INDICATOR\",\n" +
                "          \"propValue\": \"${multi_member_indicator}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"NEWBORN_REQUEST\",\n" +
                "          \"propValue\": \"${newborn_request}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"newborn_first_name\",\n" +
                "          \"propValue\": \"${newborn_first_name}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"newborn_last_name\",\n" +
                "          \"propValue\": \"${newborn_last_name}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"newborn_gender\",\n" +
                "          \"propValue\": \"${newborn_gender}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        },\n" +
                "        {\n" +
                "          \"propName\": \"newborn_date_of_birth\",\n" +
                "          \"propValue\": \"${newborn_date_of_birth}\",\n" +
                "          \"page\": 0,\n" +
                "          \"confidence\": 0,\n" +
                "          \"boundingBox\": { \"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0 }\n" +
                "        }\n" +
                "      ]\n" +
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
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("diagnosis_code").predictedValue("G242").paperNo(2).precision(0.95).leftPos(1485.0).rightPos(1635.0).upperPos(1789.0).lowerPos(1823.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_npi").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("1952628794").paperNo(2).precision(1.0).leftPos(643.0).rightPos(846.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_first_name").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("SAMANTHA B").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_last_name").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("ZERINGUE").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1062.0).lowerPos(1096.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_address_line1").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("201 4TH STREET SUITE 5B").paperNo(2).precision(1.0).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_city").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("ALEXANDRIA").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_state").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("LA").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("servicing_provider_zipcode").containerName("SERVICING_PROVIDER_DETAILS").predictedValue("71301").paperNo(2).precision(0.5).leftPos(254.0).rightPos(643.0).upperPos(1581.0).lowerPos(1615.0).sorContainerInstance("1").transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("fax_received_date").predictedValue("06-05-2025 17:19:30").paperNo(1).precision(0.95).leftPos(17.0).rightPos(372.0).upperPos(2152.0).lowerPos(2189.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("newborn_request").predictedValue("N").paperNo(1).precision(0.5).leftPos(0.0).rightPos(0.0).upperPos(0.0).lowerPos(0.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("additional_auth_properties").predictedValue("inpatient").paperNo(1).precision(0.5).leftPos(0.0).rightPos(0.0).upperPos(0.0).lowerPos(0.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("responsible_area").predictedValue("urgent emergency").paperNo(1).precision(0.5).leftPos(0.0).rightPos(0.0).upperPos(0.0).lowerPos(0.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("additional_auth_properties").predictedValue("outpatient").paperNo(2).precision(0.6).leftPos(1.0).rightPos(2.0).upperPos(3.0).lowerPos(4.0).transactionId("TRZ-758").metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().originId("ORIGIN-494").sorItemName("responsible_area").predictedValue("emergency").paperNo(2).precision(0.55).leftPos(10.0).rightPos(20.0).upperPos(30.0).lowerPos(40.0).transactionId("TRZ-758").metadataJson(metadata).build());

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        String finalGeneratedJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(finalJson);
        System.out.println("Final Generated JSON:\n" + finalGeneratedJson);

        assertNotNull(finalJson);
        JsonNode root = finalJson.get("root");
        assertNotNull(root);
        assertEquals("1cf66141-c378-499a-9c72-3ad4d1028fa6", root.get("requestTxnId").asText());
        assertEquals("Exlsie", root.get("aumipayload").get("memberLastName").get("value").asText());
        assertEquals("Noichole", root.get("aumipayload").get("memberFirstName").get("value").asText());
        assertEquals("1952628794", root.get("aumipayload").get("provider").get(0).get("providerNPI").get("value").asText());
        assertEquals("service_provider", root.get("aumipayload").get("provider").get(0).get("providerCategory").get("value").asText());
        JsonNode additionalProperties = root.get("aumipayload").get("additionalProperties");
        assertEquals(4, additionalProperties.size());
        assertEquals("AUTH_ADDL_KEYWORD", additionalProperties.get(0).get("propName").asText());
        assertEquals("inpatient", additionalProperties.get(0).get("propValue").asText());
        assertEquals(1, additionalProperties.get(0).get("page").asInt());
        assertEquals("AUTH_ADDL_KEYWORD", additionalProperties.get(1).get("propName").asText());
        assertEquals("outpatient", additionalProperties.get(1).get("propValue").asText());
        assertEquals(2, additionalProperties.get(1).get("page").asInt());
        assertEquals(1.0, additionalProperties.get(1).get("boundingBox").get("x").asDouble(), 0.001);
        assertEquals("SORTING_KEY", additionalProperties.get(2).get("propName").asText());
        assertEquals("urgent emergency", additionalProperties.get(2).get("propValue").asText());
        assertEquals("SORTING_KEY", additionalProperties.get(3).get("propName").asText());
        assertEquals("emergency", additionalProperties.get(3).get("propValue").asText());
        assertEquals(2, additionalProperties.get(3).get("page").asInt());
        assertEquals("N", root.get("aumipayload").get("memberAdditionalProperties").get(0).get("propValue").asText());
    }

    @Test
    void generateCustomJson_shouldPickNonEmptyAliasPredictionWhenDuplicatesExist() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [{ \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}}, \"providerAddressLine1\": {\"value\": \"${provider_address_line_1}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\": 0, \"width\": 0, \"y\": 0, \"height\": 0}} }] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        String metadata = "{\"requestTxnId\":\"fa8a30e1-eb31-4236-9780-ea598ac2ce25\",\"documentId\":\"COMM_P3_INREQ_9\",\"inboundTransactionId\":\"ITX-3317\",\"transactionId\":\"TRZ-833\",\"inboundDocumentName\":\"COMM_P3_INREQ_9\",\"documentExtension\":\"pdf\",\"documentType\":\"MEDICAL_GBD\"}";
        List<PredictionDTO> inputs = new ArrayList<>();
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_npi").predictedValue("").precision(0.5).metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_npi").predictedValue("1952628794").precision(1.0).paperNo(2).metadataJson(metadata).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_address_line1").predictedValue("1 Medical Center Drive").precision(1.0).paperNo(2).metadataJson(metadata).build());

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);

        assertEquals("1952628794", provider.path("providerNPI").path("value").asText());
        assertEquals("1 Medical Center Drive", provider.path("providerAddressLine1").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldParseBarePlaceholderValuesWithoutRegexFailure() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"additionalProperties\": [ { \"propName\": { \"value\": \"AUTH_ADDL_KEYWORD\" }, \"propValue\": ${additional_properties}, \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = new ArrayList<>();
        inputs.add(PredictionDTO.builder().sorItemName("additional_properties").predictedValue("foo").precision(1.0).paperNo(1).build());

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        assertEquals("foo", finalJson.path("root").path("aumipayload").path("additionalProperties").get(0).path("propValue").asText());
    }

    @Test
    void generateCustomJson_shouldPruneOnlyAumiPayloadLeafWhenNoDataPresent() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"requestTxnId\": \"\", \"aumipayload\": { \"memberFirstName\": {\"value\": \"${member_first_name}\", \"page\": 9, \"confidence\": 99, \"boundingBox\": {\"x\": 1, \"width\": 2, \"y\": 3, \"height\": 4}} }, \"outsideNode\": {\"value\": \"${outside_field}\", \"page\": 9, \"confidence\": 99, \"boundingBox\": {\"x\": 1, \"width\": 2, \"y\": 3, \"height\": 4}} } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        JsonNode finalJson = consumer.generateCustomJson(template, List.of());
        JsonNode root = finalJson.path("root");
        assertEquals(false, root.path("aumipayload").has("memberFirstName"));
        assertEquals("${outside_field}", root.path("outsideNode").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldDefaultMemberIndicatorWhenValueMissingInAumiPayload() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"memberAdditionalProperties\": [ { \"propName\": { \"value\": \"MEMBER_INDICATOR\" }, \"propValue\": \"${multi_member_indicator}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0} }, { \"propName\": { \"value\": \"NEWBORN_REQUEST\" }, \"propValue\": \"${newborn_request}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("newborn_request").predictedValue("Y").precision(1.0).paperNo(1).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode arr = finalJson.path("root").path("aumipayload").path("memberAdditionalProperties");

        assertEquals(2, arr.size());
        assertEquals("MEMBER_INDICATOR", arr.get(0).path("propName").path("value").asText());
        assertEquals("N", arr.get(0).path("propValue").asText());
        assertEquals("NEWBORN_REQUEST", arr.get(1).path("propName").path("value").asText());
        assertEquals("Y", arr.get(1).path("propValue").asText());
    }

    @Test
    void generateCustomJson_shouldExpandDiagnosisServiceAndProviderForMultipleValues() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { " +
                "\"diagnosis\": [ { \"cd\": {\"value\": \"${diagnosis_code}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ], " +
                "\"service\": [ { \"cd\": {\"value\": \"${service_code}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}}, \"serviceQuantity\": [ { \"quantityType\": {\"value\": \"Units\"}, \"quantityUnits\": {\"value\": \"${service_unit}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} }, { \"quantityType\": {\"value\": \"Visits\"}, \"quantityUnits\": {\"value\": \"${service_visit}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } ], " +
                "\"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}}, \"providerFirstName\": {\"value\": \"${provider_first_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] " +
                "} } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = new ArrayList<>();
        inputs.add(PredictionDTO.builder().sorItemName("diagnosis_code").predictedValue("D1").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("diagnosis_code").predictedValue("D2").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_code").predictedValue("S1").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_code").predictedValue("S2").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_quantity_units").predictedValue("10").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_quantity_units").predictedValue("20").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_quantity_visits").predictedValue("1").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("service_quantity_visits").predictedValue("2").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_npi").containerName("SERVICING_PROVIDER_DETAILS").sorContainerInstance("1").predictedValue("NPI1").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_first_name").containerName("SERVICING_PROVIDER_DETAILS").sorContainerInstance("1").predictedValue("FN1").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_npi").containerName("SERVICING_PROVIDER_DETAILS").sorContainerInstance("2").predictedValue("NPI2").paperNo(1).precision(1.0).build());
        inputs.add(PredictionDTO.builder().sorItemName("servicing_provider_first_name").containerName("SERVICING_PROVIDER_DETAILS").sorContainerInstance("2").predictedValue("FN2").paperNo(1).precision(1.0).build());

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode payload = finalJson.path("root").path("aumipayload");

        assertEquals(2, payload.path("diagnosis").size());
        assertEquals("D1", payload.path("diagnosis").get(0).path("cd").path("value").asText());
        assertEquals("D2", payload.path("diagnosis").get(1).path("cd").path("value").asText());

        assertEquals(2, payload.path("service").size());
        assertEquals("S1", payload.path("service").get(0).path("cd").path("value").asText());
        assertEquals("S2", payload.path("service").get(1).path("cd").path("value").asText());
        assertEquals("10", payload.path("service").get(0).path("serviceQuantity").get(0).path("quantityUnits").path("value").asText());
        assertEquals("20", payload.path("service").get(1).path("serviceQuantity").get(0).path("quantityUnits").path("value").asText());
        assertEquals("1", payload.path("service").get(0).path("serviceQuantity").get(1).path("quantityUnits").path("value").asText());
        assertEquals("2", payload.path("service").get(1).path("serviceQuantity").get(1).path("quantityUnits").path("value").asText());

        assertEquals(2, payload.path("provider").size());
        assertEquals("NPI1", payload.path("provider").get(0).path("providerNPI").path("value").asText());
        assertEquals("FN1", payload.path("provider").get(0).path("providerFirstName").path("value").asText());
        assertEquals("service_provider", payload.path("provider").get(0).path("providerCategory").path("value").asText());
        assertEquals("NPI2", payload.path("provider").get(1).path("providerNPI").path("value").asText());
        assertEquals("FN2", payload.path("provider").get(1).path("providerFirstName").path("value").asText());
        assertEquals("service_provider", payload.path("provider").get(1).path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldDeriveDistinctProviderCategoryPerIndexExpandedRow() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("servicing_provider_npi").predictedValue("S-NPI").paperNo(1).precision(1.0).build(),
                PredictionDTO.builder().sorItemName("ordering_provider_npi").predictedValue("O-NPI").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode providers = finalJson.path("root").path("aumipayload").path("provider");

        assertEquals(2, providers.size());
        assertEquals("S-NPI", providers.get(0).path("providerNPI").path("value").asText());
        assertEquals("service_provider", providers.get(0).path("providerCategory").path("value").asText());
        assertEquals("O-NPI", providers.get(1).path("providerNPI").path("value").asText());
        assertEquals("ordering_provider", providers.get(1).path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldSetProviderCategoryFromContainerName() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("ordering_provider_npi").containerName("ORDERING_PROVIDER_DETAILS").sorContainerInstance("1").predictedValue("12345").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("ordering_provider", provider.path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldDeriveProviderCategoryFromServiceProviderSorPrefix() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}}, \"providerTIN\": {\"value\": \"${provider_tin}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("service_provider_npi").predictedValue("111").paperNo(1).precision(1.0).build(),
                PredictionDTO.builder().sorItemName("service_provider_tin").predictedValue("222").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("service_provider", provider.path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldDeriveProviderCategoryFromServicingFacilitySorPrefix() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("servicing_facility_npi").predictedValue("FAC-NPI").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("servicing_facility", provider.path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldDeriveProviderCategoryFromUndefinedProviderSorPrefix() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("undefined_provider_npi").predictedValue("U-NPI").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("undefined_providers", provider.path("providerCategory").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldSetProviderCategoryWhenAnyProviderDetailsAreAssigned() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("servicing_provider_npi").containerName("SERVICING_PROVIDER_DETAILS").sorContainerInstance("1").predictedValue("11111").paperNo(1).precision(1.0).build(),
                PredictionDTO.builder().sorItemName("ordering_provider_tin").containerName("ORDERING_PROVIDER_DETAILS").sorContainerInstance("1").predictedValue("22222").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("service_provider", provider.path("providerCategory").path("value").asText());
        assertEquals("11111", provider.path("providerNPI").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldSetProviderCategoryWhenProviderExpandedByIndex() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}}, \"providerFirstName\": {\"value\": \"${provider_first_name}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("servicing_provider_npi").predictedValue("1952628794").paperNo(2).precision(1.0).build(),
                PredictionDTO.builder().sorItemName("servicing_provider_first_name").predictedValue("SAMANTHA B").paperNo(2).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("service_provider", provider.path("providerCategory").path("value").asText());
        assertEquals("1952628794", provider.path("providerNPI").path("value").asText());
        assertEquals("SAMANTHA B", provider.path("providerFirstName").path("value").asText());
    }

    @Test
    void generateCustomJson_shouldKeepProviderCategoryNodeWhenProviderExists() throws Exception {
        ActionExecutionAudit action = new ActionExecutionAudit();
        String template = "{ \"root\": { \"aumipayload\": { \"provider\": [ { \"providerCategory\": {\"value\": \"${provider_category}\"}, \"providerNPI\": {\"value\": \"${provider_npi}\", \"page\": 0, \"confidence\": 0, \"boundingBox\": {\"x\":0,\"width\":0,\"y\":0,\"height\":0}} } ] } } }";
        action.getContext().put("custom.json.generation.structure", template);
        CustomResponseGenerationConsumerProcessor consumer = new CustomResponseGenerationConsumerProcessor(log, marker, action);

        List<PredictionDTO> inputs = List.of(
                PredictionDTO.builder().sorItemName("servicing_provider_npi").predictedValue("NPI-1").paperNo(1).precision(1.0).build(),
                PredictionDTO.builder().sorItemName("ordering_provider_tin").predictedValue("TIN-1").paperNo(1).precision(1.0).build()
        );

        JsonNode finalJson = consumer.generateCustomJson(template, inputs);
        JsonNode provider = finalJson.path("root").path("aumipayload").path("provider").get(0);
        assertEquals("NPI-1", provider.path("providerNPI").path("value").asText());
        assertEquals(true, provider.has("providerCategory"));
        Iterator<String> providerFieldNames = provider.fieldNames();
        assertEquals("providerCategory", providerFieldNames.next());
    }
}
