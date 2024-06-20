package in.handyman.raven.lib.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.Validator;

public class CharacterCountAdapter implements AdapterInterface {
    @Override
    public int getThresholdScore(String sentence) throws Exception {
        if (sentence == null || sentence.isEmpty()) {
            return 0;
        }
        return sentence.length();
    }

    @Override
    public boolean getValidationModel(String sentence, String requiredFeature, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public boolean getNameValidationModel(Validator input, String uri, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    @Override
    public boolean getDateValidationModel(String sentence, int comparableYear, String[] dateFormats) throws Exception {
        return false;
    }
}
