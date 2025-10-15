package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PostProcessingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class PostProcessingExecutorActionTest {

    @Test
    void execute() throws Exception {

        PostProcessingExecutor postProcessingExecutor = PostProcessingExecutor.builder()
                .name("Post Processing executor")
                .batchId("BATCH-4_0")
                .groupId("4")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT DISTINCT\n" +
                        "    a.tenant_id AS tenant_id,\n" +
                        "    a.confidence_score AS aggregated_score,\n" +
                        "    a.weight_score AS masked_score,\n" +
                        "    a.origin_id,\n" +
                        "    a.paper_no,\n" +
                        "    a.extracted_value,\n" +
                        "    b.vqa_score,\n" +
                        "    a.weight_rank AS rank,\n" +
                        "    smca.sor_item_attribution_id AS sor_item_attribution_id,\n" +
                        "    a.sor_item_name,\n" +
                        "    'TMP-AGD-001' AS documentId,\n" +
                        "    1 AS acc_transaction_id,\n" +
                        "    b.b_box,\n" +
                        "    a.root_pipeline_id AS root_pipeline_id,\n" +
                        "    a.freq AS frequency,\n" +
                        "    b.question_id,\n" +
                        "    b.synonym_id,\n" +
                        "    a.model_registry,\n" +
                        "    smca.is_encrypted,\n" +
                        "    smca.encryption_policy,\n" +
                        "    'single_value' AS line_item_type\n" +
                        "FROM voting.weighted_value a\n" +
                        "JOIN score.scalar_validation b \n" +
                        "    ON a.validation_id = b.validation_id\n" +
                        "JOIN macro.sor_meta_consolidated_audit smca \n" +
                        "    ON smca.tenant_id = b.tenant_id\n" +
                        "    AND smca.group_id = b.group_id\n" +
                        "    AND smca.synonym_id = b.synonym_id\n" +
                        "    AND smca.sor_item_name = b.sor_item_name\n" +
                        "JOIN sor_validation.sor_validation_payload_queue_archive st \n" +
                        "    ON st.origin_id = a.origin_id\n" +
                        "    AND st.batch_id = a.batch_id\n" +
                        "    AND st.tenant_id = b.tenant_id\n" +
                        "WHERE a.group_id = '4'\n" +
                        "    AND a.weight_rank = 1\n" +
                        "    AND a.tenant_id = 1\n" +
                        "    AND a.batch_id = 'BATCH-4_0'\n" +
                        "    AND smca.line_item_type <> 'multi_value';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "4");
        action.getContext().put("batch_id", "BATCH-4_0");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("outbound.mapper.bsh.class.order", "MedicaidMemberIdValidator");
        action.getContext().put("pipeline.end.to.end.encryption", "false");
        action.getContext().put("MedicaidMemberIdValidator", "package com.intics.inticsencryptionservice.validatorMayRelease.membermapper.v2;\n" +
                "\n" +
                "import org.slf4j.Logger;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.Hashtable;\n" +
                "import java.util.Iterator;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import java.util.regex.Matcher;\n" +
                "import java.util.regex.Pattern;\n" +
                "\n" +
                "public class MedicaidMemberIdValidator {\n" +
                "    private Long rootPipelineId;\n" +
                "\n" +
                "    private static final Pattern NINE_CHAR_ALPHANUMERIC = Pattern.compile(\"^[a-zA-Z0-9]{9}$\");\n" +
                "    private static final Pattern SEVEN_EIGHT_DIGITS = Pattern.compile(\"^[a-zA-Z0-9]{7,8}$\");\n" +
                "    private static final Pattern LESS_THAN_SEVEN_DIGITS = Pattern.compile(\"^[0-9]{1,6}$\");\n" +
                "    private static final Pattern FIRST_THREE_ALPHA_PREFIX = Pattern.compile(\"^[a-zA-Z]{3}(?:[^a-zA-Z].*|$)\");\n" +
                "    private static final Pattern FIRST_THREE_ALPHA_NUMERIC_PREFIX = Pattern.compile(\"^(?=.{0,2}[A-Za-z])(?=.{0,2}[0-9])[A-Za-z0-9]{3}.*\");\n" +
                "    private static final Pattern ALL_ALPHA = Pattern.compile(\"^[a-zA-Z]+$\");\n" +
                "    private static final Pattern UM_PATTERN = Pattern.compile(\"^[uU][mM][0-9]{8}$\");\n" +
                "    private static final Pattern UM_INVALID_PATTERN = Pattern.compile(\"^[uU][mM][0-9]*$\");\n" +
                "    private static final Pattern COMBINED_FIELD = Pattern.compile(\"([a-zA-Z0-9]+)(?:\\\\([^)]*\\\\))?\\\\s*[#/:,–|\\\\\\\\]\\\\s*([a-zA-Z0-9]+)(?:\\\\([^)]*\\\\))?\");\n" +
                "    private static final Pattern STRICT_ALPHANUMERIC = Pattern.compile(\"^[a-zA-Z0-9]+$\");\n" +
                "    private static final Pattern WITH_PARENTHESES = Pattern.compile(\"^([a-zA-Z0-9]+)(?:\\\\([^)]*\\\\))?$\");\n" +
                "    private static final Pattern LAST_ID = Pattern.compile(\"([a-zA-Z0-9]+)(?!.*[a-zA-Z0-9])\");\n" +
                "\n" +
                "    private Logger logger;\n" +
                "    private List authIdCandidates;\n" +
                "    private List additionalAuthCandidates;\n" +
                "\n" +
                "    public static class ItemData {\n" +
                "        private String extractedValue;\n" +
                "        private String bbox;\n" +
                "\n" +
                "        public ItemData() {}\n" +
                "\n" +
                "        public String getExtractedValue() {\n" +
                "            return extractedValue;\n" +
                "        }\n" +
                "\n" +
                "        public void setExtractedValue(String extractedValue) {\n" +
                "            this.extractedValue = extractedValue;\n" +
                "        }\n" +
                "\n" +
                "        public String getBbox() {\n" +
                "            return bbox;\n" +
                "        }\n" +
                "\n" +
                "        public void setBbox(String bbox) {\n" +
                "            this.bbox = bbox;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static class Candidate {\n" +
                "        String value;\n" +
                "        String bbox;\n" +
                "\n" +
                "        public Candidate(String value, String bbox) {\n" +
                "            this.value = value;\n" +
                "            this.bbox = bbox;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public MedicaidMemberIdValidator(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "        this.authIdCandidates = new ArrayList();\n" +
                "        this.additionalAuthCandidates = new ArrayList();\n" +
                "    }\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
                "        this.rootPipelineId = rootPipelineId;\n" +
                "        this.authIdCandidates.clear();\n" +
                "        this.additionalAuthCandidates.clear();\n" +
                "        String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Entered doCustomPredictionMapping method\";\n" +
                "        logger.info(logMsg);\n" +
                "\n" +
                "        if (predictionKeyMap == null) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Input map is null for medicaid member id validator\";\n" +
                "            logger.error(logMsg);\n" +
                "            return new MappingResult(predictionKeyMap);\n" +
                "        }\n" +
                "\n" +
                "        Map resultMap = new Hashtable(predictionKeyMap);\n" +
                "        validateAndMapIds(resultMap);\n" +
                "        mapAuthIdAndAdditionalProperties(resultMap, predictionKeyMap);\n" +
                "\n" +
                "        return new MappingResult(resultMap);\n" +
                "    }\n" +
                "\n" +
                "    private String getExtractedValue(Object dataObj) {\n" +
                "        if (dataObj == null) return \"\";\n" +
                "        try {\n" +
                "            return (String) dataObj.getClass().getMethod(\"getExtractedValue\").invoke(dataObj);\n" +
                "        } catch (Exception e) {\n" +
                "            logger.error(\"[RootPipelineID: \" + rootPipelineId + \"] Failed to get extractedValue from dataObj\", e);\n" +
                "            return \"\";\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private String getBbox(Object dataObj) {\n" +
                "        if (dataObj == null) return \"{}\";\n" +
                "        try {\n" +
                "            return (String) dataObj.getClass().getMethod(\"getBbox\").invoke(dataObj);\n" +
                "        } catch (Exception e) {\n" +
                "            logger.error(\"[RootPipelineID: \" + rootPipelineId + \"] Failed to get bbox from dataObj\", e);\n" +
                "            return \"{}\";\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void setExtractedValue(Object dataObj, String value) {\n" +
                "        if (dataObj == null) return;\n" +
                "        try {\n" +
                "            dataObj.getClass().getMethod(\"setExtractedValue\", String.class).invoke(dataObj, value);\n" +
                "        } catch (Exception e) {\n" +
                "            logger.error(\"[RootPipelineID: \" + rootPipelineId + \"] Failed to set extractedValue on dataObj\", e);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void setBbox(Object dataObj, String bbox) {\n" +
                "        if (dataObj == null) return;\n" +
                "        try {\n" +
                "            dataObj.getClass().getMethod(\"setBbox\", String.class).invoke(dataObj, bbox);\n" +
                "        } catch (Exception e) {\n" +
                "            logger.error(\"[RootPipelineID: \" + rootPipelineId + \"] Failed to set bbox on dataObj\", e);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void validateAndMapIds(Map resultMap) {\n" +
                "        String[] idFields = {\"member_id\", \"medicaid_id\"};\n" +
                "        Map validatedIds = new Hashtable();\n" +
                "\n" +
                "        for (String field : idFields) {\n" +
                "            Object dataObj = resultMap.get(field);\n" +
                "            String value = getExtractedValue(dataObj);\n" +
                "            String bbox = getBbox(dataObj);\n" +
                "\n" +
                "            if (value != null && value.trim().length() != 0) {\n" +
                "                value = cleanString(value); // Clean whitespace and non-alphanumeric characters\n" +
                "                Matcher combinedMatcher = COMBINED_FIELD.matcher(value);\n" +
                "                if (combinedMatcher.find()) {\n" +
                "                    String firstPart = combinedMatcher.group(1);\n" +
                "                    String secondPart = combinedMatcher.group(2);\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Combined field in \" + field + \" split into two parts\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    assignCombinedValues(validatedIds, firstPart, secondPart, field, bbox);\n" +
                "                } else {\n" +
                "                    // Apply WITH_PARENTHESES for single values\n" +
                "                    Matcher parenMatcher = WITH_PARENTHESES.matcher(value);\n" +
                "                    if (parenMatcher.find()) {\n" +
                "                        value = parenMatcher.group(1);\n" +
                "                        String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Stripped parentheses in field \" + field;\n" +
                "                        logger.info(logMsg);\n" +
                "                    } else {\n" +
                "                        // Try to extract the last valid ID\n" +
                "                        Matcher lastIdMatcher = LAST_ID.matcher(value);\n" +
                "                        if (lastIdMatcher.find()) {\n" +
                "                            value = lastIdMatcher.group(1);\n" +
                "                            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Extracted last ID in field \" + field;\n" +
                "                            logger.info(logMsg);\n" +
                "                        }\n" +
                "                    }\n" +
                "                    assignSingleValue(validatedIds, value, field, bbox);\n" +
                "                }\n" +
                "            } else {\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + field + \" is null or empty\";\n" +
                "                logger.info(logMsg);\n" +
                "                setExtractedValue(dataObj, \"\");\n" +
                "                setBbox(dataObj, \"{}\");\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Assign validated IDs to resultMap\n" +
                "        assignValidatedToResult(\"member_id\", validatedIds, resultMap);\n" +
                "        assignValidatedToResult(\"medicaid_id\", validatedIds, resultMap);\n" +
                "    }\n" +
                "\n" +
                "    private void assignValidatedToResult(String field, Map validatedIds, Map resultMap) {\n" +
                "        ItemData validatedData = (ItemData) validatedIds.get(field);\n" +
                "        if (validatedData != null) {\n" +
                "            String newValue = validatedData.getExtractedValue();\n" +
                "            String newBbox = validatedData.getBbox();\n" +
                "            Object targetData = resultMap.get(field);\n" +
                "            if (targetData != null) {\n" +
                "                setExtractedValue(targetData, newValue);\n" +
                "                setBbox(targetData, newBbox);\n" +
                "            } else {\n" +
                "                resultMap.put(field, validatedData);\n" +
                "            }\n" +
                "        } else {\n" +
                "            Object targetData = resultMap.get(field);\n" +
                "            if (targetData != null) {\n" +
                "                setExtractedValue(targetData, \"\");\n" +
                "                setBbox(targetData, \"{}\");\n" +
                "            } else {\n" +
                "                ItemData newData = new ItemData();\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(\"{}\");\n" +
                "                resultMap.put(field, newData);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void assignSingleValue(Map validatedIds, String value, String originalField, String bbox) {\n" +
                "        if (value.length() == 0) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" is empty after cleaning\";\n" +
                "            logger.info(logMsg);\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        if (!STRICT_ALPHANUMERIC.matcher(value).matches()) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" contains non-alphanumeric characters\";\n" +
                "            logger.info(logMsg);\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        String assignedField = determineIdType(value);\n" +
                "        String logMsg;\n" +
                "\n" +
                "        if (\"auth_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(value, bbox));\n" +
                "        } else if (\"additional_auth_properties\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(value, bbox));\n" +
                "        } else if (\"member_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" validated as member_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(value);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" validated as medicaid_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(value);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"medicaid_id\", validatedData);\n" +
                "        } else {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" does not match any ID criteria\";\n" +
                "            logger.info(logMsg);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void assignCombinedValues(Map validatedIds, String firstPart, String secondPart, String originalField, String bbox) {\n" +
                "        firstPart = cleanString(firstPart);\n" +
                "        secondPart = cleanString(secondPart);\n" +
                "\n" +
                "        // Strip parentheses from both parts\n" +
                "        Matcher parenMatcher = WITH_PARENTHESES.matcher(firstPart);\n" +
                "        if (parenMatcher.find()) {\n" +
                "            firstPart = parenMatcher.group(1);\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Stripped parentheses from first part in field \" + originalField;\n" +
                "            logger.info(logMsg);\n" +
                "        }\n" +
                "\n" +
                "        parenMatcher = WITH_PARENTHESES.matcher(secondPart);\n" +
                "        if (parenMatcher.find()) {\n" +
                "            secondPart = parenMatcher.group(1);\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Stripped parentheses from second part in field \" + originalField;\n" +
                "            logger.info(logMsg);\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        String firstAssignedField = determineIdType(firstPart);\n" +
                "        String secondAssignedField = determineIdType(secondPart);\n" +
                "\n" +
                "        if (firstPart.length() == 0 || ALL_ALPHA.matcher(firstPart).matches() || !STRICT_ALPHANUMERIC.matcher(firstPart).matches()) {\n" +
                "            String reason = firstPart.length() == 0 ? \"is empty after cleaning\" : !STRICT_ALPHANUMERIC.matcher(firstPart).matches() ? \"contains non-alphanumeric characters\" : \"contains only alphabets\";\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" \" + reason;\n" +
                "            logger.info(logMsg);\n" +
                "            firstAssignedField = null;\n" +
                "        }\n" +
                "        if (secondPart.length() == 0 || ALL_ALPHA.matcher(secondPart).matches() || !STRICT_ALPHANUMERIC.matcher(secondPart).matches()) {\n" +
                "            String reason = secondPart.length() == 0 ? \"is empty after cleaning\" : !STRICT_ALPHANUMERIC.matcher(secondPart).matches() ? \"contains non-alphanumeric characters\" : \"contains only alphabets\";\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" \" + reason;\n" +
                "            logger.info(logMsg);\n" +
                "            secondAssignedField = null;\n" +
                "        }\n" +
                "\n" +
                "        if (\"auth_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(firstPart, bbox));\n" +
                "        } else if (\"additional_auth_properties\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(firstPart, bbox));\n" +
                "        } else if (\"member_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" assigned to member_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(firstPart);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" assigned to medicaid_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(firstPart);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"medicaid_id\", validatedData);\n" +
                "        }\n" +
                "\n" +
                "        if (\"auth_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(secondPart, bbox));\n" +
                "        } else if (\"additional_auth_properties\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(secondPart, bbox));\n" +
                "        } else if (\"member_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" assigned to member_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(secondPart);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" assigned to medicaid_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            ItemData validatedData = new ItemData();\n" +
                "            validatedData.setExtractedValue(secondPart);\n" +
                "            validatedData.setBbox(bbox);\n" +
                "            validatedIds.put(\"medicaid_id\", validatedData);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private String determineIdType(String value) {\n" +
                "        if (value == null || value.length() == 0) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value rejected as it is null or empty\");\n" +
                "            return null;\n" +
                "        } else if (ALL_ALPHA.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value rejected as it contains only alphabets\");\n" +
                "            return null;\n" +
                "        }     // Regex: matches UM/um/Um/uM followed by exactly 8 digits\n" +
                "        String UM_REGEX = \"^[Uu][Mm]\\\\d{8}$\";\n" +
                "        String UM_PREFIX_REGEX = \"^[Uu][Mm].*\";\n" +
                "\n" +
                "        // Check if starts with any case variant of UM\n" +
                "        if (value.matches(UM_PREFIX_REGEX)) {\n" +
                "            if (value.matches(UM_REGEX)) {\n" +
                "                logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value starts with UM variant and valid (8 digits) → auth_id\");\n" +
                "                return \"auth_id\";\n" +
                "            } else {\n" +
                "                logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value starts with UM variant but invalid format → additional_auth_properties\");\n" +
                "                return \"additional_auth_properties\";\n" +
                "            }\n" +
                "        }\n" +
                "        else if (NINE_CHAR_ALPHANUMERIC.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value validated as member_id\");\n" +
                "            return \"member_id\";\n" +
                "        } else if (SEVEN_EIGHT_DIGITS.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value validated as medicaid_id\");\n" +
                "            return \"medicaid_id\";\n" +
                "        } else if (LESS_THAN_SEVEN_DIGITS.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value rejected as it has less than 7 digits\");\n" +
                "            return null;\n" +
                "        } else if (value.length() > 9 && FIRST_THREE_ALPHA_PREFIX.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value validated as member_id\");\n" +
                "            return \"member_id\";\n" +
                "        } else if (value.length() > 9 && FIRST_THREE_ALPHA_NUMERIC_PREFIX.matcher(value).matches()) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value validated as member_id\");\n" +
                "            return \"member_id\";\n" +
                "        } else {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Value validated as medicaid_id\");\n" +
                "            return \"medicaid_id\";\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void mapAuthIdAndAdditionalProperties(Map resultMap, Map inputMap) {\n" +
                "        Object authDataObj = inputMap.get(\"auth_id\");\n" +
                "        String rawAuthId = getExtractedValue(authDataObj);\n" +
                "        String authBbox = getBbox(authDataObj);\n" +
                "        String authId = null;\n" +
                "        if (rawAuthId != null && rawAuthId.trim().length() != 0) {\n" +
                "            authId = cleanString(rawAuthId);\n" +
                "            if (UM_PATTERN.matcher(authId).matches()) {\n" +
                "                authIdCandidates.add(new Candidate(authId, authBbox));\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] auth_id field validated as auth_id\";\n" +
                "                logger.info(logMsg);\n" +
                "            } else if (UM_INVALID_PATTERN.matcher(authId).matches()) {\n" +
                "                additionalAuthCandidates.add(new Candidate(authId, authBbox));\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] auth_id field validated as additional_auth_properties\";\n" +
                "                logger.info(logMsg);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        List allAdditionalAuth = new ArrayList();\n" +
                "        for (int j = 0; j < additionalAuthCandidates.size(); j++) {\n" +
                "            Candidate c = (Candidate) additionalAuthCandidates.get(j);\n" +
                "            allAdditionalAuth.add(c.value);\n" +
                "        }\n" +
                "        if (!authIdCandidates.isEmpty()) {\n" +
                "            Candidate firstCandidate = (Candidate) authIdCandidates.get(0);\n" +
                "            String firstAuthId = firstCandidate.value;\n" +
                "            String firstBbox = firstCandidate.bbox;\n" +
                "            Object authResultData = resultMap.get(\"auth_id\");\n" +
                "            if (authResultData != null) {\n" +
                "                setExtractedValue(authResultData, firstAuthId);\n" +
                "                setBbox(authResultData, firstBbox);\n" +
                "            } else {\n" +
                "                ItemData newData = new ItemData();\n" +
                "                newData.setExtractedValue(firstAuthId);\n" +
                "                newData.setBbox(firstBbox);\n" +
                "                resultMap.put(\"auth_id\", newData);\n" +
                "            }\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Assigned first valid UM value to auth_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            if (authIdCandidates.size() > 1) {\n" +
                "                for (int i = 1; i < authIdCandidates.size(); i++) {\n" +
                "                    Candidate c = (Candidate) authIdCandidates.get(i);\n" +
                "                    allAdditionalAuth.add(c.value);\n" +
                "                }\n" +
                "            }\n" +
                "        } else {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] No valid UM values found, setting auth_id to empty\";\n" +
                "            logger.info(logMsg);\n" +
                "            Object authResultData = resultMap.get(\"auth_id\");\n" +
                "            if (authResultData != null) {\n" +
                "                setExtractedValue(authResultData, \"\");\n" +
                "                setBbox(authResultData, \"{}\");\n" +
                "            } else {\n" +
                "                ItemData newData = new ItemData();\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(\"{}\");\n" +
                "                resultMap.put(\"auth_id\", newData);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (!allAdditionalAuth.isEmpty()) {\n" +
                "            StringBuffer additionalAuthIds = new StringBuffer();\n" +
                "            Iterator it = allAdditionalAuth.iterator();\n" +
                "            while (it.hasNext()) {\n" +
                "                additionalAuthIds.append((String) it.next());\n" +
                "                if (it.hasNext()) {\n" +
                "                    additionalAuthIds.append(\",\");\n" +
                "                }\n" +
                "            }\n" +
                "            ItemData addData = (ItemData) resultMap.get(\"additional_auth_properties\");\n" +
                "            String additionalValue = additionalAuthIds.toString();\n" +
                "            if (addData != null) {\n" +
                "                addData.setExtractedValue(additionalValue);\n" +
                "                addData.setBbox(\"{}\");\n" +
                "            } else {\n" +
                "                ItemData newData = new ItemData();\n" +
                "                newData.setExtractedValue(additionalValue);\n" +
                "                newData.setBbox(\"{}\");\n" +
                "                resultMap.put(\"additional_auth_properties\", newData);\n" +
                "            }\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Appended UM values to additional_auth_properties\";\n" +
                "            logger.info(logMsg);\n" +
                "        } else {\n" +
                "            Object addDataObj = resultMap.get(\"additional_auth_properties\");\n" +
                "            if (addDataObj != null) {\n" +
                "                setExtractedValue(addDataObj, \"\");\n" +
                "                setBbox(addDataObj, \"{}\");\n" +
                "            } else {\n" +
                "                ItemData newData = new ItemData();\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(\"{}\");\n" +
                "                resultMap.put(\"additional_auth_properties\", newData);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private String cleanString(String str) {\n" +
                "        if (str == null || str.trim().length() == 0) {\n" +
                "            return \"\";\n" +
                "        }\n" +
                "        // Remove non-alphanumeric characters except for allowed separators and parentheses\n" +
                "        return str.trim().replaceAll(\"[^a-zA-Z0-9/:,–|\\\\\\\\()]\", \"\");\n" +
                "    }\n" +
                "\n" +
                "    public static class MappingResult {\n" +
                "        private Map mappedData;\n" +
                "\n" +
                "        public MappingResult(Map mappedData) {\n" +
                "            this.mappedData = mappedData;\n" +
                "        }\n" +
                "\n" +
                "        public Map getMappedData() {\n" +
                "            return mappedData;\n" +
                "        }\n" +
                "    }\n" +
                "}");
        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";

        action.setRootPipelineId(929L);
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url", encryptionUrl);
        action.getContext().put("protegrity.dec.api.url", decryptionUrl);

        PostProcessingExecutorAction postProcessingExecutorAction =
                new PostProcessingExecutorAction(action, log, postProcessingExecutor);

        // Execute safely
        postProcessingExecutorAction.execute();
    }
}