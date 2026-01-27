package com.intics.script.MultiLineItem;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewBornRequestOCRMapper {

    public static final List EXACT_KEYWORDS = new ArrayList();
    public static final List CONTAINS_KEYWORDS = new ArrayList();

    static {
        String[] keywords = {
                "nicu", "toddler", "neonatal", "sick baby",
                "infant", "newborn", "new born",
                "new born admit", "new born admission",
                "newborn admit", "newborn admission","sick newborn","NB","detained NB",
                "detained newborn"
        };
        for (int i = 0; i < keywords.length; i++) {
            EXACT_KEYWORDS.add(keywords[i].toLowerCase());
            CONTAINS_KEYWORDS.add(keywords[i].toLowerCase());
        }
    }

    public Logger logger;

    public NewBornRequestOCRMapper(Logger logger) {
        this.logger = logger;
    }

    public MappingResult doCustomPredictionMapping(Map data, Long rootPipelineId) {
        logger.info("[RootPipelineID: " + rootPipelineId + "] Starting Newborn Request (Y/N) mapping");

        if (data == null) {
            logger.warn("[RootPipelineID: " + rootPipelineId + "] Input data map is null");
            data = new HashMap();
        }
        Object nbRequestOct = data.get("newborn_request_ocr");
        if (nbRequestOct instanceof List) {
            List objList = (List) nbRequestOct;

            for (int i = 0; i < objList.size(); i++) {
                Object obj = objList.get(i);
                if (obj instanceof PostProcessingExecutorInput) {
                    PostProcessingExecutorInput newBornRequestOcrInput = (PostProcessingExecutorInput) obj;

                    String newbornRequest = getStringValue(newBornRequestOcrInput);

                    boolean matchFound = isNewbornRequest(newbornRequest);

                    String result = matchFound ? "Y" : (newbornRequest.isEmpty() ? "" : "N");
                    newBornRequestOcrInput.setExtractedValue(result);

                    logger.info("[RootPipelineID: " + rootPipelineId +
                            "] newborn_request_ocr mapped to '" + result +
                            "' (original: '" + newbornRequest + "')");
                }
            }
        }
        return new MappingResult(data);
    }


    public String getStringValue(PostProcessingExecutorInput map) {
        String value = map.getExtractedValue();
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    public boolean isNewbornRequest(String input) {
        if (input == null || input.trim().length() == 0) {
            return false;
        }

        String trimmedLower = input.toLowerCase().trim();
        for (int i = 0; i < EXACT_KEYWORDS.size(); i++) {
            String keyword = (String) EXACT_KEYWORDS.get(i);
            if (trimmedLower.equals(keyword)) {
                return true;
            }
        }

        String normalized = input.toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();

        for (int i = 0; i < CONTAINS_KEYWORDS.size(); i++) {
            String keyword = (String) CONTAINS_KEYWORDS.get(i);
            if (normalized.indexOf(keyword) != -1) {
                return true;
            }
        }

        return false;
    }

    public static class MappingResult {
        public Map mappedData;

        public MappingResult(Map mappedData) {
            if (mappedData != null) {
                this.mappedData = mappedData;
            } else {
                this.mappedData = new HashMap();
            }
        }

        public Map getMappedData() {
            return mappedData;
        }
    }

    public static class PostProcessingExecutorInput {

        public Long tenantId;
        public double aggregatedScore;
        public double maskedScore;
        public String originId;
        public Integer paperNo;
        public String extractedValue;
        public double vqaScore;
        public Integer rank;
        public Integer sorItemAttributionId;
        public String sorItemName;
        public String documentId;
        public Long accTransactionId;
        public String label;
        public String sectionAlias;
        public Long score;
        public String bBox;
        public Long rootPipelineId;
        public Long frequency;
        public Long questionId;
        public Long synonymId;
        public String modelRegistry;
        public String encryptionPolicy;
        public String isEncrypted;
        public String lineItemType;

        public String getExtractedValue() { return extractedValue; }

        public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }
    }
}
