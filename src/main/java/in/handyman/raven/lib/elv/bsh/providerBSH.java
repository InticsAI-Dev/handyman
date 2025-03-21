package in.handyman.raven.lib.elv.bsh;

import bsh.Interpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class providerBSH {
    private static final Logger logger = LoggerFactory.getLogger(providerBSH.class);

    /**
     * BSH script content
     */
    private String getProviderProcessorScript() {
        return "import java.util.*;\n" +
                "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "Logger logger = LoggerFactory.getLogger(\"providerBSH\");\n" +
                "\n" +
                "// OutputItem class\n" +
                "class OutputItem {\n" +
                "    String key;\n" +
                "    String value;\n" +
                "    Map boundingBox;\n" +
                "    double confidence;\n" +
                "\n" +
                "    OutputItem(String key, String value) {\n" +
                "        this.key = key;\n" +
                "        this.value = value;\n" +
                "        this.boundingBox = new HashMap();\n" +
                "        this.confidence = 100.0;\n" +
                "    }\n" +
                "\n" +
                "    Map getBoundingBox() { return boundingBox; }\n" +
                "    double getConfidence() { return confidence; }\n" +
                "    String getKey() { return key; }\n" +
                "    String getValue() { return value; }\n" +
                "}\n" +
                "\n" +
                "// Main processing function\n" +
                "List processProviders(List providers, Map metaProviderEntityDetails, Map itemMappingDetails, Map nameMappingDetails) {\n" +
                "    List result = new ArrayList();\n" +
                "\n" +
                "    for (Map provider : providers) {\n" +
                "        Map providerMap = new HashMap(provider);\n" +
                "        String providerType = (String) providerMap.get(\"Provider Type\");\n" +
                "        String matchedContainer = null;\n" +
                "        Map containerDistanceMap = new HashMap();\n" +
                "\n" +
                "        for (Map.Entry entry : metaProviderEntityDetails.entrySet()) {\n" +
                "            String container = (String) entry.getKey();\n" +
                "            String cleanedProviderType = cleanString(providerType != null ? providerType.toLowerCase() : \"\");\n" +
                "            logger.info(\"length of string : \" + cleanedProviderType.length());\n" +
                "\n" +
                "            for (String value : (List) entry.getValue()) {\n" +
                "                logger.info(providerType.toLowerCase() + \" <--------> \" + value.toLowerCase());\n" +
                "                String cleanedValue = cleanString(value.toLowerCase());\n" +
                "                if (cleanedProviderType.length() >= 10) {\n" +
                "                    double distance = calculateJaroWinklerDistance(cleanedProviderType, cleanedValue);\n" +
                "                    logger.info(\"Calculated JaroWinklerDistance distance: \" + distance);\n" +
                "                    double existingDistance = containerDistanceMap.containsKey(container) ? (Double) containerDistanceMap.get(container) : 0.0;\n" +
                "                    containerDistanceMap.put(container, Math.max(existingDistance, distance));\n" +
                "                } else {\n" +
                "                    double distance = calculateLCSLength(cleanedProviderType, cleanedValue);\n" +
                "                    logger.info(\"Calculated LCSLength distance: \" + distance);\n" +
                "                    double existingDistance = containerDistanceMap.containsKey(container) ? (Double) containerDistanceMap.get(container) : 0.0;\n" +
                "                    containerDistanceMap.put(container, Math.max(existingDistance, distance));\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        logger.info(\"containerDistanceMap.entrySet()----------------->\" + containerDistanceMap.entrySet());\n" +
                "        matchedContainer = \"UNDEFINED_PROVIDER_DETAILS\";\n" +
                "        List sorItemList = (List) itemMappingDetails.getOrDefault(matchedContainer, Collections.emptyList());\n" +
                "\n" +
                "        logger.info(\"Matched Container: \" + matchedContainer);\n" +
                "        for (String item : sorItemList) {\n" +
                "            String mappedKey = (String) nameMappingDetails.get(item);\n" +
                "            logger.info(\"Item: \" + item + \" -> Mapped Key Value: \" + mappedKey);\n" +
                "            if (mappedKey != null) {\n" +
                "                String keyValue = (String) providerMap.get(mappedKey);\n" +
                "                if (keyValue != null) {\n" +
                "                    if (matchedContainer.equals(\"UNDEFINED_PROVIDER_DETAILS\")) {\n" +
                "                        OutputItem existingItem = null;\n" +
                "                        for (Object o : result) {\n" +
                "                            OutputItem itemObj = (OutputItem) o;\n" +
                "                            if (itemObj.getKey().equals(item)) {\n" +
                "                                existingItem = itemObj;\n" +
                "                                break;\n" +
                "                            }\n" +
                "                        }\n" +
                "                        if (existingItem != null) {\n" +
                "                            if (!existingItem.value.isEmpty()) {\n" +
                "                                existingItem.value += \", \";\n" +
                "                            }\n" +
                "                            existingItem.value += \"\\\"\" + keyValue + \"\\\"\";\n" +
                "                        } else {\n" +
                "                            if (keyValue.trim().isEmpty()) {\n" +
                "                                result.add(new OutputItem(item, \"\\\"\\\"\"));\n" +
                "                            } else {\n" +
                "                                result.add(new OutputItem(item, \"\\\"\" + keyValue + \"\\\"\"));\n" +
                "                            }\n" +
                "                        }\n" +
                "                    } else {\n" +
                "                        OutputItem output = new OutputItem(item, keyValue);\n" +
                "                        result.add(output);\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    return result;\n" +
                "}\n" +
                "\n" +
                "// Helper methods\n" +
                "String cleanString(String str) {\n" +
                "    if (str == null) return \"\";\n" +
                "    String trimmed = str.trim();\n" +
                "    while (trimmed.contains(\"  \")) {\n" +
                "        trimmed = trimmed.replace(\"  \", \" \");\n" +
                "    }\n" +
                "    return trimmed.toLowerCase();\n" +
                "}\n" +
                "\n" +
                "double calculateJaroSimilarity(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) return 0.0;\n" +
                "    if (str1.isEmpty() && str2.isEmpty()) return 1.0;\n" +
                "    if (str1.isEmpty() || str2.isEmpty()) return 0.0;\n" +
                "\n" +
                "    int len1 = str1.length();\n" +
                "    int len2 = str2.length();\n" +
                "    int matchDistance = Math.max(len1, len2) / 2 - 1;\n" +
                "\n" +
                "    boolean[] matches1 = new boolean[len1];\n" +
                "    boolean[] matches2 = new boolean[len2];\n" +
                "    int matches = 0;\n" +
                "\n" +
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
                "\n" +
                "    if (matches == 0) return 0.0;\n" +
                "\n" +
                "    int transpositions = 0;\n" +
                "    int k = 0;\n" +
                "    for (int i = 0; i < len1; i++) {\n" +
                "        if (matches1[i]) {\n" +
                "            while (!matches2[k]) k++;\n" +
                "            if (str1.charAt(i) != str2.charAt(k)) transpositions++;\n" +
                "            k++;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    double m = matches;\n" +
                "    return (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;\n" +
                "}\n" +
                "\n" +
                "double calculateJaroWinklerDistance(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) return 0.0;\n" +
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
                "int calculateLCSLength(String str1, String str2) {\n" +
                "    if (str1 == null || str2 == null) return 0;\n" +
                "    int m = str1.length();\n" +
                "    int n = str2.length();\n" +
                "    int[][] dp = new int[m + 1][n + 1];\n" +
                "\n" +
                "    for (int i = 1; i <= m; i++) {\n" +
                "        for (int j = 1; j <= n; j++) {\n" +
                "            if (str1.charAt(i - 1) == str2.charAt(j - 1)) {\n" +
                "                dp[i][j] = dp[i - 1][j - 1] + 1;\n" +
                "            } else {\n" +
                "                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    return dp[m][n];\n" +
                "}\n" +
                "\n" +
                "// Return the result\n" +
                "return processProviders(providers, metaProviderEntityDetails, itemMappingDetails, nameMappingDetails);\n";
    }

    /**
     * Execute the BSH script
     */
    public List<OutputItem> executeProcessing(
            List<Map<String, String>> providers,
            Map<String, List<String>> metaProviderEntityDetails,
            Map<String, List<String>> itemMappingDetails,
            Map<String, String> nameMappingDetails) {
        try {
            String script = getProviderProcessorScript();
            logger.info("Loaded ProviderProcessor BSH script");

            Interpreter interpreter = new Interpreter();
            interpreter.eval(script);

            // Set input variables
            interpreter.set("providers", providers);
            interpreter.set("metaProviderEntityDetails", metaProviderEntityDetails);
            interpreter.set("itemMappingDetails", itemMappingDetails);
            interpreter.set("nameMappingDetails", nameMappingDetails);

            // Execute and get result
            Object resultObject = interpreter.get("returnValue"); // BSH sets this implicitly with 'return'
            if (resultObject instanceof List) {
                List<?> rawResult = (List<?>) resultObject;
                List<OutputItem> typedResult = new ArrayList<>();
                for (Object item : rawResult) {
                    if (item != null) {
                        String key = (String) interpreter.eval("((OutputItem)" + item + ").getKey()");
                        String value = (String) interpreter.eval("((OutputItem)" + item + ").getValue()");
                        Map boundingBox = (Map) interpreter.eval("((OutputItem)" + item + ").getBoundingBox()");
                        Double confidence = (Double) interpreter.eval("((OutputItem)" + item + ").getConfidence()");

                        OutputItem outputItem = new OutputItem(key, value);
                        if (boundingBox != null) outputItem.boundingBox = boundingBox;
                        if (confidence != null) outputItem.confidence = confidence;
                        typedResult.add(outputItem);
                    }
                }
                logger.info("Successfully executed BSH processing");
                return typedResult;
            } else {
                logger.error("Result is not a List: {}", resultObject);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error("Error executing BeanShell script: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // Java-defined OutputItem class for type safety
    public static class OutputItem {
        String key;
        String value;
        Map<String, Object> boundingBox;
        double confidence;

        public OutputItem(String key, String value) {
            this.key = key;
            this.value = value;
            this.boundingBox = new HashMap<>();
            this.confidence = 100.0;
        }

        public Map<String, Object> getBoundingBox() { return boundingBox; }
        public double getConfidence() { return confidence; }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    // Main method for testing
    public static void main(String[] args) {
        providerBSH processor = new providerBSH();

        List<Map<String, String>> providers = new ArrayList<>();
        Map<String, String> provider1 = new HashMap<>();
        provider1.put("Provider Type", "Current primary care physician/attending physician");
        provider1.put("Provider Name", "Theodore Richards");
        provider1.put("Provider NPI", "1891784518");
        providers.add(provider1);

        Map<String, String> provider2 = new HashMap<>();
        provider2.put("Provider Type", "Heart specialist");
        provider2.put("Provider Name", "Timothy Talbert");
        provider2.put("Provider NPI", "");
        providers.add(provider2);

        Map<String, List<String>> metaProviderEntityDetails = new HashMap<>();
        metaProviderEntityDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList("servicing_provider_name", "provider_npi"));
        metaProviderEntityDetails.put("REFERRING_PROVIDER_DETAILS", Arrays.asList("provider_synonym_1", "provider_synonym_2"));
        metaProviderEntityDetails.put("SERVICING_FACILITY_DETAILS", Arrays.asList(" ", " "));
        metaProviderEntityDetails.put("UNDEFINED_PROVIDER_DETAILS", Arrays.asList(" ", " "));

        Map<String, List<String>> itemMappingDetails = new HashMap<>();
        itemMappingDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList("servicing_provider_name", "provider_item_2", "provider_item_3"));
        itemMappingDetails.put("UNDEFINED_PROVIDER_DETAILS", Arrays.asList("provider_item_1", "provider_item_2", "provider_item_3"));

        Map<String, String> nameMappingDetails = new HashMap<>();
        nameMappingDetails.put("servicing_provider_name", "Provider Name");
        nameMappingDetails.put("provider_type", "Provider Type");
        nameMappingDetails.put("provider_npi", "Provider NPI");

        List<OutputItem> result = processor.executeProcessing(providers, metaProviderEntityDetails, itemMappingDetails, nameMappingDetails);

        for (OutputItem item : result) {
            System.out.printf("key: %s, value: %s, confidence: %.1f%n",
                    item.getKey(), item.getValue(), item.getConfidence());
        }
    }
}