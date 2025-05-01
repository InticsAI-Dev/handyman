package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PostProcessingExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.lib.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

@Slf4j
class PostProcessingExecutorActionTest {

    @Test
    void execute() throws Exception {

        PostProcessingExecutor postProcessingExecutor = PostProcessingExecutor.builder()
                .name("Post Processing executor")
                .batchId("BATCH-111_0")
                .groupId("111")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT\n" +
                        "                     a.tenant_id as tenant_id,\n" +
                        "                     a.confidence_score as aggregated_score,\n" +
                        "                     a.weight_score as masked_score,\n" +
                        "                     a.origin_id,\n" +
                        "                     a.paper_no,\n" +
                        "                     a.extracted_value,\n" +
                        "                     b.vqa_score,\n" +
                        "                     a.weight_rank as rank,\n" +
                        "                     smca.sor_item_attribution_id as sor_item_attribution_id,\n" +
                        "                     a.sor_item_name,\n" +
                        "                     '${document_id}' as documentId,\n" +
                        "                     1 as acc_transaction_id,\n" +
                        "                     b.b_box,\n" +
                        "                     a.root_pipeline_id as root_pipeline_id,\n" +
                        "                     a.freq as frequency,\n" +
                        "                     b.question_id,\n" +
                        "                     b.synonym_id,\n" +
                        "                     a.model_registry,\n" +
                        "                     smca.is_encrypted,\n" +
                        "                     smca.encryption_policy\n" +
                        "                   FROM\n" +
                        "                     voting.weighted_value a\n" +
                        "                     JOIN score.scalar_validation b ON a.validation_id = b.validation_id\n" +
                        "                     JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = b.tenant_id\n" +
                        "                                     AND smca.root_pipeline_id = b.root_pipeline_id\n" +
                        "                                     AND smca.synonym_id = b.synonym_id\n" +
                        "                                     AND smca.sor_item_name = b.sor_item_name\n" +
                        "                     JOIN sor_transaction.sor_transaction_payload_queue_archive st ON st.origin_id = a.origin_id\n" +
                        "                                     AND st.batch_id = a.batch_id\n" +
                        "                                     AND st.tenant_id = b.tenant_id\n " +
                        " WHERE a.origin_id ='ORIGIN-396' and smca.line_item_type <> 'multi_value' and b.synonym_id in (23543,25134);")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "113");
        action.getContext().put("batch_id", "BATCH-113_0");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("outbound.mapper.bsh.class.order", "NonLookUpFieldsMixerValidator");
        action.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        action.getContext().put("NonLookUpFieldsMixerValidator", "import org.slf4j.Logger;\n" +
                "import org.slf4j.LoggerFactory;\n" +
                "\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "import java.util.regex.Pattern;\n" +
                "\n" +
                "public class NonLookUpFieldsMixerValidator {\n" +
                "    private static final Logger logger = LoggerFactory.getLogger(NonLookUpFieldsMixerValidator.class);\n" +
                "    private Long rootPipelineId;\n" +
                "    private static List logMessages = new ArrayList();\n" +
                "    private static final Pattern ALPHA_ONLY_PATTERN = Pattern.compile(\"^[a-zA-Z]+$\");\n" +
                "    private static final Pattern NUMERIC_ONLY_PATTERN = Pattern.compile(\"^[0-9]+$\");\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
                "        this.rootPipelineId = rootPipelineId;\n" +
                "        String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Entered NonLookUpFieldsMixerValidator doCustomPredictionMapping method\";\n" +
                "\tlogger.info(logMsg);\n" +
                "\tlogMessages.add(logMsg);\n" +
                "        if (predictionKeyMap == null) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] Input map is null\";\n" +
                "            logger.error(logMsg);\n" +
                "            logMessages.add(logMsg);\n" +
                "            return new MappingResult(null, logMessages);\n" +
                "        }\n" +
                "\n" +
                "        compareMemberFields(predictionKeyMap);\n" +
                "        validateAddressCityStateZipFormats(predictionKeyMap);\n" +
                "\n" +
                "        return new MappingResult(predictionKeyMap, logMessages);\n" +
                "    }\n" +
                "\n" +
                "    private void compareMemberFields(Map resultMap) {\n" +
                "        String memberAddress = (String) resultMap.get(\"member_address\");\n" +
                "        String memberCity = (String) resultMap.get(\"member_city\");\n" +
                "        String memberState = (String) resultMap.get(\"member_state\");\n" +
                "        String memberZip = (String) resultMap.get(\"member_zip\");\n" +
                "\n" +
                "        String cleanedMemberAddress = cleanString(memberAddress);\n" +
                "        String cleanedMemberCity = cleanString(memberCity);\n" +
                "        String cleanedMemberState = cleanString(memberState);\n" +
                "        String cleanedMemberZip = cleanString(memberZip);\n" +
                "\n" +
                "        if ((memberAddress == null || cleanedMemberAddress.isEmpty()) &&\n" +
                "                (memberCity == null || cleanedMemberCity.isEmpty()) &&\n" +
                "                (memberState == null || cleanedMemberState.isEmpty()) &&\n" +
                "                (memberZip == null || cleanedMemberZip.isEmpty())) {\n" +
                "            String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] All member fields are null or empty, no comparison performed\";\n" +
                "            logger.info(logMsg);\n" +
                "            logMessages.add(logMsg);\n" +
                "            return;\n" +
                "        }\n" +
                "\n" +
                "        String[] providerFields = {\n" +
                "                \"servicing_provider_address_line1\", \"referring_provider_address_line1\", \"servicing_facility_address_line1\",\n" +
                "                \"servicing_provider_city\", \"referring_provider_city\", \"servicing_facility_city\",\n" +
                "                \"servicing_provider_state\", \"referring_provider_state\", \"servicing_facility_state\",\n" +
                "                \"servicing_provider_zipcode\", \"referring_provider_zipcode\", \"servicing_facility_zipcode\"\n" +
                "        };\n" +
                "\n" +
                "        for (String field : providerFields) {\n" +
                "            String providerValue = (String) resultMap.get(field);\n" +
                "            if (providerValue != null) {\n" +
                "                String cleanedProviderValue = cleanString(providerValue);\n" +
                "\n" +
                "                if (memberAddress != null && !cleanedMemberAddress.isEmpty() &&\n" +
                "                        areValuesEqual(cleanedMemberAddress, cleanedProviderValue)) {\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] member_address matches with \" + field + \", setting \" + field + \" to empty\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, \"\");\n" +
                "                    continue;\n" +
                "                }\n" +
                "\n" +
                "                if (memberCity != null && !cleanedMemberCity.isEmpty() &&\n" +
                "                        areValuesEqual(cleanedMemberCity, cleanedProviderValue)) {\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] member_city matches with \" + field + \", setting \" + field + \" to empty\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, \"\");\n" +
                "                    continue;\n" +
                "                }\n" +
                "\n" +
                "                if (memberState != null && !cleanedMemberState.isEmpty() &&\n" +
                "                        areValuesEqual(cleanedMemberState, cleanedProviderValue)) {\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] member_state matches with \" + field + \", setting \" + field + \" to empty\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, \"\");\n" +
                "                    continue;\n" +
                "                }\n" +
                "\n" +
                "                if (memberZip != null && !cleanedMemberZip.isEmpty() &&\n" +
                "                        areValuesEqual(cleanedMemberZip, cleanedProviderValue)) {\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] member_zip matches with \" + field + \", setting \" + field + \" to empty\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, \"\");\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void validateAddressCityStateZipFormats(Map resultMap) {\n" +
                "        String[] addressCityStateFields = {\n" +
                "                \"member_address\", \"member_city\", \"member_state\",\n" +
                "                \"servicing_provider_address_line1\", \"referring_provider_address_line1\", \"servicing_facility_address_line1\",\n" +
                "                \"servicing_provider_city\", \"referring_provider_city\", \"servicing_facility_city\",\n" +
                "                \"servicing_provider_state\", \"referring_provider_state\", \"servicing_facility_state\"\n" +
                "        };\n" +
                "\n" +
                "        for (String field : addressCityStateFields) {\n" +
                "            String value = (String) resultMap.get(field);\n" +
                "            if (value != null && !value.isEmpty() && NUMERIC_ONLY_PATTERN.matcher(value).matches()) {\n" +
                "                String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] \" + field + \" is numeric only, setting to empty\";\n" +
                "                logger.info(logMsg);\n" +
                "                logMessages.add(logMsg);\n" +
                "                resultMap.put(field, \"\");\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        String[] zipFields = {\n" +
                "                \"member_zip\", \"servicing_provider_zipcode\", \"referring_provider_zipcode\", \"servicing_facility_zipcode\"\n" +
                "        };\n" +
                "\n" +
                "        for (String field : zipFields) {\n" +
                "            String value = (String) resultMap.get(field);\n" +
                "            if (value != null && !value.isEmpty()) {\n" +
                "                if (ALPHA_ONLY_PATTERN.matcher(value).matches()) {\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] \" + field + \" is alphabetic only, setting to empty\";\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, \"\");\n" +
                "                } else if (!NUMERIC_ONLY_PATTERN.matcher(value).matches()) {\n" +
                "                    String numericPart = value.replaceAll(\"[^0-9]\", \"\");\n" +
                "                    String logMsg = \"[RootPipelineID: \" + rootPipelineId + \"] \" + field + \" contains alphabets, extracting numeric part: \" + numericPart;\n" +
                "                    logger.info(logMsg);\n" +
                "                    logMessages.add(logMsg);\n" +
                "                    resultMap.put(field, numericPart.isEmpty() ? \"\" : numericPart);\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private boolean areValuesEqual(String value1, String value2) {\n" +
                "        if (value1 == null || value2 == null) return false;\n" +
                "        if (value1.isEmpty() || value2.isEmpty()) return false;\n" +
                "        return value1.equalsIgnoreCase(value2);\n" +
                "    }\n" +
                "\n" +
                "    private String cleanString(String str) {\n" +
                "        if (str == null) return \"\";\n" +
                "        String trimmed = str.trim();\n" +
                "        while (trimmed.indexOf(\"  \") != -1) {\n" +
                "            trimmed = trimmed.replace(\"  \", \" \");\n" +
                "        }\n" +
                "        return trimmed.toLowerCase();\n" +
                "    }\n" +
                "\n" +
                "    public static class MappingResult {\n" +
                "        private Map mappedData;\n" +
                "        private List logMessages;\n" +
                "\n" +
                "        public MappingResult(Map mappedData, List logMessages) {\n" +
                "            this.mappedData = mappedData;\n" +
                "            this.logMessages = logMessages;\n" +
                "        }\n" +
                "\n" +
                "        public Map getMappedData() {\n" +
                "            return mappedData;\n" +
                "        }\n" +
                "\n" +
                "        public List getLogMessages() {\n" +
                "            return logMessages;\n" +
                "        }\n" +
                "    }\n" +
                "}");
        action.setRootPipelineId(1113L);

        PostProcessingExecutorAction postProcessingExecutorAction = new PostProcessingExecutorAction(action, log, postProcessingExecutor);
        postProcessingExecutorAction.execute();
    }
}