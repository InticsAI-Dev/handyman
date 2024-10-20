package in.handyman.raven.lib.adapters;

import in.handyman.raven.lib.interfaces.AdapterInterface;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

public class NumericAdapter implements AdapterInterface {
    @Override
    public boolean getValidationModel(String input, String allowedCharacters) throws Exception {
        input = validateSpecialCharacters(allowedCharacters, input);
        return StringUtils.isNumeric(input);
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
