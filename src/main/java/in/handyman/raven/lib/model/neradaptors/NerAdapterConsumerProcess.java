package in.handyman.raven.lib.model.neradaptors;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.*;
import in.handyman.raven.lib.model.*;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NerAdapterConsumerProcess implements CoproProcessor.ConsumerProcess<NerInputTable, NerOutputTable> {

    public final ActionExecutionAudit action;
    private static final String HTTP_CLIENT_TIMEOUT = "100";

    private final Logger log;
    private final Marker aMarker;
    private final NervalidatorAction nerAction;
    private final WordcountAction wordcountAction;
    private final CharactercountAction charactercountAction;
    private final AlphavalidatorAction alphaAction;
    private final NumericvalidatorAction numericAction;
    private final AlphanumericvalidatorAction alphaNumericAction;
    private final DatevalidatorAction dateAction;
    private final String PHONE_NUMBER_REGEX = "^\\(?(\\d{3})\\)?[-]?(\\d{3})[-]?(\\d{4})$";
    private final String NUMBER_REGEX = "^[+-]?(\\d+\\.?\\d*|\\.\\d+)$";
    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(Long.parseLong(HTTP_CLIENT_TIMEOUT), TimeUnit.MINUTES)
            .writeTimeout(Long.parseLong(HTTP_CLIENT_TIMEOUT), TimeUnit.MINUTES)
            .readTimeout(Long.parseLong(HTTP_CLIENT_TIMEOUT), TimeUnit.MINUTES)
            .build();
    boolean multiverseValidator;
    String[] restrictedAnswers;
    String URI;

    public NerAdapterConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.nerAction = new NervalidatorAction(action, log, Nervalidator.builder().build());
        this.wordcountAction = new WordcountAction(action, log, Wordcount.builder().build());
        this.charactercountAction = new CharactercountAction(action, log, Charactercount.builder().build());
        this.alphaAction = new AlphavalidatorAction(action, log, Alphavalidator.builder().build());
        this.numericAction = new NumericvalidatorAction(action, log, Numericvalidator.builder().build());
        this.alphaNumericAction = new AlphanumericvalidatorAction(action, log, Alphanumericvalidator.builder().build());
        this.dateAction = new DatevalidatorAction(action, log, Datevalidator.builder().build());
    }

    private static void updateEmptyValueAndCf(NerInputTable result) {
        result.setInputValue("");
        result.setVqaScore(0);
    }

    @Override
    public List<NerOutputTable> process(URL endpoint, NerInputTable result) throws Exception {
        URI = String.valueOf(endpoint);
        multiverseValidator = Boolean.parseBoolean(action.getContext().get("validation.multiverse-mode"));
        restrictedAnswers = action.getContext().get("validation.restricted-answers").split(",");
        log.info(aMarker, "Build 19-coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
        List<NerOutputTable> parentObj = new ArrayList<>();
        String inputValue = result.getInputValue();
        int wordScore = wordcountAction.getWordCount(inputValue,
                result.getWordLimit(), result.getWordThreshold());
        int charScore = CharactercountAction.getCharCount(inputValue,
                result.getCharLimit(), result.getCharThreshold());
        Validator configurationDetails = Validator.builder()
                .inputValue(inputValue)
                .adapter(result.getAllowedAdapter())
                .allowedSpecialChar(result.getAllowedCharacters())
                .comparableChar(result.getComparableCharacters())
                .threshold(result.getValidatorThreshold())
                .build();

        int validatorScore = computeAdapterScore(configurationDetails);
        int validatorNegativeScore;
        if (result.getRestrictedAdapterFlag() == 1 && validatorScore != 0) {
            configurationDetails.setAdapter(result.getRestrictedAdapter());
            validatorNegativeScore = computeAdapterScore(configurationDetails);
        } else {
            validatorNegativeScore = 0;
        }
        double valConfidenceScore = wordScore + charScore + validatorScore - validatorNegativeScore;
        log.info(aMarker, "Build 19-validator confidence score {}", valConfidenceScore);

        updateEmptyValueIfLowCf(result, valConfidenceScore);
        updateEmptyValueForRestrictedAns(result, inputValue);


        AtomicInteger atomicInteger = new AtomicInteger();
        log.info(aMarker, "coproProcessor consumer confidence score  {}", valConfidenceScore);
        String originId = result.getOriginId();
        Integer groupId = result.getGroupId();
        int sorId = result.getSorId();
        Long tenantId = result.getTenantId();
        String processId = result.getProcessId();
        int paperNo = result.getPaperNo();
        int sorItemId = result.getSorItemId();
        String sorKey = result.getSorKey();
        String question = result.getQuestion();
        int weight = result.getWeight();
        float vqaScore = result.getVqaScore();
        String createdUserId = result.getCreatedUserId();
        String modelRegistry = result.getModelRegistry();
        Long rootPipelineId = result.getRootPipelineId();
        log.info(aMarker, "Build 19-validator vqa score {}", vqaScore);


        if (valConfidenceScore >= 0) {
            parentObj.add(
                    NerOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .sorId(sorId)
                            .tenantId(tenantId)
                            .processId(processId)
                            .paperNo(paperNo)
                            .sorItemId(sorItemId)
                            .sorItemName(sorKey)
                            .question(question)
                            .answer(result.getInputValue())
                            .vqaScore(vqaScore)
                            .weight(weight)
                            .createdUserId(createdUserId)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .wordScore(wordScore)
                            .charScore(charScore)
                            .validatorScoreAllowed(validatorScore)
                            .validatorScoreNegative(validatorNegativeScore)
                            .confidenceScore(valConfidenceScore)
                            .validationName(result.getAllowedAdapter())
                            .bBox(result.getBbox())
                            .questionId(result.getQuestionId())
                            .synonymId(result.getSynonymId())
                            .status("COMPLETED")
                            .stage("SCALAR_VALIDATION")
                            .message("Ner validation macro completed")
                            .modelRegistry(modelRegistry)
                            .rootPipelineId(rootPipelineId)
                            .category(result.getCategory())
                            .build());


        } else {
            parentObj.add(
                    NerOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .sorId(sorId)
                            .tenantId(tenantId)
                            .processId(processId)
                            .paperNo(paperNo)
                            .sorItemId(sorItemId)
                            .sorItemName(sorKey)
                            .question(question)
                            .answer(result.getInputValue())
                            .vqaScore(vqaScore)
                            .weight(weight)
                            .createdUserId(createdUserId)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .wordScore(wordScore)
                            .charScore(charScore)
                            .validatorScoreAllowed(validatorScore)
                            .validatorScoreNegative(validatorNegativeScore)
                            .confidenceScore(valConfidenceScore)
                            .validationName(result.getAllowedAdapter())
                            .bBox(result.getBbox())
                            .questionId(result.getQuestionId())
                            .synonymId(result.getSynonymId())
                            .status("FAILED")
                            .stage("SCALAR_VALIDATION")
                            .message("Confidence Score is less than 0")
                            .modelRegistry(result.getModelRegistry())
                            .rootPipelineId(rootPipelineId)
                            .category(result.getCategory())
                            .build());
            log.error(aMarker, "The Exception occurred in confidence score validation by {} ", valConfidenceScore);
        }


        atomicInteger.set(0);
        log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
        return parentObj;
    }

    int computeAdapterScore(Validator inputDetail) {
        int confidenceScore = 0;
        try {

            switch (inputDetail.getAdapter()) {
                case "ner":
                    confidenceScore = this.nerAction.getNerScore(inputDetail, URI);
                    break;
               /* case "alpha":
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
                    break;
                case "phone_reg":
                    confidenceScore = regValidator(inputDetail, PHONE_NUMBER_REGEX);
                    break;
                case "numeric_reg":
                    confidenceScore = regValidator(inputDetail, NUMBER_REGEX);
                    break;*/
            }

        } catch (Throwable t) {
            log.error(aMarker, "error adapter validation{}", inputDetail, t);
            action.getContext().put(this.action.getActionName().concat(".error"), "true");
            log.error(aMarker, "Exception occurred in Scalar Computation {}", ExceptionUtil.toString(t));
            HandymanException handymanException = new HandymanException("Error in execute method for ner adapter", t, action);
            HandymanException.insertException("Exception occurred in NER Computation", handymanException, action);
            throw new HandymanException("Error in execute method for ner adapter", t, action);
        }
        return confidenceScore;
    }

    private void updateEmptyValueForRestrictedAns(NerInputTable result, String inputValue) {
        if (multiverseValidator) {
            log.info(aMarker, "validator updating for Restricted answer {}", inputValue);
            for (String format : restrictedAnswers) {
                if (inputValue.equalsIgnoreCase(format)) {
                    updateEmptyValueAndCf(result);
                }
            }
        }
    }

    private void updateEmptyValueIfLowCf(NerInputTable result, double valConfidenceScore) {
        if (valConfidenceScore < 100 && multiverseValidator) {
            log.info(aMarker, "validator updateEmptyValueIfLowCf {}", valConfidenceScore);
            updateEmptyValueAndCf(result);
        }
    }

    private int regValidator(Validator validator, String regForm) {
        String inputValue = validator.getInputValue();
        inputValue = replaceSplChars(validator.getAllowedSpecialChar(), inputValue);
        Pattern pattern = Pattern.compile(regForm);
        Matcher matcher = pattern.matcher(inputValue);
        return matcher.matches() ? validator.getThreshold() : 0;
    }

    private String replaceSplChars(final String specialCharacters, String input) {
        if (specialCharacters != null) {
            for (int i = 0; i < specialCharacters.length(); i++) {
                if (input.contains(Character.toString(specialCharacters.charAt(i)))) {
                    input = input.replace(Character.toString(specialCharacters.charAt(i)), "");
                }
            }
        }
        return input;
    }

}
