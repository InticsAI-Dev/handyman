package in.handyman.raven.lib.adapters.ocr;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        List<String> expectedWords = extractAllWords(expectedValue);

        List<String> ocrWords = extractAllWords(extractedText);
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

        for (String expectedWord : expectedWords) {
            String cleanedExpected = expectedWord.toLowerCase().trim();
            MatchResult bestMatch = findBestMatchForWord(cleanedExpected, ocrWords, extractedText);
            bestMatch.originalExpected = expectedWord;
            allMatches.add(bestMatch);
        }

        return buildFinalResult(allMatches, expectedValue, candidatesList, threshold);
    }

    /**
     * Extracts all individual words from text
     */
    private List<String> extractAllWords(String text) {
        List<String> words = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return words;
        }

        Pattern wordPattern = Pattern.compile("[\\p{L}\\p{N}]+");
        Matcher matcher = wordPattern.matcher(text.toLowerCase());

        while (matcher.find()) {
            String word = matcher.group().trim();
            if (!word.isEmpty() && word.length() > 1) {
                words.add(word);
            }
        }

        return words.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Finds the best match for a single expected word against all OCR words
     */
    private MatchResult findBestMatchForWord(String expectedWord, List<String> ocrWords, String originalExtracted) {
        double bestScore = 0.0;
        String bestCandidate = expectedWord;
        String bestOriginalCandidate = expectedWord;

        for (String ocrWord : ocrWords) {
            double score = SIMILARITY.apply(expectedWord, ocrWord);

            if (score > bestScore) {
                bestScore = score;
                bestCandidate = ocrWord;
                bestOriginalCandidate = findOriginalWordInText(ocrWord, originalExtracted);
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
     * Finds the original word in the text with exact casing
     */
    private String findOriginalWordInText(String wordToFind, String originalText) {
        if (originalText == null || wordToFind == null) {
            return wordToFind;
        }

        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(wordToFind) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(originalText);

        if (matcher.find()) {
            return originalText.substring(matcher.start(), matcher.end());
        }

        return wordToFind;
    }

    private OcrComparisonResult buildFinalResult(List<MatchResult> matches, String originalExpected,
                                                 String candidatesList, double threshold) {
        boolean allWordsMatch = matches.stream()
                .allMatch(match -> match.score >= threshold);

        boolean anyWordMatches = matches.stream()
                .anyMatch(match -> match.score >= threshold);

        List<MatchResult> matchedWords = matches.stream()
                .filter(match -> match.score >= threshold)
                .collect(Collectors.toList());

        String method = determineMatchingMethod(matches, threshold);

        String matchSummary = matches.stream()
                .map(match -> String.format("%s:%.2f->%s", match.originalExpected, match.score, match.candidate))
                .collect(Collectors.joining("|"));

        String finalBestMatch;
        int finalScore;

        if (anyWordMatches) {
            finalBestMatch = reconstructBestMatch(matches, threshold);

            double avgScore = matchedWords.stream()
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
                .isMatch(allWordsMatch)
                .bestMatch(finalBestMatch)
                .bestScore(finalScore)
                .matchingMethod(method)
                .candidatesList(candidatesList + "|MATCHES:" + matchSummary)
                .build();
    }

    /**
     * Reconstructs the best match by combining matched words
     */
    private String reconstructBestMatch(List<MatchResult> matches, double threshold) {
        StringBuilder result = new StringBuilder();

        for (MatchResult match : matches) {
            if (match.score >= threshold) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(match.restoredMatch);
            }
        }

        return result.toString();
    }

    private String determineMatchingMethod(List<MatchResult> matches, double threshold) {
        long matchCount = matches.stream()
                .filter(match -> match.score >= threshold)
                .count();
        long totalCount = matches.size();

        if (matchCount == 0) {
            return "NO_MATCH";
        } else if (matchCount == totalCount) {
            return "ALL_WORDS_MATCH";
        } else {
            return "PARTIAL_MATCH_" + matchCount + "_OF_" + totalCount;
        }
    }

    @Override
    public String getName() {
        return "alpha";
    }

    // Helper class to store match results
    private static class MatchResult {
        double score = 0.0;
        String candidate = "";
        String restoredMatch = "";
        String originalExpected = "";
    }
}