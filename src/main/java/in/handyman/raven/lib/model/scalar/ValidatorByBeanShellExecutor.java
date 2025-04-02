package in.handyman.raven.lib.model.scalar;


import bsh.EvalError;
import bsh.Interpreter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
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
    private final Jdbi jdbi;
    private final Logger log;

    private static final String PIPELINE_END_TO_END_ENCRYPTION_VARIABLE = "pipeline.end.to.end.encryption";

    private static final String CHANNEL_NAME = "UNIVERSE";
    private static final String MODEL_REGISTRY = "RADON";


    private static final Long DEFAULT_FREQUENCY = 1L;
    private static final Integer DEFAULT_RANK = 1;

    private static final Integer DEFAULT_SOR_ITEM_ATTRIBUTION_ID = 0;
    private static final Long DEFAULT_ACC_TRANSACTION_ID = 1L;
    private static final String DEFAULT_BBOX = "{}";
    private static final Long DEFAULT_SCORE = 0L;


    public ValidatorByBeanShellExecutor(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs,
                                        ActionExecutionAudit actionExecutionAudit, Jdbi jdbi,
                                        final Logger log) {
        this.postProcessingExecutorInputs = postProcessingExecutorInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.jdbi = jdbi;
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
        boolean pipelineEndToEndEncryptionActivator = Boolean.parseBoolean(actionExecutionAudit.getContext().get(PIPELINE_END_TO_END_ENCRYPTION_VARIABLE));
        Map<String, String> postProcessingDetailsMap = getValidatorConfigurationDetailsMap(postProcessingDetailsByPageNo, pipelineEndToEndEncryptionActivator);

        String customMapperClassOrderVariable = "outbound.mapper.bsh.class.order";
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
        updatePostProcessingInputDetailsByPage(originId, pageNo, postProcessingDetailsByPageNo, updatedPostProcessingDetailsMap, pipelineEndToEndEncryptionActivator);
    }

    private void getPostProcessedValidatorMap(String className, String sourceCode, Map<String, String> updatedPostProcessingDetailsMap, Long rootPipelineId) {
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.eval(sourceCode);
            log.info("Source code loaded successfully");

            String classInstantiation = className + " mapper = new " + className + "();";
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

            Method getLogMessagesMethod = validatorResultObject.getClass().getMethod("getLogMessages");
            Object logMessages = getLogMessagesMethod.invoke(validatorResultObject);

            if (mappedData instanceof Map<?, ?>) {
                updatedPostProcessingDetailsMap.putAll((Map<String, String>) mappedData);
            }

            if (logMessages instanceof List) {
                String combinedLogs = String.join("\n", (List<String>) logMessages);
                log.info("Logs from BeanShell scripts for class {}: \n{}", className, combinedLogs);
            }
        } catch (Exception e) {
            log.error("Error invoking methods via reflection: ", e);
        }
    }

    private void updatePostProcessingInputDetailsByPage(String originId, Integer pageNo, List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputsByPage,
                                                        Map<String, String> updatedValidatorConfigurationDetailsMap,
                                                        boolean pipelineEndToEndEncryptionActivator) {

        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit);

        Map<String, PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputMap =
                postProcessingExecutorInputsByPage.stream()
                        .collect(Collectors.toMap(
                                PostProcessingExecutorAction.PostProcessingExecutorInput::getSorItemName,
                                Function.identity()));

        updatedValidatorConfigurationDetailsMap.forEach((sorItem, sorItemValue) -> {
            PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput = postProcessingExecutorInputMap.get(sorItem);
            if (postProcessingExecutorInput != null) {
                log.info("PostProcessing input exists for sorItem {}, updating value", sorItem);
                updateExistingValidator(pipelineEndToEndEncryptionActivator, postProcessingExecutorInput, encryption, sorItem);
            } else {
                log.info("PostProcessing input does exists for sorItem {},", sorItem);

//                boolean postProcessingAddNewItemOnValidation = Boolean.parseBoolean(actionExecutionAudit.getContext().get("pipeline.postprocessing.bsh.addItem.activator"));
//                if (postProcessingAddNewItemOnValidation) {
//                    log.info("pipeline.postprocessing.bsh.addItem.activator is true and Validator Detail does not exist for sorItem {}, creating new one for originId: {} and pageNo: {}", sorItem, originId, pageNo);
//                    addNewValidatorDetails(originId, pageNo, pipelineEndToEndEncryptionActivator, sorItem, sorItemValue, encryption);
//                } else {
//                    log.info("pipeline.postprocessing.bsh.addItem.activator is false, so not creating new validator info for sorItem - {} and originId: {} and pageNo: {}", sorItem, originId, pageNo);
//                }
            }
        });
    }

    private void addNewValidatorDetails(String originId, Integer pageNo, boolean pipelineEndToEndEncryptionActivator, String sorItem,
                                        String sorItemValue, InticsIntegrity encryption) {
        Long tenantId = Long.valueOf(actionExecutionAudit.getContext().get("tenant_id"));
        String documentId = actionExecutionAudit.getContext().get("document_id");

        Optional<ValidatorDetailBySorItem> optionalValidatorDetailBySorItem = getSorItemConfigurationDetails(jdbi, sorItem, tenantId);
        if (optionalValidatorDetailBySorItem.isPresent()) {

            ValidatorDetailBySorItem validatorDetailBySorItem = optionalValidatorDetailBySorItem.get();

            Long validatorSynonymId = validatorDetailBySorItem.getSynonymId();
            Long validatorQuestionId = validatorDetailBySorItem.getQuestionId();
            String validatorIsEncrypted = validatorDetailBySorItem.getIsEncrypted();
            String validatorEncryptionPolicy = validatorDetailBySorItem.getEncryptionPolicy();
            Long questionWeight = validatorDetailBySorItem.getWeights();

            PostProcessingExecutorAction.PostProcessingExecutorInput additionalPostProcessingInput = PostProcessingExecutorAction.PostProcessingExecutorInput
                    .builder()
                    .tenantId(tenantId)
                    .aggregatedScore(DEFAULT_SCORE)
                    .maskedScore(questionWeight)
                    .originId(originId)
                    .paperNo(pageNo)
                    .vqaScore(DEFAULT_SCORE)
                    .rank(DEFAULT_RANK)
                    .sorItemAttributionId(DEFAULT_SOR_ITEM_ATTRIBUTION_ID)
                    .sorItemName(sorItem)
                    .documentId(documentId)
                    .accTransactionId(DEFAULT_ACC_TRANSACTION_ID)
                    .bbox(DEFAULT_BBOX)
                    .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                    .frequency(DEFAULT_FREQUENCY)
                    .questionId(validatorQuestionId)
                    .synonymId(validatorSynonymId)
                    .modelRegistry(MODEL_REGISTRY)
                    .build();

            String sorItemUpdatedValue = sorItemValue;
            if (pipelineEndToEndEncryptionActivator && Objects.equals(validatorIsEncrypted, "t")) {
                sorItemUpdatedValue = encryption.encrypt(sorItemUpdatedValue, validatorEncryptionPolicy, sorItem);
            }
            additionalPostProcessingInput.setExtractedValue(sorItemUpdatedValue);
            updatedPostProcessingExecutorInputs.add(additionalPostProcessingInput);
        }
    }

    private void updateExistingValidator(boolean pipelineEndToEndEncryptionActivator, PostProcessingExecutorAction.PostProcessingExecutorInput validatorConfigurationDetail, InticsIntegrity encryption, String sorItemName) {
        String sorItemUpdatedValue = validatorConfigurationDetail.getExtractedValue();
        Long synonymId = validatorConfigurationDetail.getSynonymId();
        Long tenantId = validatorConfigurationDetail.getTenantId();
        Long sorItemId = getSorItemId(jdbi, synonymId, tenantId);
        if (sorItemId != null) {
            Optional<SorItemInfo> optionalSorItemInfo = getSorItemInfo(jdbi, sorItemId, tenantId);
            if (optionalSorItemInfo.isPresent()) {
                log.info("SorItemInfo exists, updating existing value");
                SorItemInfo sorItemInfo = optionalSorItemInfo.get();
                String isEncrypted = sorItemInfo.getIsEncrypted();
                String encryptionPolicy = sorItemInfo.getEncryptionPolicy();
                if (pipelineEndToEndEncryptionActivator && Objects.equals(isEncrypted, "t")) {
                    sorItemUpdatedValue = encryption.encrypt(sorItemUpdatedValue, encryptionPolicy, sorItemName);
                }
                validatorConfigurationDetail.setExtractedValue(sorItemUpdatedValue);
                updatedPostProcessingExecutorInputs.add(validatorConfigurationDetail);
            }
        }
    }


    private Map<String, String> getValidatorConfigurationDetailsMap(List<PostProcessingExecutorAction.PostProcessingExecutorInput> postProcessingExecutorInputs, boolean pipelineEndToEndEncryptionActivator) {
        Long tenantId = Long.valueOf(actionExecutionAudit.getContext().get("tenant_id"));
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit);
        Map<String, String> validatorConfigurationDetailsMap = new HashMap<>();
        postProcessingExecutorInputs.forEach(postProcessingExecutorInput -> {
            String sorItemName = postProcessingExecutorInput.getSorItemName();
            String sorItemValue = postProcessingExecutorInput.getExtractedValue();
            Long synonymId = postProcessingExecutorInput.getSynonymId();
            Long sorItemId = getSorItemId(jdbi, synonymId, tenantId);
            if (sorItemId != null) {

                Optional<SorItemInfo> optionalSorItemInfo = getSorItemInfo(jdbi, sorItemId, tenantId);
                if (optionalSorItemInfo.isPresent()) {
                    log.info("SorItemInfo exists");
                    SorItemInfo sorItemInfo = optionalSorItemInfo.get();
                    String sorItemEncrypted = sorItemInfo.getIsEncrypted();
                    String sorItemEncryptionPolicy = sorItemInfo.getEncryptionPolicy();
                    if (pipelineEndToEndEncryptionActivator && Objects.equals(sorItemEncrypted, "t")) {
                        sorItemValue = encryption.decrypt(sorItemValue, sorItemEncryptionPolicy, sorItemName);
                    }
                    validatorConfigurationDetailsMap.put(sorItemName, sorItemValue);
                }
            }
        });
        return validatorConfigurationDetailsMap;
    }

    private Optional<ValidatorDetailBySorItem> getSorItemConfigurationDetails(Jdbi jdbi, String sorItemName, Long tenantId) {
        String sql = "SELECT si.sor_item_name, si.sor_item_id, sq.question, si.allowed_adapter, si.restricted_adapter, " +
                "st.synonym_id, sq.question_id, si.word_limit, si.word_threshold, si.char_limit, si.char_threshold, " +
                "si.validator_threshold, si.allowed_characters, si.comparable_characters, si.restricted_adapter_flag, " +
                "si.created_user_id, si.tenant_id, si.is_encrypted, ep.encryption_policy, sq.weights " +
                "FROM sor_meta.document d " +
                "JOIN sor_meta.asset_info ai ON ai.document_type = d.document_type " +
                "JOIN sor_meta.sor_container sc ON sc.document_type = d.document_type " +
                "JOIN sor_meta.sor_item si ON si.sor_container_id = sc.sor_container_id " +
                "JOIN sor_meta.encryption_policies ep ON si.encryption_policy_id = ep.encryption_policy_id " +
                "JOIN sor_meta.truth_entity te ON te.asset_id = ai.asset_id " +
                "JOIN sor_meta.sor_tsynonym st ON st.sor_container_id = sc.sor_container_id " +
                "AND st.sor_item_id = si.sor_item_id AND st.truth_entity_id = te.truth_entity_id " +
                "JOIN sor_meta.sor_question sq ON sq.synonym_id = st.synonym_id " +
                "WHERE si.sor_item_name = :sorItemName " +
                "AND si.status = 'ACTIVE' " +
                "AND ai.status = 'ACTIVE' " +
                "AND sc.tenant_id = :tenantId " +
                "AND d.document_type = :documentType " +
                "AND ai.template_name = :templateName;";

        String documentType = actionExecutionAudit.getContext().get("document_type");
        try (Handle handle = jdbi.open();
             Query query = handle.createQuery(sql)
                     .bind("sorItemName", sorItemName)
                     .bind("tenantId", tenantId)
                     .bind("documentType", documentType)
                     .bind("templateName", CHANNEL_NAME)) {
            ValidatorDetailBySorItem validatorDetailBySorItem = query.mapToBean(ValidatorDetailBySorItem.class).one();
            log.info("SorItem details present for sorItem {}", sorItemName);
            return Optional.of(validatorDetailBySorItem);
        } catch (Exception e) {
            log.error("Error in getting the sorItem Validator Configuration Details", e);
            return Optional.empty();
        }
    }

    private Long getSorItemId(final Jdbi jdbi, final Long synonymId, final Long tenantId) {
        Long sorItemId = jdbi.withHandle(handle ->
                handle.createQuery("SELECT sor_item_id FROM sor_meta.sor_tsynonym WHERE synonym_id = :synonymId and tenant_id = :tenantId")
                        .bind("synonymId", synonymId)
                        .bind("tenantId", tenantId)
                        .mapTo(Long.class)
                        .findOne()
                        .orElse(null)
        );
        log.info("sorItemId : {} found", sorItemId);
        return sorItemId;
    }

    private Optional<SorItemInfo> getSorItemInfo(final Jdbi jdbi, final Long sorItemId, final Long tenantId) {
        Optional<SorItemInfo> sorItemInfo = jdbi.withHandle(handle ->
                handle.createQuery("select si.sor_item_id, si.sor_item_name, si.is_encrypted, ep.encryption_policy from sor_meta.sor_item si " +
                                "join sor_meta.encryption_policies ep on ep.encryption_policy_id = si.encryption_policy_id " +
                                "where sor_item_id = :sorItemId and si.tenant_id = :tenantId;")
                        .bind("sorItemId", sorItemId)
                        .bind("tenantId", tenantId)
                        .mapToBean(SorItemInfo.class)
                        .stream().filter(Objects::nonNull).findFirst());
        log.info("checking for sorItemInfo");
        return sorItemInfo;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SorItemInfo {
        private Long sorItemId;
        private String sorItemName;
        private String isEncrypted;
        private String encryptionPolicy;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidatorDetailBySorItem {
        private String sorItemName;
        private int sorItemId;
        private String question;
        private String allowedAdapter;
        private String restrictedAdapter;
        private Long questionId;
        private Long synonymId;
        private int wordLimit;
        private int wordThreshold;
        private int charLimit;
        private int charThreshold;
        private int validatorThreshold;
        private String allowedCharacters;
        private String comparableCharacters;
        private int restrictedAdapterFlag;
        private String createdUserId;
        private Long tenantId;
        private Long weights;
        private String isEncrypted;
        private String encryptionPolicy;
    }
}