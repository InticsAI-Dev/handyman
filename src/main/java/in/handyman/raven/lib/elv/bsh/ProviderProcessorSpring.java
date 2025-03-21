package in.handyman.raven.lib.elv.bsh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProviderProcessorSpring {
    private static final Logger logger = LoggerFactory.getLogger(ProviderProcessorSpring.class);


    // Class to represent the output structure
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

        // Getters and setters
        public Map<String, Object> getBoundingBox() {
            return boundingBox;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static List<OutputItem> processProviders(
            List<Map<String, String>> providers,
            Map<String, List<String>> metaProviderEntityDetails,
            Map<String, List<String>> itemMappingDetails,
            Map<String, String> nameMappingDetails) {

        List<OutputItem> result = new ArrayList<>();

        // Process each provider in the response
        for (Map<String, String> provider : providers) {
            // Convert provider to Map<String, String>
            Map<String, String> providerMap = new HashMap<>(provider);

            // Get the Provider Type value
            String providerType = providerMap.get("Provider Type");
            String cleanedProviderType = cleanString(providerType.toLowerCase());

            String matchedContainer = null;
            String cleanedValue = "";
            String container = "";

            Map<String, Double> containerDistanceMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : metaProviderEntityDetails.entrySet()) {

                container = entry.getKey();
                logger.info("length of string : " + (cleanedProviderType.length()) + "\n");

                for (String value : entry.getValue()) {
                    logger.info(providerType.toLowerCase() + " <--------> " + value.toLowerCase());
                    cleanedValue = cleanString(value.toLowerCase());

                    double distance1 = calculateJaroWinklerDistance(cleanedProviderType, cleanedValue);
                    logger.info("Calculated JaroWinklerDistance distance: {}", distance1);
                    double distance2 = calculateLCSLength(cleanedProviderType, cleanedValue);
                    logger.info("Calculated LCSLength distance:{}", distance2);
                    double distance = (distance1+distance2)/2;
                    containerDistanceMap.merge(container, distance, Math::max);
                    logger.info("Calculated average distance:{}", distance);
                }
            }
            if(cleanedProviderType.contains(cleanedValue)){
                logger.info("Executing exact match");
                matchedContainer = container;
            }else{
                logger.info("containerDistanceMap.entrySet()----------------->" + containerDistanceMap.entrySet() + "\n");
                matchedContainer = Collections.max(containerDistanceMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            }
            List<String> sorItemList = itemMappingDetails.getOrDefault(matchedContainer, Collections.emptyList());


            // Step 3: Iterate through all items in sorItemList
            logger.info("Matched Container: " + matchedContainer);
            for (String item : sorItemList) {
                // Step 4: Get corresponding mapped key from nameMappingDetails
                String mappedKey = nameMappingDetails.get(item);
                logger.info("Item: " + item + " -> Mapped Key Value: " + mappedKey);
                if (mappedKey != null) {
                    // Get the value from providerMap
                    String keyValue = providerMap.get(mappedKey);
                    if (keyValue != null) {

                        if (Objects.equals(matchedContainer, "UNDEFINED_PROVIDER_DETAILS")) {
                            Optional<OutputItem> existingItem = result.stream()
                                    .filter(o -> o.getKey().equals(item))
                                    .findFirst();

                            if (existingItem.isPresent()) {
                                // Only add keyValue if it's not null
                                if (!existingItem.get().value.isEmpty()) {
                                    existingItem.get().value += ", ";
                                }
                                existingItem.get().value += "\"" + keyValue + "\"";

                            } else {
                                // Ensure an empty value is added as "\"\", \"\""
                                if (keyValue.trim().isEmpty()) {
                                    result.add(new OutputItem(item, "\"\""));
                                } else {
                                    result.add(new OutputItem(item, "\"" + keyValue + "\""));
                                }
                            }

                        } else {
                            // Create output item
                            OutputItem output = new OutputItem(item, keyValue);
                            result.add(output);
                        }


                    }

                }
            }


        }

        return result;
    }

    private static String cleanString(String str) {
        if (str == null) return "";
        String trimmed = str.trim();
        while (trimmed.contains("  ")) {
            trimmed = trimmed.replace("  ", " ");
        }
        return trimmed.toLowerCase();
    }

    public static double calculateJaroSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) throw new IllegalArgumentException("Strings must not be null");
        if (str1.isEmpty() && str2.isEmpty()) return 1.0;
        if (str1.isEmpty() || str2.isEmpty()) return 0.0;

        int len1 = str1.length();
        int len2 = str2.length();
        int matchDistance = Math.max(len1, len2) / 2 - 1;

        boolean[] matches1 = new boolean[len1];
        boolean[] matches2 = new boolean[len2];
        int matches = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (!matches2[j] && str1.charAt(i) == str2.charAt(j)) {
                    matches1[i] = true;
                    matches2[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) return 0.0;

        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < len1; i++) {
            if (matches1[i]) {
                while (!matches2[k]) k++;
                if (str1.charAt(i) != str2.charAt(k)) transpositions++;
                k++;
            }
        }

        double m = matches;
        return (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;
    }

    public static double calculateJaroWinklerDistance(String str1, String str2) {
        if (str1 == null || str2 == null) throw new IllegalArgumentException("Strings must not be null");

        double jaroScore = calculateJaroSimilarity(str1, str2);
        int prefixLength = 0;
        int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));
        for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {
            prefixLength++;
        }

        double p = 0.1; // Winkler scaling factor
        return jaroScore + (prefixLength * p * (1.0 - jaroScore));
    }


    // 5. Longest Common Subsequence (LCS) Length
    public static int calculateLCSLength(String str1, String str2) {
        if (str1 == null || str2 == null) throw new IllegalArgumentException("Strings must not be null");

        int m = str1.length();
        int n = str2.length();
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }


    public static void main(String[] args) throws JsonProcessingException, JsonProcessingException {
        // Sample data setup
        List<Map<String, String>> providers = new ArrayList<>();

        // Provider 1
        Map<String, String> provider1 = new HashMap<>();
        provider1.put("Provider Type", "Current");
        provider1.put("Provider Name", "Theodore Richards");
        provider1.put("Provider NPI", "1891784518");
        provider1.put("Provider Specialty", "");
        provider1.put("Provider Tax ID", "672140448");
        provider1.put("Provider Address", "");
        provider1.put("Provider City", "");
        provider1.put("Provider State", "");
        provider1.put("Provider ZIP Code", "");

        Map<String, String> provider2 = new HashMap<>();
        provider2.put("Provider Type", "physician specialist");
        provider2.put("Provider Name", "Timothy Talbert");
        provider2.put("Provider NPI", "");
        provider2.put("Provider Specialty", "");
        provider2.put("Provider Tax ID", "");
        provider2.put("Provider Address", "835 Spring Creek Rd Ste 100, CHATTANOOGA,TN,37412-3994");
        provider2.put("Provider City", "CHATTANOOGA");
        provider2.put("Provider State", "TN");
        provider2.put("Provider ZIP Code", "37412-3994");

        Map<String, String> provider3 = new HashMap<>();
        provider3.put("Provider Type", "servicing provider");
        provider3.put("Provider Name", "Theodore Richards");
        provider3.put("Provider NPI", "1891784518");
        provider3.put("Provider Specialty", "");
        provider3.put("Provider Tax ID", "672140448");
        provider3.put("Provider Address", "");
        provider3.put("Provider City", "");
        provider3.put("Provider State", "");
        provider3.put("Provider ZIP Code", "");

        Map<String, String> provider4 = new HashMap<>();
        provider4.put("Provider Type", "child");
        provider4.put("Provider Name", "Theodore Richards");
        provider4.put("Provider NPI", "1891784518");
        provider4.put("Provider Specialty", "");
        provider4.put("Provider Tax ID", "672140448");
        provider4.put("Provider Address", "");
        provider4.put("Provider City", "NEW YORK");
        provider4.put("Provider State", "");
        provider4.put("Provider ZIP Code", "");

//        providers.add(provider1);
//        providers.add(provider2);
        providers.add(provider3);
//        providers.add(provider4);

        // metaProviderEntityDetails
        Map<String, List<String>> metaProviderEntityDetails = new HashMap<>();
        metaProviderEntityDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList("servicing provider","physician","Therapist","Attending physician","Accepting physician","Rendering Provider"));
        metaProviderEntityDetails.put("REFERRING_PROVIDER_DETAILS", Arrays.asList("Referring Provider","Requesting Provider","Ordering Provider"));
//        metaProviderEntityDetails.put("SERVICING_FACILITY_DETAILS", Arrays.asList(" ", " "));
        metaProviderEntityDetails.put("UNDEFINED_PROVIDER_DETAILS", Arrays.asList(" ", " "));

        // itemMappingDetails
        Map<String, List<String>> itemMappingDetails = new HashMap<>();
        itemMappingDetails.put("SERVICING_PROVIDER_DETAILS",
                Arrays.asList("servicing_provider_name","servicing_provider_npi","servicing_provider_Specialty","servicing_provider_tin","servicing_provider_address","servicing_provider_city","servicing_provider_state","servicing_provider_zip"));
        itemMappingDetails.put("UNDEFINED_PROVIDER_DETAILS",
                Arrays.asList("undefined_provider_name","undefined_provider_npi","undefined_provider_Specialty","undefined_provider_tin","undefined_provider_address","undefined_provider_city","undefined_provider_state","undefined_provider_zip"));

        // nameMappingDetails
        Map<String, String> nameMappingDetails = new HashMap<>();
        nameMappingDetails.put("servicing_provider_name", "Provider Name");
        nameMappingDetails.put("servicing_provider_type", "Provider Type");
        nameMappingDetails.put("servicing_provider_npi", "Provider NPI");
        nameMappingDetails.put("servicing_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("servicing_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("servicing_provider_address", "Provider Address");
        nameMappingDetails.put("servicing_provider_city", "Provider City");
        nameMappingDetails.put("servicing_provider_state", "Provider State");
        nameMappingDetails.put("servicing_provider_zip", "Provider ZIP Code");

        nameMappingDetails.put("referring_provider_name", "Provider Name");
        nameMappingDetails.put("referring_provider_type", "Provider Type");
        nameMappingDetails.put("referring_provider_npi", "Provider NPI");
        nameMappingDetails.put("referring_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("referring_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("referring_provider_address", "Provider Address");
        nameMappingDetails.put("referring_provider_city", "Provider City");
        nameMappingDetails.put("referring_provider_state", "Provider State");
        nameMappingDetails.put("referring_provider_zip", "Provider ZIP Code");

        nameMappingDetails.put("undefined_provider_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_type", "Provider Type");
        nameMappingDetails.put("undefined_provider_npi", "Provider NPI");
        nameMappingDetails.put("undefined_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("undefined_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("undefined_provider_address", "Provider Address");
        nameMappingDetails.put("undefined_provider_city", "Provider City");
        nameMappingDetails.put("undefined_provider_state", "Provider State");
        nameMappingDetails.put("undefined_provider_zip", "Provider ZIP Code");
        // Process the data
        List<OutputItem> output = processProviders(providers, metaProviderEntityDetails,
                itemMappingDetails, nameMappingDetails);

        logger.info("final output----------------------------------------------------\n");
        // Print results
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print
        String jsonOutput = objectMapper.writeValueAsString(output);
        System.out.println(jsonOutput);

    }
}
