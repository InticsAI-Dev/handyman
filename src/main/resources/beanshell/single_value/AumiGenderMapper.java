package com.intics.script.MultiLineItem;

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

public class AumiGenderMapper {

    private Logger logger;

    public AumiGenderMapper(Logger logger) {
        this.logger = logger;
    }

    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {
        String logMsg = "[RootPipelineID: " + rootPipelineId + "] Entered AumiGenderMapper doCustomPredictionMapping method";
        logger.info(logMsg);

        Object genderObj = predictionKeyMap.get("member_gender");

        if(genderObj instanceof List)
        {
            List genderList = (List) genderObj;

            for(int i = 0; i<genderList.size(); i++)
            {
                Object obj = genderList.get(i);
                if(obj instanceof PostProcessingExecutorInput)
                {
                    PostProcessingExecutorInput genderInput = (PostProcessingExecutorInput) obj;
                    String validatedGenderValue = genderValidation(genderInput.getExtractedValue(), rootPipelineId);
                    genderInput.setExtractedValue(validatedGenderValue);
                }
            }
            predictionKeyMap.put("member_gender", genderList);
        }
        return new MappingResult(predictionKeyMap);
    }

    public String genderValidation(String memberGender, Long rootPipelineId) {
        logger.info("[RootPipelineID: " + rootPipelineId + "] Entered AumiGenderMapper genderValidation method");
        return normalizeGender(memberGender);
    }

    public String normalizeGender(String gender) {
        gender = gender.trim().toLowerCase();
        switch (gender) {
            case "m":
            case "male":
                return "M";
            case "f":
            case "female":
                return "F";
            default:
                return "";
        }
    }

    public class MappingResult {
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

        public String getExtractedValue() {
            return extractedValue;
        }

        public void setExtractedValue(String extractedValue) {
            this.extractedValue = extractedValue;
        }
    }
}