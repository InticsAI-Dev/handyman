package in.handyman.raven.lib.bsh.member;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MemberDataTransformerTest {

    private Interpreter interpreter;
    private String sourceCode;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        interpreter = new Interpreter();
        objectMapper = new ObjectMapper();

        // Load the BeanShell-compatible source code
        sourceCode = loadSourceCode();

        // Evaluate the source code in BeanShell
        interpreter.eval(sourceCode);
    }

    private String loadSourceCode() throws IOException {
        // Adjust path to your actual source file location
        return new String(Files.readAllBytes(
                Paths.get("/home/balasoundarya.thanga@zucisystems.com/Repo/handyman/src/test/java/in/handyman/raven/lib/bsh/member/MemberDataTransformer.java")
        ));
    }

    private Map<String, Object> parseJsonToMap(String json) throws Exception {
        return objectMapper.readValue(json, Map.class);
    }

    @Test
    public void testAddressMergingDisabled() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: Address Merging DISABLED");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": [\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 0,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Hospital Account\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"Duffield, Tasha\",\n" +
                "          \"l\": \"Name\",\n" +
                "          \"b\": [100, 200, 200, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"04/18/87\",\n" +
                "          \"l\": \"Subscriber DOB\",\n" +
                "          \"b\": [400, 200, 450, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"12345\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 250, 200, 270]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 1,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Insurance Information\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"Duffield, Tasha\",\n" +
                "          \"l\": \"Name\",\n" +
                "          \"b\": [100, 300, 200, 320]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"04/18/87\",\n" +
                "          \"l\": \"DOB\",\n" +
                "          \"b\": [400, 300, 450, 320]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_address_line1\",\n" +
                "          \"v\": \"123 Main Street\",\n" +
                "          \"l\": \"Address\",\n" +
                "          \"b\": [100, 350, 250, 370]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_city\",\n" +
                "          \"v\": \"Springfield\",\n" +
                "          \"l\": \"City\",\n" +
                "          \"b\": [100, 380, 200, 400]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_state\",\n" +
                "          \"v\": \"IL\",\n" +
                "          \"l\": \"State\",\n" +
                "          \"b\": [250, 380, 280, 400]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_zipcode\",\n" +
                "          \"v\": \"62701\",\n" +
                "          \"l\": \"Zip\",\n" +
                "          \"b\": [300, 380, 360, 400]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        // Set data in interpreter
        interpreter.set("inputData", testData);

        // Create transformer with merging disabled
        interpreter.eval("transformer = new MemberDataTransformer(false);");

        // Process data
        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        // Assertions
        assertNotNull(result);


        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        printResult(result);
    }

    @Test
    public void testAddressMergingEnabled() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: Address Merging ENABLED");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": [\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 0,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Hospital Account\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"Duffield, Tasha\",\n" +
                "          \"l\": \"Name\",\n" +
                "          \"b\": [100, 200, 200, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"04/18/87\",\n" +
                "          \"l\": \"Subscriber DOB\",\n" +
                "          \"b\": [400, 200, 450, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"12345\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 250, 200, 270]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 1,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Insurance Information\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"Duffield, Tasha\",\n" +
                "          \"l\": \"Name\",\n" +
                "          \"b\": [100, 300, 200, 320]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"04/18/87\",\n" +
                "          \"l\": \"DOB\",\n" +
                "          \"b\": [400, 300, 450, 320]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_address_line1\",\n" +
                "          \"v\": \"123 Main Street\",\n" +
                "          \"l\": \"Address\",\n" +
                "          \"b\": [100, 350, 250, 370]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_city\",\n" +
                "          \"v\": \"Springfield\",\n" +
                "          \"l\": \"City\",\n" +
                "          \"b\": [100, 380, 200, 400]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_state\",\n" +
                "          \"v\": \"IL\",\n" +
                "          \"l\": \"State\",\n" +
                "          \"b\": [250, 380, 280, 400]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_zipcode\",\n" +
                "          \"v\": \"62701\",\n" +
                "          \"l\": \"Zip\",\n" +
                "          \"b\": [300, 380, 360, 400]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        interpreter.set("inputData", testData);

        // Create transformer with merging enabled
        interpreter.eval("transformer = new MemberDataTransformer(true);");

        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        assertNotNull(result);
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        assertNotNull(memberDetails);
        assertEquals(2, memberDetails.size());

        // First section should now have address fields merged (3 + 4 address fields = 7)
        Map section1 = (Map) memberDetails.get(0);
        List fields1 = (List) section1.get("fields");
        assertEquals(7, fields1.size());

        // Verify address fields are present in first section
        boolean hasAddress = false;
        for (int i = 0; i < fields1.size(); i++) {
            Map field = (Map) fields1.get(i);
            String key = (String) field.get("key");
            if ("member_address_line1".equals(key)) {
                hasAddress = true;
                assertEquals("123 Main Street", field.get("value"));
                break;
            }
        }
        assertTrue(hasAddress, "Address field should be merged to first section");

        // Second section should have only 2 fields (address fields removed)
        Map section2 = (Map) memberDetails.get(1);
        List fields2 = (List) section2.get("fields");
        assertEquals(2, fields2.size());

        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        printResult(result);
    }

    @Test
    public void testMatchByMemberId() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: Match by member_id");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": [\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 0,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Patient Info\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"MEM-999\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 100, 200, 120]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_first_name\",\n" +
                "          \"v\": \"John\",\n" +
                "          \"l\": \"First Name\",\n" +
                "          \"b\": [100, 150, 200, 170]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 1,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Contact Details\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"MEM-999\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 200, 200, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_address_line1\",\n" +
                "          \"v\": \"456 Oak Avenue\",\n" +
                "          \"l\": \"Address\",\n" +
                "          \"b\": [100, 250, 250, 270]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_city\",\n" +
                "          \"v\": \"Boston\",\n" +
                "          \"l\": \"City\",\n" +
                "          \"b\": [100, 280, 200, 300]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_state\",\n" +
                "          \"v\": \"MA\",\n" +
                "          \"l\": \"State\",\n" +
                "          \"b\": [250, 280, 280, 300]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_zipcode\",\n" +
                "          \"v\": \"02101\",\n" +
                "          \"l\": \"Zip Code\",\n" +
                "          \"b\": [300, 280, 360, 300]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        interpreter.set("inputData", testData);
        interpreter.eval("transformer = new MemberDataTransformer(true);");

        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        assertNotNull(result);
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        assertNotNull(memberDetails);
        assertEquals(2, memberDetails.size());

        // First section should have address fields merged
        Map section1 = (Map) memberDetails.get(0);
        List fields1 = (List) section1.get("fields");
        assertEquals(6, fields1.size()); // 2 original + 4 address fields

        // Verify member_id based matching worked
        boolean foundMemberId = false;
        for (int i = 0; i < fields1.size(); i++) {
            Map field = (Map) fields1.get(i);
            if ("member_id".equals(field.get("key"))) {
                assertEquals("MEM-999", field.get("value"));
                foundMemberId = true;
                break;
            }
        }
        assertTrue(foundMemberId);

        // Second section should have only 1 field (member_id)
        Map section2 = (Map) memberDetails.get(1);
        List fields2 = (List) section2.get("fields");
        assertEquals(1, fields2.size());

        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        printResult(result);
    }

    @Test
    public void testEmptyInput() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: Empty Input");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": []\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        interpreter.set("inputData", testData);
        interpreter.eval("transformer = new MemberDataTransformer(true);");

        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        assertNotNull(result);
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        assertNotNull(memberDetails);
        assertEquals(0, memberDetails.size());

        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        System.out.println("MEMBER_DETAILS: []");
    }

    @Test
    public void testNoMatchingMembers() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: No Matching Members");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": [\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 0,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Person A\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"A-111\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 100, 200, 120]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"Alice Smith\",\n" +
                "          \"l\": \"Name\",\n" +
                "          \"b\": [100, 150, 200, 170]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 1,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Person B\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_id\",\n" +
                "          \"v\": \"B-222\",\n" +
                "          \"l\": \"Member ID\",\n" +
                "          \"b\": [100, 200, 200, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_address_line1\",\n" +
                "          \"v\": \"789 Pine St\",\n" +
                "          \"l\": \"Address\",\n" +
                "          \"b\": [100, 250, 250, 270]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_city\",\n" +
                "          \"v\": \"Denver\",\n" +
                "          \"l\": \"City\",\n" +
                "          \"b\": [100, 280, 200, 300]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_state\",\n" +
                "          \"v\": \"CO\",\n" +
                "          \"l\": \"State\",\n" +
                "          \"b\": [250, 280, 280, 300]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_zipcode\",\n" +
                "          \"v\": \"80202\",\n" +
                "          \"l\": \"Zip\",\n" +
                "          \"b\": [300, 280, 360, 300]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        interpreter.set("inputData", testData);
        interpreter.eval("transformer = new MemberDataTransformer(true);");

        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        assertNotNull(result);
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        assertEquals(2, memberDetails.size());

        // Neither section should have merged fields
        Map section1 = (Map) memberDetails.get(0);
        List fields1 = (List) section1.get("fields");
        assertEquals(2, fields1.size()); // Original fields only

        Map section2 = (Map) memberDetails.get(1);
        List fields2 = (List) section2.get("fields");
        assertEquals(5, fields2.size()); // All address fields remain

        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        printResult(result);
    }

    @Test
    public void testNameAndDOBMatching() throws Exception {
        System.out.println("\n========================================");
        System.out.println("TEST: Name and DOB Matching");
        System.out.println("========================================");

        String jsonInput = "{\n" +
                "  \"patient_data\": [\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 0,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Primary Info\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_full_name\",\n" +
                "          \"v\": \"John Doe\",\n" +
                "          \"l\": \"Full Name\",\n" +
                "          \"b\": [100, 100, 200, 120]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"1990-01-15\",\n" +
                "          \"l\": \"Date of Birth\",\n" +
                "          \"b\": [100, 150, 200, 170]\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"_meta\": {\n" +
                "        \"ri\": 1,\n" +
                "        \"pi\": 0,\n" +
                "        \"sa\": \"Contact Info\"\n" +
                "      },\n" +
                "      \"fields\": [\n" +
                "        {\n" +
                "          \"k\": \"member_first_name\",\n" +
                "          \"v\": \"John\",\n" +
                "          \"l\": \"First Name\",\n" +
                "          \"b\": [100, 200, 200, 220]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_last_name\",\n" +
                "          \"v\": \"Doe\",\n" +
                "          \"l\": \"Last Name\",\n" +
                "          \"b\": [100, 230, 200, 250]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_date_of_birth\",\n" +
                "          \"v\": \"1990-01-15\",\n" +
                "          \"l\": \"DOB\",\n" +
                "          \"b\": [100, 260, 200, 280]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_address_line1\",\n" +
                "          \"v\": \"789 Maple Drive\",\n" +
                "          \"l\": \"Address\",\n" +
                "          \"b\": [100, 300, 250, 320]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_city\",\n" +
                "          \"v\": \"Austin\",\n" +
                "          \"l\": \"City\",\n" +
                "          \"b\": [100, 330, 200, 350]\n" +
                "        },\n" +
                "        {\n" +
                "          \"k\": \"member_state\",\n" +
                "          \"v\": \"TX\",\n" +
                "          \"l\": \"State\",\n" +
                "          \"b\": [250, 330, 280, 350]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> testData = parseJsonToMap(jsonInput);

        // PRINT INPUT
        System.out.println("\n--- INPUT DATA ---");
        printInput(testData);

        interpreter.set("inputData", testData);
        interpreter.eval("transformer = new MemberDataTransformer(true);");

        Map result = (Map) interpreter.eval("transformer.processPatientData(inputData);");

        assertNotNull(result);
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        assertEquals(2, memberDetails.size());

        // First section should have address fields merged
        Map section1 = (Map) memberDetails.get(0);
        List fields1 = (List) section1.get("fields");
        assertEquals(7, fields1.size()); // 2 original + 5 fields from second section

        // Second section should have only 3 fields (address fields removed)
        Map section2 = (Map) memberDetails.get(1);
        List fields2 = (List) section2.get("fields");
        assertEquals(3, fields2.size());

        // PRINT OUTPUT
        System.out.println("\n--- OUTPUT DATA ---");
        printResult(result);
    }

    private void printInput(Map<String, Object> input) {
        List patientDataList = (List) input.get("patient_data");

        if (patientDataList == null || patientDataList.isEmpty()) {
            System.out.println("patient_data: []");
            return;
        }

        System.out.println("patient_data: [" + patientDataList.size() + " sections]");

        for (int i = 0; i < patientDataList.size(); i++) {
            Map section = (Map) patientDataList.get(i);
            Map meta = (Map) section.get("_meta");
            List fields = (List) section.get("fields");

            String sectionAlias = meta != null ? (String) meta.get("sa") : "Unknown Section";
            System.out.println("\n  Section " + (i+1) + ": " + sectionAlias);
            System.out.println("    Fields (" + (fields != null ? fields.size() : 0) + "):");

            if (fields != null) {
                for (int j = 0; j < fields.size(); j++) {
                    Map field = (Map) fields.get(j);
                    String key = (String) field.get("k");
                    String value = (String) field.get("v");
                    String label = (String) field.get("l");
                    List bbox = (List) field.get("b");

                    System.out.print("      - " + label + " (" + key + "): " + value);

                    if (bbox != null && bbox.size() == 4) {
                        System.out.print(" [BBox: " + bbox.get(0) + "," + bbox.get(1) + " -> " +
                                bbox.get(2) + "," + bbox.get(3) + "]");
                    }
                    System.out.println();
                }
            }
        }
    }

    private void printResult(Map result) {
        List memberDetails = (List) result.get("MEMBER_DETAILS");
        System.out.println("MEMBER_DETAILS: [" + memberDetails.size() + " sections]");

        for (int i = 0; i < memberDetails.size(); i++) {
            Map section = (Map) memberDetails.get(i);
            String sectionAlias = (String) section.get("sectionAlias");
            List fields = (List) section.get("fields");

            System.out.println("\n  Section " + (i+1) + ": " + sectionAlias);
            System.out.println("    Fields (" + fields.size() + "):");

            for (int j = 0; j < fields.size(); j++) {
                Map field = (Map) fields.get(j);
                String key = (String) field.get("key");
                String value = (String) field.get("value");
                String label = (String) field.get("label");
                Map bbox = (Map) field.get("boundingBox");

                System.out.print("      - " + label + " (" + key + "): " + value);

                if (bbox != null && !bbox.isEmpty()) {
                    System.out.print(" [BBox: " + bbox.get("topLeftX") + "," + bbox.get("topLeftY") + " -> " +
                            bbox.get("bottomRightX") + "," + bbox.get("bottomRightY") + "]");
                }
                System.out.println();
            }
        }
        System.out.println();
    }
}