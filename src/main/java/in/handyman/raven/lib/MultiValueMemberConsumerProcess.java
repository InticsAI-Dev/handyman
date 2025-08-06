package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.*;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperInputTable;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperOutputTable;
import in.handyman.raven.lib.model.multi.member.indicator.MultiValueMemberMapperTransformInputTable;
import in.handyman.raven.lib.model.multi.member.indicator.extractedSorItemList;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MultiValueMemberConsumerProcess implements CoproProcessor.ConsumerProcess<MultiValueMemberMapperTransformInputTable, MultiValueMemberMapperOutputTable>{
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable;
    private final Long tenantId;

    private final String PROCESSING_SOR_ITEM_NAME = "multi.member.indicator.fields";

    public MultiValueMemberConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable, Long tenantId) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.multiValueMemberMapperTransformInputTable = multiValueMemberMapperTransformInputTable;
        this.tenantId = tenantId;
    }

    public List<MultiValueMemberMapperOutputTable> process(URL endpoint, MultiValueMemberMapperTransformInputTable multiValueMemberMapperTransformInputTable) throws Exception {
        log.info(marker, "Starting MultiValueMemberMapper process for tenantId: {}, actionId: {}", tenantId, action.getActionId());

        List<MultiValueMemberMapperOutputTable> finalOutput = new ArrayList<>();

        String processingSorItemName = action.getContext().get(PROCESSING_SOR_ITEM_NAME);

        try {
            log.info(marker, "Processing MultiValueMemberMapper for tenantId: {}, actionId: {}", tenantId, action.getActionId());

            Set<String> targetSorItems = Arrays.stream(processingSorItemName.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            String processedValue = evaluateMultivaluePresenceAndUniqueness(multiValueMemberMapperTransformInputTable, targetSorItems, log, marker);
            MultiValueMemberMapperOutputTable singleRecord = outputTableCreation(multiValueMemberMapperTransformInputTable, processedValue);

            finalOutput.add(singleRecord);

            log.info(marker, "Processing completed successfully for tenantId: {}, actionId: {}", tenantId, action.getActionId());
        } catch (Exception e) {
            log.error(marker, "Error processing MultiValueMemberMapper for tenantId: {}, actionId: {}.", tenantId, action.getActionId(), e);
            throw new HandymanException("Error processing MultiValueMemberMapper", e);
        }

        return finalOutput;
    }

    public static String evaluateMultivaluePresenceAndUniqueness(MultiValueMemberMapperTransformInputTable inputRows, Set<String> targetSorItems, Logger log, Marker marker
    ) {
        Map<String, Set<String>> valuesPerSorItem = new HashMap<>();
        Set<String> presentSorItems = new HashSet<>();

        List<extractedSorItemList> multiValueMember = inputRows.getSorItemList();
        for (extractedSorItemList row : multiValueMember) {
            String sorItemName = row.getSorItemName();
            String predictedValue = row.getPredictedValue();

            if (sorItemName != null && predictedValue != null && targetSorItems.contains(sorItemName)) {
                presentSorItems.add(sorItemName);
                Set<String> values = valuesPerSorItem.computeIfAbsent(sorItemName, k -> new HashSet<>());

                Arrays.stream(predictedValue.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toLowerCase)
                        .forEach(values::add);
            }
        }

        if (!presentSorItems.containsAll(targetSorItems)) {
            log.info(marker, "Missing one or more target SOR items in input. Expected: {}, Found: {}", targetSorItems, presentSorItems);
            return "N";
        }

        // Commercial: requires all 3 (ID, Name, DOB) with more than 1 unique value each
        if (targetSorItems.containsAll(Set.of("member_id", "member_last_name", "member_date_of_birth"))) {
            boolean allHaveMultiple = true;
            for (String key : List.of("member_id", "member_last_name", "member_date_of_birth")) {
                int count = valuesPerSorItem.getOrDefault(key, Collections.emptySet()).size();
                log.info(marker, "Commercial - SOR item '{}' has {} unique values", key, count);
                if (count <= 1) {
                    allHaveMultiple = false;
                    break;
                }
            }
            return allHaveMultiple ? "Y" : "N";
        }

        // GBD: requires at least one of Member ID or Member Name to have >1 unique value (DOB is ignored)
        if (targetSorItems.contains("member_id") || targetSorItems.contains("member_last_name")) {
            int idCount = valuesPerSorItem.getOrDefault("member_id", Collections.emptySet()).size();
            int nameCount = valuesPerSorItem.getOrDefault("member_last_name", Collections.emptySet()).size();

            log.info(marker, "GBD - Member ID count: {}, Member Name count: {}", idCount, nameCount);

            return (idCount > 1 || nameCount > 1) ? "Y" : "N";
        }

        log.info(marker, "Unknown plan type. SOR items: {}", targetSorItems);
        return "N";
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

        extractedSorItemList mmIndicatorRow = mmIndicatorRowOpt.get();

        return MultiValueMemberMapperOutputTable.builder()
                .minScoreId(mmIndicatorRow.getMinScoreId())
                .originId(multiValueMemberMapperTransformInputTable.getOriginId())
                .paperNo(mmIndicatorRow.getPaperNo())
                .sorItemName("multiple_member_indicator")
                .weightScore(mmIndicatorRow.getWeightScore())
                .predictedValue(extractedValue)
                .bBox("")
                .confidenceScore(mmIndicatorRow.getConfidenceScore())
                .frequency(mmIndicatorRow.getFrequency())
                .cummulativeScore(mmIndicatorRow.getCummulativeScore())
                .questionId(mmIndicatorRow.getQuestionId())
                .synonymId(mmIndicatorRow.getSynonymId())
                .tenantId(mmIndicatorRow.getTenantId())
                .modelRegistry(mmIndicatorRow.getModelRegistry())
                .rootPipelineId(mmIndicatorRow.getRootPipelineId())
                .batchId(mmIndicatorRow.getBatchId())
                .build();
    }
}
