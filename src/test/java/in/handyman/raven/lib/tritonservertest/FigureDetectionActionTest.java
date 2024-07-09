package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.FigureDetectionAction;
import in.handyman.raven.lib.model.FigureDetection;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FigureDetectionActionTest {
    @Test
    public void tritonTest() throws Exception {
        FigureDetection figureDetection = new FigureDetection().builder()
                .name("figure detection")
                .condition(true)
                .endpoint("http://192.168.10.245:10386/copro/luffy/detection/figure")
                .resourceConn("intics_zio_db_conn")
                .outputDir("/data/")
                .outputTable("figure_detection.figure_detection_result")
                .querySet(
                        "(SELECT 'ORIGIN-9' as origin_id, '%2Fdata%2Foutput%2F81%2F2215%2F69608%2Fautorotation%2Fauto_rotation%2Falzhamar_construction_1.jpg' as input_file_path," +
                                " 1 as paper_no, 19600 as root_pipeline_id, 1 as tenant_id, 1 as group_id, 1 as process_id, " +
                                "'FACE_DETECTION' as process, 0.8 as threshold);"
                )
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("figure.detection.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("triton.request.figure.detection.activator", "false");


        FigureDetectionAction figureDetectionAction = new FigureDetectionAction(ac, log, figureDetection);

        figureDetectionAction.execute();
    }
}
