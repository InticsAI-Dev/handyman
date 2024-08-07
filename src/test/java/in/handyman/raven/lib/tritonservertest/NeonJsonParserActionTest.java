package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.NeonJsonParserAction;
import in.handyman.raven.lib.model.NeonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class NeonJsonParserActionTest {
    @Test
    public void tritonTest() throws Exception {
        NeonJsonParser neonJsonParser = NeonJsonParser.builder()
                .name("neon json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("sor_transaction.neon_json_parser_output")
                .querySet("select total_response_json as response, paper_no,  origin_id, group_id, tenant_id, root_pipeline_id, batch_id, model_registry\n" +
                        "from sor_transaction.neon_kvp_output_audit\twhere id IN (81,80)\n" )
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("neon.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");


        NeonJsonParserAction neonJsonParserAction = new NeonJsonParserAction(ac, log, neonJsonParser);

        neonJsonParserAction.execute();

    }
}
