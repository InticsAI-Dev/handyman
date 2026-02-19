package in.handyman.raven.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxJsonParser;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.checkbox.CheckboxJsonParsedResponse;
import in.handyman.raven.lib.model.kvp.checkbox.CheckboxQueryInputTable;
import in.handyman.raven.lib.model.kvp.checkbox.CheckboxQueryOutputTable;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryInputTableSorMeta;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class CheckboxJsonParserConsumerProcess
        implements CoproProcessor.ConsumerProcess<CheckboxQueryInputTable, CheckboxQueryOutputTable> {
    public static final String AES_256 = "AES256";
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final InticsIntegrity encryption;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckboxJsonParserConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action,
            CheckboxJsonParser checkboxJsonParser) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
    }

    @Override
    public List<CheckboxQueryOutputTable> process(URL endpoint, CheckboxQueryInputTable input) throws Exception {
        List<CheckboxQueryOutputTable> outputTables = new ArrayList<>();
        String encryptOutputSorItem = action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        String loggerInput = " Root pipeline Id " + input.getRootPipelineId() + " batch Id " + input.getBatchId()
                + " Origin Id " + input.getOriginId() + " paper No " + input.getPaperNo();

        log.debug(marker, "Checkbox json parser action started for {} ", loggerInput);

        try {
            String extractedContent = input.getResponse();
            if (extractedContent != null) {
                String jsonResponse = getDecryptedInputJson(encryption, extractedContent, encryptOutputSorItem);

                CheckboxJsonParsedResponse parsedResponse = objectMapper.readValue(jsonResponse,
                        CheckboxJsonParsedResponse.class);

                if (parsedResponse != null && parsedResponse.getGroups() != null) {
                    Map<String, LlmJsonQueryInputTableSorMeta> metaMap = getSorMetaMap(input.getSorMetaDetail());
                    List<String> keywordList = getKeywordList(input.getCheckboxKeywords());
                    Set<String> processedKeywords = new HashSet<>();

                    for (CheckboxJsonParsedResponse.CheckboxGroup group : parsedResponse.getGroups()) {
                        String bboxAsIs = getBboxAsIs(group);
                        String sectionAlias = group.getSectionHeader() != null ? group.getSectionHeader() : "";

                        for (CheckboxJsonParsedResponse.CheckboxOption option : group.getOpts()) {
                            CheckboxQueryOutputTable output = createOutputTable(input, option, bboxAsIs, sectionAlias,
                                    metaMap, keywordList, processedKeywords, encryptOutputSorItem);
                            outputTables.add(output);
                        }
                    }
                }
            } else {
                log.debug("Extracted content is null for {}. Skipping processing.", loggerInput);
            }
        } catch (Exception e) {
            log.error(marker, "Error in CheckboxJsonParserConsumerProcess for {}", loggerInput, e);
            throw new HandymanException("Error in CheckboxJsonParserConsumerProcess", e, action);
        }

        return outputTables;
    }

    private Map<String, LlmJsonQueryInputTableSorMeta> getSorMetaMap(String sorMetaDetail) throws Exception {
        if (sorMetaDetail != null && !sorMetaDetail.isEmpty()) {
            List<LlmJsonQueryInputTableSorMeta> metaList = objectMapper.readValue(sorMetaDetail,
                    new TypeReference<>() {
                    });
            return metaList.stream()
                    .collect(Collectors.toMap(LlmJsonQueryInputTableSorMeta::getSorItemName, meta -> meta,
                            (a, b) -> a));
        }
        return Collections.emptyMap();
    }

    private List<String> getKeywordList(String checkboxKeywords) {
        if (checkboxKeywords != null && !checkboxKeywords.isEmpty()) {
            return java.util.Arrays.stream(checkboxKeywords.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getBboxAsIs(CheckboxJsonParsedResponse.CheckboxGroup group) throws Exception {
        if (group.getGroupBbox() != null && group.getGroupBbox().size() == 4) {
            List<Integer> bbox = group.getGroupBbox();
            var bboxNode = objectMapper.createObjectNode();
            bboxNode.put("topLeftX", bbox.get(0));
            bboxNode.put("topLeftY", bbox.get(1));
            bboxNode.put("bottomRightX", bbox.get(2));
            bboxNode.put("bottomRightY", bbox.get(3));
            return objectMapper.writeValueAsString(bboxNode);
        }
        return "[]";
    }

    private CheckboxQueryOutputTable createOutputTable(CheckboxQueryInputTable input,
            CheckboxJsonParsedResponse.CheckboxOption option, String bboxAsIs, String sectionAlias,
            Map<String, LlmJsonQueryInputTableSorMeta> metaMap, List<String> keywordList,
            Set<String> processedKeywords, String encryptOutputSorItem) throws Exception {
        String label = option.getLabel() != null ? option.getLabel() : "";
        String status = option.getStatus();
        String answer = "C".equalsIgnoreCase(status) ? "Checked" : "Unchecked";

        boolean isLabelMatching = false;
        String labelMatchMessage = "Label matching is disabled";
        String trimmedLabel = label.trim();

        if (!keywordList.isEmpty()) {
            labelMatchMessage = "Label not matched with keywords";
            for (String keyword : keywordList) {
                if (trimmedLabel.equalsIgnoreCase(keyword)) {
                    processedKeywords.add(keyword);
                    if ("Checked".equals(answer)) {
                        isLabelMatching = true;
                        labelMatchMessage = "Label matched with keywords";
                    } else {
                        labelMatchMessage = "Label matched but unchecked";
                    }
                    break;
                }
            }
        }

        // Encryption logic for label and answer based on metadata
        String sorItemName = input.getSorItemName();
        LlmJsonQueryInputTableSorMeta meta = (sorItemName != null) ? metaMap.get(sorItemName) : null;
        boolean isEncryptionEnabled = "true".equalsIgnoreCase(encryptOutputSorItem);
        boolean itemEncryptionEnabled = meta != null && "true".equalsIgnoreCase(meta.getIsEncrypted())
                && isEncryptionEnabled;

        String encryptedLabel = itemEncryptionEnabled ? encryption.encrypt(label, AES_256, input.getSorItemName())
                : label;
        String encryptedSectionAlias = itemEncryptionEnabled
                ? encryption.encrypt(sectionAlias, AES_256, input.getSorItemName())
                : sectionAlias;

        return CheckboxQueryOutputTable.builder()
                .createdOn(input.getCreatedOn())
                .tenantId(input.getTenantId())
                .createdUserId(input.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(input.getTenantId())
                .confidenceScore(0.0)
                .answer(answer)
                .boundingBox(bboxAsIs)
                .paperNo(input.getPaperNo())
                .originId(input.getOriginId())
                .groupId(input.getGroupId())
                .rootPipelineId(input.getRootPipelineId())
                .batchId(input.getBatchId())
                .modelRegistry(input.getModelRegistry())
                .extractedImageUnit(input.getExtractedImageUnit())
                .imageDpi(input.getImageDpi())
                .imageHeight(input.getImageHeight())
                .imageWidth(input.getImageWidth())
                .sorContainerId(input.getSorContainerId())
                .sorItemLabel(encryptedLabel)
                .sorItemName(input.getSorItemName())
                .sectionAlias(encryptedSectionAlias)
                .bBoxAsIs(bboxAsIs)
                .isLabelMatching(isLabelMatching)
                .labelMatchMessage(labelMatchMessage)
                .isEncrypted(itemEncryptionEnabled)
                .encryptionPolicy(AES_256)
                .build();
    }

    private String getDecryptedInputJson(InticsIntegrity encryption, String extractedContent,
            String encryptOutputSorItem) {
        if ("true".equalsIgnoreCase(encryptOutputSorItem)) {
            return encryption.decrypt(extractedContent, AES_256, "CHECKBOX_OUTPUT_JSON");
        } else {
            return extractedContent;
        }
    }
}
