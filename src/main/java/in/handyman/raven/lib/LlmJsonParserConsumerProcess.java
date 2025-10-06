package in.handyman.raven.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.LlmJsonParser;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.*;
import in.handyman.raven.util.LoggingInitializer;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class LlmJsonParserConsumerProcess implements CoproProcessor.ConsumerProcess<LlmJsonQueryInputTable, LlmJsonQueryOutputTable> {
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final LlmJsonParser llmJsonParser;

    public LlmJsonParserConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, LlmJsonParser llmJsonParser) {
        // Ensure logging is initialized before any logging operations
        LoggingInitializer.initialize();
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.llmJsonParser = llmJsonParser;
    }

    public List<LlmJsonQueryOutputTable> process(URL endpoint, LlmJsonQueryInputTable input) throws Exception {
        List<LlmJsonQueryOutputTable> llmJsonQueryOutputTables = new ArrayList<>();
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);
        String encryptOutputSorItem = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
        String loggerInput = " Root pipeline Id " + input.getRootPipelineId() + " batch Id " + input.getBatchId() + " Origin Id " + input.getOriginId() + " paper No " + input.getPaperNo() + " Container Id " + input.getSorContainerId();

        log.debug(marker, "Llm json parser action started for {} ", loggerInput);
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            final String selectQuery = llmJsonParser.getQuerySet();
            log.debug(marker, "Llm json parser action for {} with query {} has been started ", loggerInput, selectQuery);

            String boundingBox = "";
            String modifiedBoundingBox;
            String extractedContent = input.getResponse();
            String jsonResponse;
            if (extractedContent != null) {
                jsonResponse = getDecryptedInputJson(encryption, extractedContent, encryptOutputSorItem);

                List<LlmJsonQueryInputTableSorMeta> llmJsonQueryInputTableSorMetas = objectMapper.readValue(input.getSorMetaDetail(), new TypeReference<>() {
                });
                JsonNode stringObjectMap = convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
                if (stringObjectMap != null && stringObjectMap.isObject()) {
                    log.info("Processing an object-type input. Type: {}", stringObjectMap.getClass().getSimpleName());
                    List<LlmJsonParsedResponse> innerParsedResponses = new ArrayList<>();

                    parseJsonNode(stringObjectMap, "", "", innerParsedResponses);
                    log.info("Total parsed responses before encryption for {} is {} ", loggerInput, innerParsedResponses.size());
                    List<LlmJsonParsedResponse> parsedResponses = encryptJsonAnswers(action, innerParsedResponses, llmJsonQueryInputTableSorMetas, encryption, encryptOutputSorItem);
                    log.info("Total parsed responses after encryption for {} is {} ", loggerInput, parsedResponses.size());
                    for (LlmJsonParsedResponse parsedResponse : parsedResponses) {
                        LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable.builder()
                                .createdOn(String.valueOf(input.getCreatedOn()))
                                .tenantId(input.getTenantId())
                                .createdUserId(input.getTenantId())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .lastUpdatedUserId(input.getTenantId())
                                .sorItemName(parsedResponse.getSorItemName())
                                .answer(parsedResponse.getAnswer())
                                .boundingBox(boundingBox)
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
                                .build();

                        llmJsonQueryOutputTables.add(insertData);
                        log.info("Insert processing for {} and parsed xenon nodes size {}", loggerInput, parsedResponses.size());

                    }
                } else if (stringObjectMap != null && stringObjectMap.isArray()) {

                    log.info("Processing an array-type input. Type: {}", stringObjectMap.getClass().getSimpleName());

                    List<LlmJsonParserKvpKrypton> innerParsedResponsesKrypton = null;
                    try {
                        innerParsedResponsesKrypton = new ArrayList<>();

                        innerParsedResponsesKrypton = objectMapper.readValue(
                                stringObjectMap.traverse(),
                                new TypeReference<List<LlmJsonParserKvpKrypton>>() {
                                }
                        );
                    } catch (Exception e) {
                        action.getContext().put(llmJsonParser.getName() + ".isSuccessful", "false");
                        HandymanException handymanException = new HandymanException(e);
                        HandymanException.insertException("Error in execute method for Llm json parser action for origin Id " + input.getOriginId() + " paper No " + input.getPaperNo(), handymanException, action);

                    }


                    for (LlmJsonParserKvpKrypton parsedResponse : innerParsedResponsesKrypton) {

                        boolean isBboxEnabled = Objects.equals(action.getContext().get("sor.transaction.bbox.parser.activator.enable"), "true");
                        log.debug("Status for the activator sor.transaction.bbox.parser.activator.enable. Result: {} ", isBboxEnabled);
                        boundingBox = isBboxEnabled ? Optional.ofNullable(parsedResponse.getBoundingBox()).map(Object::toString).orElse("{}") : "{}";
                        modifiedBoundingBox = isBboxEnabled
                                ? Optional.ofNullable(contractedBoundingBox(boundingBox, input.getImageWidth(), input.getImageHeight()))
                                .filter(b -> !b.isEmpty())
                                .orElse("{}")
                                : "{}";
                        boolean isConfidenceScoreEnabled = Objects.equals(action.getContext().get("sor.transaction.parser.confidence.activator.enable"), "true");
                        log.debug("Status for the activator sor.transaction.parser.confidence.activator.enable. Result: {} ", isConfidenceScoreEnabled);

                        double confidenceScore = isConfidenceScoreEnabled ? parsedResponse.getConfidence() : 0.00;
                        LlmJsonParserKvpKrypton parsedEncryptResponse = encryptJsonArrayAnswers(action, parsedResponse, llmJsonQueryInputTableSorMetas, encryption, encryptOutputSorItem);

                        LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable.builder()
                                .createdOn(String.valueOf(input.getCreatedOn()))
                                .tenantId(input.getTenantId())
                                .createdUserId(input.getTenantId())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .lastUpdatedUserId(input.getTenantId())
                                .confidenceScore(confidenceScore)
                                .sorItemName(parsedEncryptResponse.getKey())
                                .answer(parsedEncryptResponse.getValue())
                                .boundingBox(modifiedBoundingBox)
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
                                .sorItemLabel(parsedEncryptResponse.getLabel())
                                .sectionAlias(parsedEncryptResponse.getSectionAlias())
                                .bBoxAsIs(boundingBox)
                                .build();

                        llmJsonQueryOutputTables.add(insertData);
                        log.debug("Insert processing for {} and parsed krypton nodes size {}", loggerInput, innerParsedResponsesKrypton.size());
                    }

                }
            }else {
                log.debug("Extracted content is null for {}. Skipping processing.", loggerInput);
                    LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable.builder()
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
                            .answer("")
                            .batchId(input.getBatchId())
                            .modelRegistry(input.getModelRegistry())
                            .extractedImageUnit(input.getExtractedImageUnit())
                            .imageDpi(input.getImageDpi())
                            .imageHeight(input.getImageHeight())
                            .imageWidth(input.getImageWidth())
                            .sorContainerId(input.getSorContainerId())
                            .build();

                    llmJsonQueryOutputTables.add(insertData);

            }


            log.debug(marker, " Llm json parser action has been completed {}  ", llmJsonParser.getName());
        } catch (Exception e) {
            action.getContext().put(llmJsonParser.getName() + ".isSuccessful", "false");
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for Llm json parser action", handymanException, action);

        }
        return llmJsonQueryOutputTables;
    }

    public String contractedBoundingBox(String boundingBox, Long width, Long height) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(boundingBox);

            // Extract original coordinates
            int x1 = node.path("topLeftX").asInt();
            int y1 = node.path("topLeftY").asInt();
            int x2 = node.path("bottomRightX").asInt();
            int y2 = node.path("bottomRightY").asInt();

            // Apply scaling transformation (assuming input bbox in 1000x1000 space)
            x1 = Math.round((x1 / 1000.0f) * width);
            y1 = Math.round((y1 / 1000.0f) * height);
            x2 = Math.round((x2 / 1000.0f) * width);
            y2 = Math.round((y2 / 1000.0f) * height);

            // Build updated JSON
            ObjectNode updatedNode = mapper.createObjectNode();
            updatedNode.put("topLeftX", x1);
            updatedNode.put("topLeftY", y1);
            updatedNode.put("bottomRightX", x2);
            updatedNode.put("bottomRightY", y2);

            System.out.println(updatedNode);
            // Return as a JSON string
            return mapper.writeValueAsString(updatedNode);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public String getDecryptedInputJson(InticsIntegrity encryption, String extractedContent, String encryptOutputSorItem) {


        if (Objects.equals(encryptOutputSorItem, "true")) {
            return encryption.decrypt(extractedContent, "AES256", "LLM_OUTPUT_JSON");
        } else {
            return extractedContent;
        }

    }

    public List<LlmJsonParsedResponse> encryptJsonAnswers(ActionExecutionAudit action,
                                                          List<LlmJsonParsedResponse> responses,
                                                          List<LlmJsonQueryInputTableSorMeta> metaList, InticsIntegrity inticsIntegrity, String encryptData
    ) throws Exception {

        // Create a map of sorItemName to encryption policy
        Map<String, LlmJsonQueryInputTableSorMeta> metaMap = new HashMap<>();
        for (LlmJsonQueryInputTableSorMeta meta : metaList) {
            metaMap.put(meta.getSorItemName(), meta);
        }

        // Process encryption
        return responses.stream().map(response -> {
            try {
                LlmJsonQueryInputTableSorMeta meta = metaMap.get(response.getSorItemName());
                if (meta != null && "true".equalsIgnoreCase(meta.getIsEncrypted())) {

                    if (Objects.equals(encryptData, "true")) {
                        if (Objects.equals(meta.getIsEncrypted().toString(), "true")) {
                            response.setAnswer(trimTo255Characters(response.getAnswer(), action));
                            response.setAnswer(inticsIntegrity.encrypt(response.getAnswer(), "AES256", meta.getSorItemName()));
                        } else {
                            response.setAnswer(trimTo255Characters(response.getAnswer(), action));
                            response.setAnswer(response.getAnswer());
                        }
                    } else {
                        response.setAnswer(response.getAnswer());
                    }
                }
            } catch (Exception e) {
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error in encryptJsonAnswers method for Llm json parser action " + action.getActionName(), handymanException, action);
            }
            return response;
        }).collect(Collectors.toList());
    }


    public static LlmJsonParserKvpKrypton encryptJsonArrayAnswers(
            ActionExecutionAudit action,
            LlmJsonParserKvpKrypton response,
            List<LlmJsonQueryInputTableSorMeta> metaList,
            InticsIntegrity inticsIntegrity,
            String encryptData
    ) throws Exception {

        // Create a map of sorItemName to encryption metadata
        Map<String, LlmJsonQueryInputTableSorMeta> metaMap = new HashMap<>();
        for (LlmJsonQueryInputTableSorMeta meta : metaList) {
            metaMap.put(meta.getSorItemName(), meta);
        }

        // Check if response key exists in the metadata map
        LlmJsonQueryInputTableSorMeta meta = metaMap.get(response.getKey());

        if (meta != null && "true".equalsIgnoreCase(meta.getIsEncrypted())) {
            if (Objects.equals(encryptData, "true")) {
                response.setValue(trimTo255Characters(response.getValue(), action));
                response.setValue(inticsIntegrity.encrypt(response.getValue(), "AES256", meta.getSorItemName()));
            } else {
                response.setValue(trimTo255Characters(response.getValue(), action));
            }
        } else {
            response.setValue(trimTo255Characters(response.getValue(), action));
        }

        return response;
    }


    private void parseJsonNode(JsonNode rootNode, String currentKey, String parentPath, List<LlmJsonParsedResponse> parsedResponses) {
        if (rootNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                String newCurrentKey = key;
                String newParentPath = parentPath.isEmpty() ? currentKey : parentPath + ", " + currentKey;
                parseJsonNode(field.getValue(), newCurrentKey, newParentPath, parsedResponses);
            }
        } else if (rootNode.isArray()) {
            for (JsonNode arrayElement : rootNode) {
                parseJsonNode(arrayElement, currentKey, parentPath, parsedResponses);
            }
        } else {
            LlmJsonParsedResponse parsedResponse = LlmJsonParsedResponse.builder()
                    .sorContainerName(parentPath)
                    .sorItemName(currentKey)
                    .answer(rootNode.asText())
                    .build();
            parsedResponses.add(parsedResponse);
        }
    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                log.debug("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);
                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");
                    // Convert the cleaned JSON string to a JsonNode
                    jsonResponse = repairJson(jsonString);
                    if (!jsonResponse.isEmpty()) {
                        return objectMapper.readTree(jsonResponse);
                    } else {
                        return null;
                    }
                } else {
                    jsonResponse = repairJson(jsonResponse);
                    return objectMapper.readTree(jsonResponse);
                }
            } else if ((jsonResponse.contains("{")) | (jsonResponse.contains("["))) {
                log.info("Input does not contain the required ```json``` markers. So processing it based on the indication of object literals.");
                return objectMapper.readTree(jsonResponse);
            } else {
                log.info("Input does not contain the required ```json``` markers or any indication of object literals. So returning null.");
                return null;
            }
        } catch (Exception e) {
            HandymanException exception = new HandymanException(e);
            HandymanException.insertException("Error in convertFormattedJsonStringToJsonNode method for Llm json parser action", exception, action);
            return null;
        }
    }

    private String repairJson(String jsonString) {

        // Ensure keys and string values are enclosed in double quotes
        jsonString = addMissingQuotes(jsonString);

        // Balance braces and brackets
        jsonString = balanceBracesAndBrackets(jsonString);

        // Assign empty strings to keys with no values
        jsonString = assignEmptyValues(jsonString);

        return jsonString;
    }

    private String addMissingQuotes(String jsonString) {
        // Ensure keys are enclosed in double quotes
        jsonString = jsonString.replaceAll("(\\{|,\\s*)(\\w+)(?=\\s*:)", "$1\"$2\"");

        // Ensure string values are enclosed in double quotes
        // This regex matches values that are not already enclosed in quotes
        jsonString = jsonString.replaceAll("(?<=:)\\s*([^\"\\s,\\n}\\]]+)(?=\\s*(,|}|\\n|\\]))", "\"$1\"");

        return jsonString;
    }

    private String balanceBracesAndBrackets(String jsonString) {
        // Balance braces and brackets
        int openBraces = 0;
        int closeBraces = 0;
        int openBrackets = 0;
        int closeBrackets = 0;

        for (char c : jsonString.toCharArray()) {
            if (c == '{') openBraces++;
            if (c == '}') closeBraces++;
            if (c == '[') openBrackets++;
            if (c == ']') closeBrackets++;
        }

        // Add missing closing braces
        while (openBraces > closeBraces) {
            jsonString += "}";
            closeBraces++;
        }

        // Add missing closing brackets
        while (openBrackets > closeBrackets) {
            jsonString += "]";
            closeBrackets++;
        }

        return jsonString;
    }

    private String assignEmptyValues(String jsonString) {
        // Assign empty strings to keys with no values
        jsonString = jsonString.replaceAll("(?<=:)\\s*(?=,|\\s*}|\\s*\\])", "\"\"");
        return jsonString;
    }

    private static String trimTo255Characters(String input, ActionExecutionAudit action) {
        boolean trimExtractedValue = Objects.equals(action.getContext().get("llm.json.parser.trim.extracted.value"), "true");
        String trimmedPredictedValue = "";
        if (trimExtractedValue) {
            if (input != null && input.length() > 255) {
                trimmedPredictedValue = input.substring(0, 255);
            } else {
                // Return the original string if length is <= 255
                trimmedPredictedValue = input;
            }

        }
        return trimmedPredictedValue;
    }


}
