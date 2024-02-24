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

        OutboundDeliveryNotify outboundDeliveryNotify= OutboundDeliveryNotify.builder()
                .name("file merger ")
                .condition(true)
                .documentId("166730399_C1")
                .inticsZipUri("https://agadia.intics.ai/api/v1/downloadOutboundZip/166730399")
                .resourceConn("intics_agadia_db_conn")
                .zipChecksum("3t4ergfe32q345r6ty")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","1"),
                Map.entry("doc.delivery.notify.url","https://devlang.pahub.com/fax/api/Inbounddocument_in/docextractdeliverynotify"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("agadia.secretKey",""),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","1")));

        OutboundDeliveryNotifyAction  deliveryNotifyAction = new OutboundDeliveryNotifyAction(actionExecutionAudit,log,outboundDeliveryNotify);
        deliveryNotifyAction.execute();
    }
}