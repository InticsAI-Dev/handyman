package com.intics.script.MultiLineItem;

import org.slf4j.Logger;

import java.util.*;

public class FaxReportProcessor {

    private Logger logger;

    public FaxReportProcessor(Logger logger) { this.logger = logger; }

    private static final String[] VALID_KEYWORDS = {
            "ADT",
            "Summary Report",
            "LAD Report",
            "Last approved day Report",
            "census",
            "Notification of Admission report",
            "NOA report"
    };


    private static final String[] GLUED_KEYWORDS = {
            "ADT",
            "SUMMARYREPORT",
            "LADREPORT",
            "LASTAPPROVEDDAYREPORT",
            "CENSUS",
            "NOTIFICATIONOFADMISSIONREPORT",
            "NOAREPORT"
    };

    private static final Set VALID_KEYWORDS_SET = new HashSet();

    static {
        for (int i = 0; i < VALID_KEYWORDS.length; i++) {
            VALID_KEYWORDS_SET.add(VALID_KEYWORDS[i].toLowerCase());
        }
        for (int i = 0; i < GLUED_KEYWORDS.length; i++) {
            VALID_KEYWORDS_SET.add(GLUED_KEYWORDS[i].toLowerCase());
        }
    }

    public MappingResult doCustomPredictionMapping(Map data, Long rootPipelineId) {
        logger.info("[RootPipelineID: " + rootPipelineId + "] Starting Fax Report validation process");

        if (data == null) {
            logger.info("[RootPipelineID: " + rootPipelineId + "] Input map is null");
            Map result = new HashMap();
            PostProcessingExecutorInput faxReport = new PostProcessingExecutorInput();
            faxReport.setExtractedValue("N");
            List faxReportList = new ArrayList();
            faxReportList.add(faxReport);
            result.put("fax_report", faxReportList);
            return new MappingResult(result);
        }
        processFaxReport(data, rootPipelineId);
        return new MappingResult(data);
    }

    private void processFaxReport(Map data, Long rootPipelineId) {
        Object faxReportObj = data.get("fax_report");
        if (faxReportObj instanceof List) {
            List faxReportList = (List) faxReportObj;
            for (int i = 0; i < faxReportList.size(); i++) {
                Object obj = faxReportList.get(i);
                if (obj instanceof PostProcessingExecutorInput) {
                    PostProcessingExecutorInput faxReport = (PostProcessingExecutorInput) obj;
                    String value = faxReportValidation(faxReport.getExtractedValue(), rootPipelineId);
                    faxReport.setExtractedValue(value);
                }
            }
        }
    }

    private String faxReportValidation(String faxReport, Long rootPipelineId) {
        if (!faxReport.equals("")) {
            boolean isValid = containsExactKeyword(faxReport, rootPipelineId);

            if (isValid) {
                logger.info("[RootPipelineID: " + rootPipelineId + "] Valid fax_report found");
                faxReport = "Y";
            } else {
                logger.info("[RootPipelineID: " + rootPipelineId + "] No valid fax_report found");
                faxReport = "N";
            }
        } else {
            logger.info("[RootPipelineID: " + rootPipelineId + "] Empty or null fax_report");
            faxReport = "N";
        }
        return faxReport;
    }
    public boolean containsExactKeyword(String value, Long rootPipelineId) {
        if (isEmpty(value, rootPipelineId)) {
            return false;
        }

        String cleanedInput = cleanAndNormalize(value);
        logger.info("[RootPipelineID: " + rootPipelineId + "] Original: '" + value + "', Cleaned: '" + cleanedInput + "'");

        if (cleanedInput.isEmpty()) {
            return false;
        }


        String[] inputWords = cleanedInput.split("\\s+");
        for (String word : inputWords) {
            String lowerWord = word.toLowerCase();
            if (VALID_KEYWORDS_SET.contains(lowerWord)) {
                logger.info("[RootPipelineID: " + rootPipelineId + "] Exact keyword '" + word + "' found");
                return true;
            }
        }


        int n = inputWords.length;
        for (int i = 0; i < n; i++) {
            StringBuilder concat = new StringBuilder();
            for (int j = i; j < n && j < i + 5; j++) {
                concat.append(inputWords[j]);
                String concatStr = concat.toString();
                String lowerConcat = concatStr.toLowerCase();
                if (VALID_KEYWORDS_SET.contains(lowerConcat)) {
                    logger.info("[RootPipelineID: " + rootPipelineId + "] Glued keyword '" + concatStr + "' found");
                    return true;
                }
            }
        }


        String lowerCleaned = cleanedInput.toLowerCase();
        String gluedFull = cleanedInput.replaceAll("\\s+", "").toLowerCase();
        if (VALID_KEYWORDS_SET.contains(lowerCleaned) || VALID_KEYWORDS_SET.contains(gluedFull)) {
            logger.info("[RootPipelineID: " + rootPipelineId + "] Full match keyword found");
            return true;
        }

        logger.info("[RootPipelineID: " + rootPipelineId + "] No exact keywords found");
        return false;
    }

    public boolean isEmpty(String value, Long rootPipelineId) {
        boolean result = value == null || value.trim().equals("");
        logger.info("[RootPipelineID: " + rootPipelineId + "] Empty check performed. Result: " + result);
        return result;
    }

    private String cleanAndNormalize(String input) {
        if (input == null) return "";
        String upperInput = input.toUpperCase();
        StringBuilder cleaned = new StringBuilder();
        for (char c : upperInput.toCharArray()) {
            if (Character.isLetter(c) || Character.isWhitespace(c)) {
                cleaned.append(c);
            }
        }
        return cleaned.toString().replaceAll("\\s+", " ").trim();
    }

    public static class MappingResult {
        private Map mappedData;

        public MappingResult(Map mappedData) {
            this.mappedData = mappedData != null ? mappedData : new HashMap();
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