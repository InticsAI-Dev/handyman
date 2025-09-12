package in.handyman.raven.lib.adapters.ocr;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        // Extract all potential address-like strings from OCR text
        List<String> candidates = extractAllAddressCandidates(extractedText);
        String candidatesList = String.join(";", candidates);

        if (candidates.isEmpty()) {
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList(candidatesList)
                    .build();
        }

        // Find best match using pure fuzzy matching
        BestMatchResult bestMatch = findBestMatch(expectedValue, candidates);

        boolean isMatch = bestMatch.score >= threshold;
        int finalScore = (int) (bestMatch.score * 100);

        String method = isMatch ? "ADDRESS_MATCH" : "NO_ADDRESS_MATCH";
        if (isMatch && bestMatch.score == 1.0) {
            method = "EXACT_ADDRESS_MATCH";
        }

        return OcrComparisonResult.builder()
                .isMatch(isMatch)
                .bestMatch(bestMatch.matchedText)
                .bestScore(finalScore)
                .matchingMethod(method)
                .candidatesList(candidatesList + "|SCORE:" + String.format("%.2f", bestMatch.score))
                .build();
    }

    /**
     * Extracts all potential address-like strings from text
     */
    private List<String> extractAllAddressCandidates(String text) {
        List<String> candidates = new ArrayList<>();

        // Extract lines (common way addresses appear in OCR)
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (isPotentialAddress(trimmed)) {
                candidates.add(trimmed);
            }
        }

        // Extract phrases that look like addresses within lines
        extractAddressPhrases(text, candidates);

        return candidates.stream()
                .distinct()
                .filter(candidate -> candidate.length() >= 5) // Minimum reasonable address length
                .collect(Collectors.toList());
    }

    /**
     * Checks if text could be an address (very permissive)
     */
    private boolean isPotentialAddress(String text) {
        if (text == null || text.length() < 5) return false;

        // Very basic check: contains both letters and numbers, or looks like a street name
        boolean hasLetters = text.matches(".*[A-Za-z]+.*");
        boolean hasNumbers = text.matches(".*\\d+.*");
        boolean hasSpaces = text.contains(" ");

        return (hasLetters && hasNumbers) || (hasLetters && hasSpaces);
    }

    /**
     * Extracts address-like phrases from within text lines
     */
    private void extractAddressPhrases(String text, List<String> candidates) {
        // Look for patterns that might be addresses within longer text
        Pattern addressLikePattern = Pattern.compile(
                "\\b(?:\\d+[A-Za-z0-9]*\\s+)?[A-Za-z0-9\\s]{3,50}\\b",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = addressLikePattern.matcher(text);
        while (matcher.find()) {
            String phrase = matcher.group().trim();
            if (isPotentialAddress(phrase) && !phrase.contains("\n")) {
                candidates.add(phrase);
            }
        }
    }

    /**
     * Finds the best match using pure fuzzy matching
     */
    private BestMatchResult findBestMatch(String expected, List<String> candidates) {
        double bestScore = 0.0;
        String bestCandidate = expected;
        String normalizedExpected = expected.toLowerCase();

        for (String candidate : candidates) {
            String normalizedCandidate = candidate.toLowerCase();
            double score = SIMILARITY.apply(normalizedExpected, normalizedCandidate);

            if (score > bestScore) {
                bestScore = score;
                bestCandidate = candidate;
            }

            if (score == 1.0) {
                break;
            }
        }

        BestMatchResult result = new BestMatchResult();
        result.score = bestScore;
        result.matchedText = bestCandidate;
        return result;
    }

    @Override
    public String getName() {
        return "ADDR_ALPHANUMERIC";
    }

    // Helper class
    private static class BestMatchResult {
        double score;
        String matchedText;
    }
}