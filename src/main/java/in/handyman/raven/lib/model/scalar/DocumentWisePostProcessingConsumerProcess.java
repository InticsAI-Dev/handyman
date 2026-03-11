package in.handyman.raven.lib.model.scalar;

import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentWisePostProcessingConsumerProcess implements CoproProcessor.ConsumerProcess<DocumentWisePostProcessingOriginInput, DocumentWisePostProcessingOriginOutput> {

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final Map<String, List<String>> scriptClassesCache;
    private final Map<String, List<DocumentWisePostProcessingInput>> resultsMap;
    private final Long defaultCreatedUserId;

    public DocumentWisePostProcessingConsumerProcess(ActionExecutionAudit actionExecutionAudit, Logger log) {
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.scriptClassesCache = new HashMap<>();
        this.resultsMap = new ConcurrentHashMap<>();
        
        // Get default created_user_id from context
        String createdUserIdStr = actionExecutionAudit.getContext().get("created_user_id");
        Long createdUserIdLong = null;
        if (createdUserIdStr != null && !createdUserIdStr.isEmpty()) {
            try {
                createdUserIdLong = Long.parseLong(createdUserIdStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid created_user_id format: {}, using null", createdUserIdStr);
            }
        }
        this.defaultCreatedUserId = createdUserIdLong;
    }

    public Map<String, List<DocumentWisePostProcessingInput>> getResultsMap() {
        return resultsMap;
    }

    @Override
    public List<DocumentWisePostProcessingOriginOutput> process(URL endpoint, DocumentWisePostProcessingOriginInput entity) throws Exception {
        String originId = entity.getOriginId();
        List<DocumentWisePostProcessingInput> originInputs = entity.getInputs();
        
        log.info("Processing origin {} with {} records", originId, originInputs != null ? originInputs.size() : 0);
        long start = System.currentTimeMillis();

        try {
            if (originInputs == null || originInputs.isEmpty()) {
                log.warn("Origin {} has no inputs to process", originId);
                resultsMap.put(originId, new ArrayList<>());
                return new ArrayList<>();
            }

            List<String> scriptClasses = loadScriptOrder();
            log.info("Loaded {} script classes for origin {}", scriptClasses.size(), originId);
            
            List<DocumentWisePostProcessingInput> resultInputs = executeScripts(scriptClasses, originInputs);

            long duration = System.currentTimeMillis() - start;
            log.info("Completed processing origin {} ({} ms). Processed {} records", originId, duration, resultInputs.size());

            // Store results in shared map
            resultsMap.put(originId, resultInputs);
            log.info("Stored {} processed records for origin {} in resultsMap", resultInputs.size(), originId);

            // Return one output entity per input row for CoproProcessor to insert
            List<DocumentWisePostProcessingOriginOutput> outputs = new ArrayList<>();
            for (DocumentWisePostProcessingInput input : resultInputs) {
                outputs.add(DocumentWisePostProcessingOriginOutput.builder()
                        .input(input)
                        .defaultCreatedUserId(defaultCreatedUserId)
                        .build());
            }
            log.info("Returning {} output entities for origin {}", outputs.size(), originId);
            return outputs;

        } catch (Exception e) {
            log.error("Error processing origin {}: {}", originId, e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error processing origin: " + originId, handymanException, actionExecutionAudit);

            resultsMap.put(originId, originInputs);

            List<DocumentWisePostProcessingOriginOutput> outputs = new ArrayList<>();
            for (DocumentWisePostProcessingInput input : originInputs) {
                outputs.add(DocumentWisePostProcessingOriginOutput.builder()
                        .input(input)
                        .defaultCreatedUserId(defaultCreatedUserId)
                        .build());
            }
            return outputs;
        }
    }

    private List<String> loadScriptOrder() {
        String cacheKey = "scriptOrder";
        if (scriptClassesCache.containsKey(cacheKey)) {
            return scriptClassesCache.get(cacheKey);
        }

        String key = "document.wise.executor.bsh.class.order";
        String order = actionExecutionAudit.getContext().get(key);
        if (order == null || order.isEmpty()) {
            log.warn("No BSH class order found for key: {}", key);
            scriptClassesCache.put(cacheKey, Collections.emptyList());
            return Collections.emptyList();
        }
        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        log.info("Loaded {} script classes from order: {}", classes.size(), order);
        scriptClassesCache.put(cacheKey, classes);
        return classes;
    }

    private List<DocumentWisePostProcessingInput> executeScripts(List<String> classes, List<DocumentWisePostProcessingInput> currentInputs) {
        List<DocumentWisePostProcessingInput> updatedInputs = new ArrayList<>(currentInputs);
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            String source = actionExecutionAudit.getContext().get(className.trim());
            if (source == null || source.isEmpty()) {
                log.warn("No source code found for class: {}", className);
                continue;
            }
            log.info("Executing script {}", className);
            updatedInputs = getPostProcessedValidatorList(className, source, updatedInputs, pipelineId);
        }
        return updatedInputs;
    }

    private List<DocumentWisePostProcessingInput> getPostProcessedValidatorList(String className, String sourceCode,
                                                                                List<DocumentWisePostProcessingInput> currentInputs,
                                                                                Long rootPipelineId) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully for class: {}", className);

            interpreter.set("logger", log);

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {}", classInstantiation);

            interpreter.set("documentWisePostProcessingInputList", currentInputs);
            interpreter.set("rootPipelineId", rootPipelineId);
            log.info("Mapped documentWisePostProcessingInputList and rootPipelineId, calling doCustomPredictionMapping");

            interpreter.eval("validatorResult = mapper.doCustomPredictionMapping(documentWisePostProcessingInputList, rootPipelineId);");
            log.info("Completed execution of doCustomPredictionMapping for class {}", className);

            Object validatorResultObject = interpreter.get("validatorResult");
            if (validatorResultObject != null) {
                return processValidatorResult(validatorResultObject);
            } else {
                log.warn("Validator result is null for class: {}", className);
                return currentInputs;
            }

        } catch (EvalError e) {
            log.error("BeanShell evaluation error for class {}: {}", className, e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error for class: " + className, handymanException, actionExecutionAudit);
            return currentInputs;
        } catch (Exception e) {
            log.error("Error executing class script: {}", className, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script: " + className, handymanException, actionExecutionAudit);
            return currentInputs;
        }
    }

    @NotNull
    private List<DocumentWisePostProcessingInput> processValidatorResult(Object validatorResultObject) {
        try {
            // Check if it's already a List<DocumentWisePostProcessingInput>
            if (validatorResultObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<DocumentWisePostProcessingInput> resultList = (List<DocumentWisePostProcessingInput>) validatorResultObject;
                log.info("Successfully retrieved list of {} DocumentWisePostProcessingInput objects", resultList.size());
                return resultList;
            }

            try {
                Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
                Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

                if (mappedData instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DocumentWisePostProcessingInput> resultList = (List<DocumentWisePostProcessingInput>) mappedData;
                    log.info("Successfully retrieved list of {} DocumentWisePostProcessingInput objects via getMappedData", resultList.size());
                    return resultList;
                } else {
                    log.error("Expected mappedData to be a List<DocumentWisePostProcessingInput>, but got: {}", mappedData != null ? mappedData.getClass().getName() : "null");
                    return new ArrayList<>();
                }
            } catch (NoSuchMethodException e) {
                // If getMappedData() doesn't exist, assume the object itself is the result
                log.warn("getMappedData() method not found, treating result object as List");
                if (validatorResultObject instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DocumentWisePostProcessingInput> resultList = (List<DocumentWisePostProcessingInput>) validatorResultObject;
                    return resultList;
                }
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error processing validator result: ", e);
            return new ArrayList<>();
        }
    }
}
