package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MailServer;
import in.handyman.raven.lib.model.NerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class MailServerActionTest {



@Test
    void testCase() throws Exception {




    MailServer mailServer= MailServer.builder()
            .name("mailserver")
            .condition(true)
            .querySet("select file_name, invoice_rejection from ref_data.validation_results;")
            .resourceConn("intics_agadia_db_conn")
            .resultTable("sor_transaction.adapter_result_123456")
            .build();

    ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
    actionExecutionAudit.getContext().put("copro.text-validation.url","http://localhost:10190/copro/text-validation/patient");
    actionExecutionAudit.setProcessId(138980079308730208L);
    actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
            Map.entry("gen_group_id.group_id","5"),
            Map.entry("consumer.API.count","1"),
            Map.entry("write.batch.size","5")));


    MailServerAction mailServerAction=new MailServerAction(actionExecutionAudit,log,mailServer);
    mailServerAction.execute();
}
}