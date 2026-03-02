import org.slf4j.Logger;

import java.util.Map;

public class AumiGenderMapper {

    private Logger logger;

    public AumiGenderMapper(Logger logger) {
        this.logger = logger;
    }

    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {
        String logMsg = "[RootPipelineID: " + rootPipelineId + "] Entered AumiGenderMapper doCustomPredictionMapping method";
        logger.info(logMsg);
        if (shouldProcess(predictionKeyMap)) {
            String updatedGenderValue = genderValidation(predictionKeyMap, rootPipelineId);
            predictionKeyMap.put("member_gender", updatedGenderValue);
        } else {
            logMsg = "[RootPipelineID: " + rootPipelineId + "] No gender fields to process.";
            logger.info(logMsg);
        }
        return new MappingResult(predictionKeyMap);
    }


    private boolean shouldProcess(Map predictionKeyMap) {
        String gender = (String) predictionKeyMap.get("member_gender");

        if (gender == null) {
            return false;
        }

        return !gender.trim().isEmpty();
    }



    public String genderValidation(Map extractedGender, Long rootPipelineId) {
        if (extractedGender == null || extractedGender.isEmpty()) {
            String logMsg = "[RootPipelineID: " + rootPipelineId + "] No value found for the gender field. ";
            logger.info(logMsg);
            return "";
        }
        String memberGender = extractedGender.get("member_gender").toString();
        String formattedExtractedGender = normalizeGender(memberGender);
        return formattedExtractedGender;
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
}