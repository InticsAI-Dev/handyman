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
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonParserKvpKrypton;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class MemberDataTransformer {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final ActionExecutionAudit action;
    private final String jdbiResourceName;
    private final InticsIntegrity encryption;


    public MemberDataTransformer(Logger log, Marker aMarker, ObjectMapper objectMapper,
                                 ActionExecutionAudit action, String jdbiResourceName, InticsIntegrity encryption) {
        this.log = log;
        this.aMarker = aMarker;
        this.objectMapper = objectMapper;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        this.encryption = encryption;
        log.info("Transformer initialized with encryption: {}", encryption != null);

    }

    public List<RadonQueryOutputTable> processMemberData(
            String sourceCode, String className, String responsePayload,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) {
        log.info("Starting processData for class: {} with origin Id {} and paper no {} for container Id {}", className, entity.getOriginId(), entity.getPaperNo(), entity.getSorContainerId());

        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        try {
            Interpreter interpreter = new Interpreter();
            log.info("Beanshell script evaluated successfully.");


            interpreter.eval(sourceCode);

            List<Map<String, Object>> responseList = parseResponse(responsePayload, entity, outputList);

            if (responseList.isEmpty()) {
                log.warn("Parsed response is empty for payload: {}", responsePayload);
                return outputList;
            }
            try {

                List<RadonQueryOutputTable> mappedInterpreterData = processMappingInterpreter(
                        interpreter, className, responseList, entity, request, apiResponse, endpoint);

                outputList.addAll(mappedInterpreterData);

            } catch (EvalError e) {
                String errorMessage = "Error evaluating Beanshell script for origin id " + entity.getOriginId() + " and paper no " + entity.getPaperNo() + "message : " + e.getMessage();
                handleErrorOutputEntity(entity, errorMessage, request, responsePayload, endpoint, e, outputList);
            }

        } catch (Exception e) {
            String errorMessage = "Error executing script for origin id " + entity.getOriginId() + " and paper no " + entity.getPaperNo() + "message : " + e.getMessage();
            handleErrorOutputEntity(entity, errorMessage, request, responsePayload, endpoint, e, outputList);
        }

        log.info("Total mapped entries: {}", outputList.size());
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


    private List<Map<String, Object>> parseResponse(String responsePayload, RadonQueryInputTable entity, List<RadonQueryOutputTable> outputList) {
        if (responsePayload == null || responsePayload.trim().isEmpty()) {
            log.warn("Response payload is null or empty.");
            return new ArrayList();
        }
        try {
            // Deserialize as a list of maps
            return objectMapper.readValue(responsePayload, new TypeReference<List<Map<String, Object>>>(){});
        } catch (Exception e) {
            String errorMessage = "Error parsing response JSON for origin id " + entity.getOriginId() +
                    " and paper no " + entity.getPaperNo() + ": " + e.getMessage();
            handleErrorOutputEntity(entity, errorMessage, "", responsePayload, "", e, outputList);
            return new ArrayList();
        }
    }


    private List<RadonQueryOutputTable> processMappingInterpreter(
            Interpreter interpreter, String className, Object response,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws EvalError {

        interpreter.set("logger", log);
        String classInstantiation = className + " mapper = new " + className + "(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("responseMap", response);
        interpreter.eval("memberNameMap = mapper.processMemberName(responseMap);");

        Object mapObject = interpreter.get("memberNameMap");

        return mapOutputTable(mapObject, entity, request, apiResponse, endpoint);
    }


    private List<RadonQueryOutputTable> mapOutputTable(
            Object mapObject,
            RadonQueryInputTable entity,
            String request,
            String apiResponse,
            String endpoint) {

        List<RadonQueryOutputTable> outputList = new ArrayList<RadonQueryOutputTable>();

        if (mapObject instanceof List) {
            List dataList = (List) mapObject;
            List<LlmJsonParserKvpKrypton> kvpList = new ArrayList<LlmJsonParserKvpKrypton>();

            for (Object obj : dataList) {
                try {
                    Map item = (Map) obj;

                    LlmJsonParserKvpKrypton kvp = new LlmJsonParserKvpKrypton(
                            (String) item.get("key"),
                            (String) item.get("value"),
                            (String) item.get("label"),
                            (String) item.get("section_alias"),
                            item.get("confidence") instanceof Number
                                    ? ((Number) item.get("confidence")).doubleValue() : 0.0,
                            objectMapper.convertValue(item.get("boundingBox"), JsonNode.class)
                    );

                    kvpList.add(kvp);
                } catch (Exception e) {
                    log.warn("Skipping invalid entry: {}", obj, e);
                }
            }

            if (kvpList.isEmpty()) {
                outputList.add(buildOutputTable(
                        entity, request, apiResponse, endpoint,
                        String.valueOf(entity.getSorContainerId()), "[]"
                ));
                return outputList;
            }

            try {
                // Serialize the kvp list into JSON and pass as apiResponse
                String responseJson = objectMapper.writeValueAsString(kvpList);

                outputList.add(buildOutputTable(
                        entity, request, responseJson, endpoint,
                        String.valueOf(entity.getSorContainerId()), responseJson
                ));

            } catch (JsonProcessingException e) {
                String errorMessage = "Error parsing response JSON for origin id "
                        + entity.getOriginId() + " and paper no " + entity.getPaperNo()
                        + " message: " + e.getMessage();

                handleErrorOutputEntity(entity, errorMessage, request, apiResponse, endpoint, e, outputList);
            }
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

