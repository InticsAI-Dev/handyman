package in.handyman.raven.lib.adapters.ocr;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class NameComparisonAdaptor implements OcrComparisonAdapter {

    private static final JaroWinklerSimilarity SIMILARITY = new JaroWinklerSimilarity();

    @Override
    public OcrComparisonResult compareValues(String expectedValue, String extractedText, double threshold) {
        if (expectedValue == null || expectedValue.trim().isEmpty()) {
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("EMPTY_ANSWER")
                    .candidatesList("")
                    .build();
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList("")
                    .build();
        }

        String cleanedExpected = expectedValue.trim().toLowerCase();
        String cleanedExtracted = extractedText.trim().toLowerCase();

        String[] tokens = cleanedExtracted.split("\\s+");
        List<String> candidates = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].isEmpty()) {
                candidates.add(tokens[i]);
            }
            if (i < tokens.length - 1 && !tokens[i].isEmpty() && !tokens[i + 1].isEmpty()) {
                candidates.add(tokens[i] + " " + tokens[i + 1]);
            }
            if (i < tokens.length - 2 && !tokens[i].isEmpty() && !tokens[i + 1].isEmpty() && !tokens[i + 2].isEmpty()) {
                candidates.add(tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2]);
            }
        }

        String candidatesList = candidates.stream().collect(Collectors.joining(","));

        if (candidates.isEmpty()) {
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList(candidatesList)
                    .build();
        }

        // Find best match using Jaro-Winkler
        double bestScoreDouble = 0.0;
        String bestMatchCandidate = cleanedExpected;

        for (String candidate : candidates) {
            double score = SIMILARITY.apply(cleanedExpected, candidate);
            if (score > bestScoreDouble) {
                bestScoreDouble = score;
                bestMatchCandidate = candidate;
            }
        }

        int bestScore = (int) (bestScoreDouble * 100);
        boolean isMatch = bestScoreDouble > threshold;

        // Use original casing for bestMatch if match found, else original expected
        String finalBestMatch = isMatch ? restoreOriginalCasing(bestMatchCandidate, extractedText) : expectedValue;

        String method = "JARO_WINKLER_NAME";
        if (!isMatch) {
            method = bestScore == 0 ? "NO_MATCH_NAME" : method;
        }

        return OcrComparisonResult.builder()
                .isMatch(isMatch)
                .bestMatch(finalBestMatch)
                .bestScore(bestScore)
                .matchingMethod(method)
                .candidatesList(candidatesList)
                .build();
    }

    @Override
    public String getName() {
        return "alpha";
    }


    private String restoreOriginalCasing(String lowerCandidate, String originalExtracted) {
        if (originalExtracted == null || lowerCandidate.isEmpty()) {
            return lowerCandidate;
        }
        String lowerExtracted = originalExtracted.toLowerCase();
        int index = lowerExtracted.indexOf(lowerCandidate);
        if (index != -1) {
            int end = index + lowerCandidate.length();
            return originalExtracted.substring(index, end);
        }
        return lowerCandidate;
    }
}