package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AutoRotation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
class AutoRotationActionTest {

    @Test
    void autoRotationTest() throws Exception {

        AutoRotation action= AutoRotation.builder()
                .name("auto rotation testing after copro optimization")
                .processId("138980744174170252")
                .resourceConn("intics_agadia_db_conn")
                .outputDir("/home/anandh.andrews@zucisystems.com/Downloads/QA_PAIR_OUTPUT/")
                .condition(true)
                .querySet("select origin_id,group_id,processed_file_path as file_path,paper_no,tenant_id,template_id,process_id\n" +
                        "\t\t            from info.paper_itemizer where status='COMPLETED'\n" +
                        "\t\t            and process_id='138980744174170252'\n")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("copro.autorotation.url","http://localhost:10183/copro/denoise/autorotation");
        actionExecutionAudit.setProcessId(138980079308730208L);
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","5")));

        AutoRotationAction action1=new AutoRotationAction(actionExecutionAudit,log,action);
        action1.execute();


    }

}