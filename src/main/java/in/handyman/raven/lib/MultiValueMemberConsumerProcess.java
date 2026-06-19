package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultiValueMemberMapper;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperTransformInputTable;
import in.handyman.raven.lib.model.multi.member.indicator.extractedSorItemList;
import in.handyman.raven.lib.services.sor.transform.MultiMemberIndicatorInput;
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

    private final String MULTI_MEMBER_NAME_THRESHOLD = "multi.member.name.similarity.threshold";
    private final String MULTI_MEMBER_ID_THRESHOLD = "multi.member.id.similarity.threshold";
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

    public List<MultiMemberIndicatorInput> doMultiMemberValidation() throws Exception {
        log.info(marker, "Starting MultiValueMemberMapper process for tenantId={} actionId={}", tenantId, action.getActionId());

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multiValueMemberMapper.getResourceConn());

        List<MultiMemberIndicatorInput> finalOutput = Collections.synchronizedList(new ArrayList<>());

        String PROCESSING_SOR_ITEM_NAME = "multi.member.indicator.fields";
        String processingSorItemName = action.getContext().get(PROCESSING_SOR_ITEM_NAME);

        String MULTI_MEMBER_VOTING_VERSION = "multi.member.voting.v1";
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
                        MultiMemberIndicatorInput outputRow = outputTableCreation(inputTable, result.getOutput());
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

        Set<String> multipleMemberIndicators = extractIndicators(valuesPerSorItem.get("multiple_member_indicator"));

        String output;
        String comments;

        if (containsYes(multipleMemberIndicators)) {
            comments = "multiple_member_indicator explicitly marked as 'Y'. Confirming multiple members.";
            output = "Y";
        } else if (!presentSorItems.containsAll(targetSorItems)) {
            String missingItems = targetSorItems.stream().filter(s -> !presentSorItems.contains(s)).collect(Collectors.joining(", "));
            comments = String.format("Missing target SOR items: [%s]. Cannot confirm multiple members.", missingItems);
            output = "N";
        } else if (lastNameCount == 1) {
            comments = "Only one unique last name found, indicating a single member.";
            output = "N";
        } else if ("MEDICAL_COMMERCIAL".equalsIgnoreCase(documentType)) {
            comments = String.format("COMMERCIAL document: member_id unique count = %d, canonical full name unique count = %d, dob unique count = %d.", memberIdCount, canonicalFullNameCount, dobCount);
            output = (memberIdCount > 1 && canonicalFullNameCount > 1 && dobCount > 1) ? "Y" : "N";
            comments += output.equals("Y") ? " All these counts are >1, indicating multiple members." : " One or more counts are <=1, indicating a single member.";
        } else if ("MEDICAL_GBD".equalsIgnoreCase(documentType)) {
            comments = String.format("GBD document: member_id unique count = %d, canonical full name unique count = %d.", memberIdCount, canonicalFullNameCount);
            output = (memberIdCount > 1 || canonicalFullNameCount > 1) ? "Y" : "N";
            comments += output.equals("Y") ? " Either member_id count or canonical full name count is >1, indicating multiple members." : " Both counts are <=1, indicating a single member.";
        } else {
            comments = String.format("Unknown document type '%s'. Insufficient data to determine member multiplicity.", documentType);
            output = "N";
        }

        log.info("Evaluation completed for originId={} → output={} comments={}", inputRows.getOriginId(), output, comments);

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

        int canonicalFullNameCount = clusterAndCount(new ArrayList<>(canonicalFullNames), nameThreshold);
        int memberIdCount = clusterAndCount(rawValuesPerSorItem.getOrDefault("member_id", Collections.emptyList()), idThreshold);
        int dobCount = rawValuesPerSorItem.getOrDefault("member_date_of_birth", Collections.emptyList()).size();

        log.info("Cluster summary -> Name clusters={} ID clusters={} DOB count={}", canonicalFullNameCount, memberIdCount, dobCount);

        Set<String> multipleMemberIndicators = extractIndicators(rawValuesPerSorItem.get("multiple_member_indicator"));

        String output;
        String comments;

        if (containsYes(multipleMemberIndicators)) {
            comments = "multiple_member_indicator explicitly marked as 'Y'. Confirming multiple members.";
            output = "Y";
        } else if (!presentSorItems.containsAll(targetSorItems)) {
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

    private MultiMemberIndicatorInput outputTableCreation(MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable, String extractedValue) {
        Optional<extractedSorItemList> mmIndicatorRowOpt = multiValueMemberMapperTransformInputTable.getSorItemList()
                .stream()
                .filter(row -> "multiple_member_indicator".equalsIgnoreCase(row.getSorItemName()))
                .findFirst();

        if (mmIndicatorRowOpt.isEmpty()) {
            log.info("No row found with sor_item_name = 'multiple_member_indicator'");
        }

        extractedSorItemList mmIndicatorRow = mmIndicatorRowOpt.orElse(
                multiValueMemberMapperTransformInputTable.getSorItemList()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new HandymanException("No SOR rows found for originId=" + multiValueMemberMapperTransformInputTable.getOriginId())));

        if (mmIndicatorRowOpt.isEmpty()) {
            log.warn(marker, "No 'multiple_member_indicator' row found for originId: {}. Using first available SOR row as fallback.", multiValueMemberMapperTransformInputTable.getOriginId());
        }

        String DEFAULT_CONFIDENCE_SCORE = "radon.kvp.bbox.vqa.score.default";
        Long defaultConfidenceScore = Long.valueOf(action.getContext().get(DEFAULT_CONFIDENCE_SCORE));

        assert mmIndicatorRow != null;
        return MultiMemberIndicatorInput.builder()
                .transactionId(mmIndicatorRow.getTransactionId())
                .createdOn(LocalDateTime.now())
                .createdUserId(mmIndicatorRow.getTenantId())
                .lastUpdatedOn(LocalDateTime.now())
                .lastUpdatedUserId(mmIndicatorRow.getTenantId())
                .rootPipelineId(mmIndicatorRow.getRootPipelineId())
                .tenantId(mmIndicatorRow.getTenantId())
                .documentId("")
                .groupId(mmIndicatorRow.getGroupId())
                .batchId(mmIndicatorRow.getBatchId())
                .originId(multiValueMemberMapperTransformInputTable.getOriginId())
                .paperNo(mmIndicatorRow.getPaperNo())
                .truthId(mmIndicatorRow.getTruthId())
                .status("ACTIVE")
                .stage("MULTI_MEMBER_INDICATOR")
                .message(mmIndicatorRow.getMessage())
                .version(1)
                .extractedImageUnit("")
                .imageDpi(72L)
                .imageHeight(0L)
                .imageWidth(0L)
                .sectionPriorityAfterFilter("")
                .vqaId(0L)
                .sorContainerId(0L)
                .sorContainerName("")
                .sorContainerInstance(mmIndicatorRow.getSorContainerInstance())
                .sorItemName("multiple_member_indicator")
                .sorItemId(0L)
                .sorItemAttributionId(0)
                .modelId(0L)
                .modelInfo("")
                .modelRegistry(mmIndicatorRow.getModelRegistry())
                .modelRegistryId(0L)
                .answer(extractedValue)
                .vqaScore(0.0)
                .score(defaultConfidenceScore)
                .bBox("")
                .label("")
                .sectionAlias("")
                .synonymId(mmIndicatorRow.getSynonymId())
                .sorSynonym("")
                .questionId(mmIndicatorRow.getQuestionId())
                .sorQuestion("")
                .weight(150)
                .category("")
                .lineItemType("")
                .isMultiEntityEnabled(false)
                .encryptionPolicy("")
                .isEncrypted(false)
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
            log.info(marker, "[{}] MMI audit insert complete. {} records inserted.", originId, counts.length);

        } catch (Exception e) {
            log.error(marker, "[{}] MMI audit insert failed", originId, e);
            HandymanException.insertException(
                    "Error in MMI audit insert into " + TABLE_NAME,
                    new HandymanException(e),
                    action
            );
        }
    }



    public static String getInsertIntoValuesUpdated(){
        return  "VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +     // 0–9
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +     // 10–19
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +     // 20–29
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +     // 30–39
                "?, ?, ?, ?, ?, ?, ?, ?, ?" +          // 40–48
                ")";

    }
    public static String getColumnNamesForInsert() {
        return "transaction_id, " +                 // 0
                "created_on, " +                     // 1
                "created_user_id, " +                // 2
                "last_updated_on, " +                // 3
                "last_updated_user_id, " +            // 4
                "root_pipeline_id, " +                // 5
                "tenant_id, " +                      // 6
                "document_id, " +                    // 7
                "group_id, " +                       // 8
                "batch_id, " +                       // 9
                "origin_id, " +                      // 10
                "paper_no, " +                       // 11
                "truth_id, " +                       // 12
                "status, " +                         // 13
                "stage, " +                          // 14
                "message, " +                        // 15
                "version, " +                        // 16
                "extracted_image_unit, " +            // 17
                "image_dpi, " +                      // 18
                "image_height, " +                   // 19
                "image_width, " +                    // 20
                "section_priority_after_filter, " +  // 21
                "vqa_id, " +                          // 22
                "sor_container_id, " +                // 23
                "sor_container_name, " +              // 24
                "sor_container_instance, " +          // 25
                "sor_item_name, " +                   // 26
                "sor_item_id, " +                     // 27
                "sor_item_attribution_id, " +          // 28
                "model_id, " +                        // 29
                "model_info, " +                      // 30
                "model_registry, " +                  // 31
                "model_registry_id, " +               // 32
                "answer, " +                          // 33
                "vqa_score, " +                       // 34
                "score, " +                           // 35
                "b_box, " +                           // 36
                "label, " +                           // 37
                "section_alias, " +                   // 38
                "synonym_id, " +                      // 39
                "sor_synonym, " +                     // 40
                "question_id, " +                     // 41
                "sor_question, " +                    // 42
                "weight, " +                          // 43
                "category, " +                        // 44
                "line_item_type, " +                  // 45
                "is_multi_entity_enabled, " +          // 46
                "encryption_policy, " +               // 47
                "is_encrypted";                       // 48

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

    @SuppressWarnings("unchecked")
    private static Set<String> extractIndicators(Object rawValue) {
        if (rawValue == null) {
            return Collections.emptySet();
        }
        if (rawValue instanceof Set) {
            return (Set<String>) rawValue;
        }
        if (rawValue instanceof Collection) {
            return new HashSet<>((Collection<String>) rawValue);
        }
        return Set.of(rawValue.toString());
    }

    private static boolean containsYes(Collection<String> indicators) {
        return indicators.stream().anyMatch("Y"::equalsIgnoreCase);
    }

}