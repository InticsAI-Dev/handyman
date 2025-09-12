package in.handyman.raven.lib.adapters.ocr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AddressAdaptor implements OcrComparisonAdapter {

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

        // Normalize: trim, lowercase, remove punctuation, standardize address terms
        String cleanedExpected = normalizeText(expectedValue);
        String cleanedExtracted = normalizeText(extractedText);

        // Split into tokens (words)
        String[] tokens = cleanedExtracted.split("\\s+");
        List<String> candidates = new ArrayList<>();

        // Generate candidates: single words, 2-4 word phrases (common for addresses: street name, number, etc.)
        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].isEmpty()) {
                candidates.add(tokens[i]); // single word (e.g., "Main")
                if (i < tokens.length - 1 && !tokens[i].isEmpty() && !tokens[i + 1].isEmpty()) {
                    candidates.add(tokens[i] + " " + tokens[i + 1]); // two-word (e.g., "Main Street")
                }
                if (i < tokens.length - 2 && !tokens[i].isEmpty() && !tokens[i + 1].isEmpty() && !tokens[i + 2].isEmpty()) {
                    candidates.add(tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2]); // three-word (e.g., "123 Main Street")
                }
                if (i < tokens.length - 3 && !tokens[i].isEmpty() && !tokens[i + 1].isEmpty() && !tokens[i + 2].isEmpty() && !tokens[i + 3].isEmpty()) {
                    candidates.add(tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2] + " " + tokens[i + 3]); // four-word (e.g., "123 Main Street Apt")
                }
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

        // Restore original casing for best match
        String finalBestMatch = isMatch ? restoreOriginalCasing(bestMatchCandidate, extractedText) : expectedValue;

        String method = "JARO_WINKLER_ADDRESS";
        if (!isMatch) {
            method = bestScore == 0 ? "NO_MATCH_ADDRESS" : method;
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
        return "member_address_line1";
    }

    /**
     * Normalizes text for address comparison by trimming, converting to lowercase,
     * removing punctuation, and standardizing common address terms.
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim().toLowerCase();
        normalized = normalized.replaceAll("[^A-Za-z0-9\\s]", ""); // Remove punctuation
        normalized = normalized.replaceAll("\\bst\\b", "street")
                .replaceAll("\\bave\\b", "avenue")
                .replaceAll("\\brd\\b", "road")
                .replaceAll("\\bblvd\\b", "boulevard")
                .replaceAll("\\s+", " "); // Normalize spaces
        return normalized.trim();
    }

    /**
     * Restores the original casing from the extractedText for the best match candidate.
     */
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