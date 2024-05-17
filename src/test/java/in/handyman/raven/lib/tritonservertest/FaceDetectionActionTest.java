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
                .querySet(
                        "select sot.origin_id,a.file_path as input_file_path ,sot.paper_no , sot.root_pipeline_id , \n" +
                                " a.tenant_id , sot.group_id,1 as process_id, 'FACE_DETECTION' as process\n" +
                                "from info.source_of_truth sot\n" +
                                "join info.asset a on a.file_id = sot.preprocessed_file_id \n" +
                                "where a.root_pipeline_id = 14368"
                )
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
