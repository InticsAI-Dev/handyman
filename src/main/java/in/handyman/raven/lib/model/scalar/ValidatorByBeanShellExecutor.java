package in.handyman.raven.lib.model.scalar;


import bsh.EvalError;
import bsh.Interpreter;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import in.handyman.raven.lib.model.kvp.llm.jsonparser.BoundingBox;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidatorByBeanShellExecutor {

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs;
    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> updatedPostProcessingExecutorInputs = new ArrayList<>();

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;


    public ValidatorByBeanShellExecutor(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs,
                                        ActionExecutionAudit actionExecutionAudit,
                                        final Logger log) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
    }

    public List<PostProcessingExecutorAction.PostProcessingExecutorInput> doRowWiseValidator() {
        Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> postProcessingExecutorInputsByOriginMap = postProcessingExecutorInputs
                .stream().collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getOriginId));
        int totalOriginsWithPostProcessingDetails = postProcessingExecutorInputsByOriginMap.size();
        log.info("Starting the post processing for total origins: {}", totalOriginsWithPostProcessingDetails);
        doOriginWisePostProcessing(postProcessingExecutorInputsByOriginMap);
        return postProcessingExecutorInputs;
    }

    private void doOriginWisePostProcessing(Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> postProcessingExecutorInputsByOriginMap) {
        postProcessingExecutorInputsByOriginMap.forEach(this::doPageWisePostProcessing);
    }

    private void doPageWisePostProcessing(String originId, List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputsByOrigin) {
        Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> postProcessingDetailsMapByPageNo = postProcessingExecutorInputsByOrigin
                .stream()
                .collect(Collectors.groupingBy(PostProcessingExecutorAction.PostProcessingExecutorInput::getPaperNo));
        log.info("Total pages for originId {} with post processing details: {}", originId, postProcessingDetailsMapByPageNo.size());
        postProcessingDetailsMapByPageNo.forEach((pageNo, postProcessingExecutorInputsByPage) -> doPostProcessingWithBshByPageNo(originId, pageNo, postProcessingExecutorInputsByPage));
    }

    private void doPostProcessingWithBshByPageNo(String originId, Integer pageNo, List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingDetailsByPageNo) {
        log.info("Starting the validator for originId:{} and pageNo:{}", originId, pageNo);
        Map<String, String> updatedPostProcessingDetailsMap;
        Long rootPipelineId = actionExecutionAudit.getRootPipelineId();
        Map<String, String> postProcessingDetailsMap = getValidatorConfigurationDetailsMap(postProcessingDetailsByPageNo);

        boolean isMultiValue = postProcessingDetailsByPageNo.stream()
                .anyMatch(input -> "multi_value".equals(input.getLineItemType()));

        String customMapperClassOrderVariable = isMultiValue
                ? "outbound.mapper.multi.bsh.class.order"
                : "outbound.mapper.bsh.class.order";

        String customMapperClassOrder = actionExecutionAudit.getContext().get(customMapperClassOrderVariable);

        final List<String> outboundClassMapperOrders = Optional.ofNullable(customMapperClassOrder)
                .map(s -> Arrays.stream(s.split(",")).collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        log.info("Total class found for post processing validation by bsh is: {}", outboundClassMapperOrders.size());

        updatedPostProcessingDetailsMap = postProcessingDetailsMap;
        for (String className : outboundClassMapperOrders) {
            String sourceCode = actionExecutionAudit.getContext().get(className);
            log.info("Found source code for class {}", className);
            LocalDateTime customMappingClassStartTime = LocalDateTime.now();
            getPostProcessedValidatorMap(className, sourceCode, updatedPostProcessingDetailsMap, rootPipelineId);
            LocalDateTime customMappingClassEndTime = LocalDateTime.now();
            int totalTime = (int) ChronoUnit.MILLIS.between(customMappingClassStartTime, customMappingClassEndTime);
            log.info("{} mapper started at {}, and ended at {}, and took {} ms", className, customMappingClassStartTime, customMappingClassEndTime, totalTime);
        }
        log.info("Total entries found from all custom mapping with {} entries for originId: {} and pageNo: {}", updatedPostProcessingDetailsMap.size(), originId, pageNo);
        updatePostProcessingInputDetailsByPage(postProcessingDetailsByPageNo, updatedPostProcessingDetailsMap);
    }

    private void getPostProcessedValidatorMap(String className, String sourceCode, Map<String, String> updatedPostProcessingDetailsMap, Long rootPipelineId) {
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
                processValidatorResult(validatorResultObject, updatedPostProcessingDetailsMap, className);
            }

        } catch (EvalError e) {
            log.error("BeanShell evaluation error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error executing class script: ", e);
        }
    }

    private void processValidatorResult(Object validatorResultObject, Map<String, String> updatedPostProcessingDetailsMap, String className) {
        try {
            Method getMappedDataMethod = validatorResultObject.getClass().getMethod("getMappedData");
            Object mappedData = getMappedDataMethod.invoke(validatorResultObject);

            if (mappedData instanceof Map<?, ?>) {
                Map<String, String> mappedDataResult = (Map<String, String>) mappedData;
                updatedPostProcessingDetailsMap.putAll(mappedDataResult);
            }
        } catch (Exception e) {
            log.error("Error invoking methods via reflection: ", e);
        }
    }

    private void updatePostProcessingInputDetailsByPage(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputsByPage,
                                                        Map<String, String> updatedValidatorConfigurationDetailsMap
    ) {


        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputMap =
                postProcessingExecutorInputsByPage.stream()
                        .collect(Collectors.toMap(
                                PostProcessingExecutorAction.PostProcessingExecutorInput::getSorItemName,
                                Function.identity()));

        updatedValidatorConfigurationDetailsMap.forEach((sorItem, sorItemValue) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput = postProcessingExecutorInputMap.get(sorItem);
            if (postProcessingExecutorInput != null) {
                log.info("PostProcessing input exists for sorItem {}, updating value", sorItem);
                if(Objects.equals(postProcessingExecutorInput.getExtractedValue(), sorItemValue)){
                    log.info("Extracted value and the PostProcessing value are same for the sorItem {}, so no record has been updated.", sorItem);
                } else if (!Objects.equals(sorItemValue, "")) {
                    log.info("Value has been changed after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record.", sorItem);
                    updateExistingValidator(postProcessingExecutorInput, sorItemValue);
                } else {
                    log.info("Value has been emptied after doing PostProcessing for the sorItem {}, so the PostProcessed record will be updated in place for the current record. Where the confidence score and b-box will be updated as zeros.", sorItem);
                    postProcessingExecutorInput.setBbox("{}");
                    postProcessingExecutorInput.setVqaScore(0);
                    postProcessingExecutorInput.setAggregatedScore(0);
                    updateExistingValidator(postProcessingExecutorInput, sorItemValue);
                }
            } else {
                log.info("PostProcessing input does exists for sorItem {},", sorItem);

            }
        });
    }


    private void updateExistingValidator(PostProcessingExecutorAction.PostProcessingExecutorInput validatorConfigurationDetail, String sorItemValue) {
        log.info("SorItemInfo exists, updating existing value");

        validatorConfigurationDetail.setExtractedValue(sorItemValue);
        updatedPostProcessingExecutorInputs.add(validatorConfigurationDetail);

    }


    private Map<String, String> getValidatorConfigurationDetailsMap(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs) {

        Map<String, String> validatorConfigurationDetailsMap = new HashMap<>();
        postProcessingExecutorInputs.forEach(postProcessingExecutorInput -> {
            String sorItemName = postProcessingExecutorInput.getSorItemName();
            String sorItemValue = postProcessingExecutorInput.getExtractedValue();
            validatorConfigurationDetailsMap.put(sorItemName, sorItemValue);
        });
        return validatorConfigurationDetailsMap;
    }

}