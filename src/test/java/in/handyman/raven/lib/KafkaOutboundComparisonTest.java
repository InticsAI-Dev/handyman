package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.KafkaOutboundComparison;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class KafkaOutboundComparisonTest {
    @Test
    void executeProduct() throws Exception {
        KafkaOutboundComparison kafkaPublish = KafkaOutboundComparison.builder()
                .name("kafka")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("product_outbound.kafka_comparison_results")
                .querySet("SELECT  \n" +
                        "                            a.id AS processId, \n" +
                        "                            NULL AS groupId,  \n" +
                        "                            a.product_response AS productResponse, \n" +
                        "                            a.custom_response AS customResponse, \n" +
                        "                            a.origin_id AS originId, \n" +
                        "                            a.tenant_id AS tenantId, \n" +
                        "                            NULL AS rootPipelineId,  \n" +
                        "                            a.status AS status, \n" +
                        "                            NULL AS stage,  \n" +
                        "                            NULL AS message,  \n" +
                        "                            NULL AS triggeredUrl,  \n" +
                        "                            NULL AS feature,                              a.batch_id AS batchId, \n" +
                        "                            a.transaction_id AS inboundTransactionId \n" +
                        "                        FROM product_outbound.product_response_details a \n" +
                        "                        where a.origin_id = 'ORIGIN-8292';")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        KafkaOutboundComparisonAction deliveryNotifyAction = new KafkaOutboundComparisonAction(actionExecutionAudit, log, kafkaPublish);
        deliveryNotifyAction.execute();
    }
}