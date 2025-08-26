package in.handyman.raven.lib.custom.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.scalar.AlphaAdapter;
import in.handyman.raven.lib.adapters.scalar.CharacterCountAdapter;
import in.handyman.raven.lib.adapters.scalar.WordCountAdapter;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.interfaces.ScalarEvaluationInterface;
import in.handyman.raven.lib.model.AgadiaAdapter;

public class MemberIdAdapter implements ScalarEvaluationInterface {
    AdapterInterface wordCountAdapter = new WordCountAdapter();
    AdapterInterface charCountAdapter = new CharacterCountAdapter();
    AdapterInterface idValidatorAdapter = new AlphaAdapter();

    @Override
    public int getConfidenceScore(String memberId, AgadiaAdapter adapter, ActionExecutionAudit audit) throws Exception {
        int confidenceScore = 0;
        //Config parameter
        int wcLimit = adapter.getWordCountLimit();
        int wcThresold = adapter.getWordCountThreshold();
        int charLimit = adapter.getCharCountLimit();
        int charThreshold = adapter.getCharCountThreshold();
        int validatorThresold = adapter.getValidatorThreshold();
        String validatorFeature = adapter.getValidatorDetail();

        int wordCount = wordCountAdapter.getThresholdScore(memberId);
        confidenceScore = wordCount <= wcLimit ? confidenceScore + wcThresold : confidenceScore;

        int charCount = charCountAdapter.getThresholdScore(memberId);
        confidenceScore = charCount <= charLimit ? confidenceScore + charThreshold : confidenceScore;

        boolean idValidator = idValidatorAdapter.getValidationModel(memberId, validatorFeature, audit);
        confidenceScore = idValidator ? confidenceScore + validatorThresold : confidenceScore;

        return confidenceScore;
    }
}
