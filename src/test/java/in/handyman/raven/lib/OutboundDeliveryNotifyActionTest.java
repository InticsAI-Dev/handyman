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
                .inticsZipUri("https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g")
                .resourceConn("intics_zio_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .querySet("select file_name as file_name, '' as file_uri,'' as zip_file_check_sum,\n" +
                        "              'https://oxygenfinance.kissflow.com/integration/2/Ac8izeyQlKt7/webhook/YJ81YHEn8NDSqznEsNVezPsZBrpS5c9zvj4i4rSSbt2fHYpi2-ZwIgzbd4lTdRipsmT6FFVz2CrhLQI6xd6g' as endpoint,'${outbound.appId}' as app_id,'${outbound.appKeyId}' as app_secret_key,product_json as outbound_json,'Product' as outbound_condition\n" +
                        "              from product_outbound.product_outbound_zip_file_input  where group_id=101 and tenant_id =69 ;")
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