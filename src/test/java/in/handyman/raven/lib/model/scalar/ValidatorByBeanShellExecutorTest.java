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
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE
//        actionExecutionAudit.getContext().put("ProviderZipCodeMapper", getProviderZipCodeMapper());
//        actionExecutionAudit.getContext().put("ProviderNpiTinValidator", getProviderNpiTinValidator());
//        actionExecutionAudit.getContext().put("ProviderAddressMapper", getProviderAddressMapper());
//        actionExecutionAudit.getContext().put("ServiceToDateMapper", getServiceToDateMapper());
//        actionExecutionAudit.getContext().put("NewBornRequestOCRMapper", getNewBornRequestOCRMapper());
//        actionExecutionAudit.getContext().put("NewBornRequestMapper", getNewBornRequestMapper());
//        actionExecutionAudit.getContext().put("NewbornNameMapper", getNewbornNameMapper());
//        actionExecutionAudit.getContext().put("NewbornGenderMapper", getNewbornGenderMapper());
//        actionExecutionAudit.getContext().put("NewbornDOBMapper", getNewbornDOBMapper());
//        actionExecutionAudit.getContext().put("MemberZipcodeMapper", getMemberZipcodeMapper());
//        actionExecutionAudit.getContext().put("MemberIdValidator", getMemberIdValidator());
//        actionExecutionAudit.getContext().put("MemberDOBandServiceFromDateMapper", getMemberDOBandServiceFromDateMapper());
//        actionExecutionAudit.getContext().put("MemberAddressMapper", getMemberAddressMapper());
//        actionExecutionAudit.getContext().put("MedicaidMemberIdValidator", getMedicaidMemberIdValidator());
//        actionExecutionAudit.getContext().put("FaxFromDateMapper", getFaxFromDateMapper());
//        actionExecutionAudit.getContext().put("ClinicalPresentProcessor", getClinicalPresentProcessor());
//        actionExecutionAudit.getContext().put("AuthDischargeDateValidator", getAuthDischargeDateValidator());
//        actionExecutionAudit.getContext().put("AumiMultiMemberMapper", getAumiMultiMemberMapper());
//        actionExecutionAudit.getContext().put("AumiMemberNameMapper", getAumiMemberNameMapper());
//        actionExecutionAudit.getContext().put("AumiGenderMapper", getAumiGenderMapper());
//        actionExecutionAudit.getContext().put("LOSValidator", getLOSValidator());
//          actionExecutionAudit.getContext().put("LOSGBDValidator", getLOSGBDValidator());
//        actionExecutionAudit.getContext().put("DiagnosisServiceCodeValidator", getDiagnosisServiceCodeValidator());
        actionExecutionAudit.getContext().put("AuthIdValidator", getAuthIdValidator());



        // Default Context
        contextMap.put("multi.line.item.activator", "true");

        // Initialize Validator with mocked logger
        validator = new ValidatorByBeanShellExecutor(inputList, actionExecutionAudit, log, 2);
    }

    private PostProcessingFieldsInput createInput(Integer id, String originId, Integer paperNo, String lineItemType,
                                                  String sorContainerInstance, String sorItemName, String answer, String sectionAlias, String label,
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

//    @Test
//    void testExecuteScriptsMemberNameMapper() {
//        log.info("Test Scenario: Execute BeanShell Scripts");
//        // NOTE: This test might fail if BeanShell is not on classpath or stricter
//        // security settings
//        // But for generation purposes, this is the correct logic.
//
//        Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
//        List<String> classes = Collections.singletonList("AumiMemberNameMapper,AumiGenderMapper");
//
//        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_full_name", "Exlsie, Noichole",
//                "Patient Info", "full name", 100.0));
//        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_first_name", "", "", "", 0.0));
//        inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_last_name", "", "", "", 0.0));
//
//        inputList.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);
//
//        result.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//    }
//
//    @Test
//    void testExecuteScriptsMemberGenderMapper() {
//        log.info("Test Scenario: Execute BeanShell Scripts");
//        // NOTE: This test might fail if BeanShell is not on classpath or stricter
//        // security settings
//        // But for generation purposes, this is the correct logic.
//
//        List<String> classes = Collections.singletonList("AumiGenderMapper");
//
//        // Scenario 1: Abbreviated Male (m -> M)
//        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_gender", "m", "Patient Info",
//                "member gender", 100.0));
//
//        // Scenario 2: Full word Male (male -> M)
//        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_gender", "male", "Patient Info",
//                "member gender", 100.0));
//
//        // Scenario 3: Abbreviated Female (f -> F)
//        inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_gender", "f", "Patient Info",
//                "member gender", 100.0));
//
//        // Scenario 4: Full word Female (female -> F)
//        inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_gender", "female", "Patient Info",
//                "member gender", 100.0));
//
//        // Scenario 5: Mixed Case and Whitespace (mAlE -> M)
//        inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_gender", "  mAlE  ", "Patient Info",
//                "member gender", 100.0));
//
//        // Scenario 6: Unknown value (unknown -> "")
//        inputList.add(createInput(6, "O1", 1, "single_value", "0", "member_gender", "Not Specified", "Patient Info",
//                "member gender", 100.0));
//
//        inputList.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
//                    + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);
//
//        result.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
//                    + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//    }
//
//    @Test
//    void testExecuteScriptsMemberIdAuthIdMapper() {
//        log.info("Test Scenario: Execute BeanShell Scripts");
//        // NOTE: This test might fail if BeanShell is not on classpath or stricter
//        // security settings
//        // But for generation purposes, this is the correct logic.
//
//        Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
//        List<String> classes = Collections.singletonList("MemberIdValidator");
//
//        // Scenario 1: Clean Member ID (Should remain as member_id)
//        // inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_id",
//        // "ABC123456", "Patient Info", "member id", 100.0));
//
//        // Scenario 2: Only a UM ID found in Member ID field (Should be cleared and
//        // duplicated as auth_id)
//        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_id", "UM88887777", "Patient Info",
//                "member id", 91.0));
//
//        // Scenario 3: Combined Member ID and UM ID with "/" (Should split: one stays
//        // member_id, one duplicated as auth_id)
//        // inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_id",
//        // "99999/UM11112222", "Patient Info", "member id", 100.0));
//
//        // Scenario 4: Combined Member ID and UM ID with ":" and noise (Should split and
//        // clean special characters)
//        // inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_id",
//        // "ID#55555:UM44443333", "Patient Info", "member id", 100.0));
//
//        // Scenario 5: Invalid UM pattern (Should be detected by UM_INVALID_PATTERN and
//        // duplicated as auth_id)
//        // inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_id",
//        // "UM123", "Patient Info", "member id", 100.0));
//
//        inputList.add(
//                createInput(6, "O1", 1, "single_value", "0", "auth_id", "UM99992222", "Auth Info", "auth id", 90.0));
//
//        inputList.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
//                    + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);
//
//        System.out.println("---- After BeanShell Execution ----");
//        result.forEach((postProcessingFieldsInput) -> {
//
//            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
//                    + postProcessingFieldsInput.getOriginId() + " instance: "
//                    + postProcessingFieldsInput.getSorContainerInstance() +
//                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
//                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
//                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
//                    " answer: " + postProcessingFieldsInput.getAnswer());
//        });
//
//    }
//
//    @Test
//    void testExecuteScriptsMemberAddressMapper() {
//
//        log.info("Test Scenario: Execute MemberAddressMapper BeanShell Script");
//
//        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
//        List<String> classes = Collections.singletonList("MemberAddressMapper");
//
//        // ---------- CASE 1: Full comma-separated address ----------
//        inputList.add(createInput(1, "O1", 1, "single_value", "0",
//                "member_address_line1",
//                "123 Main St, Austin, TX 78701",
//                "Patient Info", "address", 95.0));
//
//        inputList.add(createInput(2, "O1", 1, "single_value", "0",
//                "member_city", "", "", "", 0.0));
//        inputList.add(createInput(3, "O1", 1, "single_value", "0",
//                "member_state", "", "", "", 0.0));
//        inputList.add(createInput(4, "O1", 1, "single_value", "0",
//                "member_zipcode", "", "", "", 0.0));
//
//        // ---------- CASE 2: Space separated ----------
//        inputList.add(createInput(5, "O2", 1, "single_value", "0",
//                "member_address_line1",
//                "456 Elm Street Dallas TX 75201",
//                "Patient Info", "address", 92.0));
//
//        // ---------- CASE 3: ZIP only ----------
//        inputList.add(createInput(6, "O3", 1, "single_value", "0",
//                "member_address_line1",
//                "789 Broadway Ave 10001",
//                "Patient Info", "address", 90.0));
//
//        // ---------- CASE 4: PO BOX ----------
//        inputList.add(createInput(7, "O4", 1, "single_value", "0",
//                "member_address_line1",
//                "P.O. BOX 123 Phoenix AZ 85001",
//                "Patient Info", "address", 93.0));
//
//        // ---------- CASE 5: No ZIP / no state ----------
//        inputList.add(createInput(8, "O5", 1, "single_value", "0",
//                "member_address_line1",
//                "Some Unknown Place Near River",
//                "Patient Info", "address", 70.0));
//
//        // ---------- CASE 6: Existing city/state/zip should NOT change ----------
//        inputList.add(createInput(9, "O6", 1, "single_value", "0",
//                "member_address_line1",
//                "999 Market St San Francisco CA 94103",
//                "Patient Info", "address", 96.0));
//
//        inputList.add(createInput(10, "O6", 1, "single_value", "0",
//                "member_city", "San Francisco", "Patient Info", "city", 99.0));
//        inputList.add(createInput(11, "O6", 1, "single_value", "0",
//                "member_state", "CA", "Patient Info", "state", 99.0));
//        inputList.add(createInput(12, "O6", 1, "single_value", "0",
//                "member_zipcode", "94103", "Patient Info", "zip", 99.0));
//
//        // ---------- BEFORE ----------
//        System.out.println("===== BEFORE ADDRESS MAPPING =====");
//        for (PostProcessingFieldsInput in : inputList) {
//            System.out.println(
//                    "Origin: " + in.getOriginId() +
//                            " | Item: " + in.getSorItemName() +
//                            " | Answer: " + in.getAnswer() +
//                            " | Section: " + in.getSectionAlias() +
//                            " | Score: " + in.getVqaScore()
//            );
//        }
//
//        // ---------- EXECUTE ----------
//        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);
//
//        // ---------- AFTER ----------
//        System.out.println("===== AFTER ADDRESS MAPPING =====");
//        for (PostProcessingFieldsInput in : result) {
//            System.out.println(
//                    "Origin: " + in.getOriginId() +
//                            " | Item: " + in.getSorItemName() +
//                            " | Answer: " + in.getAnswer() +
//                            " | Section: " + in.getSectionAlias() +
//                            " | Score: " + in.getVqaScore()
//            );
//        }
//    }
//
//
//    // @Test
//    // void testProcessValidatorResult_Reflection() throws NoSuchMethodException {
//    // log.info("Test Scenario: Process Validator Results via Reflection");
//    //
//    // // Mock a result object that mimics the expected BeanShell result class
//    // // Since we can't easily mock an object with a specific structure without a
//    // // class definition,
//    // // we'll rely on the logic that it uses reflection to find 'getMappedData'.
//    // // Here we create a dummy inner class for testing.
//    //
//    // class MockResult {
//    // public Map<String, List<PostProcessingFieldsInput>> getMappedData() {
//    // Map<String, List<PostProcessingFieldsInput>> map = new HashMap<>();
//    // map.put("updatedKey", new ArrayList<>());
//    // return map;
//    // }
//    // }
//    //
//    // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
//    // validator.processValidatorResult(new MockResult(), resultMap);
//    //
//    // assertTrue(resultMap.containsKey("updatedKey"));
//    // }
//
//    // @Test
//    // void testGetMappedDataResult_TypeCheck() {
//    // log.info("Test Scenario: Validate Type Checking in Result Map");
//    // Map<Object, Object> rawMap = new HashMap<>();
//    // rawMap.put("validKey", new ArrayList<PostProcessingFieldsInput>());
//    // rawMap.put(123, "InvalidKeyType"); // Should be ignored/logged
//    //
//    // Map<String, List<PostProcessingFieldsInput>> result =
//    // validator.getMappedDataResult(rawMap);
//    //
//    // assertEquals(1, result.size());
//    // assertTrue(result.containsKey("validKey"));
//    // }
//
//    // @Test
//    // void testBuildUpdatedResults() {
//    // log.info("Test Scenario: Apply updates from Result Map to original inputs");
//    //
//    // PostProcessingFieldsInput input = createInput("O1", 1, "s", "0", "key1",
//    // "oldValue");
//    // List<PostProcessingFieldsInput> inputs = Collections.singletonList(input);
//    //
//    // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
//    // PostProcessingFieldsInput updatedInput = createInput("O1", 1, "s", "0",
//    // "key1", "newValue");
//    // resultMap.put("key1", Collections.singletonList(updatedInput));
//    //
//    // List<PostProcessingFieldsInput> finalResults =
//    // validator.buildUpdatedResults(inputs, resultMap);
//    //
//    // assertEquals(1, finalResults.size());
//    // assertEquals("newValue", finalResults.get(0).getAnswer());
//    // }
//
//    // @Test
//    // void testBuildUpdatedResults_NullInput() {
//    // log.info("Test Scenario: Handle empty inputs in buildUpdatedResults");
//    // List<PostProcessingFieldsInput> res = validator.buildUpdatedResults(null, new
//    // HashMap<>());
//    // assertTrue(res.isEmpty());
//    // }
//
//    String getAumiMemberNameMapper() {
//        return "\n" +
//                "import org.slf4j.Logger;\n" +
//                "import java.util.HashMap;\n" +
//                "import java.util.List;\n" +
//                "import java.util.Map;\n" +
//                "\n" +
//                "public class AumiMemberNameMapper {\n" +
//                "\n" +
//                "    private Logger logger;\n" +
//                "\n" +
//                "    public AumiMemberNameMapper(Logger logger) {\n" +
//                "        this.logger = logger;\n" +
//                "    }\n" +
//                "\n" +
//                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
//                "\n" +
//                "        if (predictionKeyMap == null) {\n" +
//                "            return new MappingResult(new HashMap());\n" +
//                "        }\n" +
//                "\n" +
//                "        PostProcessingFieldsInput fullNameObj =\n" +
//                "                getFirst(predictionKeyMap, \"member_full_name\");\n" +
//                "        PostProcessingFieldsInput firstNameObj =\n" +
//                "                getFirst(predictionKeyMap, \"member_first_name\");\n" +
//                "        PostProcessingFieldsInput lastNameObj =\n" +
//                "                getFirst(predictionKeyMap, \"member_last_name\");\n" +
//                "\n" +
//                "        if (hasValue(fullNameObj)) {\n" +
//                "\n" +
//                "            NameParts parts = split(fullNameObj.getExtractedValue());\n" +
//                "\n" +
//                "            applyAll(\n" +
//                "                    predictionKeyMap,\n" +
//                "                    fullNameObj,\n" +
//                "                    parts.firstName,\n" +
//                "                    parts.lastName,\n" +
//                "                    fullNameObj.getExtractedValue()\n" +
//                "            );\n" +
//                "            return new MappingResult(predictionKeyMap);\n" +
//                "        }\n" +
//                "\n" +
//                "        if (hasValue(firstNameObj) && hasValue(lastNameObj)) {\n" +
//                "            applySimple(predictionKeyMap, firstNameObj, lastNameObj);\n" +
//                "            return new MappingResult(predictionKeyMap);\n" +
//                "        }\n" +
//                "\n" +
//                "        if (hasValue(firstNameObj)) {\n" +
//                "            NameParts parts = split(firstNameObj.getExtractedValue());\n" +
//                "            applyAll(predictionKeyMap, firstNameObj, parts.firstName, parts.lastName, \"\");\n" +
//                "        } else if (hasValue(lastNameObj)) {\n" +
//                "            NameParts parts = split(lastNameObj.getExtractedValue());\n" +
//                "            applyAll(predictionKeyMap, lastNameObj, parts.firstName, parts.lastName, \"\");\n" +
//                "        }\n" +
//                "\n" +
//                "        return new MappingResult(predictionKeyMap);\n" +
//                "    }\n" +
//                "\n" +
//                "    private void applyAll(Map map,\n" +
//                "                          PostProcessingFieldsInput source,\n" +
//                "                          String firstName,\n" +
//                "                          String lastName,\n" +
//                "                          String fullName) {\n" +
//                "\n" +
//                "        updateList(map, \"member_first_name\", firstName, source);\n" +
//                "        updateList(map, \"member_last_name\", lastName, source);\n" +
//                "        updateList(map, \"member_full_name\", fullName, source);\n" +
//                "    }\n" +
//                "\n" +
//                "    private void applySimple(Map map,\n" +
//                "                             PostProcessingFieldsInput first,\n" +
//                "                             PostProcessingFieldsInput last) {\n" +
//                "\n" +
//                "        updateList(map, \"member_first_name\", first.getExtractedValue(), first);\n" +
//                "        updateList(map, \"member_last_name\", last.getExtractedValue(), last);\n" +
//                "        updateList(map, \"member_full_name\", \"\", null);\n" +
//                "    }\n" +
//                "\n" +
//                "    private void updateList(Map map,\n" +
//                "                            String key,\n" +
//                "                            String value,\n" +
//                "                            PostProcessingFieldsInput source) {\n" +
//                "\n" +
//                "        Object obj = map.get(key);\n" +
//                "        if (!(obj instanceof List)) return;\n" +
//                "\n" +
//                "        List list = (List) obj;\n" +
//                "\n" +
//                "        for (int i = 0; i < list.size(); i++) {\n" +
//                "            PostProcessingFieldsInput in =\n" +
//                "                    (PostProcessingFieldsInput) list.get(i);\n" +
//                "\n" +
//                "            in.setExtractedValue(value);\n" +
//                "\n" +
//                "            if (isEmpty(value)) {\n" +
//                "                clearDependentFields(in);\n" +
//                "            } else if (source != null) {\n" +
//                "                copyDependentFields(source, in);\n" +
//                "            }\n" +
//                "        }\n" +
//                "        map.put(key, list);\n" +
//                "    }\n" +
//                "\n" +
//                "    private void clearDependentFields(PostProcessingFieldsInput in) {\n" +
//                "        in.setLabel(\"\");\n" +
//                "        in.setSectionAlias(\"\");\n" +
//                "        in.setBBox(\"\");\n" +
//                "    }\n" +
//                "\n" +
//                "    private void copyDependentFields(PostProcessingFieldsInput from,\n" +
//                "                                     PostProcessingFieldsInput to) {\n" +
//                "\n" +
//                "        to.setLabel(from.getLabel());\n" +
//                "        to.setSectionAlias(from.getSectionAlias());\n" +
//                "        to.setBBox(from.getBBox());\n" +
//                "    }\n" +
//                "\n" +
//                "    private NameParts split(String value) {\n" +
//                "\n" +
//                "        if (value == null) return new NameParts(\"\", \"\");\n" +
//                "\n" +
//                "        value = value.trim();\n" +
//                "\n" +
//                "        if (value.indexOf(\",\") >= 0) {\n" +
//                "            String[] parts = value.split(\",\", 2);\n" +
//                "            return new NameParts(clean(parts[1]), clean(parts[0]));\n" +
//                "        }\n" +
//                "\n" +
//                "        String[] tokens = value.split(\"\\\\s+\");\n" +
//                "        if (tokens.length == 1) {\n" +
//                "            return new NameParts(tokens[0], \"\");\n" +
//                "        }\n" +
//                "\n" +
//                "        StringBuffer first = new StringBuffer();\n" +
//                "        for (int i = 0; i < tokens.length - 1; i++) {\n" +
//                "            first.append(tokens[i]).append(\" \");\n" +
//                "        }\n" +
//                "\n" +
//                "        return new NameParts(first.toString().trim(), tokens[tokens.length - 1]);\n" +
//                "    }\n" +
//                "\n" +
//                "    private PostProcessingFieldsInput getFirst(Map map, String key) {\n" +
//                "        Object obj = map.get(key);\n" +
//                "        if (obj instanceof List) {\n" +
//                "            List list = (List) obj;\n" +
//                "            if (!list.isEmpty()) {\n" +
//                "                return (PostProcessingFieldsInput) list.get(0);\n" +
//                "            }\n" +
//                "        }\n" +
//                "        return null;\n" +
//                "    }\n" +
//                "\n" +
//                "    private boolean hasValue(PostProcessingFieldsInput in) {\n" +
//                "        return in != null && !isEmpty(in.getExtractedValue());\n" +
//                "    }\n" +
//                "\n" +
//                "    private boolean isEmpty(String s) {\n" +
//                "        return s == null || s.trim().length() == 0;\n" +
//                "    }\n" +
//                "\n" +
//                "    private String clean(String s) {\n" +
//                "        return s == null ? \"\" :\n" +
//                "                s.replaceAll(\"[0-9]\", \"\")\n" +
//                "                        .replaceAll(\"\\\\s+\", \" \")\n" +
//                "                        .trim();\n" +
//                "    }\n" +
//                "\n" +
//                "    private static class NameParts {\n" +
//                "        String firstName;\n" +
//                "        String lastName;\n" +
//                "\n" +
//                "        NameParts(String f, String l) {\n" +
//                "            this.firstName = f;\n" +
//                "            this.lastName = l;\n" +
//                "        }\n" +
//                "    }\n" +
//                "\n" +
//                "    public static class MappingResult {\n" +
//                "        private Map mappedData;\n" +
//                "        public MappingResult(Map mappedData) {\n" +
//                "            this.mappedData = mappedData;\n" +
//                "        }\n" +
//                "        public Map getMappedData() {\n" +
//                "            return mappedData;\n" +
//                "        }\n" +
//                "    }\n" +
//                "\n" +
//                "    public static class PostProcessingFieldsInput {\n" +
//                "\n" +
//                "        private Long tenantId;\n" +
//                "        private double aggregatedScore;\n" +
//                "        private double maskedScore;\n" +
//                "        private String originId;\n" +
//                "        private Integer paperNo;\n" +
//                "        private String extractedValue;\n" +
//                "        private double vqaScore;\n" +
//                "        private Integer rank;\n" +
//                "        private Integer sorItemAttributionId;\n" +
//                "        private String sorItemName;\n" +
//                "        private String documentId;\n" +
//                "        private Long accTransactionId;\n" +
//                "        private String label;\n" +
//                "        private String sectionAlias;\n" +
//                "        private Long score;\n" +
//                "        private String bBox;\n" +
//                "        private Long rootPipelineId;\n" +
//                "        private Long frequency;\n" +
//                "        private Long questionId;\n" +
//                "        private Long synonymId;\n" +
//                "        private String modelRegistry;\n" +
//                "        private String encryptionPolicy;\n" +
//                "        private String isEncrypted;\n" +
//                "        private String lineItemType;\n" +
//                "\n" +
//                "        public String getExtractedValue() { return extractedValue; }\n" +
//                "        public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }\n"
//                +
//                "\n" +
//                "        public String getLabel() { return label; }\n" +
//                "        public void setLabel(String label) { this.label = label; }\n" +
//                "\n" +
//                "        public String getSectionAlias() { return sectionAlias; }\n" +
//                "        public void setSectionAlias(String sectionAlias) { this.sectionAlias = sectionAlias; }\n" +
//                "\n" +
//                "        public String getBBox() { return bBox; }\n" +
//                "        public void setBBox(String bBox) { this.bBox = bBox; }\n" +
//                "    }\n" +
//                "}\n";
//    }
//
//    String getMemberAddressMapper(){
//        return "import java.util.*;\n" +
//                "import java.util.regex.Matcher;\n" +
//                "import java.util.regex.Pattern;\n" +
//                "import org.slf4j.Logger;\n" +
//                "\n" +
//                "public class MemberAddressMapper {\n" +
//                "\n" +
//                "    private Logger logger;\n" +
//                "\n" +
//                "    // ===================== PATTERNS =====================\n" +
//                "\n" +
//                "    private static final String ENDING_ADDRESS_PATTERN_STRING =\n" +
//                "        \"(?:,\\\\s*|\\\\s*|/\\\\s*|\\\\.\\\\s*)?([\\\\w\\\\s\\\\(\\\\)''.\\\\-]+?)(?:,\\\\s*|/\\\\s*|\\\\.\\\\s*|\\\\s+)([A-Za-z]{2})(?:,\\\\s*|/\\\\s*|\\\\.\\\\s*|\\\\s+)(\\\\d{5}(?:-\\\\d{4})?)\\\\s*$\";\n" +
//                "    private static final Pattern ENDING_ADDRESS_PATTERN =\n" +
//                "        Pattern.compile(ENDING_ADDRESS_PATTERN_STRING);\n" +
//                "\n" +
//                "    private static final Pattern ZIP_CODE_PATTERN =\n" +
//                "        Pattern.compile(\"\\\\b\\\\d{5}(?:-\\\\d{4})?\\\\b$\");\n" +
//                "\n" +
//                "    private static final Set VALID_US_STATES = new HashSet(Arrays.asList(new String[]{\n" +
//                "        \"AL\",\"AK\",\"AZ\",\"AR\",\"CA\",\"CO\",\"CT\",\"DE\",\"FL\",\"GA\",\"HI\",\"ID\",\"IL\",\"IN\",\"IA\",\"KS\",\"KY\",\n" +
//                "        \"LA\",\"ME\",\"MD\",\"MA\",\"MI\",\"MN\",\"MS\",\"MO\",\"MT\",\"NE\",\"NV\",\"NH\",\"NJ\",\"NM\",\"NY\",\"NC\",\"ND\",\n" +
//                "        \"OH\",\"OK\",\"OR\",\"PA\",\"RI\",\"SC\",\"SD\",\"TN\",\"TX\",\"UT\",\"VT\",\"VA\",\"WA\",\"WV\",\"WI\",\"WY\",\n" +
//                "        \"DC\",\"GU\",\"PR\",\"VI\",\"AS\",\"MP\"\n" +
//                "    }));\n" +
//                "\n" +
//                "    // ===================== CTOR =====================\n" +
//                "\n" +
//                "    public MemberAddressMapper(Logger logger) {\n" +
//                "        this.logger = logger;\n" +
//                "    }\n" +
//                "\n" +
//                "    // ===================== ENTRY =====================\n" +
//                "\n" +
//                "    public List doCustomPredictionMapping(List predictionList, Long rootPipelineId) {\n" +
//                "\n" +
//                "        String prefix = \"[RootPipelineID: \" + rootPipelineId + \"] \";\n" +
//                "        logger.info(prefix + \"Entered MemberAddressMapper\");\n" +
//                "\n" +
//                "        if (predictionList == null || predictionList.isEmpty()) {\n" +
//                "            return predictionList;\n" +
//                "        }\n" +
//                "\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput addressObj =\n" +
//                "            findFirst(predictionList, \"member_address_line1\");\n" +
//                "\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput cityObj =\n" +
//                "            findFirst(predictionList, \"member_city\");\n" +
//                "\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput stateObj =\n" +
//                "            findFirst(predictionList, \"member_state\");\n" +
//                "\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput zipObj =\n" +
//                "            findFirst(predictionList, \"member_zipcode\");\n" +
//                "\n" +
//                "        String address =\n" +
//                "            hasValue(addressObj) ? addressObj.getAnswer().trim() : \"\";\n" +
//                "\n" +
//                "        String city = hasValue(cityObj) ? cityObj.getAnswer().trim() : \"\";\n" +
//                "        String state = hasValue(stateObj) ? stateObj.getAnswer().trim() : \"\";\n" +
//                "        String zip = hasValue(zipObj) ? zipObj.getAnswer().trim() : \"\";\n" +
//                "\n" +
//                "        boolean cityEmpty = city.isEmpty();\n" +
//                "        boolean stateEmpty = state.isEmpty();\n" +
//                "        boolean zipEmpty = zip.isEmpty();\n" +
//                "\n" +
//                "        if (!address.isEmpty()) {\n" +
//                "\n" +
//                "            AddressComponents parsed = parseAddress(address);\n" +
//                "\n" +
//                "            updateValue(predictionList, \"member_address_line1\",\n" +
//                "                parsed.getAddressLine1(), addressObj);\n" +
//                "\n" +
//                "            if (cityEmpty) {\n" +
//                "                updateValue(predictionList, \"member_city\", parsed.getCity(), addressObj);\n" +
//                "            }\n" +
//                "\n" +
//                "            if (stateEmpty) {\n" +
//                "                updateValue(predictionList, \"member_state\", parsed.getState(), addressObj);\n" +
//                "            }\n" +
//                "\n" +
//                "            if (zipEmpty) {\n" +
//                "                updateValue(predictionList, \"member_zipcode\", parsed.getZipcode(), addressObj);\n" +
//                "            }\n" +
//                "        }\n" +
//                "\n" +
//                "        return predictionList;\n" +
//                "    }\n" +
//                "\n" +
//                "    // ===================== LIST HELPERS =====================\n" +
//                "\n" +
//                "    private in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput\n" +
//                "    findFirst(List list, String sorItemName) {\n" +
//                "\n" +
//                "        for (int i = 0; i < list.size(); i++) {\n" +
//                "            in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput in =\n" +
//                "                (in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput) list.get(i);\n" +
//                "\n" +
//                "            if (sorItemName.equalsIgnoreCase(in.getSorItemName())) {\n" +
//                "                return in;\n" +
//                "            }\n" +
//                "        }\n" +
//                "        return null;\n" +
//                "    }\n" +
//                "\n" +
//                "    private void updateValue(\n" +
//                "        List list,\n" +
//                "        String sorItemName,\n" +
//                "        String value,\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput source) {\n" +
//                "\n" +
//                "        for (int i = 0; i < list.size(); i++) {\n" +
//                "\n" +
//                "            in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput in =\n" +
//                "                (in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput) list.get(i);\n" +
//                "\n" +
//                "            if (!sorItemName.equalsIgnoreCase(in.getSorItemName())) {\n" +
//                "                continue;\n" +
//                "            }\n" +
//                "\n" +
//                "            in.setAnswer(value);\n" +
//                "\n" +
//                "            if (value == null || value.trim().isEmpty()) {\n" +
//                "                clearDependentFields(in);\n" +
//                "            } else if (source != null) {\n" +
//                "                copyDependentFields(source, in);\n" +
//                "            }\n" +
//                "        }\n" +
//                "    }\n" +
//                "\n" +
//                "    private boolean hasValue(\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput in) {\n" +
//                "\n" +
//                "        return in != null &&\n" +
//                "            in.getAnswer() != null &&\n" +
//                "            in.getAnswer().trim().length() > 0;\n" +
//                "    }\n" +
//                "\n" +
//                "    private void clearDependentFields(\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput in) {\n" +
//                "\n" +
//                "        in.setLabel(\"\");\n" +
//                "        in.setSectionAlias(\"\");\n" +
//                "        in.setBBox(\"\");\n" +
//                "    }\n" +
//                "\n" +
//                "    private void copyDependentFields(\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput from,\n" +
//                "        in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput to) {\n" +
//                "\n" +
//                "        to.setLabel(from.getLabel());\n" +
//                "        to.setSectionAlias(from.getSectionAlias());\n" +
//                "        to.setBBox(from.getBBox());\n" +
//                "    }\n" +
//                "\n" +
//                "    // ===================== ADDRESS PARSER =====================\n" +
//                "\n" +
//                "    public AddressComponents parseAddress(String fullAddress) {\n" +
//                "\n" +
//                "        if (fullAddress == null || fullAddress.trim().isEmpty()) {\n" +
//                "            return new AddressComponents(\"\", \"\", \"\", \"\");\n" +
//                "        }\n" +
//                "\n" +
//                "        Matcher zipMatcher = ZIP_CODE_PATTERN.matcher(fullAddress);\n" +
//                "        if (!zipMatcher.find()) {\n" +
//                "            return new AddressComponents(fullAddress, \"\", \"\", \"\");\n" +
//                "        }\n" +
//                "\n" +
//                "        String zip = zipMatcher.group();\n" +
//                "        String withoutZip = fullAddress.substring(0, zipMatcher.start()).trim();\n" +
//                "\n" +
//                "        Matcher matcher = ENDING_ADDRESS_PATTERN.matcher(fullAddress);\n" +
//                "        if (matcher.find()) {\n" +
//                "\n" +
//                "            return new AddressComponents(\n" +
//                "                withoutZip.substring(0, matcher.start()).trim(),\n" +
//                "                matcher.group(1).trim(),\n" +
//                "                matcher.group(2).trim(),\n" +
//                "                matcher.group(3).trim()\n" +
//                "            );\n" +
//                "        }\n" +
//                "\n" +
//                "        return new AddressComponents(withoutZip, \"\", \"\", zip);\n" +
//                "    }\n" +
//                "\n" +
//                "    // ===================== DTO =====================\n" +
//                "\n" +
//                "    private class AddressComponents {\n" +
//                "        private String addressLine1;\n" +
//                "        private String city;\n" +
//                "        private String state;\n" +
//                "        private String zipcode;\n" +
//                "\n" +
//                "        AddressComponents(String a, String c, String s, String z) {\n" +
//                "            addressLine1 = a != null ? a : \"\";\n" +
//                "            city = c != null ? c : \"\";\n" +
//                "            state = s != null ? s : \"\";\n" +
//                "            zipcode = z != null ? z : \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        public String getAddressLine1() { return addressLine1; }\n" +
//                "        public String getCity() { return city; }\n" +
//                "        public String getState() { return state; }\n" +
//                "        public String getZipcode() { return zipcode; }\n" +
//                "    }\n" +
//                "}\n";
//    }
//
//    String get
//    String getNewbornDOBMapper(){
//        return "import org.slf4j.Logger;\n" +
//                "import java.text.ParseException;\n" +
//                "import java.text.SimpleDateFormat;\n" +
//                "import java.time.LocalDate;\n" +
//                "import java.util.Calendar;\n" +
//                "import java.util.Date;\n" +
//                "import java.util.Locale;\n" +
//                "import java.util.List;\n" +
//                "\n" +
//                "public class NewbornDOBMapper {\n" +
//                "\n" +
//                "    private Logger logger;\n" +
//                "\n" +
//                "    public NewbornDOBMapper(Logger logger) {\n" +
//                "        this.logger = logger;\n" +
//                "    }\n" +
//                "\n" +
//                "    // =====================================================\n" +
//                "    // ENTRY POINT (LIST → LIST)\n" +
//                "    // =====================================================\n" +
//                "\n" +
//                "    public List doCustomPredictionMapping(List predictionList, Long rootPipelineId) {\n" +
//                "\n" +
//                "        String logPrefix = \"[RootPipelineID: \" + rootPipelineId + \"] \";\n" +
//                "        logger.info(logPrefix + \"Entered NewbornDOBMapper.doCustomPredictionMapping\");\n" +
//                "\n" +
//                "        if (predictionList == null || predictionList.isEmpty()) {\n" +
//                "            return predictionList;\n" +
//                "        }\n" +
//                "\n" +
//                "        for (int i = 0; i < predictionList.size(); i++) {\n" +
//                "\n" +
//                "            Object obj = predictionList.get(i);\n" +
//                "            if (!(obj instanceof in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput)) {\n" +
//                "                continue;\n" +
//                "            }\n" +
//                "\n" +
//                "            in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput input =\n" +
//                "                    (in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput) obj;\n" +
//                "\n" +
//                "            if (!\"newborn_date_of_birth\"\n" +
//                "                    .equalsIgnoreCase(input.getSorItemName())) {\n" +
//                "                continue;\n" +
//                "            }\n" +
//                "\n" +
//                "            String validatedDob =\n" +
//                "                    DateValidator(input.getAnswer(), rootPipelineId, true);\n" +
//                "\n" +
//                "            input.setAnswer(validatedDob);\n" +
//                "        }\n" +
//                "\n" +
//                "        return predictionList;\n" +
//                "    }\n" +
//                "\n" +
//                "    // =====================================================\n" +
//                "    // DATE VALIDATOR\n" +
//                "    // =====================================================\n" +
//                "\n" +
//                "    public String DateValidator(Object dateValue, Long rootPipelineId, boolean isDOB) {\n" +
//                "\n" +
//                "        String logPrefix = \"[RootPipelineID: \" + rootPipelineId + \"] \";\n" +
//                "\n" +
//                "        if (!(dateValue instanceof String)) {\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        String inputDate = ((String) dateValue).trim();\n" +
//                "        if (inputDate.isEmpty()) {\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        int currentYear = LocalDate.now().getYear();\n" +
//                "\n" +
//                "        boolean isTwoDigitYear =\n" +
//                "                inputDate.matches(\"\\\\d{1,2}[-/:. ]\\\\d{1,2}[-/:. ]\\\\d{2}(?!\\\\d)\");\n" +
//                "\n" +
//                "        Date parsedDate = parseDate(inputDate);\n" +
//                "\n" +
//                "        if (parsedDate == null) {\n" +
//                "            logger.warn(logPrefix + \"DOB parse failed\");\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        if (isTwoDigitYear) {\n" +
//                "            parsedDate =\n" +
//                "                    convertTwoDigitYearToFourDigit(inputDate, currentYear, logPrefix);\n" +
//                "        }\n" +
//                "\n" +
//                "        if (parsedDate == null) {\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        Calendar parsedCal = Calendar.getInstance();\n" +
//                "        parsedCal.setTime(parsedDate);\n" +
//                "\n" +
//                "        Calendar currentCal = Calendar.getInstance();\n" +
//                "\n" +
//                "        if (parsedCal.after(currentCal) && isDOB) {\n" +
//                "            if (isTwoDigitYear) {\n" +
//                "                return futureDateFormatter(parsedDate, currentYear, logPrefix);\n" +
//                "            }\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        SimpleDateFormat out = new SimpleDateFormat(\"yyyy-MM-dd\");\n" +
//                "        return out.format(parsedDate);\n" +
//                "    }\n" +
//                "\n" +
//                "    // =====================================================\n" +
//                "    // HELPERS\n" +
//                "    // =====================================================\n" +
//                "\n" +
//                "    public Date deductCentury(Date inputDate, int currentYear, String logPrefix) {\n" +
//                "\n" +
//                "        if (inputDate == null) return null;\n" +
//                "\n" +
//                "        Calendar cal = Calendar.getInstance();\n" +
//                "        cal.setTime(inputDate);\n" +
//                "\n" +
//                "        if (cal.get(Calendar.YEAR) > currentYear) {\n" +
//                "            cal.add(Calendar.YEAR, -100);\n" +
//                "        }\n" +
//                "\n" +
//                "        return cal.getTime();\n" +
//                "    }\n" +
//                "\n" +
//                "    public String futureDateFormatter(Date parsedDate, int currentYear, String logPrefix) {\n" +
//                "\n" +
//                "        parsedDate = deductCentury(parsedDate, currentYear, logPrefix);\n" +
//                "        if (parsedDate == null) {\n" +
//                "            return \"\";\n" +
//                "        }\n" +
//                "\n" +
//                "        return new SimpleDateFormat(\"yyyy-MM-dd\").format(parsedDate);\n" +
//                "    }\n" +
//                "\n" +
//                "    private Date parseDate(String dateStr) {\n" +
//                "\n" +
//                "        dateStr = normalizeDateString(dateStr);\n" +
//                "\n" +
//                "        String[] patterns = {\n" +
//                "            \"M/d/yy\",\"M.d.yy\",\"M-d-yy\",\"M d yy\",\n" +
//                "            \"MM/dd/yy\",\"dd/MM/yy\",\"yyyy-MM-dd\",\n" +
//                "            \"yyyy/MM/dd\",\"MMM d, yyyy\",\"d MMM yyyy\",\n" +
//                "            \"MM/dd/yyyy\",\"M/d/yyyy\",\"dd-MM-yyyy\"\n" +
//                "        };\n" +
//                "\n" +
//                "        for (int i = 0; i < patterns.length; i++) {\n" +
//                "            try {\n" +
//                "                SimpleDateFormat fmt =\n" +
//                "                        new SimpleDateFormat(patterns[i], Locale.ENGLISH);\n" +
//                "                fmt.setLenient(false);\n" +
//                "                return fmt.parse(dateStr);\n" +
//                "            } catch (ParseException ignored) {}\n" +
//                "        }\n" +
//                "        return null;\n" +
//                "    }\n" +
//                "\n" +
//                "    private String normalizeDateString(String dateStr) {\n" +
//                "        return dateStr == null ? null :\n" +
//                "                dateStr.replaceAll(\"[^a-zA-Z0-9/\\\\- .]\", \"\").trim();\n" +
//                "    }\n" +
//                "\n" +
//                "    private Date convertTwoDigitYearToFourDigit(\n" +
//                "            String dob, int currentYear, String logPrefix) {\n" +
//                "\n" +
//                "        String[] patterns = {\n" +
//                "            \"MM/dd/yy\",\"dd/MM/yy\",\"MM-dd-yy\",\"MM.dd.yy\"\n" +
//                "        };\n" +
//                "\n" +
//                "        Date parsed = null;\n" +
//                "\n" +
//                "        for (int i = 0; i < patterns.length; i++) {\n" +
//                "            try {\n" +
//                "                SimpleDateFormat fmt =\n" +
//                "                        new SimpleDateFormat(patterns[i], Locale.ENGLISH);\n" +
//                "                fmt.setLenient(false);\n" +
//                "                parsed = fmt.parse(dob);\n" +
//                "                break;\n" +
//                "            } catch (ParseException ignored) {}\n" +
//                "        }\n" +
//                "\n" +
//                "        if (parsed == null) return null;\n" +
//                "\n" +
//                "        Calendar cal = Calendar.getInstance();\n" +
//                "        cal.setTime(parsed);\n" +
//                "\n" +
//                "        int yy = cal.get(Calendar.YEAR);\n" +
//                "        int century = currentYear / 100;\n" +
//                "        int pivot = (currentYear % 100) + 10;\n" +
//                "\n" +
//                "        if (yy <= pivot) {\n" +
//                "            cal.set(Calendar.YEAR, century * 100 + yy);\n" +
//                "        } else {\n" +
//                "            cal.set(Calendar.YEAR, (century - 1) * 100 + yy);\n" +
//                "        }\n" +
//                "\n" +
//                "        if (cal.after(Calendar.getInstance())) {\n" +
//                "            cal.add(Calendar.YEAR, -100);\n" +
//                "        }\n" +
//                "\n" +
//                "        return cal.getTime();\n" +
//                "    }\n" +
//                "}\n";
//    }


    @Test
    void testExecuteScriptsMemberAddressMapper() {

        log.info("Test Scenario: Execute MemberAddressMapper BeanShell Script");

        List<PostProcessingFieldsInput> inputList = new ArrayList<>();
        Map<String, List<PostProcessingFieldsInput>> inputMap = new HashMap<>();

        List<String> classes = Collections.singletonList("AuthIdValidator");
        inputList.add(createInput(1, "ORIGIN-1", 1,
                "multi_value", "PROVIDER_1", "member_id", "747U05007", "Provider", "provider npi", 0.95));
        // ---------- BEFORE ----------
        System.out.println("===== BEFORE PROVIDER NPI MAPPING =====");
        for (PostProcessingFieldsInput in : inputList) {
            System.out.println(
                    "Origin: " + in.getOriginId() +
                            " | Item: " + in.getSorItemName() +
                            " | Answer: " + in.getAnswer() +
                            " | Section: " + in.getSectionAlias() +
                            " | Score: " + in.getVqaScore()
            );
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
                                " | Score: " + in.getVqaScore()
                );
            }
        });
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
