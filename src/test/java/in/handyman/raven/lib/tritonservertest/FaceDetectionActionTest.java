package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.FaceDetectionAction;
import in.handyman.raven.lib.model.FaceDetection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FaceDetectionActionTest {

    @Test
    public void tritonTest() throws Exception {
        FaceDetection faceDetection = new FaceDetection().builder()
                .name("face detection")
                .condition(true)
                .endpoint("http://192.168.10.245:12189/copro/chopper/face-detection")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/")
                .outputTable("face_detection.face_detection_result")
                .querySet("SELECT 'ORIGIN-3483' as origin_id, '/data/output/107/2855/91453/autorotation/auto_rotation/h_hart_cleaned_1.jpg' as input_file_path,\n" +
                        "1 as paper_no, 1 as root_pipeline_id, 1 as tenant_id, 1 as group_id, 1 as process_id, \n" +
                        "'face detection' as process,'batch_10_1' as batch_id")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("face.detection.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.face.detection.activator", "false");


        FaceDetectionAction faceDetectionAction = new FaceDetectionAction(ac, log, faceDetection);

        faceDetectionAction.execute();

    }
}
