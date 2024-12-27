package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LlmJsonParserAction;
import in.handyman.raven.lib.model.LlmJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LlmJsonParserActionTest {

    // Test Text extraction
    @Test
    public void TextTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("text_extraction.text_extraction_llm_json_parser_audit")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message, category\n" +
                        "FROM text_extraction.text_extraction_output_audit teoa \n" +
                        "WHERE origin_id ='ORIGIN-441'")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");

        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();

    }

    @Test
    public void KVPTest() throws Exception {
        // Test KVP Extraction
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("kvp_extraction.kvp_extraction_llm_json_parser_audit")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no,\n" +
                        "origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message, category\n" +
                        "FROM kvp_extraction.kvp_extraction_output_audit\n" +
                        "where origin_id ='ORIGIN-421' ")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");


        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();

    }


// Table Extraction Text
    @Test
    public void TableTest() throws Exception {
        // Test Extraction
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("table_extraction.table_extraction_llm_json_parser_109124")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, origin_id, \n" +
                        "process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message, category\n" +
                        "FROM table_extraction.table_extraction_output_109124")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");


        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();

    }


    // Table Extraction Text
    @Test
    public void CheckboxTest() throws Exception {
        // Checkbox Extraction
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("table_extraction.table_extraction_llm_json_parser_109124")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, \n" +
                        "origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message,\n" +
                        "category FROM checkbox_extraction.checkbox_extraction_output_109392\n" +
                        "WHERE id=2;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");

        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();

    }


}
