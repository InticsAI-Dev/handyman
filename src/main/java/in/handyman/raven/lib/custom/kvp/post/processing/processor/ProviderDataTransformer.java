package in.handyman.raven.lib.custom.kvp.post.processing.processor;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.DatabaseUtility;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.kvp.post.processing.bsh.ProviderTransformerFinal;
import in.handyman.raven.lib.custom.kvp.post.processing.bsh.ProviderTransformerOutputItem;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonParserKvpKrypton;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;


public class ProviderDataTransformer {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final ActionExecutionAudit action;
    private final String jdbiResourceName;
    private final InticsIntegrity encryption;

    private static final String MEMBER_ID_KEY = "member_id";
    private static final String MEDICAID_ID_KEY = "medicaid_id";
    private static final String MULTIPLE_MEMBER_INDICATOR_KEY = "multiple_member_indicator";


    public ProviderDataTransformer(Logger log, Marker aMarker, ObjectMapper objectMapper,
                                   ActionExecutionAudit action, String jdbiResourceName, InticsIntegrity encryption) {
        this.log = log;
        this.aMarker = aMarker;
        this.objectMapper = objectMapper;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        this.encryption = encryption;
        log.info("ProviderDataTransformer initialized with encryption: {}", encryption != null);

    }

    public List<RadonQueryOutputTable> processProviderData(
            String sourceCode, String className, String responsePayload,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) {
        log.info("Starting processProviderData for class: {} with origin Id {} and paper no {} for container Id {}", className, entity.getOriginId(), entity.getPaperNo(), entity.getSorContainerId());

        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        try {
            Interpreter interpreter = new Interpreter();
            log.info("Beanshell script evaluated successfully.");

            interpreter.eval(sourceCode);

            Map<String, Object> responseMap = parseResponse(responsePayload, request, endpoint, entity, outputList);
            if (responseMap.isEmpty()) {
                log.warn("Parsed response is empty for payload: {}", responsePayload);
                return outputList;
            }

            responseMap.forEach((key, value) -> {
                try {
                    boolean isMemberData = className != null && className.contains("Member");

                    List<RadonQueryOutputTable> mappedData;
                    if (isMemberData) {
                        log.info("Processing as MEMBER data with class: {}", className);

                        // Check if input matches member_id, medicaid_id, multiple_member_indicator pattern
                        if (shouldPassthroughMemberData(value)) {
                            log.info("Detected member data passthrough pattern - bypassing transformation");
                            mappedData = processMemberDataDirectly(value, entity, request, apiResponse, endpoint);
                        } else {
                            mappedData = processMappingMember(
                                    interpreter, className, value, entity, request, apiResponse, endpoint);
                        }
                    } else {
                        log.info("Processing as PROVIDER data with class: {}", className);
                        mappedData = processMappingInterpreter(
                                interpreter, className, value, entity, request, apiResponse, endpoint);
                    }

                    outputList.addAll(mappedData);

                } catch (EvalError e) {
                    String errorMessage = "Error evaluating Beanshell script for origin id " + entity.getOriginId() +
                            " and paper no " + entity.getPaperNo() + " message : " + e.getMessage();
                    handleErrorOutputEntity(entity, errorMessage, request, responsePayload, endpoint, e, outputList);
                }
            });

        } catch (Exception e) {
            String errorMessage = "Error executing script for origin id " + entity.getOriginId() + " and paper no " + entity.getPaperNo() + "message : " + e.getMessage();
            handleErrorOutputEntity(entity, errorMessage, request, responsePayload, endpoint, e, outputList);
        }

        log.info("Total mapped entries: {}", outputList.size());
        return outputList;
    }

    /**
     * Check if the input data contains the specific member keys that should be passed through
     */
    private boolean shouldPassthroughMemberData(Object value) {
        if (!(value instanceof List)) {
            return false;
        }

        List<?> dataList = (List<?>) value;
        if (dataList.isEmpty()) {
            return false;
        }

        // Check if first 3 items have the expected keys
        Set<String> foundKeys = new HashSet<>();
        int itemsToCheck = Math.min(3, dataList.size());

        for (int i = 0; i < itemsToCheck; i++) {
            Object item = dataList.get(i);
            if (item instanceof Map) {
                Map<?, ?> itemMap = (Map<?, ?>) item;
                String key = (String) itemMap.get("key");
                if (key != null) {
                    foundKeys.add(key);
                }
            }
        }

        // Check if all three required keys are present
        boolean hasAllKeys = foundKeys.contains(MEMBER_ID_KEY) &&
                foundKeys.contains(MEDICAID_ID_KEY) &&
                foundKeys.contains(MULTIPLE_MEMBER_INDICATOR_KEY);

        if (hasAllKeys) {
            log.info("Found all required member keys: {}", foundKeys);
        }

        return hasAllKeys;
    }

    /**
     * Process member data directly without Beanshell transformation
     */
    private List<RadonQueryOutputTable> processMemberDataDirectly(
            Object value, RadonQueryInputTable entity,
            String request, String apiResponse, String endpoint) {

        log.info("Processing member data directly (passthrough mode)");
        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        if (!(value instanceof List)) {
            log.warn("Expected List but got: {}", value.getClass().getName());
            return outputList;
        }

        List<?> dataList = (List<?>) value;

        try {
            // Convert to LlmJsonParserKvpKrypton format
            List<LlmJsonParserKvpKrypton> kvpFields = new ArrayList<>();

            for (Object item : dataList) {
                if (item instanceof Map) {
                    Map<?, ?> itemMap = (Map<?, ?>) item;

                    String key = (String) itemMap.get("key");
                    Object valueObj = itemMap.get("value");
                    String label = (String) itemMap.get("label");
                    String sectionAlias = (String) itemMap.get("section_alias");
                    Object boundingBox = itemMap.get("boundingBox");
                    Object confidence = itemMap.get("confidence");

                    // Use default section if empty
                    if (sectionAlias == null || sectionAlias.isEmpty()) {
                        sectionAlias = "MEMBER_INFO";
                    }

                    // Convert confidence to double
                    double confidenceValue = 0.0;
                    if (confidence instanceof Number) {
                        confidenceValue = ((Number) confidence).doubleValue();
                    }

                    LlmJsonParserKvpKrypton kvpField = new LlmJsonParserKvpKrypton(
                            key,
                            valueObj != null ? String.valueOf(valueObj) : "",
                            label != null ? label : "",
                            sectionAlias,
                            confidenceValue,
                            objectMapper.convertValue(boundingBox, JsonNode.class)
                    );
                    kvpFields.add(kvpField);

                    log.info("Processed field directly: key={}, value={}", key, valueObj);
                }
            }

            // Get container ID
            Optional<String> containerIdOpt = getContainerId("MEMBER_DETAILS");
            if (containerIdOpt.isPresent()) {
                String containerId = containerIdOpt.get();
                String responseJson = objectMapper.writeValueAsString(kvpFields);
                outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, containerId, responseJson));
                log.info("Added {} member fields directly to output", kvpFields.size());
            } else {
                log.warn("Container ID not found for MEMBER_DETAILS");
                outputList.add(buildOutputTable(entity, request, apiResponse, endpoint,
                        String.valueOf(entity.getSorContainerId()), "[]"));
            }

        } catch (JsonProcessingException e) {
            String errorMessage = "Error processing member data directly for origin id " + entity.getOriginId() +
                    " and paper no " + entity.getPaperNo() + " message : " + e.getMessage();
            handleErrorOutputEntity(entity, errorMessage, request, apiResponse, endpoint, e, outputList);
        }

        return outputList;
    }

    void handleErrorOutputEntity(RadonQueryInputTable entity, String message, String request, String responsePayload, String endpoint, Exception e, List<RadonQueryOutputTable> outputList) {
        outputList.add(RadonQueryOutputTable.builder()
                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .actionId(action.getActionId())
                .tenantId(entity.getTenantId())
                .processId(entity.getTenantId())
                .rootPipelineId(entity.getRootPipelineId())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(entity.getApiName())
                .message(message)
                .batchId(entity.getBatchId())
                .category(entity.getCategory())
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(responsePayload))
                .endpoint(String.valueOf(endpoint))
                .sorContainerId(entity.getSorContainerId())
                .build());
        log.error(message);
        handleHandymanExceptionInsert(message, e);

    }

    private void handleHandymanExceptionInsert(String message, Exception e) {
        HandymanException handymanException = new HandymanException(e);
        HandymanException.insertException(message, handymanException, action);
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }


    private Map<String, Object> parseResponse(String responsePayload, String request, String endpoint, RadonQueryInputTable entity, List<RadonQueryOutputTable> outputList) {

        if (responsePayload == null) {
            log.warn("Response payload is null or empty.");
            return Map.of();
        }
        try {
            return objectMapper.readValue(responsePayload, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            String errorMessage = "Error parsing response JSON in bean shell script for origin id " + entity.getOriginId() + " and paper no " + entity.getPaperNo() + " message : " + e.getMessage();
            handleErrorOutputEntity(entity, errorMessage, request, responsePayload, endpoint, e, outputList);
        }
        return Map.of();
    }

    private List<RadonQueryOutputTable> processMappingInterpreter(
            Interpreter interpreter, String className, Object response,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws EvalError {

        interpreter.set("logger", log);
        String classInstantiation = className + " mapper = new " + className + "(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("responseMap", response);
        interpreter.eval("providerMap = mapper.processProviders(responseMap);");

        Object providerMapObject = interpreter.get("providerMap");

        return mapOutputTable(providerMapObject, entity, request, apiResponse, endpoint);
    }

    private List<RadonQueryOutputTable> processMappingMember(
            Interpreter interpreter, String className, Object response,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws EvalError {

        log.info("Processing member data with className: {}", className);
        interpreter.set("logger", log);
        String classInstantiation = className + " mapper = new " + className + "(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("inputData", response);
        interpreter.eval("memberMap = mapper.processPatientData(inputData);");

        Object memberMapObject = interpreter.get("memberMap");

        return mapOutputTableForMember(memberMapObject, entity, request, apiResponse, endpoint);
    }

    private List<RadonQueryOutputTable> processMappingJava(
            Interpreter interpreter, String className, List<Map<String, String>> response,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws EvalError {
        log.info("Processing provider data using Java class: {}", className);

        ProviderTransformerFinal processor = new ProviderTransformerFinal(log);
        List<ProviderTransformerOutputItem> results = processor.processProviders(response);

        return mapOutputTable(results, entity, request, apiResponse, endpoint);
    }

    private List<RadonQueryOutputTable> mapOutputTable(
            Object providerMapObject, RadonQueryInputTable entity,
            String request, String apiResponse, String endpoint) {

        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        if (providerMapObject instanceof List) {
            List<?> providerDataList = (List<?>) providerMapObject;
            Map<String, List<LlmJsonParserKvpKrypton>> kvpContainers = new HashMap<>();

            for (int i = 0; i < providerDataList.size(); i++) {

                Hashtable item = (Hashtable) providerDataList.get(i);
                String container = (String) item.get("sorContainerName");

                LlmJsonParserKvpKrypton llmJsonParserKvpKrypton = createKvp(item);

                if (kvpContainers.containsKey(container)) {
                    List<LlmJsonParserKvpKrypton> llmJsonParserKvpKryptonList = kvpContainers.get(container);
                    llmJsonParserKvpKryptonList.add(llmJsonParserKvpKrypton);
                    kvpContainers.put(container, llmJsonParserKvpKryptonList);
                } else {
                    List<LlmJsonParserKvpKrypton> llmJsonParserKvpKryptonList = new ArrayList<>();
                    llmJsonParserKvpKryptonList.add(llmJsonParserKvpKrypton);
                    kvpContainers.put(container, llmJsonParserKvpKryptonList);
                }

            }
            if (kvpContainers.isEmpty()) {
                outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, String.valueOf(entity.getSorContainerId()), "[]"));
            }

            kvpContainers.forEach((container, kvps) -> {
                Optional<String> containerIdOpt = getContainerId(container);
                containerIdOpt.ifPresent(containerId -> {
                    try {
                        String responseJson = objectMapper.writeValueAsString(kvps);

                        outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, containerId, responseJson));
                    } catch (JsonProcessingException e) {
                        String errorMessage = "Error parsing response JSON in bean shell script for origin id " + entity.getOriginId() + " and paper no " + entity.getPaperNo() + " message : " + e.getMessage();
                        handleErrorOutputEntity(entity, errorMessage, request, apiResponse, endpoint, e, outputList);

                    }
                });
            });
        }

        return outputList;
    }

    private List<RadonQueryOutputTable> mapOutputTableForMember(
            Object memberMapObject, RadonQueryInputTable entity,
            String request, String apiResponse, String endpoint) {

        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        if (memberMapObject instanceof Map) {
            Map<?, ?> memberMap = (Map<?, ?>) memberMapObject;
            Object memberDetailsObj = memberMap.get("MEMBER_DETAILS");

            if (memberDetailsObj instanceof List) {
                List<?> memberDetailsList = (List<?>) memberDetailsObj;

                log.info("Processing {} member detail sections", memberDetailsList.size());

                for (Object sectionObj : memberDetailsList) {
                    if (sectionObj instanceof Map) {
                        Map<?, ?> section = (Map<?, ?>) sectionObj;
                        String sectionAlias = (String) section.get("sectionAlias");
                        List<?> fields = (List<?>) section.get("fields");

                        log.info("Processing section: {} with {} fields", sectionAlias, fields != null ? fields.size() : 0);

                        if (fields != null && !fields.isEmpty()) {
                            Optional<String> containerIdOpt = getContainerId("MEMBER_DETAILS");
                            containerIdOpt.ifPresent(containerId -> {
                                try {
                                    // Convert fields to LlmJsonParserKvpKrypton format
                                    List<LlmJsonParserKvpKrypton> kvpFields = new ArrayList<>();
                                    for (Object fieldObj : fields) {
                                        if (fieldObj instanceof Map) {
                                            Map<?, ?> field = (Map<?, ?>) fieldObj;

                                            LlmJsonParserKvpKrypton kvpField = new LlmJsonParserKvpKrypton(
                                                    (String) field.get("key"),
                                                    (String) field.get("value"),
                                                    (String) field.get("label"),
                                                    sectionAlias,
                                                    0.0,
                                                    objectMapper.convertValue(field.get("boundingBox"), JsonNode.class)
                                            );
                                            kvpFields.add(kvpField);
                                        }
                                    }

                                    String responseJson = objectMapper.writeValueAsString(kvpFields);
                                    outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, containerId, responseJson));
                                    log.info("Added member section to output with {} fields", kvpFields.size());

                                } catch (JsonProcessingException e) {
                                    String errorMessage = "Error parsing member data for origin id " + entity.getOriginId() +
                                            " and paper no " + entity.getPaperNo() + " message : " + e.getMessage();
                                    handleErrorOutputEntity(entity, errorMessage, request, apiResponse, endpoint, e, outputList);
                                }
                            });
                        }
                    }
                }
            }
        }

        if (outputList.isEmpty()) {
            log.warn("No member details processed, adding empty output");
            outputList.add(buildOutputTable(entity, request, apiResponse, endpoint,
                    String.valueOf(entity.getSorContainerId()), "[]"));
        }

        return outputList;
    }

    private LlmJsonParserKvpKrypton createKvp(Hashtable<?, ?> data) {
        return new LlmJsonParserKvpKrypton(
                (String) data.get("key"),
                (String) data.get("value"),
                (String) data.get("label"),
                (String) data.get("sectionAlias"),
                (Double) data.get("confidence"),
                objectMapper.convertValue(data.get("boundingBox"), JsonNode.class)
        );
    }


    private Optional<String> getContainerId(String sorContainerName) {
        log.info("Fetching container ID for {}", sorContainerName);

        String query = "SELECT sor_container_id FROM sor_meta.sor_container " +
                "WHERE sor_container_name = :sorContainerName " +
                "AND document_type = :documentType " +
                "AND tenant_id = :tenantId " +
                "AND status='ACTIVE'";

        log.info(aMarker, "Fetching container ID for {}", sorContainerName);

        Map<String, Object> params = Map.of(
                "documentType", action.getContext().get("document_type"),
                "tenantId", Long.valueOf(action.getContext().get("tenant_id")),
                "sorContainerName", sorContainerName
        );

        return DatabaseUtility.fetchSingleResult(jdbiResourceName, query, params);
    }

    private String encryptIfRequired(String content) {

        if ("true".equals(action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION))) {
            return encryption.encrypt(content, "AES256", "RADON_KVP_JSON");
        }
        return content;
    }

    private String encryptReqResIfRequired(String content) {

        if ("true".equals(action.getContext().get(ENCRYPT_REQUEST_RESPONSE))) {
            return encryption.encrypt(content, "AES256", "RADON_KVP_JSON");
        }
        return content;
    }

    private RadonQueryOutputTable buildOutputTable(
            RadonQueryInputTable entity, String request, String apiResponse,
            String endpoint, String containerId, String encryptedContent) {

        return RadonQueryOutputTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(entity.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(entity.getTenantId())
                .originId(entity.getOriginId())
                .paperNo(entity.getPaperNo())
                .totalResponseJson(encryptIfRequired(encryptedContent))
                .groupId(entity.getGroupId())
                .inputFilePath(entity.getInputFilePath())
                .actionId(action.getActionId())
                .tenantId(entity.getTenantId())
                .processId(entity.getProcessId())
                .rootPipelineId(entity.getRootPipelineId())
                .modelRegistry(entity.getModelRegistry())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .message("Radon KVP mapping completed")
                .category(entity.getCategory())
                .request(encryptReqResIfRequired(request))
                .response(encryptReqResIfRequired(apiResponse))
                .endpoint(endpoint)
                .sorContainerId(Long.valueOf(containerId))
                .build();
    }
}