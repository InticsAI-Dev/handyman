package in.handyman.raven.lib.model.scalar;


import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidatorByBeanShellExecutor {

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs;

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> updatedPostProcessingExecutorInputs = new ArrayList<>();

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final ExecutorService executor;


    public ValidatorByBeanShellExecutor(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs,
                                        ActionExecutionAudit actionExecutionAudit,
                                        final Logger log,
                                        int threadPoolSize) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }


    public List<PostProcessingExecutorAction.PostProcessingExecutorInput> doRowWiseValidator() throws InterruptedException, ExecutionException {
        int inputSize = postProcessingExecutorInputs.size();
        log.info("Starting row-wise validation for {} inputs", inputSize);

        Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> byOrigin = groupByOrigin(postProcessingExecutorInputs);
        List<CompletableFuture<Void>> originFutures = new ArrayList<>();

        byOrigin.forEach((origin, originInputs) -> originFutures.add(
                CompletableFuture.runAsync(() -> processOrigin(origin, originInputs), executor)
        ));

        CompletableFuture.allOf(originFutures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Completed all validations for post processing inputs.");
        return postProcessingExecutorInputs;
    }

    private Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> groupByOrigin(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs) {
        log.info("Grouping inputs by origin");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getOriginId));
    }

    private void processOrigin(String originId, List<PostProcessingExecutorAction.PostProcessingExecutorInput> originInputs) {
        log.info("Processing origin {} with {} inputs", originId, originInputs.size());
        Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> byPage = groupByPage(originInputs);

        byPage.forEach((pageNo, pageInputs) ->
                CompletableFuture.runAsync(() -> processPage(originId, pageNo, pageInputs), executor)
        );
    }

    private Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> groupByPage(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs) {
        log.info("Grouping inputs by page");
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getPaperNo));
    }

    private void processPage(String originId, Integer pageNo, List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
        log.info("START validation for origin {} page {}", originId, pageNo);
        long start = System.currentTimeMillis();

        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> initialMap = createMap(pageInputs);
        List<String> scriptClasses = loadScriptOrder(pageInputs);

        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> resultMap = executeScripts(scriptClasses, initialMap);
        updateInputs(pageInputs, resultMap);

        long duration = System.currentTimeMillis() - start;
        log.info("END validation for origin {} page {} ({} ms)", originId, pageNo, duration);
    }

    private Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> createMap(List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> map = new HashMap<>();
        for (PostProcessingExecutorAction.PostProcessingExecutorInput input : pageInputs) {
            map.put(input.getSorItemName(), input);
        }
        return map;
    }

    private List<String> loadScriptOrder(List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
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

    private Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> executeScripts(List<String> classes, Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> currentMap) {
        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> updatedMap = new HashMap<>(currentMap);
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            String source = actionExecutionAudit.getContext().get(className.trim());
            log.info("Executing script {}", className);
            getPostProcessedValidatorMap(className, source, updatedMap, pipelineId);
        }
        return updatedMap;
    }

    private void getPostProcessedValidatorMap(String className, String sourceCode, Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> updatedPostProcessingDetailsMap, Long rootPipelineId) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully");

            interpreter.set("logger", log);

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {}", classInstantiation);

            interpreter.set("predictionKeyMap", updatedPostProcessingDetailsMap);
            interpreter.set("rootPipelineId", rootPipelineId);
            log.info("Mapped predictionKeyMap and rootPipelineId, calling doCustomPredictionMapping");

            interpreter.eval("validatorResultMap = mapper.doCustomPredictionMapping(predictionKeyMap, rootPipelineId);");
            log.info("Completed execution of doCustomPredictionMapping for class {}", className);

            Object validatorResultObject = interpreter.get("validatorResultMap");
            if (validatorResultObject != null) {
                processValidatorResult(validatorResultObject, updatedPostProcessingDetailsMap);
            }

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

    private void processValidatorResult(Object validatorResultObject, Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> updatedPostProcessingDetailsMap) {
        try {
            Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
            Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

            if (mappedData instanceof Map<?, ?>) {
                Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> mappedDataResult = getMappedDataResult((Map<?, ?>) mappedData);
                updatedPostProcessingDetailsMap.putAll(mappedDataResult);
            }
        } catch (Exception e) {
            log.error("Error invoking methods via reflection: ", e);
        }
    }

    @NotNull
    private Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> getMappedDataResult(Map<?, ?> mappedData) {
        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> mappedDataResult = new HashMap<>();

        for (Map.Entry<?, ?> entry : mappedData.entrySet()) {
            if (entry.getKey() instanceof String && entry.getValue() instanceof PostProcessingExecutorAction.PostProcessingExecutorInput) {
                mappedDataResult.put((String) entry.getKey(), (PostProcessingExecutorAction.PostProcessingExecutorInput) entry);
            } else {
                log.error("Expected mappedData to be a Map, but got: {}", mappedData.getClass().getName());
            }
        }
        return mappedDataResult;
    }

    private void updateInputs(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs, Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> resultMap) {
        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputMap =
                inputs.stream()
                        .collect(Collectors.toMap(
                                PostProcessingExecutorAction.PostProcessingExecutorInput::getSorItemName,
                                Function.identity()));

        resultMap.forEach((sorItem, PostProcessingExecutorOutput) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput = postProcessingExecutorInputMap.get(sorItem);
            if (postProcessingExecutorInput != null) {
                log.info("PostProcessing input exists for sorItem {}, updating value", sorItem);
                if(Objects.equals(postProcessingExecutorInput.getExtractedValue(), PostProcessingExecutorOutput.getExtractedValue())){
                    log.info("Extracted value and the PostProcessing value are same for the sorItem {}, so no record has been updated.", sorItem);
                } else if (!Objects.equals(PostProcessingExecutorOutput.getExtractedValue(), "")) {
                    log.info("Value has been changed after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record.", sorItem);
                    postProcessingExecutorInput.setExtractedValue(PostProcessingExecutorOutput.getExtractedValue());
                } else {
                    log.info("Value has been emptied after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record. Where the confidence score and b-box will be updated as zeros.", sorItem);
                    postProcessingExecutorInput.setBbox("{}");
                    postProcessingExecutorInput.setVqaScore(0);
                    postProcessingExecutorInput.setAggregatedScore(0);
                    postProcessingExecutorInput.setExtractedValue(PostProcessingExecutorOutput.getExtractedValue());
                }
            } else {
                log.info("PostProcessing input does exists for sorItem {},", sorItem);

            }
        });
    }
}