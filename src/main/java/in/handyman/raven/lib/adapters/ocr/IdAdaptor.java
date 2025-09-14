package in.handyman.raven.lib.adapters.ocr;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IdAdaptor implements OcrComparisonAdapter {

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

        List<String> expectedValues = splitByCommaAndClean(expectedValue);

        List<String> ocrWords = extractAllWordsAndPhrases(extractedText);
        String candidatesList = String.join(",", ocrWords);

        if (ocrWords.isEmpty()) {
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList(candidatesList)
                    .build();
        }

        List<MatchResult> allMatches = new ArrayList<>();

        for (String expected : expectedValues) {
            String cleanedExpected = expected.toLowerCase().trim();
            MatchResult bestMatch = findBestMatch(cleanedExpected, ocrWords, extractedText);
            bestMatch.originalExpected = expected;
            allMatches.add(bestMatch);
        }

        return buildFinalResult(allMatches, expectedValue, candidatesList, threshold);
    }

    /**
     * Splits text by comma and cleans each part
     */
    private List<String> splitByCommaAndClean(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Extracts all individual words and phrases from text (dynamic)
     */
    private List<String> extractAllWordsAndPhrases(String text) {
        List<String> wordsAndPhrases = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return wordsAndPhrases;
        }

        List<String> commaSeparated = splitByCommaAndClean(text);
        wordsAndPhrases.addAll(commaSeparated);

        List<String> individualWords = extractAllWords(text);
        wordsAndPhrases.addAll(individualWords);

        extractCommonPatterns(text, wordsAndPhrases);

        return wordsAndPhrases.stream()
                .distinct()
                .filter(s -> s.length() >= 2)
                .collect(Collectors.toList());
    }

    /**
     * Extracts all individual words from text
     */
    private List<String> extractAllWords(String text) {
        List<String> words = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return words;
        }

        Pattern wordPattern = Pattern.compile("[\\p{L}\\p{N}'-]+");
        Matcher matcher = wordPattern.matcher(text.toLowerCase());

        while (matcher.find()) {
            String word = matcher.group().trim();
            if (!word.isEmpty()) {
                words.add(word);
            }
        }

        return words;
    }

    /**
     * Extracts common multi-word patterns from text
     */
    private void extractCommonPatterns(String text, List<String> results) {
        String lowerText = text.toLowerCase();

        // Extract 2-word phrases
        Pattern twoWordPattern = Pattern.compile("\\b[\\p{L}\\p{N}'-]+\\s+[\\p{L}\\p{N}'-]+\\b");
        Matcher matcher = twoWordPattern.matcher(lowerText);
        while (matcher.find()) {
            results.add(matcher.group());
        }

        // Extract 3-word phrases
        Pattern threeWordPattern = Pattern.compile("\\b[\\p{L}\\p{N}'-]+\\s+[\\p{L}\\p{N}'-]+\\s+[\\p{L}\\p{N}'-]+\\b");
        matcher = threeWordPattern.matcher(lowerText);
        while (matcher.find()) {
            results.add(matcher.group());
        }
    }

    /**
     * Finds the best match for a single expected value against all OCR words/phrases
     */
    private MatchResult findBestMatch(String expectedValue, List<String> candidates, String originalExtracted) {
        double bestScore = 0.0;
        String bestCandidate = expectedValue;
        String bestOriginalCandidate = expectedValue;

        for (String candidate : candidates) {
            double score = SIMILARITY.apply(expectedValue, candidate);

            if (score > bestScore) {
                bestScore = score;
                bestCandidate = candidate;
                bestOriginalCandidate = findOriginalInText(candidate, originalExtracted);
            }

            if (score == 1.0) {
                break;
            }
        }

        MatchResult result = new MatchResult();
        result.score = bestScore;
        result.candidate = bestCandidate;
        result.restoredMatch = bestOriginalCandidate;

        return result;
    }

    /**
     * Finds the original text in the extracted text with proper casing
     */
    private String findOriginalInText(String searchText, String originalText) {
        if (originalText == null || searchText == null || searchText.isEmpty()) {
            return searchText;
        }

        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(searchText) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            return originalText.substring(matcher.start(), matcher.end());
        }

        return searchText;
    }

    private OcrComparisonResult buildFinalResult(List<MatchResult> matches, String originalExpected,
                                                 String candidatesList, double threshold) {
        boolean allMatch = matches.stream().allMatch(match -> match.score >= threshold);

        boolean anyMatch = matches.stream().anyMatch(match -> match.score >= threshold);

        List<MatchResult> matchedValues = matches.stream()
                .filter(match -> match.score >= threshold)
                .collect(Collectors.toList());

        String method = determineMatchingMethod(matches, threshold);

        String matchSummary = matches.stream()
                .map(match -> String.format("%s:%.2f->%s", match.originalExpected, match.score, match.candidate))
                .collect(Collectors.joining("|"));

        String finalBestMatch;
        int finalScore;

        if (anyMatch) {
            finalBestMatch = reconstructCommaSeparatedMatch(matches, threshold);

            double avgScore = matchedValues.stream()
                    .mapToDouble(match -> match.score)
                    .average()
                    .orElse(0.0);
            finalScore = (int) (avgScore * 100);
        } else {
            finalBestMatch = originalExpected;
            finalScore = matches.stream()
                    .mapToInt(match -> (int) (match.score * 100))
                    .max()
                    .orElse(0);
        }

        return OcrComparisonResult.builder()
                .isMatch(allMatch)
                .bestMatch(finalBestMatch)
                .bestScore(finalScore)
                .matchingMethod(method)
                .candidatesList(candidatesList + "|MATCHES:" + matchSummary)
                .build();
    }

    /**
     * Reconstructs comma-separated match preserving original order
     */
    private String reconstructCommaSeparatedMatch(List<MatchResult> matches, double threshold) {
        List<String> matchedParts = new ArrayList<>();

        for (MatchResult match : matches) {
            if (match.score >= threshold) {
                matchedParts.add(match.restoredMatch);
            } else {
                matchedParts.add(match.originalExpected);
            }
        }

        return String.join(", ", matchedParts);
    }

    private String determineMatchingMethod(List<MatchResult> matches, double threshold) {
        long matchCount = matches.stream()
                .filter(match -> match.score >= threshold)
                .count();
        long totalCount = matches.size();

        if (matchCount == 0) {
            return "NO_MATCH";
        } else if (matchCount == totalCount) {
            return "ALL_MATCH";
        } else {
            return "PARTIAL_MATCH_" + matchCount + "_OF_" + totalCount;
        }
    }

    @Override
    public String getName() {
        return "alphanumeric";
    }

    // Helper class to store match results
    private static class MatchResult {
        double score = 0.0;
        String candidate = "";
        String restoredMatch = "";
        String originalExpected = "";
    }
}