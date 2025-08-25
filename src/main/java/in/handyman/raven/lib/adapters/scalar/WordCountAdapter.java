package in.handyman.raven.lib.adapters.scalar;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.FieldValidator;

import java.util.StringTokenizer;

public class WordCountAdapter implements AdapterInterface {
    @Override
    public int getThresholdScore(String sentence) throws Exception {
        if (sentence == null || sentence.isEmpty()) {
            return 0;
        }
        StringTokenizer tokens = new StringTokenizer(sentence);
        return tokens.countTokens();
    }

    @Override
    public boolean getValidationModel(String sentence, String requiredFeature, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public boolean getNameValidationModel(FieldValidator input, String uri, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public boolean getDateValidationModel(String sentence, int comparableYear, String[] dateFormats) throws Exception {
        return false;
    }
}
