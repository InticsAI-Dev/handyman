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

    // === Updated ItemData to match primitive double types ===
    public static class ItemData {
        private String extractedValue;
        private String bbox;
        private double vqaScore;   // ← PRIMITIVE double (matches PostProcessingExecutorInput)
        private int rank;          // ← PRIMITIVE int (matches PostProcessingExecutorInput)

        public ItemData() {
            this.vqaScore = 0.0;  // ← Default 0.0
            this.rank = 0;        // ← Default 0
        }

        public String getExtractedValue() { return extractedValue; }
        public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }

        public String getBbox() { return bbox; }
        public void setBbox(String bbox) { this.bbox = bbox; }

        public double getVqaScore() { return vqaScore; }
        public void setVqaScore(double vqaScore) { this.vqaScore = vqaScore; }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
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

    private List<Integer> getCustomPageNumbers(PostProcessingExecutorAction.PostProcessingExecutorInput input) {
        List<Integer> pages = new ArrayList<>();
        if (input.getPaperNo() != null) {
            pages.add(input.getPaperNo());
            if ("age".equals(input.getSorItemName())) {
                pages.add(2);
            }
            if ("name".equals(input.getSorItemName()) && input.getPaperNo() == 2) {
                pages.add(3);
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
            if (input == null || input.getSorItemName() == null) {
                log.warn("Found input with null sorItemName or null input, skipping");
                continue;
            }

            ItemData data = new ItemData();
            data.setExtractedValue(input.getExtractedValue() == null ? "" : input.getExtractedValue());
            data.setBbox(input.getBbox() == null ? "{}" : input.getBbox());
            data.setVqaScore(input.getVqaScore());
            data.setRank(input.getRank());

            map.put(input.getSorItemName(), data);
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
        return Arrays.stream(order.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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
            interpreter.set("predictionKeyMap", updatedPostProcessingDetailsMap);
            interpreter.set("rootPipelineId", rootPipelineId);

            String classInstantiation = className + " mapper = new " + className + "(logger);";
            interpreter.eval(classInstantiation);
            log.info("Class instantiated: {}", classInstantiation);

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

    // === Updated: Handle primitive double from script results ===
    private void processValidatorResult(Object validatorResultObject, Map<String, ItemData> updatedPostProcessingDetailsMap) {
        if (validatorResultObject == null || updatedPostProcessingDetailsMap == null) return;

        try {
            Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
            Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

            if (mappedData instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<?, ?> resultMap = (Map<?, ?>) mappedData;

                for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
                    if (!(entry.getKey() instanceof String)) {
                        log.warn("Skipping non-String key in resultMap: {}", entry.getKey());
                        continue;
                    }
                    String key = (String) entry.getKey();
                    Object valueObj = entry.getValue();

                    ItemData target = updatedPostProcessingDetailsMap.computeIfAbsent(key, k -> new ItemData());

                    // Default values (preserve existing if script doesn't provide)
                    String newValue = target.getExtractedValue();
                    String newBbox = target.getBbox();
                    double newVqaScore = target.getVqaScore();  // ← Preserve original
                    int newRank = target.getRank();             // ← Preserve original

                    if (valueObj != null) {
                        try {
                            // Extracted Value
                            Method getValueMethod = findMethod(valueObj, "getExtractedValue");
                            if (getValueMethod != null) {
                                Object val = getValueMethod.invoke(valueObj);
                                newValue = val != null ? val.toString() : "";
                            }

                            // BBox
                            Method getBboxMethod = findMethod(valueObj, "getBbox");
                            if (getBboxMethod != null) {
                                Object val = getBboxMethod.invoke(valueObj);
                                newBbox = val != null ? val.toString() : "{}";
                            }

                            // VqaScore (primitive double)
                            Method getVqaMethod = findMethod(valueObj, "getVqaScore");
                            if (getVqaMethod != null) {
                                Object val = getVqaMethod.invoke(valueObj);
                                if (val instanceof Number) {
                                    newVqaScore = ((Number) val).doubleValue();
                                }
                            }

                            // Rank (primitive int)
                            Method getRankMethod = findMethod(valueObj, "getRank");
                            if (getRankMethod != null) {
                                Object val = getRankMethod.invoke(valueObj);
                                if (val instanceof Number) {
                                    newRank = ((Number) val).intValue();
                                }
                            }

                        } catch (Exception e) {
                            log.error("Reflection failed for key {} on object {}: {}", key, valueObj.getClass().getName(), e.getMessage(), e);
                        }
                    }

                    // Apply updates (only override if script provided values)
                    target.setExtractedValue(newValue);
                    target.setBbox(newBbox);
                    target.setVqaScore(newVqaScore);
                    target.setRank(newRank);

                }
            } else {
                log.warn("getMappedData() did not return a Map: {}", mappedData.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing validator result via reflection: ", e);
        }
    }

    private Method findMethod(Object obj, String name) {
        try {
            Method m = obj.getClass().getDeclaredMethod(name);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    // === Updated: Write back primitive values ===
    private void updateInputs(List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs, Map<String, ItemData> resultMap) {
        if (inputs == null || resultMap == null || resultMap.isEmpty()) {
            log.debug("No updates to apply to inputs");
            return;
        }

        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> inputMap =
                inputs.stream()
                        .filter(Objects::nonNull)
                        .filter(i -> i.getSorItemName() != null)
                        .collect(Collectors.toMap(
                                PostProcessingExecutorAction.PostProcessingExecutorInput::getSorItemName,
                                Function.identity(),
                                (e1, e2) -> e1
                        ));

        resultMap.forEach((sorItem, itemData) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput input = inputMap.get(sorItem);
            if (input == null) return;

            String originalValue = input.getExtractedValue() == null ? "" : input.getExtractedValue();
            String newValue = itemData.getExtractedValue() == null ? "" : itemData.getExtractedValue();
            String newBbox = itemData.getBbox() == null ? "{}" : itemData.getBbox();

            // Update values
            input.setExtractedValue(newValue);
            input.setBbox(newBbox);
            input.setVqaScore(itemData.getVqaScore());
            input.setRank(itemData.getRank());

            // Zero out scores if value becomes empty (existing logic)
            if (!Objects.equals(originalValue, newValue) && newValue.isEmpty()) {
                input.setVqaScore(0.0);  // ← Explicit 0.0
                input.setAggregatedScore(0.0);  // ← Matches primitive double in input class
            }
        });
    }
}