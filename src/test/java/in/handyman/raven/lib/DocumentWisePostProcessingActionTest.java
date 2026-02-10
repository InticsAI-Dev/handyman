package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocumentWisePostProcessing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Integration test for DocumentWisePostProcessingAction.
 * 
 * This test requires:
 * 1. Database connection configured in resource_config table with name "intics_zio_db_conn"
 * 2. Test data in the database matching the querySet SQL
 * 3. Encryption service running at http://localhost:8189/vulcan/api/encryption/encrypt and decrypt endpoints
 * 
 * To run this test:
 * - Ensure database is accessible and configured
 * - Set system property: -Dtest.database.enabled=true
 * - Or run with: mvn test -Dtest.database.enabled=true
 * 
 * Note: This is an integration test that connects to a real database.
 * For unit testing, consider mocking ResourceAccess and JDBI connections.
 */
@Slf4j
class DocumentWisePostProcessingActionTest {

    @Test
    @EnabledIfSystemProperty(named = "test.database.enabled", matches = "true", 
                             disabledReason = "Database connection required. Set -Dtest.database.enabled=true to run")
    void execute() throws Exception {
        // Note: This is an integration test that requires:
        // 1. Database connection "intics_zio_db_conn" configured in resource_config table
        // 2. Test data matching the querySet SQL
        // 3. Encryption service endpoints available
        // If you see database connection errors, ensure the database is properly configured
        // or skip this test by not setting -Dtest.database.enabled=true

        DocumentWisePostProcessing documentWisePostProcessing = DocumentWisePostProcessing.builder()
                .name("Document Wise Post Processing executor")
                .batchId("BATCH-5_0")
                .condition(true)
                .outputTable("valuation.document_wise_post_processing_output")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT " +
                        "cr.created_on, " +
                        "cr.created_user_id, " +
                        "cr.last_updated_on, " +
                        "cr.last_updated_user_id, " +
                        "'ACTIVE' AS status, " +
                        "cr.version, " +
                        "sot.encode, " +
                        "'KIE' AS feature, " +
                        "'' AS label, " +
                        "cr.origin_id, " +
                        "ROUND(cr.confidence_score::numeric / 100, 2) AS precision, " +
                        "cr.extracted_value AS predicted_value, " +
                        "cr.question_id, " +
                        "cr.root_pipeline_id, " +
                        "'' AS state, " +
                        "cr.synonym_id, " +
                        "cr.tenant_id, " +
                        "sot.transaction_id, " +
                        "sot.truth_id, " +
                        "sot.channel_id, " +
                        "'' AS csv_file_path, " +
                        "smca.sor_container_id, " +
                        "smca.truth_entity_id, " +
                        "'' AS currency_ascii_value, " +
                        "'' AS currency_value, " +
                        "'' AS paragraph_section, " +
                        "smca.sor_item_id, " +
                        "COALESCE((NULLIF(cr.b_box, '')::jsonb ->> 'topLeftX')::int8, 0) AS left_pos, " +
                        "COALESCE((NULLIF(cr.b_box, '')::jsonb ->> 'bottomRightX')::int8, 0) AS right_pos, " +
                        "COALESCE((NULLIF(cr.b_box, '')::jsonb ->> 'bottomRightY')::int8, 0) AS lower_pos, " +
                        "COALESCE((NULLIF(cr.b_box, '')::jsonb ->> 'topLeftY')::int8, 0) AS upper_pos, " +
                        "smca.is_encrypted, " +
                        "cr.group_id, " +
                        "cr.batch_id " +
                        "FROM voting.cummulative_result cr " +
                        "JOIN info.source_of_truth sot " +
                        "ON cr.origin_id = sot.origin_id " +
                        "AND cr.paper_no = sot.paper_no " +
                        "AND cr.batch_id = sot.batch_id " +
                        "AND cr.tenant_id = sot.tenant_id " +
                        "JOIN transit_data.sor_meta_consolidated_521 smca " +
                        "ON smca.group_id = cr.group_id " +
                        "AND smca.synonym_id = cr.synonym_id " +
                        "AND smca.sor_item_name = cr.sor_item_name " +
                        "WHERE cr.group_id = 5 " +
                        "AND cr.tenant_id = 1 " +
                        "AND cr.batch_id = 'BATCH-5_0';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "5");
        action.getContext().put("batch_id", "BATCH-5_0");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("document.wise.executor.bsh.class.order", "NewbornDOBValidator");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        action.getContext().put("document.wise.post.processing.thread.count", "10");
        action.getContext().put("NewbornDOBValidator", "import org.slf4j.Logger;\n" +
                "import java.util.*;\n" +
                "import java.text.SimpleDateFormat;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "public class NewbornDOBValidator {\n" +
                "\n" +
                "    private Logger logger;\n" +
                "\n" +
                "    public NewbornDOBValidator(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "    }\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(List documentWisePostProcessingInputList, Long rootPipelineId) {\n" +
                "        logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Starting Newborn DOB validation process\");\n" +
                "\n" +
                "        if (documentWisePostProcessingInputList == null || documentWisePostProcessingInputList.isEmpty()) {\n" +
                "            logger.warn(\"[RootPipelineID: \" + rootPipelineId + \"] Input list is null or empty\");\n" +
                "            return new MappingResult(new ArrayList());\n" +
                "        }\n" +
                "\n" +
                "        String faxReceivedDate = null;\n" +
                "        String newbornDOB = null;\n" +
                "        String memberDOB = null;\n" +
                "        int newbornDOBIndex = -1;\n" +
                "        int memberDOBIndex = -1;\n" +
                "\n" +
                "        // Find fax_received_date, newborn DOB, and member DOB from the list\n" +
                "        for (int i = 0; i < documentWisePostProcessingInputList.size(); i++) {\n" +
                "            Object item = documentWisePostProcessingInputList.get(i);\n" +
                "            String sorItemName = getFieldValue(item, \"getSorItemName\");\n" +
                "            String predictedValue = getFieldValue(item, \"getPredictedValue\");\n" +
                "\n" +
                "            if (sorItemName != null && predictedValue != null) {\n" +
                "                if (sorItemName.equalsIgnoreCase(\"fax_received_date\")) {\n" +
                "                    faxReceivedDate = predictedValue;\n" +
                "                } else if (sorItemName.equalsIgnoreCase(\"newborn_date_of_birth\")) {\n" +
                "                    newbornDOB = predictedValue;\n" +
                "                    newbornDOBIndex = i;\n" +
                "                } else if (sorItemName.equalsIgnoreCase(\"member_date_of_birth\")) {\n" +
                "                    memberDOB = predictedValue;\n" +
                "                    memberDOBIndex = i;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Validate dates\n" +
                "        Date faxDate = parseDateSafeFromString(faxReceivedDate);\n" +
                "        Date newbornDobDate = parseDateSafeFromString(newbornDOB);\n" +
                "        Date memberDobDate = parseDateSafeFromString(memberDOB);\n" +
                "\n" +
                "        if (faxDate == null) {\n" +
                "            faxDate = new Date();\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] fax_received_date missing, using current date\");\n" +
                "        }\n" +
                "\n" +
                "        // Update newborn DOB if needed\n" +
                "        if (newbornDobDate != null && isWithin30Days(newbornDobDate, faxDate)) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Newborn DOB is valid (within 30 days of fax date)\");\n" +
                "        } else if (memberDobDate != null && isWithin30Days(memberDobDate, faxDate)) {\n" +
                "            logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Newborn DOB invalid, member DOB is valid (within 30 days of fax date)\");\n" +
                "            if (newbornDOBIndex >= 0) {\n" +
                "                Object newbornItem = documentWisePostProcessingInputList.get(newbornDOBIndex);\n" +
                "                setFieldValue(newbornItem, \"setPredictedValue\", memberDOB);\n" +
                "                logger.info(\"[RootPipelineID: \" + rootPipelineId + \"] Updated newborn DOB with member DOB value\");\n" +
                "            }\n" +
                "        } else {\n" +
                "            logger.warn(\"[RootPipelineID: \" + rootPipelineId + \"] Both newborn DOB and member DOB are invalid\");\n" +
                "        }\n" +
                "\n" +
                "        return new MappingResult(documentWisePostProcessingInputList);\n" +
                "    }\n" +
                "\n" +
                "    private String getFieldValue(Object obj, String methodName) {\n" +
                "        try {\n" +
                "            java.lang.reflect.Method method = obj.getClass().getMethod(methodName);\n" +
                "            Object result = method.invoke(obj);\n" +
                "            return result != null ? result.toString() : null;\n" +
                "        } catch (Exception e) {\n" +
                "            return null;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private void setFieldValue(Object obj, String methodName, String value) {\n" +
                "        try {\n" +
                "            java.lang.reflect.Method method = obj.getClass().getMethod(methodName, String.class);\n" +
                "            method.invoke(obj, value);\n" +
                "        } catch (Exception e) {\n" +
                "            logger.warn(\"Failed to set field value: \" + e.getMessage());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private Date parseDateSafeFromString(String dateValue) {\n" +
                "        Date result = null;\n" +
                "        if (dateValue != null && !dateValue.trim().isEmpty()) {\n" +
                "            try {\n" +
                "                String dateOnly = dateValue.split(\"\\\\s+\")[0];\n" +
                "                String ymd = toYMD(dateOnly);\n" +
                "                if (ymd != null && !ymd.isEmpty()) {\n" +
                "                    SimpleDateFormat sdf = new SimpleDateFormat(\"yyyy-MM-dd\");\n" +
                "                    sdf.setLenient(false);\n" +
                "                    result = sdf.parse(ymd);\n" +
                "                } else {\n" +
                "                    logger.warn(\"Could not convert date to YYYY-MM-DD format\");\n" +
                "                }\n" +
                "            } catch (Exception e) {\n" +
                "                logger.error(\"Error parsing date from string\", e);\n" +
                "            }\n" +
                "        } else {\n" +
                "            logger.debug(\"Date value is null or empty\");\n" +
                "        }\n" +
                "        return result;\n" +
                "    }\n" +
                "\n" +
                "    private String toYMD(String input) {\n" +
                "        if (input == null || input.trim().isEmpty()) return input;\n" +
                "        String datePart = input.trim().split(\" \")[0];\n" +
                "        String cleaned = datePart.replaceAll(\"[^0-9/\\\\-]\", \"\");\n" +
                "        if (cleaned.matches(\"\\\\d{1,2}[-/]\\\\d{1,2}[-/]\\\\d{4}\")) {\n" +
                "            String[] p = cleaned.split(\"[-/]\");\n" +
                "            return p[2] + \"-\" + (p[0].length() == 1 ? \"0\" + p[0] : p[0]) + \"-\" + (p[1].length() == 1 ? \"0\" + p[1] : p[1]);\n" +
                "        }\n" +
                "        if (cleaned.matches(\"\\\\d{4}-\\\\d{1,2}-\\\\d{1,2}\")) {\n" +
                "            logger.info(\"cleaned the date format\");\n" +
                "            return cleaned;\n" +
                "        }\n" +
                "        logger.info(\"not a valid format so return empty\");\n" +
                "        return input;\n" +
                "    }\n" +
                "\n" +
                "    private boolean isWithin30Days(Date dob, Date faxDate) {\n" +
                "        long diffDays = (faxDate.getTime() - dob.getTime()) / (1000L * 60 * 60 * 24);\n" +
                "        logger.info(\"Days difference between fax date & DOB = \" + diffDays);\n" +
                "        return diffDays >= 0 && diffDays <= 30;\n" +
                "    }\n" +
                "\n" +
                "    public static class MappingResult {\n" +
                "        private List mappedData;\n" +
                "\n" +
                "        public MappingResult(List mappedData) {\n" +
                "            this.mappedData = mappedData != null ? mappedData : new ArrayList();\n" +
                "        }\n" +
                "\n" +
                "        public List getMappedData() {\n" +
                "            return mappedData;\n" +
                "        }\n" +
                "    }\n" +
                "}");
        action.getContext().put("MemberDOBValidator", "");
        String encryptionUrl = "http://localhost:8189/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8189/vulcan/api/encryption/decrypt";
        action.setRootPipelineId(4636L);
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url", encryptionUrl);
        action.getContext().put("protegrity.dec.api.url", decryptionUrl);
        DocumentWisePostProcessingAction documentWisePostProcessingAction = new DocumentWisePostProcessingAction(action, log, documentWisePostProcessing);
        documentWisePostProcessingAction.execute();
    }
}
