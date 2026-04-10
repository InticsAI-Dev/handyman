package in.handyman.raven.lib;

import in.handyman.raven.core.azure.adapters.HikariJdbiProvider;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxAttribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.Test;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class CheckboxAttributionActionTest {

        private static final Logger log = LoggerFactory.getLogger(CheckboxAttributionActionTest.class);

        @Test
        public void tritonTest() throws Exception {
                HikariJdbiProvider.init();
                CheckboxAttribution checkboxAttribution = CheckboxAttribution.builder()
                                .name("checkboxAttribution")
                                .condition(true)
                                .resourceConn("intics_zio_db_conn")
                                .outputTable("checkbox_extraction.checkbox_extraction_output_audit")
                                .endpoint("http://192.168.10.239:9000/predict")
                                .querySet(
                                                "SELECT\n" +
                                                                "    input_file_path,\n" +
                                                                "    user_prompt,\n" +
                                                                "    system_prompt,\n" +
                                                                "    process,\n" +
                                                                "    paper_no,\n" +
                                                                "    origin_id,\n" +
                                                                "    process_id,\n" +
                                                                "    group_id,\n" +
                                                                "    tenant_id,\n" +
                                                                "    root_pipeline_id,\n" +
                                                                "    batch_id,\n" +
                                                                "    model_registry,\n" +
                                                                "    category,\n" +
                                                                "    now() AS created_on,\n" +
                                                                "    'KRYPTON START' AS api_name\n" +
                                                                "FROM (\n" +
                                                                "    SELECT\n" +
                                                                "        ci.*,\n" +
                                                                "        ROW_NUMBER() OVER (\n" +
                                                                "            PARTITION BY origin_id\n" +
                                                                "            ORDER BY paper_no ASC\n" +
                                                                "        ) AS row_number\n" +
                                                                "    FROM checkbox_extraction.checkbox_extraction_input_audit ci\n" +
                                                                "    WHERE ci.model_registry = 'KRYPTON'\n" +
                                                                "      AND ci.process = 'CHECKBOX_EXTRACTION'\n" +
                                                                "      AND ci.group_id = '282'\n" +
                                                                "      AND ci.tenant_id = '1'\n" +
                                                                "      AND ci.batch_id = 'BATCH-282_0'\n" +
                                                                ") t\n" +
                                                                "WHERE t.row_number <= (\n" +
                                                                "    SELECT MAX(si.paper_count)\n" +
                                                                "    FROM sor_meta.sor_item si\n" +
                                                                "    WHERE si.tenant_id = '1'\n" +
                                                                ")\n" +
                                                                "ORDER BY t.origin_id, t.paper_no ASC;")
                                .build();

                ActionExecutionAudit ac = new ActionExecutionAudit();
                ac.setRootPipelineId(1234L);
                ac.setActionId(1234L);
                ac.setProcessId(123L);
                ac.getContext().put("checkbox.consumer.API.count", "8");
                ac.getContext().put("write.batch.size", "1");
                ac.getContext().put("read.batch.size", "1");
                ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
                ac.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
                ac.getContext().put("copro.processor.consumer.route.type", "MODERN");
                ac.getContext().put("copro.isretry.enabled", "true");
                ac.getContext().put("copro.api.file.input.format", "LINK");

                CheckboxAttributionAction checkboxAttributionAction = new CheckboxAttributionAction(ac, log,
                                checkboxAttribution);
                checkboxAttributionAction.execute();

                Object isSuccessful = ac.getContext().get("checkbox attribution.isSuccessful");
                if (isSuccessful != null && "false".equals(isSuccessful.toString())) {
                        throw new RuntimeException(
                                        "Checkbox attribution action failed. Check exception logs for details.");
                }
        }
}
