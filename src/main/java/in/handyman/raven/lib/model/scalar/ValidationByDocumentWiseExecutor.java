package in.handyman.raven.lib.model.scalar;

import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ValidationByDocumentWiseExecutor {

    private final List<DocumentWisePostProcessingInput> documentWisePostProcessingInputs;

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final ExecutorService executor;

    public ValidationByDocumentWiseExecutor(List<DocumentWisePostProcessingInput> documentWisePostProcessingInputs,
                                            ActionExecutionAudit actionExecutionAudit,
                                            final Logger log,
                                            int threadPoolSize) {
        this.documentWisePostProcessingInputs = documentWisePostProcessingInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<DocumentWisePostProcessingInput> doDocumentWiseValidator() throws InterruptedException, ExecutionException {
        int inputSize = documentWisePostProcessingInputs.size();
        log.info("Starting document-wise validation for {} records", inputSize);

        Map<String, List<DocumentWisePostProcessingInput>> byOrigin = groupByOrigin(documentWisePostProcessingInputs);
        List<CompletableFuture<Void>> originFutures = new ArrayList<>();

        byOrigin.forEach((origin, originPredictions) -> originFutures.add(
                CompletableFuture.runAsync(() -> processOrigin(origin, originPredictions), executor)
        ));

        CompletableFuture.allOf(originFutures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Completed all validations for document-wise post processing.");
        return documentWisePostProcessingInputs;
    }

    private Map<String, List<DocumentWisePostProcessingInput>> groupByOrigin(List<DocumentWisePostProcessingInput> inputs) {
        log.info("Grouping records by origin_id");
        return inputs.stream()
                .filter(p -> p.getOriginId() != null)
                .collect(Collectors.groupingBy(DocumentWisePostProcessingInput::getOriginId));
    }

    private void processOrigin(String originId, List<DocumentWisePostProcessingInput> originInputs) {
        log.info("START validation for origin {} with {} records", originId, originInputs.size());
        long start = System.currentTimeMillis();

        List<String> scriptClasses = loadScriptOrder();
        List<DocumentWisePostProcessingInput> resultInputs = executeScripts(scriptClasses, originInputs);

        long duration = System.currentTimeMillis() - start;
        log.info("END validation for origin {} ({} ms)", originId, duration);
    }

    private List<String> loadScriptOrder() {
        String key = "document.wise.executor.bsh.class.order";
        String order = actionExecutionAudit.getContext().get(key);
        if (order == null || order.isEmpty()) {
            log.warn("No BSH class order found for key: {}", key);
            return Collections.emptyList();
        }
        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.info("Loaded {} script classes from order: {}", classes.size(), order);
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
            
            // Try to get mappedData via getMappedData() method (MappingResult pattern)
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
