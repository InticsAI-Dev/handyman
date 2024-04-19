package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EocJsonGenerator;
import org.junit.jupiter.api.Test;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


@Slf4j
public class EocJsonGeneratorTest {

    @Test
    public void eocJsonGenerator() throws Exception {

        EocJsonGenerator eocJsonGenerator = EocJsonGenerator.builder()
                    .name("eoc json generator")
                .resourceConn("intics_zio_db_conn")
                .documentId("julipedra_1")
                .eocId("julipedra_1_NID")
                .originId("ORIGIN-62")
                .groupId("1")
                .authtoken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJHYXRlS2VlcGVyIiwiZXhwIjoxNzEzNTI0NjcxLCJpYXQiOjE3MTM0MzgyNzEsImVtYWlsIjoia2lzc2Zsb3dfcmZAaW50aWNzLmFpIn0.lrvY6iUS2cOh1TiMYmODMr13JDdYcbS0xWkYZlWHLGA")

                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");

        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("gatekeeper.appid", "kissflow_rf@intics.ai"),
                Map.entry("tenant_id", "8"),
                Map.entry("batch_id","batch_1_0"),
                Map.entry("gatekeeper.url", "http://intics.gatekeeper:40002/"),
                 Map.entry("write.batch.size", "5")));



        EocJsonGeneratorAction eocJsonGeneratoraction = new EocJsonGeneratorAction(action,log,eocJsonGenerator) ;
        eocJsonGeneratoraction.execute();

    }
}
