package in.handyman.raven.lib.model.scalar;

import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ValidationByDocumentWiseExecutor {

    private final List<PredictionDTO> predictionDTOs;

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final ExecutorService executor;

    public ValidationByDocumentWiseExecutor(List<PredictionDTO> predictionDTOs,
                                            ActionExecutionAudit actionExecutionAudit,
                                            final Logger log,
                                            int threadPoolSize) {
        this.predictionDTOs = predictionDTOs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public List<PredictionDTO> doDocumentWiseValidator() throws InterruptedException, ExecutionException {
        int inputSize = predictionDTOs.size();
        log.info("Starting document-wise validation for {} predictions", inputSize);

        Map<String, List<PredictionDTO>> byOrigin = groupByOrigin(predictionDTOs);
        List<CompletableFuture<Void>> originFutures = new ArrayList<>();

        byOrigin.forEach((origin, originPredictions) -> originFutures.add(
                CompletableFuture.runAsync(() -> processOrigin(origin, originPredictions), executor)
        ));

        CompletableFuture.allOf(originFutures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Completed all validations for document-wise post processing.");
        return predictionDTOs;
    }

    private Map<String, List<PredictionDTO>> groupByOrigin(List<PredictionDTO> predictions) {
        log.info("Grouping predictions by origin_id");
        return predictions.stream()
                .filter(p -> p.getOriginId() != null)
                .collect(Collectors.groupingBy(PredictionDTO::getOriginId));
    }

    private void processOrigin(String originId, List<PredictionDTO> originPredictions) {
        log.info("START validation for origin {} with {} predictions", originId, originPredictions.size());
        long start = System.currentTimeMillis();

        List<String> scriptClasses = loadScriptOrder();
        List<PredictionDTO> resultPredictions = executeScripts(scriptClasses, originPredictions);
        updatePredictions(originPredictions, resultPredictions);

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

    private List<PredictionDTO> executeScripts(List<String> classes, List<PredictionDTO> currentPredictions) {
        List<PredictionDTO> updatedPredictions = new ArrayList<>(currentPredictions);
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            String source = actionExecutionAudit.getContext().get(className.trim());
            if (source == null || source.isEmpty()) {
                log.warn("No source code found for class: {}", className);
                continue;
            }
            log.info("Executing script {}", className);
            updatedPredictions = getPostProcessedValidatorList(className, source, updatedPredictions, pipelineId);
        }
        return updatedPredictions;
    }

    private List<PredictionDTO> getPostProcessedValidatorList(String className, String sourceCode, 
                                                               List<PredictionDTO> currentPredictions, 
                                                               Long rootPipelineId) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully for class: {}", className);

            interpreter.set("logger", log);

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {}", classInstantiation);

            interpreter.set("predictionDTOList", currentPredictions);
            interpreter.set("rootPipelineId", rootPipelineId);
            log.info("Mapped predictionDTOList and rootPipelineId, calling doCustomPredictionMapping");

            interpreter.eval("validatorResult = mapper.doCustomPredictionMapping(predictionDTOList, rootPipelineId);");
            log.info("Completed execution of doCustomPredictionMapping for class {}", className);

            Object validatorResultObject = interpreter.get("validatorResult");
            if (validatorResultObject != null) {
                return processValidatorResult(validatorResultObject);
            } else {
                log.warn("Validator result is null for class: {}", className);
                return currentPredictions;
            }

        } catch (EvalError e) {
            log.error("BeanShell evaluation error for class {}: {}", className, e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error for class: " + className, handymanException, actionExecutionAudit);
            return currentPredictions;
        } catch (Exception e) {
            log.error("Error executing class script: {}", className, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script: " + className, handymanException, actionExecutionAudit);
            return currentPredictions;
        }
    }

    @NotNull
    private List<PredictionDTO> processValidatorResult(Object validatorResultObject) {
        try {
            // Check if it's already a List<PredictionDTO>
            if (validatorResultObject instanceof List) {
                @SuppressWarnings("unchecked")
                List<PredictionDTO> resultList = (List<PredictionDTO>) validatorResultObject;
                log.info("Successfully retrieved list of {} PredictionDTO objects", resultList.size());
                return resultList;
            }
            
            // Try to get mappedData via getMappedData() method (MappingResult pattern)
            try {
                Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
                Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

                if (mappedData instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<PredictionDTO> resultList = (List<PredictionDTO>) mappedData;
                    log.info("Successfully retrieved list of {} PredictionDTO objects via getMappedData", resultList.size());
                    return resultList;
                } else {
                    log.error("Expected mappedData to be a List<PredictionDTO>, but got: {}", mappedData != null ? mappedData.getClass().getName() : "null");
                    return new ArrayList<>();
                }
            } catch (NoSuchMethodException e) {
                // If getMappedData() doesn't exist, assume the object itself is the result
                log.warn("getMappedData() method not found, treating result object as List");
                if (validatorResultObject instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<PredictionDTO> resultList = (List<PredictionDTO>) validatorResultObject;
                    return resultList;
                }
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error processing validator result: ", e);
            return new ArrayList<>();
        }
    }

    private void updatePredictions(List<PredictionDTO> originalPredictions, List<PredictionDTO> resultPredictions) {
        if (resultPredictions == null || resultPredictions.isEmpty()) {
            log.warn("Result predictions list is null or empty, no updates will be made");
            return;
        }

        Map<Long, PredictionDTO> resultMap = resultPredictions.stream()
                .filter(p -> p.getPredictionId() != null)
                .collect(Collectors.toMap(
                        PredictionDTO::getPredictionId,
                        p -> p,
                        (existing, replacement) -> replacement
                ));

        int updatedCount = 0;
        for (PredictionDTO original : originalPredictions) {
            if (original.getPredictionId() != null) {
                PredictionDTO updated = resultMap.get(original.getPredictionId());
                if (updated != null) {
                    updatePredictionFields(original, updated);
                    updatedCount++;
                }
            }
        }

        log.info("Updated {} predictions out of {} original predictions", updatedCount, originalPredictions.size());
    }

    private void updatePredictionFields(PredictionDTO original, PredictionDTO updated) {
        if (updated.getPredictedValue() != null) {
            if (Objects.equals(original.getPredictedValue(), updated.getPredictedValue())) {
                log.debug("Predicted value unchanged for predictionId: {}", original.getPredictionId());
            } else if (!updated.getPredictedValue().isEmpty()) {
                log.info("Updating predicted value for predictionId: {} from '{}' to '{}'", 
                        original.getPredictionId(), original.getPredictedValue(), updated.getPredictedValue());
                original.setPredictedValue(updated.getPredictedValue());
            } else {
                log.info("Predicted value emptied for predictionId: {}, clearing related fields", original.getPredictionId());
                original.setPredictedValue("");
                original.setPrecision(0.0);
                original.setLeftPos(0.0);
                original.setUpperPos(0.0);
                original.setRightPos(0.0);
                original.setLowerPos(0.0);
            }
        }

        if (updated.getPrecision() != null) {
            original.setPrecision(updated.getPrecision());
        }
        if (updated.getLeftPos() != null) {
            original.setLeftPos(updated.getLeftPos());
        }
        if (updated.getUpperPos() != null) {
            original.setUpperPos(updated.getUpperPos());
        }
        if (updated.getRightPos() != null) {
            original.setRightPos(updated.getRightPos());
        }
        if (updated.getLowerPos() != null) {
            original.setLowerPos(updated.getLowerPos());
        }
    }
}
