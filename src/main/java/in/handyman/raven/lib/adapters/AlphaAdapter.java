package in.handyman.raven.lib.adapters;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.interfaces.AdapterInterface;
import in.handyman.raven.lib.model.FieldValidator;
import org.apache.commons.lang3.StringUtils;

public class AlphaAdapter implements AdapterInterface {
    @Override
    public boolean getValidationModel(String input, String allowedCharacters, ActionExecutionAudit audit) throws Exception {
        input = validateSpecialCharacters(allowedCharacters, input);
        return StringUtils.isAlpha(input);
    }

    @Override
    public boolean getNameValidationModel(FieldValidator input, String uri, ActionExecutionAudit audit) throws Exception {
        return false;
    }

    String validateSpecialCharacters(String specialCharacters, String input) {
        return AlphaAdapter.replaceSplChars(specialCharacters, input);
    }

    protected static String replaceSplChars(final String specialCharacters, String input) {
        /*final char targetChar = Character.MI
        final Set<Character> inputCharsSet = input.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        final Set<Character> specialCharactersSet = specialCharacters.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        final boolean retainAll = inputCharsSet.retainAll(specialCharactersSet);
        if (retainAll) {
            for (final Character chars : inputCharsSet) {
                input = input.replace(chars, targetChar);
            }
        }*/
        if (specialCharacters != null) {
            for (int i = 0; i < specialCharacters.length(); i++) {
                if (input.contains(Character.toString(specialCharacters.charAt(i)))) {
                    input = input.replace(Character.toString(specialCharacters.charAt(i)), "");
                }
            }
        }
        return input;
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
