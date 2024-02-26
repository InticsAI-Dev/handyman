package in.handyman.raven.lib.adapters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationPurger {

    public static String purgerForAlpha(String inputValue){
        // Define the regular expression pattern to match numbers
        Pattern pattern = Pattern.compile("\\d");

        // Create a Matcher object
        Matcher matcher = pattern.matcher(inputValue);

        // Use Matcher.replaceAll() to replace all matches with an empty string
        String result = matcher.replaceAll("");

        return result;

    }

    public static String purgerForAlphaNumeric(String inputValue){
        String pattern = "[^a-zA-Z0-9]";

        // Use String.replaceAll() to replace all non-alphanumeric characters with an empty string
        return inputValue.replaceAll(pattern, "");
    }

    public static String purgerForNer(String inputValue){
        // Define the regular expression pattern to match numbers
        Pattern pattern = Pattern.compile("\\d");

        // Create a Matcher object
        Matcher matcher = pattern.matcher(inputValue);

        // Use Matcher.replaceAll() to replace all matches with an empty string
        String result = matcher.replaceAll("");
        return result;
    }

    public static String purgerForNumeric(String inputValue){
        // Define the regex pattern to match alphabetic and special characters
        String pattern = "[a-zA-Z\\p{Punct}\\s]";

        // Use String.replaceAll() to replace all alphabetic and special characters with an empty string
        return inputValue.replaceAll(pattern, "");
    }

    public static String purgerForDate(String inputValue){
        // Define the regex pattern to match unwanted characters
        String pattern = "[^\\d/-]";

        // Use String.replaceAll() to replace all unwanted characters with an empty string
        return inputValue.replaceAll(pattern, "");
    }




}
