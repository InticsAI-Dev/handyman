package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.OutboundDeliveryNotify;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
class OutboundDeliveryNotifyActionTest {

    @Test
    void execute() throws Exception {

        OutboundDeliveryNotify outboundDeliveryNotify = OutboundDeliveryNotify.builder()
                .name("file merger ")
                .condition(true)
                .documentId("166730399_C1")
                .inticsZipUri("https://agadia.intics.ai/api/v1/downloadOutboundZip/166730399")
                .resourceConn("intics_agadia_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("doc.delivery.notify.url", "https://devlang.pahub.com/fax/api/Inbounddocument_in/docextractdeliverynotify"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        OutboundDeliveryNotifyAction deliveryNotifyAction = new OutboundDeliveryNotifyAction(actionExecutionAudit, log, outboundDeliveryNotify);
        deliveryNotifyAction.execute();
    }

    @Test
    void executeProduct() throws Exception {

        OutboundDeliveryNotify outboundDeliveryNotify = OutboundDeliveryNotify.builder()
                .name("Outbound delivery notify")
                .condition(true)
                .documentId("166730399_C1")
                .inticsZipUri("http://192.168.14.15:5000/receive-payload")
                .resourceConn("intics_zio_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .querySet("\n" +
                        "select file_name as file_name, '' as file_uri,'' as zip_file_check_sum,\n" +
                        " 'http://192.168.14.15:5000/receive-payload' as endpoint,'${outbound.appId}' as app_id,'${outbound.appKeyId}' as app_secret_key,product_json as outbound_json,'Product' as outbound_condition\n" +
                        "    from product_outbound.product_outbound_zip_file_input  \n" +
                        "   where group_id=43")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", "https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g"),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        OutboundDeliveryNotifyAction deliveryNotifyAction = new OutboundDeliveryNotifyAction(actionExecutionAudit, log, outboundDeliveryNotify);
        deliveryNotifyAction.execute();
    }

    @Test
    void executeELV() throws Exception {

        OutboundDeliveryNotify outboundDeliveryNotify = OutboundDeliveryNotify.builder()
                .name("Outbound delivery notify for ELV ")
                .condition(true)
                .documentId("166730399_C1")
                .inticsZipUri("")
                .resourceConn("intics_zio_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .querySet("select ifd.file_name as file_name, '' as file_uri, '' as zip_file_check_sum,\n" +
                        "'http://0.0.0.0:5001/messages' as endpoint, \n" +
                        "pozfi.product_json as outbound_json, \n" +
                        "'ELV' as outbound_condition, pozfi.batch_id, ifd.document_id as document_id,\n" +
                        "'AES' as encryption_type, ifd.\"uuid\" as \"uuid\" , 'RgF7I3z5FC8k9HkKUm3Htb1HhZPBczdk' as encryption_key,ifd.transaction_id\n" +
                        "from product_outbound.product_outbound_zip_file_input pozfi join inbound_config.ingestion_file_details ifd \n" +
                        "on pozfi.file_name = ifd.file_name\n" +
                        "where pozfi.group_id=43 and pozfi.tenant_id = 115 and pozfi.batch_id = 'BATCH-43_0' and  ifd.transaction_id = 'TRZ-653'")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("kafka.endpoint", ""),
                Map.entry("kafka.encryptionType", "AES"),
                Map.entry("kafka.encryptionkey", "RgF7I3z5FC8k9HkKUm3Htb1HhZPBczdksVr9fqGbTwc="),
                Map.entry("read.batch.size", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", ""),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("write.batch.size", "1")));

        OutboundDeliveryNotifyAction deliveryNotifyAction = new OutboundDeliveryNotifyAction(actionExecutionAudit, log, outboundDeliveryNotify);
        deliveryNotifyAction.execute();
    }
}