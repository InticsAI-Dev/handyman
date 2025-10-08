package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperOutputTable;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperTransformInputTable;
import in.handyman.raven.lib.model.multi.member.indicator.extractedSorItemList;
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

    private final String PROCESSING_SOR_ITEM_NAME = "multi.member.indicator.fields";

    private final String DEFAULT_CONFIDENCE_SCORE = "radon.kvp.bbox.vqa.score.default";

    public MultiValueMemberConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, List<MultiValueMemberMapperTransformInputTable> multiValueMemberMapperTransformInputTables, Long tenantId, Integer threadCount) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.multiValueMemberMapperTransformInputTables = multiValueMemberMapperTransformInputTables;
        this.tenantId = tenantId;
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public List<MultiValueMemberMapperOutputTable> doMultiMemberValidation() throws Exception {
        log.info(marker, "Starting MultiValueMemberMapper process for tenantId: {}, actionId: {}", tenantId, action.getActionId());

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

                        String result = evaluateMultivaluePresenceAndUniqueness(inputTable, targetSorItems, log, marker);
                        MultiValueMemberMapperOutputTable outputRow = outputTableCreation(inputTable, result);
                        finalOutput.add(outputRow);

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

    public static String evaluateMultivaluePresenceAndUniqueness(MultiValueMemberMapperTransformInputTable inputRows, Set<String> targetSorItems, Logger log, Marker marker) {
        Map<String, Set<String>> valuesPerSorItem = new HashMap<>();
        Set<String> presentSorItems = new HashSet<>();

        List<extractedSorItemList> multiValueMember = inputRows.getSorItemList();

        List<String> firstNames = new ArrayList<>();
        List<String> lastNames = new ArrayList<>();

        String documentType = "";
        for (extractedSorItemList row : multiValueMember) {
            String sorItemName = row.getSorItemName();
            String predictedValue = row.getPredictedValue();
            documentType = row.getDocumentType();

            if (sorItemName != null && predictedValue != null && targetSorItems.contains(sorItemName)) {
                presentSorItems.add(sorItemName);

                if ("member_first_name".equalsIgnoreCase(sorItemName)) {
                    Arrays.stream(predictedValue.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(firstNames::add);
                } else if ("member_last_name".equalsIgnoreCase(sorItemName)) {
                    Arrays.stream(predictedValue.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(lastNames::add);
                } else {
                    Set<String> values = valuesPerSorItem.computeIfAbsent(sorItemName, k -> new HashSet<>());
                    Arrays.stream(predictedValue.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .forEach(values::add);
                }
            }
        }

        if (!presentSorItems.containsAll(targetSorItems)) {
            log.info(marker, "Missing one or more target SOR items in input. Expected: {}, Found: {}", targetSorItems, presentSorItems);
            return "N";
        }

        Set<String> canonicalFullNames = new HashSet<>();
        int maxNames = Math.max(firstNames.size(), lastNames.size());

        for (int i = 0; i < maxNames; i++) {
            String fn = (i < firstNames.size()) ? firstNames.get(i).toLowerCase().trim() : "";
            String ln = (i < lastNames.size()) ? lastNames.get(i).toLowerCase().trim() : "";

            List<String> nameParts = new ArrayList<>();
            if (!fn.isEmpty()) nameParts.add(fn);
            if (!ln.isEmpty()) nameParts.add(ln);

            nameParts.sort(String::compareTo);

            String canonicalName = String.join(" ", nameParts);
            if (!canonicalName.isEmpty()) {
                canonicalFullNames.add(canonicalName);
            }
        }

        int canonicalFullNameCount = canonicalFullNames.size();

        int lastNameCount = valuesPerSorItem.getOrDefault("member_last_name", Collections.emptySet()).size();

        if (lastNameCount == 1) {
            log.info(marker, "Only one unique last name found - setting multiple_member_indicator to 'N'");
            return "N";
        }

        if ("MEDICAL_COMMERCIAL".equalsIgnoreCase(documentType)) {
            int memberIdCount = valuesPerSorItem.getOrDefault("member_id", Collections.emptySet()).size();
            int dobCount = valuesPerSorItem.getOrDefault("member_date_of_birth", Collections.emptySet()).size();

            log.info(marker, "COMMERCIAL - member_id count: {}, canonical full name count: {}, dob count: {}", memberIdCount, canonicalFullNameCount, dobCount);

            boolean allHaveMultiple = memberIdCount > 1 && canonicalFullNameCount > 1 && dobCount > 1;

            return allHaveMultiple ? "Y" : "N";

        } else if ("MEDICAL_GBD".equalsIgnoreCase(documentType)) {
            int memberIdCount = valuesPerSorItem.getOrDefault("member_id", Collections.emptySet()).size();

            log.info(marker, "GBD - member_id count: {}, canonical full name count: {}", memberIdCount, canonicalFullNameCount);

            return (memberIdCount > 1 || canonicalFullNameCount > 1) ? "Y" : "N";
        } else {
            log.info(marker, "Unknown document type '{}'. SOR items: {}", documentType, targetSorItems);
            return "N";
        }
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

}