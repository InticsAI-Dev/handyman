package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FileMerger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class FileMergerActionTest {

    @Test
    public void mergerTest() throws Exception {

        FileMerger fileMerger= FileMerger.builder()
                .name("file merger ")
                .condition(true)
                .outputDir("/data/output/")
                .requestBody("{\"outputDir\": \"/data/output/\", \"inputFilePaths\": [\"/data/output/auto_rotation/1220147418_0.jpg\"], \"outputFileName\": \"test_1.pdf\"}")
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size","1"),
                Map.entry("copro.file-merger.url","http://192.168.10.245:9300/v2/models/merger-service/versions/1/infer"),
                Map.entry("gen_group_id.group_id","1"),
                Map.entry("consumer.API.count","1"),
                Map.entry("write.batch.size","1")));

        FileMergerAction  fileMergerAction=new FileMergerAction(actionExecutionAudit,log,fileMerger);
        fileMergerAction.execute();


    }

}