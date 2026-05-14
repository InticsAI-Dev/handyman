package in.handyman.raven.lib.model.deepSiftSearch;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.util.regex.Pattern;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Locale;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_DEEP_SIFT_OUTPUT;

@Data
@AllArgsConstructor
@Builder
public class DeepSiftSearchConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftSearchInputTable, DeepSiftSearchOutputTable> {

    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final Integer pageContentMinLength;
    private static final String ENCRYPTION_ALGORITHM = "AES256";
    private static final String TEXT_DATA_TYPE = "TEXT_DATA";
    /**
     * N/A-like token on the same line as the date label (used in a lookahead). Allows junk between
     * the label and a well-formed N/A (e.g. OCR "n/an n/a") while still requiring a real marker on that line.
     */
    private static final String NA_MARKER_ON_LINE = "(?:n\\s*(?:[/\\\\|i1l]?\\s*)a|null|none|nil|not\\s+available)\\b";
    private static final Pattern INPATIENT_NA_PHRASE_PATTERN = Pattern.compile(
            "\\binpatient\\s+date(?:\\s*/\\s*time)?\\s*[:;\\-–—.,]?\\s*" +
                    "(?=[^\\n]*" + NA_MARKER_ON_LINE + ")" +
                    "[^\\n]+(?:\\r?\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    private static final Pattern OBSERVATION_NA_PHRASE_PATTERN = Pattern.compile(
            "\\bobservation\\s+date(?:\\s*/\\s*time)?\\s*[:;\\-–—.,]?\\s*" +
                    "(?=[^\\n]*" + NA_MARKER_ON_LINE + ")" +
                    "[^\\n]+(?:\\r?\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    @Override
    public List<DeepSiftSearchOutputTable> process(URL endpoint, DeepSiftSearchInputTable entity) {
        List<DeepSiftSearchOutputTable> outputRecords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        log.info(marker, "Starting process for sorItemId: {}", entity.getSorItemId());

        try {
            if (entity.getSorItemId() == null || entity.getSorItemName() == null || entity.getSearchName() == null) {
                log.error(marker, "Invalid input for sorItemId: {} - missing required fields", entity.getSorItemId());
                HandymanException handymanException = new HandymanException("Missing required fields: sorItemId, sorItemName, or searchType");
                HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(), handymanException, action);
                long elapsedTimeMs = System.currentTimeMillis() - startTime;
                outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        "Missing required fields", elapsedTimeMs));
                return outputRecords;
            }

            String searchType = entity.getSearchName().trim().toLowerCase();

            String decryptSotPageContent = action.getContext().get(ENCRYPT_DEEP_SIFT_OUTPUT);
            String extractedText = entity.getExtractedText() != null ? entity.getExtractedText(): "";
            String finalExtractedText = extractedText;
            if ("true".equals(decryptSotPageContent)) {
                InticsIntegrity decryption = SecurityEngine.getInticsIntegrityMethod(action, log);
                finalExtractedText = decryption.decrypt(extractedText, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
            }

            List<String> keywordList = new ArrayList<>();
            if (entity.getKeywords() != null && !entity.getKeywords().trim().isEmpty()) {
                keywordList = Arrays.stream(entity.getKeywords().split("\\s*,\\s*"))
                        .filter(k -> !k.isEmpty())
                        .collect(Collectors.toList());
            }

            List<String> matchedKeywords = new ArrayList<>();
            List<String> blockedKeywords = new ArrayList<>();
            boolean matchFound = false;

            log.debug(marker, "Processing searchType: {} for sorItemId: {}, total keywords: {}",
                    searchType, entity.getSorItemId(), keywordList.size());

            if ("exact".equals(searchType)) {
                for (String keyword : keywordList) {
                    String pattern = "\\b" + Pattern.quote(keyword) + "\\b";
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                            .matcher(finalExtractedText)
                            .find()) {
                        matchedKeywords.add(keyword);
                        matchFound = true;
                    }
                }
            } else if ("contains".equals(searchType)) {
                for (String kw : keywordList) {
                    if (finalExtractedText.toLowerCase().contains(kw.toLowerCase())) {
                        matchedKeywords.add(kw);
                        matchFound = true;
                    }
                }
            } else if ("empty_page".equals(searchType)) {
                String belowMinFlag = getBelowMinPageLengthFlag(finalExtractedText);

                matchedKeywords.add(belowMinFlag);
                matchFound = true;
            } else {
                log.error(marker, "Invalid searchType: {} for sorItemId: {}", searchType, entity.getSorItemId());
                HandymanException handymanException = new HandymanException("Invalid searchType: " + searchType);
                HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                        handymanException, action);
                long elapsedTimeMs = System.currentTimeMillis() - startTime;
                outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        "Invalid searchType", elapsedTimeMs));
                return outputRecords;
            }

            if (matchFound) {
                BlockingResult result = applyBlockingRules(
                        finalExtractedText,
                        matchedKeywords,
                        entity.getBlocked_keywords_json()
                );

                matchedKeywords = result.getAllowedKeywords();
                blockedKeywords = result.getBlockedKeywords();

                matchFound = !matchedKeywords.isEmpty();
            }

            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            if (matchFound) {
                log.info(marker, "Match found for sorItemId: {}, searchType: {}, matched keyword count: {}",
                        entity.getSorItemId(), searchType, matchedKeywords.size());
                outputRecords.add(DeepSiftSearchOutputTable.builder()
                        .sorItemId(entity.getSorItemId())
                        .sorItemName(entity.getSorItemName())
                        .sorContainerId(entity.getSorContainerId())
                        .sorContainerName(entity.getSorContainerName())
                        .sourceDocumentType(entity.getSourceDocumentType())
                        .originId(entity.getOriginId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .searchId(Math.toIntExact(entity.getSearchId()))
                        .searchName(entity.getSearchName())
                        .batchId(entity.getBatchId())
                        .tenantId(Math.toIntExact(entity.getTenantId()))
                        .createdOn(entity.getCreatedOn())
                        .createdBy(entity.getTenantId().toString())
                        .searchOutput(matchedKeywords)
                        .paperNo(entity.getPaperNo())
                        .groupId(entity.getGroupId())
                        .timeTakenMS(elapsedTimeMs)
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .blocked_output(blockedKeywords)
                        .build());
            } else {
                log.info(marker, "No keyword match found for sorItemId: {}", entity.getSorItemId());
                outputRecords.add(DeepSiftSearchOutputTable.builder()
                        .sorItemId(entity.getSorItemId())
                        .sorItemName(entity.getSorItemName())
                        .sorContainerId(entity.getSorContainerId())
                        .sorContainerName(entity.getSorContainerName())
                        .sourceDocumentType(entity.getSourceDocumentType())
                        .originId(entity.getOriginId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .searchId(Math.toIntExact(entity.getSearchId()))
                        .searchName(entity.getSearchName())
                        .batchId(entity.getBatchId())
                        .tenantId(Math.toIntExact(entity.getTenantId()))
                        .createdOn(entity.getCreatedOn())
                        .createdBy(entity.getTenantId().toString())
                        .searchOutput(matchedKeywords)
                        .paperNo(entity.getPaperNo())
                        .groupId(entity.getGroupId())
                        .timeTakenMS(elapsedTimeMs)
                        .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                        .blocked_output(blockedKeywords)
                        .build());
            }

        } catch (Exception e) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            log.error(marker, "Exception processing sorItemId: {}", entity.getSorItemId(), e);
            HandymanException handymanException = new HandymanException("Deep sift search failed for sorItemId: " +
                    entity.getSorItemId(), e);
            HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                    handymanException, action);
            outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    ExceptionUtil.toString(e), elapsedTimeMs));
        }

        long elapsedTimeMs = System.currentTimeMillis() - startTime;
        log.info(marker, "Completed process for sorItemId: {}, output records: {}, time taken: {} ms",
                entity.getSorItemId(), outputRecords.size(), elapsedTimeMs);
        return outputRecords;
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private List<BlockedKeywordRule> parseBlockedRules(String blockedKeywordsJson) {
        try {
            if (blockedKeywordsJson == null || blockedKeywordsJson.trim().isEmpty()) {
                return Collections.emptyList();
            }

            List<BlockedKeywordRule> rules = OBJECT_MAPPER.readValue(
                    blockedKeywordsJson,
                    new TypeReference<List<BlockedKeywordRule>>() {});

            for (BlockedKeywordRule r : rules) {
                if (r.getLabel() != null) {
                    r.setLabel(r.getLabel().trim().toLowerCase());
                }
                if (r.getValue() != null) {
                    r.setValue(r.getValue().trim().toLowerCase());
                }
            }

            return rules;

        } catch (Exception e) {
            log.error(marker, "Failed to parse blocked keyword JSON: {}", blockedKeywordsJson, e);
            return Collections.emptyList();
        }
    }

    private BlockingResult applyBlockingRules(
            String ocrText,
            List<String> matchedKeywords,
            String blockedKeywordsJson) {

        List<BlockedKeywordRule> blockedRules = parseBlockedRules(blockedKeywordsJson);

        log.info(marker, "Blocklisting JSON: {}", blockedRules);

        if (matchedKeywords == null || matchedKeywords.isEmpty()) {
            return new BlockingResult(Collections.emptyList(), Collections.emptyList());
        }

        if (blockedRules.isEmpty()) {
            return applyNaDatePhraseBlocking(ocrText, matchedKeywords, Collections.emptyList());
        }

        String normalizedText = ocrText == null ? "" : ocrText.toLowerCase();

        List<String> allowedKeywords = new ArrayList<>();
        List<String> blockedKeywords = new ArrayList<>();

        for (String keyword : matchedKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) continue;

            String normalizedKeyword = keyword.trim().toLowerCase();

            if (isInsideAddress(normalizedText, keyword)) {
                log.info(marker, "Keyword '{}' ignored as it appears inside address context", keyword);
                continue;
            }

            boolean isBlocked = false;

            for (BlockedKeywordRule rule : blockedRules) {
                if (!normalizedKeyword.equals(rule.getValue())) continue;

                String[] labels = rule.getLabel().split("\\s*,\\s*");

                String textWithoutAllLabels = normalizedText;

                for (String label : labels) {
                    if (label != null && !label.trim().isEmpty()) {
                        textWithoutAllLabels = textWithoutAllLabels.replaceAll(
                                "\\b" + Pattern.quote(label.trim()) + "\\b", "");
                    }
                }

                boolean existsElsewhere = Pattern.compile("\\b" + Pattern.quote(normalizedKeyword) + "\\b")
                        .matcher(textWithoutAllLabels)
                        .find();

                if (!existsElsewhere) {
                    blockedKeywords.add(keyword);
                    log.info(marker, "Blocked keyword '{}' only found inside phrases '{}'", keyword, rule.getLabel());
                    isBlocked = true;
                    break;
                } else {
                    log.info(marker, "Keyword '{}' also exists outside phrases '{}', not blocking", keyword, rule.getLabel());
                }
            }

            if (!isBlocked) {
                allowedKeywords.add(keyword);
            }
        }

        return applyNaDatePhraseBlocking(ocrText, allowedKeywords, blockedKeywords);
    }

    private BlockingResult applyNaDatePhraseBlocking(
            String ocrText,
            List<String> matchedKeywords,
            List<String> blockedKeywords) {
        if (matchedKeywords == null || matchedKeywords.isEmpty()) {
            return new BlockingResult(Collections.emptyList(), blockedKeywords == null ? Collections.emptyList() : blockedKeywords);
        }

        String normalizedText = ocrText == null ? "" : ocrText.toLowerCase(Locale.ROOT);
        List<String> allowedKeywords = new ArrayList<>();
        List<String> finalBlockedKeywords = blockedKeywords == null ? new ArrayList<>() : blockedKeywords;

        String textWithoutInpatientNaPhrase = INPATIENT_NA_PHRASE_PATTERN.matcher(normalizedText).replaceAll(" ");
        String textWithoutObservationNaPhrase = OBSERVATION_NA_PHRASE_PATTERN.matcher(normalizedText).replaceAll(" ");

        for (String keyword : matchedKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }

            String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
            boolean isBlocked = false;

            if ("inpatient".equals(normalizedKeyword)) {
                boolean keywordExistsOutsidePhrase = Pattern.compile("\\binpatient\\b")
                        .matcher(textWithoutInpatientNaPhrase)
                        .find();
                if (!keywordExistsOutsidePhrase && INPATIENT_NA_PHRASE_PATTERN.matcher(normalizedText).find()) {
                    finalBlockedKeywords.add(keyword);
                    isBlocked = true;
                    log.info(marker, "Blocked keyword '{}' found only in 'Inpatient Date: N/A' phrase", keyword);
                }
            } else if ("observation".equals(normalizedKeyword)) {
                boolean keywordExistsOutsidePhrase = Pattern.compile("\\bobservation\\b")
                        .matcher(textWithoutObservationNaPhrase)
                        .find();
                if (!keywordExistsOutsidePhrase && OBSERVATION_NA_PHRASE_PATTERN.matcher(normalizedText).find()) {
                    finalBlockedKeywords.add(keyword);
                    isBlocked = true;
                    log.info(marker, "Blocked keyword '{}' found only in 'Observation Date/Time ... N/A' phrase", keyword);
                }
            }

            if (!isBlocked) {
                allowedKeywords.add(keyword);
            }
        }

        return new BlockingResult(allowedKeywords, finalBlockedKeywords);
    }

    private boolean isInsideAddress(String text, String keyword) {

        if (text == null || keyword == null ||
                text.trim().isEmpty() || keyword.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        for (String line : lowerText.split("\\n")) {

            if (!line.contains(lowerKeyword)) {
                continue;
            }

            boolean hasNumber = line.matches(".*\\b\\d{1,6}\\b.*");

            boolean hasStreetWord = line.matches(
                    ".*\\b(st|street|rd|road|ave|avenue|dr|drive|blvd|lane|ln|way|court|ct)\\b.*");

            boolean hasStateZip = line.matches(
                    ".*\\b[a-z]{2}\\b\\s+\\d{5}(-\\d{4})?.*");

            boolean hasSuite = line.matches(
                    ".*\\b(suite|ste|unit|apt)\\b.*");

            if ((hasNumber && hasStreetWord) ||
                    hasStateZip ||
                    (hasSuite && hasNumber)) {
                return true;
            }
        }

        return false;
    }

    private String getBelowMinPageLengthFlag(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "Y";
        }
        int wordCount = text.trim().split("\\s+").length;
        return wordCount < pageContentMinLength ? "Y" : "N";
    }

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status, String message, long timeTakenMS) {
        return buildOutputTable(entity, status, message, new ArrayList<>(), timeTakenMS, new ArrayList<>());
    }

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status, String message, List<String> matchedKeywords, long timeTakenMS, List<String> blockedKeywords) {
        log.debug(marker, "Building output table for sorItemId: {} with status: {}", entity.getSorItemId(), status);
        return DeepSiftSearchOutputTable.builder()
                .sorItemId(entity.getSorItemId())
                .sorItemName(entity.getSorItemName())
                .sorContainerId(entity.getSorContainerId())
                .sorContainerName(entity.getSorContainerName())
                .sourceDocumentType(entity.getSourceDocumentType())
                .originId(entity.getOriginId())
                .rootPipelineId(entity.getRootPipelineId())
                .searchId(Math.toIntExact(entity.getSearchId()))
                .searchName(entity.getSearchName())
                .batchId(entity.getBatchId())
                .tenantId(Math.toIntExact(entity.getTenantId()))
                .createdOn(entity.getCreatedOn())
                .createdBy(entity.getTenantId().toString())
                .searchOutput(matchedKeywords)
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .timeTakenMS(timeTakenMS)
                .status(status)
                .blocked_output(blockedKeywords)
                .build();
    }

    @Data
    public static class BlockedKeywordRule {
        private String label;
        private String value;
    }

    @Data
    @AllArgsConstructor
    public class BlockingResult {
        private List<String> allowedKeywords;
        private List<String> blockedKeywords;
    }

}