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
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.enums.EncryptionConstants.KVP_JSON_PARSER_ENCRYPTION;

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
                    for (CheckboxJsonParsedResponse.CheckboxGroup group : parsedResponse.getGroups()) {
                        String bboxAsIs = "[]";
                        if (group.getGroupBbox() != null && group.getGroupBbox().size() == 4) {
                            List<Integer> bbox = group.getGroupBbox();
                            var bboxNode = objectMapper.createObjectNode();
                            bboxNode.put("topLeftX", bbox.get(0));
                            bboxNode.put("topLeftY", bbox.get(1));
                            bboxNode.put("bottomRightX", bbox.get(2));
                            bboxNode.put("bottomRightY", bbox.get(3));
                            bboxAsIs = objectMapper.writeValueAsString(bboxNode);
                        }
                        String sectionAlias = group.getSectionHeader() != null ? group.getSectionHeader() : "";

                        for (CheckboxJsonParsedResponse.CheckboxOption option : group.getOpts()) {
                            String label = option.getLabel() != null ? option.getLabel() : "";
                            String status = option.getStatus();
                            String answer = "C".equalsIgnoreCase(status) ? "Checked" : "Unchecked";

                            boolean isEncryptionEnabled = "true".equalsIgnoreCase(
                                    action.getContext().getOrDefault(KVP_JSON_PARSER_ENCRYPTION, "true"));

                            // Encryption logic for label and answer
                            String encryptedLabel = encryption.encrypt(label, AES_256, "check_box_extraction");
                            String encryptedSectionAlias = encryption.encrypt(sectionAlias, AES_256,
                                    "check_box_extraction");
                            String encryptedAnswer = answer;

                            if (isEncryptionEnabled) {
                                encryptedAnswer = encryption.encrypt(answer, AES_256, "check_box_extraction");
                            }

                            CheckboxQueryOutputTable output = CheckboxQueryOutputTable.builder()
                                    .createdOn(String.valueOf(input.getCreatedOn()))
                                    .tenantId(input.getTenantId())
                                    .createdUserId(input.getTenantId())
                                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                    .lastUpdatedUserId(input.getTenantId())
                                    .confidenceScore(0.0)
                                    .sorItemName("check_box_extraction")
                                    .answer(encryptedAnswer)
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
                                    .sectionAlias(encryptedSectionAlias)
                                    .bBoxAsIs(bboxAsIs)
                                    .isLabelMatching(false)
                                    .labelMatchMessage("")
                                    .isEncrypted(String.valueOf(isEncryptionEnabled))
                                    .encryptionPolicy(AES_256)
                                    .build();

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

    private String getDecryptedInputJson(InticsIntegrity encryption, String extractedContent,
            String encryptOutputSorItem) {
        if ("true".equalsIgnoreCase(encryptOutputSorItem)) {
            return encryption.decrypt(extractedContent, AES_256, "CHECKBOX_OUTPUT_JSON");
        } else {
            return extractedContent;
        }
    }
}
