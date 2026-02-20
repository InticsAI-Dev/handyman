package in.handyman.raven.lib.services.soritemhandling;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultivalueSorItemHandlingActionInputTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testBuilderAndGetters() {
        MultivalueSorItemHandlingActionInput input = MultivalueSorItemHandlingActionInput.builder()
                .answer("Test Answer")
                .originId("ORIGIN-001")
                .paperNo(1L)
                .sorItemName("PatientName")
                .vqaScore(0.98)
                .lineItemType("single_value")
                .isEncrypted("false")
                .encryptionPolicy("AES256")
                .sorContainerInstance("Instance1")
                .isMultiEntityEnabled("true")
                .sorContainerName("MedicalForm")
                .sectionAlias("Header")
                .whitelistedSections("{\"priorityLevel\": 1}")
                .build();

        assertNotNull(input);

    }

    @Test
    void testNoArgsConstructor() {
        MultivalueSorItemHandlingActionInput input = new MultivalueSorItemHandlingActionInput();
        assertNotNull(input);
        assertNull(input.getStatus());
    }

    @Test
    void testAllArgsConstructor() {
        MultivalueSorItemHandlingActionInput input = new MultivalueSorItemHandlingActionInput(
                "COMPLETED", "Answer", "0,0,0,0", "DOC1", 1L, 10, "Info", "ORG1", 1L, 1L, 1L, 1.0, 1L, "Name", "Q", 1L,
                1L, 1.0, "Reg",
                "Cat", "Stage", "Batch", "Type", "false", 1, "Pol", "Inst", "true", "Cont", "Alias", "White", false,
                "Msg", "Pri");
        assertEquals("COMPLETED", input.getStatus());
        assertEquals("Answer", input.getAnswer());
    }

    @Test
    void testEqualsAndHashCode() {
        MultivalueSorItemHandlingActionInput input1 = MultivalueSorItemHandlingActionInput.builder().originId("1")
                .build();
        MultivalueSorItemHandlingActionInput input2 = MultivalueSorItemHandlingActionInput.builder().originId("1")
                .build();
        MultivalueSorItemHandlingActionInput input3 = MultivalueSorItemHandlingActionInput.builder().originId("2")
                .build();

        assertEquals(input1, input2);
        assertEquals(input1.hashCode(), input2.hashCode());
        assertNotEquals(input1, input3);
    }

    @Test
    void testToString() {
        MultivalueSorItemHandlingActionInput input = MultivalueSorItemHandlingActionInput.builder().originId("1")
                .build();
        String stringReq = input.toString();
        assertNotNull(stringReq);
        assertTrue(stringReq.contains("originId=1"));
    }

    @Test
    void testJsonSerialization() throws Exception {
        MultivalueSorItemHandlingActionInput input = MultivalueSorItemHandlingActionInput.builder()
                .originId("test_origin")
                .answer("test_answer")
                .build();

        String json = mapper.writeValueAsString(input);
        assertTrue(json.contains("\"originId\":\"test_origin\""));
        assertTrue(json.contains("\"answer\":\"test_answer\""));

        MultivalueSorItemHandlingActionInput deserialized = mapper.readValue(json,
                MultivalueSorItemHandlingActionInput.class);
        assertEquals(input, deserialized);
    }
}
