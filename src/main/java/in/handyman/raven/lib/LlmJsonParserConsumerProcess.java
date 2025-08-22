package in.handyman.raven.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequest;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.LlmJsonParser;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.*;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.List;                 // For List
import java.util.stream.Stream;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class LlmJsonParserConsumerProcess implements CoproProcessor.ConsumerProcess<LlmJsonQueryInputTable, LlmJsonQueryOutputTable> {
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final LlmJsonParser llmJsonParser;

    public LlmJsonParserConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, LlmJsonParser llmJsonParser) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.llmJsonParser = llmJsonParser;
    }

    public List<LlmJsonQueryOutputTable> process(URL endpoint, LlmJsonQueryInputTable input) throws Exception {

        List<LlmJsonQueryOutputTable> llmJsonQueryOutputTables = new ArrayList<>();
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(llmJsonParser.getResourceConn());
        log.info(marker, "Llm json parser action started for origin Id {} paper No {} ", input.getOriginId(), input.getPaperNo());
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            final String selectQuery = llmJsonParser.getQuerySet();
            log.info(marker, "Llm json parser action {} has been started ", selectQuery);
            List<LlmJsonQueryInputTable> inputTableList = jdbi.withHandle(handle -> handle.createQuery(selectQuery)
                    .mapToBean(LlmJsonQueryInputTable.class)
                    .list());

            jdbi.useTransaction(handle -> {
                String boundingBox = "";
                String extractedContent = input.getResponse();
                String jsonResponse;

                String encryptOutputSorItem = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
                InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action, log);

                jsonResponse = decryptData(encryptOutputSorItem,extractedContent,"AES256","LLM_OUTPUT_JSON",encryption);
//                if (Objects.equals(encryptOutputSorItem, "true")) {
//                    jsonResponse = encryption.decrypt(extractedContent, "AES256", "LLM_OUTPUT_JSON");
//                } else {
//                    jsonResponse = extractedContent;
//                }

                List<LlmJsonQueryInputTableSorMeta> llmJsonQueryInputTableSorMetas = objectMapper.readValue(input.getSorMetaDetail(), new TypeReference<>() {
                });
                JsonNode stringObjectMap = convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper);
                if (stringObjectMap != null && stringObjectMap.isObject()) {

                    final String insertQueryXenon = buildInsertQueryXenon();


                    List<LlmJsonParsedResponse> innerParsedResponses = new ArrayList<>();
                    log.info(marker, "Llm json parser insert query {}", insertQueryXenon);

                    parseJsonNode(stringObjectMap, "", "", innerParsedResponses);


                    List<LlmJsonParsedResponse> parsedResponses = encryptJsonAnswers(action, innerParsedResponses, llmJsonQueryInputTableSorMetas, encryption, encryptOutputSorItem);
                    for (LlmJsonParsedResponse parsedResponse : parsedResponses) {

                        getInsertIntoXenonResultTable(handle, input, parsedResponse, insertQueryXenon);

                        log.info("\n insert processing for process {} :\n", input.getProcess());

                    }
                } else if (stringObjectMap != null && stringObjectMap.isArray()) {

                    log.info("Processing an array-type input. Type: {}", stringObjectMap.getClass().getSimpleName());

                    final String insertQueryKrypton = buildInsertQueryKrypton();

                    List<LlmJsonParserKvpKrypton> innerParsedResponsesKrypton = null;
                    try {
                        log.info(marker, "LLM json parser insert query {}", insertQueryKrypton);
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
                    List<EncryptionRequest> list = encryptDataMethod(innerParsedResponsesKrypton,encryption,encryptOutputSorItem,action,"AES256",llmJsonQueryInputTableSorMetas);
                    int index=0;
                    for (LlmJsonParserKvpKrypton parsedResponse : innerParsedResponsesKrypton) {

                        boolean isBboxEnabled = Objects.equals(action.getContext().get("sor.transaction.bbox.parser.activator.enable"), "true");
                        log.info("Status for the activator sor.transaction.bbox.parser.activator.enable. Result: {} ", isBboxEnabled);
                        boundingBox = isBboxEnabled ? Optional.ofNullable(parsedResponse.getBoundingBox()).map(Object::toString).orElse("{}") : "{}";

                        boolean isConfidenceScoreEnabled = Objects.equals(action.getContext().get("sor.transaction.parser.confidence.activator.enable"), "true");
                        log.info("Status for the activator sor.transaction.parser.confidence.activator.enable. Result: {} ", isConfidenceScoreEnabled);

                        double confidenceScore = isConfidenceScoreEnabled ? parsedResponse.getConfidence() : 0.00;

                        LlmJsonParserKvpKrypton parsedEncryptResponse = encryptJsonArrayAnswers(action, parsedResponse, llmJsonQueryInputTableSorMetas, encryption, encryptOutputSorItem);
                        LlmJsonQueryOutputTable insertData = LlmJsonQueryOutputTable.builder()
                                .createdOn(String.valueOf(input.getCreatedOn()))
                                .tenantId(input.getTenantId())
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .lastUpdatedUserId(input.getTenantId())
                                .confidenceScore(confidenceScore)
                                //.sorItemName(parsedEncryptResponse.getKey())
                                .sorItemName(list.get(index).getKey())
                                //.answer(parsedEncryptResponse.getValue())
                                .answer( Objects.equals(encryptOutputSorItem, "true")?
                                        list.get(index).getValue():parsedResponse.getValue())
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
                                .sorItemLabel(parsedEncryptResponse.getLabel())
                                .sectionAlias(parsedEncryptResponse.getSectionAlias())
                                .build();

                        getInsertIntoKryptonResultTable(handle, insertQueryKrypton, insertData);
                        index++;
                    }

                }
            });

            log.info(marker, " Llm json parser action has been completed {}  ", llmJsonParser.getName());
        } catch (Exception e) {
            action.getContext().put(llmJsonParser.getName() + ".isSuccessful", "false");
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for Llm json parser action", handymanException, action);

        }
        return llmJsonQueryOutputTables;
    }

    public static List<LlmJsonParsedResponse> encryptJsonAnswers(ActionExecutionAudit action,
                                                                 List<LlmJsonParsedResponse> responses,
                                                                 List<LlmJsonQueryInputTableSorMeta> metaList, InticsIntegrity inticsIntegrity, String encryptData
    ) throws Exception {

        // Create a map of sorItemName to encryption policy
        Map<String, LlmJsonQueryInputTableSorMeta> metaMap = new HashMap<>();
        for (LlmJsonQueryInputTableSorMeta meta : metaList) {
            metaMap.put(meta.getSorItemName(), meta);
        }
        List<EncryptionRequest> responseData=encryptData(responses,inticsIntegrity,encryptData,metaMap,"AES256",action);
        AtomicInteger index = new AtomicInteger(0);
        // Process encryption
        return responses.stream().map(response -> {
            try {
                LlmJsonQueryInputTableSorMeta meta = metaMap.get(response.getSorItemName());
                int currentIndex = index.getAndIncrement();
                if (meta != null && "true".equalsIgnoreCase(meta.getIsEncrypted())) {

                    if (Objects.equals(encryptData, "true")) {
                        if (Objects.equals(meta.getIsEncrypted().toString(), "true")) {
                           // response.setAnswer(trimTo255Characters(response.getAnswer(), action));
                            //response.setAnswer(inticsIntegrity.encrypt(response.getAnswer(), "AES256", meta.getSorItemName()));
                            if (responseData!=null  && currentIndex < responseData.size()) {
                                response.setAnswer(responseData.get(currentIndex).getValue());
                            }
                        } else {
                            response.setAnswer(trimTo255Characters(response.getAnswer(), action));
                            response.setAnswer(response.getAnswer());
                        }
                    } else {
                        response.setAnswer(response.getAnswer());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        //List<EncryptionRequest> responseData=encryptDataObj(response,inticsIntegrity,encryptData,meta,"AES256",action);

        if (meta != null && "true".equalsIgnoreCase(meta.getIsEncrypted())) {
            if (Objects.equals(encryptData, "true")) {
                response.setValue(trimTo255Characters(response.getValue(), action));
                //response.setValue(inticsIntegrity.encrypt(response.getValue(), "AES256", meta.getSorItemName()));
               // response.setValue(responseData.get(0).getValue());
            } else {
                response.setValue(trimTo255Characters(response.getValue(), action));
            }
        } else {
            response.setValue(trimTo255Characters(response.getValue(), action));
        }

        return response;
    }

    private static void getInsertIntoXenonResultTable(Handle handle, LlmJsonQueryInputTable inputTable, LlmJsonParsedResponse parsedResponse, String insertQueryXenon) {

        handle.createUpdate(insertQueryXenon)
                .bind(0, inputTable.getCreatedOn())
                .bind(1, inputTable.getTenantId())
                .bind(2, CreateTimeStamp.currentTimestamp())
                .bind(3, inputTable.getTenantId())
                .bind(4, parsedResponse.getSorContainerName())
                .bind(5, parsedResponse.getSorItemName())
                .bind(6, parsedResponse.getAnswer())
                .bind(7, inputTable.getPaperNo())
                .bind(8, inputTable.getOriginId())
                .bind(9, inputTable.getGroupId())
                .bind(10, inputTable.getTenantId())
                .bind(11, inputTable.getRootPipelineId())
                .bind(12, inputTable.getBatchId())
                .bind(13, inputTable.getModelRegistry())
                .bind(14, inputTable.getExtractedImageUnit())
                .bind(15, inputTable.getImageDpi())
                .bind(16, inputTable.getImageHeight())
                .bind(17, inputTable.getImageWidth())
                .bind(18, inputTable.getSorContainerId())
                .bind(19, inputTable.getSorItemLabel())
                .bind(20, inputTable.getSectionAlias())
                .execute();
    }

    private static int getInsertIntoKryptonResultTable(Handle handle, String insertQueryKrypton, LlmJsonQueryOutputTable llmJsonQueryOutputTable) {

        return handle.createUpdate(insertQueryKrypton)
                .bind(0, llmJsonQueryOutputTable.getCreatedOn())
                .bind(1, llmJsonQueryOutputTable.getTenantId())
                .bind(2, CreateTimeStamp.currentTimestamp())
                .bind(3, llmJsonQueryOutputTable.getTenantId())
                .bind(4, llmJsonQueryOutputTable.getConfidenceScore())
                .bind(5, llmJsonQueryOutputTable.getSorItemName())
                .bind(6, llmJsonQueryOutputTable.getAnswer())
                .bind(7, llmJsonQueryOutputTable.getBoundingBox())
                .bind(8, llmJsonQueryOutputTable.getPaperNo())
                .bind(9, llmJsonQueryOutputTable.getOriginId())
                .bind(10, llmJsonQueryOutputTable.getGroupId())
                .bind(11, llmJsonQueryOutputTable.getTenantId())
                .bind(12, llmJsonQueryOutputTable.getRootPipelineId())
                .bind(13, llmJsonQueryOutputTable.getBatchId())
                .bind(14, llmJsonQueryOutputTable.getModelRegistry())
                .bind(15, llmJsonQueryOutputTable.getExtractedImageUnit())
                .bind(16, llmJsonQueryOutputTable.getImageDpi())
                .bind(17, llmJsonQueryOutputTable.getImageHeight())
                .bind(18, llmJsonQueryOutputTable.getImageWidth())
                .bind(19, llmJsonQueryOutputTable.getSorContainerId())
                .bind(20, llmJsonQueryOutputTable.getSorItemLabel())
                .bind(21,llmJsonQueryOutputTable.getSectionAlias())
                .execute();
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
                log.info("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
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

        }else{
            trimmedPredictedValue = input;
        }
        return trimmedPredictedValue;
    }

    private String buildInsertQueryKrypton() {
        return "INSERT INTO " + llmJsonParser.getOutputTable() +
                "(created_on, created_user_id, last_updated_on, last_updated_user_id, confidence, sor_item_name, answer, bbox, paper_no, \n" +
                "origin_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, \n" +
                "extracted_image_unit, image_dpi, image_height, image_width, sor_container_id, sor_item_label,section_alias) \n" +
                "VALUES (?::timestamp, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
    }


    @NotNull
    private String buildInsertQueryXenon() {
        return "INSERT INTO " + llmJsonParser.getOutputTable() +
                "(created_on,created_user_id, last_updated_on, last_updated_user_id,sor_container_name,sor_item_name, answer, paper_no, " +
                "origin_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry," +
                "extracted_image_unit, image_dpi, image_height, image_width, sor_container_id, sor_item_label,section_alias) "
                + " VALUES(?::timestamp,?,?,?,?,?,?,?,?,?,?,?,?,?  ,?,?,?,?,?,?,?)";
    }

    private String decryptData(String encryptOutputSorItem,String extractedContent,String policy,String sorItemName,InticsIntegrity encryption){
       String response = "";
        try{
            List<EncryptionRequest> decryptList = new ArrayList<EncryptionRequest>();
            if (Objects.equals(encryptOutputSorItem, "true")) {
                List<EncryptionRequest> list = new ArrayList<EncryptionRequest>();
                EncryptionRequest request = new EncryptionRequest(
                        policy, extractedContent, sorItemName);
                list.add(request);
                decryptList = encryption.decrypt(list);
                response=decryptList!=null?decryptList.get(0).getValue():"";
            }else{
                response = extractedContent;
            }
        }catch(Exception e){
            log.error("EXception in decryptData Method "+e);
        }
        return response;
    }
    private static List<EncryptionRequest> encryptData(List<LlmJsonParsedResponse> responses, InticsIntegrity inticsIntegrity, String encryptData, Map<String, LlmJsonQueryInputTableSorMeta> metaMap,String policy,ActionExecutionAudit action) {
        List<EncryptionRequest> list = new ArrayList<EncryptionRequest>();
        for(int i=0;i<responses.size();i++) {
            responses.get(i).setAnswer(trimTo255Characters(responses.get(i).getAnswer(), action));
            EncryptionRequest request = new EncryptionRequest(
                    policy, responses.get(i).getAnswer(), responses.get(i).getSorItemName());
            list.add(request);
        }
        list = inticsIntegrity.encrypt(list);
        return list;
    }

//    private static List<EncryptionRequest> encryptDataObj(LlmJsonParserKvpKrypton responses, InticsIntegrity inticsIntegrity, String encryptData, LlmJsonQueryInputTableSorMeta meta,String policy,ActionExecutionAudit action) {
//        List<EncryptionRequest> list = new ArrayList<EncryptionRequest>();
//            responses.setValue(trimTo255Characters(responses.getValue(), action));
//            EncryptionRequest request = new EncryptionRequest(
//                    policy, responses.getValue(), meta.getSorItemName());
//            list.add(request);
//        list = inticsIntegrity.decrypt(list);
//        return list;
//    }

    private List<EncryptionRequest> encryptDataMethod(List<LlmJsonParserKvpKrypton> responses, InticsIntegrity encryption, String encryptOutputSorItem, ActionExecutionAudit action,String policy, List<LlmJsonQueryInputTableSorMeta> metaList) {
        Map<String, LlmJsonQueryInputTableSorMeta> metaMap = new HashMap<>();
        for (LlmJsonQueryInputTableSorMeta meta : metaList) {
            metaMap.put(meta.getSorItemName(), meta);
        }
        List<EncryptionRequest> list = new ArrayList<EncryptionRequest>();
        for(int i=0;i<responses.size();i++) {
            LlmJsonQueryInputTableSorMeta meta = metaMap.get(responses.get(i).getKey());
            responses.get(i).setValue((trimTo255Characters(responses.get(i).getValue(), action)));
            EncryptionRequest request = new EncryptionRequest(
                    policy, responses.get(i).getValue(), meta.getSorItemName());
            list.add(request);
        }
                list = encryption.encrypt(list);

        return list;
    }
}
