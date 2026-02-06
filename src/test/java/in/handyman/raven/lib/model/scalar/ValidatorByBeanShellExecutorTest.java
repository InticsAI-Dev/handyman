package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
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

    @BeforeEach
    void setUp() throws IOException {
        inputList = new ArrayList<>();
        contextMap = new HashMap<>();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("ProviderZipCodeMapper", getProviderZipCodeMapper());
        actionExecutionAudit.getContext().put("ProviderNpiTinValidator", getProviderNpiTinValidator());
        actionExecutionAudit.getContext().put("ProviderAddressMapper", getProviderAddressMapper());
        actionExecutionAudit.getContext().put("ServiceToDateMapper", getServiceToDateMapper());
        actionExecutionAudit.getContext().put("NewBornRequestOCRMapper", getNewBornRequestOCRMapper());
        actionExecutionAudit.getContext().put("NewBornRequestMapper", getNewBornRequestMapper());
        actionExecutionAudit.getContext().put("NewbornNameMapper", getNewbornNameMapper());
        actionExecutionAudit.getContext().put("NewbornGenderMapper", getNewbornGenderMapper());
        actionExecutionAudit.getContext().put("NewbornDOBMapper", getNewbornDOBMapper());
        actionExecutionAudit.getContext().put("MemberZipcodeMapper", getMemberZipcodeMapper());
        actionExecutionAudit.getContext().put("MemberIdValidator", getMemberIdValidator());
        actionExecutionAudit.getContext().put("MemberDOBandServiceFromDateMapper",
                getMemberDOBandServiceFromDateMapper());
        actionExecutionAudit.getContext().put("MemberAddressMapper", getMemberAddressMapper());
        actionExecutionAudit.getContext().put("MedicaidMemberIdValidator", getMedicaidMemberIdValidator());
        actionExecutionAudit.getContext().put("FaxFromDateMapper", getFaxFromDateMapper());
        actionExecutionAudit.getContext().put("ClinicalPresentProcessor", getClinicalPresentProcessor());
        actionExecutionAudit.getContext().put("AuthDischargeDateValidator", getAuthDischargeDateValidator());
        actionExecutionAudit.getContext().put("AumiMultiMemberMapper", getAumiMultiMemberMapper());
        actionExecutionAudit.getContext().put("AumiMemberNameMapper", getAumiMemberNameMapper());
        actionExecutionAudit.getContext().put("AumiGenderMapper", getAumiGenderMapper());
        actionExecutionAudit.getContext().put("LOSValidator", getLOSValidator());
        actionExecutionAudit.getContext().put("LOSGBDValidator", getLOSGBDValidator());
        actionExecutionAudit.getContext().put("DiagnosisServiceCodeValidator",
                getDiagnosisServiceCodeValidator());
        actionExecutionAudit.getContext().put("AuthIdValidator", getAuthIdValidator());
        actionExecutionAudit.getContext().put("FaxReportProcessor", getFaxReportProcessor());

        // Default Context
        contextMap.put("multi.line.item.activator", "true");

        // Initialize Validator with mocked logger

        validator = new ValidatorByBeanShellExecutor(inputList, actionExecutionAudit, log, 2);
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


        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_full_name", "Exlsie, Noichole",
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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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
        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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
        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, inputMap);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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
        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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
        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, groupedSorItems);

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

        validator.executeScripts(classes, groupedSorItems);
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

        validator.executeScripts(classes, groupedSorItems);
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

        validator.executeScripts(classes, groupedSorItems);
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

        validator.executeScripts(classes, groupedSorItems);
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

        validator.executeScripts(classes, groupedSorItems);
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
        validator.executeScripts(classes, groupedSorItems);
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
        File file = new File("src/main/resources/beanshell/single_value/MemberAddressMapper.txt");
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

}
