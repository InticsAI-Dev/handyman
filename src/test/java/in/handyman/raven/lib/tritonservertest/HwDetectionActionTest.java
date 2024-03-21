package in.handyman.raven.lib.tritonservertest;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.HwDetectionAction;
import in.handyman.raven.lib.model.HwDetection;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.reflections.Reflections.log;
class HwDetectionActionTest {
    @Test
    void execute() throws Exception {
        HwDetection hwDetection=HwDetection.builder()
                .name("paper classification macro")
                .condition(true)
                .endPoint("http://copro.paper.classification:10185/copro/paper-classification/hw-detection")
                .resourceConn("intics_zio_db_conn")
                .directoryPath("/home/sanjeeya.v@zucisystems.com/workspace/updated_jupyter/output")
                .modelPath("/home/sanjeeya.v@zucisystems.com/Documents/agadia/pr2/v1/hw_detection_v2.0.pth")
                .querySet("SELECT 'INT-1' as originId, '1234567' as preprocessedFileId, 1 as paperNo,'/data/output/1098/preprocess/paper_itemizer/pdf_to_image/cropped-bw-Applcation form -9872/cropped-bw-Applcation form -9872_0.jpg'  as filePath, '123456' as createdUserId, '123456y' as lastUpdatedUserId, '12345643' as tenantId,-1 as templateId, 90.00 as modelScore, 123 as modelId;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.hw-detection.url", "http://localhost:10189/copro/paper-classification/hw-detection");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("triton.request.activator", "false");
        action.setActionId(12345L);
        action.getContext().put("paper.classification.consumer.API.count", "1");
        action.getContext().put("consumer.API.count", "1");
        action.getContext().put("write.batch.size", "1");

        HwDetectionAction hwDetectionAction=new HwDetectionAction(action,log,hwDetection);
        hwDetectionAction.execute();
    }


    @Test
    void tritonServer() throws Exception {
        HwDetection hwDetection=HwDetection.builder()
                .name("paper classification macro")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .directoryPath("/data/output/")
                .modelPath("")
                .outputTable("paper_classification.paper_classification_result")
                .endPoint("http://192.168.10.248:8600/v2/models/paper-classification-service/versions/1/infer")
                .querySet("SELECT 'INT-1' as originId, '1234567' as preprocessedFileId, 1 as groupId, 1 as paperNo,'/data/input/resized_image.png'  as filePath, '/data/output/' as outputDir,'123456' as createdUserId, '123456y' as lastUpdatedUserId, '12345643' as tenantId,-1 as templateId, 90.00 as modelScore, 123 as modelRegistryId ,'1' as batchId;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.hw-detection.url", "http://192.168.10.248:8600/v2/models/paper-classification-service/versions/1/infer");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("paper.classification.consumer.API.count", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("okhttp.client.timeout", "10");
        action.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("triton.request.activator","true"),
                Map.entry("actionId", "1"))
        );


        HwDetectionAction hwDetectionAction=new HwDetectionAction(action,log,hwDetection);
        hwDetectionAction.execute();
    }

}