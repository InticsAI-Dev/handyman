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

        List<ExtractedField> filteredItems = fields.stream()

                // Ensure default TRUE before blacklist processing
                .map(field -> {
                    if (!field.isLabelMatching()) {
                        field.setLabelMatching(true);
                    }
                    return field;
                })

                .map(field -> isLabelValueMatching(field.getBlacklistedSections(), field, "SECTIONS"))
                .map(field -> {
                    if (field.isLabelMatching()) {
                        return isLabelValueMatching(field.getBlacklistedLabels(), field, "LABELS");
                    }
                    return field;
                })
                .collect(Collectors.toList());

        emptyEntryCheck(filteredItems);
        return filteredItems;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public void emptyEntryCheck(List<ExtractedField> fields) {
        for (ExtractedField f : fields) {
            if (isEmpty(f.getValue())
                    && isEmpty(f.getLabel())
                    && isEmpty(f.getSectionAlias())) {

                f.setLabelMatching(true);
                f.setLabelMatchMessage(
                        (f.getLabelMatchMessage() == null ? "" : f.getLabelMatchMessage())
                                + " | Checking for missing Entry check, Empty entry with no value, label or section so allowing them for downstream process."
                );
            }
        }
    }
    /**
     * Checks if label or section-value pair matches blacklist and adjusts label accordingly.
     */
    public ExtractedField isLabelValueMatching(Set<String> blackListFields, ExtractedField response, String filteringType) {
        if (response == null) {
            return null;
        }

        if (blackListFields == null || blackListFields.isEmpty()) {
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
        List<String> sanitizedBlacklist = blackListFields.stream()
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

        // Case 1.5: Section contains blacklist substring
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

        // Case 3: Value is inside label → adjust
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

    public String sanitizeEndingPunctuations(String input) {
        if (input == null) return "";
        return input.replaceAll("[,\\-:;#\\s]+$", "").trim();
    }

    public String safeTrim(String input) {
        return input == null ? "" : input.trim();
    }

    public String removeSpecialCharacters(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").trim();
    }


    @Override
    public String getName() {
        return "blacklist";
    }
}