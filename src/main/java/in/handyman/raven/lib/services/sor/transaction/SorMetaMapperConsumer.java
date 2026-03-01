package in.handyman.raven.lib.services.sor.transaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryInputTableSorMeta;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonQueryOutputTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.enums.EncryptionConstants.KVP_JSON_PARSER_ENCRYPTION;

public class SorMetaMapperConsumer implements CoproProcessor.ConsumerProcess<LlmJsonQueryInputTable, LlmJsonQueryOutputTable>{

    public static final String AES_256 = "AES256";
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final InticsIntegrity encryption;
    private final ObjectMapper objectMapper;

    public SorMetaMapperConsumer(Logger log, Marker marker, ActionExecutionAudit action) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper once
    }

    @Override
    public List<LlmJsonQueryOutputTable> process(URL endpoint, LlmJsonQueryInputTable input) throws Exception {

        List<LlmJsonQueryOutputTable> parsedFinalOutput = new ArrayList<>();
        String loggerInput = buildLoggerInput(input);

        log.info(marker, "SorMetaMapperConsumer process started for {}", loggerInput);

        try {
            String extractedContent = input.getResponse();

            // 1. Load SOR Metadata
            List<LlmJsonQueryInputTableSorMeta> sorMetaInputDetails = loadSorMetaDetails(input.getSorMetaDetail(), loggerInput);

            if (extractedContent == null || extractedContent.isEmpty()) {
                log.warn(marker, "Input content is null/empty for {}", loggerInput);
                createEmptyOutputObjects(input, parsedFinalOutput, sorMetaInputDetails, loggerInput);
                return parsedFinalOutput;
            }

            // 2. Decrypt and Repair JSON
            String decryptedJson = getDecryptedInputJson(extractedContent, loggerInput);
            JsonNode cleanedInputJsonNode = JsonRepairUtil.toJsonNode(decryptedJson, objectMapper);

            if (cleanedInputJsonNode == null || cleanedInputJsonNode.isObject()) {
                log.warn(marker, "Input JSON is null or an object (not an array) for {}. Cannot proceed with KVP parsing.", loggerInput);
                createEmptyOutputObjects(input, parsedFinalOutput, sorMetaInputDetails, loggerInput);
                return parsedFinalOutput;
            }

            // 3. Parse KVP JSON
            List<LlmJsonKvpKryptonParser> parsedKvpJson = parseKvpJsonNode(cleanedInputJsonNode, loggerInput);

            // 4. Map SorMeta with Parsed KVP
            mapSorMetaWithParsedOutput(parsedKvpJson, sorMetaInputDetails, parsedFinalOutput, input, loggerInput);

            // 5. Apply Item-wise Encryption
            handleOutputEncryption(parsedFinalOutput, loggerInput);

            log.info(marker, "SorMetaMapperConsumer process completed successfully for {}. Final output size: {}",
                    loggerInput, parsedFinalOutput.size());

        } catch (Exception e) {
            log.error(marker, "Llm json parser action failed for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in process method for SorMetaMapperConsumer", handymanException, action);
            throw handymanException;
        }

        return parsedFinalOutput;
    }

    private String buildLoggerInput(LlmJsonQueryInputTable input) {
        return "Root pipeline Id " + input.getRootPipelineId() +
                " | Batch Id " + input.getBatchId() +
                " | Origin Id " + input.getOriginId() +
                " | Paper No " + input.getPaperNo() +
                " | Container Id " + input.getSorContainerId();
    }

    // --- Data Loading and Parsing Blocks ---

    private List<LlmJsonQueryInputTableSorMeta> loadSorMetaDetails(String sorMetaDetail, String loggerInput) throws HandymanException {
        log.debug(marker, "Loading SOR meta details for {}", loggerInput);
        try {
            return objectMapper.readValue(sorMetaDetail, new TypeReference<>() {});
        } catch (IOException e) {
            log.error(marker, "Failed to load SOR meta details for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in loadSorMetaDetails method for SorMetaMapperConsumer", handymanException, action);
            throw handymanException;
        }
    }

    private String getDecryptedInputJson(String extractedContent, String loggerInput) throws HandymanException {
        String encryptOutputSorItem = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
        log.debug(marker, "Starting decryption check {} . encryptOutputSorItem={}", loggerInput, encryptOutputSorItem);

        if (Objects.equals(encryptOutputSorItem, "true")) {
            try {
                // Log decryption is happening, but NOT the extractedContent
                log.info(marker, "Decrypting extracted content for {}", loggerInput);
                return encryption.decrypt(extractedContent, AES_256, "LLM_OUTPUT_JSON");
            } catch (Exception e) {
                log.error(marker, "Decryption failed for {} with exception: {}", loggerInput, e.getMessage());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error in getDecryptedInputJson method for SorMetaMapperConsumer", handymanException, action);
                throw handymanException;
            }
        }
        return extractedContent;
    }

    private List<LlmJsonKvpKryptonParser> parseKvpJsonNode(JsonNode cleanedInputJsonNode, String loggerInput) throws HandymanException {
        log.debug(marker, "Starting KVP JSON parsing for {}", loggerInput);
        try {
            return objectMapper.readValue(
                    cleanedInputJsonNode.traverse(),
                    new TypeReference<List<LlmJsonKvpKryptonParser>>() {}
            );
        } catch (IOException e) {
            log.error(marker, "KVP JSON parsing failed for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in parseKvpJsonNode method for SorMetaMapperConsumer", handymanException, action);
            throw handymanException;
        }
    }


    // --- Mapping and Preparation Blocks ---
    private void mapSorMetaWithParsedOutput(
            List<LlmJsonKvpKryptonParser> parsedKvpJson,
            List<LlmJsonQueryInputTableSorMeta> sorMetaInputDetails,
            List<LlmJsonQueryOutputTable> parsedFinalOutput,
            LlmJsonQueryInputTable input,
            String loggerInput) {

        log.info(marker, "Starting SOR Meta mapping for {}. Meta count: {}, KVP count: {}",
                loggerInput, sorMetaInputDetails.size(), parsedKvpJson.size());

        if (sorMetaInputDetails == null || sorMetaInputDetails.isEmpty()) {
            log.warn(marker, "SOR metadata input details are empty for {}. Cannot map.", loggerInput);
            return;
        }

        for (LlmJsonQueryInputTableSorMeta sorMeta : sorMetaInputDetails) {

            // 🔥 Get ALL matching KVPs (not findFirst)
            List<LlmJsonKvpKryptonParser> matchedKvps = parsedKvpJson.stream()
                    .filter(kvp -> kvp.getKey() != null
                            && kvp.getKey().equalsIgnoreCase(sorMeta.getSorItemName()))
                    .collect(Collectors.toList());

            if (matchedKvps.isEmpty()) {

                // ✅ Meta not found → create ONE empty parsedResponse
                LlmJsonKvpKryptonParser emptyResponse =
                        buildEmptyKvpFromMeta(sorMeta);

                buildParsedStructuredOutput(parsedFinalOutput, input, emptyResponse);

            } else {

                // ✅ Meta found → could be multiple → create for EACH
                for (LlmJsonKvpKryptonParser kvp : matchedKvps) {

                    LlmJsonKvpKryptonParser parsedResponse =
                            buildKvpFromMatch(sorMeta, kvp);

                    buildParsedStructuredOutput(parsedFinalOutput, input, parsedResponse);
                }
            }
        }

        log.info(marker, "Completed SOR Meta mapping for {}. Output records created: {}",
                loggerInput, parsedFinalOutput.size());
    }

    private LlmJsonKvpKryptonParser buildKvpFromMatch(
            LlmJsonQueryInputTableSorMeta sorMeta,
            LlmJsonKvpKryptonParser kvp) {

        LlmJsonKvpKryptonParser parsedResponse = new LlmJsonKvpKryptonParser();

        parsedResponse.setKey(kvp.getKey());
        parsedResponse.setValue(kvp.getValue());
        parsedResponse.setConfidence(kvp.getConfidence());
        parsedResponse.setSectionAlias(kvp.getSectionAlias());
        parsedResponse.setLabel(kvp.getLabel());
        parsedResponse.setBoundingBox(kvp.getBoundingBox());
        parsedResponse.setLabelMatching(true);
        parsedResponse.setLabelMatchMessage("Matching KVP found in LLM Output.");

        // Apply metadata-driven fields
        parsedResponse.setIsEncrypted(sorMeta.getIsEncrypted());
        parsedResponse.setEncryptionPolicy(sorMeta.getEncryptionPolicy());

        return parsedResponse;
    }

    private LlmJsonKvpKryptonParser buildEmptyKvpFromMeta(
            LlmJsonQueryInputTableSorMeta sorMeta) {

        LlmJsonKvpKryptonParser parsedResponse = new LlmJsonKvpKryptonParser();

        parsedResponse.setKey(sorMeta.getSorItemName());
        parsedResponse.setValue("");
        parsedResponse.setConfidence(0.0);
        parsedResponse.setBoundingBox(null);
        parsedResponse.setSectionAlias(null);
        parsedResponse.setLabel(null);
        parsedResponse.setLabelMatching(false);
        parsedResponse.setLabelMatchMessage("No matching KVP found in LLM Output.");

        parsedResponse.setIsEncrypted(sorMeta.getIsEncrypted());
        parsedResponse.setEncryptionPolicy(sorMeta.getEncryptionPolicy());

        return parsedResponse;
    }

    private void buildParsedStructuredOutput(List<LlmJsonQueryOutputTable> llmJsonQueryOutputTables, LlmJsonQueryInputTable input, LlmJsonKvpKryptonParser parsedDecryptedResponse){

        // This method performs the final DB Insert Preparation and Bounding Box modification

        String boundingBox = Optional.ofNullable(parsedDecryptedResponse.getBoundingBox()).map(Object::toString).orElse("{}");
        String modifiedBoundingBox = contractedBoundingBox(boundingBox, input.getImageWidth(), input.getImageHeight());

        boolean isConfidenceScoreEnabled = Objects.equals(action.getContext().get("sor.transaction.parser.confidence.activator.enable"), "true");
        boolean isBoundingBoxModifierEnable = Objects.equals(action.getContext().get("sor.transaction.bbox.modifier.activator"), "true");
        double  confidenceScore= isConfidenceScoreEnabled ? parsedDecryptedResponse.getConfidence() : 0.00;

        LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable
                .builder()
                .id(UUID.randomUUID().toString())
                .createdOn(String.valueOf(input.getCreatedOn()))
                .tenantId(input.getTenantId())
                .createdUserId(input.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(input.getTenantId())
                .confidenceScore(confidenceScore)
                .sorItemName(parsedDecryptedResponse.getKey())
                .answer(parsedDecryptedResponse.getValue())
                .boundingBox(isBoundingBoxModifierEnable ? modifiedBoundingBox: boundingBox)
                // ... (other fields from input)
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
                // ... (fields from parsed response)
                .sorItemLabel(parsedDecryptedResponse.getLabel())
                .sectionAlias(parsedDecryptedResponse.getSectionAlias())
                .bBoxAsIs(boundingBox)
                .isLabelMatching(parsedDecryptedResponse.isLabelMatching())
                .labelMatchMessage(parsedDecryptedResponse.getLabelMatchMessage())
                .isEncrypted(parsedDecryptedResponse.getIsEncrypted())
                .encryptionPolicy(parsedDecryptedResponse.getEncryptionPolicy())
                .sorContainerInstance(input.getSorContainerInstance())
                .build();
        llmJsonQueryOutputTables.add(insertData);
    }

    public String contractedBoundingBox(String boundingBox, Long width, Long height) {
        // This handles coordinate scaling
        try {
            JsonNode node = objectMapper.readTree(boundingBox);

            // Extract original coordinates
            int x1 = node.path("topLeftX").asInt();
            int y1 = node.path("topLeftY").asInt();
            int x2 = node.path("bottomRightX").asInt();
            int y2 = node.path("bottomRightY").asInt();

            // Apply scaling transformation (assuming input bbox in 1000x1000 space)
            // Using Math.round and casting to int is correct for coordinate output
            int scaledX1 = Math.round((x1 / 1000.0f) * width);
            int scaledY1 = Math.round((y1 / 1000.0f) * height);
            int scaledX2 = Math.round((x2 / 1000.0f) * width);
            int scaledY2 = Math.round((y2 / 1000.0f) * height);

            // Build updated JSON
            ObjectNode updatedNode = objectMapper.createObjectNode();
            updatedNode.put("topLeftX", scaledX1);
            updatedNode.put("topLeftY", scaledY1);
            updatedNode.put("bottomRightX", scaledX2);
            updatedNode.put("bottomRightY", scaledY2);
            log.debug(marker, "Bounding box scaled from 1000x1000 to actual image size. Coords: ({},{}) to ({},{})", scaledX1, scaledY1, scaledX2, scaledY2);

            return objectMapper.writeValueAsString(updatedNode);
        } catch (Exception e) {
            log.error(marker, "Error in modifying bounding box method with exception: {}", e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in modifying bounding box method for SorMetaMapperConsumer", handymanException, action);
            return "{}";
        }
    }


    private void createEmptyOutputObjects(LlmJsonQueryInputTable input, List<LlmJsonQueryOutputTable> llmJsonQueryOutputTables,
                                          List<LlmJsonQueryInputTableSorMeta> sorMetaInputDetails, String loggerInput) {
        if(sorMetaInputDetails.isEmpty()){
            log.warn(marker, "SOR metadata input details are empty, cannot create empty output objects for {}", loggerInput);
            return;
        }else {
            log.info(marker, "Creating {} empty output objects as parsed json is null/empty for {}", sorMetaInputDetails.size(), loggerInput);
            sorMetaInputDetails.forEach(llmJsonQueryInputTableSorMeta -> {
                LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable.builder()
                        // ... (Populate all necessary input and default fields)
                        .createdOn(String.valueOf(input.getCreatedOn()))
                        .createdUserId(input.getTenantId())
                        .tenantId(input.getTenantId())
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(input.getTenantId())
                        .boundingBox("{}")
                        .paperNo(input.getPaperNo())
                        .originId(input.getOriginId())
                        .groupId(input.getGroupId())
                        .rootPipelineId(input.getRootPipelineId())
                        .sorItemName(llmJsonQueryInputTableSorMeta.getSorItemName())
                        .answer("") // Empty answer
                        .sorItemLabel("") // Empty label
                        .sectionAlias("") // Empty alias
                        .sorContainerInstance("")
                        .batchId(input.getBatchId())
                        .modelRegistry(input.getModelRegistry())
                        .extractedImageUnit(input.getExtractedImageUnit())
                        .imageDpi(input.getImageDpi())
                        .imageHeight(input.getImageHeight())
                        .imageWidth(input.getImageWidth())
                        .sorContainerId(input.getSorContainerId())
                        .isEncrypted(llmJsonQueryInputTableSorMeta.getIsEncrypted())
                        .encryptionPolicy(llmJsonQueryInputTableSorMeta.getEncryptionPolicy())
                        .confidenceScore(0.0)
                        .build();
                llmJsonQueryOutputTables.add(insertData);
            });
        }
    }


    // --- Encryption Blocks ---

    private void handleOutputEncryption(List<LlmJsonQueryOutputTable> parsedFinalOutput, String loggerInput) {
        if (action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION).equals("true")) {
            log.info(marker, "Starting final encryption steps for {}", loggerInput);

            encryptAnswers(parsedFinalOutput, loggerInput);

            if (action.getContext().get(KVP_JSON_PARSER_ENCRYPTION).equals("true")) {
                encryptLabels(parsedFinalOutput, loggerInput);
                encryptSectionAlias(parsedFinalOutput, loggerInput);
            }
            log.info(marker, "Completed final encryption steps for {}", loggerInput);
        }
    }

    public void encryptAnswers(List<LlmJsonQueryOutputTable> inputList, String loggerInput) {
        // Renamed to remove dependency on the field 'encryption' in the signature
        log.info(marker, "Starting encryption for answers for {}", loggerInput);
        if (inputList == null || inputList.isEmpty()) {
            return;
        }

        try {
            // Step 1: Convert to EncryptionRequestClass
            List<EncryptionRequestClass> encryptionRequests = inputList.stream()
                    .filter(o -> "true".equalsIgnoreCase(o.getIsEncrypted()))
                    // Filter out null or empty answers for encryption
                    .filter(obj -> obj.getId() != null && obj.getAnswer() != null && !obj.getAnswer().isEmpty())
                    .map(obj -> new EncryptionRequestClass(AES_256, obj.getAnswer(), obj.getId()))
                    .collect(Collectors.toList());

            log.info(marker, "Total records with 'isEncrypted=true' for answers: {}", encryptionRequests.size());

            if (encryptionRequests.isEmpty()) return;

            // Step 2: Call external Protegrity API
            List<EncryptionRequestClass> responseList = encryption.encrypt(encryptionRequests);

            // Step 3: Build a lookup map from response (Key is ID, Value is Encrypted Answer)
            Map<String, String> encryptedMap = buildEncryptionMap(responseList);

            // Step 4: Update original list
            updateOutputList(inputList, encryptedMap, "answer");
            log.info(marker, "Completed encryption for answers for {}", loggerInput);

        } catch (Exception e) {
            log.error(marker, "Encryption failed for answers for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in encryptAnswers method for SorMetaMapperConsumer", handymanException, action);
        }
    }


    public void encryptLabels(List<LlmJsonQueryOutputTable> inputList, String loggerInput) {
        log.info(marker, "Starting encryption for label for {}", loggerInput);
        if (inputList == null || inputList.isEmpty()) {
            return;
        }

        try {
            // Step 1: Convert to EncryptionRequestClass
            List<EncryptionRequestClass> encryptionRequests = inputList.stream()
                    .filter(obj -> obj.getId() != null && obj.getSorItemLabel() != null && !obj.getSorItemLabel().isEmpty())
                    .map(obj -> new EncryptionRequestClass(AES_256, obj.getSorItemLabel(), obj.getId()))
                    .collect(Collectors.toList());

            log.info(marker, "Total records to encrypt for Labels: {}", encryptionRequests.size());

            if (encryptionRequests.isEmpty()) return;

            // Step 2: Call external Protegrity API
            List<EncryptionRequestClass> responseList = encryption.encrypt(encryptionRequests);

            // Step 3: Build a lookup map from response
            Map<String, String> encryptedMap = buildEncryptionMap(responseList);

            // Step 4: Update original list
            updateOutputList(inputList, encryptedMap, "label");
            log.info(marker, "Completed encryption for label for {}", loggerInput);

        } catch (Exception e) {
            log.error(marker, "Encryption failed for labels for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in encryptLabels method for SorMetaMapperConsumer", handymanException, action);
        }
    }

    public void encryptSectionAlias(List<LlmJsonQueryOutputTable> inputList, String loggerInput) {
        log.info(marker, "Starting encryption for section Alias for {}", loggerInput);
        if (inputList == null || inputList.isEmpty()) {
            return;
        }

        try {
            // Step 1: Convert to EncryptionRequestClass
            List<EncryptionRequestClass> encryptionRequests = inputList.stream()
                    .filter(obj -> obj.getId() != null && obj.getSectionAlias() != null && !obj.getSectionAlias().isEmpty())
                    .map(obj -> new EncryptionRequestClass(AES_256, obj.getSectionAlias(), obj.getId()))
                    .collect(Collectors.toList());

            log.info(marker, "Total records to encrypt for section alias: {}", encryptionRequests.size());

            if (encryptionRequests.isEmpty()) return;

            // Step 2: Call external Protegrity API
            List<EncryptionRequestClass> responseList = encryption.encrypt(encryptionRequests);

            // Step 3: Build a lookup map from response
            Map<String, String> encryptedMap = buildEncryptionMap(responseList);

            // Step 4: Update original list
            updateOutputList(inputList, encryptedMap, "sectionAlias");
            log.info(marker, "Completed encryption for section Alias for {}", loggerInput);

        } catch (Exception e) {
            log.error(marker, "Encryption failed for section alias for {} with exception: {}", loggerInput, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in encryptSectionAlias method for SorMetaMapperConsumer", handymanException, action);
        }
    }

    // --- Helper Methods for Encryption ---

    private Map<String, String> buildEncryptionMap(List<EncryptionRequestClass> responseList) {
        return responseList.stream()
                .filter(item -> item.getKey() != null)
                .collect(Collectors.toMap(
                        EncryptionRequestClass::getKey,
                        EncryptionRequestClass::getValue
                ));
    }

    private void updateOutputList(List<LlmJsonQueryOutputTable> inputList, Map<String, String> encryptedMap, String fieldName) {
        for (LlmJsonQueryOutputTable item : inputList) {
            if (item.getId() != null && encryptedMap.containsKey(item.getId())) {
                switch (fieldName) {
                    case "answer":
                        item.setAnswer(encryptedMap.get(item.getId()));
                        break;
                    case "label":
                        item.setSorItemLabel(encryptedMap.get(item.getId()));
                        break;
                    case "sectionAlias":
                        item.setSectionAlias(encryptedMap.get(item.getId()));
                        break;
                }
            }
        }
    }
}