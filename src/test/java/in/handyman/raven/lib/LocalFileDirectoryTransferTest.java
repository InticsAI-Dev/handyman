package in.handyman.raven.lib;

import in.handyman.raven.lib.model.LocalDirectoryfileTransfer;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.LocalDirectoryfileTransferAction;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Slf4j
public class LocalFileDirectoryTransferTest {
    @Test
    void localFileTransferTest() throws Exception{

        LocalDirectoryfileTransfer localDirectoryfileTransfer= LocalDirectoryfileTransfer.builder()
                .name("local-directory-file-transfer-action")
                .condition(true)
                .resultTable("onboard_wizard_info.local_directory_info")
                .sourceDirectory("/home/christopher.paulraj@zucisystems.com/Documents/testing/input/")
                .destinationDirectoryPath("/home/christopher.paulraj@zucisystems.com/Documents/testing/output/")
                .resourceConn("intics_agadia_db_conn")
                .build();



        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

//        actionExecutionAudit.getContext().putAll(Map.ofEntries(
//                Map.entry("read.batch.size","1"),
//                Map.entry("copro.file-merger.url","http://192.168.10.245:9300/v2/models/merger-service/versions/1/infer"),
//                Map.entry("gen_group_id.group_id","1"),
//                Map.entry("consumer.API.count","1"),
//                Map.entry("write.batch.size","1")));

        LocalDirectoryfileTransferAction  localDirectoryfileTransferAction=new LocalDirectoryfileTransferAction(actionExecutionAudit,log,localDirectoryfileTransfer);
        localDirectoryfileTransferAction.execute();


    }


}
