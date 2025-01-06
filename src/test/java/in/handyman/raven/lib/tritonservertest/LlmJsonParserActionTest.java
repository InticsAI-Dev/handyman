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
                .querySet("SELECT total_response_json as response, paper_no, 'ORIGIN-76499' as origin_id, group_id, tenant_id, root_pipeline_id,  \n" +
                        "batch_id, 'Primary', model_registry, 0 as image_dpi, 0.0 as image_width, 0.0 as image_height, created_on, 'TEXT_EXTRACTION' as process  \n" +
                        "from text_extraction.text_extraction_output_audit teia;")
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
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, '{\"forms\": {\"formElements\": [{\"key\": \"PATIENT NAME\", \"value\": \"Eywttuw H\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"ST. CHARLES (TIN)\", \"value\": \"111871039\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"NPI\", \"value\": \"116481529\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"finalized Policy and Technical Changes\", \"value\": \"null\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"DOS\", \"value\": \"05/18/2024 to 05/20/2024\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"determination\", \"value\": \"null\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"SENT VIA SIGNATURE CONFIRMATION\", \"value\": \"2321 2880 0000 0302 3880\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"final rule\", \"value\": \"null\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"time period\", \"value\": \"60 days\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"Contract year\", \"value\": \"2024\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"INSURANCE ID#\", \"value\": \"XPK45Y26492\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"CHS ACCOUNT#\", \"value\": \"C3067433450\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"1ST LEVEL OF APPEAL\", \"value\": \"null\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}, {\"key\": \"REASON\", \"value\": \"Lack of Medical Necessity\", \"confidence\": 0.0, \"boundingBox\": {\"x\": 0, \"y\": 0, \"width\": 0, \"height\": 0}}]}}' as response, paper_no,  origin_id, process_id, action_id, process, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, status, stage, message, category FROM kvp_extraction.kvp_extraction_output_audit keoa where root_pipeline_id=178047;")
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
                .outputTable("table_extraction_info.table_extraction_llm_json_parser_168329")
                .querySet("SELECT id, created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path, \n" +
                        "total_response_json, paper_no, origin_id, process_id, action_id, process, group_id, tenant_id, \n" +
                        "root_pipeline_id, batch_id, model_registry, status, stage, message, category, request, response, endpoint\n" +
                        "FROM table_extraction_info.table_extraction_output_177608;")
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
                .outputTable("checkbox_extraction.checkbox_extraction_llm_json_parser_109661")
                    .querySet("SELECT total_response_json as response, paper_no,  origin_id, group_id, tenant_id, root_pipeline_id, \n" +
                            "batch_id, 'Primary', model_registry, 0 as image_dpi, 0.0 as image_width, 0.0 as image_height, " +
                            "created_on, 'CHECKBOX_EXTRACTION' as process " +
                            "from checkbox_extraction.checkbox_extraction_output_audit teeoa \n" +
                            "where root_pipeline_id =178047;")
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
