package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultiValueMemberMapper;
import in.handyman.raven.lib.model.multi.member.indicator.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MultiValueMemberConsumerProcess {
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final List<MultiValueMemberMapperTransformInputTable> multiValueMemberMapperTransformInputTables;
    private final Long tenantId;
    private final ExecutorService executor;

    private final MultiValueMemberMapper multiValueMemberMapper;

    public static final String INSERT_INTO = "INSERT INTO ";

    public static final String TABLE_NAME = "voting.multi_member_final_audit";

    public static final String INSERT_COLUMNS = "root_pipeline_id, created_on, last_updated_on, created_user_id, last_updated_user_id, batch_id, origin_id, group_id, document_type, tenant_id, paper_no, sor_item_name, indicator_type, comments";

    public static final String INSERT_VALUES = "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String PROCESSING_SOR_ITEM_NAME = "multi.member.indicator.fields";

    private final String DEFAULT_CONFIDENCE_SCORE = "radon.kvp.bbox.vqa.score.default";
    private final String MULTI_MEMBER_NAME_THRESHOLD = "multi.member.name.similarity.threshold";
    private final String MULTI_MEMBER_ID_THRESHOLD = "multi.member.id.similarity.threshold";
    private final String MULTI_MEMBER_VOTING_VERSION = "multi.member.voting.v1";
    private static final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();

    public MultiValueMemberConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, List<MultiValueMemberMapperTransformInputTable> multiValueMemberMapperTransformInputTables, Long tenantId, Integer threadCount, MultiValueMemberMapper multiValueMemberMapper) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.multiValueMemberMapperTransformInputTables = multiValueMemberMapperTransformInputTables;
        this.tenantId = tenantId;
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.multiValueMemberMapper = multiValueMemberMapper;
    }

    public List<MultiValueMemberMapperOutputTable> doMultiMemberValidation() throws Exception {
        log.info(marker, "Starting MultiValueMemberMapper process for tenantId={} actionId={}", tenantId, action.getActionId());

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multiValueMemberMapper.getResourceConn());

        List<MultiValueMemberMapperOutputTable> finalOutput = Collections.synchronizedList(new ArrayList<>());

        String processingSorItemName = action.getContext().get(PROCESSING_SOR_ITEM_NAME);

        String votingFeatureFlags = action.getContext().get(MULTI_MEMBER_VOTING_VERSION);
        log.debug(marker, "Processing SOR item name(s): {}", processingSorItemName);

        try {
            log.info(marker, "Processing MultiValueMemberMapper for tenantId: {}, actionId: {}", tenantId, action.getActionId());

            Set<String> targetSorItems = Arrays.stream(processingSorItemName.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            List<Future<?>> futures = new ArrayList<>();
            for (MultiValueMemberMapperTransformInputTable inputTable : multiValueMemberMapperTransformInputTables) {
                futures.add(executor.submit(() -> {
                    String threadName = Thread.currentThread().getName();
                    String originId = inputTable.getOriginId();

                    log.info(marker, "[{}] Processing originId={} on thread={}", action.getActionId(), originId, threadName);

                    double nameSimilarityThreshold = Double.parseDouble(action.getContext().get(MULTI_MEMBER_NAME_THRESHOLD));
                    double idSimilarityThreshold = Double.parseDouble(action.getContext().get(MULTI_MEMBER_ID_THRESHOLD));

                    log.debug(marker, "[{}] Name threshold={} ID threshold={}", originId, nameSimilarityThreshold, idSimilarityThreshold);

                    try {
                        MultipleMemberSummary result;
                        if(votingFeatureFlags.equals("true")) {
                            result = evaluateMultivaluePresenceAndUniquenessVersion1(inputTable, targetSorItems, log);
                        } else {
                            result = evaluateMultivaluePresenceAndUniquenessVersion2(inputTable, targetSorItems, nameSimilarityThreshold, idSimilarityThreshold, log);
                        }
                        MultiValueMemberMapperOutputTable outputRow = outputTableCreation(inputTable, result.getOutput());
                        finalOutput.add(outputRow);

                        jdbi.useTransaction(handle -> {
                            log.info(marker, "[{}] Inserting audit entries...", originId);
                            executeMMIAuditInsert(handle, result, originId);
                            log.info("Executed audit insert for originId: {}", originId);
                        });
                        log.info(marker, "SUCCESS for originId: {}", originId);
                    } catch (Exception e) {
                        log.error(marker, "ERROR processing originId: {}", originId, e);
                        throw new HandymanException("Error processing input table for originId: " + originId, e);
                    }
                }));

            }

            for (Future<?> future : futures) {
                future.get();
            }
            log.info(marker, "Processing completed successfully for tenantId: {}, actionId: {}", tenantId, action.getActionId());
        } catch (Exception e) {
            log.error(marker, "Error processing MultiValueMemberMapper for tenantId: {}, actionId: {}.", tenantId, action.getActionId(), e);
            HandymanException handymanException = new HandymanException("Error processing MultiValueMemberMapper", e);
            HandymanException.insertException("Error processing MultiValueMemberMapper", handymanException, action);
        } finally {
            log.info(marker, "Shutting down executor service...");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    log.warn(marker, "Executor did not terminate in the specified time.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException ie) {
                log.error(marker, "Executor termination interrupted.", ie);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return finalOutput;
    }

    private static String collectValuesAndMetadata(List<extractedSorItemList> multiValueMember, Set<String> targetSorItems, Map<String, Set<String>> valuesPerSorItem, Set<String> presentSorItems, Set<String> pageNumbersSet, List<String> firstNames, List<String> lastNames, Logger log) {
        String documentType = "";
        for (extractedSorItemList row : multiValueMember) {
            String sorItemName = row.getSorItemName();
            String predictedValue = row.getPredictedValue();

            log.info("Processing sorItemName: {}", sorItemName);

            if (documentType.isEmpty() && row.getDocumentType() != null) {
                documentType = row.getDocumentType();
            }

            if (sorItemName != null && predictedValue != null && targetSorItems.contains(sorItemName)) {
                presentSorItems.add(sorItemName);
                if (row.getPaperNo() != null) {
                    pageNumbersSet.add(row.getPaperNo().toString());
                }

                Set<String> values = valuesPerSorItem.computeIfAbsent(sorItemName, k -> new HashSet<>());
                Arrays.stream(predictedValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(value -> {
                            values.add(value);
                            if ("member_first_name".equalsIgnoreCase(sorItemName)) {
                                firstNames.add(value);
                            }
                            if ("member_last_name".equalsIgnoreCase(sorItemName)) {
                                lastNames.add(value);
                            }
                        });
            }
        }
        return documentType;
    }

    public static MultipleMemberSummary evaluateMultivaluePresenceAndUniquenessVersion1(MultiValueMemberMapperTransformInputTable inputRows, Set<String> targetSorItems, Logger log) {
        log.info("Starting evaluation for originId: {}", inputRows.getOriginId());

        List<extractedSorItemList> multiValueMember = inputRows.getSorItemList();

        Map<String, Set<String>> valuesPerSorItem = new HashMap<>();
        Set<String> presentSorItems = new HashSet<>();
        Set<String> pageNumbersSet = new HashSet<>();
        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();
        String documentType = "";

        documentType = collectValuesAndMetadata(multiValueMember, targetSorItems, valuesPerSorItem, presentSorItems, pageNumbersSet, firstNames, lastNames, log);

        String pageNo = composePageNumbers(pageNumbersSet);
        Set<String> canonicalFullNames = buildCanonicalFullNames(firstNames, lastNames);

        System.out.println(canonicalFullNames);

        List<ValueTrace> valueTraces = buildValueTracesVersion1(valuesPerSorItem, canonicalFullNames);

        return determineOutputAndBuildSummary(inputRows, targetSorItems, valuesPerSorItem, presentSorItems, documentType, pageNo, canonicalFullNames, valueTraces, log);
    }

    private static List<ValueTrace> buildValueTracesVersion1(Map<String, Set<String>> valuesPerSorItem, Set<String> canonicalFullNames) {
        List<ValueTrace> valueTraces = new ArrayList<>();
        if (valuesPerSorItem.containsKey("member_id")) {
            valueTraces.add(ValueTrace.builder().key("member_id").values(String.join(",", valuesPerSorItem.get("member_id"))).build());
        }
        valueTraces.add(ValueTrace.builder().key("member_full_name").values(String.join(",", canonicalFullNames)).build());
        if (valuesPerSorItem.containsKey("member_date_of_birth")) {
            valueTraces.add(ValueTrace.builder().key("member_date_of_birth").values(String.join(",", valuesPerSorItem.get("member_date_of_birth"))).build());
        }
        return valueTraces;
    }

    private static MultipleMemberSummary determineOutputAndBuildSummary(MultiValueMemberMapperTransformInputTable inputRows, Set<String> targetSorItems, Map<String, Set<String>> valuesPerSorItem, Set<String> presentSorItems, String documentType, String pageNo, Set<String> canonicalFullNames, List<ValueTrace> valueTraces, Logger log) {
        int canonicalFullNameCount = canonicalFullNames.size();
        int lastNameCount = valuesPerSorItem.getOrDefault("member_last_name", Collections.emptySet()).size();
        int memberIdCount = valuesPerSorItem.getOrDefault("member_id", Collections.emptySet()).size();
        int dobCount = valuesPerSorItem.getOrDefault("member_date_of_birth", Collections.emptySet()).size();

        String comments;
        String output;

        if (!presentSorItems.containsAll(targetSorItems)) {
            String missingItems = targetSorItems.stream().filter(s -> !presentSorItems.contains(s)).collect(Collectors.joining(", "));
            comments = String.format("Missing target SOR items: [%s]. Cannot confirm multiple members.", missingItems);
            log.info(comments);
            output = "N";
        } else if (lastNameCount == 1) {
            comments = "Only one unique last name found, indicating a single member.";
            log.info(comments);
            output = "N";
        } else if ("MEDICAL_COMMERCIAL".equalsIgnoreCase(documentType)) {
            comments = String.format("COMMERCIAL document: member_id unique count = %d, canonical full name unique count = %d, dob unique count = %d.", memberIdCount, canonicalFullNameCount, dobCount);

            log.info(comments);
            output = (memberIdCount > 1 && canonicalFullNameCount > 1 && dobCount > 1) ? "Y" : "N";
            comments += output.equals("Y") ? " All these counts are >1, indicating multiple members." : " One or more counts are <=1, indicating a single member.";
        } else if ("MEDICAL_GBD".equalsIgnoreCase(documentType)) {
            comments = String.format("GBD document: member_id unique count = %d, canonical full name unique count = %d.", memberIdCount, canonicalFullNameCount);

            log.info(comments);
            output = (memberIdCount > 1 || canonicalFullNameCount > 1) ? "Y" : "N";
            comments += output.equals("Y") ? " Either member_id count or canonical full name count is >1, indicating multiple members." : " Both counts are <=1, indicating a single member.";
        } else {
            comments = String.format("Unknown document type '%s'. Insufficient data to determine member multiplicity.", documentType);
            log.info(comments);
            output = "N";
        }

        return MultipleMemberSummary.builder()
                .pageNo(pageNo)
                .output(output)
                .comments(comments)
                .valueTraces(valueTraces)
                .build();
    }

    public static MultipleMemberSummary evaluateMultivaluePresenceAndUniquenessVersion2(MultiValueMemberMapperTransformInputTable inputRows, Set<String> targetSorItems, double nameThreshold, double idThreshold, Logger log) {
        log.info("Evaluating multi-member presence for originId={}", inputRows.getOriginId());
        log.debug("Target SOR items: {}", targetSorItems);

        List<extractedSorItemList> multiValueMember = inputRows.getSorItemList();

        Map<String, List<String>> rawValuesPerSorItem = new HashMap<>();
        Set<String> presentSorItems = new HashSet<>();
        Set<String> pageNumbersSet = new HashSet<>();
        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();
        String documentType = "";

        for (extractedSorItemList row : multiValueMember) {
            String sorItemName = row.getSorItemName();
            String predictedValue = row.getPredictedValue();

            log.info("Processing sorItemName: {}", sorItemName);

            if (documentType.isEmpty() && row.getDocumentType() != null) {
                documentType = row.getDocumentType();
            }

            if (sorItemName != null && predictedValue != null && targetSorItems.contains(sorItemName)) {
                presentSorItems.add(sorItemName);
                if (row.getPaperNo() != null) {
                    pageNumbersSet.add(row.getPaperNo().toString());
                }

                List<String> values = rawValuesPerSorItem.computeIfAbsent(sorItemName, k -> new ArrayList<>());
                Arrays.stream(predictedValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(value -> {
                            values.add(value);
                            if ("member_first_name".equalsIgnoreCase(sorItemName)) {
                                firstNames.add(value);
                            }
                            if ("member_last_name".equalsIgnoreCase(sorItemName)) {
                                lastNames.add(value);
                            }
                        });
            }
        }

        String pageNo = composePageNumbers(pageNumbersSet);
        log.debug("Page numbers composed: {}", pageNo);

        Set<String> canonicalFullNames = buildCanonicalFullNames(firstNames, lastNames);

        System.out.println(canonicalFullNames);

        int canonicalFullNameCount = clusterAndCount(new ArrayList<>(canonicalFullNames), nameThreshold);
        int memberIdCount = clusterAndCount(rawValuesPerSorItem.getOrDefault("member_id", Collections.emptyList()), idThreshold);
        int dobCount = rawValuesPerSorItem.getOrDefault("member_date_of_birth", Collections.emptyList()).size();

        log.info("Cluster summary -> Name clusters={} ID clusters={} DOB count={}", canonicalFullNameCount, memberIdCount, dobCount);

        String comments;
        String output;

        if (!presentSorItems.containsAll(targetSorItems)) {
            String missingItems = targetSorItems.stream().filter(s -> !presentSorItems.contains(s)).collect(Collectors.joining(", "));
            comments = String.format("Missing target SOR items: [%s]. Cannot confirm multiple members.", missingItems);
            log.info(comments);
            output = "N";
        } else if ("MEDICAL_COMMERCIAL".equalsIgnoreCase(documentType)) {
            comments = String.format("COMMERCIAL doc: member_id clusters=%d, name clusters=%d, dob count=%d", memberIdCount, canonicalFullNameCount, dobCount);
            log.info("MEDICAL_COMMERCIAL comments: {}", comments);
            output = (memberIdCount > 1 && canonicalFullNameCount > 1 && dobCount > 1) ? "Y" : "N";
            log.info("Determined output for MEDICAL_COMMERCIAL={}", output);
        } else if ("MEDICAL_GBD".equalsIgnoreCase(documentType)) {
            comments = String.format("GBD doc: member_id clusters=%d, name clusters=%d", memberIdCount, canonicalFullNameCount);
            log.info("MEDICAL_GBD comments: {}", comments);
            output = (memberIdCount > 1 || canonicalFullNameCount > 1) ? "Y" : "N";
            log.info("Determined output for MEDICAL_GBD={}", output);
        } else {
            comments = "Unknown document type or insufficient data for multiplicity check.";
            log.info("fallback case comments: {}", comments);
            output = "N";
            log.info("Determined output for fallback case={}", output);
        }

        log.info("Evaluation completed for originId={} → output={} comments={}", inputRows.getOriginId(), output, comments);

        return MultipleMemberSummary.builder()
                .pageNo(pageNo)
                .output(output)
                .comments(comments)
                .valueTraces(buildValueTraces(rawValuesPerSorItem, canonicalFullNames))
                .build();
    }

    private static List<ValueTrace> buildValueTraces(Map<String, List<String>> rawValuesPerSorItem, Set<String> canonicalFullNames) {
        List<ValueTrace> traces = new ArrayList<>();
        if (rawValuesPerSorItem.containsKey("member_id")) {
            traces.add(ValueTrace.builder().key("member_id").values(String.join(",", rawValuesPerSorItem.get("member_id"))).build());
        }
        traces.add(ValueTrace.builder().key("member_full_name").values(String.join(",", canonicalFullNames)).build());
        if (rawValuesPerSorItem.containsKey("member_date_of_birth")) {
            traces.add(ValueTrace.builder().key("member_date_of_birth").values(String.join(",", rawValuesPerSorItem.get("member_date_of_birth"))).build());
        }
        return traces;
    }

    private static double computeSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;

        s1 = normalizeName(s1);
        s2 = normalizeName(s2);

        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        return jaroWinkler.apply(s1, s2);
    }

    private static String normalizeName(String raw) {
        if (raw == null) return "";
        raw = raw.toLowerCase(Locale.ROOT).trim();
        raw = raw.replaceAll("[^a-z\\s]", "");
        String[] parts = raw.split("\\s+");
        Arrays.sort(parts);
        return String.join(" ", parts).trim();
    }

    private static int clusterAndCount(List<String> values, double threshold) {
        if (values == null || values.isEmpty()) return 0;
        List<Set<String>> clusters = new ArrayList<>();
        for (String value : values) {
            boolean placed = false;
            for (Set<String> cluster : clusters) {
                if (computeSimilarity(value, cluster.iterator().next()) >= threshold) {
                    cluster.add(value);
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                clusters.add(new HashSet<>(Collections.singletonList(value)));
            }
        }
        return clusters.size();
    }

    private static String composePageNumbers(Set<String> pageNumbersSet) {
        return pageNumbersSet.stream()
                .map(Integer::valueOf)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static Set<String> buildCanonicalFullNames(List<String> firstNames, List<String> lastNames) {
        Set<String> canonicalFullNames = new HashSet<>();
        int maxNames = Math.max(firstNames.size(), lastNames.size());
        for (int i = 0; i < maxNames; i++) {
            String fn = i < firstNames.size() ? firstNames.get(i).toLowerCase().trim() : "";
            String ln = i < lastNames.size() ? lastNames.get(i).toLowerCase().trim() : "";
            List<String> parts = new ArrayList<>();
            if (!fn.isEmpty()) parts.add(fn);
            if (!ln.isEmpty()) parts.add(ln);
            parts.sort(String::compareTo);
            String canonicalName = String.join(" ", parts);
            if (!canonicalName.isEmpty()) canonicalFullNames.add(canonicalName);
        }
        return canonicalFullNames;
    }

    private MultiValueMemberMapperOutputTable outputTableCreation(MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable, String extractedValue) {
        Optional<extractedSorItemList> mmIndicatorRowOpt = multiValueMemberMapperTransformInputTable.getSorItemList()
                .stream()
                .filter(row -> "multiple_member_indicator".equalsIgnoreCase(row.getSorItemName()))
                .findFirst();

        if (mmIndicatorRowOpt.isEmpty()) {
            log.warn("No row found with sor_item_name = 'multiple_member_indicator' for originId: {}. Creating a default record.", multiValueMemberMapperTransformInputTable.getOriginId());
        }

        extractedSorItemList mmIndicatorRow = mmIndicatorRowOpt.orElse(null);
        Long defaultConfidenceScore = Long.valueOf(action.getContext().get(DEFAULT_CONFIDENCE_SCORE));

        String finalExtractedValue;
        if (mmIndicatorRow != null && "Y".equalsIgnoreCase(mmIndicatorRow.getPredictedValue())) {
            finalExtractedValue = "Y";
            log.info("Existing multiple_member_indicator found as 'Y'. Using 'Y' as final extracted value.");
        } else {
            finalExtractedValue = extractedValue;
            log.info("Using new extracted value for multiple_member_indicator: {}", extractedValue);
        }

        return MultiValueMemberMapperOutputTable.builder()
                .createdOn(LocalDateTime.now())
                .createdUserId(mmIndicatorRow.getTenantId())
                .lastUpdatedOn(LocalDateTime.now())
                .lastUpdatedUserId(mmIndicatorRow.getTenantId())
                .status("ACTIVE")
                .version(1)
                .frequency(mmIndicatorRow.getFrequency())
                .bBox("")
                .confidenceScore(defaultConfidenceScore)
                .extractedValue(finalExtractedValue)
                .filterScore(0L)
                .groupId(mmIndicatorRow.getGroupId())
                .maximumScore(defaultConfidenceScore)
                .originId(multiValueMemberMapperTransformInputTable.getOriginId())
                .paperNo(mmIndicatorRow.getPaperNo())
                .questionId(mmIndicatorRow.getQuestionId())
                .rootPipelineId(mmIndicatorRow.getRootPipelineId())
                .sorItemName("multiple_member_indicator")
                .synonymId(mmIndicatorRow.getSynonymId())
                .tenantId(mmIndicatorRow.getTenantId())
                .modelRegistry(mmIndicatorRow.getModelRegistry())
                .batchId(mmIndicatorRow.getBatchId())
                .build();
    }

    private void executeMMIAuditInsert(Handle handle, MultipleMemberSummary rows, String originId) {
        String insertQuery = INSERT_INTO + TABLE_NAME + " ( " + INSERT_COLUMNS + " ) " + INSERT_VALUES;
        log.debug(marker, "[{}] Executing audit insert into {}", originId, TABLE_NAME);

        try (PreparedBatch batch = handle.prepareBatch(insertQuery)) {
            for (ValueTrace vt : rows.getValueTraces()) {
                batch.bind(0, action.getRootPipelineId())
                        .bind(1, LocalDateTime.now())
                        .bind(2, LocalDateTime.now())
                        .bind(3, Long.parseLong(action.getContext().get("tenant_id")))
                        .bind(4, Long.parseLong(action.getContext().get("tenant_id")))
                        .bind(5, action.getContext().get("batch_id"))
                        .bind(6, originId)
                        .bind(7, Long.parseLong(action.getContext().get("group_id")))
                        .bind(8, action.getContext().get("document_type"))
                        .bind(9, Long.parseLong(action.getContext().get("tenant_id")))
                        .bind(10, rows.getPageNo())
                        .bind(11, vt.getKey())
                        .bind(12, rows.getOutput())
                        .bind(13, rows.getComments());
                batch.add();
            }
            int[] counts = batch.execute();
            log.info(marker, "[{}] Audit insert complete. {} records inserted.", originId, counts.length);
        } catch (Exception e) {
            log.error(marker, "[{}] Batch insert failed: {}", originId, e.getMessage(), e);
            HandymanException.insertException("Error in batch insert into " + TABLE_NAME, new HandymanException(e), action);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueTrace {
        private String key;
        private String values;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MultipleMemberSummary {
        private String pageNo;
        private String output;
        private String comments;
        private List<ValueTrace> valueTraces;
    }

}