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
                .querySet("SELECT origin_id, '/data/output/81/112/83645/autorotation/grey_scale_converted_output/h_hart_packet_18_1.jpg' as input_file_path, paper_no, root_pipeline_id, tenant_id, group_id, process_id, process, batch_id, threshold\n" +
                                "FROM figure_detection.figure_detection_input_83003\n" +
                                "where group_id=88 AND tenant_id=81 and batch_id='BATCH-88_0';")
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
