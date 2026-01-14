package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.scalar.ValidatorByBeanShellExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ValidatorByBeanShellExecutorTest {


    private final ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

    private final List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs = new ArrayList<>();
//    private ValidatorByBeanShellExecutor validatorByBeanShellExecutor;

    @Test
    void setUp() throws ExecutionException, InterruptedException {
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, actionExecutionAudit, log, 1);
        validatorByBeanShellExecutor.doRowWiseValidator();
    }

    /* ===================== PUBLIC METHOD ===================== */

    @Test
    void processOrigin() {

        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, actionExecutionAudit, log, 1);
        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputList = inputList();
        validatorByBeanShellExecutor.processOrigin("OriginId_1", inputList);

    }

    /* ===================== PRIVATE METHODS VIA REFLECTION ===================== */

    @Test
    void TestGroupByOrigin() {
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, actionExecutionAudit, log, 1);

        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputList = inputList();
        Map<String, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> result = validatorByBeanShellExecutor.groupByOrigin(inputList);
        System.out.println(result);
    }

    @Test
    public void getGroupByPage() {
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, actionExecutionAudit, log, 1);
        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputList = inputList();
        Map<Integer, List<PostProcessingExecutorAction.PostProcessingExecutorInput>> result = validatorByBeanShellExecutor.groupByPage(inputList);
        System.out.println(result);
    }


    @Test
    public void processPageTest() {
        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("outbound.mapper.multi.bsh.class.order", "DiagnosisServiceCodeValidator,AuthIdValidator,LOCValidator,LOSValidator");
        ac.getContext().put("DiagnosisServiceCodeValidator", sourceCode);

        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, ac, log, 1);
        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs = inputList();
        final String originId = "OriginId_1";
        final Integer pageNo = 1;
        validatorByBeanShellExecutor.processPage(originId, pageNo, pageInputs);


    }

    @Test
    public void createMapTest() {
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, actionExecutionAudit, log, 1);
        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs = inputList();
        Map<String, String> result = validatorByBeanShellExecutor.createMap(pageInputs);
        System.out.println(result);
    }


    @Test
    protected void loadScriptOrderTest() {

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("outbound.mapper.multi.bsh.class.order", "DiagnosisServiceCodeValidator,AuthIdValidator,LOCValidator,LOSValidator");

        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs = inputList();
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(pageInputs, ac, log, 1);
        List<String> result = validatorByBeanShellExecutor.loadScriptOrder(pageInputs);

        System.out.println(result);
    }


    @Test
    protected void executeScriptsTest() {

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.getContext().put("outbound.mapper.multi.bsh.class.order", "DiagnosisServiceCodeValidator,AuthIdValidator,LOCValidator,LOSValidator");
        ac.getContext().put("DiagnosisServiceCodeValidator", sourceCode);

        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(inputs, ac, log, 1);
        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs = inputList();
        Map<String, String> result = validatorByBeanShellExecutor.createMap(pageInputs);
        List<String> resultClass = validatorByBeanShellExecutor.loadScriptOrder(pageInputs);
        Map<String, String> resultFinal = validatorByBeanShellExecutor.executeScripts(resultClass, result);
        System.out.println(resultFinal);
    }


    @Test
    protected void getPostProcessedValidatorMapTest() {

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "1");
        ac.getContext().put("outbound.mapper.multi.bsh.class.order", "DiagnosisServiceCodeValidator,AuthIdValidator,LOCValidator,LOSValidator");
        ac.getContext().put("DiagnosisServiceCodeValidator", sourceCode);

        final List<PostProcessingExecutorAction.PostProcessingExecutorInput> pageInputs = inputList();
        ValidatorByBeanShellExecutor validatorByBeanShellExecutor = new ValidatorByBeanShellExecutor(pageInputs, ac, log, 1);
        Map<String, String> result = validatorByBeanShellExecutor.createMap(pageInputs);
        List<String> resultClass = validatorByBeanShellExecutor.loadScriptOrder(pageInputs);
        Map<String, String> resultFinal = validatorByBeanShellExecutor.executeScripts(resultClass, result);
        validatorByBeanShellExecutor.getPostProcessedValidatorMap("DiagnosisServiceCodeValidator", sourceCode, resultFinal, 1L);
        System.out.println(result);
    }


    @Test
    void testGetMappedDataResult() throws Exception {

        // Arrange: mixed-type mock data
        Map<Object, Object> mappedData = new HashMap<>();
        mappedData.put("member_name", "stephen");     // valid
        mappedData.put("provider_name", "antony");    // invalid
        // invalid

        ValidatorByBeanShellExecutor executor =
                new ValidatorByBeanShellExecutor(
                        new ArrayList<>(),
                        Mockito.mock(ActionExecutionAudit.class),
                        log,
                        1
                );

        Method method = ValidatorByBeanShellExecutor.class
                .getDeclaredMethod("getMappedDataResult", Map.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> result =
                (Map<String, String>) method.invoke(executor, mappedData);


        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));


    }


    @Test
    void testUpdateInputs_valueEmptied() throws Exception {

        // ---------- Arrange ----------
        PostProcessingExecutorAction.PostProcessingExecutorInput input =
                new PostProcessingExecutorAction.PostProcessingExecutorInput();

        input.setSorItemName("member_name");
        input.setExtractedValue("Stephen");
        input.setVqaScore(75.0);
        input.setAggregatedScore(80.0);
        input.setBbox("{old}");

        List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputs =
                new ArrayList<>();
        inputs.add(input);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("member_name", "stephen"); // emptied value

        ValidatorByBeanShellExecutor executor =
                new ValidatorByBeanShellExecutor(
                        new ArrayList<>(),
                        Mockito.mock(ActionExecutionAudit.class),
                        log,
                        1
                );

        Method method = ValidatorByBeanShellExecutor.class
                .getDeclaredMethod("updateInputs", List.class, Map.class);
        method.setAccessible(true);

        // ---------- Act ----------
        method.invoke(executor, inputs, resultMap);

        // ---------- Assert ----------
        assertEquals("stephen", input.getExtractedValue());
        assertEquals("{}", input.getBbox());
        assertEquals(0, input.getVqaScore());
        assertEquals(0, input.getAggregatedScore());

    }








    public List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputList(){
        PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput1 = new PostProcessingExecutorAction.PostProcessingExecutorInput();
        PostProcessingExecutorAction.PostProcessingExecutorInput postProcessingExecutorInput2 = new PostProcessingExecutorAction.PostProcessingExecutorInput();

        List<PostProcessingExecutorAction.PostProcessingExecutorInput> inputList = new ArrayList<>();

        postProcessingExecutorInput1.setEncryptionPolicy("AES256");
        postProcessingExecutorInput1.setExtractedValue("Stephen");
        postProcessingExecutorInput1.setSorItemName("member_first_name");
        postProcessingExecutorInput1.setVqaScore(60.0);
        postProcessingExecutorInput1.setOriginId("OriginId_1");
        postProcessingExecutorInput1.setPaperNo(1);

        inputList.add(postProcessingExecutorInput1);

        postProcessingExecutorInput2.setEncryptionPolicy("INTICS_ENC");
        postProcessingExecutorInput2.setExtractedValue("Barron");
        postProcessingExecutorInput2.setSorItemName("provider_name");
        postProcessingExecutorInput2.setVqaScore(80.0);
        postProcessingExecutorInput2.setOriginId("OriginId_2");
        postProcessingExecutorInput2.setEncryptionPolicy("AES256");
        postProcessingExecutorInput2.setExtractedValue("Barron");
        postProcessingExecutorInput2.setVqaScore(80.0);
        postProcessingExecutorInput2.setPaperNo(2);
        postProcessingExecutorInput1.setLineItemType("multi_value");
        inputList.add(postProcessingExecutorInput2);

        return inputList;
    }

    String sourceCode = "import org.slf4j.Logger;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Hashtable;\n" +
            "import java.util.Iterator;\n" +
            "import java.util.LinkedHashMap;\n" +
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "import java.util.regex.Matcher;\n" +
            "import java.util.regex.Pattern;\n" +
            "\n" +
            "public class DiagnosisServiceCodeValidator {\n" +
            "\n" +
            "    private Logger logger;\n" +
            "\n" +
            "    private List logMessages = new ArrayList();\n" +
            "    private Map serviceCodeToModifier = new LinkedHashMap();\n" +
            "    private Map serviceCodeToUnit = new LinkedHashMap();\n" +
            "    private List validDiagnosisCode = new ArrayList();\n" +
            "\n" +
            "    private static final String PURE_ALPHA_REGEX = \"^[A-Z]{5,7}$\";\n" +
            "    private static final Pattern NUMERIC_ONLY_1_2 = Pattern.compile(\"^\\\\d{1,2}$\");\n" +
            "    private static final Pattern X_QTY_PATTERN = Pattern.compile(\"[Xx*]\\\\s*(\\\\d{1,2})\");\n" +
            "    private static final Pattern TRAILING_DASH_ANY = Pattern.compile(\"-\\\\s*([A-Z0-9]{1,2})$\");\n" +
            "    private static final Pattern TRAILING_DASH_SINGLE_DIGIT = Pattern.compile(\"-\\\\s*(\\\\d)$\");\n" +
            "\n" +
            "    public DiagnosisServiceCodeValidator(Logger logger) {\n" +
            "        this.logger = logger;\n" +
            "    }\n" +
            "\n" +
            "    public MappingResult doCustomPredictionMapping(Map predictionMap, Long rootPipelineId) {\n" +
            "        if (predictionMap == null) {\n" +
            "            predictionMap = new Hashtable();\n" +
            "        }\n" +
            "\n" +
            "        String svcInput = getAsString(predictionMap, \"service_code\");\n" +
            "        String diagInput = getAsString(predictionMap, \"diagnosis_code\");\n" +
            "        String qtyUnitsInput = getAsString(predictionMap, \"service_quantity_units\");\n" +
            "        String qtyVisits = getAsString(predictionMap, \"service_quantity_visits\");\n" +
            "\n" +
            "        serviceCodeToModifier.clear();\n" +
            "        serviceCodeToUnit.clear();\n" +
            "        validDiagnosisCode.clear();\n" +
            "        logMessages.clear();\n" +
            "\n" +
            "        processServiceCodes(svcInput);\n" +
            "        processDiagnosisCodes(diagInput);\n" +
            "\n" +
            "        String cleanVisits = extractNumbers(qtyVisits);\n" +
            "        String globalUnits = extractNumbers(qtyUnitsInput);\n" +
            "\n" +
            "        List validServiceCode = new ArrayList(serviceCodeToModifier.keySet());\n" +
            "        List finalModifiers = new ArrayList();\n" +
            "        List finalUnits = new ArrayList();\n" +
            "\n" +
            "        String[] globalUnitArray = globalUnits.length() > 0 ? globalUnits.split(\",\") : new String[0];\n" +
            "        int globalIdx = 0;\n" +
            "\n" +
            "        boolean anyParsedUnit = false;\n" +
            "        Iterator unitIter = serviceCodeToUnit.values().iterator();\n" +
            "        while (unitIter.hasNext()) {\n" +
            "            String unit = (String) unitIter.next();\n" +
            "            if (unit != null && !\"*\".equals(unit)) {\n" +
            "                anyParsedUnit = true;\n" +
            "                break;\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        Iterator iter = validServiceCode.iterator();\n" +
            "        while (iter.hasNext()) {\n" +
            "            String code = (String) iter.next();\n" +
            "\n" +
            "            String parsedMod = (String) serviceCodeToModifier.get(code);\n" +
            "            String mod = (parsedMod != null && !\"*\".equals(parsedMod)) ? parsedMod : \"*\";\n" +
            "            finalModifiers.add(mod);\n" +
            "\n" +
            "            String parsedUnit = (String) serviceCodeToUnit.get(code);\n" +
            "            if (parsedUnit != null && !\"*\".equals(parsedUnit)) {\n" +
            "                finalUnits.add(parsedUnit);\n" +
            "            } else {\n" +
            "                // Only apply global units if exactly one service code and no parsed unit\n" +
            "                if (validServiceCode.size() == 1 && !anyParsedUnit && globalIdx < globalUnitArray.length) {\n" +
            "                    finalUnits.add(globalUnitArray[globalIdx].trim());\n" +
            "                    globalIdx++;\n" +
            "                } else {\n" +
            "                    finalUnits.add(\"*\");\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        predictionMap.put(\"service_code\", join(validServiceCode));\n" +
            "        predictionMap.put(\"diagnosis_code\", join(validDiagnosisCode));\n" +
            "        predictionMap.put(\"service_quantity_units\", join(finalUnits));\n" +
            "        predictionMap.put(\"service_quantity_visits\", cleanVisits);\n" +
            "\n" +
            "        if (finalModifiers.size() > 0) {\n" +
            "            predictionMap.put(\"service_code_modifier\", join(finalModifiers));\n" +
            "        } else {\n" +
            "            predictionMap.remove(\"service_code_modifier\");\n" +
            "        }\n" +
            "\n" +
            "        ensureEmptyString(predictionMap, \"service_code\");\n" +
            "        ensureEmptyString(predictionMap, \"diagnosis_code\");\n" +
            "        ensureEmptyString(predictionMap, \"service_quantity_units\");\n" +
            "        ensureEmptyString(predictionMap, \"service_quantity_visits\");\n" +
            "\n" +
            "        return new MappingResult(new Hashtable(predictionMap), new ArrayList(logMessages));\n" +
            "    }\n" +
            "\n" +
            "    private void ensureEmptyString(Map map, String key) {\n" +
            "        Object value = map.get(key);\n" +
            "        if (value == null || !(value instanceof String) || ((String)value).trim().length() == 0) {\n" +
            "            map.put(key, \"\");\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private void processServiceCodes(String input) {\n" +
            "        if (isEmpty(input)) return;\n" +
            "\n" +
            "        String[] parts = input.split(\",\");\n" +
            "        for (int i = 0; i < parts.length; i++) {\n" +
            "            String token = parts[i].trim();\n" +
            "            if (token.length() == 0 || token.indexOf('.') != -1) continue;\n" +
            "            addServiceCode(token);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private void addServiceCode(String raw) {\n" +
            "        String upper = raw.toUpperCase();\n" +
            "        String clean = keepAlnum(upper);\n" +
            "\n" +
            "        if (clean.length() < 5 || clean.length() > 7) {\n" +
            "            return;\n" +
            "        }\n" +
            "        if (clean.matches(PURE_ALPHA_REGEX)) {\n" +
            "            return;\n" +
            "        }\n" +
            "        if (!clean.matches(\".*\\\\d.*\")) {\n" +
            "            return;\n" +
            "        }\n" +
            "\n" +
            "        String baseCode = clean.substring(0, 5);\n" +
            "        String currentModifier = \"*\";\n" +
            "        String currentUnit = \"*\";\n" +
            "\n" +
            "        if (clean.length() == 7) {\n" +
            "            char c5 = clean.charAt(5);\n" +
            "            char c6 = clean.charAt(6);\n" +
            "            if (c5 == 'X' && Character.isDigit(c6)) {\n" +
            "                currentUnit = String.valueOf(c6);\n" +
            "                updateMaps(baseCode, currentModifier, currentUnit);\n" +
            "                return;\n" +
            "            }\n" +
            "            // Only accept two-letter modifier for 7-char code\n" +
            "            if (Character.isLetter(c5) && Character.isLetter(c6)) {\n" +
            "                currentModifier = clean.substring(5);\n" +
            "                updateMaps(baseCode, currentModifier, currentUnit);\n" +
            "                return;\n" +
            "            }\n" +
            "            // If not XX or two letters → treat as no modifier\n" +
            "        }\n" +
            "\n" +
            "        int pos = indexAfterNthAlnum(upper, 5);\n" +
            "        String trailing = pos < upper.length() ? upper.substring(pos).trim() : \"\";\n" +
            "\n" +
            "        Matcher singleDigitMatcher = TRAILING_DASH_SINGLE_DIGIT.matcher(upper);\n" +
            "        if (singleDigitMatcher.find()) {\n" +
            "            currentUnit = singleDigitMatcher.group(1);\n" +
            "            trailing = trailing.replaceFirst(\"-\\\\s*\" + singleDigitMatcher.group(1), \"\").trim();\n" +
            "        } else {\n" +
            "            Matcher dashMatcher = TRAILING_DASH_ANY.matcher(upper);\n" +
            "            if (dashMatcher.find()) {\n" +
            "                String candidate = dashMatcher.group(1);\n" +
            "                // Enforce exactly 2 characters for modifier after dash\n" +
            "                if (candidate.length() == 2 && candidate.matches(\"[A-Z0-9]{2}\")) {\n" +
            "                    currentModifier = candidate;\n" +
            "                }\n" +
            "                trailing = trailing.replaceFirst(\"-\\\\s*\" + Pattern.quote(dashMatcher.group(1)), \"\").trim();\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        Matcher xMatcher = X_QTY_PATTERN.matcher(trailing);\n" +
            "        if (xMatcher.find()) {\n" +
            "            currentUnit = xMatcher.group(1);\n" +
            "            trailing = trailing.replaceFirst(\"[Xx*]\\\\s*\" + xMatcher.group(1), \"\").trim();\n" +
            "        }\n" +
            "\n" +
            "        String extractedMod = extractSingleModifier(trailing);\n" +
            "        if (extractedMod != null) {\n" +
            "            currentModifier = extractedMod;\n" +
            "        }\n" +
            "\n" +
            "        if (clean.length() <= 6 && \"*\".equals(currentUnit) && \"*\".equals(currentModifier)) {\n" +
            "            baseCode = clean;\n" +
            "        }\n" +
            "\n" +
            "        updateMaps(baseCode, currentModifier, currentUnit);\n" +
            "    }\n" +
            "\n" +
            "    private void updateMaps(String baseCode, String modifier, String unit) {\n" +
            "        serviceCodeToModifier.put(baseCode, modifier);\n" +
            "        serviceCodeToUnit.put(baseCode, unit);\n" +
            "    }\n" +
            "\n" +
            "    private String extractSingleModifier(String input) {\n" +
            "        if (isEmpty(input)) return null;\n" +
            "        String[] tokens = input.split(\"[\\\\-\\\\s]+\");\n" +
            "        for (int i = 0; i < tokens.length; i++) {\n" +
            "            String t = tokens[i].trim().toUpperCase();\n" +
            "            if (t.length() == 0) continue;\n" +
            "            // Must be exactly 2 characters AND not purely numeric (to exclude units like \"90\")\n" +
            "            if (t.length() == 2 && t.matches(\"[A-Z0-9]{2}\") && !t.matches(\"\\\\d{2}\")) {\n" +
            "                return t;\n" +
            "            }\n" +
            "        }\n" +
            "        return null;\n" +
            "    }\n" +
            "\n" +
            "    private void processDiagnosisCodes(String input) {\n" +
            "        if (isEmpty(input)) return;\n" +
            "\n" +
            "        String[] parts = input.split(\",\");\n" +
            "        for (int i = 0; i < parts.length; i++) {\n" +
            "            String original = parts[i];\n" +
            "            String code = original.replaceAll(\"[^A-Za-z0-9.]\", \"\").toUpperCase();\n" +
            "\n" +
            "            if (code.length() < 3 || code.length() > 7) {\n" +
            "                logMessages.add(\"Rejected diagnosis code (invalid length): \" + original);\n" +
            "                continue;\n" +
            "            }\n" +
            "            if (code.matches(\"^[A-Z]+$\")) {\n" +
            "                logMessages.add(\"Rejected diagnosis code (pure alpha): \" + original);\n" +
            "                continue;\n" +
            "            }\n" +
            "            if (!code.matches(\".*\\\\d.*\")) {\n" +
            "                logMessages.add(\"Rejected diagnosis code (no digit): \" + original);\n" +
            "                continue;\n" +
            "            }\n" +
            "\n" +
            "            int dotPos = code.indexOf('.');\n" +
            "            if (code.length() == 7) {\n" +
            "                if (dotPos != 3) {\n" +
            "                    logMessages.add(\"Rejected 7-char diagnosis code '\" + original + \"' - must have decimal at 4th position (e.g. A12.B34)\");\n" +
            "                    continue;\n" +
            "                }\n" +
            "            } else {\n" +
            "                if (dotPos != -1 && dotPos != 3) {\n" +
            "                    logMessages.add(\"Rejected diagnosis code (dot not at 4th position): \" + original);\n" +
            "                    continue;\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            if (code.indexOf('.', dotPos + 1) != -1) {\n" +
            "                logMessages.add(\"Rejected diagnosis code (multiple dots): \" + original);\n" +
            "                continue;\n" +
            "            }\n" +
            "\n" +
            "            if (!validDiagnosisCode.contains(code)) {\n" +
            "                validDiagnosisCode.add(code);\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        if (validDiagnosisCode.isEmpty() && input.trim().length() > 0) {\n" +
            "            logMessages.add(\"All diagnosis codes invalid - setting diagnosis_code to empty\");\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private String extractNumbers(String s) {\n" +
            "        if (isEmpty(s)) return \"\";\n" +
            "        StringBuffer sb = new StringBuffer();\n" +
            "        Matcher m = Pattern.compile(\"\\\\d+\").matcher(s);\n" +
            "        boolean first = true;\n" +
            "        while (m.find()) {\n" +
            "            if (!first) sb.append(\",\");\n" +
            "            String num = m.group();\n" +
            "            if (num.length() > 1 && num.startsWith(\"0\")) {\n" +
            "                num = num.replaceFirst(\"^0+\", \"\");\n" +
            "                if (num.isEmpty()) num = \"0\";\n" +
            "            }\n" +
            "            sb.append(num);\n" +
            "            first = false;\n" +
            "        }\n" +
            "        return sb.toString();\n" +
            "    }\n" +
            "\n" +
            "    private boolean isEmpty(String s) {\n" +
            "        return s == null || s.trim().length() == 0;\n" +
            "    }\n" +
            "\n" +
            "    private String keepAlnum(String s) {\n" +
            "        StringBuffer out = new StringBuffer();\n" +
            "        for (int i = 0; i < s.length(); i++) {\n" +
            "            char c = s.charAt(i);\n" +
            "            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {\n" +
            "                out.append(c);\n" +
            "            }\n" +
            "        }\n" +
            "        return out.toString();\n" +
            "    }\n" +
            "\n" +
            "    private int indexAfterNthAlnum(String s, int n) {\n" +
            "        int count = 0;\n" +
            "        for (int i = 0; i < s.length(); i++) {\n" +
            "            char c = s.charAt(i);\n" +
            "            if (Character.isLetterOrDigit(c)) {\n" +
            "                count++;\n" +
            "                if (count == n) return i + 1;\n" +
            "            }\n" +
            "        }\n" +
            "        return s.length();\n" +
            "    }\n" +
            "\n" +
            "    private String join(List list) {\n" +
            "        if (list.isEmpty()) return \"\";\n" +
            "        StringBuffer sb = new StringBuffer();\n" +
            "        for (int i = 0; i < list.size(); i++) {\n" +
            "            if (i > 0) sb.append(\",\");\n" +
            "            sb.append(list.get(i));\n" +
            "        }\n" +
            "        return sb.toString();\n" +
            "    }\n" +
            "\n" +
            "    private String getAsString(Map map, String key) {\n" +
            "        Object o = map.get(key);\n" +
            "        if (o == null) return \"\";\n" +
            "        return o.toString().trim();\n" +
            "    }\n" +
            "\n" +
            "    public static class MappingResult {\n" +
            "        private Map mappedData;\n" +
            "        private List logMessages;\n" +
            "\n" +
            "        public MappingResult(Map mappedData, List logMessages) {\n" +
            "            this.mappedData = mappedData != null ? new Hashtable(mappedData) : new Hashtable();\n" +
            "            this.logMessages = logMessages != null ? new ArrayList(logMessages) : new ArrayList();\n" +
            "        }\n" +
            "\n" +
            "        public Map getMappedData() {\n" +
            "            return mappedData;\n" +
            "        }\n" +
            "\n" +
            "        public List getLogMessages() {\n" +
            "            return logMessages;\n" +
            "        }\n" +
            "    }\n" +
            "}";

}
