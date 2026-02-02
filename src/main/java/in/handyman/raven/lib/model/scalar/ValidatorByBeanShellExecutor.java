package in.handyman.raven.lib.model.scalar;


import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import org.slf4j.Logger;

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

    public void processPage(String originId, Integer pageNo, List<PostProcessingFieldsInput> pageInputs) {
        log.info("START validation for origin {} page {}", originId, pageNo);
        long start = System.currentTimeMillis();

        List<String> scriptClasses = loadScriptOrder(pageInputs);

        List<PostProcessingFieldsInput> resultMap = executeScripts(scriptClasses, pageInputs);
//        buildUpdatedResults(pageInputs, resultMap);
        pageInputs.clear();
        pageInputs.addAll(resultMap);

        long duration = System.currentTimeMillis() - start;
        log.info("END validation for origin {} page {} ({} ms)", originId, pageNo, duration);
    }



    public List<String> loadScriptOrder(List<PostProcessingFieldsInput> pageInputs) {
        boolean multi = pageInputs.stream().anyMatch(i -> "multi_value".equals(i.getLineItemType()));
        String key = multi ? "outbound.mapper.multi.bsh.class.order" : "outbound.mapper.bsh.class.order";
        String order = actionExecutionAudit.getContext().get(key);
        if (order == null || order.isEmpty()) return Collections.emptyList();
        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        log.info("Loaded {} script classes", classes.size());
        return classes;
    }

    public List<PostProcessingFieldsInput> executeScripts(List<String> classes, List<PostProcessingFieldsInput> currentMap) {
        List<PostProcessingFieldsInput> updatedMap = new ArrayList<>();
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            String source = actionExecutionAudit.getContext().get(className.trim());
            log.info("Executing script {}", className);
            getPostProcessedValidatorMap(className, source, currentMap, pipelineId, updatedMap);
        }
        return updatedMap;
    }

    public void getPostProcessedValidatorMap(String className, String sourceCode, List<PostProcessingFieldsInput> currentPostProcessingDetailsMap, Long rootPipelineId, List<PostProcessingFieldsInput> updatedPostProcessingDetailsMap) {
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
            log.info("Retrieved validatorResultMap from interpreter context for class {}", validatorResultObject);
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


    public void processValidatorListResult(Object validatorResultObject, List<PostProcessingFieldsInput> updatedPostProcessingDetailsMap) {
        try {

            if (validatorResultObject instanceof List) {
                List<PostProcessingFieldsInput> mappedDataResult = (List<PostProcessingFieldsInput>) validatorResultObject;
                updatedPostProcessingDetailsMap.addAll(mappedDataResult);
            }
        } catch (Exception e) {
            log.error("Error invoking methods via reflection: ", e);
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