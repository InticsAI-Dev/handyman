package in.handyman.raven.lib.adapters.selections;

import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelConfig;

import java.util.List;
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
    public ExtractedField isLabelValueMatching(List<WhitelistLabelConfig> whitelistFields,
                                                   ExtractedField response,
                                                   String filteringType) {

        if (response == null) {
            return null;
        }

        // If no whitelist provided, allow everything but clearly indicate that
        if (whitelistFields == null || whitelistFields.isEmpty()) {
            response.setLabelMatching(true);
            response.setLabelMatchMessage(
                    "No whitelist configured for " + filteringType + ". All labels are allowed."
            );
            return response;
        }

        String labelSource = filteringType.equals("SECTIONS") ? response.getSectionAlias() : response.getLabel();
        String rawLabel = safeTrim(labelSource);
        String label = removeSpecialCharacters(rawLabel).toLowerCase();

        boolean isLabelMatching = false;
        String message = "No whitelist match found for labels in whitelisted " + filteringType + ".";

        for (WhitelistLabelConfig cfg : whitelistFields) {
            if (cfg == null || cfg.getWhitelistKey() == null) continue;

            String sanitizedKey = removeSpecialCharacters(cfg.getWhitelistKey()).toLowerCase();
            String searchConfig = cfg.getLabelSearchConfig() == null
                    ? "EXACT"
                    : cfg.getLabelSearchConfig().trim().toUpperCase();

            boolean matched = false;

            switch (searchConfig) {
                case "CONTAINS":
                    matched = label.contains(sanitizedKey);
                    if (matched) {
                        isLabelMatching = true;
                        message = filteringType + " label matched CONTAINS mode: '" + sanitizedKey + "'.";
                    }
                    break;

                case "EXACT":
                default:
                    matched = label.equals(sanitizedKey);
                    if (matched) {
                        isLabelMatching = true;
                        message = filteringType + " label matched EXACT mode: '" + sanitizedKey + "'.";
                    }
                    break;
            }

            if (matched) break;
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
        return input.replaceAll("[^a-zA-Z0-9]", "").trim();
    }

    @Override
    public String getName() {
        return "whitelist";
    }
}
