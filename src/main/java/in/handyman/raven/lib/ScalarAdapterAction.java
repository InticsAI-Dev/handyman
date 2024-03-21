package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.*;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ScalarAdapter"
)
public class ScalarAdapterAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final ScalarAdapter scalarAdapter;
    private final Marker aMarker;

    private final Integer writeBatchSize = 1000;

    private final AlphavalidatorAction alphaAction;
    private final NumericvalidatorAction numericAction;
    private final AlphanumericvalidatorAction alphaNumericAction;
    private final DatevalidatorAction dateAction;
    private final WordcountAction wordcountAction;
    private final CharactercountAction charactercountAction;
    String URI;
    boolean multiverseValidator;
    String[] restrictedAnswers;
    private final String PHONE_NUMBER_REGEX = "^\\(?(\\d{3})\\)?[-]?(\\d{3})[-]?(\\d{4})$";
    private final String NUMBER_REGEX = "^[+-]?(\\d+\\.?\\d*|\\.\\d+)$";

    public ScalarAdapterAction(final ActionExecutionAudit action, final Logger log,
                               final Object scalarAdapter) {
        this.scalarAdapter = (ScalarAdapter) scalarAdapter;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ScalarAdapter:" + this.scalarAdapter.getName());
        this.wordcountAction = new WordcountAction(action, log, Wordcount.builder().build());
        this.charactercountAction = new CharactercountAction(action, log, Charactercount.builder().build());
        this.alphaAction = new AlphavalidatorAction(action, log, Alphavalidator.builder().build());
        this.numericAction = new NumericvalidatorAction(action, log, Numericvalidator.builder().build());
        this.alphaNumericAction = new AlphanumericvalidatorAction(action, log, Alphanumericvalidator.builder().build());
        this.dateAction = new DatevalidatorAction(action, log, Datevalidator.builder().build());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "scalar has started" + scalarAdapter.getName());

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(scalarAdapter.getResourceConn());
            final List<ValidatorConfigurationDetail> validatorConfigurationDetails = new ArrayList<>();
            URI = action.getContext().get("copro.text-validation.url");
            multiverseValidator = Boolean.valueOf(action.getContext().get("validation.multiverse-mode"));
            restrictedAnswers = action.getContext().get("validation.restricted-answers").split(",");
            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(scalarAdapter.getResultSet());
                AtomicInteger i = new AtomicInteger(0);
                formattedQuery.forEach(sqlToExecute -> {
                    log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                    Query query = handle.createQuery(sqlToExecute);
                    ResultIterable<ValidatorConfigurationDetail> resultIterable = query.mapToBean(ValidatorConfigurationDetail.class);
                    List<ValidatorConfigurationDetail> detailList = resultIterable.stream().collect(Collectors.toList());
                    validatorConfigurationDetails.addAll(detailList);
                    log.info(aMarker, "executed query from index {}", i.get());
                });
            });

            //Add summary audit - ${process-id}.sanitizer_summary - this should hold row count, correct row count, error_row_count
            insertSummaryAudit(jdbi, validatorConfigurationDetails.size(), 0, 0);
            doCompute(jdbi, validatorConfigurationDetails);
            //doProcess(jdbi, validatorConfigurationDetails);
            log.info(aMarker, "scalar has completed" + scalarAdapter.getName());
        } catch (Exception e) {
            action.getContext().put(scalarAdapter.getName().concat(".error"), "true");
            log.error(aMarker, "The Exception occurred in Scalar Adapter ", e);
            throw new HandymanException("The Exception occurred in Scalar Adapter", e, action);
        }
    }

  /*  private void doProcess(final Jdbi jdbi, final List<ValidatorConfigurationDetail> validatorConfigurationDetails) {
        final int parallelism;
        if (scalarAdapter.getForkBatchSize() != null) {
            parallelism = Integer.parseInt(scalarAdapter.getForkBatchSize());
        } else {
            parallelism = 1;
        }
        log.info(aMarker, "total records to process {}", validatorConfigurationDetails.size());
        try {
            final int batchSize = validatorConfigurationDetails.size() / parallelism;
            if (parallelism > 1 && batchSize > 0) {
                log.info(aMarker, "parallel processing has started" + scalarAdapter.getName());
                final List<ValidatorConfigurationDetail> nerValidatorConfigurationDetails = validatorConfigurationDetails.stream().filter(validatorConfigurationDetail -> Objects.equals(validatorConfigurationDetail.getAllowedAdapter(), "")).collect(Collectors.toList());

                validatorConfigurationDetails.removeAll(nerValidatorConfigurationDetails);

                final List<List<ValidatorConfigurationDetail>> partition = Lists.partition(validatorConfigurationDetails, batchSize);
                final CountDownLatch countDownLatch = new CountDownLatch(partition.size());
                final ExecutorService executorService = Executors.newFixedThreadPool(parallelism);

                partition.forEach(items -> executorService.submit(() -> {
                    try {
                        items.forEach(validatorConfigurationDetail -> {

                            doCompute(jdbi, validatorConfigurationDetail);

                        });
                        log.info(aMarker, "total records to processed {}", items.size());
                    } finally {
                        countDownLatch.countDown();
                        log.info(aMarker, " {} batch processed", countDownLatch.getCount());
                    }
                }));

                if (!nerValidatorConfigurationDetails.isEmpty()) {
                    doProcess(jdbi, nerValidatorConfigurationDetails);
                }

                countDownLatch.await();

            } else {
                log.info(aMarker, "sequential processing has started" + scalarAdapter.getName());

                validatorConfigurationDetails.forEach(validatorConfigurationDetail -> {

                    doCompute(jdbi, validatorConfigurationDetail);

                });
            }
        } catch (Exception e) {
            log.error(aMarker, "The Failure Response {} --> {}", scalarAdapter.getName(), e.getMessage(), e);
        }
    }*/

    private void doCompute(final Jdbi jdbi, List<ValidatorConfigurationDetail> listOfDetails) {
        try {
            List<ValidatorConfigurationDetail> resultQueue = new ArrayList<>();
            for (ValidatorConfigurationDetail result : listOfDetails) {
                log.info(aMarker, "Build 19- scalar executing  validator {}", result);

//                String inputValue = result.getInputValue();
                Validator scrubbingInput = Validator.builder()
                        .inputValue(result.getInputValue())
                        .adapter(result.getAllowedAdapter())
                        .allowedSpecialChar(result.getAllowedCharacters())
                        .comparableChar(result.getComparableCharacters())
                        .threshold(result.getValidatorThreshold())
                        .build();

                Validator configurationDetails = computeScrubbingForValue(scrubbingInput);
                String inputValue = configurationDetails.getInputValue();
                result.setInputValue(inputValue);
                int wordScore = wordcountAction.getWordCount(inputValue,
                        result.getWordLimit(), result.getWordThreshold());
                int charScore = charactercountAction.getCharCount(inputValue,
                        result.getCharLimit(), result.getCharThreshold());

                int validatorScore = computeAdapterScore(configurationDetails);
                int validatorNegativeScore = 0;
                if (result.getRestrictedAdapterFlag() == 1 && validatorScore != 0) {
                    configurationDetails.setAdapter(result.getRestrictedAdapter());
                    validatorNegativeScore = computeAdapterScore(configurationDetails);
                }

                double valConfidenceScore = wordScore + charScore + validatorScore - validatorNegativeScore;
                log.info(aMarker, "Build 19-validator scalar confidence score {}", valConfidenceScore);

                updateEmptyValueIfLowCf(result, valConfidenceScore);
                updateEmptyValueForRestrictedAns(result, inputValue);
                log.info(aMarker, "Build 19-validator vqa score {}", result.getVqaScore());

                result.setWordScore(wordScore);
                result.setCharScore(charScore);
                result.setValidatorScore(validatorScore);
                result.setValidatorNegativeScore(validatorNegativeScore);
                result.setRootPipelineId(action.getRootPipelineId());
                result.setConfidenceScore(valConfidenceScore);
                result.setProcessId(String.valueOf(action.getProcessId()));
                result.setStatus("COMPLETED");
                result.setStage("SCALAR_VALIDATION");
                result.setMessage("scalar validation macro completed");
                resultQueue.add(result);
                log.info(aMarker, "executed  validator {}", result);

                if (resultQueue.size() == this.writeBatchSize) {
                    log.info(aMarker, "executing  batch {}", resultQueue.size());
                    consumerBatch(jdbi, resultQueue);
                    log.info(aMarker, "executed  batch {}", resultQueue.size());
                    insertSummaryAudit(jdbi, listOfDetails.size(), resultQueue.size(), 0);
                    resultQueue.clear();
                    log.info(aMarker, "cleared batch {}", resultQueue.size());
                }
            }
            if (!resultQueue.isEmpty()) {
                log.info(aMarker, "executing final batch {}", resultQueue.size());
                consumerBatch(jdbi, resultQueue);
                log.info(aMarker, "executed final batch {}", resultQueue.size());
                insertSummaryAudit(jdbi, listOfDetails.size(), resultQueue.size(), 0);
                resultQueue.clear();
                log.info(aMarker, "cleared final batch {}", resultQueue.size());
            }
        } catch (Exception e) {
            action.getContext().put(scalarAdapter.getName().concat(".error"), "true");
            log.error(aMarker, "Exception occurred in Scalar Computation {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in Scalar Computation", handymanException, action);
        }
    }

    private void updateEmptyValueForRestrictedAns(ValidatorConfigurationDetail result, String inputValue) {
        if (multiverseValidator) {
            log.info(aMarker, "Build 19-validator updatating for Restricted answer {}");
            for (String format : restrictedAnswers) {
                if (inputValue.equalsIgnoreCase(format)) {
                    updateEmptyValueAndCf(result);
                }
            }
        }
    }

    private void updateEmptyValueIfLowCf(ValidatorConfigurationDetail result, double valConfidenceScore) {
        if (valConfidenceScore < 100 && multiverseValidator) {
            log.info(aMarker, "Build 19-validator updateEmptyValueIfLowCf {}", valConfidenceScore);
            updateEmptyValueAndCf(result);

        }
    }

    private static void updateEmptyValueAndCf(ValidatorConfigurationDetail result) {
        result.setInputValue("");
        result.setVqaScore(0);
    }

    void consumerBatch(final Jdbi jdbi, List<ValidatorConfigurationDetail> resultQueue) {
        try {
            resultQueue.forEach(insert -> {
                        jdbi.useTransaction(handle -> {
                            try {
                                String COLUMN_LIST = "origin_id, paper_no, group_id, process_id, sor_id, sor_item_id, sor_item_name,question,question_id, synonym_id, answer,vqa_score, weight, created_user_id, tenant_id, created_on, word_score, char_score, validator_score_allowed, validator_score_negative, confidence_score,validation_name,b_box,status,stage,message,root_pipeline_id,model_name,model_version, model_registry, batch_id";
                                String COLUMN_BINDED_LIST = ":originId, :paperNo, :groupId, :processId , :sorId, :sorItemId, :sorKey, :question ,:questionId, :synonymId, :inputValue,:vqaScore, :weight, :createdUserId, :tenantId, NOW(), :wordScore , :charScore , :validatorScore, :validatorNegativeScore, :confidenceScore,:allowedAdapter,:bbox,:status,:stage,:message,:rootPipelineId,:modelName,:modelVersion, :modelRegistry, :batchId";
                                Update update = handle.createUpdate("  INSERT INTO sor_transaction.adapter_result_" + scalarAdapter.getProcessID() +
                                        " ( " + COLUMN_LIST + ") " +
                                        " VALUES( " + COLUMN_BINDED_LIST + ");" +
                                        "   ");
                                Update bindBean = update.bindBean(insert);
                                bindBean.execute();
                            } catch (Exception t) {
                                insertSummaryAudit(jdbi, 0, 0, 1);
                                log.error(aMarker, "error inserting result {}", resultQueue, t);
                                HandymanException handymanException = new HandymanException(t);
                                HandymanException.insertException("Exception occurred in Scalar Computation consumer batch insert into adapter result for groupId" + insert.groupId, handymanException, action);
                            }
                        });
                    }
            );
        } catch (Exception t) {
            insertSummaryAudit(jdbi, 0, 0, resultQueue.size());
            log.error(aMarker, "error inserting result {}", resultQueue, t);
            HandymanException handymanException = new HandymanException(t);
            HandymanException.insertException("Exception occurred in Scalar Computation consumer batch insert into adapter result", handymanException, action);
        }
    }

    int computeAdapterScore(Validator inputDetail) {
        int confidenceScore = 0;
        try {
            switch (inputDetail.getAdapter()) {
                case "alpha":
                    confidenceScore = this.alphaAction.getAlphaScore(inputDetail);
                    break;
                case "alphanumeric":
                    confidenceScore = this.alphaNumericAction.getAlphaNumericScore(inputDetail);
                    break;
                case "numeric":
                    confidenceScore = this.numericAction.getNumericScore(inputDetail);
                    break;
                case "date":
                    confidenceScore = this.dateAction.getDateScore(inputDetail);
//                    confidenceScore = inputDetail.getThreshold();
                    break;
                case "date_reg":
//                    confidenceScore = this.dateAction.getDateScore(inputDetail);
                    confidenceScore = inputDetail.getThreshold();
                    break;
                case "phone_reg":
                    confidenceScore = regValidator(inputDetail, PHONE_NUMBER_REGEX);
                    break;
                case "numeric_reg":
                    confidenceScore = regValidator(inputDetail, NUMBER_REGEX);
                    break;
            }
        } catch (Exception t) {
            log.error(aMarker, "error adpater validation{}", inputDetail, t);
            HandymanException handymanException = new HandymanException(t);
            HandymanException.insertException("Exception occurred in computing adapter score", handymanException, action);
        }
        return confidenceScore;

    }

    Validator computeScrubbingForValue(Validator inputDetail) {
        try {
            switch (inputDetail.getAdapter()) {
                case "alpha":
                    //Special character removed
                    inputDetail = scrubbingInput(inputDetail, "[^a-zA-Z0-9]");
                    break;
                case "numeric":
                    //Special character and alphabets removed
                    inputDetail = scrubbingInput(inputDetail, "[^0-9]");
                    break;
                case "numeric_reg":
                    //Special character and alphabets removed
                    inputDetail = scrubbingInput(inputDetail, "[^0-9]");
                    break;
                case "date_reg":
                    //Remove alphabets
                    // inputDetail = scrubbingInput(inputDetail,"[a-zA-Z]");
                    inputDetail = scrubbingDate(inputDetail);
                    break;
            }
        } catch (Exception t) {
            log.error(aMarker, "error adpater validation{}", inputDetail, t);
            HandymanException handymanException = new HandymanException(t);
            HandymanException.insertException("Exception occurred in computing adapter score", handymanException, action);
        }
        return inputDetail;
    }

    void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount) {
        SanitarySummary summary = new SanitarySummary().builder()
                .rowCount(rowCount)
                .correctRowCount(executeCount)
                .errorRowCount(errorCount)
                .build();
        jdbi.useTransaction(handle -> {
            Update update = handle.createUpdate("  INSERT INTO sor_transaction.sanitizer_summary_" + scalarAdapter.getProcessID() +
                    " ( row_count, correct_row_count, error_row_count, created_at) " +
                    " VALUES(:rowCount, :correctRowCount, :errorRowCount, NOW());");
            Update bindBean = update.bindBean(summary);
            bindBean.execute();
        });

    }

    private int regValidator(Validator validator, String regForm) {
        String inputValue = validator.getInputValue();
        inputValue = replaceAllowedChars(validator.getAllowedSpecialChar(), inputValue);
        Pattern pattern = Pattern.compile(regForm);
        Matcher matcher = pattern.matcher(inputValue);
        boolean matchValue = matcher.matches();
        return matchValue ? validator.getThreshold() : 0;
    }

    private String replaceAllowedChars(final String specialCharacters, String input) {
        if (specialCharacters != null) {
            for (int i = 0; i < specialCharacters.length(); i++) {
                if (input.contains(Character.toString(specialCharacters.charAt(i)))) {
                    input = input.replace(Character.toString(specialCharacters.charAt(i)), "");
                }
            }
        }
        return input;
    }

    private Validator scrubbingInput(Validator validator, String regix) {
        if (validator.getInputValue() != null) {
            String correctedValue = validator.getInputValue().replaceAll(regix, "");
            validator.setInputValue(correctedValue);
        }
        return validator;
    }

    private Validator scrubbingDate(Validator validator) {
        if (validator.getInputValue().length() > 2) {
            String originalString = validator.getInputValue();
            StringBuilder modifiedString = new StringBuilder(originalString);

            // Remove last alphabet character
            while (Character.isAlphabetic(modifiedString.charAt(modifiedString.length() - 1))) {
                modifiedString.deleteCharAt(modifiedString.length() - 1);
            }

            // Remove first alphabet character
            while (Character.isAlphabetic(modifiedString.charAt(0))) {
                modifiedString.deleteCharAt(0);
            }

//            String replacedString = modifiedString.toString().replaceAll("[a-zA-Z]", "/");
            validator.setInputValue(modifiedString.toString());

        }
        return validator;
    }

    @Override
    public boolean executeIf() throws Exception {
        return scalarAdapter.getCondition();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SanitarySummary {
        int rowCount;
        int correctRowCount;
        int errorRowCount;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidatorConfigurationDetail {
        private String originId;
        private int paperNo;
        private Integer groupId;
        private String ProcessId;
        private int sorId;
        private int sorItemId;
        private String sorKey;
        private String question;
        private Integer questionId;
        private Integer synonymId;
        private String inputValue;
        private float vqaScore;
        private int weight;
        private String createdUserId;
        private Long tenantId;
        private double wordScore;
        private double charScore;
        private double validatorScore;
        private double validatorNegativeScore;
        private double confidenceScore;
        private String allowedAdapter;
        private String restrictedAdapter;
        private String bbox;
        private String status;
        private String stage;
        private String message;
        private Long rootPipelineId;
        private String modelName;
        private String modelVersion;
        private String modelRegistry;
        private int wordLimit;
        private int wordThreshold;
        private int charLimit;
        private int charThreshold;
        private int validatorThreshold;
        private String allowedCharacters;
        private String comparableCharacters;
        private int restrictedAdapterFlag;
        private String sorItemName;
        private String batchId;
    }

}
