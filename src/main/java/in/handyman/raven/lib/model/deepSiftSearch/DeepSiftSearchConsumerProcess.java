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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final String DEEP_SIFT_BBOX_EXTRACTION_ACTIVATOR = "deep.sift.bbox.extraction.activator";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Pattern INPATIENT_NA_PHRASE_PATTERN = Pattern.compile(
            "\\binpatient\\s+date(?:\\s*/\\s*time)?\\s*[:;\\-–—.,]?\\s*" +
                    "(?:(?:n\\s*(?:[/\\\\|i1l]?\\s*)a|null|none|nil|not\\s+available)\\b\\s*)+" +
                    "(?:\\r?\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );
    private static final Pattern OBSERVATION_NA_PHRASE_PATTERN = Pattern.compile(
            "\\bobservation\\s+date(?:\\s*/\\s*time)?\\s*[:;\\-–—.,]?\\s*" +
                    "(?:(?:n\\s*(?:[/\\\\|i1l]?\\s*)a|null|none|nil|not\\s+available)\\b\\s*)+" +
                    "(?:\\r?\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    @Override
    public List<DeepSiftSearchOutputTable> process(URL endpoint, DeepSiftSearchInputTable entity) {
        List<DeepSiftSearchOutputTable> outputRecords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        log.info(marker, "Starting process | sorItemId={} originId={} paperNo={} rootPipelineId={}",
                entity.getSorItemId(), entity.getOriginId(), entity.getPaperNo(), entity.getRootPipelineId());

        try {
            if (entity.getSorItemId() == null || entity.getSorItemName() == null || entity.getSearchName() == null) {
                log.error(marker, "Invalid input, missing required fields | sorItemId={} originId={} paperNo={}",
                        entity.getSorItemId(), entity.getOriginId(), entity.getPaperNo());
                HandymanException handymanException = new HandymanException(
                        "Missing required fields: sorItemId, sorItemName, or searchType");
                HandymanException.insertException(
                        "Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                        handymanException, action);
                long elapsedTimeMs = System.currentTimeMillis() - startTime;
                outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        "Missing required fields", elapsedTimeMs));
                return outputRecords;
            }

            String searchType = entity.getSearchName().trim().toLowerCase();
            String decryptSotPageContent = action.getContext().get(ENCRYPT_DEEP_SIFT_OUTPUT);
            boolean deepSiftBboxActivator = Boolean.parseBoolean(
                    action.getContext().getOrDefault(DEEP_SIFT_BBOX_EXTRACTION_ACTIVATOR, "false"));

            String extractedText = entity.getExtractedText() != null ? entity.getExtractedText() : "";
            String extractedTextWithBbox = entity.getExtractedTextWithBbox() != null
                    ? entity.getExtractedTextWithBbox() : "";

            String finalExtractedText = extractedText;
            String finalExtractedTextWithBbox = extractedTextWithBbox;

            if ("true".equals(decryptSotPageContent)) {
                InticsIntegrity decryption = SecurityEngine.getInticsIntegrityMethod(action, log);
                if (!extractedText.isEmpty()) {
                    finalExtractedText = decryption.decrypt(extractedText, ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
                }
                if (deepSiftBboxActivator && !extractedTextWithBbox.isEmpty()) {
                    finalExtractedTextWithBbox = decryption.decrypt(extractedTextWithBbox,
                            ENCRYPTION_ALGORITHM, TEXT_DATA_TYPE);
                }
            }

            List<Map<String, Object>> bboxNodes = Collections.emptyList();
            Map<String, List<Integer>> bboxWordIndex = Collections.emptyMap();
            if (deepSiftBboxActivator && !finalExtractedTextWithBbox.isEmpty()) {
                bboxNodes = parseBboxNodes(finalExtractedTextWithBbox);
                bboxWordIndex = buildBboxWordIndex(bboxNodes);
                log.debug(marker, "Bbox index built | sorItemId={} originId={} nodes={} uniqueWords={}",
                        entity.getSorItemId(), entity.getOriginId(), bboxNodes.size(), bboxWordIndex.size());
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

            log.debug(marker, "Matching | searchType={} sorItemId={} keywordCount={}",
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
                log.error(marker, "Invalid searchType | searchType={} sorItemId={} originId={}",
                        searchType, entity.getSorItemId(), entity.getOriginId());
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

            String searchOutputBboxJson = null;
            if (deepSiftBboxActivator && matchFound && !bboxNodes.isEmpty()
                    && !"empty_page".equals(searchType)) {
                searchOutputBboxJson = buildMatchedBboxJson(matchedKeywords, searchType, bboxNodes, bboxWordIndex);
            }

            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            if (matchFound) {
                log.info(marker, "Match found | sorItemId={} originId={} paperNo={} searchType={} "
                                + "matchedCount={} blockedCount={} bboxResolved={} timeMs={}",
                        entity.getSorItemId(), entity.getOriginId(), entity.getPaperNo(), searchType,
                        matchedKeywords.size(), blockedKeywords.size(),
                        searchOutputBboxJson != null, elapsedTimeMs);
            } else {
                log.info(marker, "No match | sorItemId={} originId={} paperNo={} timeMs={}",
                        entity.getSorItemId(), entity.getOriginId(), entity.getPaperNo(), elapsedTimeMs);
            }

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
                    .searchOutputBbox(searchOutputBboxJson)
                    .paperNo(entity.getPaperNo())
                    .groupId(entity.getGroupId())
                    .timeTakenMS(elapsedTimeMs)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .blocked_output(blockedKeywords)
                    .build());

        } catch (Exception e) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            log.error(marker, "Exception | sorItemId={} originId={} paperNo={} rootPipelineId={}",
                    entity.getSorItemId(), entity.getOriginId(), entity.getPaperNo(),
                    entity.getRootPipelineId(), e);
            HandymanException handymanException = new HandymanException(
                    "Deep sift search failed for sorItemId: " + entity.getSorItemId(), e);
            HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                    handymanException, action);
            outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    ExceptionUtil.toString(e), elapsedTimeMs));
        }

        long elapsedTimeMs = System.currentTimeMillis() - startTime;
        log.info(marker, "Completed process | sorItemId={} originId={} records={} timeMs={}",
                entity.getSorItemId(), entity.getOriginId(), outputRecords.size(), elapsedTimeMs);
        return outputRecords;
    }

    private List<Map<String, Object>> parseBboxNodes(String bboxJson) {
        if (bboxJson == null) {
            return Collections.emptyList();
        }
        String trimmed = bboxJson.trim();
        if (trimmed.isEmpty() || "[]".equals(trimmed)) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> nodes = OBJECT_MAPPER.readValue(
                    trimmed,
                    new TypeReference<>() {
                    }
            );
            return nodes != null ? nodes : Collections.emptyList();
        } catch (Exception e) {
            log.error(marker, "Failed to parse bbox JSON nodes");
            return Collections.emptyList();
        }
    }

    private Map<String, List<Integer>> buildBboxWordIndex(List<Map<String, Object>> bboxNodes) {
        if (bboxNodes.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<Integer>> index = new HashMap<>(bboxNodes.size() * 2);
        for (int i = 0; i < bboxNodes.size(); i++) {
            Object textObj = bboxNodes.get(i).get("text");
            if (textObj == null) {
                continue;
            }
            String text = textObj.toString().trim().toLowerCase(Locale.ROOT);
            if (text.isEmpty()) {
                continue;
            }
            index.computeIfAbsent(text, k -> new ArrayList<>(2)).add(i);
        }
        return index;
    }

    private String buildMatchedBboxJson(List<String> matchedKeywords, String searchType,
                                        List<Map<String, Object>> bboxNodes,
                                        Map<String, List<Integer>> bboxWordIndex) {
        List<Map<String, Object>> result = new ArrayList<>(matchedKeywords.size());
        for (String keyword : matchedKeywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                continue;
            }
            Map<String, Object> bbox = findKeywordBbox(keyword, searchType, bboxNodes, bboxWordIndex);
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("keyword", keyword);
            entry.put("bbox", bbox);
            result.add(entry);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        } catch (Exception e) {
            log.error(marker, "Failed to serialize matched bbox payload");
            return null;
        }
    }

    private Map<String, Object> findKeywordBbox(String keyword, String searchType,
                                                List<Map<String, Object>> bboxNodes,
                                                Map<String, List<Integer>> bboxWordIndex) {
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        String[] tokens = normalized.split("\\s+");
        if (tokens.length == 0) {
            return null;
        }

        boolean useContains = "contains".equals(searchType);

        if (useContains) {
            for (int i = 0; i < bboxNodes.size(); i++) {
                Object textObj = bboxNodes.get(i).get("text");
                if (textObj == null) {
                    continue;
                }
                String nodeText = textObj.toString().toLowerCase(Locale.ROOT);
                if (nodeText.contains(tokens[0])) {
                    if (tokens.length == 1 || phraseMatchesAt(tokens, bboxNodes, i, true)) {
                        return mergeBboxRange(bboxNodes, i, tokens.length);
                    }
                }
            }
            return null;
        }

        List<Integer> candidates = bboxWordIndex.get(tokens[0]);
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (int startIdx : candidates) {
            if (tokens.length == 1 || phraseMatchesAt(tokens, bboxNodes, startIdx, false)) {
                return mergeBboxRange(bboxNodes, startIdx, tokens.length);
            }
        }
        return null;
    }

    private boolean phraseMatchesAt(String[] tokens, List<Map<String, Object>> bboxNodes,
                                    int startIdx, boolean useContains) {
        if (startIdx + tokens.length > bboxNodes.size()) {
            return false;
        }
        for (int i = 1; i < tokens.length; i++) {
            Object textObj = bboxNodes.get(startIdx + i).get("text");
            if (textObj == null) {
                return false;
            }
            String nodeText = textObj.toString().toLowerCase(Locale.ROOT);
            if (useContains) {
                if (!nodeText.contains(tokens[i])) {
                    return false;
                }
            } else {
                if (!nodeText.equals(tokens[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private Map<String, Object> mergeBboxRange(List<Map<String, Object>> bboxNodes, int startIdx, int count) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean any = false;

        for (int i = startIdx; i < startIdx + count && i < bboxNodes.size(); i++) {
            Object bboxObj = bboxNodes.get(i).get("bbox");
            if (!(bboxObj instanceof List<?> bbox)) {
                continue;
            }
            if (bbox.size() < 4) {
                continue;
            }
            int x1 = toIntSafe(bbox.get(0));
            int y1 = toIntSafe(bbox.get(1));
            int x2 = toIntSafe(bbox.get(2));
            int y2 = toIntSafe(bbox.get(3));
            if (x1 < minX) minX = x1;
            if (y1 < minY) minY = y1;
            if (x2 > maxX) maxX = x2;
            if (y2 > maxY) maxY = y2;
            any = true;
        }

        if (!any) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topLeftX", minX);
        result.put("topLeftY", minY);
        result.put("bottomRightX", maxX);
        result.put("bottomRightY", maxY);
        return result;
    }

    private int toIntSafe(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<BlockedKeywordRule> parseBlockedRules(String blockedKeywordsJson) {
        try {
            if (blockedKeywordsJson == null || blockedKeywordsJson.trim().isEmpty()) {
                return Collections.emptyList();
            }

            List<BlockedKeywordRule> rules = OBJECT_MAPPER.readValue(
                    blockedKeywordsJson,
                    new TypeReference<>() {
                    });

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
            log.error(marker, "Failed to parse blocked keyword JSON rules");
            return Collections.emptyList();
        }
    }

    private BlockingResult applyBlockingRules(String ocrText, List<String> matchedKeywords,
                                              String blockedKeywordsJson) {

        List<BlockedKeywordRule> blockedRules = parseBlockedRules(blockedKeywordsJson);

        log.debug(marker, "Blocking rules loaded | count={}", blockedRules.size());

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
                log.debug(marker, "Keyword ignored due to address context");
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
                    log.debug(marker, "Keyword blocked by rule");
                    isBlocked = true;
                    break;
                } else {
                    log.debug(marker, "Keyword present outside rule context, allowed");
                }
            }

            if (!isBlocked) {
                allowedKeywords.add(keyword);
            }
        }

        return applyNaDatePhraseBlocking(ocrText, allowedKeywords, blockedKeywords);
    }

    private BlockingResult applyNaDatePhraseBlocking(String ocrText, List<String> matchedKeywords,
                                                     List<String> blockedKeywords) {
        if (matchedKeywords == null || matchedKeywords.isEmpty()) {
            return new BlockingResult(Collections.emptyList(),
                    blockedKeywords == null ? Collections.emptyList() : blockedKeywords);
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
                    log.debug(marker, "Keyword blocked by inpatient N/A phrase");
                }
            } else if ("observation".equals(normalizedKeyword)) {
                boolean keywordExistsOutsidePhrase = Pattern.compile("\\bobservation\\b")
                        .matcher(textWithoutObservationNaPhrase)
                        .find();
                if (!keywordExistsOutsidePhrase && OBSERVATION_NA_PHRASE_PATTERN.matcher(normalizedText).find()) {
                    finalBlockedKeywords.add(keyword);
                    isBlocked = true;
                    log.debug(marker, "Keyword blocked by observation N/A phrase");
                }
            }

            if (!isBlocked) {
                allowedKeywords.add(keyword);
            }
        }

        return new BlockingResult(allowedKeywords, finalBlockedKeywords);
    }

    private boolean isInsideAddress(String text, String keyword) {
        if (text == null || keyword == null
                || text.trim().isEmpty() || keyword.trim().isEmpty()) {
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
            boolean hasStateZip = line.matches(".*\\b[a-z]{2}\\b\\s+\\d{5}(-\\d{4})?.*");
            boolean hasSuite = line.matches(".*\\b(suite|ste|unit|apt)\\b.*");

            if ((hasNumber && hasStreetWord) || hasStateZip || (hasSuite && hasNumber)) {
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

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status,
                                                       String message, long timeTakenMS) {
        return buildOutputTable(entity, status, message, new ArrayList<>(), timeTakenMS, new ArrayList<>());
    }

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status, String message,
                                                       List<String> matchedKeywords, long timeTakenMS,
                                                       List<String> blockedKeywords) {
        log.debug(marker, "Building output | sorItemId={} status={}", entity.getSorItemId(), status);
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
                .searchOutputBbox(null)
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
    public static class BlockingResult {
        private List<String> allowedKeywords;
        private List<String> blockedKeywords;
    }
}