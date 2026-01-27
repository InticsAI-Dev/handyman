package com.intics.script.MultiLineItem;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AumiMemberNameMapper {

    private Logger logger;

    public AumiMemberNameMapper(Logger logger) {
        this.logger = logger;
    }

    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {

        if (predictionKeyMap == null) {
            return new MappingResult(new HashMap());
        }

        PostProcessingExecutorInput fullNameObj =
                getFirst(predictionKeyMap, "member_full_name");
        PostProcessingExecutorInput firstNameObj =
                getFirst(predictionKeyMap, "member_first_name");
        PostProcessingExecutorInput lastNameObj =
                getFirst(predictionKeyMap, "member_last_name");

        if (hasValue(fullNameObj)) {

            NameParts parts = split(fullNameObj.getExtractedValue());

            applyAll(
                    predictionKeyMap,
                    fullNameObj,
                    parts.firstName,
                    parts.lastName,
                    fullNameObj.getExtractedValue()
            );
            return new MappingResult(predictionKeyMap);
        }

        if (hasValue(firstNameObj) && hasValue(lastNameObj)) {
            applySimple(predictionKeyMap, firstNameObj, lastNameObj);
            return new MappingResult(predictionKeyMap);
        }

        if (hasValue(firstNameObj)) {
            NameParts parts = split(firstNameObj.getExtractedValue());
            applyAll(predictionKeyMap, firstNameObj, parts.firstName, parts.lastName, "");
        } else if (hasValue(lastNameObj)) {
            NameParts parts = split(lastNameObj.getExtractedValue());
            applyAll(predictionKeyMap, lastNameObj, parts.firstName, parts.lastName, "");
        }

        return new MappingResult(predictionKeyMap);
    }

    private void applyAll(Map map,
                          PostProcessingExecutorInput source,
                          String firstName,
                          String lastName,
                          String fullName) {

        updateList(map, "member_first_name", firstName, source);
        updateList(map, "member_last_name", lastName, source);
        updateList(map, "member_full_name", fullName, source);
    }

    private void applySimple(Map map,
                             PostProcessingExecutorInput first,
                             PostProcessingExecutorInput last) {

        updateList(map, "member_first_name", first.getExtractedValue(), first);
        updateList(map, "member_last_name", last.getExtractedValue(), last);
        updateList(map, "member_full_name", "", null);
    }

    private void updateList(Map map,
                            String key,
                            String value,
                            PostProcessingExecutorInput source) {

        Object obj = map.get(key);
        if (!(obj instanceof List)) return;

        List list = (List) obj;

        for (int i = 0; i < list.size(); i++) {
            PostProcessingExecutorInput in =
                    (PostProcessingExecutorInput) list.get(i);

            in.setExtractedValue(value);

            if (isEmpty(value)) {
                clearDependentFields(in);
            } else if (source != null) {
                copyDependentFields(source, in);
            }
        }
        map.put(key, list);
    }

    private void clearDependentFields(PostProcessingExecutorInput in) {
        in.setLabel("");
        in.setSectionAlias("");
        in.setBBox("");
    }

    private void copyDependentFields(PostProcessingExecutorInput from,
                                     PostProcessingExecutorInput to) {

        to.setLabel(from.getLabel());
        to.setSectionAlias(from.getSectionAlias());
        to.setBBox(from.getBBox());
    }

    private NameParts split(String value) {

        if (value == null) return new NameParts("", "");

        value = value.trim();

        if (value.indexOf(",") >= 0) {
            String[] parts = value.split(",", 2);
            return new NameParts(clean(parts[1]), clean(parts[0]));
        }

        String[] tokens = value.split("\\s+");
        if (tokens.length == 1) {
            return new NameParts(tokens[0], "");
        }

        StringBuffer first = new StringBuffer();
        for (int i = 0; i < tokens.length - 1; i++) {
            first.append(tokens[i]).append(" ");
        }

        return new NameParts(first.toString().trim(), tokens[tokens.length - 1]);
    }

    private PostProcessingExecutorInput getFirst(Map map, String key) {
        Object obj = map.get(key);
        if (obj instanceof List) {
            List list = (List) obj;
            if (!list.isEmpty()) {
                return (PostProcessingExecutorInput) list.get(0);
            }
        }
        return null;
    }

    private boolean hasValue(PostProcessingExecutorInput in) {
        return in != null && !isEmpty(in.getExtractedValue());
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    private String clean(String s) {
        return s == null ? "" :
                s.replaceAll("[0-9]", "")
                        .replaceAll("\\s+", " ")
                        .trim();
    }

    private static class NameParts {
        String firstName;
        String lastName;

        NameParts(String f, String l) {
            this.firstName = f;
            this.lastName = l;
        }
    }

    public static class MappingResult {
        private Map mappedData;
        public MappingResult(Map mappedData) {
            this.mappedData = mappedData;
        }
        public Map getMappedData() {
            return mappedData;
        }
    }

    public static class PostProcessingExecutorInput {

        private Long tenantId;
        private double aggregatedScore;
        private double maskedScore;
        private String originId;
        private Integer paperNo;
        private String extractedValue;
        private double vqaScore;
        private Integer rank;
        private Integer sorItemAttributionId;
        private String sorItemName;
        private String documentId;
        private Long accTransactionId;
        private String label;
        private String sectionAlias;
        private Long score;
        private String bBox;
        private Long rootPipelineId;
        private Long frequency;
        private Long questionId;
        private Long synonymId;
        private String modelRegistry;
        private String encryptionPolicy;
        private String isEncrypted;
        private String lineItemType;

        public String getExtractedValue() { return extractedValue; }
        public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getSectionAlias() { return sectionAlias; }
        public void setSectionAlias(String sectionAlias) { this.sectionAlias = sectionAlias; }

        public String getBBox() { return bBox; }
        public void setBBox(String bBox) { this.bBox = bBox; }
    }
}
