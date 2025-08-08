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
                .outputTable("product_outbound.kafka_comparison_table")
                .querySet("SELECT created_on, created_user_id, last_updated_on, last_updated_user_id, status, \n" +
                        "version, production_response, document_id, extension, origin_id, tenant_id, transaction_id, \n" +
                        "requesttxnid, filename, batch_id, message \n" +
                        "FROM product_outbound.kafka_production_response \n" +
                        "WHERE requesttxnid = 'SI-123' and status='SUCCESS'")

                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        KafkaOutboundComparisonAction deliveryNotifyAction = new KafkaOutboundComparisonAction(actionExecutionAudit, log, kafkaPublish);
        deliveryNotifyAction.execute();
    }
}