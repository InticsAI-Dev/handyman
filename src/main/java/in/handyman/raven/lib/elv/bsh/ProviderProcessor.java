package in.handyman.raven.lib.elv.bsh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProviderProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ProviderProcessor.class);

    public static List<Map<String, Object>> processProviders(
            List<Map<String, String>> providers,
            Map<String, List<String>> metaProviderEntityDetails,
            Map<String, List<String>> itemMappingDetails,
            Map<String, String> nameMappingDetails) {

        logger.info("Starting processProviders execution");
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, String> provider : providers) {
            Map<String, Object> providerMap = new HashMap<>(provider);
            String providerType = (String) providerMap.get("Provider Type");
            String matchedContainer = null;
            Map<String, Double> containerDistanceMap = new HashMap<>();

            String cleanedProviderType = cleanString(providerType != null ? providerType.toLowerCase() : "");

            for (Map.Entry<String, List<String>> entry : metaProviderEntityDetails.entrySet()) {
                String container = entry.getKey();
                List<String> values = entry.getValue();

                for (String value : values) {
                    logger.info("{} <--------> {}", providerType.toLowerCase(), value.toLowerCase());
                    String cleanedValue = cleanString(value.toLowerCase());
                    double distance;

                    if (cleanedProviderType.length() >= 10) {
                        distance = calculateJaroWinklerDistance(cleanedProviderType, cleanedValue);
                        logger.info("Calculated Jaro-Winkler Distance: {}", distance);
                    } else {
                        distance = calculateLCSLength(cleanedProviderType, cleanedValue);
                        logger.info("Calculated LCS Length: {}", distance);
                    }

                    containerDistanceMap.put(container, Math.max(containerDistanceMap.getOrDefault(container, Double.NEGATIVE_INFINITY), distance));
                }
            }

            matchedContainer = findMaxValueKey(containerDistanceMap);
            if (matchedContainer == null) matchedContainer = "UNDEFINED_PROVIDER_DETAILS";

            List<String> sorItemList = itemMappingDetails.getOrDefault(matchedContainer, Collections.emptyList());
            logger.info("Matched Container: {}", matchedContainer);

            for (String item : sorItemList) {
                String mappedKey = nameMappingDetails.get(item);
                if (mappedKey != null) {
                    String keyValue = (String) providerMap.get(mappedKey);
                    if (keyValue != null) {
                        Map<String, Object> outputItem = new HashMap<>();
                        outputItem.put("key", item);

                        if ("UNDEFINED_PROVIDER_DETAILS".equals(matchedContainer)) {
                            Map<String, Object> existingItem = findExistingItem(result, item);
                            if (existingItem != null) {
                                String currentValue = (String) existingItem.get("value");
                                if (!currentValue.isEmpty()) currentValue += ", ";
                                currentValue += "\"" + keyValue + "\"";
                                existingItem.put("value", currentValue);
                            } else {
                                outputItem.put("value", keyValue.trim().isEmpty() ? "\"\"" : "\"" + keyValue + "\"");
                                outputItem.put("boundingBox", new HashMap<>());
                                outputItem.put("confidence", 100.0);
                                result.add(outputItem);
                            }
                        } else {
                            outputItem.put("value", keyValue);
                            outputItem.put("boundingBox", new HashMap<>());
                            outputItem.put("confidence", 100.0);
                            result.add(outputItem);
                        }
                    }
                }
            }
        }

        logger.info("processProviders completed with result size: {}", result.size());
        return result;
    }

    private static Map<String, Object> findExistingItem(List<Map<String, Object>> items, String key) {
        return items.stream().filter(item -> key.equals(item.get("key"))).findFirst().orElse(null);
    }

    private static String findMaxValueKey(Map<String, Double> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static String cleanString(String str) {
        if (str == null) return "";
        String trimmed = str.trim();
        while (trimmed.contains("  ")) {
            trimmed = trimmed.replace("  ", " ");
        }
        return trimmed.toLowerCase();
    }

    private static double calculateJaroWinklerDistance(String str1, String str2) {
        double jaroScore = calculateJaroSimilarity(str1, str2);
        int prefixLength = 0;
        int maxPrefix = Math.min(4, Math.min(str1.length(), str2.length()));

        for (int i = 0; i < maxPrefix && str1.charAt(i) == str2.charAt(i); i++) {
            prefixLength++;
        }

        double p = 0.1;
        return jaroScore + (prefixLength * p * (1.0 - jaroScore));
    }

    private static double calculateJaroSimilarity(String str1, String str2) {
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

    private static double calculateLCSLength(String str1, String str2) {
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
}
