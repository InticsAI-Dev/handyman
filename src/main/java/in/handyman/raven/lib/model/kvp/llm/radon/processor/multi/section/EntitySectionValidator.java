package in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.jdbi.v3.core.Jdbi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class EntitySectionValidator {

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final Marker aMarker;
    private final InticsIntegrity securityEngine;
    private static final String ENCRYPTION_POLICY = "AES256";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern JSON_MARKER_PATTERN = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
    private static final String CONTAINER_CONSOLIDATION_FLAG_BY_PAGE = "CONSOLIDATED_BY_PAGE";
    private static final String CONTAINER_CONSOLIDATION_FLAG_BY_DOCUMENT = "CONSOLIDATED_BY_DOCUMENT";


    private final Jdbi jdbi;

    public EntitySectionValidator(ActionExecutionAudit action, Logger log, Marker aMarker, InticsIntegrity securityEngine, Jdbi jdbi) {
        this.log = log;
        this.aMarker = aMarker;
        this.securityEngine = securityEngine;
        this.actionExecutionAudit = action;
        this.jdbi = jdbi;
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = actionExecutionAudit.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        if ("true".equals(encryptReqRes) && request != null) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, log).encrypt(request, ENCRYPTION_POLICY, "KVP_COPRO_REQUEST");
            if (log.isInfoEnabled()) {
                log.info(aMarker, "Request/Response encrypted before persistence (len={})", request.length());
            }
            return encryptedRequest;
        } else {
            return request;
        }
    }

    public List<RadonMultiSectionOutputTable> doValidation(List<RadonMultiSectionResponseTable> parentObj, List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables) {

        for (RadonMultiSectionResponseTable entity : parentObj) {
            if ("FAILED".equalsIgnoreCase(entity.getStatus())) {
                Long tenantId = entity.getTenantId();
                Integer paperNo = entity.getPaperNo();
                Long groupId = entity.getGroupId();
                Long processId = entity.getProcessId();
                Long rootPipelineId = entity.getRootPipelineId();
                String processedFilePaths = entity.getInputFilePath();
                Long actionId = actionExecutionAudit.getActionId();
                String process = entity.getProcess();
                String batchId = entity.getBatchId();
                doFailedFilesResponseBuilder(radonMultiSectionOutputTables, entity, paperNo, groupId, processedFilePaths, tenantId, processId, rootPipelineId, actionId, process, batchId);
            }
        }

        Map<String, List<RadonMultiSectionResponseTable>> parentObjByOrigin = parentObj.stream().filter(radonMultiSectionResponseTable -> radonMultiSectionResponseTable.getStatus().equalsIgnoreCase("COMPLETED"))
                .collect(Collectors.groupingBy(RadonMultiSectionResponseTable::getOriginId, LinkedHashMap::new, Collectors.toList()));

        parentObjByOrigin.forEach((originId, radonMultiSectionResponseTables) -> {
            Map<Long, List<RadonMultiSectionResponseTable>> radonMultiSectionResponseTablesByContainer = radonMultiSectionResponseTables.stream()
                    .collect(Collectors.groupingBy(RadonMultiSectionResponseTable::getSorContainerId, LinkedHashMap::new, Collectors.toList()));

            radonMultiSectionResponseTablesByContainer.forEach((sorContainerId, entityList) -> {
                boolean isConsolidateByDocument = getConsolidatedPresentByContainer(sorContainerId, CONTAINER_CONSOLIDATION_FLAG_BY_DOCUMENT);
                if (isConsolidateByDocument) {
                    String consolidatedPostProcessingScript = getConsolidatedPostProcessingScript(sorContainerId, CONTAINER_CONSOLIDATION_FLAG_BY_DOCUMENT);
                    doConsolidationByDocumentWithContainerId(consolidatedPostProcessingScript, sorContainerId, entityList, radonMultiSectionOutputTables);

                } else {
                    entityList.forEach(entity -> {
                        Long tenantId = entity.getTenantId();
                        Integer paperNo = entity.getPaperNo();
                        Long groupId = entity.getGroupId();
                        Long processId = entity.getProcessId();
                        Long rootPipelineId = entity.getRootPipelineId();
                        String processedFilePaths = entity.getInputFilePath();
                        String status = entity.getStatus();

                        if ("COMPLETED".equalsIgnoreCase(status)) {
                            doCompletedFilesResponseBuilder(radonMultiSectionOutputTables, entity, tenantId, originId, paperNo, groupId, processedFilePaths, processId, rootPipelineId);
                        } else {
                            log.debug(aMarker, "Skipping entity with unexpected status {} for origin {} paper {}", status, maskForLog(originId), paperNo);
                        }
                    });
                }
            });
        });

        return radonMultiSectionOutputTables;
    }


    private void doConsolidationByDocumentWithContainerId(String consolidatedPostProcessingScript, Long sorContainerId, List<RadonMultiSectionResponseTable> entityList, List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables) {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> finalConsolidatedList = new ArrayList<>();
        entityList.forEach(radonMultiSectionResponseTable -> {
            Integer paperNo = radonMultiSectionResponseTable.getPaperNo();
            String postprocessingScript = radonMultiSectionResponseTable.getPostprocessingScript();
            String totalResponseJson = radonMultiSectionResponseTable.getTotalResponseJson();

            try {
                if (postprocessingScript != null && !postprocessingScript.isEmpty()) {
                    String postProcessedJson = getParserPostProcessingResponse(postprocessingScript, totalResponseJson, paperNo);

                    List<Map<String, Object>> postProcessedList = parsePostProcessedJson(postProcessedJson);
                    finalConsolidatedList.addAll(postProcessedList);
                }
                else {
                    List<Map<String, Object>> totalInputMap = parseJsonArray(totalResponseJson);
                    Map<String, Object> formattedTotal = formatSectionAliasResponse(paperNo, totalInputMap);
                    finalConsolidatedList.add(formattedTotal);
                }

            } catch (Exception e) {
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error in parsing post-processed JSON for consolidation by document", handymanException, actionExecutionAudit);
            }
        });

        if (consolidatedPostProcessingScript != null && !consolidatedPostProcessingScript.isEmpty()){
            ArrayNode validationInput = mapper.valueToTree(finalConsolidatedList);
            Map<String, Integer> sectionAliasInput = getSectionAliasInput(sorContainerId);
            Map<String, List<String>> blackListedKeywords = getBlackListedKeywords(sorContainerId, actionExecutionAudit.getContext().get("root-pipeline-name"));
            String consolidatedJson = getValidationPostProcessingResponse(consolidatedPostProcessingScript, validationInput, sectionAliasInput, blackListedKeywords);
            try {
                JsonNode consolidatedRoot = mapper.readTree(consolidatedJson);
                if (consolidatedRoot.isObject() && !consolidatedRoot.isEmpty()) {
                    JsonNode responseNode = consolidatedRoot.path("detailedInfo").path("response");
                    JsonNode paperNo = consolidatedRoot.path("detailedInfo").path("page_no");
                    String responseJson = responseNode.toString();
                    int pageNo = paperNo.isInt() ? paperNo.intValue() : Integer.parseInt(paperNo.asText());


                    Optional<RadonMultiSectionResponseTable> optionalEntity = entityList.stream()
                            .filter(radonMultiSectionResponseTable -> radonMultiSectionResponseTable.getPaperNo().equals(pageNo))
                            .findFirst();

                    optionalEntity.ifPresent(entity -> {
                        Long tenantId = entity.getTenantId();
                        Long groupId = entity.getGroupId();
                        String originId = entity.getOriginId();
                        String processedFilePaths = entity.getInputFilePath();
                        Long processId = entity.getProcessId();
                        Long rootPipelineId = entity.getRootPipelineId();

                        radonMultiSectionOutputTables.add(RadonMultiSectionOutputTable.builder()
                                .createdOn(entity.getCreatedOn())
                                .createdUserId(tenantId)
                                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                                .lastUpdatedUserId(tenantId)
                                .originId(originId)
                                .paperNo(pageNo)
                                .totalResponseJson(encryptRequestResponse(responseJson))
                                .groupId(groupId)
                                .inputFilePath(processedFilePaths)
                                .actionId(actionExecutionAudit.getActionId())
                                .tenantId(tenantId)
                                .processId(processId)
                                .rootPipelineId(rootPipelineId)
                                .process(entity.getProcess())
                                .batchId(entity.getBatchId())
                                .modelRegistry(entity.getModelRegistry())
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .stage(entity.getMessage())
                                .category(entity.getCategory())
                                .message("Radon kvp action macro completed")
                                .request(encryptRequestResponse(entity.getRequest()))
                                .response(encryptRequestResponse(entity.getResponse()))
                                .sorContainerId(sorContainerId)
                                .endpoint(String.valueOf(entity.getEndpoint()))
                                .build());
                    });
                } else {
                    log.info("Consolidated JSON is an empty object");
                }
            } catch (JsonProcessingException e) {
                log.error(aMarker, "Error parsing consolidated JSON", e);
                HandymanException.insertException("Error parsing consolidated JSON", new HandymanException(e), actionExecutionAudit);
            }
        }
    }

    public static List<Map<String, Object>> parsePostProcessedJson(String postProcessedJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> parsedList = mapper.readValue(postProcessedJson, new TypeReference<>() {
        });

        for (Map<String, Object> obj : parsedList) {
            String responseString = (String) obj.get("response");
            List<Map<String, Object>> responseList = mapper.readValue(responseString, new TypeReference<>() {
            });
            obj.put("response", responseList);
        }
        return parsedList;
    }

    public static List<Map<String, Object>> parseJsonArray(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<>() {
        });
    }

    public static Map<String, Object> formatSectionAliasResponse(int pageNo, List<Map<String, Object>> inputMap) {
        String sectionAlias = "";

        for (Map<String, Object> entry : inputMap) {
            Object alias = entry.get("section_alias");
            if (alias != null && !alias.toString().trim().isEmpty()) {
                sectionAlias = alias.toString();
                break;
            }
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("id", 1);
        responseMap.put("page_no", pageNo);
        responseMap.put("section_alias", sectionAlias);
        responseMap.put("response", inputMap);

        return responseMap;
    }

    private void doFailedFilesResponseBuilder(List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables, RadonMultiSectionResponseTable entity, Integer paperNo, Long groupId, String processedFilePaths, Long tenantId, Long processId, Long rootPipelineId, Long actionId, String process, String batchId) {
        radonMultiSectionOutputTables.add(RadonMultiSectionOutputTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(paperNo)
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .actionId(actionId)
                .process(process)
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(entity.getStage())
                .message(encryptRequestResponse(entity.getMessage()))
                .batchId(batchId)
                .createdOn(entity.getCreatedOn())
                .createdUserId(entity.getCreatedUserId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getLastUpdatedUserId())
                .category(entity.getCategory())
                .build());
    }

    private void doCompletedFilesResponseBuilder(
            List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables,
            RadonMultiSectionResponseTable entity,
            Long tenantId,
            String originId,
            Integer paperNo,
            Long groupId,
            String processedFilePaths,
            Long processId,
            Long rootPipelineId
    ) {
        String totalResponseJson = entity.getTotalResponseJson();
        String formattedJsonString = formattedJsonString(totalResponseJson);
        String postProcessingScript = entity.getPostprocessingScript();

        Long sorContainerId = entity.getSorContainerId();

        if (isNullOrEmpty(postProcessingScript)) {
            addCompletedOutput(radonMultiSectionOutputTables, entity, tenantId, originId, paperNo, groupId, processedFilePaths, processId, rootPipelineId, totalResponseJson);
            return;
        }

        String postProcessedJson = getParserPostProcessingResponse(postProcessingScript, formattedJsonString, entity.getPaperNo());

        if (postProcessedJson == null) {
            log.info(aMarker, "Post-processing produced null result for rootPipelineId={}, origin={}", entity.getRootPipelineId(), maskForLog(originId));
            addCompletedOutput(radonMultiSectionOutputTables, entity, tenantId, originId, paperNo, groupId, processedFilePaths, processId, rootPipelineId, totalResponseJson);
            return;
        }

        try {
            totalResponseJson = parseAndPossiblyConsolidateResponse(entity, postProcessedJson, sorContainerId);
        } catch (JsonProcessingException e) {
            log.error(aMarker, "Error parsing post-processed JSON for rootPipelineId={}", entity.getRootPipelineId(), e);
            HandymanException.insertException("Error parsing post-processed JSON", new HandymanException(e), actionExecutionAudit);
        }

        addCompletedOutput(radonMultiSectionOutputTables, entity, tenantId, originId, paperNo, groupId, processedFilePaths, processId, rootPipelineId, totalResponseJson);
    }


    private String parseAndPossiblyConsolidateResponse(RadonMultiSectionResponseTable entity, String postProcessedJson, Long sorContainerId) throws JsonProcessingException {
        JsonNode rootNode = MAPPER.readTree(postProcessedJson);
        if (!rootNode.isArray()) {
            return postProcessedJson;
        }

        ArrayNode arrayNode = (ArrayNode) rootNode;
        boolean isConsolidatedPresent = getConsolidatedPresentByContainer(sorContainerId, CONTAINER_CONSOLIDATION_FLAG_BY_PAGE);

        if (isConsolidatedPresent) {
            return handleConsolidatedResponse(sorContainerId, arrayNode);
        } else {
            return extractResponseFromArray(arrayNode);
        }
    }

    private String handleConsolidatedResponse(Long sorContainerId, ArrayNode arrayNode) throws JsonProcessingException {
        String consolidatedScript = getConsolidatedPostProcessingScript(sorContainerId, CONTAINER_CONSOLIDATION_FLAG_BY_PAGE);
        if (isNullOrEmpty(consolidatedScript)) {
            return "{}";
        }

        Map<String, Integer> sectionAliasInput = getSectionAliasInput(sorContainerId);
        Map<String, List<String>> blackListedKeywords = getBlackListedKeywords(sorContainerId, actionExecutionAudit.getContext().get("root-pipeline-name"));

        String consolidatedJson = getValidationPostProcessingResponse(consolidatedScript, arrayNode, sectionAliasInput, blackListedKeywords);

        if (isNullOrEmpty(consolidatedJson) || "{}".equals(consolidatedJson.trim())) {
            return "{}";
        }

        JsonNode consolidatedRoot = MAPPER.readTree(consolidatedJson);
        if (!consolidatedRoot.isObject() || consolidatedRoot.isEmpty()) {
            log.info("Consolidated JSON is an empty object");
            return "{}";
        }

        JsonNode responseNode = consolidatedRoot.path("detailedInfo").path("response");
        return extractResponseNodeAsString(responseNode);
    }

    private String extractResponseFromArray(ArrayNode arrayNode) {
        JsonNode responseNode = arrayNode.get(0).get("response");
        return extractResponseNodeAsString(responseNode);
    }

    private String extractResponseNodeAsString(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            log.warn("'response' field not found");
            return "{}";
        }

        if (node.isTextual()) return node.asText();
        return node.toString(); // handles array and object cases
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    private void addCompletedOutput(List<RadonMultiSectionOutputTable> radonMultiSectionOutputTables,
                                    RadonMultiSectionResponseTable entity,
                                    Long tenantId,
                                    String originId,
                                    Integer paperNo,
                                    Long groupId,
                                    String processedFilePaths,
                                    Long processId,
                                    Long rootPipelineId,
                                    String totalResponseJson) {

        radonMultiSectionOutputTables.add(RadonMultiSectionOutputTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(encryptRequestResponse(totalResponseJson))
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(actionExecutionAudit.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .batchId(entity.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getMessage())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(encryptRequestResponse(entity.getRequest()))
                .response(encryptRequestResponse(entity.getResponse()))
                .sorContainerId(entity.getSorContainerId())
                .endpoint(String.valueOf(entity.getEndpoint()))
                .build());
    }


    private String getParserPostProcessingResponse(String sourceCode, String sourceJson, Integer paperNo) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully for parsing");

            interpreter.set("logger", log);

            String className = "MultiSectionTransformer";

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {} for parsing", classInstantiation);

            interpreter.set("inputMap", sourceJson);
            interpreter.set("pageNo", paperNo);

            interpreter.eval("resultMap = mapper.multiSectionTransformer(inputMap, pageNo);");

            Object result = interpreter.get("resultMap");
            if (result instanceof String) {
                return (String) result;
            } else {
                log.error(aMarker, "Post-processor returned non-string result for parsing");
                return "{}";
            }

        } catch (EvalError e) {
            log.error(aMarker, "BeanShell evaluation error: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error", handymanException, actionExecutionAudit);
        } catch (Exception e) {
            log.error(aMarker, "Error executing post-processing script", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script", handymanException, actionExecutionAudit);
        }
        return null;
    }

    private String getValidationPostProcessingResponse(String sourceCode, ArrayNode validationInput, Map<String, Integer> sectionAliasInput, Map<String, List<String>> blackListedKeywords) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully for validation");

            interpreter.set("logger", log);
            String className = "SectionVotingProcessor";

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {} for validation", classInstantiation);

            JSONArray validationInputJson = new JSONArray(validationInput.toString());
            JSONObject sectionAliasJson = new JSONObject(sectionAliasInput);
            JSONObject blacklistedJson = new JSONObject(blackListedKeywords);

            interpreter.set("validationInput", validationInputJson);
            interpreter.set("sectionAliasInput", sectionAliasJson);
            interpreter.set("blacklistedKeywords", blacklistedJson);

            interpreter.eval("resultMap = mapper.process(validationInput, sectionAliasInput, blacklistedKeywords);");

            Object result = interpreter.get("resultMap");
            if (result instanceof String) {
                return (String) result;
            } else {
                log.error(aMarker, "Post-processor returned non-string result for validation");
                return "{}";
            }

        } catch (EvalError e) {
            log.error(aMarker, "BeanShell evaluation error: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error", handymanException, actionExecutionAudit);
        } catch (Exception e) {
            log.error(aMarker, "Error executing post-processing script", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script", handymanException, actionExecutionAudit);
        }
        return null;
    }

    private boolean getConsolidatedPresentByContainer(Long sorContainerId, String entity) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT 1 FROM " +
                                "sor_meta.multi_entity_processing " +
                                "WHERE sor_container_id = :sorContainerId AND entity = :entity and status = true " +
                                "LIMIT 1")
                        .bind("sorContainerId", sorContainerId)
                        .bind("entity", entity)
                        .mapTo(Integer.class)
                        .findFirst()
                        .isPresent()
        );
    }

    private String getConsolidatedPostProcessingScript(Long sorContainerId, String entity) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT postprocessing_script " +
                                "FROM sor_meta.multi_entity_processing WHERE " +
                                "sor_container_id = :sorContainerId AND entity = :entity and status = true LIMIT 1")
                        .bind("sorContainerId", sorContainerId)
                        .bind("entity", entity)
                        .mapTo(String.class)
                        .findFirst()
                        .orElse(null)
        );
    }

    private Map<String, Integer> getSectionAliasInput(Long sorContainerId) {
        Map<String, Integer> sectionAliasMap = jdbi.withHandle(handle ->
                handle.createQuery("SELECT truth_entity, priority_level FROM sor_meta.truth_entity_priority WHERE sor_container_id = :sorContainerId")
                        .bind("sorContainerId", sorContainerId)
                        .map((rs, ctx) -> Map.entry(
                                rs.getString("truth_entity"),
                                rs.getInt("priority_level")
                        ))
                        .list()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        log.info(aMarker, "Section alias map for container {}: {}", sorContainerId, sectionAliasMap);
        return sectionAliasMap;
    }

    private Map<String, List<String>> getBlackListedKeywords(Long sorContainerId, String instanceName) {

        Map<String, List<String>> blackListedKeywordsMap = jdbi.withHandle(handle ->
                handle.createQuery(
                                "SELECT sor_item_name, black_list_keyword " +
                                        "FROM sor_meta.sor_item_label_config " +
                                        "WHERE instance = :instanceName " +
                                        "AND sor_container_id = :sorContainerId"
                        )
                        .bind("instanceName", instanceName)
                        .bind("sorContainerId", sorContainerId)
                        .map((rs, ctx) -> Map.entry(
                                rs.getString("sor_item_name"),
                                rs.getString("black_list_keyword")
                        ))
                        .collect(Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                        ))
        );
        log.info(aMarker, "Blacklisted keywords map for container {}: {}", sorContainerId, blackListedKeywordsMap);
        return blackListedKeywordsMap;
    }


    private Map<String, List<String>> getAllowedKeywords() {
        //TODO implement allowed keywords fetch
        return Map.of();
    }

    public String formattedJsonString(String jsonResponse) {
        try {
            if (jsonResponse == null) {
                return null;
            }

            if (jsonResponse.contains("```json")) {
                log.debug(aMarker, "Input contains ```json``` markers; extracting JSON block");
                Matcher matcher = JSON_MARKER_PATTERN.matcher(jsonResponse);
                if (matcher.find()) {
                    String jsonString = matcher.group(1).replace("\n", "");
                    String repaired = repairJson(jsonString);
                    return repaired.isEmpty() ? null : repaired;
                } else {
                    return repairJson(jsonResponse);
                }
            } else if (jsonResponse.contains("{") || jsonResponse.contains("[")) {
                log.debug(aMarker, "Input seems like JSON, returning as-is (no markers)");
                return jsonResponse;
            } else {
                log.debug(aMarker, "Input not JSON-like or missing markers");
                return null;
            }
        } catch (Exception e) {
            HandymanException exception = new HandymanException(e);
            HandymanException.insertException("Error in formattedJsonString for LLM JSON parser action", exception, actionExecutionAudit);
            return null;
        }
    }

    private String repairJson(String jsonString) {
        jsonString = addMissingQuotes(jsonString);
        jsonString = balanceBracesAndBrackets(jsonString);
        jsonString = assignEmptyValues(jsonString);
        return jsonString;
    }

    private String addMissingQuotes(String jsonString) {
        jsonString = jsonString.replaceAll("(\\{|,\\s*)(\\w+)(?=\\s*:)", "$1\"$2\"");
        jsonString = jsonString.replaceAll("(?<=:)\\s*([^\"\\s,\\n}\\]]+)(?=\\s*(,|}|\\n|\\]))", "\"$1\"");
        return jsonString;
    }

    private String balanceBracesAndBrackets(String jsonString) {
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
        StringBuilder builder = new StringBuilder(jsonString);
        while (openBraces > closeBraces) {
            builder.append('}');
            closeBraces++;
        }
        while (openBrackets > closeBrackets) {
            builder.append(']');
            closeBrackets++;
        }
        return builder.toString();
    }

    private String assignEmptyValues(String jsonString) {
        jsonString = jsonString.replaceAll("(?<=:)\\s*(?=,|\\s*}|\\s*\\])", "\"\"");
        return jsonString;
    }

    private String maskForLog(String s) {
        if (s == null) return "null";
        int len = s.length();
        String prefix = s.length() <= 6 ? s : s.substring(0, 6);
        return String.format("%s... (len=%d)", prefix, len);
    }
}