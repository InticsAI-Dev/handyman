package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LlmJsonParserAction;
import in.handyman.raven.lib.model.LlmJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;



@Slf4j
public class LlmJsonParserActionTest {

    @Test
    public void textExtractionTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("text_extraction.text_extraction_llm_json_parser_audit")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, \n" +
                        "origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message,\n" +
                        "category FROM text_extraction.checkbox_extraction_output_109392")
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
    public void kvpExtractionTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("kvp_extraction.kvp_extraction_llm_json_parser_audit")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, \n" +
                        "origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message,\n" +
                        "category FROM kvp_extraction.kvp_extraction_output_109392")
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
    public void tableExtractionTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("table_extraction.table_extraction_llm_json_parser_audit")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, total_response_json as response, paper_no, \n" +
                        "origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message,\n" +
                        "category FROM table_extraction.table_extraction_output_109392")
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
    public void tritonTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("checkbox_extraction.kvp_extraction_llm_json_parser_109472")
                .querySet("sELECT total_response_json as response, paper_no,  origin_id, group_id, tenant_id, root_pipeline_id,\n" +
                        "                batch_id, 'Primary', model_registry, 0 as image_dpi, 0.0 as image_width, 0.0 as image_height, created_on, 'CHECKBOX_EXTRACTION' as process\n" +
                        "                from checkbox_extraction.checkbox_extraction_output_109472 teeoa\n" +
                        "                WHERE tenant_id = 115 AND model_registry = 'KRYPTON' and group_id ='446' and batch_id ='BATCH-446_0';")
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
