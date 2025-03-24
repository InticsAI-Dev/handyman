package in.handyman.raven.lib.custom.kvp.post.processing.bsh;

import org.slf4j.Logger;

import java.util.*;
public class ProviderTransformerFinal {
    private Logger logger;

    public ProviderTransformerFinal(Logger logger) {
        this.logger = logger;
    }


    public List<ProviderTransformerOutputItem> processProviders(List<Map<String, String>> providers) {
        logger.info("Starting processProviders execution");

        List<ProviderTransformerOutputItem> result = new ArrayList<>();
        Map<String, List<String>> metaProviderEntityDetails = metaProviderEntityDetails();
        Map<String, List<String>> itemMappingDetails = itemMappingDetails();
        Map<String, String> nameMappingDetails = nameMappingDetails();

        for (Map<String, String> provider : providers) {
            String matchedContainer = findMatchingContainer(provider.get("Provider Type"), metaProviderEntityDetails);
            processProviderData(provider, matchedContainer, itemMappingDetails, nameMappingDetails, result);
        }

        logger.info("processProviders completed with result size: {}", result.size());
        return result;
    }

    private String findMatchingContainer(String providerType, Map<String, List<String>> metaProviderEntityDetails) {
        String cleanedProviderType = cleanString(providerType != null ? providerType : "");

        for (Map.Entry<String, List<String>> entry : metaProviderEntityDetails.entrySet()) {
            for (String value : entry.getValue()) {
                if (cleanedProviderType.contains(cleanString(value))) {
                    logger.info("Executing exact match for container: {}", entry.getKey());
                    return entry.getKey();
                }
            }
        }

        return findClosestMatchingContainer(cleanedProviderType, metaProviderEntityDetails);
    }

    private String findClosestMatchingContainer(String cleanedProviderType, Map<String, List<String>> metaProviderEntityDetails) {
        Map<String, Double> containerDistanceMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : metaProviderEntityDetails.entrySet()) {
            for (String value : entry.getValue()) {
                double distance = (calculateJaroWinklerDistance(cleanedProviderType, cleanString(value)) +
                        calculateLCSLength(cleanedProviderType, cleanString(value))) / 2;
                containerDistanceMap.put(entry.getKey(), Math.max(containerDistanceMap.getOrDefault(entry.getKey(), 0.0), distance));
            }
        }

        double maxDistance = containerDistanceMap.values().stream().max(Double::compare).orElse(0.0);
        if (maxDistance > 0.7) {
            return containerDistanceMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == maxDistance)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("UNDEFINED_PROVIDER_DETAILS");
        }

        logger.info("No exact match found. Using closest match: {} with similarity: {}", "UNDEFINED_PROVIDER_DETAILS", maxDistance);
        return "UNDEFINED_PROVIDER_DETAILS";
    }

    private void processProviderData(Map<String, String> provider, String matchedContainer, Map<String, List<String>> itemMappingDetails,
                                     Map<String, String> nameMappingDetails, List<ProviderTransformerOutputItem> result) {
        List<String> itemsList = itemMappingDetails.get(matchedContainer);
        if (itemsList == null) {
            logger.warn("No items mapping found for container: {}", matchedContainer);
            return;
        }

        for (String item : itemsList) {
            String mappedKey = nameMappingDetails.get(item);


            if (mappedKey != null) {
                String keyValue = provider.get(mappedKey);
                if(mappedKey.equals("Provider Name")){
                    if(matchedContainer.equals("SERVICING_PROVIDER_DETAILS")){
                        List<ProviderTransformerOutputItem> providerTransformerOutputItems=transformServicingProviderName(keyValue,matchedContainer);
                        result.addAll(providerTransformerOutputItems);
                    } else if (matchedContainer.equals("REFERRING_PROVIDER_DETAILS")) {
                        List<ProviderTransformerOutputItem> providerTransformerOutputItems=transformReferringProviderName(keyValue,matchedContainer);
                        result.addAll(providerTransformerOutputItems);
                    }
                }else {
                    if (keyValue != null && !keyValue.trim().isEmpty()) {
                        result.add(new ProviderTransformerOutputItem(item, keyValue, new HashMap<>(), matchedContainer));
                    }
                }


            }
        }
    }

    private String cleanString(String str) {
        if (str == null) return "";
        return str.trim().replaceAll(" +", " ").toLowerCase();
    }
    public List<ProviderTransformerOutputItem> transformReferringProviderName(String name, String containerName) {
        List<ProviderTransformerOutputItem> ProviderTransformerOutputItems = new ArrayList<>();

        if (name == null || name.trim().isEmpty()) {
            return ProviderTransformerOutputItems;
        }

        String[] parts = name.trim().split("\\s+");
        String firstName, lastName = "";

        if (parts.length > 1) {
            firstName = String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1));
            lastName = parts[parts.length - 1];
        } else {
            firstName = parts[0];
        }

        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_first_name", firstName, new HashMap<>(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_last_name", lastName, new HashMap<>(), containerName));

        return ProviderTransformerOutputItems;
    }

    public List<ProviderTransformerOutputItem> transformServicingProviderName(String name, String containerName) {
        List<ProviderTransformerOutputItem> ProviderTransformerOutputItems = new ArrayList<>();

        if (name == null || name.trim().isEmpty()) {
            return ProviderTransformerOutputItems;
        }

        String[] parts = name.trim().split("\\s+");
        String firstName, lastName = "";

        if (parts.length > 1) {
            firstName = String.join(" ", Arrays.copyOfRange(parts, 0, parts.length - 1));
            lastName = parts[parts.length - 1];
        } else {
            firstName = parts[0];
        }

        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_first_name", firstName, new HashMap<>(), containerName));
        ProviderTransformerOutputItems.add(new ProviderTransformerOutputItem("servicing_provider_last_name", lastName, new HashMap<>(), containerName));

        return ProviderTransformerOutputItems;
    }

    private double calculateJaroWinklerDistance(String str1, String str2) {
        double jaroScore = calculateJaroSimilarity(str1, str2);
        int prefixLength = 0;
        int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));

        for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {
            prefixLength++;
        }

        return jaroScore + (prefixLength * 0.1 * (1.0 - jaroScore));
    }

    private double calculateJaroSimilarity(String str1, String str2) {
        int len1 = str1.length(), len2 = str2.length();
        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] matches1 = new boolean[len1], matches2 = new boolean[len2];
        int matches = 0, transpositions = 0, k = 0;

        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (!matches2[j] && str1.charAt(i) == str2.charAt(j)) {
                    matches1[i] = matches2[j] = true;
                    matches++;
                    break;
                }
            }
        }

        for (int i = 0; i < len1; i++) {
            if (matches1[i]) {
                while (!matches2[k]) k++;
                if (str1.charAt(i) != str2.charAt(k)) transpositions++;
                k++;
            }
        }

        double m = matches;
        return matches == 0 ? 0.0 : (m / len1 + m / len2 + (m - transpositions / 2.0) / m) / 3.0;
    }

    private double calculateLCSLength(String str1, String str2) {
        int len1 = str1.length(), len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                dp[i][j] = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? dp[i - 1][j - 1] + 1 :
                        Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }

        return (double) dp[len1][len2] / Math.max(len1, len2);
    }


    private static Map<String, List<String>> metaProviderEntityDetails() {
        Map<String, List<String>> metaProviderEntityDetails = new HashMap<>();
        metaProviderEntityDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList("servicing provider", "physician", "Therapist", "Attending physician", "Accepting physician", "Rendering Provider"));
        metaProviderEntityDetails.put("REFERRING_PROVIDER_DETAILS", Arrays.asList("Referring Provider", "Requesting Provider", "Ordering Provider"));
        metaProviderEntityDetails.put("SERVICING_FACILITY_DETAILS", Arrays.asList("SERVICING_FACILITY_DETAILS","Service Facility","Facility","facility name"));
        return metaProviderEntityDetails;
    }

    private static Map<String, List<String>> itemMappingDetails() {
        Map<String, List<String>> itemMappingDetails = new HashMap<>();
        itemMappingDetails.put("SERVICING_PROVIDER_DETAILS",
                Arrays.asList("servicing_provider_full_name", "servicing_provider_specialty", "servicing_provider_npi",
                        "servicing_provider_address_line1", "servicing_provider_address_line2", "servicing_provider_city",
                        "servicing_provider_state", "servicing_provider_tin","servicing_provider_zipcode"));
        itemMappingDetails.put("SERVICING_FACILITY_DETAILS",
                Arrays.asList("servicing_facility_last_name", "servicing_facility_npi", "servicing_facility_zipcode",
                        "servicing_facility_address_line1", "servicing_facility_address_line2", "servicing_facility_city",
                        "servicing_facility_state","servicing_facility_tin","servicing_facility_specialty"));

        // Referring Provider Details
        itemMappingDetails.put("REFERRING_PROVIDER_DETAILS",
                Arrays.asList("referring_provider_address_line1","referring_provider_full_name", "referring_provider_npi",
                        "referring_provider_city", "referring_provider_specialty", "referring_provider_state",
                        "referring_provider_tin", "referring_provider_zipcode","referring_provider_address_line2"));
        return itemMappingDetails;
    }

    private static Map<String, String> nameMappingDetails() {

        Map<String, String> nameMappingDetails = new HashMap<>();
        nameMappingDetails.put("referring_provider_type", "Provider Type");
        nameMappingDetails.put("referring_provider_npi", "Provider NPI");
        nameMappingDetails.put("referring_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("referring_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("referring_provider_address_line1", "Provider Address");
        nameMappingDetails.put("referring_provider_city", "Provider City");
        nameMappingDetails.put("referring_provider_state", "Provider State");
        nameMappingDetails.put("referring_provider_full_name", "Provider Name");
        nameMappingDetails.put("referring_provider_zipcode", "Provider ZIP Code");

        // Service Facility Details
        nameMappingDetails.put("servicing_facility_last_name", "Provider Name");
        nameMappingDetails.put("servicing_facility_type", "Provider Type");
        nameMappingDetails.put("servicing_facility_npi", "Provider NPI");
        nameMappingDetails.put("servicing_facility_specialty", "Provider Specialty");
        nameMappingDetails.put("servicing_facility_tin", "Provider Tax ID");
        nameMappingDetails.put("servicing_facility_address_line1", "Provider Address");
        nameMappingDetails.put("servicing_facility_city", "Provider City");
        nameMappingDetails.put("servicing_facility_state", "Provider State");
        nameMappingDetails.put("servicing_facility_zipcode", "Provider ZIP Code");


        nameMappingDetails.put("servicing_provider_full_name", "Provider Name");
        nameMappingDetails.put("servicing_provider_type", "Provider Type");
        nameMappingDetails.put("servicing_provider_npi", "Provider NPI");
        nameMappingDetails.put("servicing_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("servicing_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("servicing_provider_address_line1", "Provider Address");
        nameMappingDetails.put("servicing_provider_city", "Provider City");
        nameMappingDetails.put("servicing_provider_state", "Provider State");
        nameMappingDetails.put("servicing_provider_zipcode", "Provider ZIP Code");



        nameMappingDetails.put("undefined_provider_name", "Provider Name");
        nameMappingDetails.put("undefined_provider_type", "Provider Type");
        nameMappingDetails.put("undefined_provider_npi", "Provider NPI");
        nameMappingDetails.put("undefined_provider_specialty", "Provider Specialty");
        nameMappingDetails.put("undefined_provider_tin", "Provider Tax ID");
        nameMappingDetails.put("undefined_provider_address_line1", "Provider Address");
        nameMappingDetails.put("undefined_provider_city", "Provider City");
        nameMappingDetails.put("undefined_provider_state", "Provider State");
        nameMappingDetails.put("undefined_facility_zipcode", "Provider ZIP Code");
        return nameMappingDetails;
    }
}
