package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.KryptonJsonParserAction;
import in.handyman.raven.lib.KryptonKvpAction;
import in.handyman.raven.lib.model.KryptonKvp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class KryptonKvpActionTest {
    @Test
    public void tritonTest() throws Exception {
        KryptonKvp kryptonKvp = KryptonKvp.builder()
                .name("krypton kvp api call action")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("")
                .outputTable("krypton.krypton_kvp_output")
                .querySet("")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("krypton.kvp.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.krypton.kvp.activator", "false");


        KryptonKvpAction kryptonKvpAction = new KryptonKvpAction(ac, log, kryptonKvp);

        kryptonKvpAction.execute();

    }
}
