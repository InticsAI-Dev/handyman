package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ScalarAdapter;
import in.handyman.raven.lib.model.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
class ScalarAdapterActionTest {

    @Test
    void execute() throws Exception {

        final ScalarAdapter build = ScalarAdapter.builder()
                .condition(true)
                .name("Test ScalarAdapter")
                .processID("12345")
                .resultSet("SELECT dp.sor_item_name as sor_key,si.sor_item_id, dp.sor_question as question, dp.answer as input_value, dp.weight,dp.vqa_score,\n" +
                        "si.allowed_adapter , si.restricted_adapter ,dp.synonym_id, dp.question_id,'12345' as process_id,\n" +
                        "si.word_limit , si.word_threshold ,\n" +
                        "si.char_limit , si.char_threshold ,\n" +
                        "si.validator_threshold , si.allowed_characters ,\n" +
                        "si.comparable_characters, si.restricted_adapter_flag,\n" +
                        "dp.origin_id ,dp.paper_no ,dp.group_id,\n" +
                        "dp.created_user_id, dp.tenant_id,dp.b_box, dp.model_registry, dp.root_pipeline_id, dp.batch_id, si.is_encrypted::varchar, ep.encryption_policy as encryption_policy\n" +
                        "FROM sor_transaction.vqa_transaction dp\n" +
                        "JOIN sor_meta.sor_item si ON si.sor_item_name = dp.sor_item_name and si.tenant_id=dp.tenant_id\n" +
                        "join sor_meta.encryption_policies ep on ep.encryption_policy_id =si.encryption_policy_id\n" +
                        "join sor_meta.sor_container sc on si.sor_container_id=sc.sor_container_id and si.tenant_id=sc.tenant_id\n" +
                        "join sor_transaction.sor_transaction_payload_queue_archive st on st.origin_id=dp.origin_id\n" +
                        "WHERE dp.transaction_id =215 AND si.allowed_adapter !='ner'\n" +
                        "AND dp.answer is not null and sc.document_type='HEALTH_CARE';")
                .resourceConn("intics_zio_db_conn")

                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("validation.multiverse-mode", "true");
        action.getContext().put("validation.restricted-answers", "No,None of the above");
        action.getContext().put("pipeline.end.to.end.encryption", "true");
        action.getContext().put("pipeline.encryption.default.holder", "");
        action.getContext().put("validaiton.char-limit-count", "1");
        //action.getContext().put("copro.text-validation.url", "http://localhost:10189/copro/text-validation/patient");
        final ScalarAdapterAction scalarAdapterAction = new ScalarAdapterAction(action, log, build);
        scalarAdapterAction.execute();

    }

    @Test
    public void removePrefixAndSuffix() {
        Validator validator = Validator.builder()
                .inputValue("1 0/19/2021")
                .build();
        if (validator.getInputValue().length() > 2) {
            String originalString = validator.getInputValue();
            StringBuilder modifiedString = new StringBuilder(originalString);
            // Remove last alphabet character

            while (modifiedString.length() > 0 && Character.isAlphabetic(modifiedString.charAt(modifiedString.length() - 1))) {
                modifiedString.deleteCharAt(modifiedString.length() - 1);
            }
            // Remove first alphabet character
            while (Character.isAlphabetic(modifiedString.charAt(0))) {
                modifiedString.deleteCharAt(0);
            }
            validator.setInputValue(modifiedString.toString());
        }
        System.out.println(validator);
    }

    @Test
    public void testScrubbingDate() {
        Validator validator = Validator.builder()
                .inputValue("42511974")
                .build();
        if (!StringUtils.isNumeric(validator.getInputValue())) {
            System.out.println("yes");
        }
        Pattern pattern = Pattern.compile("(0?[1-9]|1?[0-2])([-./\\s]?)(0?[1-9]|[12]\\d|3[01])([-./\\s]?)(\\d{4}|\\d{2})");
        Matcher matcher = pattern.matcher(validator.getInputValue());
        if (matcher.find()) {
            // Extract matched groups (month, separator, day, year)
            String month = matcher.group(1);
            String separator = matcher.group(2);
            String day = matcher.group(3);
            String separator2 = matcher.group(4);
            String year = matcher.group(5);
            String inputValue = "";
            // Insert a default separator if it's missing
            if (separator.trim().isEmpty()) {
                separator = "-";
                inputValue = inputValue.concat(month + separator + day + separator + year);
                validator.setInputValue(inputValue);
                log.info("With Formatted date: " + month + separator + day + separator + year);
            } else {
                inputValue = inputValue.concat(month + separator + day + separator2 + year);
                validator.setInputValue(inputValue);
                log.info("Extracted date: " + month + separator + day + separator2 + year);
            }
        }
        System.out.println(validator);
    }


    @Test
    public void testScrubbingDate1() {
        Validator validator = Validator.builder()
                .inputValue("20/10/2021")
                .build();
        Pattern pattern = Pattern.compile("(0?[1-9]|1[0-2])([-./\\s])(0?[1-9]|[12]\\d|3[01])\\2(\\d{4}|\\d{2})");
        Matcher matcher = pattern.matcher(validator.getInputValue());
        if (matcher.find()) {
            String month = matcher.group(1);
            String separator = matcher.group(2);
            String day = matcher.group(3);
            String year = matcher.group(4);
            validator.setInputValue(month + separator + day + separator + year);
            System.out.println(validator);
        }
        System.out.println(validator);
    }


    @Test
    public void testPrefSuf() {
//        String dateRegValue = "1 0/12/2021";
        String dateRegValue = "1 0/1 2/ 2021";
        String correctedValue = dateRegValue;
        if (dateRegValue != null && !dateRegValue.isEmpty()) {
            int startIndex = 0;
            while (startIndex < dateRegValue.length() && (isAlphabetic(dateRegValue.charAt(startIndex)) || dateRegValue.charAt(startIndex) == ' ')) {
                startIndex++;
            }

            int endIndex = dateRegValue.length() - 1;
            while (endIndex >= 0 && (isAlphabetic(dateRegValue.charAt(endIndex)) || dateRegValue.charAt(endIndex) == ' ')) {
                endIndex--;
            }
            if (startIndex > endIndex) {
                correctedValue = dateRegValue;
//                validator.setInputValue(dateRegValue);
            } else {
                correctedValue = dateRegValue.substring(startIndex, endIndex + 1);
//                validator.setInputValue(dateRegValue.substring(startIndex, endIndex + 1));
            }
        }
        System.out.println(correctedValue.replaceAll("\\s", ""));
    }

    @Test
    public void scrubbingInput() {
        String validator = "mani kandan";
        String regix = "[^a-zA-Z0-9 ]";
        if (validator != null) {
            String correctedValue = validator.replaceAll(regix, "");
            System.out.println(correctedValue);
        }
    }


    @Test
    public void removePrefixAndSuffix1() {
        Validator validator = Validator.builder()
                .inputValue("NPI")
                .build();
        String originalString = validator.getInputValue().trim(); // Trim to remove leading/trailing spaces

        // Find the index of the first non-digit character
        int startIndex = 0;
        int length = originalString.length();
        while (startIndex < length && Character.isAlphabetic(originalString.charAt(startIndex))) {
            startIndex++;
        }

        // Find the index of the last non-digit character
        int endIndex = length - 1;
        while (endIndex >= 0 && Character.isAlphabetic(originalString.charAt(endIndex))) {
            endIndex--;
        }

        // Extract the substring containing only digits
        String modifiedString = originalString.substring(startIndex, endIndex + 1).trim(); // Trim to remove leading/trailing spaces
        validator.setInputValue(modifiedString);

        System.out.println(validator);
    }


    @Test
    public void remove() {
        String text = "NPI";


        int startIndex = 0;
        while (startIndex < text.length() && (isAlphabetic(text.charAt(startIndex)) || text.charAt(startIndex) == ' ')) {
            startIndex++;
        }

        int endIndex = text.length() - 1;
        while (endIndex >= 0 && (isAlphabetic(text.charAt(endIndex)) || text.charAt(endIndex) == ' ')) {
            endIndex--;
        }
        if (startIndex > endIndex) {
            System.out.println(""); // No non-alphabetic characters remaining
        }

//        System.out.println(text.substring(startIndex, endIndex + 1));
    }

    private static boolean isAlphabetic(char ch) {
        return Character.isLetter(ch);
    }
}


