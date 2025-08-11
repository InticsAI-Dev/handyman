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
                .batchId("BATCH-24_0")
                .groupId("2014")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn")
                .querySet("  SELECT DISTINCT\n" +
                        "          b.tenant_id,\n" +
                        "          b.aggregated_score,\n" +
                        "          b.masked_score,\n" +
                        "          b.origin_id,\n" +
                        "          b.paper_no,\n" +
                        "          b.predicted_value as extracted_value ,\n" +
                        "          b.vqa_score,\n" +
                        "          b.rank,\n" +
                        "          smca.sor_item_attribution_id as sor_item_attribution_id,\n" +
                        "          b.sor_item_name,\n" +
                        "          'TMP-AGD-001' as documentId,\n" +
                        "  1 as acc_transaction_id,\n" +
                        "  b.b_box,\n" +
                        "  b.root_pipeline_id as root_pipeline_id,\n" +
                        "  b.frequency,\n" +
                        "  b.question_id,\n" +
                        "  b.synonym_id,\n" +
                        "  b.model_registry,\n" +
                        "  smca.is_encrypted,\n" +
                        "  smca.encryption_policy,\n" +
                        "  'multi_value' as line_item_type\n" +
                        "   from macro.multi_value_sor_item_audit b\n" +
                        "      JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = b.tenant_id\n" +
                        "                      AND smca.root_pipeline_id = b.root_pipeline_id\n" +
                        "                      AND smca.synonym_id = b.synonym_id\n" +
                        "                      AND smca.sor_item_name = b.sor_item_name\n" +
                        "      JOIN sor_transaction.sor_transaction_payload_queue_archive st ON st.origin_id = b.origin_id\n" +
                        "                      AND st.batch_id = b.batch_id\n" +
                        "                      AND st.tenant_id = b.tenant_id\n" +
                        "    WHERE\n" +
                        "          b.group_id = '2014'\n" +
                        "      AND b.tenant_id = 1\n" +
                        "      AND b.batch_id = 'BATCH-24_0'\n" +
                        "      AND b.root_pipeline_id = '903'\n" +
                        "      AND smca.line_item_type = 'multi_value'\n" +
                        "      and b.sor_item_name = 'member_address_line1';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-24_0");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("outbound.mapper.bsh.class.order", "AuthIdValidator");
        action.getContext().put("pipeline.end.to.end.encryption", "true");
        action.getContext().put("AuthIdValidator", "import org.slf4j.Logger;\n" +
                "import java.util.*;\n" +
                "\n" +
                "public class AuthIdValidator {\n" +
                "\n" +
                "    private Logger logger;\n" +
                "\n" +
                "    public AuthIdValidator(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "    }\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
                "        logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Starting Auth ID mapping process\");\n" +
                "\n" +
                "        if (predictionKeyMap != null) {\n" +
                "            processAuthAndMemberIds(predictionKeyMap, rootPipelineId);\n" +
                "        } else {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Input map is null\");\n" +
                "            Map result = new HashMap();\n" +
                "            result.put(\"auth_id\", \"\");\n" +
                "            result.put(\"member_id\", \"\");\n" +
                "            return new MappingResult(result);\n" +
                "        }\n" +
                "\n" +
                "        return new MappingResult(predictionKeyMap);\n" +
                "    }\n" +
                "\n" +
                "    private void processAuthAndMemberIds(Map data, Long rootPipelineId) {\n" +
                "        // Handle null inputs explicitly\n" +
                "        String authId = data.containsKey(\"auth_id\") && data.get(\"auth_id\") != null ? String.valueOf(data.get(\"auth_id\")) : \"\";\n" +
                "        String memberId = data.containsKey(\"member_id\") && data.get(\"member_id\") != null ? String.valueOf(data.get(\"member_id\")) : \"\";\n" +
                "        String additionalAuthProperties = data.containsKey(\"additional_auth_properties\") && data.get(\"additional_auth_properties\") != null ? String.valueOf(data.get(\"additional_auth_properties\")) : \"\";\n" +
                "\n" +
                "        // Validate auth_id\n" +
                "        boolean authIdValid = false;\n" +
                "        String firstValidAuthId = null;\n" +
                "        ArrayList remainingValidAuthIds = new ArrayList();\n" +
                "        if (!authId.equals(\"\")) {\n" +
                "            String[] authIds = authId.split(\",\");\n" +
                "            HashSet uniqueAuthIds = new HashSet();\n" +
                "            for (String id : authIds) {\n" +
                "                id = id.trim();\n" +
                "                if (!id.equals(\"\") && !uniqueAuthIds.contains(id) && isValidAuthId(id)) {\n" +
                "                    uniqueAuthIds.add(id);\n" +
                "                    if (firstValidAuthId == null) {\n" +
                "                        firstValidAuthId = id;\n" +
                "                        authIdValid = true;\n" +
                "                    } else {\n" +
                "                        remainingValidAuthIds.add(id);\n" +
                "                    }\n" +
                "                } else if (!id.equals(\"\")) {\n" +
                "                    logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Invalid auth_id (must be UM + 8 digits or greater than 8 digits)\");\n" +
                "                }\n" +
                "            }\n" +
                "        } else {\n" +
                "            data.put(\"auth_id\", \"\");\n" +
                "        }\n" +
                "\n" +
                "        // Handle member_id\n" +
                "        if (!memberId.equals(\"\")) {\n" +
                "            String[] memberIds = memberId.split(\",\");\n" +
                "            HashSet uniqueMemberIds = new HashSet();\n" +
                "            ArrayList validMemberIds = new ArrayList();\n" +
                "            String umId = null;\n" +
                "            ArrayList validUmIds = new ArrayList();\n" +
                "\n" +
                "            for (String id : memberIds) {\n" +
                "                id = id.trim();\n" +
                "                if (!id.equals(\"\") && !uniqueMemberIds.contains(id)) {\n" +
                "                    uniqueMemberIds.add(id);\n" +
                "                    if (id.toUpperCase().startsWith(\"UM\")) {\n" +
                "                        if (isValidAuthId(id)) {\n" +
                "                            if (!authIdValid && umId == null) {\n" +
                "                                umId = id;\n" +
                "                            } else {\n" +
                "                                validUmIds.add(id);\n" +
                "                            }\n" +
                "                        } else {\n" +
                "                            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Invalid UM member_id (must be UM + 8 digits or greater than 8 digits)\");\n" +
                "                        }\n" +
                "                    } else {\n" +
                "                        if (isValidMemberId(id)) {\n" +
                "                            validMemberIds.add(id);\n" +
                "                        } else {\n" +
                "                            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Invalid member_id (length 9-13 or non-alphanumeric)\");\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            if (umId != null || !validUmIds.isEmpty()) {\n" +
                "                logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] member_id starts with UM, reassigning to auth_id\");\n" +
                "                if (umId != null) {\n" +
                "                    data.put(\"auth_id\", umId);\n" +
                "                    authIdValid = true;\n" +
                "                }\n" +
                "                if (!validUmIds.isEmpty()) {\n" +
                "                    String newAdditional = join(validUmIds, \",\");\n" +
                "                    additionalAuthProperties = additionalAuthProperties.equals(\"\") ? newAdditional : additionalAuthProperties + \",\" + newAdditional;\n" +
                "                }\n" +
                "                data.put(\"member_id\", validMemberIds.isEmpty() ? \"\" : join(validMemberIds, \",\"));\n" +
                "            } else {\n" +
                "                data.put(\"member_id\", validMemberIds.isEmpty() ? \"\" : join(validMemberIds, \",\"));\n" +
                "            }\n" +
                "        } else {\n" +
                "            data.put(\"member_id\", \"\");\n" +
                "        }\n" +
                "\n" +
                "        // Handle auth_id if it contains valid IDs and hasn't been overridden\n" +
                "        if (authIdValid && firstValidAuthId != null && data.get(\"auth_id\").equals(authId)) {\n" +
                "            data.put(\"auth_id\", firstValidAuthId);\n" +
                "            if (!remainingValidAuthIds.isEmpty()) {\n" +
                "                String newAdditional = join(remainingValidAuthIds, \",\");\n" +
                "                additionalAuthProperties = additionalAuthProperties.equals(\"\") ? newAdditional : additionalAuthProperties + \",\" + newAdditional;\n" +
                "            }\n" +
                "        } else if (!authIdValid && data.get(\"auth_id\").equals(authId)) {\n" +
                "            data.put(\"auth_id\", \"\");\n" +
                "        }\n" +
                "\n" +
                "        // Update additional_auth_properties without clearing existing values\n" +
                "        data.put(\"additional_auth_properties\", additionalAuthProperties);\n" +
                "    }\n" +
                "\n" +
                "    private boolean isValidAuthId(String id) {\n" +
                "        if (id == null || id.equals(\"\")) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        // Check for UM followed by 8 or more digits (case-insensitive)\n" +
                "        if (!id.toUpperCase().startsWith(\"UM\")) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        if (id.length() < 10) { // UM + 8 digits = 10 characters minimum\n" +
                "            return false;\n" +
                "        }\n" +
                "        // Check if remaining characters after UM are digits\n" +
                "        for (int i = 2; i < id.length(); i++) {\n" +
                "            if (!Character.isDigit(id.charAt(i))) {\n" +
                "                return false;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    private boolean isValidMemberId(String id) {\n" +
                "        if (id == null || id.equals(\"\")) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        // Check length between 9 and 13\n" +
                "        int length = id.length();\n" +
                "        if (length < 9 || length > 13) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        // Check if id contains only alphanumeric characters\n" +
                "        for (int i = 0; i < id.length(); i++) {\n" +
                "            char c = id.charAt(i);\n" +
                "            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {\n" +
                "                return false;\n" +
                "            }\n" +
                "        }\n" +
                "        return true;\n" +
                "    }\n" +
                "\n" +
                "    private String join(List list, String separator) {\n" +
                "        StringJoiner joiner = new StringJoiner(separator);\n" +
                "        for (Object item : list) {\n" +
                "            joiner.add(item.toString());\n" +
                "        }\n" +
                "        return joiner.toString();\n" +
                "    }\n" +
                "\n" +
                "    public static class MappingResult {\n" +
                "        private Map mappedData;\n" +
                "\n" +
                "        public MappingResult(Map mappedData) {\n" +
                "            this.mappedData = mappedData != null ? mappedData : new HashMap();\n" +
                "        }\n" +
                "\n" +
                "        public Map getMappedData() {\n" +
                "            return mappedData;\n" +
                "        }\n" +
                "    }\n" +
                "}");
        action.getContext().put("MedicaidMemberIdValidator", "");
        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        action.setRootPipelineId(929L);
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url",encryptionUrl);
        action.getContext().put("protegrity.dec.api.url",decryptionUrl);
        PostProcessingExecutorAction postProcessingExecutorAction = new PostProcessingExecutorAction(action, log, postProcessingExecutor);
        postProcessingExecutorAction.execute();
    }
}