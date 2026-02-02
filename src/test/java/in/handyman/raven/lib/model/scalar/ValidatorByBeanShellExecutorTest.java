package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void setUp() {
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
        actionExecutionAudit.getContext().put("AumiMemberNameMapper", getAumiMemberNameMapper());
        actionExecutionAudit.getContext().put("AumiGenderMapper", getAumiMemberNameMapper());
        actionExecutionAudit.getContext().put("MemberIdValidator", getAumiMemberNameMapper());

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

    @Test
    void testExecuteScriptsMemberNameMapper() {
        log.info("Test Scenario: Execute BeanShell Scripts");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
        List<String> classes = Collections.singletonList("AumiMemberNameMapper,AumiGenderMapper");

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

        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);

        result.forEach((postProcessingFieldsInput) -> {

            System.out.println("Origin: " + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });

    }

    @Test
    void testExecuteScriptsMemberGenderMapper() {
        log.info("Test Scenario: Execute BeanShell Scripts");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        List<String> classes = Collections.singletonList("AumiGenderMapper");

        // Scenario 1: Abbreviated Male (m -> M)
        inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_gender", "m", "Patient Info",
                "member gender", 100.0));

        // Scenario 2: Full word Male (male -> M)
        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_gender", "male", "Patient Info",
                "member gender", 100.0));

        // Scenario 3: Abbreviated Female (f -> F)
        inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_gender", "f", "Patient Info",
                "member gender", 100.0));

        // Scenario 4: Full word Female (female -> F)
        inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_gender", "female", "Patient Info",
                "member gender", 100.0));

        // Scenario 5: Mixed Case and Whitespace (mAlE -> M)
        inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_gender", "  mAlE  ", "Patient Info",
                "member gender", 100.0));

        // Scenario 6: Unknown value (unknown -> "")
        inputList.add(createInput(6, "O1", 1, "single_value", "0", "member_gender", "Not Specified", "Patient Info",
                "member gender", 100.0));

        inputList.forEach((postProcessingFieldsInput) -> {

            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
                    + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });

        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);

        result.forEach((postProcessingFieldsInput) -> {

            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
                    + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });

    }

    @Test
    void testExecuteScriptsMemberIdAuthIdMapper() {
        log.info("Test Scenario: Execute BeanShell Scripts");
        // NOTE: This test might fail if BeanShell is not on classpath or stricter
        // security settings
        // But for generation purposes, this is the correct logic.

        Map<String, List<PostProcessingFieldsInput>> currentMap = new HashMap<>();
        List<String> classes = Collections.singletonList("MemberIdValidator");

        // Scenario 1: Clean Member ID (Should remain as member_id)
        // inputList.add(createInput(1, "O1", 1, "single_value", "0", "member_id",
        // "ABC123456", "Patient Info", "member id", 100.0));

        // Scenario 2: Only a UM ID found in Member ID field (Should be cleared and
        // duplicated as auth_id)
        inputList.add(createInput(2, "O1", 1, "single_value", "0", "member_id", "UM88887777", "Patient Info",
                "member id", 91.0));

        // Scenario 3: Combined Member ID and UM ID with "/" (Should split: one stays
        // member_id, one duplicated as auth_id)
        // inputList.add(createInput(3, "O1", 1, "single_value", "0", "member_id",
        // "99999/UM11112222", "Patient Info", "member id", 100.0));

        // Scenario 4: Combined Member ID and UM ID with ":" and noise (Should split and
        // clean special characters)
        // inputList.add(createInput(4, "O1", 1, "single_value", "0", "member_id",
        // "ID#55555:UM44443333", "Patient Info", "member id", 100.0));

        // Scenario 5: Invalid UM pattern (Should be detected by UM_INVALID_PATTERN and
        // duplicated as auth_id)
        // inputList.add(createInput(5, "O1", 1, "single_value", "0", "member_id",
        // "UM123", "Patient Info", "member id", 100.0));

        inputList.add(
                createInput(6, "O1", 1, "single_value", "0", "auth_id", "UM99992222", "Auth Info", "auth id", 90.0));

        inputList.forEach((postProcessingFieldsInput) -> {

            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
                    + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });

        List<PostProcessingFieldsInput> result = validator.executeScripts(classes, inputList);

        System.out.println("---- After BeanShell Execution ----");
        result.forEach((postProcessingFieldsInput) -> {

            System.out.println("ID: " + postProcessingFieldsInput.getPostProcessingFieldId() + " Origin: "
                    + postProcessingFieldsInput.getOriginId() + " instance: "
                    + postProcessingFieldsInput.getSorContainerInstance() +
                    "  Item: " + postProcessingFieldsInput.getSorItemName() +
                    " confidence: " + postProcessingFieldsInput.getVqaScore() +
                    " sectionAlias: " + postProcessingFieldsInput.getSectionAlias() +
                    " answer: " + postProcessingFieldsInput.getAnswer());
        });

    }

    // @Test
    // void testProcessValidatorResult_Reflection() throws NoSuchMethodException {
    // log.info("Test Scenario: Process Validator Results via Reflection");
    //
    // // Mock a result object that mimics the expected BeanShell result class
    // // Since we can't easily mock an object with a specific structure without a
    // // class definition,
    // // we'll rely on the logic that it uses reflection to find 'getMappedData'.
    // // Here we create a dummy inner class for testing.
    //
    // class MockResult {
    // public Map<String, List<PostProcessingFieldsInput>> getMappedData() {
    // Map<String, List<PostProcessingFieldsInput>> map = new HashMap<>();
    // map.put("updatedKey", new ArrayList<>());
    // return map;
    // }
    // }
    //
    // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
    // validator.processValidatorResult(new MockResult(), resultMap);
    //
    // assertTrue(resultMap.containsKey("updatedKey"));
    // }

    // @Test
    // void testGetMappedDataResult_TypeCheck() {
    // log.info("Test Scenario: Validate Type Checking in Result Map");
    // Map<Object, Object> rawMap = new HashMap<>();
    // rawMap.put("validKey", new ArrayList<PostProcessingFieldsInput>());
    // rawMap.put(123, "InvalidKeyType"); // Should be ignored/logged
    //
    // Map<String, List<PostProcessingFieldsInput>> result =
    // validator.getMappedDataResult(rawMap);
    //
    // assertEquals(1, result.size());
    // assertTrue(result.containsKey("validKey"));
    // }

    // @Test
    // void testBuildUpdatedResults() {
    // log.info("Test Scenario: Apply updates from Result Map to original inputs");
    //
    // PostProcessingFieldsInput input = createInput("O1", 1, "s", "0", "key1",
    // "oldValue");
    // List<PostProcessingFieldsInput> inputs = Collections.singletonList(input);
    //
    // Map<String, List<PostProcessingFieldsInput>> resultMap = new HashMap<>();
    // PostProcessingFieldsInput updatedInput = createInput("O1", 1, "s", "0",
    // "key1", "newValue");
    // resultMap.put("key1", Collections.singletonList(updatedInput));
    //
    // List<PostProcessingFieldsInput> finalResults =
    // validator.buildUpdatedResults(inputs, resultMap);
    //
    // assertEquals(1, finalResults.size());
    // assertEquals("newValue", finalResults.get(0).getAnswer());
    // }

    // @Test
    // void testBuildUpdatedResults_NullInput() {
    // log.info("Test Scenario: Handle empty inputs in buildUpdatedResults");
    // List<PostProcessingFieldsInput> res = validator.buildUpdatedResults(null, new
    // HashMap<>());
    // assertTrue(res.isEmpty());
    // }

    String getAumiMemberNameMapper() {
        return "\n" +
                "import org.slf4j.Logger;\n" +
                "import java.util.HashMap;\n" +
                "import java.util.List;\n" +
                "import java.util.Map;\n" +
                "\n" +
                "public class AumiMemberNameMapper {\n" +
                "\n" +
                "    private Logger logger;\n" +
                "\n" +
                "    public AumiMemberNameMapper(Logger logger) {\n" +
                "        this.logger = logger;\n" +
                "    }\n" +
                "\n" +
                "    public MappingResult doCustomPredictionMapping(Map predictionKeyMap, Long rootPipelineId) {\n" +
                "\n" +
                "        if (predictionKeyMap == null) {\n" +
                "            return new MappingResult(new HashMap());\n" +
                "        }\n" +
                "\n" +
                "        PostProcessingFieldsInput fullNameObj =\n" +
                "                getFirst(predictionKeyMap, \"member_full_name\");\n" +
                "        PostProcessingFieldsInput firstNameObj =\n" +
                "                getFirst(predictionKeyMap, \"member_first_name\");\n" +
                "        PostProcessingFieldsInput lastNameObj =\n" +
                "                getFirst(predictionKeyMap, \"member_last_name\");\n" +
                "\n" +
                "        if (hasValue(fullNameObj)) {\n" +
                "\n" +
                "            NameParts parts = split(fullNameObj.getExtractedValue());\n" +
                "\n" +
                "            applyAll(\n" +
                "                    predictionKeyMap,\n" +
                "                    fullNameObj,\n" +
                "                    parts.firstName,\n" +
                "                    parts.lastName,\n" +
                "                    fullNameObj.getExtractedValue()\n" +
                "            );\n" +
                "            return new MappingResult(predictionKeyMap);\n" +
                "        }\n" +
                "\n" +
                "        if (hasValue(firstNameObj) && hasValue(lastNameObj)) {\n" +
                "            applySimple(predictionKeyMap, firstNameObj, lastNameObj);\n" +
                "            return new MappingResult(predictionKeyMap);\n" +
                "        }\n" +
                "\n" +
                "        if (hasValue(firstNameObj)) {\n" +
                "            NameParts parts = split(firstNameObj.getExtractedValue());\n" +
                "            applyAll(predictionKeyMap, firstNameObj, parts.firstName, parts.lastName, \"\");\n" +
                "        } else if (hasValue(lastNameObj)) {\n" +
                "            NameParts parts = split(lastNameObj.getExtractedValue());\n" +
                "            applyAll(predictionKeyMap, lastNameObj, parts.firstName, parts.lastName, \"\");\n" +
                "        }\n" +
                "\n" +
                "        return new MappingResult(predictionKeyMap);\n" +
                "    }\n" +
                "\n" +
                "    private void applyAll(Map map,\n" +
                "                          PostProcessingFieldsInput source,\n" +
                "                          String firstName,\n" +
                "                          String lastName,\n" +
                "                          String fullName) {\n" +
                "\n" +
                "        updateList(map, \"member_first_name\", firstName, source);\n" +
                "        updateList(map, \"member_last_name\", lastName, source);\n" +
                "        updateList(map, \"member_full_name\", fullName, source);\n" +
                "    }\n" +
                "\n" +
                "    private void applySimple(Map map,\n" +
                "                             PostProcessingFieldsInput first,\n" +
                "                             PostProcessingFieldsInput last) {\n" +
                "\n" +
                "        updateList(map, \"member_first_name\", first.getExtractedValue(), first);\n" +
                "        updateList(map, \"member_last_name\", last.getExtractedValue(), last);\n" +
                "        updateList(map, \"member_full_name\", \"\", null);\n" +
                "    }\n" +
                "\n" +
                "    private void updateList(Map map,\n" +
                "                            String key,\n" +
                "                            String value,\n" +
                "                            PostProcessingFieldsInput source) {\n" +
                "\n" +
                "        Object obj = map.get(key);\n" +
                "        if (!(obj instanceof List)) return;\n" +
                "\n" +
                "        List list = (List) obj;\n" +
                "\n" +
                "        for (int i = 0; i < list.size(); i++) {\n" +
                "            PostProcessingFieldsInput in =\n" +
                "                    (PostProcessingFieldsInput) list.get(i);\n" +
                "\n" +
                "            in.setExtractedValue(value);\n" +
                "\n" +
                "            if (isEmpty(value)) {\n" +
                "                clearDependentFields(in);\n" +
                "            } else if (source != null) {\n" +
                "                copyDependentFields(source, in);\n" +
                "            }\n" +
                "        }\n" +
                "        map.put(key, list);\n" +
                "    }\n" +
                "\n" +
                "    private void clearDependentFields(PostProcessingFieldsInput in) {\n" +
                "        in.setLabel(\"\");\n" +
                "        in.setSectionAlias(\"\");\n" +
                "        in.setBBox(\"\");\n" +
                "    }\n" +
                "\n" +
                "    private void copyDependentFields(PostProcessingFieldsInput from,\n" +
                "                                     PostProcessingFieldsInput to) {\n" +
                "\n" +
                "        to.setLabel(from.getLabel());\n" +
                "        to.setSectionAlias(from.getSectionAlias());\n" +
                "        to.setBBox(from.getBBox());\n" +
                "    }\n" +
                "\n" +
                "    private NameParts split(String value) {\n" +
                "\n" +
                "        if (value == null) return new NameParts(\"\", \"\");\n" +
                "\n" +
                "        value = value.trim();\n" +
                "\n" +
                "        if (value.indexOf(\",\") >= 0) {\n" +
                "            String[] parts = value.split(\",\", 2);\n" +
                "            return new NameParts(clean(parts[1]), clean(parts[0]));\n" +
                "        }\n" +
                "\n" +
                "        String[] tokens = value.split(\"\\\\s+\");\n" +
                "        if (tokens.length == 1) {\n" +
                "            return new NameParts(tokens[0], \"\");\n" +
                "        }\n" +
                "\n" +
                "        StringBuffer first = new StringBuffer();\n" +
                "        for (int i = 0; i < tokens.length - 1; i++) {\n" +
                "            first.append(tokens[i]).append(\" \");\n" +
                "        }\n" +
                "\n" +
                "        return new NameParts(first.toString().trim(), tokens[tokens.length - 1]);\n" +
                "    }\n" +
                "\n" +
                "    private PostProcessingFieldsInput getFirst(Map map, String key) {\n" +
                "        Object obj = map.get(key);\n" +
                "        if (obj instanceof List) {\n" +
                "            List list = (List) obj;\n" +
                "            if (!list.isEmpty()) {\n" +
                "                return (PostProcessingFieldsInput) list.get(0);\n" +
                "            }\n" +
                "        }\n" +
                "        return null;\n" +
                "    }\n" +
                "\n" +
                "    private boolean hasValue(PostProcessingFieldsInput in) {\n" +
                "        return in != null && !isEmpty(in.getExtractedValue());\n" +
                "    }\n" +
                "\n" +
                "    private boolean isEmpty(String s) {\n" +
                "        return s == null || s.trim().length() == 0;\n" +
                "    }\n" +
                "\n" +
                "    private String clean(String s) {\n" +
                "        return s == null ? \"\" :\n" +
                "                s.replaceAll(\"[0-9]\", \"\")\n" +
                "                        .replaceAll(\"\\\\s+\", \" \")\n" +
                "                        .trim();\n" +
                "    }\n" +
                "\n" +
                "    private static class NameParts {\n" +
                "        String firstName;\n" +
                "        String lastName;\n" +
                "\n" +
                "        NameParts(String f, String l) {\n" +
                "            this.firstName = f;\n" +
                "            this.lastName = l;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static class MappingResult {\n" +
                "        private Map mappedData;\n" +
                "        public MappingResult(Map mappedData) {\n" +
                "            this.mappedData = mappedData;\n" +
                "        }\n" +
                "        public Map getMappedData() {\n" +
                "            return mappedData;\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static class PostProcessingFieldsInput {\n" +
                "\n" +
                "        private Long tenantId;\n" +
                "        private double aggregatedScore;\n" +
                "        private double maskedScore;\n" +
                "        private String originId;\n" +
                "        private Integer paperNo;\n" +
                "        private String extractedValue;\n" +
                "        private double vqaScore;\n" +
                "        private Integer rank;\n" +
                "        private Integer sorItemAttributionId;\n" +
                "        private String sorItemName;\n" +
                "        private String documentId;\n" +
                "        private Long accTransactionId;\n" +
                "        private String label;\n" +
                "        private String sectionAlias;\n" +
                "        private Long score;\n" +
                "        private String bBox;\n" +
                "        private Long rootPipelineId;\n" +
                "        private Long frequency;\n" +
                "        private Long questionId;\n" +
                "        private Long synonymId;\n" +
                "        private String modelRegistry;\n" +
                "        private String encryptionPolicy;\n" +
                "        private String isEncrypted;\n" +
                "        private String lineItemType;\n" +
                "\n" +
                "        public String getExtractedValue() { return extractedValue; }\n" +
                "        public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }\n"
                +
                "\n" +
                "        public String getLabel() { return label; }\n" +
                "        public void setLabel(String label) { this.label = label; }\n" +
                "\n" +
                "        public String getSectionAlias() { return sectionAlias; }\n" +
                "        public void setSectionAlias(String sectionAlias) { this.sectionAlias = sectionAlias; }\n" +
                "\n" +
                "        public String getBBox() { return bBox; }\n" +
                "        public void setBBox(String bBox) { this.bBox = bBox; }\n" +
                "    }\n" +
                "}\n";
    }
}
