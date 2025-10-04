package in.handyman.raven.lib.adapters.ocr;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NameComparisonAdaptor implements OcrComparisonAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NameComparisonAdaptor.class);
    private static final JaroWinklerSimilarity SIMILARITY = new JaroWinklerSimilarity();

    @Override
    public OcrComparisonResult compareValues(String expectedValue, String extractedText, double threshold) {
        logger.debug("Starting name comparison with threshold: {}", threshold);

        if (expectedValue == null || expectedValue.trim().isEmpty()) {
            logger.warn("Expected value is null or empty");
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("EMPTY_ANSWER")
                    .candidatesList("")
                    .build();
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            logger.warn("Extracted text is null or empty");
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList("")
                    .build();
        }

        // Detect if expected value has comma format
        boolean hasCommaFormat = expectedValue.contains(",");
        logger.debug("Expected value comma format detected: {}", hasCommaFormat);

        List<String> expectedWords = extractAllWords(expectedValue);
        logger.debug("Expected words count: {}", expectedWords.size());

        List<String> ocrWords = extractAllWords(extractedText);
        String candidatesList = String.join(",", ocrWords);
        logger.debug("OCR words count: {}", ocrWords.size());

        if (ocrWords.isEmpty()) {
            logger.info("No candidates found in OCR text");
            return OcrComparisonResult.builder()
                    .isMatch(false)
                    .bestMatch(expectedValue)
                    .bestScore(0)
                    .matchingMethod("NO_CANDIDATES")
                    .candidatesList(candidatesList)
                    .build();
        }

        List<OcrComparisonMatchResult> allMatches = new ArrayList<>();

        for (String expectedWord : expectedWords) {
            String cleanedExpected = expectedWord.toLowerCase().trim();
            OcrComparisonMatchResult bestMatch = findBestMatchForWord(cleanedExpected, ocrWords, extractedText);
            bestMatch.setOriginalExpected(expectedWord);
            allMatches.add(bestMatch);
        }

        logger.debug("Completed word matching for {} expected words", expectedWords.size());
        return buildFinalResult(allMatches, expectedValue, candidatesList, threshold, hasCommaFormat);
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
    private OcrComparisonMatchResult findBestMatchForWord(String expectedWord, List<String> ocrWords, String originalExtracted) {
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
                logger.debug("Perfect match found for word with score: 1.0");
                break;
            }
        }

        return OcrComparisonMatchResult.builder()
                .score(bestScore)
                .candidate(bestCandidate)
                .restoredMatch(bestOriginalCandidate)
                .build();
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

    private OcrComparisonResult buildFinalResult(List<OcrComparisonMatchResult> matches, String originalExpected,
                                                 String candidatesList, double threshold, boolean hasCommaFormat) {
        boolean allWordsMatch = matches.stream()
                .allMatch(match -> match.getScore() >= threshold);

        boolean anyWordMatches = matches.stream()
                .anyMatch(match -> match.getScore() >= threshold);

        List<OcrComparisonMatchResult> matchedWords = matches.stream()
                .filter(match -> match.getScore() >= threshold)
                .collect(Collectors.toList());

        String method = determineMatchingMethod(matches, threshold);
        logger.debug("Matching method determined: {}", method);

        String matchSummary = matches.stream()
                .map(match -> String.format("%s:%.2f->%s", match.getOriginalExpected(), match.getScore(), match.getCandidate()))
                .collect(Collectors.joining("|"));

        String finalBestMatch;
        int finalScore;

        if (anyWordMatches) {
            finalBestMatch = reconstructBestMatch(matches, threshold, hasCommaFormat, originalExpected);

            double avgScore = matchedWords.stream()
                    .mapToDouble(OcrComparisonMatchResult::getScore)
                    .average()
                    .orElse(0.0);
            finalScore = (int) (avgScore * 100);
            logger.info("Match found with average score: {}", finalScore);
        } else {
            finalBestMatch = originalExpected;
            finalScore = matches.stream()
                    .mapToInt(match -> (int) (match.getScore() * 100))
                    .max()
                    .orElse(0);
            logger.info("No match found, best score: {}", finalScore);
        }

        logger.debug("Final result - isMatch: {}, score: {}, method: {}", allWordsMatch, finalScore, method);

        return OcrComparisonResult.builder()
                .isMatch(allWordsMatch)
                .bestMatch(finalBestMatch)
                .bestScore(finalScore)
                .matchingMethod(method)
                .candidatesList(candidatesList + "|MATCHES:" + matchSummary)
                .build();
    }

    private String reconstructBestMatch(List<OcrComparisonMatchResult> matches, double threshold,
                                        boolean hasCommaFormat, String originalExpected) {
        List<String> matchedWords = new ArrayList<>();

        for (OcrComparisonMatchResult match : matches) {
            if (match.getScore() >= threshold) {
                matchedWords.add(match.getRestoredMatch());
            }
        }

        if (matchedWords.isEmpty()) {
            return originalExpected;
        }

        if (hasCommaFormat) {
            int commaIndex = getCommaPositionInWords(originalExpected, matches);

            if (commaIndex > 0 && commaIndex < matchedWords.size()) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < matchedWords.size(); i++) {
                    if (i == commaIndex) {
                        result.append(", ");
                    } else if (i > 0) {
                        result.append(" ");
                    }
                    result.append(matchedWords.get(i));
                }
                return result.toString();
            }
        }

        // Default format without comma: "First Middle Last"
        return String.join(" ", matchedWords);
    }

    /**
     * Determines where the comma should be placed based on the original expected value
     */
    private int getCommaPositionInWords(String originalExpected, List<OcrComparisonMatchResult> matches) {
        List<String> expectedWords = extractAllWords(originalExpected);
        String[] parts = originalExpected.split(",");

        if (parts.length >= 2) {
            // Count words before the comma
            List<String> beforeComma = extractAllWords(parts[0]);
            return beforeComma.size();
        }

        return -1;
    }

    private String determineMatchingMethod(List<OcrComparisonMatchResult> matches, double threshold) {
        long matchCount = matches.stream()
                .filter(match -> match.getScore() >= threshold)
                .count();
        long totalCount = matches.size();

        logger.debug("Match statistics - matched: {}, total: {}", matchCount, totalCount);

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
}