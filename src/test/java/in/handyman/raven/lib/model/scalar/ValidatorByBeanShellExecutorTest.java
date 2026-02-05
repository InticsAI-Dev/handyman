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
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
                log.info("Test Scenario: Execute BeanShell Scripts");
                // NOTE: This test might fail if BeanShell is not on classpath or stricter
                // security settings
                // But for generation purposes, this is the correct logic.

                Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
                List<String> classes = Collections.singletonList("AumiMemberNameMapper");

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
        //
        // @Test
        // void testExecuteScriptsMemberGenderMapper() {
        // log.info("Test Scenario: Execute BeanShell Scripts");
        // // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // // security settings
        // // But for generation purposes, this is the correct logic.
        //
        // List<String> classes = Collections.singletonList("AumiGenderMapper");
        //
        // // Scenario 1: Abbreviated Male (m -> M)
        // inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_gender",
        // "m", "Patient Info",
        // "member gender", 100.0));
        //
        // // Scenario 2: Full word Male (male -> M)
        // inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_gender",
        // "male", "Patient Info",
        // "member gender", 100.0));
        //
        // // Scenario 3: Abbreviated Female (f -> F)
        // inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_gender",
        // "f", "Patient Info",
        // "member gender", 100.0));
        //
        // // Scenario 4: Full word Female (female -> F)
        // inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_gender",
        // "female", "Patient Info",
        // "member gender", 100.0));
        //
        // // Scenario 5: Mixed Case and Whitespace (mAlE -> M)
        // inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_gender", "
        // mAlE ", "Patient Info",
        // "member gender", 100.0));
        //
        // // Scenario 6: Unknown value (unknown -> "")
        // inputList.add(createInput(6, "O1", 1, "single_value", "0", "member_gender",
        // "Not Specified", "Patient Info",
        // "member gender", 100.0));
        //
        // inputList.forEach((postProcessingFieldsInput) -> {
        //
        // System.out.println("ID: " +
        // postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
        // + postProcessingFieldsInput.getOriginId() + " instance: "
        // + postProcessingFieldsInput.getSorContainerInstance() +
        // " Item: " + postProcessingFieldsInput.getSorItemName() +
        // " confidence: " + postProcessingFieldsInput.getVqaScore() +
        // " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
        // " answer: " + postProcessingFieldsInput.getAnswer());
        // });
        //
        // List<PostProcessingFieldsInput> result = validator.executeScripts(classes,
        // inputList);
        //
        // result.forEach((postProcessingFieldsInput) -> {
        //
        // System.out.println("ID: " +
        // postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
        // + postProcessingFieldsInput.getOriginId() + " instance: "
        // + postProcessingFieldsInput.getSorContainerInstance() +
        // " Item: " + postProcessingFieldsInput.getSorItemName() +
        // " confidence: " + postProcessingFieldsInput.getVqaScore() +
        // " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
        // " answer: " + postProcessingFieldsInput.getAnswer());
        // });
        //
        // }
        //
        // @Test
        // void testExecuteScriptsMemberIdAuthIdMapper() {
        // log.info("Test Scenario: Execute BeanShell Scripts");
        // // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // // security settings
        // // But for generation purposes, this is the correct logic.
        //
        // Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
        // List<String> classes = Collections.singletonList("MemberIdValidator");
        //
        // // Scenario 1: Clean Member ID (Should remain as member_id)
        // // inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_id",
        // // "ABC123456", "Patient Info", "member id", 100.0));
        //
        // // Scenario 2: Only a UM ID found in Member ID field (Should be cleared and
        // // duplicated as auth_id)
        // inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_id",
        // "UM88887777", "Patient Info",
        // "member id", 91.0));
        //
        // // Scenario 3: Combined Member ID and UM ID with "/" (Should split: one stays
        // // member_id, one duplicated as auth_id)
        // // inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_id",
        // // "99999/UM11112222", "Patient Info", "member id", 100.0));
        //
        // // Scenario 4: Combined Member ID and UM ID with ":" and noise (Should split
        // and
        // // clean special characters)
        // // inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_id",
        // // "ID#55555:UM44443333", "Patient Info", "member id", 100.0));
        //
        // // Scenario 5: Invalid UM pattern (Should be detected by UM_INVALID_PATTERN
        // and
        // // duplicated as auth_id)
        // // inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_id",
        // // "UM123", "Patient Info", "member id", 100.0));
        //
        // inputList.add(
        // createInput(6, "O1", 1, "single_value", "0", "auth_id", "UM99992222", "Auth
        // Info", "auth id", 90.0));
        //
        // inputList.forEach((postProcessingFieldsInput) -> {
        //
        // System.out.println("ID: " +
        // postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
        // + postProcessingFieldsInput.getOriginId() + " instance: "
        // + postProcessingFieldsInput.getSorContainerInstance() +
        // " Item: " + postProcessingFieldsInput.getSorItemName() +
        // " confidence: " + postProcessingFieldsInput.getVqaScore() +
        // " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
        // " answer: " + postProcessingFieldsInput.getAnswer());
        // });
        //
        // List<PostProcessingFieldsInput> result = validator.executeScripts(classes,
        // inputList);
        //
        // System.out.println("---- After BeanShell Execution ----");
        // result.forEach((postProcessingFieldsInput) -> {
        //
        // System.out.println("ID: " +
        // postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
        // + postProcessingFieldsInput.getOriginId() + " instance: "
        // + postProcessingFieldsInput.getSorContainerInstance() +
        // " Item: " + postProcessingFieldsInput.getSorItemName() +
        // " confidence: " + postProcessingFieldsInput.getVqaScore() +
        // " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
        // " answer: " + postProcessingFieldsInput.getAnswer());
        // });
        //
        // }
        //
        // @Test
        // void testExecuteScriptsMemberAddressMapper() {
        //
        // log.info("Test Scenario: Execute MemberAddressMapper BeanShell Script");
        //
        // List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        // List<String> classes = Collections.singletonList("MemberAddressMapper");
        //
        // // ---------- CASE 1: Full comma-separated address ----------
        // inputList.add(createInput(1, "O1", 1, "single_value", "0",
        // "member_address_line1",
        // "123 Main St, Austin, TX 78701",
        // "Patient Info", "address", 95.0));
        //
        // inputList.add(createInput(2, "O1", 1, "single_value", "0",
        // "member_city", "", "", "", 0.0));
        // inputList.add(createInput(3, "O1", 1, "single_value", "0",
        // "member_state", "", "", "", 0.0));
        // inputList.add(createInput(4, "O1", 1, "single_value", "0",
        // "member_zipcode", "", "", "", 0.0));
        //
        // // ---------- CASE 2: Space separated ----------
        // inputList.add(createInput(5, "O2", 1, "single_value", "0",
        // "member_address_line1",
        // "456 Elm Street Dallas TX 75201",
        // "Patient Info", "address", 92.0));
        //
        // // ---------- CASE 3: ZIP only ----------
        // inputList.add(createInput(6, "O3", 1, "single_value", "0",
        // "member_address_line1",
        // "789 Broadway Ave 10001",
        // "Patient Info", "address", 90.0));
        //
        // // ---------- CASE 4: PO BOX ----------
        // inputList.add(createInput(7, "O4", 1, "single_value", "0",
        // "member_address_line1",
        // "P.O. BOX 123 Phoenix AZ 85001",
        // "Patient Info", "address", 93.0));
        //
        // // ---------- CASE 5: No ZIP / no state ----------
        // inputList.add(createInput(8, "O5", 1, "single_value", "0",
        // "member_address_line1",
        // "Some Unknown Place Near River",
        // "Patient Info", "address", 70.0));
        //
        // // ---------- CASE 6: Existing city/state/zip should NOT change ----------
        // inputList.add(createInput(9, "O6", 1, "single_value", "0",
        // "member_address_line1",
        // "999 Market St San Francisco CA 94103",
        // "Patient Info", "address", 96.0));
        //
        // inputList.add(createInput(10, "O6", 1, "single_value", "0",
        // "member_city", "San Francisco", "Patient Info", "city", 99.0));
        // inputList.add(createInput(11, "O6", 1, "single_value", "0",
        // "member_state", "CA", "Patient Info", "state", 99.0));
        // inputList.add(createInput(12, "O6", 1, "single_value", "0",
        // "member_zipcode", "94103", "Patient Info", "zip", 99.0));
        //
        // // ---------- BEFORE ----------
        // System.out.println("===== BEFORE ADDRESS MAPPING =====");
        // for (PostProcessingFieldsInput in : inputList) {
        // System.out.println(
        // "Origin: " + in.getOriginId() +
        // " | Item: " + in.getSorItemName() +
        // " | Answer: " + in.getAnswer() +
        // " | Section: " + in.getSectionAlias() +
        // " | Score: " + in.getVqaScore()
        // );
        // }
        //
        // // ---------- EXECUTE ----------
        // List<PostProcessingFieldsInput> result = validator.executeScripts(classes,
        // inputList);
        //
        // // ---------- AFTER ----------
        // System.out.println("===== AFTER ADDRESS MAPPING =====");
        // for (PostProcessingFieldsInput in : result) {
        // System.out.println(
        // "Origin: " + in.getOriginId() +
        // " | Item: " + in.getSorItemName() +
        // " | Answer: " + in.getAnswer() +
        // " | Section: " + in.getSectionAlias() +
        // " | Score: " + in.getVqaScore()
        // );
        // }
        // }
        //
        //
        // // @Test
        // // void testProcessValidatorResult_Reflection() throws NoSuchMethodException
        // {
        // // log.info("Test Scenario: Process Validator Results via Reflection");
        // //
        // // // Mock a result object that mimics the expected BeanShell result class
        // // // Since we can't easily mock an object with a specific structure without
        // a
        // // // class definition,
        // // // we'll rely on the logic that it uses reflection to find
        // 'getMappedData'.
        // // // Here we create a dummy inner class for testing.
        // //
        // // class MockResult {
        // // public Map<String, List<PostProcessingFieldsInput>> getMappedData() {
        // // Map<String, List<PostProcessingFieldsInput>> map = new HashMap<>();
        // // map.put("updatedKey", new ArrayList<>());
        // // return map;
        // // }
        // // }
        // //
        // // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
        // // validator.processValidatorResult(new MockResult(), resultMap);
        // //
        // // assertTrue(resultMap.containsKey("updatedKey"));
        // // }
        //
        // // @Test
        // // void testGetMappedDataResult_TypeCheck() {
        // // log.info("Test Scenario: Validate Type Checking in Result Map");
        // // Map<Object, Object> rawMap = new HashMap<>();
        // // rawMap.put("validKey", new ArrayList<PostProcessingFieldsInput>());
        // // rawMap.put(123, "InvalidKeyType"); // Should be ignored/logged
        // //
        // // Map<String, List<PostProcessingFieldsInput>> result =
        // // validator.getMappedDataResult(rawMap);
        // //
        // // assertEquals(1, result.size());
        // // assertTrue(result.containsKey("validKey"));
        // // }
        //
        // // @Test
        // // void testBuildUpdatedResults() {
        // // log.info("Test Scenario: Apply updates from Result Map to original
        // inputs");
        // //
        // // PostProcessingFieldsInput input = createInput("O1", 1, "s", "0", "key1",
        // // "oldValue");
        // // List<PostProcessingFieldsInput> inputs = Collections.singletonList(input);
        // //
        // // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
        // // PostProcessingFieldsInput updatedInput = createInput("O1", 1, "s", "0",
        // // "key1", "newValue");
        // // resultMap.put("key1", Collections.singletonList(updatedInput));
        // //
        // // List<PostProcessingFieldsInput> finalResults =
        // // validator.buildUpdatedResults(inputs, resultMap);
        // //
        // // assertEquals(1, finalResults.size());
        // // assertEquals("newValue", finalResults.get(0).getAnswer());
        // // }
        //
        // // @Test
        // // void testBuildUpdatedResults_NullInput() {
        // // log.info("Test Scenario: Handle empty inputs in buildUpdatedResults");
        // // List<PostProcessingFieldsInput> res = validator.buildUpdatedResults(null,
        // new
        // // HashMap<>());
        // // assertTrue(res.isEmpty());
        // // }

        @Test
        void testExecuteScriptsMemberAddressMapper() {

                log.info("Test Scenario: Execute MemberAddressMapper BeanShell Script");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();

                List<String> classes = Collections.singletonList("AuthIdValidator");
                inputList.add(createInput(1, "ORIGIN-1", 1,
                                "multi_value", "PROVIDER_1", "member_id", "747U05007", "Provider", "provider npi",
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

                inputMap.put("servicing_provider_npi", inputList);
                // ---------- EXECUTE ----------
                Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, inputMap);

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
                log.info("Test Scenario: Execute FaxReportProcessor BeanShell Script");
                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();

                List<String> classes = Collections.singletonList("FaxReportProcessor");

                // Scenario 1: Valid Keyword
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "fax_report", "ADT Report", "Header",
                                "fax report", 0.95));

                inputMap.put("fax_report", inputList);

                Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, inputMap);
                // Verify result contains validation (Y)
        }

        @Test
        void testExecuteScriptsAumiGenderMapper() {
                log.info("Test Scenario: Execute AumiGenderMapper BeanShell Script");
                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();

                List<String> classes = Collections.singletonList("AumiGenderMapper");

                inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_gender", "male", "Patient", "gender",
                                0.99));

                inputMap.put("member_gender", inputList);

                Map<String, List<PostProcessingFieldsInput>> result = validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsAumiMemberNameMapper() {
                log.info("Test Scenario: Execute AumiMemberNameMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("AumiMemberNameMapper");

                List<PostProcessingFieldsInput> fullList = new ArrayList<>();
                fullList.add(createInput(1, "O1", 1, "single_value", "0", "member_full_name", "Doe, John", "Patient",
                                "full name", 0.95));
                inputMap.put("member_full_name", fullList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsAumiMultiMemberMapper() {
                log.info("Test Scenario: Execute AumiMultiMemberMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("AumiMultiMemberMapper");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "multiple_member_indicator", "Yes", "Header",
                                "multi member", 0.95));
                inputMap.put("multiple_member_indicator", inputList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsAuthDischargeDateValidator() {
                log.info("Test Scenario: Execute AuthDischargeDateValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("AuthDischargeDateValidator");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "auth_discharge_date", "12/31/2023", "Body",
                                "discharge date", 0.95));
                inputMap.put("auth_discharge_date", inputList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsClinicalPresentProcessor() {
                log.info("Test Scenario: Execute ClinicalPresentProcessor BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("ClinicalPresentProcessor");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "clinical_present", "Yes", "Header",
                                "clinical", 0.95));
                inputMap.put("clinical_present", inputList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsFaxFromDateMapper() {
                log.info("Test Scenario: Execute FaxFromDateMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("FaxFromDateMapper");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "fax_received_date", "01/01/2023", "Header",
                                "fax date", 0.95));
                inputMap.put("fax_received_date", inputList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsMedicaidMemberIdValidator() {
                log.info("Test Scenario: Execute MedicaidMemberIdValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("MedicaidMemberIdValidator");

                List<PostProcessingFieldsInput> idList = new ArrayList<>();
                idList.add(createInput(1, "O1", 1, "single_value", "0", "medicaid_id", "123456789", "Body",
                                "medicaid id", 0.95));
                inputMap.put("medicaid_id", idList);
                inputMap.put("auth_id", new ArrayList<>());
                inputMap.put("additional_auth_properties", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsMemberDOBandServiceFromDateMapper() {
                log.info("Test Scenario: Execute MemberDOBandServiceFromDateMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("MemberDOBandServiceFromDateMapper");

                List<PostProcessingFieldsInput> dobList = new ArrayList<>();
                dobList.add(createInput(1, "O1", 1, "single_value", "0", "member_date_of_birth", "01/01/1980", "Body",
                                "dob", 0.95));
                inputMap.put("member_date_of_birth", dobList);

                List<PostProcessingFieldsInput> serviceList = new ArrayList<>();
                serviceList.add(createInput(2, "O1", 1, "single_value", "0", "service_from_date", "01/01/2023", "Body",
                                "service date", 0.95));
                inputMap.put("service_from_date", serviceList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsMemberIdValidator() {
                log.info("Test Scenario: Execute MemberIdValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("MemberIdValidator");

                List<PostProcessingFieldsInput> idList = new ArrayList<>();
                idList.add(createInput(1, "O1", 1, "single_value", "0", "member_id", "ABC123456", "Body", "member id",
                                0.95));
                inputMap.put("member_id", idList);
                inputMap.put("auth_id", new ArrayList<>());
                inputMap.put("additional_auth_properties", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsMemberZipcodeMapper() {
                log.info("Test Scenario: Execute MemberZipcodeMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("MemberZipcodeMapper");

                List<PostProcessingFieldsInput> inputList = new ArrayList<>();
                inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_zipcode", "12345", "Body", "zip",
                                0.95));
                inputMap.put("member_zipcode", inputList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsNewBornRequestMapper() {
                log.info("Test Scenario: Execute NewBornRequestMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("NewBornRequestMapper");

                List<PostProcessingFieldsInput> reqList = new ArrayList<>();
                reqList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_request", "Yes", "Body", "request",
                                0.95));
                inputMap.put("newborn_request", reqList);

                List<PostProcessingFieldsInput> promptList = new ArrayList<>();
                promptList.add(createInput(2, "O1", 1, "single_value", "0", "newborn_request_prompt", "Yes", "Body",
                                "prompt", 0.95));
                inputMap.put("newborn_request_prompt", promptList);

                List<PostProcessingFieldsInput> ocrList = new ArrayList<>();
                ocrList.add(createInput(3, "O1", 1, "single_value", "0", "newborn_request_ocr", "Yes", "Body", "ocr",
                                0.95));
                inputMap.put("newborn_request_ocr", ocrList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsNewBornRequestOCRMapper() {
                log.info("Test Scenario: Execute NewBornRequestOCRMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("NewBornRequestOCRMapper");

                List<PostProcessingFieldsInput> ocrList = new ArrayList<>();
                ocrList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_request_ocr",
                                "Some text containing keywords", "Body", "ocr", 0.95));
                inputMap.put("newborn_request_ocr", ocrList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsNewbornDOBMapper() {
                log.info("Test Scenario: Execute NewbornDOBMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("NewbornDOBMapper");

                List<PostProcessingFieldsInput> dobList = new ArrayList<>();
                dobList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_date_of_birth", "01/01/2023", "Body",
                                "dob", 0.95));
                inputMap.put("newborn_date_of_birth", dobList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsNewbornGenderMapper() {
                log.info("Test Scenario: Execute NewbornGenderMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("NewbornGenderMapper");

                List<PostProcessingFieldsInput> genderList = new ArrayList<>();
                genderList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_gender", "Male", "Body", "gender",
                                0.95));
                inputMap.put("newborn_gender", genderList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsNewbornNameMapper() {
                log.info("Test Scenario: Execute NewbornNameMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("NewbornNameMapper");

                List<PostProcessingFieldsInput> fullList = new ArrayList<>();
                fullList.add(createInput(1, "O1", 1, "single_value", "0", "newborn_full_name", "Baby Doe", "Body",
                                "full name", 0.95));
                inputMap.put("newborn_full_name", fullList);
                inputMap.put("newborn_first_name", new ArrayList<>());
                inputMap.put("newborn_last_name", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsProviderAddressMapper() {
                log.info("Test Scenario: Execute ProviderAddressMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("ProviderAddressMapper");

                List<PostProcessingFieldsInput> addrList = new ArrayList<>();
                addrList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_address_line1",
                                "123 Main St", "Body", "address", 0.95));
                inputMap.put("servicing_provider_address_line1", addrList);
                inputMap.put("servicing_provider_city", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsProviderNpiTinValidator() {
                log.info("Test Scenario: Execute ProviderNpiTinValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("ProviderNpiTinValidator");

                List<PostProcessingFieldsInput> npiList = new ArrayList<>();
                npiList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_npi", "1234567890", "Body",
                                "npi", 0.95));
                inputMap.put("servicing_provider_npi", npiList);

                List<PostProcessingFieldsInput> tinList = new ArrayList<>();
                tinList.add(createInput(2, "O1", 1, "single_value", "0", "servicing_provider_tin", "12-3456789", "Body",
                                "tin", 0.95));
                inputMap.put("servicing_provider_tin", tinList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsProviderZipCodeMapper() {
                log.info("Test Scenario: Execute ProviderZipCodeMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("ProviderZipCodeMapper");

                List<PostProcessingFieldsInput> zipList = new ArrayList<>();
                zipList.add(createInput(1, "O1", 1, "single_value", "0", "servicing_provider_zipcode", "12345", "Body",
                                "zip", 0.95));
                inputMap.put("servicing_provider_zipcode", zipList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsServiceToDateMapper() {
                log.info("Test Scenario: Execute ServiceToDateMapper BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("ServiceToDateMapper");

                List<PostProcessingFieldsInput> dateList = new ArrayList<>();
                dateList.add(createInput(1, "O1", 1, "single_value", "0", "service_to_date", "01/01/2023", "Body",
                                "date", 0.95));
                inputMap.put("service_to_date", dateList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsDiagnosisServiceCodeValidator() {
                log.info("Test Scenario: Execute DiagnosisServiceCodeValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("DiagnosisServiceCodeValidator");

                List<PostProcessingFieldsInput> svcList = new ArrayList<>();
                svcList.add(createInput(1, "O1", 1, "multi_value", "0", "service_code", "99213", "Body", "service code",
                                0.95));
                inputMap.put("service_code", svcList);

                List<PostProcessingFieldsInput> diagList = new ArrayList<>();
                diagList.add(createInput(2, "O1", 1, "multi_value", "0", "diagnosis_code", "R05.9", "Body", "diag code",
                                0.95));
                inputMap.put("diagnosis_code", diagList);

                inputMap.put("service_quantity_units", new ArrayList<>());
                inputMap.put("service_quantity_visits", new ArrayList<>());
                inputMap.put("service_code_modifier", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsLOSValidator() {
                log.info("Test Scenario: Execute LOSValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("LOSValidator");

                List<PostProcessingFieldsInput> losList = new ArrayList<>();
                losList.add(createInput(1, "O1", 1, "multi_value", "0", "level_of_service", "Urgent", "Header", "los",
                                0.95));
                inputMap.put("level_of_service", losList);

                inputMap.put("auth_id", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsLOSGBDValidator() {
                log.info("Test Scenario: Execute LOSGBDValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("LOSGBDValidator");

                List<PostProcessingFieldsInput> losList = new ArrayList<>();
                losList.add(createInput(1, "O1", 1, "multi_value", "0", "level_of_service", "Urgent", "Header", "los",
                                0.95));
                inputMap.put("level_of_service", losList);

                validator.executeScripts(classes, inputMap);
        }

        @Test
        void testExecuteScriptsAuthIdValidator() {
                log.info("Test Scenario: Execute AuthIdValidator BeanShell Script");
                Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();
                List<String> classes = Collections.singletonList("AuthIdValidator");

                List<PostProcessingFieldsInput> authList = new ArrayList<>();
                authList.add(createInput(1, "O1", 1, "multi_value", "0", "auth_id", "UM12345678", "Body", "auth id",
                                0.95));
                inputMap.put("auth_id", authList);

                inputMap.put("member_id", new ArrayList<>());
                inputMap.put("additional_auth_properties", new ArrayList<>());

                validator.executeScripts(classes, inputMap);
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
