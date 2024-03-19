package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.Intellimatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class intellimatchActionTest {
    @Test
    public void intellimatchTest() throws Exception {

        Intellimatch intellimatch = Intellimatch.builder()
                .name("intellimatch ")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .matchResult("control_data_macro.eval_drug_name")
                .inputSet("\n" +
                        " SELECT  a.file_name,a.origin_id,now() as created_on,a.group_id,a.root_pipeline_id,a.actual_value, \n" +
                        " a.extracted_value, a.confidence_score, a.tenant_id, 'batch_1' as batch_id \n" +
                        "FROM control_data_macro.drug_name_similarity_input a\n" +
                        "JOIN control_data.control_data_payload_queue b on a.origin_id=b.origin_id\n" +
                        "where  a.group_id='639' and a.tenant_id = 1\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("copro.intelli-match.url", "http://192.168.10.248:9200/v2/models/cos-service/versions/1/infer"),
                Map.entry("consumer.intellimatch.API.count", "1"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        IntellimatchAction intellimatchAction = new IntellimatchAction(actionExecutionAudit, log, intellimatch);
        intellimatchAction.execute();


    }

    }
