package in.handyman.raven.lib.services.sor.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MultiEntityFieldHandlingProcessor {

    private static final Logger log = LoggerFactory.getLogger(MultiEntityFieldHandlingProcessor.class);

    public List<MultiEntityFieldHandlingInput> process(List<MultiEntityFieldHandlingInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("Starting MultiEntityFieldHandlingProcessor with {} inputs", inputs.size());
        List<MultiEntityFieldHandlingInput> finalOutput = new ArrayList<>();

        // Group by OriginId -> SorContainerInstance
        // "group by origin_id, sorContainerInstance"
        Map<String, List<MultiEntityFieldHandlingInput>> groupedInputs = inputs.stream()
                .collect(Collectors.groupingBy(
                        input -> Optional.ofNullable(input.getOriginId()).orElse("UNKNOWN_ORIGIN")));

            for (Map.Entry<String, List<MultiEntityFieldHandlingInput>> instanceEntry : groupedInputs
                    .entrySet()) {
                String originId = instanceEntry.getKey();
                List<MultiEntityFieldHandlingInput> groupList = instanceEntry.getValue();

                log.debug("Processing group: Origin={}, Size={}", originId, groupList.size());

                // Set to track duplicates within this group (Origin + Instance)
                // "remove the duplicates for each origin" (Assuming per instance as well since
                // we group by it)
                // Key: sorItemName + "|" + answer
                Set<String> uniqueKeys = new HashSet<>();
                List<MultiEntityFieldHandlingInput> processedGroup = new ArrayList<>();

                for (MultiEntityFieldHandlingInput item : groupList) {
                    if ("multi_value".equalsIgnoreCase(item.getLineItemType())) {
                        handleMultiValueItem(item, processedGroup, uniqueKeys);
                    } else {
                        // "other lineItemType will be returned as is"
                        // But we should still probably check for strict duplicates if they occur?
                        // The user said "remove the duplicates for each origin", usually implies global
                        // dedup.
                        // For non-multi-value, we'll add them. If implicit dedup is needed for them
                        // too:
                        // For now, adhering to "returned as is", but usually dedup applies to all.
                        // I will apply dedup for them too to be safe as per "remove duplicates for each
                        // origin".
                        handleSingleValueItem(item, processedGroup, uniqueKeys);
                    }
                }
                finalOutput.addAll(processedGroup);
            }


        log.info("Completed processing. Output size: {}", finalOutput.size());
        return finalOutput;
    }

    private void handleMultiValueItem(MultiEntityFieldHandlingInput item,
            List<MultiEntityFieldHandlingInput> outputList, Set<String> uniqueKeys) {
        String answer = item.getAnswer();
        if (answer == null || answer.isEmpty()) {
            return;
        }

        // Split by comma
        String[] parts = answer.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty())
                continue;

            String key = item.getSorItemName() + "|" + trimmed;
            if (!uniqueKeys.contains(key)) {
                uniqueKeys.add(key);

                MultiEntityFieldHandlingInput clone = cloneInput(item);
                clone.setAnswer(trimmed);
                // "generate a systemkey" -> mapped to transactionId
                clone.setTransactionId(UUID.randomUUID().toString());

                outputList.add(clone);
            }
        }
    }

    private void handleSingleValueItem(MultiEntityFieldHandlingInput item,
            List<MultiEntityFieldHandlingInput> outputList, Set<String> uniqueKeys) {
        String answer = item.getAnswer();
        String key = item.getSorItemName() + "|" + (answer == null ? "" : answer);

        // Ensure we don't add duplicates even for single items if they match something
        // we already processed
        // (e.g. if a multi-value split resulted in "A" and there was a single value "A"
        // in the same group)
        if (!uniqueKeys.contains(key)) {
            uniqueKeys.add(key);
            // We pass it as is, but maybe we should ensure it has a transactionId if
            // missing?
            // User requirement: "generate a systemkey" was in the context of splitting.
            // Existing items might already have one. I'll leave existing ID unless null.
            if (item.getTransactionId() == null) {
                item.setTransactionId(UUID.randomUUID().toString());
            }
            outputList.add(item);
        }
    }

    private MultiEntityFieldHandlingInput cloneInput(MultiEntityFieldHandlingInput original) {
        // Deep copy of all known fields
        MultiEntityFieldHandlingInput clone = new MultiEntityFieldHandlingInput();

        // Fields from MultiEntityFieldHandlingInput
        clone.setMultiEntityFilteringId(original.getMultiEntityFilteringId());
        clone.setRemovedAfterFiltering(original.isRemovedAfterFiltering());

        // Fields from Vqa Transaction Output
        clone.setVqaId(original.getVqaId());
        clone.setSorContainerId(original.getSorContainerId());
        clone.setSorContainerName(original.getSorContainerName());
        clone.setSorContainerInstance(original.getSorContainerInstance());
        clone.setSorItemName(original.getSorItemName());
        clone.setSorItemId(original.getSorItemId());
        clone.setSorItemAttributionId(original.getSorItemAttributionId());
        clone.setModelId(original.getModelId());
        clone.setModelInfo(original.getModelInfo());
        clone.setModelRegistry(original.getModelRegistry());
        clone.setModelRegistryId(original.getModelRegistryId());
        // Answer is set by caller
        clone.setAnswer(original.getAnswer());
        clone.setVqaScore(original.getVqaScore());
        clone.setScore(original.getScore());
        clone.setBBox(original.getBBox());
        clone.setLabel(original.getLabel());
        clone.setSectionAlias(original.getSectionAlias());
        clone.setSynonymId(original.getSynonymId());
        clone.setSorSynonym(original.getSorSynonym());
        clone.setQuestionId(original.getQuestionId());
        clone.setSorQuestion(original.getSorQuestion());
        clone.setWeight(original.getWeight());
        clone.setCategory(original.getCategory());
        clone.setLineItemType(original.getLineItemType());
        clone.setIsMultiEntityEnabled(original.getIsMultiEntityEnabled());
        clone.setEncryptionPolicy(original.getEncryptionPolicy());
        clone.setIsEncrypted(original.getIsEncrypted());

        // Fields from VqaTransactionBase
        // Note: transactionId is not copied here because we generate a new one for
        // splits,
        // but for consistency with a "clone" operation, we usually copy it.
        // However, the caller overwrites it immediately. I will copy it to be a true
        // clone.
        clone.setTransactionId(original.getTransactionId());
        clone.setCreatedOn(original.getCreatedOn() != null ? original.getCreatedOn() : LocalDateTime.now());
        clone.setCreatedUserId(original.getCreatedUserId());
        clone.setLastUpdatedOn(LocalDateTime.now());
        clone.setLastUpdatedUserId(original.getLastUpdatedUserId());
        clone.setRootPipelineId(original.getRootPipelineId());
        clone.setTenantId(original.getTenantId());
        clone.setDocumentId(original.getDocumentId());
        clone.setGroupId(original.getGroupId());
        clone.setBatchId(original.getBatchId());
        clone.setOriginId(original.getOriginId());
        clone.setPaperNo(original.getPaperNo());
        clone.setTruthId(original.getTruthId());
        clone.setStatus(original.getStatus());
        clone.setStage(original.getStage());
        clone.setMessage(original.getMessage());
        clone.setVersion(original.getVersion());
        clone.setExtractedImageUnit(original.getExtractedImageUnit());
        clone.setImageDpi(original.getImageDpi());
        clone.setImageHeight(original.getImageHeight());
        clone.setImageWidth(original.getImageWidth());

        return clone;
    }
}
