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
                .documentId("SYNT_166529664")
                .eocId("H67100435")
                .originId("ORIGIN-1")
                .groupId("1")
                .authtoken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJHYXRlS2VlcGVyIiwiZXhwIjoxNzEyMjU5NTk1LCJpYXQiOjE3MTIxNzMxOTUsImVtYWlsIjoiZGVtb0BpbnRpY3MuYWkifQ.W0bEOwS2MHlpJUH0c6oB1zP1nCBUd9CvkbDNQb733qs")

                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");

        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("gatekeeper.appid", "demo@intics.ai"),
                Map.entry("tenant_id", "8"),
                Map.entry("gatekeeper.url", "http://192.168.10.248:40002/"),
                 Map.entry("write.batch.size", "5")));



        EocJsonGeneratorAction eocJsonGeneratoraction = new EocJsonGeneratorAction(action,log,eocJsonGenerator) ;
        eocJsonGeneratoraction.execute();

    }
}
