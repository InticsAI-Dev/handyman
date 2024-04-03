package in.handyman.raven.lib.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.Validator;
import org.apache.commons.lang3.StringUtils;

public class AlphaNumericAdapter implements AdapterInterface {
    @Override
    public boolean getValidationModel(String input, String allowedCharacters, ActionExecutionAudit audit) throws Exception {
        input = validateSpecialCharacters(allowedCharacters, input);
        return StringUtils.isAlphanumeric(input);
    }

    @Override
    public boolean getNameValidationModel(Validator sentence, String requiredFeature, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    String validateSpecialCharacters(String specialCharacters, String input) {
        return AlphaAdapter.replaceSplChars(specialCharacters,input);
    }

    @Override
    public int getThresholdScore(String sentence) throws Exception {
        return 0;
    }

    @Override
    public boolean getDateValidationModel(String sentence, int comparableYear, String[] dateFormats) throws Exception {
        return false;
    }
}
