package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.selections.ExtractedField;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.model.ScalarAdapter;
import in.handyman.raven.lib.model.SectionFiltering;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.List;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@Slf4j
public class SectionFilteringActionTest {
    private SectionFilteringAction actionUnderTest;
    private ActionExecutionAudit actionAudit;
    private SectionFiltering sectionFiltering;
    private InticsIntegrity mockEncryption;
    private Jdbi mockJdbi;

    @Test
    public void testSectionFiltering() throws Exception {
        log.info("SectionFilteringActionTest executed successfully.");
        final SectionFiltering build = SectionFiltering.builder()
                .condition(true)
                .name("Test section Filtering")
                .outputTable("transit_data.selection_over_filtering_output_10027")
                .inputTable("${temp_schema_name}.selection_over_filtering_input_${init_process_id.process_id}")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT a.id, now() as created_on ,a.tenant_id as created_user_id,now() as last_updated_on,a.tenant_id as last_updated_user_id,a.tenant_id,a.group_id,\n" +
                        "a.root_pipeline_id,a.batch_id,a.model_registry,a.sor_container_id, a.sor_container_name,\n" +
                        "a.sor_item_name,a.sor_item_label, a.section_alias, a.answer, a.confidence, a.bbox,\n" +
                        "a.bbox_asis,a.paper_no,a.origin_id,a.extracted_image_unit, a.image_dpi, a.image_height,\n" +
                        "a.image_width,string_agg(distinct silc.black_list_keyword,',') as blacklisted_labels ,\n" +
                        "string_agg(distinct tep.truth_entity,',') as blacklisted_sections,a.is_encrypted,a.encryption_policy\n" +
                        "from sor_transaction.llm_json_parser_output_audit a\n" +
                        "left join sor_meta.sor_container sc on sc.sor_container_id =a.sor_container_id\n" +
                        "left join sor_meta.truth_entity_priority tep on tep.sor_container_id =sc.sor_container_id\n" +
                        "left join sor_meta.sor_item si on si.sor_item_name =a.sor_item_name  and sc.sor_container_id =si.sor_container_id\n" +
                        "left join sor_meta.sor_item_label_config silc on silc.sor_item_id =si.sor_item_id and silc.\"instance\" ='root.processor#6'\n" +
                        "WHERE a.tenant_id =1 and a.group_id =197 and a.batch_id ='BATCH-197_0' and tep.section_type ='BLACKLISTED'\n" +
                        "and sc.status='ACTIVE' and si.status='ACTIVE' and si.sor_item_name='member_id'\n" +
                        "and silc.active is true\n" +
                        "group by a.tenant_id,a.tenant_id,a.group_id,\n" +
                        "a.root_pipeline_id,a.batch_id,a.model_registry,a.sor_container_id, a.sor_container_name,\n" +
                        "a.sor_item_name,a.sor_item_label, a.section_alias, a.answer, a.confidence, a.bbox,\n" +
                        "a.bbox_asis,a.paper_no,a.origin_id,a.extracted_image_unit, a.image_dpi, a.image_width, \n" +
                        "a.image_height,a.is_encrypted,a.encryption_policy,a.id;")
                .build();

        String encryptionUrl = "http://localhost:8190/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8190/vulcan/api/encryption/decrypt";
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setProcessId(12345L);
        action.getContext().put("validation.multiverse-mode", "true");
        action.getContext().put("validation.restricted-answers", "No,None of the above");
        action.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "true");
        action.getContext().put("validaiton.char-limit-count", "1");

        action.getContext().put("scalar.adapter.scrubbing.alpha.activator", "true");
        action.getContext().put("scalar.adapter.scrubbing.numeric.activator", "false");
        action.getContext().put("scalar.adapter.scrubbing.date.activator", "true");

        action.getContext().put("scalar.adapter.alpha.activator", "false");
        action.getContext().put("scalar.adapter.alphanumeric.activator", "false");
        action.getContext().put("scalar.adapter.numeric.activator", "false");
        action.getContext().put("scalar.adapter.date.activator", "false");
        action.getContext().put("scalar.adapter.date_reg.activator", "false");
        action.getContext().put("scalar.adapter.phone_reg.activator", "false");
        action.getContext().put("scalar.adapter.numeric_reg.activator", "false");
        action.getContext().put("temp_schema_name", "transist_data");

        action.getContext().put("date.input.formats", "M/d/yy;MM/dd/yyyy;MM/dd/yy;MM.dd.yyyy;MM.dd.yy;M.dd.yyyy;M.d.yyyy;MM-dd-yyyy;MM-dd-yy;M-dd-yyyy;M-dd-yy;M/d/yyyy;M/dd/yyyy;yyyy-MM-dd;yyyy/MM/dd;dd-MM-yyyy;dd/MM/yyyy;d/M/yyyy;MMM dd, yyyy;dd-MMM-yyyy;dd/yyyy/MM;dd-yyyy-MM;yyyyMMdd;MMddyyyy;yyyyddMM;dd MMM yyyy;dd.MM.yyyy;dd MMMM yyyy;MMMM dd, yyyy;EEE, dd MMM yyyy;EEEE, MMM dd, yyyy");
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url",encryptionUrl);
        action.getContext().put("protegrity.dec.api.url",decryptionUrl);
        //action.getContext().put("copro.text-validation.url", "http://localhost:10189/copro/text-validation/patient");
        final SectionFilteringAction sectionFilteringAction = new SectionFilteringAction(action, log, build);
        sectionFilteringAction.execute();
    }
    @BeforeEach
    public void setup() {
        actionAudit = ActionExecutionAudit.builder().build();
        sectionFiltering = SectionFiltering.builder()
                .name("MockFiltering")
                .outputTable("mock_output_table")
                .resourceConn("mock_db_conn")
                .condition(true)
                .build();

        mockEncryption = Mockito.mock(InticsIntegrity.class);
        mockJdbi = Mockito.mock(Jdbi.class);

        actionUnderTest = new SectionFilteringAction(actionAudit, log, sectionFiltering);
    }

    @Test
    public void testEncryptDecryptFlow() {
        // Mock encryption/decryption responses
        when(mockEncryption.decrypt(anyList())).thenAnswer(invocation -> {
            List<EncryptionRequestClass> reqs = invocation.getArgument(0);
            reqs.forEach(r -> r.setValue("decrypted_" + r.getValue()));
            return reqs;
        });

        when(mockEncryption.encrypt(anyList())).thenAnswer(invocation -> {
            List<EncryptionRequestClass> reqs = invocation.getArgument(0);
            reqs.forEach(r -> r.setValue("encrypted_" + r.getValue()));
            return reqs;
        });

        // Prepare dummy data
        SelectionFilteringInputTable record = new SelectionFilteringInputTable();
        record.setId(1L);
        record.setAnswer("mock_value");

        List<SelectionFilteringInputTable> list = List.of(record);

        // Test decrypt
        actionUnderTest.decryptAnswers(list, mockEncryption);
        log.info("After decrypt: {}", list.get(0).getAnswer());

        // Test encrypt
        actionUnderTest.encryptAnswers(list, mockEncryption);
        log.info("After encrypt: {}", list.get(0).getAnswer());
    }

    @Test
    public void testFilteringAndMergingLogic() throws Exception {
        // Prepare data and simulate filtering
        SelectionFilteringInputTable row = new SelectionFilteringInputTable();
        row.setId(100L);
        row.setAnswer("ABC");
        row.setSorItemLabel("label1");
        row.setSectionAlias("alias1");
        row.setCreatedOn(new Timestamp(System.currentTimeMillis()));
        List<SelectionFilteringInputTable> inputList = List.of(row);

        // Simulate extracted and filtered fields
        ExtractedField ef = ExtractedField.builder()
                .id(100)
                .label("filtered_label")
                .sectionAlias("filtered_alias")
                .value("XYZ")
                .labelMatchMessage("Filtered OK")
                .isLabelMatching(true)
                .build();

        actionUnderTest.mergeFilteredResults(inputList, List.of(ef));

        log.info("Merged answer: {}", inputList.get(0).getAnswer());
    }
}
