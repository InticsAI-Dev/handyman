package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ScalarAdapter;
import in.handyman.raven.lib.model.Validator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ScalarAdapterActionTest {

    @Test
    void execute() throws Exception {

        final ScalarAdapter build = ScalarAdapter.builder()
                .condition(true)
                .name("Test ScalarAdapter")
                .processID("138968829607360172")
                .resultSet(" SELECT distinct dp.sor_item_name as sor_key,si.sor_item_id, dp.sor_question as question, dp.answer as input_value, dp.weight,dp.vqa_score,\n" +
                        "                     si.allowed_adapter , si.restricted_adapter ,dp.synonym_id, dp.question_id,'${init_process_id.process_id}' as process_id,\n" +
                        "                     si.word_limit , si.word_threshold ,\n" +
                        "                     si.char_limit , si.char_threshold ,\n" +
                        "                     si.validator_threshold , si.allowed_characters ,\n" +
                        "                     si.comparable_characters, si.restricted_adapter_flag,\n" +
                        "                     dp.origin_id ,dp.paper_no ,dp.group_id,\n" +
                        "                     dp.created_user_id, dp.root_pipeline_id, dp.tenant_id,dp.b_box,dp.model_registry, dp.category\n" +
                        "                     FROM sor_transaction.vqa_transaction dp\n" +
                        "                     JOIN sor_meta.sor_item si ON si.sor_item_name = dp.sor_item_name\n" +
                        "                     WHERE dp.group_id = '116' AND si.allowed_adapter ='ner' AND dp.answer is not null" +
                        " AND dp.sor_item_name ='patient_name';\n" +
                        "   ")

                .resourceConn("intics_zio_db_conn")

                .build();


        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("validation.multiverse-mode", "true");
        action.getContext().put("validation.restricted-answers", "No,None of the above");
        action.getContext().put("validaiton.char-limit-count", "1");
        //action.getContext().put("copro.text-validation.url", "http://localhost:10189/copro/text-validation/patient");
        final ScalarAdapterAction scalarAdapterAction = new ScalarAdapterAction(action, log, build);
        scalarAdapterAction.execute();

    }

    @Test
    public void removePrefixAndSuffix() {
        Validator validator = Validator.builder()
                .inputValue("123 ak NPI")
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


