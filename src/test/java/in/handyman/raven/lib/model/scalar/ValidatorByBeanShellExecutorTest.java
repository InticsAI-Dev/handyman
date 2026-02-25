package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwBshConfig;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import in.handyman.raven.lib.model.PostProcessingExecutor;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


@Slf4j
class ValidatorByBeanShellExecutorTest {

    private ValidatorByBeanShellExecutor validator;
    private List<PostProcessingFieldsInput> inputList;
    private Map<String, String> contextMap;
    private ActionExecutionAudit actionExecutionAudit;

    @BeforeEach
    void setUp() throws IOException {
        inputList = new ArrayList<>();
        contextMap = new HashMap<>();
         actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE

        actionExecutionAudit.getContext().put("outbound.mapper.bsh.class.order","ServiceCodeValidator");
        List<SpwBshConfig> bshConfigs = getSpwBshConfigs();
        actionExecutionAudit.getContext().put("ProviderZipCodeMapper", "ProviderZipCodeMapper");
        actionExecutionAudit.getContext().put("ProviderNpiTinValidator", "ProviderNpiTinValidator");
        actionExecutionAudit.getContext().put("ProviderAddressMapper", "ProviderAddressMapper");
        actionExecutionAudit.getContext().put("ServiceToDateMapper", "ServiceToDateMapper");
        actionExecutionAudit.getContext().put("NewBornRequestOCRMapper", "NewBornRequestOCRMapper");
        actionExecutionAudit.getContext().put("NewBornRequestMapper", "NewBornRequestMapper");
        actionExecutionAudit.getContext().put("NewbornNameMapper", "NewbornNameMapper");
        actionExecutionAudit.getContext().put("NewbornGenderMapper", "NewbornGenderMapper");
        actionExecutionAudit.getContext().put("NewbornDOBMapper", "NewbornDOBMapper");
        actionExecutionAudit.getContext().put("MemberZipcodeMapper", "MemberZipcodeMapper");
        actionExecutionAudit.getContext().put("MemberIdValidator", "MemberIdValidator");
        actionExecutionAudit.getContext().put("MemberDOBandServiceFromDateMapper",
                "MemberDOBandServiceFromDateMapper");
        actionExecutionAudit.getContext().put("MemberAddressMapper", "MemberAddressMapper");
        actionExecutionAudit.getContext().put("MedicaidMemberIdValidator", "MedicaidMemberIdValidator");
        actionExecutionAudit.getContext().put("FaxFromDateMapper", "FaxFromDateMapper");
        actionExecutionAudit.getContext().put("ClinicalPresentProcessor", "ClinicalPresentProcessor");
        actionExecutionAudit.getContext().put("AuthDischargeDateValidator", "AuthDischargeDateValidator");
        actionExecutionAudit.getContext().put("AumiMultiMemberMapper", "AumiMultiMemberMapper");
        actionExecutionAudit.getContext().put("AumiMemberNameMapper", "AumiMemberNameMapper");
        actionExecutionAudit.getContext().put("AumiGenderMapper", "AumiGenderMapper");
        actionExecutionAudit.getContext().put("LOSValidator", "LOSValidator");
        actionExecutionAudit.getContext().put("LOSGBDValidator", "LOSGBDValidator");
        actionExecutionAudit.getContext().put("DiagnosisServiceCodeValidator",
                "DiagnosisServiceCodeValidator");
        actionExecutionAudit.getContext().put("AuthIdValidator", "AuthIdValidator");
        actionExecutionAudit.getContext().put("FaxReportProcessor", "FaxReportProcessor");
        actionExecutionAudit.getContext().put("ServiceCodeValidator", "ServiceCodeValidator");
        actionExecutionAudit.getContext().put("tenant_id","1");
        actionExecutionAudit.setRootPipelineId(1L);
        // Default Context
        contextMap.put("multi.line.item.activator", "true");

        validator = new ValidatorByBeanShellExecutor(inputList, actionExecutionAudit, log, 2, bshConfigs);

    }

    @Test
    public void executeTest() throws Exception {

        // Initialize Validator with mocked logger
        PostProcessingExecutor postProcessingExecutor=new PostProcessingExecutor();
        postProcessingExecutor.setName("TestExecutor");
        postProcessingExecutor.setCondition(true);
        postProcessingExecutor.setBatchId("BATCH-2");
        postProcessingExecutor.setResourceConn("intics_zio_db_conn");
        postProcessingExecutor.setGroupId("GROUP-2");
        postProcessingExecutor.setOutputTable("sor_transform.vqa_transaction_post_processing_output");
        postProcessingExecutor.setQuerySet("select\n" +
                "                    vqa_id, transaction_id, created_on, created_user_id, last_updated_on, last_updated_user_id, root_pipeline_id, tenant_id,\n" +
                "                    document_id, group_id, batch_id, origin_id, paper_no, truth_id, status, stage, message, version, extracted_image_unit,\n" +
                "                    image_dpi, image_height, image_width, section_priority_after_filter, sor_container_id, sor_container_name, sor_container_instance,\n" +
                "                    sor_item_name, sor_item_id, sor_item_attribution_id, model_id, model_info, model_registry, model_registry_id, answer, vqa_score,\n" +
                "                    score, b_box, label, section_alias, synonym_id, sor_synonym, question_id, sor_question, weight, category, line_item_type,\n" +
                "                    is_multi_entity_enabled, encryption_policy, is_encrypted, post_processing_code, post_processing_key, aggregated_score\n" +
                "                   from sor_transform.vqa_transaction_post_processing_input\t\n" +
                "where origin_id='ORIGIN-118' and sor_container_name ='SERVICE_CODE' and post_processing_field_id in (5014,5019,5020,5021);");
        PostProcessingExecutorAction postProcessingExecutorAction = new PostProcessingExecutorAction(actionExecutionAudit, log, postProcessingExecutor);
        postProcessingExecutorAction.execute();
    }


    private PostProcessingFieldsInput createInput(Integer id, String originId, Integer paperNo, String lineItemType,
                                                  String sorContainerInstance, String sorItemName, String answer, String sectionAlias,
                                                  String label,
                                                  Double vqaScore) {
        PostProcessingFieldsInput input = new PostProcessingFieldsInput();
        input.setPostProcessingFieldId(id);
        input.setOriginId(originId);
        input.setPaperNo(paperNo);
        input.setLineItemType(lineItemType);
        input.setSorContainerInstance(sorContainerInstance);
        input.setSorItemName(sorItemName);
        input.setAnswer(answer);
        input.setLabel(label);
        input.setSectionAlias(sectionAlias);
        input.setBBox("{}");
        input.setVqaScore(vqaScore);
        input.setAggregatedScore(0.95);
        return input;
    }

    @Test
    void testExecuteScriptsMemberNameMapper() {
        log.info("Test Scenario: Execute BeanShell Scripts for member name");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        List<String> classes = Collections.singletonList("AumiMemberNameMapper");
        log.info("First and last name split - Command separated");


        inputList.add(createInput(1, "O1", 1, "single_value", "MEMBER_0", "member_full_name", "Exlsie, Noichole",
                "Patient Info", "full name", 100.0));
        inputList.add(createInput(2, "O1", 1, "single_value", "MEMBER_0", "member_first_name", "", "", "", 0.0));
        inputList.add(createInput(3, "O1", 1, "single_value", "MEMBER_0", "member_last_name", "", "", "", 0.0));

        inputList.forEach((postProcessingFieldsInput) -> {

            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });

    }

    private Map<String, List<PostProcessingFieldsInput>> getResult(List<String> classes, Map<String, List<PostProcessingFieldsInput>> groupedSorItems) {
        Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>(groupedSorItems);
        String originId ="O1";
        Integer pageNo = 1;
        String containerInstance = "0";
        return validator.executeScripts(classes, currentMap, originId, pageNo, containerInstance);
    }


    @Test
    void testExecuteScriptsMemberAddressMapper() {
        log.info("Test Scenario: Execute BeanShell Scripts for member name");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        List<String> classes = Collections.singletonList("MemberAddressMapper");
        log.info("First and last name split - Command separated");


        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_address_line1", "44c114 Express PoInt, Plano, TX 75074",
                "Patient Info", "full name", 100.0));
        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_city", "", "", "", 0.0));
        inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_zipcode", "", "", "", 0.0));
        inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_state", "", "", "", 0.0));

        inputList.forEach((postProcessingFieldsInput) -> {

            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });


    }

    @Test
    void testExecuteScriptsMemberNameMapper1() {
        log.info("Test Scenario: Execute BeanShell Scripts1 for member name");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        List<String> classes = Collections.singletonList("AumiMemberNameMapper");

        log.info("First name and last name split - comma separated version2");

        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_full_name", "Darwin Robert, Kennedy",
                "Patient Info", "full name", 100.0));
        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_first_name", "", "", "", 0.0));
        inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_last_name", "", "", "", 0.0));

        inputList.forEach((postProcessingFieldsInput) -> {

            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });

    }

    @Test
    void testExecuteScriptsMemberID() {

        log.info("Test Scenario: Execute Member-ID BeanShell Script1");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();

        List<String> classes = Collections.singletonList("AuthIdValidator");
        inputList.add(createInput(1, "ORIGIN-1", 1,
                "multi_value", "MEMBER", "member_id", "747U05007", "Member Information", "Sub ID",
                0.95));
        // ---------- BEFORE ----------
        System.out.println("===== BEFORE PROVIDER NPI MAPPING =====");
        for (PostProcessingFieldsInput in : inputList) {
            System.out.println(
                    "Origin: " + in.getOriginId() +
                            " | Item: " + in.getSorItemName() +
                            " | Answer: " + in.getAnswer() +
                            " | Section: " + in.getSectionAlias() +
                            " | Score: " + in.getVqaScore());
        }

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);
        // ---------- EXECUTE ----------
        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== AFTER PROVIDER NPI MAPPING =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsMemberID1() {
        // TODO: Member id regex pattern split (,/)
        // TODO: Same for medicaid id
        log.info("Test Scenario: Execute Member-ID BeanShell Script2");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();

        List<String> classes = Collections.singletonList("AuthIdValidator");
        inputList.add(createInput(1, "ORIGIN-1", 1,
                "multi_value", "MEMBER", "member_id", "747U05007/747U05007213", "Member Information", "Sub ID",
                0.95));
        // ---------- BEFORE ----------
        System.out.println("===== BEFORE PROVIDER NPI MAPPING =====");
        for (PostProcessingFieldsInput in : inputList) {
            System.out.println(
                    "Origin: " + in.getOriginId() +
                            " | Item: " + in.getSorItemName() +
                            " | Answer: " + in.getAnswer() +
                            " | Section: " + in.getSectionAlias() +
                            " | Score: " + in.getVqaScore());
        }

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);
        // ---------- EXECUTE ----------
        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== AFTER PROVIDER NPI MAPPING =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsFaxReportProcessor() {
        // TODO: Complete this test case, logical confirmation - Bala
        log.info("Test Scenario: Execute FaxReportProcessor BeanShell Script");
        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();

        List<String> classes = Collections.singletonList("FaxReportProcessor");

        // Scenario 1: Valid Keyword
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "fax_report", "ADT Report", "Header",
                "fax report", 0.95));

        inputMap.put("fax_report", inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, inputMap);

        // ---------- AFTER ----------
        System.out.println("===== Fax report =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsAumiGenderMapper() {
        // TODO: Everything working as expected
        log.info("Test Scenario: Execute AumiGenderMapper BeanShell Script");
        List<PostProcessingFieldsInput> inputList = new ArrayList<>();

        List<String> classes = Collections.singletonList("AumiGenderMapper");

        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_gender", "Male", "Patient", "gender",
                0.99));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== Gender =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsAumiMemberNameMapper() {
        // TODO: Already we have the test cases
        log.info("Test Scenario: Execute AumiMemberNameMapper BeanShell Script");
        List<String> classes = Collections.singletonList("AumiMemberNameMapper");

        List<PostProcessingFieldsInput> fullList = new ArrayList<>();
        fullList.add(createInput(1, "O1", 1, "single_value", "0", "member_full_name", "Doe, John", "Patient",
                "full name", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== Name =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsAumiMultiMemberMapper() {
        // TODO: Working as expected
        log.info("Test Scenario: Execute AumiMultiMemberMapper BeanShell Script");
        List<String> classes = Collections.singletonList("AumiMultiMemberMapper");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "multiple_member_indicator", "No", "Header",
                "multi member", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

//        validator.executeScripts(classes, groupedSorItems);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== MM =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsAuthDischargeDateValidator() {
        // TODO: Working as expected
        log.info("Test Scenario: Execute AuthDischargeDateValidator BeanShell Script");
        List<String> classes = Collections.singletonList("AuthDischargeDateValidator");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "auth_discharge_date", "12/31/2023", "Body",
                "discharge date", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsAuthDischargeDateValidator1() {
        log.info("Test Scenario: Execute AuthDischargeDateValidator BeanShell Script1");
        List<String> classes = Collections.singletonList("AuthDischargeDateValidator");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "auth_discharge_date", "2023/11/1", "Body",
                "discharge date", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsClinicalPresentProcessor() {
        // TODO: Clinical present please revisit the logic both 'yes' and 'no' returns 'N'
        log.info("Test Scenario: Execute ClinicalPresentProcessor BeanShell Script");

        List<String> classes = Collections.singletonList("ClinicalPresentProcessor");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "100", "clinical_present", "Yes", "Header",
                "clinical", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== clinical present =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsFaxFromDateMapper() {
        // TODO: Not tested
        log.info("Test Scenario: Execute FaxFromDateMapper BeanShell Script");

        List<String> classes = Collections.singletonList("FaxFromDateMapper");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "fax_received_date", "01/01/2023", "Header",
                "fax date", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        // ---------- AFTER ----------
        System.out.println("===== fax received date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsMedicaidMemberIdValidator() {
        log.info("Test Scenario: Execute MedicaidMemberIdValidator BeanShell Script");

        List<String> classes = Collections.singletonList("MedicaidMemberIdValidator");

        List<PostProcessingFieldsInput> idList = new ArrayList<>();
        idList.add(createInput(1, "O1", 1, "single_value", "0", "medicaid_id", "123456789", "Body",
                "medicaid id", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(idList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Medicaid ID =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsMemberDOBandServiceFromDateMapper() {
        // TODO: Date formation failed '1980-01-01'
        log.info("Test Scenario: Execute MemberDOBandServiceFromDateMapper BeanShell Script");

        List<String> classes = Collections.singletonList("MemberDOBandServiceFromDateMapper");

        List<PostProcessingFieldsInput> dobList = new ArrayList<>();
        dobList.add(createInput(1, "O1", 1, "single_value", "0", "member_date_of_birth", "01/01/1980", "Body",
                "dob", 0.95));

        dobList.add(createInput(2, "O1", 1, "single_value", "0", "service_from_date", "01/01/2023", "Body",
                "service date", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(dobList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== ID =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsMemberIdValidator() {
        // TODO: Complete alphabets should be marked as empty
        log.info("Test Scenario: Execute MemberIdValidator BeanShell Script");
        List<String> classes = Collections.singletonList("MemberIdValidator");

        List<PostProcessingFieldsInput> idList = new ArrayList<>();
        idList.add(createInput(1, "O1", 1, "single_value", "0", "member_id", "AHGSFASUFHIUAF", "Body", "member id",
                0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(idList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsMemberZipcodeMapper() {
        // TODO: zip code must be alpha numerical not completely alphabets
        log.info("Test Scenario: Execute MemberZipcodeMapper BeanShell Script");
        List<String> classes = Collections.singletonList("MemberZipcodeMapper");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_zipcode", "ahfihaif", "Body", "zip",
                0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(inputList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Zip =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsNewBornRequestMapper() {
        // TODO: 'yes' -> 'Y' or 'No' -> 'N'
        log.info("Test Scenario: Execute NewBornRequestMapper BeanShell Script");
        List<String> classes = Collections.singletonList("NewBornRequestMapper");

        List<PostProcessingFieldsInput> reqList = new ArrayList<>();
        reqList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_request", "Yes", "Body", "request",
                0.95));

        reqList.add(createInput(2, "O1", 1, "single_value", "0", "newborn_request_prompt", "Yes", "Body",
                "prompt", 0.95));

        reqList.add(createInput(3, "O1", 1, "single_value", "0", "newborn_request_ocr", "Yes", "Body", "ocr",
                0.95));


        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(reqList);
        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsNewBornRequestOCRMapper() {
        log.info("Test Scenario: Execute NewBornRequestOCRMapper BeanShell Script");
        List<String> classes = Collections.singletonList("NewBornRequestOCRMapper");

        List<PostProcessingFieldsInput> ocrList = new ArrayList<>();
        ocrList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_request_ocr",
                "Some text containing keywords", "Body", "ocr", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(ocrList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsNewbornDOBMapper() {
        // TODO: dob format issue
        log.info("Test Scenario: Execute NewbornDOBMapper BeanShell Script");
        List<String> classes = Collections.singletonList("NewbornDOBMapper");

        List<PostProcessingFieldsInput> dobList = new ArrayList<>();
        dobList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_date_of_birth", "01/01/2023", "Body",
                "dob", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(dobList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== DOB =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsNewbornGenderMapper() {
        log.info("Test Scenario: Execute NewbornGenderMapper BeanShell Script");
        List<String> classes = Collections.singletonList("NewbornGenderMapper");

        List<PostProcessingFieldsInput> genderList = new ArrayList<>();
        genderList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_gender", "Male", "Body", "gender",
                0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(genderList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Gender =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsNewbornNameMapper() {
        // TODO: Check the logic once
        log.info("Test Scenario: Execute NewbornNameMapper BeanShell Script");
        List<String> classes = Collections.singletonList("NewbornNameMapper");

        List<PostProcessingFieldsInput> fullList = new ArrayList<>();
        fullList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_full_name", "Baby Doe", "Body",
                "full name", 0.95));
        fullList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_first_name", "", "",
                "full name", 0.95));
        fullList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_last_name", "", "",
                "full name", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(fullList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Name =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsProviderAddressMapper() {
        log.info("Test Scenario: Execute ProviderAddressMapper BeanShell Script");
        List<String> classes = Collections.singletonList("ProviderAddressMapper");

        List<PostProcessingFieldsInput> addrList = new ArrayList<>();
        addrList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_address_line1",
                "123 Main St", "Body", "address", 0.95));
        addrList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_city",
                "", "", "", 0.95));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(addrList);
        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Address =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsProviderNpiTinValidator() {
            // TODO: hyphen case not handled
        log.info("Test Scenario: Execute ProviderNpiTinValidator BeanShell Script");
        List<String> classes = Collections.singletonList("ProviderNpiTinValidator");

        List<PostProcessingFieldsInput> npiList = new ArrayList<>();
        npiList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_npi", "1234567890", "Body",
                "npi", 0.95));


        npiList.add(createInput(2, "O1", 1, "single_value", "0", "servicing_provider_tin", "12-3456789", "Body",
                "tin", 0.95));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(npiList);

        Map<String, List<PostProcessingFieldsInput>> result = getResult(classes, groupedSorItems);

        System.out.println("===== Date =====");
        result.forEach((s, postProcessingFieldsInputs) -> {
            for (PostProcessingFieldsInput in : postProcessingFieldsInputs) {
                System.out.println(
                        "Origin: " + in.getOriginId() +
                                " | Item: " + in.getSorItemName() +
                                " | Answer: " + in.getAnswer() +
                                " | Section: " + in.getSectionAlias() +
                                " | Score: " + in.getVqaScore());
            }
        });
    }

    @Test
    void testExecuteScriptsProviderZipCodeMapper() {
        log.info("Test Scenario: Execute ProviderZipCodeMapper BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("ProviderZipCodeMapper");

        List<PostProcessingFieldsInput> zipList = new ArrayList<>();
        zipList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_zipcode", "12345", "Body",
                "zip", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(zipList);

        getResult(classes, groupedSorItems);
    }

    @Test
    void testExecuteScriptsServiceToDateMapper() {
        log.info("Test Scenario: Execute ServiceToDateMapper BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("ServiceToDateMapper");

        List<PostProcessingFieldsInput> dateList = new ArrayList<>();
        dateList.add(createInput(1, "O1", 1, "single_value", "0", "service_to_date", "01/01/2023", "Body",
                "date", 0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(dateList);

        getResult(classes, groupedSorItems);
    }

    @Test
    void testExecuteScriptsDiagnosisServiceCodeValidator() {
        log.info("Test Scenario: Execute DiagnosisServiceCodeValidator BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("DiagnosisServiceCodeValidator");

        List<PostProcessingFieldsInput> svcList = new ArrayList<>();
        svcList.add(createInput(1, "O1", 1, "multi_value", "0", "service_code", "99213", "Body", "service code",
                0.95));

        svcList.add(createInput(2, "O1", 1, "multi_value", "0", "diagnosis_code", "R05.9", "Body", "diag code",
                0.95));
        svcList.add(createInput(3, "O1", 1, "multi_value", "0", "service_quantity_units", "", "", "",
                0.95));
        svcList.add(createInput(4, "O1", 1, "multi_value", "0", "service_quantity_visits", "", "", "",
                0.95));
        svcList.add(createInput(5, "O1", 1, "multi_value", "0", "service_code_modifier", "", "", "",
                0.95));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(svcList);

        getResult(classes, groupedSorItems);
    }

    @Test
    void testExecuteScriptsLOSValidator() {
        log.info("Test Scenario: Execute LOSValidator BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("LOSValidator");

        List<PostProcessingFieldsInput> losList = new ArrayList<>();
        losList.add(createInput(1, "O1", 1, "multi_value", "0", "level_of_service", "Urgent", "Header", "los",
                0.95));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(losList);

        getResult(classes, groupedSorItems);
    }

    @Test
    void testExecuteScriptsLOSGBDValidator() {
        log.info("Test Scenario: Execute LOSGBDValidator BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("LOSGBDValidator");

        List<PostProcessingFieldsInput> losList = new ArrayList<>();
        losList.add(createInput(1, "O1", 1, "multi_value", "0", "level_of_service", "Urgent", "Header", "los",
                0.95));
        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(losList);

        getResult(classes, groupedSorItems);
    }

    @Test
    void testExecuteScriptsAuthIdValidator() {
        log.info("Test Scenario: Execute AuthIdValidator BeanShell Script");
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
        List<String> classes = Collections.singletonList("AuthIdValidator");

        List<PostProcessingFieldsInput> authList = new ArrayList<>();
        authList.add(createInput(1, "O1", 1, "multi_value", "0", "auth_id", "UM12345678", "Body", "auth id",
                0.95));

        authList.add(createInput(1, "O1", 1, "single_value", "0", "member_id", "", "Body", "auth id",
                0.95));

        authList.add(createInput(1, "O1", 1, "single _value", "0", "additional_auth_properties", "", "Body", "auth id",
                0.95));

        Map<String, List<PostProcessingFieldsInput>> groupedSorItems = validator.groupBySorItemNames(authList);
        getResult(classes, groupedSorItems);
    }

    String getProviderZipCodeMapper() throws IOException {

        // read the code from a file

            File file = new File("src/main/resources/beanshell/single_value/ProviderZipCodeMapper.txt");
            return fileReader(file);

    }

    String getProviderNpiTinValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/ProviderNpiTinValidator.txt");
        return fileReader(file);
    }

    String getProviderAddressMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/ProviderAddressMapper.txt");
        return fileReader(file);
    }

    String getServiceToDateMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/ServiceToDateMapper.txt");
        return fileReader(file);
    }

    String getNewBornRequestOCRMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/NewBornRequestOCRMapper.txt");
        return fileReader(file);
    }

    String getNewBornRequestMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/NewBornRequestMapper.txt");
        return fileReader(file);
    }

    String getNewbornNameMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/NewbornNameMapper.txt");
        return fileReader(file);
    }

    String getNewbornGenderMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/NewbornGenderMapper.txt");
        return fileReader(file);
    }

    String getNewbornDOBMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/NewbornDOBMapper.txt");
        return fileReader(file);
    }

    String getMemberZipcodeMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/MemberZipcodeMapper.txt");
        return fileReader(file);
    }

    String getMemberIdValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/MemberIdValidator.txt");
        return fileReader(file);
    }

    String getMemberDOBandServiceFromDateMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/MemberDOBandServiceFromDateMapper.txt");
        return fileReader(file);
    }

    String getMemberAddressMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/MemberAddressMapper.java");
        return fileReader(file);
    }

    String getMedicaidMemberIdValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/MedicaidMemberIdValidator.txt");
        return fileReader(file);
    }

    String getFaxFromDateMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/FaxFromDateMapper.txt");
        return fileReader(file);
    }

    String getClinicalPresentProcessor() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/ClinicalPresentProcessor.txt");
        return fileReader(file);
    }

    String getAuthDischargeDateValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/AuthDischargeDateValidator.txt");
        return fileReader(file);
    }

    String getAumiMultiMemberMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/AumiMultiMemberMapper.txt");
        return fileReader(file);
    }

    String getAumiMemberNameMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/AumiMemberNameMapper.txt");
        return fileReader(file);
    }

    String getAumiGenderMapper() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/single_value/AumiGenderMapper.txt");
        return fileReader(file);
    }

    String getLOSValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/multi_value/LOSValidator.txt");
        return fileReader(file);
    }

    String getFaxReportProcessor() throws IOException {
        File file = new File("src/main/resources/beanshell/single_value/FaxReportProcessor.txt");
        return fileReader(file);
    }

    String getLOSGBDValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/multi_value/LOSGBDValidator.txt");
        return fileReader(file);
    }

    String getDiagnosisServiceCodeValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/multi_value/DiagnosisServiceCodeValidator.txt");
        return fileReader(file);
    }

    String getAuthIdValidator() throws IOException {
        // read the code from a file
        File file = new File("src/main/resources/beanshell/multi_value/AuthIdValidator.txt");
        return fileReader(file);
    }

    @NotNull
    private String fileReader(File file) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");

            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    @NotNull
    public List<SpwBshConfig> getSpwBshConfigs() throws IOException {

        List<SpwBshConfig> bshConfigs = new ArrayList<>();

        bshConfigs.add(SpwBshConfig.builder()
                .className("ProviderZipCodeMapper")
                .callerName("ProviderZipCodeMapper")
                .sourceCode(getProviderZipCodeMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("ProviderNpiTinValidator")
                .callerName("ProviderNpiTinValidator")
                .sourceCode(getProviderNpiTinValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("ProviderAddressMapper")
                .callerName("ProviderAddressMapper")
                .sourceCode(getProviderAddressMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("ServiceToDateMapper")
                .callerName("ServiceToDateMapper")
                .sourceCode(getServiceToDateMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("NewBornRequestOCRMapper")
                .callerName("NewBornRequestOCRMapper")
                .sourceCode(getNewBornRequestOCRMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("NewBornRequestMapper")
                .callerName("NewBornRequestMapper")
                .sourceCode(getNewBornRequestMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("NewbornNameMapper")
                .callerName("NewbornNameMapper")
                .sourceCode(getNewbornNameMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("NewbornGenderMapper")
                .callerName("NewbornGenderMapper")
                .sourceCode(getNewbornGenderMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("NewbornDOBMapper")
                .callerName("NewbornDOBMapper")
                .sourceCode(getNewbornDOBMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("MemberZipcodeMapper")
                .callerName("MemberZipcodeMapper")
                .sourceCode(getMemberZipcodeMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("MemberIdValidator")
                .callerName("MemberIdValidator")
                .sourceCode(getMemberIdValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("MemberDOBandServiceFromDateMapper")
                .callerName("MemberDOBandServiceFromDateMapper")
                .sourceCode(getMemberDOBandServiceFromDateMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("MemberAddressMapper")
                .callerName("MemberAddressMapper")
                .sourceCode(getMemberAddressMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("MedicaidMemberIdValidator")
                .callerName("MedicaidMemberIdValidator")
                .sourceCode(getMedicaidMemberIdValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("FaxFromDateMapper")
                .callerName("FaxFromDateMapper")
                .sourceCode(getFaxFromDateMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("ClinicalPresentProcessor")
                .callerName("ClinicalPresentProcessor")
                .sourceCode(getClinicalPresentProcessor())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("AuthDischargeDateValidator")
                .callerName("AuthDischargeDateValidator")
                .sourceCode(getAuthDischargeDateValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("AumiMultiMemberMapper")
                .callerName("AumiMultiMemberMapper")
                .sourceCode(getAumiMultiMemberMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("AumiMemberNameMapper")
                .callerName("AumiMemberNameMapper")
                .sourceCode(getAumiMemberNameMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("AumiGenderMapper")
                .callerName("AumiGenderMapper")
                .sourceCode(getAumiGenderMapper())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("LOSValidator")
                .callerName("LOSValidator")
                .sourceCode(getLOSValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("LOSGBDValidator")
                .callerName("LOSGBDValidator")
                .sourceCode(getLOSGBDValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("DiagnosisServiceCodeValidator")
                .callerName("DiagnosisServiceCodeValidator")
                .sourceCode(getDiagnosisServiceCodeValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("AuthIdValidator")
                .callerName("AuthIdValidator")
                .sourceCode(getAuthIdValidator())
                .build());

        bshConfigs.add(SpwBshConfig.builder()
                .className("FaxReportProcessor")
                .callerName("FaxReportProcessor")
                .sourceCode(getFaxReportProcessor())
                .build());

        return bshConfigs;
    }


}
