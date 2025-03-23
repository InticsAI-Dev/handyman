package in.handyman.raven.lib.bsh.latest;

import bsh.Interpreter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Slf4j
public class ProviderBshLatestOne {
    private static final Logger logger = LoggerFactory.getLogger(ProviderBshLatestOne.class);

    /**
     * Script content that will be evaluated by BeanShell
     */
    private String getProviderProcessorScript() {
        return "import java.util.*;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "\n" +
                "public class ProcessProvider {\n" +
                "    private Logger logger;\n" +
                "\n" +
                "    public ProcessProvider(Logger logger) {\n" +
                "    this.logger = logger;\n" +
                "    }\n" +
                "\n" +
                "    public List processProviders(List providers) {\n" +
                "        logger.info(\"Starting processProviders execution\");\n" +
                "        List result = new Vector();\n" +
                "        Map metaProviderEntityDetails = metaProviderEntityDetails();\n" +
                "        Map itemMappingDetails = itemMappingDetails();\n" +
                "        Map nameMappingDetails = nameMappingDetails();\n" +
                "\n" +
                "        for (int i = 0; i < providers.size(); i++) {\n" +
                "            Map provider = (Map) providers.get(i);\n" +
                "            Map providerMap = new Hashtable(provider);\n" +
                "            String providerType = (String) providerMap.get(\"Provider Type\");\n" +
                "            String cleanedProviderType = cleanString(providerType != null ? providerType.toLowerCase() : \"\");\n" +
                "            String matchedContainer = null;\n" +
                "            String cleanedValue = \"\";\n" +
                "            Map containerDistanceMap = new Hashtable();\n" +
                "            boolean exactMatchFound = false;\n" +
                "\n" +
                "            Iterator it = metaProviderEntityDetails.entrySet().iterator();\n" +
                "            while (it.hasNext()) {\n" +
                "                Map.Entry entry = (Map.Entry) it.next();\n" +
                "                String container = (String) entry.getKey();\n" +
                "                logger.info(\"length of string : \" + cleanedProviderType.length());\n" +
                "                List values = (List) entry.getValue();\n" +
                "                \n" +
                "                for (int j = 0; j < values.size(); j++) {\n" +
                "                    String value = (String) values.get(j);\n" +
                "                    cleanedValue = cleanString(value);\n" +
                "                    boolean checkForContainerMatch = cleanedProviderType.contains(cleanedValue);\n" +
                "                    \n" +
                "                    if (checkForContainerMatch) {\n" +
                "                        logger.info(\"Executing exact match\");\n" +
                "                        matchedContainer = container;\n" +
                "                        exactMatchFound = true;\n" +
                "                        break;\n" +
                "                    } else {\n" +
                "                        logger.info(cleanedProviderType + \" : \" + value.toLowerCase());\n" +
                "                        cleanedValue = cleanString(value.toLowerCase());\n" +
                "                        double distance1 = calculateJaroWinklerDistance(cleanedProviderType, cleanedValue);\n" +
                "                        logger.info(\"Calculated JaroWinklerDistance distance: {}\", distance1);\n" +
                "                        double distance2 = calculateLCSLength(cleanedProviderType, cleanedValue);\n" +
                "                        logger.info(\"Calculated LCSLength distance: {}\", distance2);\n" +
                "                        double distance = (distance1 + distance2) / 2;\n" +
                "                        Double currentDistance = (Double) containerDistanceMap.get(container);\n" +
                "                        if (currentDistance == null || distance > currentDistance.doubleValue()) {\n" +
                "                            containerDistanceMap.put(container, new Double(distance));\n" +
                "                        }\n" +
                "                        logger.info(\"Calculated average distance: {}\", distance);\n" +
                "                    }\n" +
                "                }\n" +
                "                if (exactMatchFound) break;\n" +
                "            }\n" +
                "\n" +
                "            if (!exactMatchFound) {\n" +
                "                double maxDistance = findMaxValue(containerDistanceMap);\n" +
                "                logger.info(\"maxDistance : \" + maxDistance);\n" +
                "                if (maxDistance > 0.7) {\n" +
                "                    logger.info(\"containerDistanceMap.entrySet() :\" + containerDistanceMap.entrySet());\n" +
                "                    matchedContainer = findMaxValueKey(containerDistanceMap);\n" +
                "                } else {\n" +
                "                    matchedContainer = \"UNDEFINED_PROVIDER_DETAILS\";\n" +
                "                    logger.info(\"matchedContainer :\" + matchedContainer);\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            List sorItemList = (List) itemMappingDetails.get(matchedContainer);\n" +
                "            if (sorItemList == null) sorItemList = Collections.EMPTY_LIST;\n" +
                "            logger.info(\"Matched Container: {}\", matchedContainer);\n" +
                "\n" +
                "            for (int k = 0; k < sorItemList.size(); k++) {\n" +
                "                String item = (String) sorItemList.get(k);\n" +
                "                String mappedKey = (String) nameMappingDetails.get(item);\n" +
                "                logger.info(\"Item: \" + item + \" -> Mapped Key Value: \" + mappedKey);\n" +
                "\n" +
                "                if (mappedKey != null) {\n" +
                "                    String keyValue = (String) providerMap.get(mappedKey);\n" +
                "                    if (keyValue != null) {\n" +
                "                        Map outputItem = new Hashtable();\n" +
                "                        outputItem.put(\"key\", item);\n" +
                "                        if (\"UNDEFINED_PROVIDER_DETAILS\".equals(matchedContainer)) {\n" +
                "                            Map existingItem = findExistingItem(result, item);\n" +
                "                            if (existingItem != null) {\n" +
                "                                String currentValue = (String) existingItem.get(\"value\");\n" +
                "                                if (!currentValue.isEmpty()) currentValue += \", \";\n" +
                "                                currentValue += \"\\\"\" + keyValue + \"\\\"\";\n" +
                "                                existingItem.put(\"value\", currentValue);\n" +
                "                            } else {\n" +
                "                                String value = keyValue.trim().isEmpty() ? \"\\\"\\\"\" : \"\\\"\" + keyValue + \"\\\"\";\n" +
                "                                outputItem.put(\"value\", value);\n" +
                "                                outputItem.put(\"boundingBox\", new Hashtable());\n" +
                "                                outputItem.put(\"confidence\", new Double(100.0));\n" +
                "                                outputItem.put(\"sorContainerName\", matchedContainer);\n" +
                "                                result.add(outputItem);\n" +
                "                            }\n" +
                "                        } else {\n" +
                "                            outputItem.put(\"value\", keyValue);\n" +
                "                            outputItem.put(\"boundingBox\", new Hashtable());\n" +
                "                            outputItem.put(\"confidence\", new Double(100.0));\n" +
                "                            outputItem.put(\"sorContainerName\", matchedContainer);\n" +
                "                            result.add(outputItem);\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        logger.info(\"processProviders completed with result size: {}\", result.size());\n" +
                "        return result;\n" +
                "    }\n" +
                "\n" +
                "    private Map metaProviderEntityDetails() {\n" +
                "    Map details = new Hashtable();\n" +
                "    List servicingProviderValues = new Vector();\n" +
                "    servicingProviderValues.add(\"servicing provider\");\n" +
                "    servicingProviderValues.add(\"physician\");\n" +
                "    servicingProviderValues.add(\"Therapist\");\n" +
                "    servicingProviderValues.add(\"Attending physician\");\n" +
                "    servicingProviderValues.add(\"Accepting physician\");\n" +
                "    servicingProviderValues.add(\"Rendering Provider\");\n" +
                "    details.put(\"SERVICING_PROVIDER_DETAILS\", servicingProviderValues);\n" +
                "    List referringProviderValues = new Vector();\n" +
                "    referringProviderValues.add(\"Referring Provider\");\n" +
                "    referringProviderValues.add(\"Requesting Provider\");\n" +
                "    referringProviderValues.add(\"Ordering Provider\");\n" +
                "    details.put(\"REFERRING_PROVIDER_DETAILS\", referringProviderValues);\n" +
                "    return details;\n" +
                "}\n" +
                "\n" +
                "    private Map itemMappingDetails() {\n" +
                "    Map details = new Hashtable();\n" +
                "    List servicingProviderItems = new Vector();\n" +
                "    servicingProviderItems.add(\"servicing_provider_name\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_npi\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_Specialty\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_tin\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_address\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_city\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_state\");\n" +
                "    servicingProviderItems.add(\"servicing_provider_zip\");\n" +
                "    details.put(\"SERVICING_PROVIDER_DETAILS\", servicingProviderItems);\n" +
                "    List referringProviderItems = new Vector();\n" +
                "    referringProviderItems.add(\"referring_provider_name\");\n" +
                "    referringProviderItems.add(\"referring_provider_npi\");\n" +
                "    referringProviderItems.add(\"referring_provider_Specialty\");\n" +
                "    referringProviderItems.add(\"referring_provider_tin\");\n" +
                "    referringProviderItems.add(\"referring_provider_address\");\n" +
                "    referringProviderItems.add(\"referring_provider_city\");\n" +
                "    referringProviderItems.add(\"referring_provider_state\");\n" +
                "    referringProviderItems.add(\"referring_provider_zip\");\n" +
                "    details.put(\"REFERRING_PROVIDER_DETAILS\", referringProviderItems);\n" +
                "    return details;\n" +
                "}\n" +
                "\n" +
                "    private Map nameMappingDetails() {\n" +
                "    Map details = new Hashtable();\n" +
                "    details.put(\"servicing_provider_name\", \"Provider Name\");\n" +
                "    details.put(\"servicing_provider_type\", \"Provider Type\");\n" +
                "    details.put(\"servicing_provider_npi\", \"Provider NPI\");\n" +
                "    details.put(\"servicing_provider_specialty\", \"Provider Specialty\");\n" +
                "    details.put(\"servicing_provider_tin\", \"Provider Tax ID\");\n" +
                "    details.put(\"servicing_provider_address_line1\", \"Provider Address\");\n" +
                "    details.put(\"servicing_provider_city\", \"Provider City\");\n" +
                "    details.put(\"servicing_provider_state\", \"Provider State\");\n" +
                "    details.put(\"servicing_provider_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "    details.put(\"referring_provider_name\", \"Provider Name\");\n" +
                "    details.put(\"referring_provider_type\", \"Provider Type\");\n" +
                "    details.put(\"referring_provider_npi\", \"Provider NPI\");\n" +
                "    details.put(\"referring_provider_specialty\", \"Provider Specialty\");\n" +
                "    details.put(\"referring_provider_tin\", \"Provider Tax ID\");\n" +
                "    details.put(\"referring_provider_address_line1\", \"Provider Address\");\n" +
                "    details.put(\"referring_provider_city\", \"Provider City\");\n" +
                "    details.put(\"referring_provider_state\", \"Provider State\");\n" +
                "    details.put(\"referring_provider_zipcode\", \"Provider ZIP Code\");\n" +
                "\n" +
                "    details.put(\"undefined_provider_name\", \"Provider Name\");\n" +
                "    details.put(\"undefined_provider_type\", \"Provider Type\");\n" +
                "    details.put(\"undefined_provider_npi\", \"Provider NPI\");\n" +
                "    details.put(\"undefined_provider_specialty\", \"Provider Specialty\");\n" +
                "    details.put(\"undefined_provider_tin\", \"Provider Tax ID\");\n" +
                "    details.put(\"undefined_provider_address\", \"Provider Address\");\n" +
                "    details.put(\"undefined_provider_city\", \"Provider City\");\n" +
                "    details.put(\"undefined_provider_state\", \"Provider State\");\n" +
                "    details.put(\"undefined_provider_zip\", \"Provider ZIP Code\");\n" +
                "    return details;\n" +
                "}\n" +
                "\n" +
                "    Map findExistingItem(List items, String key) {\n" +
                "    for (int i = 0; i < items.size(); i++) {\n" +
                "        Map item = (Map) items.get(i);\n" +
                "        if (item.get(\"key\").equals(key)) return item;\n" +
                "    }\n" +
                "    return null;\n" +
                "}\n" +
                "\n" +
                "double findMaxValue(Map map) {\n" +
                "    if (map.isEmpty()) return 0.0;\n" +
                "    double maxValue = 0.0;\n" +
                "    Iterator it = map.entrySet().iterator();\n" +
                "    while (it.hasNext()) {\n" +
                "        Map.Entry entry = (Map.Entry) it.next();\n" +
                "        Double value = (Double) entry.getValue();\n" +
                "        if (value != null && value.doubleValue() > maxValue) {\n" +
                "            maxValue = value.doubleValue();\n" +
                "        }\n" +
                "    }\n" +
                "    return maxValue;\n" +
                "}\n" +
                "\n" +
                "String findMaxValueKey(Map map) {\n" +
                "    if (map.isEmpty()) return null;\n" +
                "    double maxValue = 0.0;\n" +
                "    String maxKey = null;\n" +
                "    Iterator it = map.entrySet().iterator();\n" +
                "    while (it.hasNext()) {\n" +
                "        Map.Entry entry = (Map.Entry) it.next();\n" +
                "        Double value = (Double) entry.getValue();\n" +
                "        if (value != null && value.doubleValue() > maxValue) {\n" +
                "            maxValue = value.doubleValue();\n" +
                "            maxKey = (String) entry.getKey();\n" +
                "        }\n" +
                "    }\n" +
                "    return maxKey;\n" +
                "}\n" +
                "\n" +
                "String cleanString(String str) {\n" +
                "    if (str == null) return \"\";\n" +
                "    String trimmed = str.trim();\n" +
                "    while (trimmed.contains(\"  \")) trimmed = trimmed.replace(\"  \", \" \");\n" +
                "    return trimmed.toLowerCase();\n" +
                "}\n" +
                "\n" +
                "double calculateJaroSimilarity(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) throw new IllegalArgumentException(\"Strings must not be null\");\n" +
                "    if (str1.isEmpty() && str2.isEmpty()) return 1.0;\n" +
                "    if (str1.isEmpty() || str2.isEmpty()) return 0.0;\n" +
                "    int len1 = str1.length();\n" +
                "    int len2 = str2.length();\n" +
                "    int matchDistance = Math.max(len1, len2) / 2 - 1;\n" +
                "    boolean[] matches1 = new boolean[len1];\n" +
                "    boolean[] matches2 = new boolean[len2];\n" +
                "    int matches = 0;\n" +
                "    for (int i = 0; i < len1; i++) {\n" +
                "        int start = Math.max(0, i - matchDistance);\n" +
                "        int end = Math.min(i + matchDistance + 1, len2);\n" +
                "        for (int j = start; j < end; j++) {\n" +
                "            if (!matches2[j] && str1.charAt(i) == str2.charAt(j)) {\n" +
                "                matches1[i] = true;\n" +
                "                matches2[j] = true;\n" +
                "                matches++;\n" +
                "                break;\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    if (matches == 0) return 0.0;\n" +
                "    int transpositions = 0;\n" +
                "    int k = 0;\n" +
                "    for (int i = 0; i < len1; i++) {\n" +
                "        if (matches1[i]) {\n" +
                "            while (!matches2[k]) k++;\n" +
                "            if (str1.charAt(i) != str2.charAt(k)) transpositions++;\n" +
                "            k++;\n" +
                "        }\n" +
                "    }\n" +
                "    double m = matches;\n" +
                "    return (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;\n" +
                "}\n" +
                "\n" +
                "double calculateJaroWinklerDistance(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) throw new IllegalArgumentException(\"Strings must not be null\");\n" +
                "    double jaroScore = calculateJaroSimilarity(str1, str2);\n" +
                "    int prefixLength = 0;\n" +
                "    int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));\n" +
                "    for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {\n" +
                "        prefixLength++;\n" +
                "    }\n" +
                "    double p = 0.1;\n" +
                "    return jaroScore + (prefixLength * p * (1.0 - jaroScore));\n" +
                "}\n" +
                "\n" +
                "double calculateLCSLength(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) throw new IllegalArgumentException(\"Strings must not be null\");\n" +
                "    int len1 = str1.length();\n" +
                "    int len2 = str2.length();\n" +
                "    int[][] dp = new int[len1 + 1][len2 + 1];\n" +
                "    for (int i = 1; i <= len1; i++) {\n" +
                "        for (int j = 1; j <= len2; j++) {\n" +
                "            if (str1.charAt(i - 1) == str2.charAt(j - 1)) {\n" +
                "                dp[i][j] = dp[i - 1][j - 1] + 1;\n" +
                "            } else {\n" +
                "                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    int lcsLength = dp[len1][len2];\n" +
                "    int maxLength = Math.max(len1, len2);\n" +
                "    return maxLength > 0 ? (double) lcsLength / maxLength : 0.0;\n" +
                "}\n" +
                "}\n";
    }

    public List executeProviderProcessing() {
        try {
            // Create mock input data
            List providers = new Vector();

            Map provider1 = new Hashtable();
            provider1.put("Provider Type", "welcome to anthem healthcare");
            provider1.put("Provider Name", "Theodore Richards");
            provider1.put("Provider NPI", "1891784518");
            provider1.put("Provider Specialty", "");
            provider1.put("Provider Tax ID", "672140448");
            provider1.put("Provider Address", "");
            provider1.put("Provider City", "");
            provider1.put("Provider State", "");
            provider1.put("Provider ZIP Code", "");

            Map provider2 = new Hashtable();
            provider2.put("Provider Type", "physician specialist");
            provider2.put("Provider Name", "Timothy Talbert");
            provider2.put("Provider NPI", "");
            provider2.put("Provider Specialty", "");
            provider2.put("Provider Tax ID", "");
            provider2.put("Provider Address", "835 Spring Creek Rd Ste 100, CHATTANOOGA,TN,37412-3994");
            provider2.put("Provider City", "CHATTANOOGA");
            provider2.put("Provider State", "TN");
            provider2.put("Provider ZIP Code", "37412-3994");

            Map provider3 = new Hashtable();
            provider3.put("Provider Type", "servicing provider");
            provider3.put("Provider Name", "Theodore Richards");
            provider3.put("Provider NPI", "1891784518");
            provider3.put("Provider Specialty", "");
            provider3.put("Provider Tax ID", "672140448");
            provider3.put("Provider Address", "");
            provider3.put("Provider City", "");
            provider3.put("Provider State", "");
            provider3.put("Provider ZIP Code", "");

            Map provider4 = new Hashtable();
            provider4.put("Provider Type", "child");
            provider4.put("Provider Name", "Theodore Richards");
            provider4.put("Provider NPI", "1891784518");
            provider4.put("Provider Specialty", "");
            provider4.put("Provider Tax ID", "672140448");
            provider4.put("Provider Address", "");
            provider4.put("Provider City", "NEW YORK");
            provider4.put("Provider State", "");
            provider4.put("Provider ZIP Code", "");

            providers.add(provider1);
            providers.add(provider2);
            providers.add(provider3);
            providers.add(provider4);

            // Get the script
            String sourceCode = getProviderProcessorScript();
            logger.info("Starting provider processing with mock data");

            // Initialize the BeanShell interpreter
            Interpreter interpreter = new Interpreter();

            // Set variables before evaluating the script


            // Evaluate the script
            interpreter.eval(sourceCode);
            interpreter.set("logger",log);
            String classInstantiation = "ProcessProvider" + " mapper = new " + "ProcessProvider" + "(logger);";
            interpreter.eval(classInstantiation);
            interpreter.set("providers", providers);
            interpreter.eval("providerMap = mapper.processProviders(providers);");
            Object providerMapObject = interpreter.get("providerMap");


            if (providerMapObject instanceof List) {
                List resultList = (List) providerMapObject;
                logger.info("Provider processing completed successfully with result size: {}", resultList.size());

                // Convert result to JSON and log it
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String jsonOutput = objectMapper.writeValueAsString(resultList);
                logger.info("final output\n" + jsonOutput);

                // Print as JsonNode
                JsonNode res = objectMapper.readTree(jsonOutput);
                System.out.println(res);

                return resultList;
            } else {
                logger.error("Result is not a List: {}", providerMapObject);
                return new Vector();
            }
        } catch (Exception e) {
            logger.error("Error executing BeanShell script: {}", e.getMessage(), e);
            return new Vector();
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        ProviderBshLatestOne executor = new ProviderBshLatestOne();
        List result = executor.executeProviderProcessing();
        System.out.println("Final Result List: " + result);
    }
}
