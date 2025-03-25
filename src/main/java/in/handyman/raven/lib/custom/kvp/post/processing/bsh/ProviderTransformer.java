package in.handyman.raven.lib.custom.kvp.post.processing.bsh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProviderTransformer {
    private final Logger logger = LoggerFactory.getLogger(ProviderProcessorSpring.class);

    private final List<Map<String, String>> providers;
    private final Map<String, List<String>> metaProviderEntityDetails;
    private final Map<String, List<String>> itemMappingDetails;
    private final Map<String, String> nameMappingDetails;

    public ProviderTransformer(
            List<Map<String, String>> providers) {
        this.providers = providers;
        this.metaProviderEntityDetails = metaProviderEntityDetails();
        this.itemMappingDetails = itemMappingDetails();
        this.nameMappingDetails = nameMappingDetails();
    }

    public List<ProviderTransformerOutputItem> process() {
        List<ProviderTransformerOutputItem> result = new ArrayList<>();

        // Process each provider in the response
        for (Map<String, String> provider : providers) {
            Map<String, String> providerMap = new HashMap<>(provider);
            String providerType = providerMap.get("Provider Type");
            String cleanedProviderType = cleanString(providerType.toLowerCase());

            String matchedContainer = null;
            String cleanedValue = "";
            String container = "";
            Double maxDistance = 0.0;
            Map<String, Double> containerDistanceMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : metaProviderEntityDetails().entrySet()) {
                matchedContainer = "";
                container = entry.getKey();
                logger.info("length of string : " + (cleanedProviderType.length()) + "\n");

                for (String value : entry.getValue()) {

                    cleanedValue = cleanString(value);

                    boolean checkForContainerMatch = cleanedProviderType.contains(cleanedValue);

                    if (checkForContainerMatch) {
                        logger.info("Executing exact match");
                        matchedContainer = container;
                        break;
                    } else {

                        logger.info(providerType.toLowerCase() + " <--------> " + value.toLowerCase());
                        cleanedValue = cleanString(value.toLowerCase());
                        double distance1 = calculateJaroWinklerDistance(cleanedProviderType, cleanedValue);
                        logger.info("Calculated JaroWinklerDistance distance: {}", distance1);
                        double distance2 = calculateLCSLength(cleanedProviderType, cleanedValue);
                        logger.info("Calculated LCSLength distance:{}", distance2);
                        double distance = (distance1 + distance2) / 2;
                        containerDistanceMap.merge(container, distance, Math::max);
                        logger.info("Calculated average distance:{}", distance);
                    }

                    maxDistance = Collections.max(containerDistanceMap.values());


                }

                if (matchedContainer != null && !matchedContainer.isEmpty()) {
                    break;
                } else {
                    logger.info("<----------------maxDistance----------------->{}", maxDistance + "\n");
                    if (maxDistance > 0.7) {
                        logger.info("<----------------containerDistanceMap.entrySet()----------------->{}", containerDistanceMap.entrySet() + "\n");
                        matchedContainer = Collections.max(containerDistanceMap.entrySet(), Map.Entry.comparingByValue()).getKey();
                        break;

                    } else {
                        matchedContainer = "UNDEFINED_PROVIDER_DETAILS";
                        logger.info("<----------------matchedContainer----------------->{}", matchedContainer);
                        break;
                    }
                }
            }

            List<String> sorItemList = itemMappingDetails().getOrDefault(matchedContainer, Collections.emptyList());

            logger.info("Matched Container: " + matchedContainer);
            for (String item : sorItemList) {
                String mappedKey = nameMappingDetails().get(item);
                logger.info("Item: " + item + " -> Mapped Key Value: " + mappedKey);
                if (mappedKey != null) {
                    String keyValue = providerMap.get(mappedKey);
                    if (keyValue != null) {
                        if (Objects.equals(matchedContainer, "UNDEFINED_PROVIDER_DETAILS")) {
                            Optional<ProviderTransformerOutputItem> existingItem = result.stream()
                                    .filter(o -> o.getKey().equals(item))
                                    .findFirst();

                            if (existingItem.isPresent()) {
                                if (!existingItem.get().value.isEmpty()) {
                                    existingItem.get().value += ", ";
                                }
                                existingItem.get().value += "\"" + keyValue + "\"";
                            } else {
                                if (keyValue.trim().isEmpty()) {
                                    result.add(new ProviderTransformerOutputItem(item, "\"\"", new HashMap<>(), matchedContainer));
                                } else {
                                    result.add(new ProviderTransformerOutputItem(item, "\"" + keyValue + "\"", new HashMap<>(), matchedContainer));
                                }
                            }
                        } else {
                            ProviderTransformerOutputItem output = new ProviderTransformerOutputItem(item, keyValue, new HashMap<>(), matchedContainer);
                            result.add(output);
                        }
                    }
                }
            }
        }
        return result;
    }

    // Static utility methods remain unchanged
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

        double p = 0.1;
        return jaroScore + (prefixLength * p * (1.0 - jaroScore));
    }

    public static double calculateLCSLength(String str1, String str2) {
        if (str1 == null || str2 == null) throw new IllegalArgumentException("Strings must not be null");

        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        int lcsLength = dp[len1][len2];
        int maxLength = Math.max(len1, len2);
        return maxLength > 0 ? (double) lcsLength / maxLength : 0.0;
    }

    public static Map<String, List<String>> metaProviderEntityDetails() {
        Map<String, List<String>> metaProviderEntityDetails = new HashMap<>();
        metaProviderEntityDetails.put("SERVICING_PROVIDER_DETAILS", Arrays.asList("servicing provider", "physician", "Therapist", "Attending physician", "Accepting physician", "Rendering Provider"));
        metaProviderEntityDetails.put("REFERRING_PROVIDER_DETAILS", Arrays.asList("Referring Provider", "Requesting Provider", "Ordering Provider"));
        return metaProviderEntityDetails;
    }

    public static Map<String, List<String>> itemMappingDetails() {
        Map<String, List<String>> itemMappingDetails = new HashMap<>();
        itemMappingDetails.put("SERVICING_PROVIDER_DETAILS",
                Arrays.asList("servicing_provider_name", "servicing_provider_npi", "servicing_provider_Specialty", "servicing_provider_tin", "servicing_provider_address", "servicing_provider_city", "servicing_provider_state", "servicing_provider_zip"));
        itemMappingDetails.put("UNDEFINED_PROVIDER_DETAILS",
                Arrays.asList("undefined_provider_name", "undefined_provider_npi", "undefined_provider_Specialty", "undefined_provider_tin", "undefined_provider_address", "undefined_provider_city", "undefined_provider_state", "undefined_provider_zip"));
        itemMappingDetails.put("REFERRING_PROVIDER_DETAILS",
                Arrays.asList("referring_provider_name", "referring_provider_npi", "referring_provider_Specialty", "referring_provider_tin", "referring_provider_address", "referring_provider_city", "referring_provider_state", "referring_provider_zip"));

        return itemMappingDetails;
    }

    public static Map<String, String> nameMappingDetails() {

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

        return nameMappingDetails;

    }

}


