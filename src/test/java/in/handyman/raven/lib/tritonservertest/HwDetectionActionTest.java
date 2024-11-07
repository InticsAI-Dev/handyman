package in.handyman.raven.lib.tritonservertest;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.HwDetectionAction;
import in.handyman.raven.lib.model.HwDetection;
import org.junit.jupiter.api.Test;

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
        action.getContext().put("copro.hw-detection.url", "http://copro.paper.classification:10185/copro/paper-classification/hw-detection");
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
                .directoryPath("/data/output")
                .modelPath("")
                .endPoint("https://api.replicate.com/v1/predictions")
                .querySet("SELECT encode as base64img,'BATCH-1' as batch_id, 'INT-1' as originId, '1234567' as preprocessedFileId, 1 as paperNo,'/data/output/pdf_to_image/1220147418/1220147418_0.jpg'  as filePath, '123456' as createdUserId, '123456y' as lastUpdatedUserId, '12345643' as tenantId,-1 as templateId, 90.00 as modelScore, 123 as modelRegistryId, 1 as group_id, 1 as process_id" +
                        " from macro.file_details_truth_audit ;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setActionId(1L);

        action.getContext().put("copro.hw-detection.url", "https://api.replicate.com/v1/predictions");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("paper.classification.consumer.API.count", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("okhttp.client.timeout", "10");
        action.getContext().put("copro.request.activator.handler.name", "REPLICATE");
        action.getContext().put("replicate.request.api.token", "");
        action.getContext().put("replicate.paper.classification.version", "80e47411b9d0bb66bb422bcfaa46738d8d44c9dfcc3630332b9cf40450bdda51");

        HwDetectionAction hwDetectionAction=new HwDetectionAction(action,log,hwDetection);
        hwDetectionAction.execute();
    }

}