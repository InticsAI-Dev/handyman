package in.handyman.raven.lib.model.scalar;


import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ValidatorByBeanShellExecutor {

    private final List<PostProcessingFieldsInput> postProcessingExecutorInputs;

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final ExecutorService executor;


    public ValidatorByBeanShellExecutor(List<PostProcessingFieldsInput> postProcessingExecutorInputs,
                                        ActionExecutionAudit actionExecutionAudit,
                                        final Logger log,
                                        int threadPoolSize) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }


    public List<PostProcessingFieldsInput> doRowWiseValidator()
            throws InterruptedException, ExecutionException {

        int inputSize = postProcessingExecutorInputs.size();
        log.info("Starting row-wise validation for {} inputs", inputSize);

        List<CompletableFuture<Void>> allFutures = new ArrayList<>();

        if (!postProcessingExecutorInputs.isEmpty()) {
            Map<String,List<PostProcessingFieldsInput>> byOrigin =
                    groupByOrigin(postProcessingExecutorInputs);

            byOrigin.forEach((origin, originInputs) ->
                    allFutures.add(
                            CompletableFuture.runAsync(
                                    () -> processOrigin(origin, originInputs),
                                    executor
                            )
                    )
            );
        }

        // ---- WAIT FOR ALL ----
        CompletableFuture
                .allOf(allFutures.toArray(new CompletableFuture[0]))
                .get();

        // ---- SHUTDOWN ONCE ----
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Completed all validations for post processing inputs.");

        return postProcessingExecutorInputs;
    }

    public Map<String, List<PostProcessingFieldsInput>> groupByOrigin(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by origin");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingFieldsInput::getOriginId));
    }


    public Map<String, List<PostProcessingFieldsInput>> groupByOriginAndContainerInstance(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by origin and paper no.");
        return inputs.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getOriginId() + "|" + i.getPaperNo()
                ));
    }


    public Map<String, List<PostProcessingFieldsInput>> groupByOriginAndContainerInstanceMultiLine(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by origin and container instance for multi line items ");
        return inputs.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getOriginId() + "|" + i.getPaperNo() + "|" + i.getSorContainerInstance()
                ));
    }

    public void processOrigin(String originId, List<PostProcessingFieldsInput> originInputs) {
        log.info("Processing origin {} with {} inputs", originId, originInputs.size());
        Map<Integer, List<PostProcessingFieldsInput>> byPage = groupByPage(originInputs);

        byPage.forEach((pageNo, pageInputs) ->
                CompletableFuture.runAsync(() -> processPage(originId, pageNo, pageInputs), executor)
        );
    }

    public Map<Integer, List<PostProcessingFieldsInput>> groupByPage(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by page");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingFieldsInput::getPaperNo));
    }

    public Map<String, List<PostProcessingFieldsInput>> groupBySorItemNames(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by sor item names");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingFieldsInput::getSorItemName));
    }

    public Map<String, List<PostProcessingFieldsInput>> groupBySorContainerInstance(List<PostProcessingFieldsInput> inputs) {
        log.info("Grouping inputs by sor container instances");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingFieldsInput::getSorContainerInstance));
    }


    public void processPage(String originId, Integer pageNo, List<PostProcessingFieldsInput> pageInputs) {
        log.info("START validation for origin {} page {} page inputs {}", originId, pageNo, pageInputs.size());
        long start = System.currentTimeMillis();

        List<String> scriptClasses = loadScriptOrder(pageInputs);
        Map<String, List<PostProcessingFieldsInput>> groupBySorContainerInstance = groupBySorContainerInstance(pageInputs);

        groupBySorContainerInstance.forEach((s, postProcessingFieldsInputs) -> {
                log.info("Processing sor container instance {} with {} inputs for origin {} page {}", s, postProcessingFieldsInputs.size(), originId, pageNo);
                Map<String, List<PostProcessingFieldsInput>> groupedBySorItemNames = groupBySorItemNames(postProcessingFieldsInputs);
                Map<String, List<PostProcessingFieldsInput>> resultMap = executeScripts(scriptClasses, groupedBySorItemNames);

                List<PostProcessingFieldsInput> flatList =
                        resultMap.values()
                                .stream()
                                .flatMap(List::stream)
                                .collect(Collectors.toList());
                flatList.addAll(postProcessingFieldsInputs);
                pageInputs.addAll(flatList);
        });

        long duration = System.currentTimeMillis() - start;
        log.info("END validation for origin {} page {} ({} ms)", originId, pageNo, duration);
    }



    public List<String> loadScriptOrder(List<PostProcessingFieldsInput> pageInputs) {
        String key = "outbound.mapper.bsh.class.order";
        String order = actionExecutionAudit.getContext().get(key);
        if (order == null || order.isEmpty()) return Collections.emptyList();
        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        log.info("Loaded {} script classes", classes.size());
        return classes;
    }

    public Map<String, List<PostProcessingFieldsInput>> executeScripts(List<String> classes, Map<String, List<PostProcessingFieldsInput>> currentMap) {
        Map<String, List<PostProcessingFieldsInput>> updatedMap = new HashMap<>();
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            String source = actionExecutionAudit.getContext().get(className.trim());
            log.info("Executing script {}", className);
            getPostProcessedValidatorMap(className, source, currentMap, pipelineId, updatedMap);
        }
        return updatedMap;
    }

    public void getPostProcessedValidatorMap(String className, String sourceCode, Map<String, List<PostProcessingFieldsInput>> currentPostProcessingDetailsMap, Long rootPipelineId, Map<String, List<PostProcessingFieldsInput>> updatedPostProcessingDetailsMap) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully");

            interpreter.set("logger", log);

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {}", classInstantiation);

            interpreter.set("predictionKeyMap", currentPostProcessingDetailsMap);
            interpreter.set("rootPipelineId", rootPipelineId);
            log.info("Mapped predictionKeyMap and rootPipelineId, calling doCustomPredictionMapping");

            interpreter.eval("validatorResultMap = mapper.doCustomPredictionMapping(predictionKeyMap, rootPipelineId);");
            log.info("Completed execution of doCustomPredictionMapping for class {}", className);

            Object validatorResultObject = interpreter.get("validatorResultMap");
//            log.info("Retrieved validatorResultMap from interpreter context for class {}", validatorResultObject);
            if(currentPostProcessingDetailsMap == null){
                log.info("updatedPostProcessingDetailsMap is null");
            } else {

                processValidatorListResult(validatorResultObject, updatedPostProcessingDetailsMap);
                log.info("updatedPostProcessingDetailsMap size: {}", updatedPostProcessingDetailsMap.size());
            }
//            if (validatorResultObject != null) {
//                processValidatorResult(validatorResultObject, updatedPostProcessingDetailsMap);
//            }

        } catch (EvalError e) {
            log.error("BeanShell evaluation error: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error", handymanException, actionExecutionAudit);
        } catch (Exception e) {
            log.error("Error executing class script: ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script", handymanException, actionExecutionAudit);
        }
    }


    public void processValidatorListResult(
            Object validatorResultObject,
            Map<String, List<PostProcessingFieldsInput>> updatedPostProcessingDetailsMap) {

        try {
            if (validatorResultObject == null) {
                log.warn("validatorResultObject is null");
                return;
            }

            Object mapCandidate = validatorResultObject;

            // Case 1: validator directly returned Map
            if (mapCandidate instanceof Map) {
                log.info("validatorResultObject is Map");
            }
            // Case 2: validator returned MappingResult (not accessible) → extract via reflection
            else {
                try {
                    Method getMappedDataMethod =
                            validatorResultObject.getClass().getMethod("getMappedData");

                    mapCandidate = getMappedDataMethod.invoke(validatorResultObject);

                    log.info("Extracted mappedData via reflection from {}",
                            validatorResultObject.getClass().getName());

                } catch (NoSuchMethodException e) {
                    log.warn("No getMappedData() method found on {}",
                            validatorResultObject.getClass().getName());
                    return;
                }
            }

            // Final validation
            if (mapCandidate instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, List<PostProcessingFieldsInput>> mappedData =
                        (Map<String, List<PostProcessingFieldsInput>>) mapCandidate;

                updatedPostProcessingDetailsMap.putAll(mappedData);
            } else {
                log.warn("Extracted object is not a Map, actual type: {}",
                        mapCandidate.getClass().getName());
            }

        } catch (Exception e) {
            log.error("Error processing validator result", e);
        }
    }


    //
//    public void processValidatorResult(Object validatorResultObject, List<PostProcessingFieldsInput> updatedPostProcessingDetailsMap) {
//        try {
//            Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
//            Object mappedData = getMappedDataMethod.invoke(validatorResultObject);
//
//            if (mappedData instanceof Map<?, ?>) {
//                List<PostProcessingFieldsInput> mappedDataResult = getMappedDataResult((Map<?, ?>) mappedData);
//                updatedPostProcessingDetailsMap.addAll(mappedDataResult);
//            }
//        } catch (Exception e) {
//            log.error("Error invoking methods via reflection: ", e);
//        }
//    }
//
//    @NotNull
//    public List<PostProcessingFieldsInput> getMappedDataResult(List<PostProcessingFieldsInput> mappedData) {
//        List<PostProcessingFieldsInput> mappedDataResult = new ArrayList<>();
//
//        for (Map.Entry<?, ?> entry : mappedData.entrySet()) {
//            if (entry.getKey() instanceof String
//                    && entry.getValue() instanceof List<?>) {
//                @SuppressWarnings("unchecked")
//                List<PostProcessingFieldsInput> value =
//                        (List<PostProcessingFieldsInput>) entry.getValue();
//
//                mappedDataResult.addAll(value);
//
//            } else {
//                log.error(
//                        "Invalid entry in mappedData. Key type: {}, Value type: {}",
//                        entry.getKey() == null ? "null" : entry.getKey().getClass().getName(),
//                        entry.getValue() == null ? "null" : entry.getValue().getClass().getName()
//                );
//            }
//        }
//        return mappedDataResult;
//    }
//
//    public List<PostProcessingFieldsInput> buildUpdatedResults(
//            List<PostProcessingFieldsInput> inputs,
//            List<PostProcessingFieldsInput> resultMap) {
//
//        List<PostProcessingFieldsInput> finalResults =
//                new ArrayList<>();
//
//        if (inputs == null || inputs.isEmpty()) {
//            return finalResults;
//        }
//
//        resultMap.forEach((sorItemName, postProcessedList) -> {
//
//            for (int i = 0; i < postProcessedList.size(); i++) {
//
//                PostProcessingFieldsInput postProcessed = postProcessedList.get(i);
//                String processedValue = postProcessed.getAnswer();
//                if (processedValue == null || processedValue.trim().isEmpty()) {
//                    postProcessed.setLabel("");
//                    postProcessed.setSectionAlias("");
//                    postProcessed.setBBox("{}");
//                    postProcessed.setVqaScore(0.0);
//                    postProcessed.setAggregatedScore(0.0);
//                }
//                finalResults.add(postProcessed);
//
//            }
//        });
//        return finalResults;
//    }
}