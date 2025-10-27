package in.handyman.raven.lib.adapters.selections;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlacklistFilterAdapter implements FieldSelectionAdapter {

    @Override
    public List<ExtractedField> filter(List<ExtractedField> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }

        return fields.stream()
                // Step 1: Apply Section filtering
                .map(field -> isLabelValueMatching(field.getBlacklistedSections(), field, "SECTIONS"))
                // Step 2: Apply Label filtering only if section passed
                .map(field -> {
                    if (field.isLabelMatching()) {
                        return isLabelValueMatching(field.getBlacklistedLabels(), field, "LABELS");
                    }
                    return field;
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if label or section-value pair matches blacklist and adjusts label accordingly.
     */
    public ExtractedField isLabelValueMatching(Set<String> blackListFields, ExtractedField response, String filteringType) {
        if (response == null) {
            return null;
        }

        if(blackListFields == null || blackListFields.isEmpty()){
            response.setLabelMatching(true);
            response.setLabelMatchMessage("No blacklist provided for " + filteringType + ". All values are allowed.");
            return response;
        }
        // Normalize and sanitize input
        String labelSource = filteringType.equals("SECTIONS") ? response.getSectionAlias() : response.getLabel();
        String rawLabel = safeTrim(labelSource);
        String rawValue = safeTrim(response.getValue());
        String label = removeSpecialCharacters(rawLabel);
        String value = removeSpecialCharacters(rawValue);

        // Prepare sanitized blacklist
        List<String> sanitizedBlacklist = blackListFields == null ? List.of() :
                blackListFields.stream()
                        .filter(Objects::nonNull)
                        .map(this::removeSpecialCharacters)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

        String labelLower = label.toLowerCase();
        String valueLower = value.toLowerCase();

        boolean isLabelMatching = true;
        String message = "No match found between " + filteringType + " and value.";

        // Case 1: Label is directly blacklisted
        boolean isBlacklisted = sanitizedBlacklist.stream()
                .anyMatch(labelLower::equals);

        if (isBlacklisted) {
            isLabelMatching = false;
            message = filteringType + " is blacklisted and not allowed.";
        }
        // Case 1.5: For SECTIONS, label contains any blacklisted substring
        else if ("SECTIONS".equalsIgnoreCase(filteringType)) {
            String matchedSubstring = sanitizedBlacklist.stream()
                    .filter(bl -> !bl.isEmpty() && labelLower.contains(bl.toLowerCase(Locale.ROOT)))
                    .findFirst()
                    .orElse(null);

            if (matchedSubstring != null) {
                isLabelMatching = false;
                message = "Section contains a blacklisted substring: '" + matchedSubstring + "'.";
            }
        }
        // Case 2: Label exactly equals value
        else if (labelLower.equals(valueLower)) {
            message = filteringType + " exactly matches the value.";
        }
        // Case 3: Value is contained within label
        else if (!valueLower.isEmpty() && labelLower.contains(valueLower)) {
            String updatedLabel = label.replaceFirst("(?i)" + Pattern.quote(value), "").trim();
            response.setLabel(updatedLabel);

            String updatedLabelLower = removeSpecialCharacters(updatedLabel.toLowerCase());

            if (updatedLabel.isEmpty() && sanitizedBlacklist.contains("")) {

                message = filteringType + " became empty after removing value and empty is allowed in blacklist.";
            } else if (!updatedLabel.isEmpty() && sanitizedBlacklist.contains(updatedLabelLower)) {
                isLabelMatching = false;
                message = "Updated " + filteringType + " is blacklisted after removing value.";
            } else {
                message = "Value is contained within " + filteringType + ". Updated label after removal.";
            }
        }

        response.setLabelMatching(isLabelMatching);
        response.setLabelMatchMessage(message);
        return response;
    }

    /**
     * Removes trailing punctuation and whitespace.
     */
    public String sanitizeEndingPunctuations(String input) {
        if (input == null) return "";
        return input.replaceAll("[,\\-:;#\\s]+$", "").trim();
    }

    public String safeTrim(String input) {
        return input == null ? "" : input.trim();
    }

    /**
     * Removes special characters from the input string, keeping only alphanumeric characters and spaces.
     */
    public String removeSpecialCharacters(String input) {
        if (input == null) return "";
        // Replace all non-alphanumeric characters (except spaces) with an empty string
        return input.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }


    @Override
    public String getName() {
        return "blacklist";
    }
}
