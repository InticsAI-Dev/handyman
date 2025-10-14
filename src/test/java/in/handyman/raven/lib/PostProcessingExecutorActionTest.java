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
                "import in.handyman.raven.lib.model.SharedItemData;\n" +  // New import for shared ItemData\n" +
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
                "    // Removed inner ItemData - use SharedItemData\n" +
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
                "    private void validateAndMapIds(Map resultMap) {\n" +
                "        String[] idFields = {\"member_id\", \"medicaid_id\"};\n" +
                "        Map validatedIds = new Hashtable();\n" +
                "\n" +
                "        for (String field : idFields) {\n" +
                "            SharedItemData data = (SharedItemData) resultMap.get(field);  // Use shared type\n" +
                "            String value = data != null ? data.getExtractedValue() : null;\n" +
                "            String originalBbox = data != null ? data.getBbox() : null;  // Allow null\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: validateAndMapIds - field=\" + field + \", originalBbox='\" + (originalBbox == null ? \"NULL\" : originalBbox) + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "\n" +
                "            if (value != null && value.trim().length() != 0) {\n" +
                "                value = cleanString(value); // Clean whitespace and non-alphanumeric characters\n" +
                "                Matcher combinedMatcher = COMBINED_FIELD.matcher(value);\n" +
                "                if (combinedMatcher.find()) {\n" +
                "                    String firstPart = combinedMatcher.group(1);\n" +
                "                    String secondPart = combinedMatcher.group(2);\n" +
                "                    String logMsg2 = \"[RootPipelineID: \" + rootPipelineId + \"] Combined field in \" + field + \" split into two parts\";\n" +
                "                    logger.info(logMsg2);\n" +
                "                    assignCombinedValues(validatedIds, firstPart, secondPart, field, data, originalBbox);\n" +
                "                } else {\n" +
                "                    // Apply WITH_PARENTHESES for single values\n" +
                "                    Matcher parenMatcher = WITH_PARENTHESES.matcher(value);\n" +
                "                    if (parenMatcher.find()) {\n" +
                "                        value = parenMatcher.group(1);\n" +
                "                        String logMsg2 = \"[RootPipelineID: \" + rootPipelineId + \"] Stripped parentheses in field \" + field;\n" +
                "                        logger.info(logMsg2);\n" +
                "                    } else {\n" +
                "                        // Try to extract the last valid ID\n" +
                "                        Matcher lastIdMatcher = LAST_ID.matcher(value);\n" +
                "                        if (lastIdMatcher.find()) {\n" +
                "                            value = lastIdMatcher.group(1);\n" +
                "                            String logMsg2 = \"[RootPipelineID: \" + rootPipelineId + \"] Extracted last ID in field \" + field;\n" +
                "                            logger.info(logMsg2);\n" +
                "                        }\n" +
                "                    }\n" +
                "                    assignSingleValue(validatedIds, value, field, data, originalBbox);\n" +
                "                }\n" +
                "            } else {\n" +
                "                String logMsg2 = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + field + \" is null or empty\";\n" +
                "                logger.info(logMsg2);\n" +
                "                if (data != null) {\n" +
                "                    data.setExtractedValue(\"\");  // Clear value but preserve bbox\n" +
                "                    // Do NOT set bbox to \"{}\" - keep original\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Assign validated IDs to resultMap\n" +
                "        assignValidatedToResult(\"member_id\", validatedIds, resultMap);\n" +
                "        assignValidatedToResult(\"medicaid_id\", validatedIds, resultMap);\n" +
                "    }\n" +
                "\n" +
                "    private void assignValidatedToResult(String field, Map validatedIds, Map resultMap) {\n" +
                "        SharedItemData validatedData = (SharedItemData) validatedIds.get(field);  // Use shared\n" +
                "        SharedItemData targetData = (SharedItemData) resultMap.get(field);  // Use shared\n" +
                "        String originalBbox = targetData != null ? targetData.getBbox() : null;  // Allow null\n" +
                "\n" +
                "        if (validatedData != null) {\n" +
                "            String newValue = validatedData.getExtractedValue();\n" +
                "            String newBbox = validatedData.getBbox();\n" +
                "            if (targetData != null) {\n" +
                "                targetData.setExtractedValue(newValue);\n" +
                "                // Preserve bbox if validated didn't change it\n" +
                "                targetData.setBbox(newBbox != null ? newBbox : originalBbox);\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: assignValidatedToResult - field=\" + field + \", final bbox='\" + (targetData != null ? targetData.getBbox() : \"NULL\") + \"', preserved?=\" + (newBbox != null ? \"yes\" : \"no\");\n" +
                "                logger.info(logMsg);\n" +
                "                logger.info(\"[RootPipelineID: {}] Updated {} with bbox preserved: '{}'\", rootPipelineId, field, targetData.getBbox());\n" +
                "            } else {\n" +
                "                resultMap.put(field, validatedData);\n" +
                "            }\n" +
                "        } else {\n" +
                "            // No validated data - clear value but preserve bbox\n" +
                "            if (targetData != null) {\n" +
                "                targetData.setExtractedValue(\"\");\n" +
                "                // Keep existing bbox\n" +
                "                logger.info(\"[RootPipelineID: {}] Cleared {} value but preserved bbox: '{}'\", rootPipelineId, field, targetData.getBbox());\n" +
                "            } else {\n" +
                "                SharedItemData newData = new SharedItemData();  // Use shared\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(originalBbox);  // Use original if available\n" +
                "                resultMap.put(field, newData);\n" +
                "                logger.info(\"[RootPipelineID: {}] Created empty {} with bbox: '{}'\", rootPipelineId, field, newData.getBbox());\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void assignSingleValue(Map validatedIds, String value, String originalField, SharedItemData data, String originalBbox) {\n" +
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
                "        String bboxToSet = data != null ? data.getBbox() : originalBbox;\n" +
                "        String traceLog = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: assignSingleValue - field=\" + originalField + \", bbox to set='\" + (bboxToSet == null ? \"NULL\" : bboxToSet) + \"'\";\n" +
                "        logger.info(traceLog);\n" +
                "\n" +
                "        if (\"auth_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(value, bboxToSet));  // Use preserved bbox\n" +
                "        } else if (\"additional_auth_properties\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(value, bboxToSet));\n" +
                "        } else if (\"member_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" validated as member_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(value);\n" +
                "            validatedData.setBbox(bboxToSet);  // Preserve\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(assignedField)) {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" validated as medicaid_id\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(value);\n" +
                "            validatedData.setBbox(bboxToSet);\n" +
                "            validatedIds.put(\"medicaid_id\", validatedData);\n" +
                "        } else {\n" +
                "            logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Field \" + originalField + \" does not match any ID criteria\";\n" +
                "            logger.info(logMsg);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void assignCombinedValues(Map validatedIds, String firstPart, String secondPart, String originalField, SharedItemData data, String originalBbox) {\n" +
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
                "        String preservedBbox = data != null ? data.getBbox() : originalBbox;\n" +
                "\n" +
                "        // Handle first part\n" +
                "        if (\"auth_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(firstPart, preservedBbox));\n" +
                "        } else if (\"additional_auth_properties\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(firstPart, preservedBbox));\n" +
                "        } else if (\"member_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" assigned to member_id with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(firstPart);\n" +
                "            validatedData.setBbox(preservedBbox);\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(firstAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] First part of combined field in \" + originalField + \" assigned to medicaid_id with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(firstPart);\n" +
                "            validatedData.setBbox(preservedBbox);\n" +
                "            validatedIds.put(\"medicaid_id\", validatedData);\n" +
                "        }\n" +
                "\n" +
                "        // Handle second part\n" +
                "        if (\"auth_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" starts with UM and has 8 digits, adding to auth_id candidates with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            authIdCandidates.add(new Candidate(secondPart, preservedBbox));\n" +
                "        } else if (\"additional_auth_properties\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" starts with UM but does not have 8 digits, adding to additional_auth_properties candidates with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            additionalAuthCandidates.add(new Candidate(secondPart, preservedBbox));\n" +
                "        } else if (\"member_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" assigned to member_id with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(secondPart);\n" +
                "            validatedData.setBbox(preservedBbox);\n" +
                "            validatedIds.put(\"member_id\", validatedData);\n" +
                "        } else if (\"medicaid_id\".equals(secondAssignedField)) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Second part of combined field in \" + originalField + \" assigned to medicaid_id with bbox: '\" + preservedBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData validatedData = new SharedItemData();  // Use shared\n" +
                "            validatedData.setExtractedValue(secondPart);\n" +
                "            validatedData.setBbox(preservedBbox);\n" +
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
                "        SharedItemData authDataObj = (SharedItemData) inputMap.get(\"auth_id\");  // Use shared\n" +
                "        String rawAuthId = authDataObj != null ? authDataObj.getExtractedValue() : null;\n" +
                "        String originalAuthBbox = authDataObj != null ? authDataObj.getBbox() : null;  // Allow null\n" +
                "        String authId = null;\n" +
                "        if (rawAuthId != null && rawAuthId.trim().length() != 0) {\n" +
                "            authId = cleanString(rawAuthId);\n" +
                "            if (UM_PATTERN.matcher(authId).matches()) {\n" +
                "                authIdCandidates.add(new Candidate(authId, originalAuthBbox));\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] auth_id field validated as auth_id with bbox: '\" + originalAuthBbox + \"'\";\n" +
                "                logger.info(logMsg);\n" +
                "            } else if (UM_INVALID_PATTERN.matcher(authId).matches()) {\n" +
                "                additionalAuthCandidates.add(new Candidate(authId, originalAuthBbox));\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] auth_id field validated as additional_auth_properties with bbox: '\" + originalAuthBbox + \"'\";\n" +
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
                "            String firstBbox = firstCandidate.bbox;  // From candidate (preserved)\n" +
                "            SharedItemData authResultData = (SharedItemData) resultMap.get(\"auth_id\");  // Use shared\n" +
                "            if (authResultData != null) {\n" +
                "                authResultData.setExtractedValue(firstAuthId);\n" +
                "                authResultData.setBbox(firstBbox);  // Use preserved\n" +
                "            } else {\n" +
                "                SharedItemData newData = new SharedItemData();  // Use shared\n" +
                "                newData.setExtractedValue(firstAuthId);\n" +
                "                newData.setBbox(firstBbox);\n" +
                "                resultMap.put(\"auth_id\", newData);\n" +
                "            }\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Assigned first valid UM value to auth_id with bbox: '\" + firstBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            String traceLog = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: mapAuth... - auth_id bbox set to '\" + (authResultData != null ? authResultData.getBbox() : \"NULL\") + \"'\";\n" +
                "            logger.info(traceLog);\n" +
                "            if (authIdCandidates.size() > 1) {\n" +
                "                for (int i = 1; i < authIdCandidates.size(); i++) {\n" +
                "                    Candidate c = (Candidate) authIdCandidates.get(i);\n" +
                "                    allAdditionalAuth.add(c.value);\n" +
                "                }\n" +
                "            }\n" +
                "        } else {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] No valid UM values found, clearing auth_id but preserving bbox: '\" + originalAuthBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            SharedItemData authResultData = (SharedItemData) resultMap.get(\"auth_id\");  // Use shared\n" +
                "            if (authResultData != null) {\n" +
                "                authResultData.setExtractedValue(\"\");  // Clear value\n" +
                "                authResultData.setBbox(originalAuthBbox);  // Preserve!\n" +
                "            } else {\n" +
                "                SharedItemData newData = new SharedItemData();  // Use shared\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(originalAuthBbox);\n" +
                "                resultMap.put(\"auth_id\", newData);\n" +
                "            }\n" +
                "            String traceLog = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: mapAuth... - auth_id bbox set to '\" + originalAuthBbox + \"'\";\n" +
                "            logger.info(traceLog);\n" +
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
                "            SharedItemData addData = (SharedItemData) resultMap.get(\"additional_auth_properties\");  // Use shared\n" +
                "            String additionalValue = additionalAuthIds.toString();\n" +
                "            String addBbox = originalAuthBbox;  // Preserve from original\n" +
                "            if (addData != null) {\n" +
                "                addData.setExtractedValue(additionalValue);\n" +
                "                addData.setBbox(addBbox);\n" +
                "            } else {\n" +
                "                SharedItemData newData = new SharedItemData();  // Use shared\n" +
                "                newData.setExtractedValue(additionalValue);\n" +
                "                newData.setBbox(addBbox);\n" +
                "                resultMap.put(\"additional_auth_properties\", newData);\n" +
                "            }\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Appended UM values to additional_auth_properties with bbox: '\" + addBbox + \"'\";\n" +
                "            logger.info(logMsg);\n" +
                "            String traceLog = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: mapAuth... - additional_auth_properties bbox set to '\" + addBbox + \"'\";\n" +
                "            logger.info(traceLog);\n" +
                "        } else {\n" +
                "            // For empty additional, preserve if exists\n" +
                "            SharedItemData addData = (SharedItemData) resultMap.get(\"additional_auth_properties\");  // Use shared\n" +
                "            if (addData != null) {\n" +
                "                addData.setExtractedValue(\"\");\n" +
                "                // Keep existing bbox\n" +
                "            } else {\n" +
                "                SharedItemData newData = new SharedItemData();  // Use shared\n" +
                "                newData.setExtractedValue(\"\");\n" +
                "                newData.setBbox(originalAuthBbox);  // From auth original\n" +
                "                resultMap.put(\"additional_auth_properties\", newData);\n" +
                "            }\n" +
                "            String traceLog = \"[RootPipelineID: \" + rootPipelineId + \"] BBOX-TRACE: mapAuth... - additional_auth_properties bbox set to '\" + (addData != null ? addData.getBbox() : originalAuthBbox) + \"'\";\n" +
                "            logger.info(traceLog);\n" +
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