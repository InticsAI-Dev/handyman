
package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.KafkaProductionResponseAction;
import in.handyman.raven.lib.model.KafkaProductionResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class KafkaProductionResponseTest {
    @Test
    void executeProduct() throws Exception {
        KafkaProductionResponse kafkaPublish = KafkaProductionResponse.builder()
                .name("kafka")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("product_outbound.kafka_production_response")
                .querySet("SELECT \n" +

                        "    ifd.document_id,\n" +
                        "    pozfi.origin_id,\n" +
                        "    pozfi.batch_id,\n" +
                        "    pozfi.tenant_id,\n" +
                        "    ifd.transaction_id,  \n" +
                        "   ifd.inbound_transaction_id,\n" +
                        "   ifd.request_txn_id,\n" +
                        "   ifd.file_name,\n" +
                        "   ab.root_pipeline_id\n" +
                        "FROM product_outbound.product_outbound_payload_queue_archive ab\n" +
                        "JOIN product_outbound.product_outbound_zip_file_input pozfi\n" +
                        "    ON ab.origin_id = pozfi.origin_id\n" +
                        "    AND ab.tenant_id = pozfi.tenant_id\n" +
                        "    AND ab.group_id = pozfi.group_id\n" +
                        "    AND ab.batch_id = pozfi.batch_id\n" +
                        "JOIN info.asset a\n" +
                        "    ON a.tenant_id = pozfi.tenant_id\n" +
                        "    AND a.batch_id = pozfi.batch_id\n" +
                        "JOIN inbound_config.ingestion_downloaded_file_details ifd\n" +
                        "    ON pozfi.file_name = a.file_name\n" +
                        "    AND ifd.file_name = a.file_name\n" +
                        "    AND ab.tenant_id = ifd.tenant_id\n" +
                        "WHERE ab.group_id = 64\n" +
                        "  AND ab.tenant_id = 1\n" +
                        "  AND ab.batch_id = 'BATCH-64_0'\n" +
                        "  AND ifd.transaction_id = 'TRZ-68';\n")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("kafka.production.response.url", "http://localhost:8080/aumiextractionapi/getAumiPayloadByTxnId");


        KafkaProductionResponseAction deliveryNotifyAction = new KafkaProductionResponseAction(actionExecutionAudit, log, kafkaPublish);
        deliveryNotifyAction.execute();
    }
}