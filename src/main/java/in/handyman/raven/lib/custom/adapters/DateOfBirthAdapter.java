package in.handyman.raven.lib.custom.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.CharacterCountAdapter;
import in.handyman.raven.lib.adapters.DateAdapter;
import in.handyman.raven.lib.adapters.WordCountAdapter;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.interfaces.ScalarEvaluationInterface;
import in.handyman.raven.lib.model.AgadiaAdapter;

public class DateOfBirthAdapter implements ScalarEvaluationInterface {
    AdapterInterface wordCountAdapter = new WordCountAdapter();
    AdapterInterface charCountAdapter = new CharacterCountAdapter();
    AdapterInterface dobValidatorAdapter = new DateAdapter();

    @Override
    public int getConfidenceScore(String dob, AgadiaAdapter adapter, ActionExecutionAudit action) throws Exception {
        int confidenceScore = 0;
        //Config parameter
        int wcLimit = adapter.getWordCountLimit();
        int wcThresold = adapter.getWordCountThreshold();
        int charLimit = adapter.getCharCountLimit();
        int charThreshold = adapter.getCharCountThreshold();
        int validatorThresold = adapter.getValidatorThreshold();
        int comparableYear = adapter.getComparableYear();
        String[] dateFormats = adapter.getDateFormats();

        boolean dobValidator = dobValidatorAdapter.getDateValidationModel(dob, comparableYear, dateFormats);
        confidenceScore = dobValidator ? confidenceScore + validatorThresold : confidenceScore;

        if (dobValidator) {
            int wordCount = wordCountAdapter.getThresholdScore(dob);
            confidenceScore = wordCount <= wcLimit ? confidenceScore + wcThresold : confidenceScore;

            int charCount = charCountAdapter.getThresholdScore(dob);
            confidenceScore = charCount <= charLimit ? confidenceScore + charThreshold : confidenceScore;
        }
        return confidenceScore;
    }
}
