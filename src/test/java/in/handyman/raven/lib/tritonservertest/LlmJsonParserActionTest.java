package in.handyman.raven.lib.tritonservertest;


import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LlmJsonParserAction;
import in.handyman.raven.lib.model.LlmJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class LlmJsonParserActionTest {
    @Test
    public void tritonTest() throws Exception {
        LlmJsonParser llmJsonParser = LlmJsonParser.builder()
                .name("llm json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transaction.llm_json_parser_output_audit")
                .querySet("SELECT  total_response_json as response, paper_no,  origin_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry, category, now() as created_on\n" +
                        "FROM sor_transaction.radon_bbox_output_audit\n" +
                        "where root_pipeline_id =2649;")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("llm.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("sor.transaction.bbox.activator.enable", "true");
        ac.getContext().put("sor.transaction.confidence.activator.enable", "true");



        LlmJsonParserAction llmJsonParserAction = new LlmJsonParserAction(ac, log, llmJsonParser);

        llmJsonParserAction.execute();



    }
}
