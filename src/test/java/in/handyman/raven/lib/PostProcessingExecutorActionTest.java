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
                .batchId("BATCH-22_0")
                .groupId("22")
                .condition(true)
                .outputTable("score.aggregation_evaluator")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT\n" +
                        "                                            a.tenant_id as tenant_id,\n" +
                        "                                            a.confidence_score as aggregated_score,\n" +
                        "                                            a.weight_score as masked_score,\n" +
                        "                                            a.origin_id,\n" +
                        "                                            a.paper_no,\n" +
                        "                                            a.extracted_value,\n" +
                        "                                            b.vqa_score,\n" +
                        "                                            a.weight_rank as rank,\n" +
                        "                                            smca.sor_item_attribution_id as sor_item_attribution_id,\n" +
                        "                                            a.sor_item_name,\n" +
                        "                                            '1' as documentId,\n" +
                        "                                            1 as acc_transaction_id,\n" +
                        "                                            b.b_box,\n" +
                        "                                            a.root_pipeline_id as root_pipeline_id,\n" +
                        "                                            a.freq as frequency,\n" +
                        "                                            b.question_id,\n" +
                        "                                            b.synonym_id,\n" +
                        "                                            a.model_registry,\n" +
                        "                                            smca.is_encrypted,\n" +
                        "                                            smca.encryption_policy\n" +
                        "                                          FROM\n" +
                        "                                            voting.weighted_value a\n" +
                        "                                            JOIN score.scalar_validation b ON a.validation_id = b.validation_id\n" +
                        "                                            JOIN macro.sor_meta_consolidated_audit smca ON smca.tenant_id = b.tenant_id\n" +
                        "                                                            AND smca.root_pipeline_id = b.root_pipeline_id\n" +
                        "                                                            AND smca.synonym_id = b.synonym_id\n" +
                        "                                                            AND smca.sor_item_name = b.sor_item_name\n" +
                        "                                            JOIN sor_transaction.sor_transaction_payload_queue_archive st ON st.origin_id = a.origin_id\n" +
                        "                                                            AND st.batch_id = a.batch_id\n" +
                        "                                                            AND st.tenant_id = b.tenant_id\n" +
                        "                        WHERE a.origin_id ='ORIGIN-99' and b.synonym_id in (23548);\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "22");
        action.getContext().put("batch_id", "BATCH-22_0");
        action.getContext().put("created_user_id", "1");
                action.getContext().put("outbound.mapper.bsh.class.order", "ServiceDetailsMapper");
        action.getContext().put("pipeline.end.to.end.encryption", "false");
        action.getContext().put("ServiceDetailsMapper", "import org.slf4j.Logger;\n" +
                "import java.util.Map;\n" +
                "import java.util.regex.Pattern;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "\n" +
                "public class ServiceDetailsMapper {\n" +
                "\n" +
                "    private Logger logger;\n" +
                "    private Pattern serviceCodePattern;\n" +
                "    private Pattern diagnosisCodePattern;\n" +
                "\n" +
                "    public ServiceDetailsMapper(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "        this.serviceCodePattern = Pattern.compile(\"^[9HGJTS][0-9A-Za-z]{3}[0-9]{1}$|^[0-9A-Za-z]{5}$|^[0-9A-Za-z]{7}$\");\n" +
                "        this.diagnosisCodePattern = Pattern.compile(\"^[FR].*\");\n" +
                "    }\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
                "        logger.info(\"[RootPipelineID: {}] Entered ServiceDetailsMapper doCustomPredictionMapping method\", rootPipelineId);\n" +
                "\n" +
                "        if (shouldProcess(predictionKeyMap)) {\n" +
                "            serviceCodeValidation(predictionKeyMap, rootPipelineId);\n" +
                "            diagnosisCodeValidation(predictionKeyMap, rootPipelineId);\n" +
                "        } else {\n" +
                "            logger.info(\"[RootPipelineID: {}] No fields to process.\", rootPipelineId);\n" +
                "        }\n" +
                "\n" +
                "        return new MappingResult(predictionKeyMap);\n" +
                "    }\n" +
                "\n" +
                "    private boolean shouldProcess(Map predictionKeyMap) {\n" +
                "        return (predictionKeyMap.containsKey(\"service_code\") && predictionKeyMap.get(\"service_code\") != null) ||\n" +
                "               (predictionKeyMap.containsKey(\"diagnosis_code\") && predictionKeyMap.get(\"diagnosis_code\") != null);\n" +
                "    }\n" +
                "\n" +
                "    public String serviceCodeValidation(Map extractedData, Long rootPipelineId) {\n" +
                "        if (extractedData == null || !extractedData.containsKey(\"service_code\")) {\n" +
                "            logger.info(\"[RootPipelineID: {}] No value found for the service code field.\", rootPipelineId);\n" +
                "            return \"\";\n" +
                "        }\n" +
                "\n" +
                "        String serviceCode = String.valueOf(extractedData.get(\"service_code\"));\n" +
                "        if (serviceCode == null || serviceCode.trim().length() == 0) {\n" +
                "            logger.info(\"[RootPipelineID: {}] service_code is null or empty.\", rootPipelineId);\n" +
                "            return \"\";\n" +
                "        }\n" +
                "\n" +
                "        // Split service codes\n" +
                "        String[] serviceCodes;\n" +
                "        serviceCodes = serviceCode.split(\",\");\n" +
                "        List validCodes = new ArrayList(); // Use ArrayList instead of HashSet\n" +
                "\n" +
                "        // Traditional for loop\n" +
                "        for (int i = 0; i < serviceCodes.length; i++) {\n" +
                "            String trimmedCode = serviceCodes[i].trim();\n" +
                "            if (isValidServiceCode(trimmedCode)) {\n" +
                "                if (!validCodes.contains(trimmedCode)) { // Ensure uniqueness\n" +
                "                    validCodes.add(trimmedCode);\n" +
                "                }\n" +
                "            } else {\n" +
                "                logger.warn(\"[RootPipelineID: {}] Invalid service code: {}\", rootPipelineId, trimmedCode);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        if (validCodes.isEmpty()) {\n" +
                "            logger.warn(\"[RootPipelineID: {}] No valid service codes found.\", rootPipelineId);\n" +
                "            extractedData.remove(\"service_code\");\n" +
                "            return \"\";\n" +
                "        }\n" +
                "\n" +
                "        // Manual concatenation for Java 1.5 and BeanShell\n" +
                "        StringBuffer result = new StringBuffer();\n" +
                "        for (int i = 0; i < validCodes.size(); i++) {\n" +
                "            if (i > 0) {\n" +
                "                result.append(\",\");\n" +
                "            }\n" +
                "            result.append(validCodes.get(i).toString());\n" +
                "        }\n" +
                "\n" +
                "        String resultString = result.toString();\n" +
                "        extractedData.put(\"service_code\", resultString);\n" +
                "        return resultString;\n" +
                "    }\n" +
                "\n" +
                "    private boolean isValidServiceCode(String code) {\n" +
                "        if (code == null || code.length() == 0) {\n" +
                "            return false;\n" +
                "        }\n" +
                "        return serviceCodePattern.matcher(code).matches();\n" +
                "    }\n" +
                "\n" +
                "    public String diagnosisCodeValidation(Map extractedData, Long rootPipelineId) {\n" +
                "        if (extractedData == null || !extractedData.containsKey(\"diagnosis_code\")) {\n" +
                "            logger.info(\"[RootPipelineID: {}] No value found for the diagnosis code field.\", rootPipelineId);\n" +
                "            return \"\";\n" +
                "        }\n" +
                "\n" +
                "        String diagnosisCode = String.valueOf(extractedData.get(\"diagnosis_code\"));\n" +
                "        if (diagnosisCode == null || diagnosisCode.trim().length() == 0) {\n" +
                "            logger.info(\"[RootPipelineID: {}] diagnosis_code is null or empty.\", rootPipelineId);\n" +
                "            return \"\";\n" +
                "        }\n" +
                "\n" +
                "        if (diagnosisCodePattern.matcher(diagnosisCode).matches()) {\n" +
                "            extractedData.put(\"diagnosis_code\", diagnosisCode);\n" +
                "            return diagnosisCode;\n" +
                "        } else {\n" +
                "            logger.warn(\"[RootPipelineID: {}] Invalid diagnosis code: {}. Must start with F or R.\", rootPipelineId, diagnosisCode);\n" +
                "            extractedData.remove(\"diagnosis_code\");\n" +
                "            return \"\";\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public class MappingResult {\n" +
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
        action.setRootPipelineId(929L);

        PostProcessingExecutorAction postProcessingExecutorAction = new PostProcessingExecutorAction(action, log, postProcessingExecutor);
        postProcessingExecutorAction.execute();
    }
}