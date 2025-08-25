package in.handyman.raven.lib.adapters.comparison;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimilarityComparisonAdapter implements ComparisonAdapter{
    @Override
    public Long validate(ControlDataComparisonQueryInputTable controlDataInputLineItem, ActionExecutionAudit action, Logger log) {
        String normalizedExtracted = getNormalizedExtractedValue(
                controlDataInputLineItem.getActualValue(),
                controlDataInputLineItem.getExtractedValue(),
                controlDataInputLineItem.getLineItemType()
        );
        return dataValidation(
                normalizedExtracted,
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

    public Long dataValidation(String extractedData, ControlDataComparisonQueryInputTable controlDataInputLineItem,Logger log,ActionExecutionAudit action) {
        String actualData = controlDataInputLineItem.getActualValue();
        String originId = controlDataInputLineItem.getOriginId();
        Long paperNo = controlDataInputLineItem.getPaperNo();
        String sorItemName = controlDataInputLineItem.getSorItemName();
        Long tenantId = controlDataInputLineItem.getTenantId();
        if (extractedData == null || extractedData.isEmpty()) {
            log.warn("Invalid input encountered for extractedData. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return actualData == null ? 0L : (long) actualData.length();
        }
        if (actualData == null || actualData.isEmpty()) {
            log.warn("Invalid input encountered for actualData. Details - Origin ID: {}, Sor Item Name: {}, Paper No: {}, Tenant ID: {}", originId, sorItemName, paperNo, tenantId);
            return (long) extractedData.length();
        }

        String normalizedExtracted = extractedData.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String normalizedActual = actualData.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();


        int distance = LevenshteinDistance.getDefaultInstance().apply(normalizedExtracted, normalizedActual);
        int maxLength = Math.max(normalizedExtracted.length(), normalizedActual.length());

        double similarity = (maxLength == 0 ? 1.0 : (1.0 - (double) distance / maxLength)) * 100;


        if (distance == 0) {
            return 0L;
        }

        Set<String> extractedWords = new HashSet<>(Arrays.asList(extractedData.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+")));
        Set<String> actualWords = new HashSet<>(Arrays.asList(actualData.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().split("\\s+")));

        if (extractedWords.equals(actualWords)) {
            return 0L;
        }

        return (long) distance;
    }


    @Override
    public String getName() {
        return "alphanumeric";
    }
}
