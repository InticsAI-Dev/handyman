package in.handyman.raven.lib.tritonservertest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FaceDetectionActionTest {

    @Test
    public void tritonTest() throws Exception {
//        FaceDetection faceDetection = new FaceDetection().builder()
//                .name("face detection")
//                .condition(true)
//                .endpoint("http://192.168.10.245:12189/copro/chopper/face-detection")
//                .resourceConn("intics_zio_db_conn")
//                .outputDir("/data/")
//                .outputTable("face_detection.face_detection_result")
//                .querySet(
//                        "select 'ORIGIN-1' as  origin_id,'/data/output/pdf_to_image/h_hart_packet/h_hart_packet_17.jpg' as input_file_path ,1 as paper_no , 1 as root_pipeline_id , \n" +
//                                " 1 as tenant_id , 1 as group_id,1 as process_id, 'FACE_DETECTION' as process\n"
//                )
//                .build();
//
//        ActionExecutionAudit ac = new ActionExecutionAudit();
//        ac.setRootPipelineId(1234L);
//        ac.setActionId(1234L);
//        ac.setProcessId(123L);
//        ac.getContext().put("face.detection.consumer.API.count", "1");
//        ac.getContext().put("write.batch.size", "1");
//        ac.getContext().put("read.batch.size", "1");
//        ac.getContext().put("triton.request.face.detection.activator", "false");
//
//
//        FaceDetectionAction faceDetectionAction = new FaceDetectionAction(ac, log, faceDetection);
//
//        faceDetectionAction.execute();

    }
}
