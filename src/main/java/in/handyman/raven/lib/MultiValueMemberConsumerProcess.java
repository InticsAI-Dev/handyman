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
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
        log.info(marker, "Starting MultiValueMemberMapper process for tenantId: {}, actionId: {}", tenantId, action.getActionId());

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multiValueMemberMapper.getResourceConn());

        List<MultiValueMemberMapperOutputTable> finalOutput = Collections.synchronizedList(new ArrayList<>());

        String processingSorItemName = action.getContext().get(PROCESSING_SOR_ITEM_NAME);

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

                    try {
                        log.info(marker, "[Thread: {}] START processing for originId: {}", threadName, originId);

                        MultipleMemberSummary result = evaluateMultivaluePresenceAndUniqueness(inputTable, targetSorItems, log, marker);
                        MultiValueMemberMapperOutputTable outputRow = outputTableCreation(inputTable, result.getOutput());
                        finalOutput.add(outputRow);

                        jdbi.useTransaction(handle -> {
                                    executeMMIAuditInsert(handle, result, originId, inputTable);
                            log.info("Executed query from by establishing handle for originId: {}", originId);
                        });

                        log.info(marker, "[Thread: {}] SUCCESS for originId: {}", threadName, originId);

                    } catch (Exception e) {
                        log.error(marker, "[Thread: {}] ERROR processing originId: {}", threadName, originId, e);
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

    public static MultipleMemberSummary evaluateMultivaluePresenceAndUniqueness(
            MultiValueMemberMapperTransformInputTable inputRows,
            Set<String> targetSorItems,
            Logger log,
            Marker marker) {

        log.info("Starting evaluation for originId: {}", inputRows.getOriginId());

        List<extractedSorItemList> multiValueMember = inputRows.getSorItemList();

        Map<String, Set<String>> valuesPerSorItem = new HashMap<>();
        Set<String> presentSorItems = new HashSet<>();
        Set<String> pageNumbersSet = new HashSet<>();
        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();
        String documentType = "";

        // Collect values and metadata
        documentType = collectValuesAndMetadata(multiValueMember, targetSorItems, valuesPerSorItem, presentSorItems, pageNumbersSet, firstNames, lastNames, log);

        String pageNo = composePageNumbers(pageNumbersSet);
        Set<String> canonicalFullNames = buildCanonicalFullNames(firstNames, lastNames);

        List<ValueTrace> valueTraces = buildValueTraces(valuesPerSorItem, canonicalFullNames);

        return determineOutputAndBuildSummary(inputRows, targetSorItems, valuesPerSorItem, presentSorItems, documentType, pageNo, canonicalFullNames, valueTraces, log);
    }

    private static String collectValuesAndMetadata(List<extractedSorItemList> multiValueMember,
                                                   Set<String> targetSorItems,
                                                   Map<String, Set<String>> valuesPerSorItem,
                                                   Set<String> presentSorItems,
                                                   Set<String> pageNumbersSet,
                                                   List<String> firstNames,
                                                   List<String> lastNames,
                                                   Logger log) {
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
            if (!canonicalName.isEmpty()) {
                canonicalFullNames.add(canonicalName);
            }
        }
        return canonicalFullNames;
    }

    private static List<ValueTrace> buildValueTraces(Map<String, Set<String>> valuesPerSorItem, Set<String> canonicalFullNames) {
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

    private static MultipleMemberSummary determineOutputAndBuildSummary(MultiValueMemberMapperTransformInputTable inputRows,
                                                                        Set<String> targetSorItems,
                                                                        Map<String, Set<String>> valuesPerSorItem,
                                                                        Set<String> presentSorItems,
                                                                        String documentType,
                                                                        String pageNo,
                                                                        Set<String> canonicalFullNames,
                                                                        List<ValueTrace> valueTraces,
                                                                        Logger log) {
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

    private MultiValueMemberMapperOutputTable outputTableCreation(
            MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable,
            String extractedValue
    ) {
        Optional<extractedSorItemList> mmIndicatorRowOpt = multiValueMemberMapperTransformInputTable.getSorItemList()
                .stream()
                .filter(row -> "multiple_member_indicator".equalsIgnoreCase(row.getSorItemName()))
                .findFirst();

        if (mmIndicatorRowOpt.isEmpty()) {
            log.info("No row found with sor_item_name = 'multiple_member_indicator'");
        }

        extractedSorItemList mmIndicatorRow = null;
        if (mmIndicatorRowOpt.isEmpty()) {
            log.warn(marker, "No 'multiple_member_indicator' row found for originId: {}. Creating a default record.", multiValueMemberMapperTransformInputTable.getOriginId()); // fallback
        } else {
            mmIndicatorRow = mmIndicatorRowOpt.get();
        }

        Long defaultConfidenceScore = Long.valueOf(action.getContext().get(DEFAULT_CONFIDENCE_SCORE));

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
                .extractedValue(extractedValue)
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

    private void executeMMIAuditInsert(Handle handle, MultipleMemberSummary rows, String originId, MultiValueMemberMapperTransformInputTable inputTable) {

        String insertQuery = INSERT_INTO + TABLE_NAME + " ( " + INSERT_COLUMNS + " ) " + INSERT_VALUES;

        try (PreparedBatch batch = handle.prepareBatch(insertQuery)) {
            for (ValueTrace vt : rows.getValueTraces()) {
                batch.bind(0, inputTable.getSorItemList().get(0).getRootPipelineId())
                        .bind(1, LocalDateTime.now())
                        .bind(2, LocalDateTime.now())
                        .bind(3, action.getContext().get("tenant_id"))
                        .bind(4, action.getContext().get("tenant_id"))
                        .bind(5, action.getContext().get("batch_id"))
                        .bind(6, originId)
                        .bind(7, action.getContext().get("group_id"))
                        .bind(8, action.getContext().get("document_type"))
                        .bind(9, action.getContext().get("tenant_id"))
                        .bind(10, rows.getPageNo())
                        .bind(11, vt.getKey())
                        .bind(12, rows.getOutput())
                        .bind(13, rows.getComments());
                batch.add();
            }
            int[] counts = batch.execute();
            log.info("Batch inserted {} records", counts.length);
        } catch (Exception e) {
            log.error("Batch insert failed", e);
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