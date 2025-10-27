package in.handyman.raven.lib.adapters.selections;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WhitelistFilterAdapter implements FieldSelectionAdapter {

    @Override
    public List<ExtractedField> filter(List<ExtractedField> fields) {
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }

        return fields.stream()
                .map(field -> {
                        if(field.isLabelMatching()){
                            return isLabelValueMatching(field.getWhitelistedLabels(), field, "LABELS");

                        }else {
                            return field;
                        }
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if label or section-value pair matches whitelist.
     */
    public ExtractedField isLabelValueMatching(Set<String> whitelistFields, ExtractedField response, String filteringType) {
        if (response == null) {
            return null;
        }
        if(whitelistFields == null || whitelistFields.isEmpty()){
            response.setLabelMatching(true);
            response.setLabelMatchMessage("No whitelist provided for " + filteringType + ". All values are allowed.");
            return response;
        }

        String labelSource = filteringType.equals("SECTIONS") ? response.getSectionAlias() : response.getLabel();
        String rawLabel = safeTrim(labelSource);
        String rawValue = safeTrim(response.getValue());
        String label = removeSpecialCharacters(rawLabel);
        String value = removeSpecialCharacters(rawValue);

        List<String> sanitizedWhitelist = whitelistFields == null ? List.of() :
                whitelistFields.stream()
                        .filter(Objects::nonNull)
                        .map(this::removeSpecialCharacters)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());

        String labelLower = label.toLowerCase();
        String valueLower = value.toLowerCase();

        boolean isLabelMatching = false;
        String message = "No match found between " + filteringType + " and whitelist.";

        // Case 1: Label is directly whitelisted
        boolean isWhitelisted = sanitizedWhitelist.stream()
                .anyMatch(labelLower::equals);

        if (isWhitelisted) {
            isLabelMatching = true;
            message = filteringType + " is whitelisted and allowed.";
        }
        // Case 2: Label exactly equals value and value is whitelisted
        else if (sanitizedWhitelist.contains(valueLower)) {
            isLabelMatching = true;
            message = filteringType + " value is explicitly whitelisted.";
        }
        // Case 3: Label contains a whitelisted substring
        else {
            boolean containsWhitelisted = sanitizedWhitelist.stream()
                    .anyMatch(wl -> !wl.isEmpty() && labelLower.contains(wl));

            if (containsWhitelisted) {
                isLabelMatching = true;
                message = filteringType + " contains a whitelisted keyword.";
            }
        }

        response.setLabelMatching(isLabelMatching);
        response.setLabelMatchMessage(message);
        return response;
    }

    public String safeTrim(String input) {
        return input == null ? "" : input.trim();
    }

    public String removeSpecialCharacters(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }

    @Override
    public String getName() {
        return "whitelist";
    }
}
