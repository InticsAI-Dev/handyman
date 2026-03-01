package in.handyman.raven.lib.services.sor.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonRepairUtil {

    private JsonRepairUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Repairs malformed JSON by:
     * 1. Adding missing quotes to keys and values
     * 2. Balancing braces and brackets
     * 3. Assigning empty values where missing
     */
    public static String repairJson(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return jsonString;
        }

        jsonString = addMissingQuotes(jsonString);
        jsonString = balanceBracesAndBrackets(jsonString);
        jsonString = assignEmptyValues(jsonString);

        return jsonString;
    }

    private static String addMissingQuotes(String jsonString) {
        jsonString = jsonString.replaceAll(
                "(\\{|,\\s*)(\\w+)(?=\\s*:)",
                "$1\"$2\""
        );
        jsonString = jsonString.replaceAll(
                "(?<=:)\\s*([^\"\\s,\\n}\\]]+)(?=\\s*(,|}|\\n|\\]))",
                "\"$1\""
        );
        return jsonString;
    }

    private static String balanceBracesAndBrackets(String jsonString) {
        int openBraces = 0, closeBraces = 0, openBrackets = 0, closeBrackets = 0;

        for (char c : jsonString.toCharArray()) {
            switch (c) {
                case '{':
                    openBraces++;
                    break;
                case '}':
                    closeBraces++;
                    break;
                case '[':
                    openBrackets++;
                    break;
                case ']':
                    closeBrackets++;
                    break;
            }
        }

        StringBuilder sb = new StringBuilder(jsonString);
        for (int i = 0; i < Math.max(0, openBraces - closeBraces); i++) {
            sb.append('}');
        }
        for (int i = 0; i < Math.max(0, openBrackets - closeBrackets); i++) {
            sb.append(']');
        }

        return sb.toString();
    }


    private static String assignEmptyValues(String jsonString) {
        return jsonString.replaceAll(
                "(?<=:)\\s*(?=,|\\s*}|\\s*\\])",
                "\"\""
        );
    }

    /**
     * Converts a formatted JSON string into a JsonNode.
     * Handles strings with ```json``` markers and repairs malformed JSON.
     *
     * @param jsonResponse the input string
     * @param objectMapper the Jackson ObjectMapper
     * @return a JsonNode or null if input cannot be parsed
     * @throws Exception if parsing fails
     */
    public static JsonNode toJsonNode(String jsonResponse, ObjectMapper objectMapper) throws Exception {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return null;
        }

        String processedJson = jsonResponse;

        if (jsonResponse.contains("```json")) {
            // Extract content between ```json and ```
            Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
            Matcher matcher = pattern.matcher(jsonResponse);

            if (matcher.find()) {
                processedJson = matcher.group(1).replace("\n", "");
                processedJson = repairJson(processedJson);
            } else {
                processedJson = repairJson(jsonResponse);
            }

        } else if (!jsonResponse.contains("{") && !jsonResponse.contains("[")) {
            // No JSON structure found
            return null;
        }

        return objectMapper.readTree(processedJson);
    }
}

