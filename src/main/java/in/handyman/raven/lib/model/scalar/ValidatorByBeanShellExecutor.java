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

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final ExecutorService executor;

    public static class ItemData {
        private String extractedValue;
        private String bbox;

        public ItemData() {}

        public String getExtractedValue() {
            return extractedValue;
        }

        public void setExtractedValue(String extractedValue) {
            this.extractedValue = extractedValue;
        }

        public String getBbox() {
            return bbox;
        }

        public void setBbox(String bbox) {
            this.bbox = bbox;
        }
    }

    public ValidatorByBeanShellExecutor(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs,
                                        ActionExecutionAudit actionExecutionAudit,
                                        final Logger log,
                                        int threadPoolSize) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(Math.max(1, threadPoolSize));
    }

    public List<PostProcessingExecutorAction.PostProcessingExecutorInput> doRowWiseValidator() throws InterruptedException, ExecutionException {
        int inputSize = postProcessingExecutorInputs == null ? 0 : postProcessingExecutorInputs.size();
        log.info("Starting row-wise validation for {} inputs", inputSize);

        if (inputSize == 0) {
            try {
                if (actionExecutionAudit != null && actionExecutionAudit.getContext() != null) {
                    actionExecutionAudit.getContext().put("postprocessing.inputs", String.valueOf(0));
                }
            } catch (Exception ignored) {}
            return postProcessingExecutorInputs;
        }

        Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> byOrigin = groupByOrigin(postProcessingExecutorInputs);
        List<CompletableFuture<Void>> originFutures = new ArrayList<>();

        byOrigin.forEach((origin, originInputs) -> originFutures.add(
                CompletableFuture.runAsync(() -> {
                    try {
                        processOrigin(origin, originInputs);
                    } finally {
                        // no-op
                    }
                }, executor)
        ));

        CompletableFuture.allOf(originFutures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        log.info("Completed all validations for post processing inputs.");
        try {
            if (actionExecutionAudit != null && actionExecutionAudit.getContext() != null) {
                actionExecutionAudit.getContext().put("postprocessing.inputs", String.valueOf(inputSize));
                actionExecutionAudit.getContext().put("postprocessing.originsProcessed", String.valueOf(byOrigin.size()));
            }
        } catch (Exception ignored) {}

        return postProcessingExecutorInputs;
    }

    private Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> groupByOrigin(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs) {
        log.info("Grouping inputs by origin");
        if (inputs == null) return Collections.emptyMap();
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getOriginId));
    }

    private void processOrigin(String originId, List<PostProcessingExecutorAction.PostProcessingExecutorInput> originInputs) {
        log.info("Processing origin {} with {} inputs", originId, originInputs == null ? 0 : originInputs.size());
        Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> byPage = groupByPage(originInputs);

        List<CompletableFuture<Void>> pageFutures = new ArrayList<>();
        byPage.forEach((pageNo, pageInputs) ->
                pageFutures.add(CompletableFuture.runAsync(() -> {
                    long start = System.currentTimeMillis();
                    try {
                        processPage(originId, pageNo, pageInputs);
                    } finally {
                        long duration = System.currentTimeMillis() - start;
                        try {
                            if (actionExecutionAudit != null && actionExecutionAudit.getContext() != null) {
                                String key = "postprocessing.page.time." + originId + "." + pageNo;
                                actionExecutionAudit.getContext().put(key, String.valueOf(duration));
                            }
                        } catch (Exception ignored) {}
                    }
                }, executor))
        );

        try {
            CompletableFuture.allOf(pageFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("Error processing pages for origin {}", originId, e);
            throw new RuntimeException(e);
        }
    }

    // ===== Updated groupByPage to handle multiple pages per input =====
    private Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> groupByPage(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs) {
        log.info("Grouping inputs by page (custom: allow multiple pages per input)");

        Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> pageMap = new HashMap<>();
        if (inputs == null) return pageMap;

        for (PostProcessingExecutorAction.PostProcessingExecutorInput input : inputs) {
            if (input == null) continue;

            List<Integer> pageNos = getCustomPageNumbers(input);
            if (pageNos == null || pageNos.isEmpty()) continue;

            for (Integer pageNo : pageNos) {
                pageMap.computeIfAbsent(pageNo, k -> new ArrayList<>()).add(input);
            }
        }

        return pageMap;
    }

    // ===== Helper: get custom page numbers per input =====
    private List<Integer> getCustomPageNumbers(PostProcessingExecutorAction.PostProcessingExecutorInput input) {
        List<Integer> pages = new ArrayList<>();
        if (input.getPaperNo() != null) {
            pages.add(input.getPaperNo()); // always include original page

            // Example custom rules for multiple pages
            if ("age".equals(input.getSorItemName())) {
                pages.add(2); // "age" also appears in page 2
            }
            if ("name".equals(input.getSorItemName()) && input.getPaperNo() == 2) {
                pages.add(3); // "name" on page 2 also goes to page 3
            }
        }
        return pages;
    }

    private void processPage(String originId, Integer pageNo, List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
        log.info("START validation for origin {} page {}", originId, pageNo);
        long start = System.currentTimeMillis();

        Map<String, ItemData> initialMap = createMap(pageInputs);
        List<String> scriptClasses = loadScriptOrder(pageInputs);

        Map<String, ItemData> resultMap = executeScripts(scriptClasses, initialMap);
        updateInputs(pageInputs, resultMap);

        long duration = System.currentTimeMillis() - start;
        log.info("END validation for origin {} page {} ({} ms)", originId, pageNo, duration);
    }

    private Map<String, ItemData> createMap(List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
        Map<String, ItemData> map = new HashMap<>();
        if (pageInputs == null) return map;
        for (PostProcessingExecutorAction.PostProcessingExecutorInput input : pageInputs) {
            if (input == null) continue;
            ItemData data = new ItemData();
            data.setExtractedValue(input.getExtractedValue() == null ? "" : input.getExtractedValue());
            data.setBbox(input.getBbox() == null ? "{}" : input.getBbox());
            if (input.getSorItemName() != null) {
                map.put(input.getSorItemName(), data);
            } else {
                log.warn("Found input with null sorItemName, skipping");
            }
        }
        return map;
    }

    private List<String> loadScriptOrder(List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs) {
        boolean multi = pageInputs != null && pageInputs.stream().anyMatch(i -> "multi_value".equals(i.getLineItemType()));
        String key = multi ? "outbound.mapper.multi.bsh.class.order" : "outbound.mapper.bsh.class.order";
        String order = null;
        try {
            order = actionExecutionAudit == null ? null : actionExecutionAudit.getContext().get(key);
        } catch (Exception e) {
            log.warn("Unable to load script order from audit context for key {}: {}", key, e.getMessage());
        }
        if (order == null || order.isEmpty()) return Collections.emptyList();
        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.info("Loaded {} script classes", classes.size());
        return classes;
    }

    private Map<String, ItemData> executeScripts(List<String> classes, Map<String, ItemData> currentMap) {
        Map<String, ItemData> updatedMap = new HashMap<>(currentMap == null ? Collections.emptyMap() : currentMap);
        Long pipelineId = null;
        try {
            pipelineId = actionExecutionAudit == null ? null : actionExecutionAudit.getRootPipelineId();
        } catch (Exception e) {
            log.debug("rootPipelineId unavailable: {}", e.getMessage());
        }

        if (classes == null || classes.isEmpty()) {
            log.debug("No BeanShell classes configured to execute");
            return updatedMap;
        }

        for (String className : classes) {
            if (className == null || className.isEmpty()) continue;
            String source = null;
            try {
                source = actionExecutionAudit == null ? null : actionExecutionAudit.getContext().get(className.trim());
            } catch (Exception e) {
                log.error("Unable to fetch source for class {}: {}", className, e.getMessage());
            }
            if (source == null || source.isEmpty()) {
                log.warn("No source code found for class {}. Skipping.", className);
                continue;
            }
            log.info("Executing script {}", className);
            getPostProcessedValidatorMap(className, source, updatedMap, pipelineId);
        }
        return updatedMap;
    }

    private void getPostProcessedValidatorMap(String className, String sourceCode, Map<String, ItemData> updatedPostProcessingDetailsMap, Long rootPipelineId) {
        if (className == null || sourceCode == null) return;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully for {}", className);

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
            log.error("BeanShell evaluation error for {}: {}", className, e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error", handymanException, actionExecutionAudit);
        } catch (Exception e) {
            log.error("Error executing class script {}: ", className, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script", handymanException, actionExecutionAudit);
        }
    }

    private void processValidatorResult(Object validatorResultObject, Map<String, ItemData> updatedPostProcessingDetailsMap) {
        if (validatorResultObject == null || updatedPostProcessingDetailsMap == null) return;
        try {
            Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
            Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

            if (mappedData instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<?, ?> resultMap = (Map<?, ?>) mappedData;
                for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        String key = (String) entry.getKey();
                        ItemData target = updatedPostProcessingDetailsMap.get(key);
                        Object valueObj = entry.getValue();
                        String newValue = "";
                        String newBbox = "{}";
                        if (valueObj != null) {
                            try {
                                Method getExtractedValueMethod = valueObj.getClass().getDeclaredMethod("getExtractedValue");
                                getExtractedValueMethod.setAccessible(true);
                                Method getBboxMethod = valueObj.getClass().getDeclaredMethod("getBbox");
                                getBboxMethod.setAccessible(true);

                                newValue = (String) getExtractedValueMethod.invoke(valueObj);
                                newBbox = (String) getBboxMethod.invoke(valueObj);
                            } catch (NoSuchMethodException nsme) {
                                log.error("NoSuchMethodException for obj {}: {}. Available methods: {}",
                                        valueObj.getClass().getName(), nsme.getMessage(),
                                        java.util.Arrays.toString(valueObj.getClass().getDeclaredMethods()));
                            } catch (Exception e) {
                                log.error("Reflection failed for obj {}: {}", valueObj.getClass().getName(), e.getMessage(), e);
                            }
                        }
                        newValue = newValue != null ? newValue : "";
                        newBbox = newBbox != null ? newBbox : "{}";

                        if (target != null) {
                            target.setExtractedValue(newValue);
                            target.setBbox(newBbox);
                        } else {
                            ItemData newData = new ItemData();
                            newData.setExtractedValue(newValue);
                            newData.setBbox(newBbox);
                            updatedPostProcessingDetailsMap.put(key, newData);
                        }
                    } else {
                        log.warn("Skipping non-String key in resultMap: {}", entry.getKey());
                    }
                }
            } else {
                log.warn("validatorResultObject.getMappedData() did not return a Map for object: {}", validatorResultObject.getClass().getName());
            }
        } catch (NoSuchMethodException nsme) {
            log.error("Validator result object does not have getMappedData method: {}", nsme.getMessage());
        } catch (Exception e) {
            log.error("Error invoking methods via reflection: ", e);
        }
    }

    private void updateInputs(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs, Map<String, ItemData> resultMap) {
        if (inputs == null || resultMap == null || resultMap.isEmpty()) {
            log.debug("No updates to apply to inputs");
            return;
        }

        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputMap =
                inputs.stream()
                        .filter(Objects::nonNull)
                        .filter(i -> i.getSorItemName() != null)
                        .collect(Collectors.toMap(
                                PostProcessingExecutorAction.PostProcessingExecutorInput::getSorItemName,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        resultMap.forEach((sorItem, itemData) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput = postProcessingExecutorInputMap.get(sorItem);
            if (postProcessingExecutorInput != null) {
                String originalValue = postProcessingExecutorInput.getExtractedValue() == null ? "" : postProcessingExecutorInput.getExtractedValue();
                String newValue = itemData == null || itemData.getExtractedValue() == null ? "" : itemData.getExtractedValue();
                String newBbox = itemData == null || itemData.getBbox() == null ? "{}" : itemData.getBbox();

                postProcessingExecutorInput.setExtractedValue(newValue);
                postProcessingExecutorInput.setBbox(newBbox);

                if (!Objects.equals(originalValue, newValue) && newValue.isEmpty()) {
                    postProcessingExecutorInput.setVqaScore(0);
                    postProcessingExecutorInput.setAggregatedScore(0);
                }
            }
        });
    }
}