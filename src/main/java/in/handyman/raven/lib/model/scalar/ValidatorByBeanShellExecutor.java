package in.handyman.raven.lib.model.scalar;

import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwBshConfig;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ValidatorByBeanShellExecutor {

    private final List<PostProcessingFieldsInput> postProcessingExecutorInputs;
    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final List<SpwBshConfig> bshConfigList;

    // Pre-loaded: className -> ready-to-use Interpreter with class already instantiated
    private final Map<String, Interpreter> preLoadedInterpreters = new LinkedHashMap<>();

    public ValidatorByBeanShellExecutor(
            List<PostProcessingFieldsInput> postProcessingExecutorInputs,
            ActionExecutionAudit actionExecutionAudit,
            final Logger log,
            int threadPoolSize,
            List<SpwBshConfig> bshConfigList) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.bshConfigList = bshConfigList;
    }

    // -----------------------------------------------------------------------
    // PUBLIC ENTRY POINT — do not modify signature
    // -----------------------------------------------------------------------
    public List<PostProcessingFieldsInput> doRowWiseValidator()
            throws InterruptedException, ExecutionException {

        int inputSize = postProcessingExecutorInputs.size();
        log.info("Starting row-wise validation for {} inputs", inputSize);

        if (postProcessingExecutorInputs.isEmpty()) {
            log.warn("Input list is empty — nothing to validate");
            return Collections.emptyList();
        }

        // 1. Load script class order once
        List<String> scriptClasses = loadScriptOrder();
        log.info("Script class order: {}", scriptClasses);

        // 2. Pre-load all interpreters once
        preLoadInterpreters(scriptClasses);

        if (preLoadedInterpreters.isEmpty()) {
            log.warn("No interpreters were loaded — returning input as-is");
            return new ArrayList<>(postProcessingExecutorInputs);
        }

        // 3. Separate output list — avoids duplicates
        List<PostProcessingFieldsInput> finalOutput = new ArrayList<>();

        // 4. Process all origins sequentially
        Map<String, List<PostProcessingFieldsInput>> byOrigin =
                groupByOrigin(postProcessingExecutorInputs);
        log.info("Total origins to process: {}", byOrigin.size());

        for (Map.Entry<String, List<PostProcessingFieldsInput>> originEntry : byOrigin.entrySet()) {
            processOrigin(originEntry.getKey(), originEntry.getValue(), scriptClasses, finalOutput);
        }

        log.info("Completed all validations. Total output records: {}", finalOutput.size());
        return finalOutput;
    }

    // -----------------------------------------------------------------------
    // PRE-LOADING
    // -----------------------------------------------------------------------
    public void preLoadInterpreters(List<String> scriptClasses) {
        log.info("Pre-loading {} script class(es)", scriptClasses.size());

        for (String className : scriptClasses) {
            String callerName = actionExecutionAudit.getContext().get(className.trim());

            if (callerName == null || callerName.isEmpty()) {
                log.warn("No caller name mapped in context for class '{}' — skipping pre-load",
                        className);
                continue;
            }

            Optional<SpwBshConfig> configOpt = bshConfigList.stream()
                    .filter(c -> callerName.equals(c.getCallerName()))
                    .findFirst();

            if (!configOpt.isPresent()
                    || configOpt.get().getSourceCode() == null
                    || configOpt.get().getSourceCode().isEmpty()) {
                log.warn("No source code found for class='{}', callerName='{}' — skipping pre-load",
                        className, callerName);
                continue;
            }

            String sourceCode = configOpt.get().getSourceCode();
            try {
                Interpreter interpreter = new Interpreter();
                interpreter.eval(sourceCode);
                log.info("Source code evaluated for class='{}', callerName='{}'",
                        className, callerName);

                interpreter.set("logger", log);
                String instantiation = className + " mapper = new " + className + "(logger);";
                interpreter.eval(instantiation);
                log.info("Class instantiated in interpreter: {}", instantiation);

                preLoadedInterpreters.put(className, interpreter);
                log.info("Interpreter cached for class='{}'", className);

            } catch (EvalError e) {
                log.error("BeanShell eval error while pre-loading class='{}': {}",
                        className, e.getMessage(), e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException(
                        "BeanShell pre-load eval error", handymanException, actionExecutionAudit);
            } catch (Exception e) {
                log.error("Unexpected error while pre-loading class='{}': {}",
                        className, e.getMessage(), e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException(
                        "Pre-load error for class " + className, handymanException, actionExecutionAudit);
            }
        }

        log.info("Pre-loading complete — {} interpreter(s) ready: {}",
                preLoadedInterpreters.size(), preLoadedInterpreters.keySet());
    }

    // -----------------------------------------------------------------------
    // GROUPING HELPERS
    // -----------------------------------------------------------------------
    public Map<String, List<PostProcessingFieldsInput>> groupByOrigin(
            List<PostProcessingFieldsInput> inputs) {
        Map<String, List<PostProcessingFieldsInput>> grouped =
                inputs.stream()
                        .collect(Collectors.groupingBy(PostProcessingFieldsInput::getOriginId));
        log.info("Grouped {} inputs into {} origin(s)", inputs.size(), grouped.size());
        return grouped;
    }

    public Map<Integer, List<PostProcessingFieldsInput>> groupByPage(
            List<PostProcessingFieldsInput> inputs) {
        Map<Integer, List<PostProcessingFieldsInput>> grouped =
                inputs.stream()
                        .collect(Collectors.groupingBy(PostProcessingFieldsInput::getPaperNo));
        log.info("Grouped {} inputs into {} page(s)", inputs.size(), grouped.size());
        return grouped;
    }

    public Map<String, List<PostProcessingFieldsInput>> groupBySorItemNames(
            List<PostProcessingFieldsInput> inputs) {
        Map<String, List<PostProcessingFieldsInput>> grouped =
                inputs.stream()
                        .collect(Collectors.groupingBy(PostProcessingFieldsInput::getSorItemName));
        log.info("Grouped {} inputs into {} sor-item name(s)", inputs.size(), grouped.size());
        return grouped;
    }

    public Map<Integer, List<PostProcessingFieldsInput>> groupByInstanceOrder(
            List<PostProcessingFieldsInput> inputs) {

        Map<Integer, List<PostProcessingFieldsInput>> grouped = new LinkedHashMap<>();

        for (PostProcessingFieldsInput input : inputs) {

            String container = input.getSorContainerInstance();

            int instanceIndex = extractInstanceIndex(container);

            grouped
                    .computeIfAbsent(instanceIndex, k -> new ArrayList<>())
                    .add(input);
        }

        log.info("Grouped {} inputs into {} instance levels",
                inputs.size(), grouped.size());

        return grouped;
    }

    private int extractInstanceIndex(String container) {

        if (container == null)
            return 1;

        // Check if ends with underscore + number
        if (container.matches(".*_\\d+$")) {

            String numberPart = container.substring(container.lastIndexOf("_") + 1);

            try {
                return Integer.parseInt(numberPart) + 1; // 0 → 1st
            } catch (NumberFormatException e) {
                return 1;
            }
        }

        // No numeric suffix → treat as 1st
        return 1;
    }


    // -----------------------------------------------------------------------
    // PROCESSING: ORIGIN → PAGE → SOR-CONTAINER → SOR-ITEM  (all sequential)
    // -----------------------------------------------------------------------
    public void processOrigin(String originId,
                              List<PostProcessingFieldsInput> originInputs,
                              List<String> scriptClasses,
                              List<PostProcessingFieldsInput> finalOutput) {

        log.info("[Origin={}] START — {} inputs", originId, originInputs.size());

        Map<Integer, List<PostProcessingFieldsInput>> byPage = groupByPage(originInputs);
        log.info("[Origin={}] Pages to process: {}", originId, byPage.keySet());

        for (Map.Entry<Integer, List<PostProcessingFieldsInput>> pageEntry : byPage.entrySet()) {
            processPage(originId, pageEntry.getKey(), pageEntry.getValue(),
                    scriptClasses, finalOutput);
        }

        log.info("[Origin={}] END — all pages processed", originId);
    }

    public void processPage(String originId,
                            Integer pageNo,
                            List<PostProcessingFieldsInput> pageInputs,
                            List<String> scriptClasses,
                            List<PostProcessingFieldsInput> finalOutput) {

        log.info("[Origin={} | Page={}] START — {} inputs", originId, pageNo, pageInputs.size());
        long start = System.currentTimeMillis();

        Map<Integer, List<PostProcessingFieldsInput>> byContainer =
                groupByInstanceOrder(pageInputs);
        log.info("[Origin={} | Page={}] Sor-container instances: {}",
                originId, pageNo, byContainer.keySet());

        for (Map.Entry<Integer, List<PostProcessingFieldsInput>> containerEntry : byContainer.entrySet()) {
            processContainer(originId, pageNo, String.valueOf(containerEntry.getKey()),
                    containerEntry.getValue(), scriptClasses, finalOutput);
        }

        long duration = System.currentTimeMillis() - start;
        log.info("[Origin={} | Page={}] END — completed in {} ms", originId, pageNo, duration);
    }

    private void processContainer(String originId,
                                  Integer pageNo,
                                  String containerInstance,
                                  List<PostProcessingFieldsInput> containerInputs,
                                  List<String> scriptClasses,
                                  List<PostProcessingFieldsInput> finalOutput) {

        log.info("[Origin={} | Page={} | Container={}] START — {} inputs",
                originId, pageNo, containerInstance, containerInputs.size());

        Map<String, List<PostProcessingFieldsInput>> bySorItem =
                groupBySorItemNames(containerInputs);
        log.info("[Origin={} | Page={} | Container={}] Sor-item names: {}",
                originId, pageNo, containerInstance, bySorItem.keySet());

        Map<String, List<PostProcessingFieldsInput>> scriptResultMap =
                executeScripts(scriptClasses, bySorItem, originId, pageNo, containerInstance);

        List<PostProcessingFieldsInput> flatResult = scriptResultMap.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (flatResult.isEmpty()) {
            log.info("[Origin={} | Page={} | Container={}] No script output — " +
                            "falling back to {} original inputs",
                    originId, pageNo, containerInstance, containerInputs.size());
            finalOutput.addAll(containerInputs);
        } else {
            log.info("[Origin={} | Page={} | Container={}] Script produced {} output(s)",
                    originId, pageNo, containerInstance, flatResult.size());
            finalOutput.addAll(flatResult);
        }

        log.info("[Origin={} | Page={} | Container={}] END",
                originId, pageNo, containerInstance);
    }

    // -----------------------------------------------------------------------
    // SCRIPT EXECUTION
    // -----------------------------------------------------------------------
    public List<String> loadScriptOrder() {
        String key = "outbound.mapper.bsh.class.order";
        String order = actionExecutionAudit.getContext().get(key);

        if (order == null || order.isEmpty()) {
            log.warn("No script class order found for key '{}' — no scripts will run", key);
            return Collections.emptyList();
        }

        List<String> classes = Arrays.stream(order.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        log.info("Loaded {} script class(es) from context key '{}': {}",
                classes.size(), key, classes);
        return classes;
    }

    public Map<String, List<PostProcessingFieldsInput>> executeScripts(
            List<String> classes,
            Map<String, List<PostProcessingFieldsInput>> currentMap,
            String originId,
            Integer pageNo,
            String containerInstance) {

        log.info("[Origin={} | Page={} | Container={}] Executing {} script(s) over {} sor-item(s)",
                originId, pageNo, containerInstance, classes.size(), currentMap.size());

        Map<String, List<PostProcessingFieldsInput>> updatedMap = new HashMap<>();
        Long pipelineId = actionExecutionAudit.getRootPipelineId();

        for (String className : classes) {
            Interpreter preLoaded = preLoadedInterpreters.get(className);

            if (preLoaded == null) {
                log.warn("[Origin={} | Page={} | Container={}] No pre-loaded interpreter for " +
                        "class='{}' — skipping", originId, pageNo, containerInstance, className);
                continue;
            }

            log.info("[Origin={} | Page={} | Container={}] Running class='{}'",
                    originId, pageNo, containerInstance, className);

            getPostProcessedValidatorMap(className, preLoaded, currentMap, pipelineId, updatedMap);
        }

        log.info("[Origin={} | Page={} | Container={}] Script execution complete — " +
                        "{} sor-item(s) in result map",
                originId, pageNo, containerInstance, updatedMap.size());
        return updatedMap;
    }

    // -----------------------------------------------------------------------
    // INTERPRETER LAYER — unchanged
    // -----------------------------------------------------------------------
    public void getPostProcessedValidatorMap(
            String className,
            Interpreter interpreter,
            Map<String, List<PostProcessingFieldsInput>> currentPostProcessingDetailsMap,
            Long rootPipelineId,
            Map<String, List<PostProcessingFieldsInput>> updatedPostProcessingDetailsMap) {

        try {
            log.info("Input predictionKeyMap for class {}: {}", className, currentPostProcessingDetailsMap);
            interpreter.set("predictionKeyMap", currentPostProcessingDetailsMap);
            interpreter.set("rootPipelineId", rootPipelineId);
            log.info("Mapped predictionKeyMap and rootPipelineId, calling doCustomPredictionMapping");

            interpreter.eval("validatorResultMap = mapper.doCustomPredictionMapping(predictionKeyMap, rootPipelineId);");
            log.info("Completed execution of doCustomPredictionMapping for class {}", className);

            Object validatorResultObject = interpreter.get("validatorResultMap");

            if (currentPostProcessingDetailsMap == null) {
                log.info("updatedPostProcessingDetailsMap is null");
            } else {
                processValidatorListResult(validatorResultObject, updatedPostProcessingDetailsMap);
                log.info("updatedPostProcessingDetailsMap size for class name {} : {}",
                        className, updatedPostProcessingDetailsMap.size());
            }

        } catch (EvalError e) {
            log.error("BeanShell evaluation error: {}", e.getMessage(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("BeanShell evaluation error",
                    handymanException, actionExecutionAudit);
        } catch (Exception e) {
            log.error("Error executing class script: ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error executing class script",
                    handymanException, actionExecutionAudit);
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

            if (mapCandidate instanceof Map) {
                log.info("validatorResultObject is Map");
            } else {
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
}