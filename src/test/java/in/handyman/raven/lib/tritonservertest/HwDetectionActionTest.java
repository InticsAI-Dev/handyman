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
                .endPoint("http://localhost:10189/copro/paper-classification/hw-detection")
                .resourceConn("intics_zio_db_conn")
                .outputTable("paper_classification.paper_classification_result")
                .directoryPath("/home/christopher.paulraj@zucisystems.com/Music/output/")
                .querySet("SELECT 'INT-1' as originId, '1234567' as preprocessedFileId, 1 as group_id, 1 as paperNo,'/home/christopher.paulraj@zucisystems.com/Videos/workspace/dev/flow/agadia/dev/data/noise_testing/data/clear_resized/clear_img1.png' as filePath, '123456' as createdUserId, '123456y' as lastUpdatedUserId, '12345643' as tenantId,-1 as templateId, 90.00 as confidence_score, 'classification_model' as model_name, 1 as model_version, 1 as model_id;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.setActionId(1L);
        action.getContext().put("copro.hw-detection.url", "http://localhost:10189/copro/paper-classification/hw-detection");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("paper.classification.consumer.API.count", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("okhttp.client.timeout", "10");
        action.getContext().put("triton.request.activator","false");
        HwDetectionAction hwDetectionAction=new HwDetectionAction(action,log,hwDetection);
        hwDetectionAction.execute();
    }


    @Test
    void tritonServer() throws Exception {
        HwDetection hwDetection=HwDetection.builder()
                .name("paper classification macro")
                .condition(true)
                .resourceConn("intics_agadia_db_conn")
                .directoryPath("/data/output")
                .querySet("SELECT 'INT-1' as originId, '1234567' as preprocessedFileId, 1 as paperNo,'/data/input/elixir-2page/SYNT_166838894_c1.pdf'  as filePath, '123456' as createdUserId, '123456y' as lastUpdatedUserId, '12345643' as tenantId,-1 as templateId, 90.00 as modelScore, 123 as modelRegistryId;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.hw-detection.url", "http://localhost:8600/v2/models/paper-classifier-service/versions/1/infer");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("paper.classification.consumer.API.count", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("okhttp.client.timeout", "10");

        HwDetectionAction hwDetectionAction=new HwDetectionAction(action,log,hwDetection);
        hwDetectionAction.execute();
    }

}