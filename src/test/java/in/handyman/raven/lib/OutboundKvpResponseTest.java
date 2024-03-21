package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.OutboundKvpResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;
@Slf4j
public class OutboundKvpResponseTest {
    @Test
    public void outboundkvpresponse() throws Exception {

        OutboundKvpResponse outboundKvpResponse = OutboundKvpResponse.builder()
                .name("intellimatch ")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .resultTable("alchemy_response.alchemy_kvp_response")
                .processId("1234456")
                .querySet("\n" +
                        " SELECT ampq.producer_process_id as process_id, ap.group_id, ap.tenant_id, ap.root_pipeline_id, ap.origin_id as alchemy_origin_id, ap.origin_id as pipeline_origin_id   ,'batch_1' as batch_id      \n" +
                        " FROM product_outbound.product_outbound_payload_queue ampq\n" +
                        "join info.source_of_truth ap on ampq.origin_id = ap.origin_id\n" +
                        "limit 1\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("alchemy.outbound.kvp.url", "http://192.168.10.248:9000/v2/models/xenon-vqa-service/versions/1/infer"),
                Map.entry("consumer.intellimatch.API.count", "1"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        OutboundKvpResponseAction outboundKvpResponseAction = new OutboundKvpResponseAction(actionExecutionAudit, log, outboundKvpResponse);
        outboundKvpResponseAction.execute();
    }
}