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
                .inticsZipUri("http://localhost:5002/messages")
                .resourceConn("intics_zio_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .querySet("select file_name as file_name, '' as file_uri,'' as zip_file_check_sum,\n" +
                        "  'http://localhost:5002/messages' as endpoint,'demo_user' as app_id,'demo_user' as app_secret_key,product_response as outbound_json\n" +
                        "  ,'OUTBOUND_ASIS' as outbound_condition, 'ITX-605' as document_id\n" +
                        "  from outbound_asis.outbound_alchemy_asis_response\n" +
                        "  where group_id=5101 and tenant_id = 115 ;")
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
}