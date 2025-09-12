package in.handyman.raven.lib.adapters.ocr;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class IdAdaptor implements OcrComparisonAdapter {

    private static final Pattern MEMBER_ID_PATTERN = Pattern.compile("\\b[A-Z0-9]{6,12}\\b"); // 6-12 alphanumeric chars

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

        // Normalize: trim, uppercase, remove non-alphanumeric characters
        String cleanedExpected = normalizeText(expectedValue);
        String cleanedExtracted = normalizeText(extractedText);

        // Extract potential member IDs using regex
        Matcher matcher = MEMBER_ID_PATTERN.matcher(cleanedExtracted);
        List<String> candidates = new ArrayList<>();
        while (matcher.find()) {
            candidates.add(matcher.group());
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

        // Exact match for member IDs
        double bestScoreDouble = 0.0;
        String bestMatchCandidate = cleanedExpected;

        for (String candidate : candidates) {
            if (candidate.equals(cleanedExpected)) {
                bestScoreDouble = 1.0;
                bestMatchCandidate = candidate;
                break;
            }
        }

        int bestScore = (int) (bestScoreDouble * 100);
        boolean isMatch = bestScoreDouble >= threshold;

        // Restore original casing for best match
        String finalBestMatch = isMatch ? restoreOriginalCasing(bestMatchCandidate, extractedText) : expectedValue;

        String method = isMatch ? "REGEX_MEMBER_ID" : "NO_MATCH_MEMBER_ID";

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
        return "member_id";
    }

    /**
     * Normalizes text for member ID comparison by trimming, converting to uppercase,
     * and removing non-alphanumeric characters.
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toUpperCase().replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Restores the original casing from the extractedText for the best match candidate.
     */
    private String restoreOriginalCasing(String candidate, String originalExtracted) {
        if (originalExtracted == null || candidate.isEmpty()) {
            return candidate;
        }
        String lowerExtracted = originalExtracted.toUpperCase(); // Member IDs typically uppercase
        int index = lowerExtracted.indexOf(candidate);
        if (index != -1) {
            int end = index + candidate.length();
            return originalExtracted.substring(index, end);
        }
        return candidate;
    }
}

