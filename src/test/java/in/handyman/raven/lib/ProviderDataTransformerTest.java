package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProviderDataTransformerTest {

    private ProviderDataTransformer transformer;
    private RadonQueryInputTable inputTable;
    private String providerClassName = "TestProviderClass";
    private String request = "{}";
    private String response = "{}";
    private String endpoint = "http://localhost";

    @BeforeEach
    void setUp() {

// In your test class setup method:
        Logger mockLogger = Mockito.mock(Logger.class);
        Marker mockMarker = Mockito.mock(Marker.class);
        ObjectMapper mockObjectMapper = Mockito.mock(ObjectMapper.class);
        ActionExecutionAudit mockAction = Mockito.mock(ActionExecutionAudit.class);
        Jdbi mockJdbi = Mockito.mock(Jdbi.class);
        InticsIntegrity mockEncryption = Mockito.mock(InticsIntegrity.class);

        transformer = new ProviderDataTransformer(
                mockLogger, mockMarker, mockObjectMapper, mockAction, mockJdbi, mockEncryption
        );
        inputTable = RadonQueryInputTable.builder()
                .originId("origin-1")
                .groupId(1L)
                .tenantId(1L)
                .processId(1L)
                .inputFilePath("file/path")
                .paperNo(1)
                .rootPipelineId(1L)
                .batchId("batch-1")
                .apiName("api")
                .category("cat")
                .modelRegistry("reg")
                .sorContainerId(1L)
                .build();
    }

    @Test
    void testProcessProviderData_NormalCase() {
        String payload = "{\"Providers\": [{\"Provider Type\": \"Servicing Provider\", \"Provider Name\": \"Theodore Richards\", \"Provider NPI\": \"\", \"Provider Specialty\": \"\", \"Provider Tax ID\": \"1234567891\", \"Provider Address\": \"941 Spring Creek Rd\", \"Provider City\": \"Chattanooga\", \"Provider State\": \"TN\", \"Provider ZIP Code\": \"37412\"}]}";
        String className = getProviderClassName();

        List<RadonQueryOutputTable> result = transformer.processProviderData(className, providerClassName, payload, inputTable, request, response, endpoint);
//        assertNotNull(result);
//        assertFalse(result.isEmpty());
//        assertEquals("Theodore Richards", result.get(0).getTotalResponseJson());
        System.out.println(result.get(0).getTotalResponseJson());
    }

    @Test
    void testProcessProviderData_EmptyProviders() {
        String payload = "{\"Providers\": []}";
        List<RadonQueryOutputTable> result = transformer.processProviderData(payload, providerClassName, payload, inputTable, request, response, endpoint);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testProcessProviderData_MissingFields() {
        String payload = "{\"Providers\": [{\"Provider Name\": \"Unknown\"}]}";
        List<RadonQueryOutputTable> result = transformer.processProviderData(payload, providerClassName, payload, inputTable, request, response, endpoint);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Unknown", result.get(0).getTotalResponseJson());
    }

    @Test
    void testProcessProviderData_InvalidJson() {
        String payload = "not a json";
        assertThrows(Exception.class, () -> {
            transformer.processProviderData(payload, providerClassName, payload, inputTable, request, response, endpoint);
        });
    }

    public String getProviderClassName(){

        String className = "import java.util.*;\n" +
                "import org.slf4j.Logger;\n" +
                "\n" +
                "public class ProviderTransformerFinalBsh {\n" +
                "    private Logger logger;\n" +
                "\n" +
                "    public ProviderTransformerFinalBsh(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    List processProviders(List providers) {\n" +
                "        logger.info(\"Starting processProviders execution\");\n" +
                "\n" +
                "        List result = new ArrayList();\n" +
                "        Map metaProviderEntityDetails = metaProviderEntityDetails();\n" +
                "        Map itemMappingDetails = itemMappingDetails();\n" +
                "        Map nameMappingDetails = nameMappingDetails();\n" +
                "\n" +
                "        for (int i = 0; i < providers.size(); i++) {\n" +
                "            Map provider = (Map) providers.get(i);\n" +
                "            String matchedContainer = findMatchingContainer((String) provider.get(\"Provider Type\"), metaProviderEntityDetails);\n" +
                "            processProviderData(provider, matchedContainer, itemMappingDetails, nameMappingDetails, result);\n" +
                "        }\n" +
                "\n" +
                "        logger.info(\"processProviders completed with result size: \" + result.size());\n" +
                "\n" +
                "        return transformListToVector(result);\n" +
                "    }\n" +
                "\n" +
                "Vector transformListToVector(List inputList) {\n" +
                "    Vector result = new Vector();\n" +
                "    \n" +
                "    for (int i = 0; i < inputList.size(); i++) {\n" +
                "        ProviderTransformerOutputItem item = (ProviderTransformerOutputItem) inputList.get(i);\n" +
                "        Hashtable outputItem = new Hashtable();\n" +
                "        outputItem.put(\"key\", item.key);\n" +
                "        outputItem.put(\"value\", item.value);\n" +
                "        outputItem.put(\"boundingBox\", new Hashtable());\n" +
                "        outputItem.put(\"confidence\", new Double(100.0));\n" +
                "        outputItem.put(\"sorContainerName\", item.sorContainerName);\n" +
                "        \n" +
                "        result.add(outputItem);\n" +
                "    }\n" +
                "    \n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "    String findMatchingContainer(String providerType, Map metaProviderEntityDetails) {\n" +
                "        String cleanedProviderType = cleanString(providerType != null ? providerType : \"\");\n" +
                "\n" +
                "        for (Iterator it = metaProviderEntityDetails.entrySet().iterator(); it.hasNext(); ) {\n" +
                "            Map.Entry entry = (Map.Entry) it.next();\n" +
                "            List values = (List) entry.getValue();\n" +
                "\n" +
                "            for (int i = 0; i < values.size(); i++) {\n" +
                "                String value = (String) values.get(i);\n" +
                "                if (cleanedProviderType.contains(cleanString(value))) {\n" +
                "                    logger.info(\"Executing exact match for container: \" + entry.getKey());\n" +
                "                    return (String) entry.getKey();\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return findClosestMatchingContainer(cleanedProviderType, metaProviderEntityDetails);\n" +
                "    }\n" +
                "    String findClosestMatchingContainer(String cleanedProviderType, Map metaProviderEntityDetails) {\n" +
                "        Map containerDistanceMap = new HashMap();\n" +
                "\n" +
                "        for (Iterator it = metaProviderEntityDetails.entrySet().iterator(); it.hasNext(); ) {\n" +
                "            Map.Entry entry = (Map.Entry) it.next();\n" +
                "            List values = (List) entry.getValue();\n" +
                "\n" +
                "            for (int i = 0; i < values.size(); i++) {\n" +
                "                String value = (String) values.get(i);\n" +
                "                double distance = (calculateJaroWinklerDistance(cleanedProviderType, cleanString(value)) +\n" +
                "                        calculateLCSLength(cleanedProviderType, cleanString(value))) / 2;\n" +
                "\n" +
                "                String key = (String) entry.getKey();\n" +
                "                double existingDistance = containerDistanceMap.containsKey(key) ? ((Double) containerDistanceMap.get(key)) : 0.0;\n" +
                "                containerDistanceMap.put(key, Math.max(existingDistance, distance));\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        double maxDistance = 0.0;\n" +
                "        String bestMatch = \"UNDEFINED_PROVIDER_DETAILS\";\n" +
                "\n" +
                "        for (Iterator it = containerDistanceMap.entrySet().iterator(); it.hasNext(); ) {\n" +
                "            Map.Entry entry = (Map.Entry) it.next();\n" +
                "            double value = (Double) entry.getValue();\n" +
                "            if (value > maxDistance) {\n" +
                "                maxDistance = value;\n" +
                "                bestMatch = (String) entry.getKey();\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (maxDistance > 0.7) {\n" +
                "            return bestMatch;\n" +
                "        }\n" +
                "\n" +
                "        logger.info(\"No exact match found. Using closest match: \" + bestMatch + \" with similarity: \" + maxDistance);\n" +
                "        return \"UNDEFINED_PROVIDER_DETAILS\";\n" +
                "    }\n" +
                "    void processProviderData(Map provider, String matchedContainer, Map itemMappingDetails,\n" +
                "                             Map nameMappingDetails, List result) {\n" +
                "        List itemsList = (List) itemMappingDetails.get(matchedContainer);\n" +
                "        if (itemsList == null) {\n" +
                "            logger.warn(\"No items mapping found for container: \" + matchedContainer);\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        for (int i = 0; i < itemsList.size(); i++) {\n" +
                "            String item = (String) itemsList.get(i);\n" +
                "            String mappedKey = (String) nameMappingDetails.get(item);\n" +
                "\n" +
                "            if (mappedKey != null) {\n" +
                "                String keyValue = (String) provider.get(mappedKey);\n" +
                "                if (mappedKey.equals(\"Provider Name\")) {\n" +
                "                    if (matchedContainer.equals(\"SERVICING_PROVIDER_DETAILS\")) {\n" +
                "                        List providerTransformerOutputItems = transformServicingProviderName(keyValue, matchedContainer);\n" +
                "                        result.addAll(providerTransformerOutputItems);\n" +
                "                    } else if (matchedContainer.equals(\"REFERRING_PROVIDER_DETAILS\")) {\n" +
                "                        List providerTransformerOutputItems = transformReferringProviderName(keyValue, matchedContainer);\n" +
                "                        result.addAll(providerTransformerOutputItems);\n" +
                "                    } else {\n" +
                "                        if (keyValue != null && !keyValue.trim().isEmpty()) {\n" +
                "                            result.add(new ProviderTransformerOutputItem(item, keyValue, new HashMap(), matchedContainer));\n" +
                "                        }\n" +
                "                    }\n" +
                "                } else {\n" +
                "                    if (keyValue != null && !keyValue.trim().isEmpty()) {\n" +
                "                        result.add(new ProviderTransformerOutputItem(item, keyValue, new HashMap(), matchedContainer));\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    String cleanString(String str) {\n" +
                "        if (str == null) return \"\";\n" +
                "        return str.trim().replaceAll(\" +\", \" \").toLowerCase();\n" +
                "    }\n" +
                "\n" +
                "    List transformReferringProviderName(String name, String containerName) {\n" +
                "        List ProviderTransformerOutputItems = new ArrayList();\n" +
                "\n" +
                "        if (name == null || name.trim().isEmpty()) {\n" +
                "            return ProviderTransformerOutputItems;\n" +
                "        }\n" +
                "\n" +
                "        String[] parts = name.trim().split(\"\\\\s+\");\n" +
                "        String firstName, lastName = \"\";\n" +
                "\n" +
                "        if (parts.length > 1) {\n" +
                "            firstName = \"\";\n" +
                "            for (int i = 0; i < parts.length - 1; i++) {\n" +
                "                firstName += parts[i] + \" \";\n" +
                "            }\n" +
                "            firstName = firstName.trim();\n" +
                "            lastName = parts[parts.length - 1];\n" +
                "        } else {\n" +
                "            firstName = parts[0];\n" +
                "        }\n" +
                "        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem(\"referring_provider_last_name\", name, new HashMap(), containerName));\n" +
                "\n" +
                "\n" +
                "        return ProviderTransformerOutputItems;\n" +
                "    }\n" +
                "\n" +
                "    List transformServicingProviderName(String name, String containerName) {\n" +
                "        List ProviderTransformerOutputItems = new ArrayList();\n" +
                "\n" +
                "        if (name == null || name.trim().isEmpty()) {\n" +
                "            return ProviderTransformerOutputItems;\n" +
                "        }\n" +
                "\n" +
                "        String[] parts = name.trim().split(\"\\\\s+\");\n" +
                "        String firstName, lastName = \"\";\n" +
                "\n" +
                "        if (parts.length > 1) {\n" +
                "            firstName = \"\";\n" +
                "            for (int i = 0; i < parts.length - 1; i++) {\n" +
                "                firstName += parts[i] + \" \";\n" +
                "            }\n" +
                "            firstName = firstName.trim();\n" +
                "            lastName = parts[parts.length - 1];\n" +
                "        } else {\n" +
                "            firstName = parts[0];\n" +
                "        }\n" +
                "\n" +
                "        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem(\"servicing_provider_last_name\", name, new HashMap(), containerName));\n" +
                "\n" +
                "        return ProviderTransformerOutputItems;\n" +
                "    }\n" +
                "\n" +
                "    double calculateJaroWinklerDistance(String str1, String str2) {\n" +
                "        double jaroScore = calculateJaroSimilarity(str1, str2);\n" +
                "        int prefixLength = 0;\n" +
                "        int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));\n" +
                "\n" +
                "        for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {\n" +
                "            prefixLength++;\n" +
                "        }\n" +
                "\n" +
                "        return jaroScore + (prefixLength * 0.1 * (1.0 - jaroScore));\n" +
                "    }\n" +
                "\n" +
                "    double calculateJaroSimilarity(String str1, String str2) {\n" +
                "        int len1 = str1.length(), len2 = str2.length();\n" +
                "        int matchDistance = Math.max(len1, len2) / 2 - 1;\n" +
                "        if (matchDistance < 0) matchDistance = 0;\n" +
                "\n" +
                "        boolean[] matches1 = new boolean[len1], matches2 = new boolean[len2];\n" +
                "        int matches = 0, transpositions = 0, k = 0;\n" +
                "\n" +
                "        for (int i = 0; i < len1; i++) {\n" +
                "            int start = Math.max(0, i - matchDistance);\n" +
                "            int end = Math.min(i + matchDistance + 1, len2);\n" +
                "            for (int j = start; j < end; j++) {\n" +
                "                if (!matches2[j] && str1.charAt(i) == str2.charAt(j)) {\n" +
                "                    matches1[i] = matches2[j] = true;\n" +
                "                    matches++;\n" +
                "                    break;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        for (int i = 0; i < len1; i++) {\n" +
                "            if (matches1[i]) {\n" +
                "                while (!matches2[k]) k++;\n" +
                "                if (str1.charAt(i) != str2.charAt(k)) transpositions++;\n" +
                "                k++;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        double m = matches;\n" +
                "        return matches == 0 ? 0.0 : (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;\n" +
                "    }\n" +
                "\n" +
                "    double calculateLCSLength(String str1, String str2) {\n" +
                "        int len1 = str1.length(), len2 = str2.length();\n" +
                "        int[][] dp = new int[len1 + 1][len2 + 1];\n" +
                "\n" +
                "        for (int i = 1; i <= len1; i++) {\n" +
                "            for (int j = 1; j <= len2; j++) {\n" +
                "                dp[i][j] = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? dp[i - 1][j - 1] + 1 :\n" +
                "                        Math.max(dp[i - 1][j], dp[i][j - 1]);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return (double) dp[len1][len2] / Math.max(len1, len2);\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "\n" +
                "    Map metaProviderEntityDetails() {\n" +
                "        Map metaProviderEntityDetails = new HashMap();\n" +
                "        metaProviderEntityDetails.put(\"SERVICING_PROVIDER_DETAILS\", Arrays.asList(new String[]{\"Servicing Provider\", \"physician\", \"Therapist\", \"Attending physician\", \"Accepting physician\", \"Rendering Provider\"}));\n" +
                "        metaProviderEntityDetails.put(\"REFERRING_PROVIDER_DETAILS\", Arrays.asList(new String[]{\"Referring Provider\", \"Requesting Provider\", \"Ordering Provider\"}));\n" +
                "        metaProviderEntityDetails.put(\"SERVICING_FACILITY_DETAILS\", Arrays.asList(new String[]{\"SERVICING_FACILITY_DETAILS\", \"Servicing Facility\", \"Service Facility\", \"Facility\", \"facility name\"}));\n" +
                "        return metaProviderEntityDetails;\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    Map itemMappingDetails() {\n" +
                "        Map itemMappingDetails = new HashMap();\n" +
                "        itemMappingDetails.put(\"SERVICING_PROVIDER_DETAILS\",\n" +
                "                Arrays.asList(new String[]{\"servicing_provider_full_name\", \"servicing_provider_specialty\", \"servicing_provider_npi\",\n" +
                "                        \"servicing_provider_address_line1\", \"servicing_provider_address_line2\", \"servicing_provider_city\",\n" +
                "                        \"servicing_provider_state\", \"servicing_provider_tin\", \"servicing_provider_zipcode\",\"servicing_provider_last_name\"}));\n" +
                "\n" +
                "        itemMappingDetails.put(\"SERVICING_FACILITY_DETAILS\",\n" +
                "                Arrays.asList(new String[]{\"servicing_facility_last_name\", \"servicing_facility_npi\", \"servicing_facility_zipcode\",\n" +
                "                        \"servicing_facility_address_line1\", \"servicing_facility_address_line2\", \"servicing_facility_city\",\n" +
                "                        \"servicing_facility_state\", \"servicing_facility_tin\", \"servicing_facility_specialty\"}));\n" +
                "\n" +
                "        itemMappingDetails.put(\"REFERRING_PROVIDER_DETAILS\",\n" +
                "                Arrays.asList(new String[]{\"referring_provider_address_line1\", \"referring_provider_full_name\", \"referring_provider_npi\",\n" +
                "                        \"referring_provider_city\", \"referring_provider_specialty\", \"referring_provider_state\",\n" +
                "                        \"referring_provider_tin\", \"referring_provider_zipcode\", \"referring_provider_address_line2\",\"referring_provider_last_name\"}));\n" +
                "\n" +
                "        itemMappingDetails.put(\"UNDEFINED_PROVIDER_DETAILS\",\n" +
                "                Arrays.asList(new String[]{\"undefined_provider_address_line_1\", \"undefined_provider_full_name\", \"undefined_provider_npi\",\n" +
                "                        \"undefined_provider_city\", \"undefined_provider_specialty\", \"undefined_provider_state\",\n" +
                "                        \"undefined_provider_tin\", \"undefined_provider_zipcode\", \"undefined_provider_address_line2\"}));\n" +
                "        return itemMappingDetails;\n" +
                "    }\n" +
                "\n" +
                "\n" +
                "    Map nameMappingDetails() {\n" +
                "        Map nameMappingDetails = new HashMap();\n" +
                "\n" +
                "        nameMappingDetails.put(\"referring_provider_type\", \"Provider Type\");\n" +
                "        nameMappingDetails.put(\"referring_provider_npi\", \"Provider NPI\");\n" +
                "        nameMappingDetails.put(\"referring_provider_specialty\", \"Provider Specialty\");\n" +
                "        nameMappingDetails.put(\"referring_provider_tin\", \"Provider Tax ID\");\n" +
                "        nameMappingDetails.put(\"referring_provider_address_line1\", \"Provider Address\");\n" +
                "        nameMappingDetails.put(\"referring_provider_city\", \"Provider City\");\n" +
                "        nameMappingDetails.put(\"referring_provider_state\", \"Provider State\");\n" +
                "        nameMappingDetails.put(\"referring_provider_last_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"referring_provider_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "        nameMappingDetails.put(\"servicing_facility_last_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_type\", \"Provider Type\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_npi\", \"Provider NPI\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_specialty\", \"Provider Specialty\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_tin\", \"Provider Tax ID\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_address_line1\", \"Provider Address\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_city\", \"Provider City\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_state\", \"Provider State\");\n" +
                "        nameMappingDetails.put(\"servicing_facility_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "        nameMappingDetails.put(\"servicing_provider_last_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_type\", \"Provider Type\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_npi\", \"Provider NPI\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_specialty\", \"Provider Specialty\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_tin\", \"Provider Tax ID\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_address_line1\", \"Provider Address\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_city\", \"Provider City\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_state\", \"Provider State\");\n" +
                "        nameMappingDetails.put(\"servicing_provider_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "        nameMappingDetails.put(\"undefined_provider_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_full_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_first_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_last_name\", \"Provider Name\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_type\", \"Provider Type\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_npi\", \"Provider NPI\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_specialty\", \"Provider Specialty\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_tin\", \"Provider Tax ID\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_address_line_1\", \"Provider Address\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_city\", \"Provider City\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_state\", \"Provider State\");\n" +
                "        nameMappingDetails.put(\"undefined_provider_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "        return nameMappingDetails;\n" +
                "    }\n" +
                "\n" +
                "    class ProviderTransformerOutputItem {\n" +
                "        String key;\n" +
                "        String value;\n" +
                "        Map boundingBox;\n" +
                "        double confidence;\n" +
                "        String sorContainerName;\n" +
                "\n" +
                "        public ProviderTransformerOutputItem(String key, String value, Map boundingBox, String sorContainerName) {\n" +
                "            this.key = key;\n" +
                "            this.value = value;\n" +
                "            this.boundingBox = boundingBox;\n" +
                "            this.confidence = 100.0;\n" +
                "            this.sorContainerName = sorContainerName;\n" +
                "        }\n" +
                "\n" +
                "        // Getters and setters\n" +
                "        public Map getBoundingBox() {\n" +
                "            return boundingBox;\n" +
                "        }\n" +
                "\n" +
                "        public double getConfidence() {\n" +
                "            return confidence;\n" +
                "        }\n" +
                "\n" +
                "        public String getKey() {\n" +
                "            return key;\n" +
                "        }\n" +
                "\n" +
                "        public String getValue() {\n" +
                "            return value;\n" +
                "        }\n" +
                "\n" +
                "        public String getSorContainerName() {\n" +
                "            return sorContainerName;\n" +
                "        }\n" +
                "\n" +
                "    }\n" +
                "\n" +
                "}";
        return className;
    }

}