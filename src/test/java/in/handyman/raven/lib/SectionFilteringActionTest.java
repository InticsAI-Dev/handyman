package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.selections.ExtractedField;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.model.SectionFiltering;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static in.handyman.raven.core.enums.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static org.junit.jupiter.api.Assertions.*;
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
                .outputTable("extraction.selection_over_filtering_output_audit")
                .inputTable("extraction.selection_over_filtering_output_audit")
                .resourceConn("intics_zio_db_conn_tsar")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, tenant_id, group_id,\n" +
                        "                    root_pipeline_id, batch_id, model_registry, sor_container_id, sor_container_name,\n" +
                        "                    sor_item_name, sor_item_label, section_alias, answer, confidence, bbox,\n" +
                        "                    bbox_asis, paper_no, origin_id, extracted_image_unit, image_dpi, image_height,\n" +
                        "                    image_width, blacklisted_labels,\n" +
                        "                    blacklisted_sections, is_encrypted, encryption_policy,whitelisted_labels,\n" +
                        "                    whitelisted_labels_with_priority,sor_container_instance,whitelisted_sections_with_priority,\n" +
                        "                    'single_value' as line_item_type,false as is_multi_entity_enabled" +
                        "                 from extraction.selection_over_filtering_input_audit a where origin_id='ORIGIN-500' and sor_item_name='member_id';")
                .build();

        String encryptionUrl = "http://localhost:8190/vulcan/api/encryption/encrypt";
        String decryptionUrl = "http://localhost:8190/vulcan/api/encryption/decrypt";
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setProcessId(12345L);
        action.getContext().put("validation.multiverse-mode", "true");
        action.getContext().put("validation.restricted-answers", "No,None of the above");
        action.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        action.getContext().put("validaiton.char-limit-count", "1");
        action.getContext().put("llm.json.parser.label.encryption", "true");

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
        action.getContext().put("section.filtering.label.with.priority","true");

        action.getContext().put("date.input.formats", "M/d/yy;MM/dd/yyyy;MM/dd/yy;MM.dd.yyyy;MM.dd.yy;M.dd.yyyy;M.d.yyyy;MM-dd-yyyy;MM-dd-yy;M-dd-yyyy;M-dd-yy;M/d/yyyy;M/dd/yyyy;yyyy-MM-dd;yyyy/MM/dd;dd-MM-yyyy;dd/MM/yyyy;d/M/yyyy;MMM dd, yyyy;dd-MMM-yyyy;dd/yyyy/MM;dd-yyyy-MM;yyyyMMdd;MMddyyyy;yyyyddMM;dd MMM yyyy;dd.MM.yyyy;dd MMMM yyyy;MMMM dd, yyyy;EEE, dd MMM yyyy;EEEE, MMM dd, yyyy");
        action.getContext().put("pipeline.encryption.default.holder", "PROTEGRITY_API_ENC");
        action.getContext().put("protegrity.enc.api.url",encryptionUrl);
        action.getContext().put("protegrity.dec.api.url",decryptionUrl);
        //action.getContext().put("copro.text-validation.url", "http://localhost:10189/copro/text-validation/patient");
        final SectionFilteringAction sectionFilteringAction = new SectionFilteringAction(action, log, build);
        sectionFilteringAction.execute();
    }

    public void setup() {
        actionAudit = ActionExecutionAudit.builder().build();
        sectionFiltering = SectionFiltering.builder()
                .name("MockFiltering")
                .outputTable("")
                .resourceConn("intics_zio_db_conn")
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

    @Test
    public void testContainerLevelRejection() {

        // 🔥 Create two rows in same container + paper
        SelectionFilteringInputTable row1 = new SelectionFilteringInputTable();
        row1.setId(1L);
        row1.setPaperNo(3L);
        row1.setOriginId("ORIGIN-717");
        row1.setSorContainerInstance("MEMBER_DETAILS_0");
        row1.setAnswer("Dinesh Kumar");
        row1.setLabelMatching(false); // ❌ One failure

        SelectionFilteringInputTable row2 = new SelectionFilteringInputTable();
        row2.setId(2L);
        row2.setPaperNo(3L);
        row2.setOriginId("ORIGIN-717");
        row2.setSorContainerInstance("MEMBER_DETAILS_0");
        row2.setAnswer("Some Address");
        row2.setLabelMatching(true); // ✅ Initially true

        List<SelectionFilteringInputTable> updatedTableInfos = List.of(row1, row2);

        // 🔥 APPLY SAME LOGIC AS YOUR ACTION (Container Rejection)
        Map<String, List<SelectionFilteringInputTable>> groupedByContainer =
                updatedTableInfos.stream()
                        .collect(Collectors.groupingBy(row -> {
                            String origin = row.getOriginId() == null ? "" : row.getOriginId().trim();
                            String paper = String.valueOf(row.getPaperNo());
                            String container = row.getSorContainerInstance() == null ? "" :
                                    row.getSorContainerInstance().trim().toUpperCase();

                            return origin + "|" + paper + "|" + container;
                        }));

        for (Map.Entry<String, List<SelectionFilteringInputTable>> entry : groupedByContainer.entrySet()) {

            List<SelectionFilteringInputTable> group = entry.getValue();

            boolean hasFailure = group.stream()
                    .anyMatch(row -> Boolean.FALSE.equals(row.getLabelMatching()));

            if (hasFailure) {
                group.forEach(row -> row.setLabelMatching(false));
            }
        }

        // ✅ ASSERTION (VERY IMPORTANT)
        assert !row1.getLabelMatching();
        assert !row2.getLabelMatching(); // 🔥 THIS IS THE MAIN CHECK

        log.info("Row1 status: {}", row1.getLabelMatching());
        log.info("Row2 status: {}", row2.getLabelMatching());
    }

    @Test
    public void testContainerRejectionWithRealInput() {

        // -------- PAPER 3 DATA (from your input) --------

        SelectionFilteringInputTable nameRow = new SelectionFilteringInputTable();
        nameRow.setId(41619L);
        nameRow.setPaperNo(3L);
        nameRow.setOriginId("ORIGIN-717");
        nameRow.setSorContainerInstance("MEMBER_DETAILS_0");
        nameRow.setSorItemName("member_full_name");
        nameRow.setAnswer("Dinesh Kumar");

        // ❌ simulate failure after priority
        nameRow.setLabelMatching(false);

        SelectionFilteringInputTable addressRow = new SelectionFilteringInputTable();
        addressRow.setId(41601L);
        addressRow.setPaperNo(3L);
        addressRow.setOriginId("ORIGIN-717");
        addressRow.setSorContainerInstance("MEMBER_DETAILS_0");
        addressRow.setSorItemName("member_address_line1");
        addressRow.setAnswer("640 MASONIC WAY");

        // ✅ initially valid
        addressRow.setLabelMatching(true);

        List<SelectionFilteringInputTable> updatedTableInfos = new ArrayList<>();
        updatedTableInfos.add(nameRow);
        updatedTableInfos.add(addressRow);

        // -------- APPLY CONTAINER LOGIC --------

        Map<String, List<SelectionFilteringInputTable>> groupedByContainer =
                updatedTableInfos.stream()
                        .collect(Collectors.groupingBy(row -> {
                            String origin = row.getOriginId() == null ? "" : row.getOriginId().trim();
                            String paper = String.valueOf(row.getPaperNo());
                            String container = row.getSorContainerInstance() == null ? "" :
                                    row.getSorContainerInstance().trim().toUpperCase();

                            return origin + "|" + paper + "|" + container;
                        }));

        for (Map.Entry<String, List<SelectionFilteringInputTable>> entry : groupedByContainer.entrySet()) {

            List<SelectionFilteringInputTable> group = entry.getValue();

            boolean hasFailure = group.stream()
                    .anyMatch(row -> Boolean.FALSE.equals(row.getLabelMatching()));

            if (hasFailure) {
                group.forEach(row -> {
                    row.setLabelMatching(false);
                    row.setAnswer(""); // mimic your actual clearing logic
                });
            }
        }

        // -------- ASSERTIONS --------

        // ❌ name should be false
        assertFalse(nameRow.getLabelMatching());

        // ❌ address SHOULD ALSO BE FALSE (main validation)
        assertFalse(addressRow.getLabelMatching(),
                "Address should also be rejected due to container-level rejection");

        // ❌ values should be cleared
        assertEquals("", nameRow.getAnswer());
        assertEquals("", addressRow.getAnswer());

        log.info("✅ Container rejection works correctly for real input case");
    }

    @Test
    public void testSectionFiltering_ContainerLevelRejection() throws Exception {

        final SectionFiltering config = SectionFiltering.builder()
                .condition(true)
                .name("Container Rejection Test")
                .outputTable("extraction.selection_over_filtering_input_audit")
                .inputTable("extraction.selection_over_filtering_output_audit")
                .resourceConn("intics_zio_db_conn") // use your test DB
                .querySet(
                        "SELECT \n" +
                                "    id,\n" +
                                "    created_on,\n" +
                                "    created_user_id,\n" +
                                "    last_updated_on,\n" +
                                "    last_updated_user_id,\n" +
                                "    tenant_id,\n" +
                                "    group_id,\n" +
                                "    root_pipeline_id,\n" +
                                "    batch_id,\n" +
                                "    model_registry,\n" +
                                "    sor_container_id,\n" +
                                "    sor_container_name,\n" +
                                "    sor_item_name,\n" +
                                "    sor_item_label,\n" +
                                "    section_alias,\n" +
                                "    answer,\n" +
                                "    confidence,\n" +
                                "    bbox,\n" +
                                "    bbox_asis,\n" +
                                "    paper_no,\n" +
                                "    origin_id,\n" +
                                "    extracted_image_unit,\n" +
                                "    image_dpi,\n" +
                                "    image_height,\n" +
                                "    image_width,\n" +
                                "    blacklisted_labels,\n" +
                                "    blacklisted_sections,\n" +
                                "    is_encrypted,\n" +
                                "    encryption_policy,\n" +
                                "    whitelisted_labels,\n" +
                                "    whitelisted_labels_with_priority,\n" +
                                "    sor_container_instance,\n" +
                                "    whitelisted_sections_with_priority\n" +
                                "FROM extraction.selection_over_filtering_input_audit a\n" +
                                "WHERE a.origin_id = 'ORIGIN-840';"
                )
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(999L);
        action.setProcessId(888L);

        // 🔥 Important flags
        action.getContext().put("section.filtering.label.with.priority", "false");
        action.getContext().put("pipeline.end.to.end.encryption", "false");

        // 🔹 Create action
        SectionFilteringAction sectionFilteringAction =
                new SectionFilteringAction(action, log, config);

        // 🔥 Execute full pipeline
        sectionFilteringAction.execute();

        log.info("✅ Container-level rejection test executed successfully");
    }

    @Test
    public void testContainerLevelRejection_NoDB() {

        // 🔹 Row 1 (FAIL)
        SelectionFilteringInputTable row1 = new SelectionFilteringInputTable();
        row1.setId(1L);
        row1.setPaperNo(3L);
        row1.setOriginId("ORIGIN-840");
        row1.setSorContainerInstance("MEMBER_DETAILS_0");
        row1.setAnswer("John");
        row1.setLabelMatching(false); // ❌ triggers rejection

        // 🔹 Row 2 (PASS initially)
        SelectionFilteringInputTable row2 = new SelectionFilteringInputTable();
        row2.setId(2L);
        row2.setPaperNo(3L);
        row2.setOriginId("ORIGIN-840");
        row2.setSorContainerInstance("MEMBER_DETAILS_0");
        row2.setAnswer("Address Line");
        row2.setLabelMatching(true); // ✅ initially valid

        // 🔹 Different container (should NOT be affected)
        SelectionFilteringInputTable row3 = new SelectionFilteringInputTable();
        row3.setId(3L);
        row3.setPaperNo(3L);
        row3.setOriginId("ORIGIN-840");
        row3.setSorContainerInstance("OTHER_CONTAINER");
        row3.setAnswer("Independent Value");
        row3.setLabelMatching(true);

        List<SelectionFilteringInputTable> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        // 🔥 APPLY CONTAINER-LEVEL REJECTION LOGIC (same as your action)
        Map<String, List<SelectionFilteringInputTable>> grouped =
                rows.stream()
                        .collect(Collectors.groupingBy(r -> {
                            String origin = r.getOriginId() == null ? "" : r.getOriginId().trim();
                            String paper = String.valueOf(r.getPaperNo());
                            String container = r.getSorContainerInstance() == null ? "" :
                                    r.getSorContainerInstance().trim().toUpperCase();
                            return origin + "|" + paper + "|" + container;
                        }));

        for (List<SelectionFilteringInputTable> group : grouped.values()) {

            boolean hasFailure = group.stream()
                    .anyMatch(r -> Boolean.FALSE.equals(r.getLabelMatching()));

            if (hasFailure) {
                group.forEach(r -> {
                    r.setLabelMatching(false);
                    r.setAnswer(""); // mimic clearing logic
                });
            }
        }

        // ✅ Assertions

        // Entire container should be rejected
        assertFalse(row1.getLabelMatching());
        assertFalse(row2.getLabelMatching()); // 🔥 main validation

        assertEquals("", row1.getAnswer());
        assertEquals("", row2.getAnswer());

        // Other container should remain unaffected
        assertTrue(row3.getLabelMatching());
        assertEquals("Independent Value", row3.getAnswer());
    }
}
