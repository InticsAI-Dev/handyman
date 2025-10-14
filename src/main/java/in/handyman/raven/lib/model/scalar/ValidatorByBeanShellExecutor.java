package in.handyman.raven.lib.model.scalar;

import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidatorByBeanShellExecutor {

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs;

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> updatedPostProcessingExecutorInputs = new ArrayList<>();

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
            // set metrics to context and return
            try {
                if (actionExecutionAudit != null && actionExecutionAudit.getContext() != null) {
                    actionExecutionAudit.getContext().put("postprocessing.inputs", String.valueOf(0));
                }
            } catch (Exception ignored) {}
            return postProcessingExecutorInputs;
        }

        Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> byOrigin = groupByOrigin(postProcessingExecutorInputs);
        List<CompletableFuture<Void>> originFutures = new ArrayList<>();
        final AtomicInteger originProcessed = new AtomicInteger(0);
        final AtomicInteger pagesProcessed = new AtomicInteger(0);

        byOrigin.forEach((origin, originInputs) -> originFutures.add(
                CompletableFuture.runAsync(() -> {
                    try {
                        processOrigin(origin, originInputs);
                        originProcessed.incrementAndGet();
                    } finally {
                        // no-op
                    }
                }, executor)
        ));

        CompletableFuture<Void> allOrigins = CompletableFuture.allOf(originFutures.toArray(new CompletableFuture[0]));
        try {
            // wait with a reasonable timeout to avoid hanging indefinitely
            allOrigins.get(Math.max(60, Math.min(600, inputSize / 10 + 60)), TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            log.warn("Timeout waiting for origin tasks to complete: {}", te.getMessage());
            // attempt to continue and force shutdown below
        } catch (ExecutionException ee) {
            log.error("Execution exception in origin processing: {}", ee.getMessage(), ee);
            throw ee;
        } finally {
            // graceful shutdown with forced fallback
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.MINUTES)) {
                    log.warn("Executor did not terminate in time, forcing shutdown");
                    List<Runnable> dropped = executor.shutdownNow();
                    log.warn("Forced shutdown, dropped tasks: {}", dropped == null ? 0 : dropped.size());
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.warn("Interrupted while awaiting executor shutdown");
                executor.shutdownNow();
            }
        }

        log.info("Completed all validations for post processing inputs.");
        // store some lightweight metrics into audit context if available
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
                        // record per-page timing into audit context
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
        log.info("Grouping inputs by page");
        if (inputs == null) return Collections.emptyMap();
        return inputs.stream()
                .collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getPaperNo));
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
            log.info("BBOX-TRACE: createMap - sorItemName={}, original bbox={}, set bbox={}",
                    input.getSorItemName(),
                    (input.getBbox() == null ? "NULL" : "'" + input.getBbox() + "'"),
                    data.getBbox());
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
        int maxRetries = 2;
        int attempt = 0;
        while (attempt <= maxRetries) {
            attempt++;
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
                // success: break out of retry loop
                break;
            } catch (EvalError e) {
                log.error("BeanShell evaluation error on attempt {} for {}: {}", attempt, className, e.getMessage());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("BeanShell evaluation error", handymanException, actionExecutionAudit);
                if (attempt > maxRetries) {
                    log.error("Exceeded max retries for BeanShell eval for class {}", className);
                } else {
                    try {
                        // exponential backoff with jitter
                        long backoff = 100L * (1L << (attempt - 1)) + ThreadLocalRandom.current().nextLong(0, 100);
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted during backoff before retrying BeanShell eval");
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error executing class script {} on attempt {}: {}", className, attempt, e.getMessage(), e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Error executing class script", handymanException, actionExecutionAudit);
                if (attempt > maxRetries) {
                    log.error("Exceeded max retries for script execution for class {}", className);
                } else {
                    try {
                        long backoff = 100L * (1L << (attempt - 1));
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Interrupted during backoff before retrying script execution");
                        break;
                    }
                }
            }
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
                // Update or add entries in-place
                for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        String key = (String) entry.getKey();
                        ItemData target = updatedPostProcessingDetailsMap.get(key);
                        Object valueObj = entry.getValue();
                        String newValue = "";
                        String newBbox = "{}";
                        if (valueObj != null) {
                            try {
                                // Use getDeclaredMethod + setAccessible for BeanShell proxies
                                Method getExtractedValueMethod = valueObj.getClass().getDeclaredMethod("getExtractedValue");
                                getExtractedValueMethod.setAccessible(true);
                                Method getBboxMethod = valueObj.getClass().getDeclaredMethod("getBbox");
                                getBboxMethod.setAccessible(true);

                                newValue = (String) getExtractedValueMethod.invoke(valueObj);
                                newBbox = (String) getBboxMethod.invoke(valueObj);
                                log.info("Extracted from script for {}: value='{}', bbox='{}' (obj class: {})",
                                        key, newValue, newBbox, valueObj.getClass().getName());
                            } catch (NoSuchMethodException nsme) {
                                log.error("NoSuchMethodException for key {} (obj: {}): {}. Available methods: {}",
                                        key, valueObj.getClass().getName(), nsme.getMessage(),
                                        java.util.Arrays.toString(valueObj.getClass().getDeclaredMethods()));
                            } catch (Exception e) {
                                log.error("Reflection failed for key {} (obj: {}): {}", key, valueObj.getClass().getName(), e.getMessage(), e);
                            }
                        }
                        newValue = newValue != null ? newValue : "";
                        newBbox = newBbox != null ? newBbox : "{}";

                        if (target != null) {
                            // Update existing Java ItemData
                            target.setExtractedValue(newValue);
                            target.setBbox(newBbox);
                            log.info("Updated existing entry for {}: value='{}', bbox='{}'", key, target.getExtractedValue(), target.getBbox());
                        } else {
                            // Create and add new local ItemData
                            ItemData newData = new ItemData();
                            newData.setExtractedValue(newValue);
                            newData.setBbox(newBbox);
                            updatedPostProcessingDetailsMap.put(key, newData);
                            log.info("Added new entry for {}: value='{}', bbox='{}'", key, newData.getExtractedValue(), newData.getBbox());
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
                                (existing, replacement) -> existing  // in case of duplicates, keep first (consistent with previous behavior)
                        ));

        resultMap.forEach((sorItem, itemData) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput = postProcessingExecutorInputMap.get(sorItem);
            if (postProcessingExecutorInput != null) {
                log.info("PostProcessing input exists for sorItem {}, updating value", sorItem);
                String originalValue = postProcessingExecutorInput.getExtractedValue() == null ? "" : postProcessingExecutorInput.getExtractedValue();
                String newValue = itemData == null || itemData.getExtractedValue() == null ? "" : itemData.getExtractedValue();
                String newBbox = itemData == null || itemData.getBbox() == null ? "{}" : itemData.getBbox();
                log.info("BBOX-TRACE: updateInputs - sorItem={}, original input bbox={}, script bbox={}, final newBbox={}",
                        sorItem,
                        (postProcessingExecutorInput.getBbox() == null ? "NULL" : "'" + postProcessingExecutorInput.getBbox() + "'"),
                        (itemData == null || itemData.getBbox() == null ? "NULL" : "'" + itemData.getBbox() + "'"),
                        newBbox);

                postProcessingExecutorInput.setExtractedValue(newValue);
                postProcessingExecutorInput.setBbox(newBbox);

                if (Objects.equals(originalValue, newValue)) {
                    log.info("Extracted value and the PostProcessing value are same for the sorItem {}, bbox updated if changed.", sorItem);
                } else if (!newValue.isEmpty()) {
                    log.info("Value has been changed after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record.", sorItem);
                } else {
                    log.info("Value has been emptied after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record. Where the confidence score and b-box will be updated as zeros.", sorItem);
                    postProcessingExecutorInput.setVqaScore(0);
                    postProcessingExecutorInput.setAggregatedScore(0);
                }
            } else {
                log.info("PostProcessing input does not exist for sorItem {}", sorItem);
            }
        });
    }
}