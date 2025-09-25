package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContainsComparisonAdapter implements ComparisonAdapter{
    @Override
    public Long validate(ControlDataComparisonQueryInputTable controlDataInputLineItem, ActionExecutionAudit action, Logger log) {

        return dataValidation(
                controlDataInputLineItem,log,action

        );
    }

    public String getNormalizedExtractedValue(String actualValue, String extractedValue, String lineItemType) {
        if (!"multi_value".equals(lineItemType) || actualValue == null || actualValue.trim().isEmpty()) {
            return extractedValue;
        }

        Map<String, String> actualMap = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\\s*[^,]+").matcher(actualValue);
        while (matcher.find()) {
            String token = matcher.group().trim();
            actualMap.putIfAbsent(token.toLowerCase(), token);
        }

        // Split extracted values and normalize them using actualValue casing
        StringBuilder result = new StringBuilder();
        String[] extractedTokens = extractedValue.split(",");
        for (int i = 0; i < extractedTokens.length; i++) {
            String token = extractedTokens[i].trim();
            String normalized = actualMap.getOrDefault(token.toLowerCase(), token);  // fallback to original if not found
            if (i > 0) result.append(",");
            result.append(normalized);
        }

        return result.toString();
    }

    public Long dataValidation(ControlDataComparisonQueryInputTable controlDataInputLineItem,Logger log,ActionExecutionAudit action) {
        String normalizedExtracted = getNormalizedExtractedValue(
                controlDataInputLineItem.getActualValue(),
                controlDataInputLineItem.getExtractedValue(),
                controlDataInputLineItem.getLineItemType()
        );
        String actualData = controlDataInputLineItem.getActualValue();
        String originId = controlDataInputLineItem.getOriginId();
        Long paperNo = controlDataInputLineItem.getPaperNo();
        String sorItemName = controlDataInputLineItem.getSorItemName();
        Long tenantId = controlDataInputLineItem.getTenantId();
        if (normalizedExtracted == null || normalizedExtracted.isEmpty()) {
            log.warn("Invalid input encountered for extractedData. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return actualData == null ? 0L : (long) actualData.length();
        }
        if (actualData == null || actualData.isEmpty()) {
            log.warn("Invalid input encountered for actualData. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) normalizedExtracted.length();
        }

        String normalizeExtractedData = normalizedExtracted.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String normalizedActual = actualData.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        int maxLength = Math.max(normalizedExtracted.length(), normalizedActual.length());



        int distance = LevenshteinDistance.getDefaultInstance().apply(normalizeExtractedData, normalizedActual);


        if (distance == 0) {
            return 0L;
        }

        if (!"multi_value".equals(controlDataInputLineItem.getLineItemType())) {
            Set<String> extractedWords = Arrays.stream(normalizedExtracted.replaceAll("[^a-zA-Z0-9,]", " ")
                            .toLowerCase()
                            .split("\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            Set<String> actualWords = Arrays.stream(actualData.replaceAll("[^a-zA-Z0-9,]", " ")
                            .toLowerCase()
                            .split("\\s+"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            if (actualWords.equals(extractedWords)) {
                return 0L;  // swap-tolerant match for single_value
            }
        }

        Set<String> extractedWords = Arrays.stream(actualData.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        Set<String> actualWords = Arrays.stream(normalizedExtracted.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        if (extractedWords.equals(actualWords)) {
            return 0L;
        }

        return (long) distance;
    }


    @Override
    public String getName() {
        return "alpha";
    }
}
