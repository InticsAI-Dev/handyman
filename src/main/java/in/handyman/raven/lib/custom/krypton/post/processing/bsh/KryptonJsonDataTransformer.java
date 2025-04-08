package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.LlmJsonParserKvpKrypton;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.utils.DatabaseUtility;
import javassist.NotFoundException;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.*;

import static in.handyman.raven.lib.utils.DatabaseUtility.fetchBshResultByClassName;


public class KryptonJsonDataTransformer {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final ActionExecutionAudit action;
    private final Jdbi jdbi;
    private final InticsIntegrity encryption;


    public KryptonJsonDataTransformer(Logger log, Marker aMarker, ObjectMapper objectMapper,
                                      ActionExecutionAudit action, Jdbi jdbi, InticsIntegrity encryption) {
        this.log = log;
        this.aMarker = aMarker;
        this.objectMapper = objectMapper;
        this.action = action;
        this.jdbi = jdbi;
        this.encryption = encryption;
        log.info("KryptonDataTransformer initialized with encryption: {}", encryption != null);

    }

    public List<RadonQueryOutputTable> processKryptonJsonData(
            String instanceVariableName, String responseJson,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws JsonProcessingException {
        log.info("Starting processKrypton bsh for class: {}", instanceVariableName);
        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        Map<String,Object> innerParsedResponsesKrypton = objectMapper.readValue(
                responseJson,
                new TypeReference<>() {
                }
        );

        String className = action.getContext().get(instanceVariableName);
        Optional<String> sourceCode = fetchBshResultByClassName(jdbi, className, entity.getTenantId());
        if(sourceCode.isPresent()){
            log.info("Starting processKrypton bsh code for class: {}", className);



            try {
                Interpreter interpreter = new Interpreter();
                log.info("Beanshell script evaluated successfully.");

                interpreter.eval(sourceCode.get());

                    try {
                        List<RadonQueryOutputTable> mappedInterpreterData = processMappingInterpreter(
                                interpreter, className, innerParsedResponsesKrypton, entity, request, apiResponse, endpoint);

                        outputList.addAll(mappedInterpreterData);
                    } catch (EvalError e) {
                        throw new HandymanException("Error evaluating Beanshell script", e, action);
                    }


            } catch (Exception e) {
                throw new HandymanException("Error executing script ", e, action);
            }

            log.info("Total mapped entries: {}", outputList.size());

        }else {
            throw new HandymanException("Class not found in the bsh config table, check the instance config reference class name : "+instanceVariableName, new NotFoundException("Bsh config not found with the class name : "+className),action);
        }
        return outputList;
    }

    private Map<String, Object> parseResponse(String responsePayload) {

        if (responsePayload == null) {
            log.warn("Response payload is null or empty.");

            return Map.of();
        }
        try {
            return objectMapper.readValue(responsePayload, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new HandymanException("Error parsing response JSON", e, action);
        }
    }

    private List<RadonQueryOutputTable> processMappingInterpreter(
            Interpreter interpreter, String className, Map<String,Object> responseJson,
            RadonQueryInputTable entity, String request, String apiResponse, String endpoint) throws EvalError {

        interpreter.set("logger", log);
        String classInstantiation = className + " mapper = new " + className + "(logger);";
        interpreter.eval(classInstantiation);
        interpreter.set("responseMap", responseJson);
        interpreter.eval("kryptonJsonMap = mapper.processKryptonJson(responseMap);");

        Object kryptonMapObject = interpreter.get("kryptonJsonMap");

        return mapOutputTable(kryptonMapObject, entity, request, apiResponse, endpoint);
    }

    private List<RadonQueryOutputTable> mapOutputTable(
            Object kryptonMapObject, RadonQueryInputTable entity,
            String request, String apiResponse, String endpoint) {

        List<RadonQueryOutputTable> outputList = new ArrayList<>();

        if (kryptonMapObject instanceof List) {
            List<?> kryptonDataList = (List<?>) kryptonMapObject;
            Map<String, List<LlmJsonParserKvpKrypton>> kvpContainers = new HashMap<>();

            for (int i = 0; i < kryptonDataList.size(); i++) {

                Hashtable item = (Hashtable) kryptonDataList.get(i);
                String container = (String) item.get("sorContainerName");

                LlmJsonParserKvpKrypton llmJsonParserKvpKrypton = createKvpFromHashtable(item);

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

            kvpContainers.forEach((container, kvps) -> {
                Optional<String> containerIdOpt = getContainerId(container);
                containerIdOpt.ifPresent(containerId -> {
                    try {
                        String responseJson = objectMapper.writeValueAsString(kvps);
                        String encryptedContent = encryptIfRequired(responseJson);

                        outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, containerId, encryptedContent));
                    } catch (JsonProcessingException e) {
                        throw new HandymanException("Error processing JSON", e, action);
                    }
                });
            });
        } else if (kryptonMapObject instanceof Map) {

            Map<String,List<Object>> kryptonDataList = (Map<String,List<Object>>) kryptonMapObject;
            Map<String, List<LlmJsonParserKvpKrypton>> kvpContainers = new HashMap<>();

            kryptonDataList.forEach((containerName, kryptonMap) -> {
                for (int i = 0; i < kryptonMap.size(); i++) {

                    Object item = (Object) kryptonMap.get(i);
                    LlmJsonParserKvpKrypton llmJsonParserKvpKrypton = createKvpFromObject(item);

                    if (kvpContainers.containsKey(containerName)) {
                        List<LlmJsonParserKvpKrypton> llmJsonParserKvpKryptonList = kvpContainers.get(containerName);
                        llmJsonParserKvpKryptonList.add(llmJsonParserKvpKrypton);
                        kvpContainers.put(containerName, llmJsonParserKvpKryptonList);
                    } else {
                        List<LlmJsonParserKvpKrypton> llmJsonParserKvpKryptonList = new ArrayList<>();
                        llmJsonParserKvpKryptonList.add(llmJsonParserKvpKrypton);
                        kvpContainers.put(containerName, llmJsonParserKvpKryptonList);
                    }
                }
                kvpContainers.forEach((container, kvps) -> {
                    Optional<String> containerIdOpt = getContainerId(container);
                    containerIdOpt.ifPresent(containerId -> {
                        try {
                            String responseJson = objectMapper.writeValueAsString(kvps);
                            String encryptedContent = encryptIfRequired(responseJson);

                            outputList.add(buildOutputTable(entity, request, apiResponse, endpoint, containerId, encryptedContent));
                        } catch (JsonProcessingException e) {
                            throw new HandymanException("Error processing JSON", e, action);
                        }
                    });
                });

            });



        }

        return outputList;
    }

    private LlmJsonParserKvpKrypton createKvpFromHashtable(Hashtable<?, ?> data) {
        return new LlmJsonParserKvpKrypton(
                (String) data.get("key"),
                (String) data.get("value"),
                (Double) data.get("confidence"),
                objectMapper.convertValue(data.get("boundingBox"), JsonNode.class)
        );
    }


    private LlmJsonParserKvpKrypton createKvpFromObject(Object data) {
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;

            return new LlmJsonParserKvpKrypton(
                    (String) map.get("key"),
                    (String) map.get("value"),
                    map.get("confidence") != null ? Double.valueOf(map.get("confidence").toString()) : null,
                    objectMapper.convertValue(map.get("boundingBox"), JsonNode.class)
            );
        } else {
            throw new IllegalArgumentException("Expected a Map-like object, but got: " + data.getClass());
        }
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

        return DatabaseUtility.fetchSingleResult(jdbi, query, params);
    }

    private String encryptIfRequired(String content) {

        if ("true".equals(action.getContext().get("pipeline.end.to.end.encryption"))) {
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
                .totalResponseJson(encryptedContent)
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
                .request(encryptIfRequired(request))
                .response(encryptIfRequired(apiResponse))
                .endpoint(endpoint)
                .sorContainerId(Long.valueOf(containerId))
                .build();
    }
}

