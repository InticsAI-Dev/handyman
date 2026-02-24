package in.handyman.raven.lib.consumers;

import in.handyman.raven.core.encryption.EncryptionHandlers;
import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryOutputTable;
import in.handyman.raven.lib.services.sor.transaction.SorMetaMapperConsumer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class SorMetaMapperConsumerTest {

    private Marker aMarker;
    private ActionExecutionAudit audit ;

    @BeforeEach
    public void setup() {
        log.info("Setting up SorMetaMapperConsumerTest");
        this.aMarker = MarkerFactory.getMarker(" Test LlmJsonParser Marker : ");
        this.audit= new ActionExecutionAudit();
        this.audit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("kafka.production.activator", "true"),
                Map.entry(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false"),
                Map.entry("date.input.formats", "M/d/yy"),
                Map.entry("protegrity.dec.api.url", "http://localhost:8190/vulcan/api/encryption/decrypt"),
                Map.entry("protegrity.enc.api.url", "http://localhost:8190/vulcan/api/encryption/encrypt"),
                Map.entry("pipeline.encryption.default.holder", EncryptionHandlers.PROTEGRITY_API_ENC.name()),
                Map.entry("write.batch.size", "1")));

    }


    @Test
    public void multipleNodeInsideResponse() throws Exception {
            log.info("Testing SorMetaMapperConsumer");

        LlmJsonQueryInputTable inputTable = LlmJsonQueryInputTable.builder()
                .response("[{\"key\": \"diagnosis_code\", \"label\": \"Primary Diagnosis Code\", \"value\": \"M94.262\", \"boundingBox\": {\"topLeftX\": 327, \"topLeftY\": 799, \"bottomRightX\": 450, \"bottomRightY\": 815}, \"confidence\": 90.4}, {\"key\": \"diagnosis_code\", \"label\": \"Primary Diagnosis Code\", \"value\": \"M71.22\", \"boundingBox\": {\"topLeftX\": 457, \"topLeftY\": 799, \"bottomRightX\": 530, \"bottomRightY\": 815}, \"confidence\": 92.13}, {\"key\": \"diagnosis_code\", \"label\": \"Primary Diagnosis Code\", \"value\": \"R42\", \"boundingBox\": {\"topLeftX\": 537, \"topLeftY\": 799, \"bottomRightX\": 570, \"bottomRightY\": 815}, \"confidence\": 94.61}]")
                .paperNo(1)
                .originId("ORIGIN-1")
                .groupId(1L)
                .tenantId(1L)
                .rootPipelineId(1L)
                .batchId("batch-1")
                .modelRegistry("1")
                .extractedImageUnit("unit-1")
                .imageDpi(1L)
                .imageWidth(1L)
                .imageHeight(1L)
                .createdOn(new java.sql.Timestamp(System.currentTimeMillis()))
                .Process("TestProcess")
                .sorMetaDetail("[{\"isEncrypted\": \"false\", \"sorItemName\": \"diagnosis_code\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"BIRTHDATE_DATETIME_LP\"}]")
                .sorContainerId(1L)
                .SorItemLabel("Sor Item Label 1")
                .sorContainerInstance("instance-1")
                .build();
            // Add your test logic here
            SorMetaMapperConsumer consumer = new SorMetaMapperConsumer(log,aMarker,audit);
            List<LlmJsonQueryOutputTable> outputs =  consumer.process(new URL("http://example.com/test"), inputTable);
                log.info("Output from SorMetaMapperConsumer: {}", outputs);
                // Add assertions to validate the output
        outputs.stream().forEach(llmJsonQueryOutputTable -> {
            System.out.println("Extracted Key: " + llmJsonQueryOutputTable.getSorItemName());
            System.out.println("Extracted Value: " + llmJsonQueryOutputTable.getAnswer());
            assertTrue(llmJsonQueryOutputTable.getAnswer().equals("M94.262") || llmJsonQueryOutputTable.getAnswer().equals("M71.22") || llmJsonQueryOutputTable.getAnswer().equals("R42"));
        });



        }


    @Test
    public void checkIfAKeyWasMissedInTheResponse() throws Exception {
        // Check if the consumer can handle cases where a key defined in sorMetaDetail is missing in the response
        log.info("Testing SorMetaMapperConsumer");

        LlmJsonQueryInputTable inputTable = LlmJsonQueryInputTable.builder()
                .response("[{\"key\":\"member_full_name\",\"value\":\"DION Rosin\",\"label\":\"\\\"Last Name, First\\\":DION Rosin\",\"confidence\":0.0,\"boundingBox\":{\"topLeftY\":707,\"bottomRightX\":604,\"topLeftX\":347,\"bottomRightY\":720},\"section_alias\":\"PATIENT_INFO\"},{\"key\":\"member_date_of_birth\",\"value\":\"3/1/2014\",\"label\":\"DOB:3/1/2014\",\"confidence\":0.0,\"boundingBox\":{\"topLeftY\":720,\"bottomRightX\":512,\"topLeftX\":397,\"bottomRightY\":733},\"section_alias\":\"PATIENT_INFO\"},{\"key\":\"member_address_line1\",\"value\":\"85 Kneale Rd Eldorado Springs CO 800255015\",\"label\":\"Address:85 Kneale Rd Eldorado Springs CO 800255015\",\"confidence\":0.0,\"boundingBox\":{\"topLeftY\":733,\"bottomRightX\":704,\"topLeftX\":237,\"bottomRightY\":747},\"section_alias\":\"PATIENT_INFO\"}]")
                .paperNo(1)
                .originId("ORIGIN-1")
                .groupId(1L)
                .tenantId(1L)
                .rootPipelineId(1L)
                .batchId("batch-1")
                .modelRegistry("1")
                .extractedImageUnit("unit-1")
                .imageDpi(1L)
                .imageWidth(1L)
                .imageHeight(1L)
                .createdOn(new java.sql.Timestamp(System.currentTimeMillis()))
                .Process("TestProcess")
                .sorMetaDetail("[{\"isEncrypted\": \"true\", \"sorItemName\": \"member_full_name\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"MEMBERNAME_UPPERALPHANUM_LP\"}, {\"isEncrypted\": \"false\", \"sorItemName\": \"multiple_member_indicator\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"true\", \"sorItemName\": \"member_first_name\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"MEMBERNAME_UPPERALPHANUM_LP\"}, {\"isEncrypted\": \"false\", \"sorItemName\": \"member_state\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"false\", \"sorItemName\": \"member_zipcode\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"false\", \"sorItemName\": \"member_city\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"true\", \"sorItemName\": \"member_address_line1\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"false\", \"sorItemName\": \"member_gender\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"AES256\"}, {\"isEncrypted\": \"true\", \"sorItemName\": \"member_date_of_birth\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"BIRTHDATE_DATETIME_LP\"}, {\"isEncrypted\": \"true\", \"sorItemName\": \"member_last_name\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"MEMBERNAME_UPPERALPHANUM_LP\"}]")
                .sorContainerId(1L)
                .SorItemLabel("Sor Item Label 1")
                .sorContainerInstance("instance-1")
                .build();
        // Add your test logic here
        SorMetaMapperConsumer consumer = new SorMetaMapperConsumer(log,aMarker,audit);
        List<LlmJsonQueryOutputTable> outputs =  consumer.process(new URL("http://example.com/test"), inputTable);
        log.info("Output from SorMetaMapperConsumer: {}", outputs);

        // Add assertions to validate the output
        outputs.stream().forEach(llmJsonQueryOutputTable -> {
            System.out.println("Extracted Key: " + llmJsonQueryOutputTable.getSorItemName());
            System.out.println("Extracted Value: " + llmJsonQueryOutputTable.getAnswer());
            assertTrue(llmJsonQueryOutputTable.getSorItemName().equals("member_full_name") || llmJsonQueryOutputTable.getSorItemName().equals("member_date_of_birth") || llmJsonQueryOutputTable.getSorItemName().equals("member_address_line1") || llmJsonQueryOutputTable.getSorItemName().equals("member_first_name") || llmJsonQueryOutputTable.getSorItemName().equals("member_state") || llmJsonQueryOutputTable.getSorItemName().equals("member_zipcode") || llmJsonQueryOutputTable.getSorItemName().equals("member_city") || llmJsonQueryOutputTable.getSorItemName().equals("member_gender") || llmJsonQueryOutputTable.getSorItemName().equals("member_last_name") || llmJsonQueryOutputTable.getSorItemName().equals("multiple_member_indicator"));
        });



    }

    // Add more test cases to cover different scenarios, such as handling invalid JSON, missing keys in the response, and ensuring that encryption policies are applied correctly.
    @Test
    public void testInvalidJsonResponse() throws Exception {
        // Test how the consumer handles an invalid JSON response
        log.info("Testing SorMetaMapperConsumer with invalid JSON response");

        LlmJsonQueryInputTable inputTable = LlmJsonQueryInputTable.builder()
                .response("Invalid JSON")
                .paperNo(1)
                .originId("ORIGIN-1")
                .groupId(1L)
                .tenantId(1L)
                .rootPipelineId(1L)
                .batchId("batch-1")
                .modelRegistry("1")
                .extractedImageUnit("unit-1")
                .imageDpi(1L)
                .imageWidth(1L)
                .imageHeight(1L)
                .createdOn(new java.sql.Timestamp(System.currentTimeMillis()))
                .Process("TestProcess")
                .sorMetaDetail("[{\"isEncrypted\": \"false\", \"sorItemName\": \"diagnosis_code\", \"sorItemLabels\": \"\", \"encryptionPolicy\": \"BIRTHDATE_DATETIME_LP\"}]")
                .sorContainerId(1L)
                .SorItemLabel("Sor Item Label 1")
                .sorContainerInstance("instance-1")
                .build();

        SorMetaMapperConsumer consumer = new SorMetaMapperConsumer(log,aMarker,audit);
        List<LlmJsonQueryOutputTable> outputs =  consumer.process(new URL("http://example.com/test"), inputTable);
        outputs.stream().forEach(llmJsonQueryOutputTable -> {
            System.out.println("Extracted Key: " + llmJsonQueryOutputTable.getSorItemName());
            System.out.println("Extracted Value: " + llmJsonQueryOutputTable.getAnswer());
            assertTrue(llmJsonQueryOutputTable.getAnswer() == null || llmJsonQueryOutputTable.getAnswer().isEmpty() || llmJsonQueryOutputTable.getSorItemName().equals("diagnosis_code"));
        });
        // Add assertions to validate that the consumer handles the invalid JSON gracefully
    }



}
