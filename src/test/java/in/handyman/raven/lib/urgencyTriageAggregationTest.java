package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.UrgencyTriageAggregation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class urgencyTriageAggregationTest {
    @Test
    void testurgencyTRiageAggregation() throws Exception {
        UrgencyTriageAggregation urgencyTriageAggregation = UrgencyTriageAggregation.builder()
                .name("urgencyTriageAggregation")
                .resourceConn("intics_zio_db_conn")
                .inputDir("/home/christopher.paulraj@zucisystems.com/Pictures/Aggeregatio/")
                .outputDir("/home/christopher.paulraj@zucisystems.com/Pictures/out/")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId (11011L);
        action.getContext().put("ratio", ".40");
        UrgencyTriageAggregationAction urgencyTriageAggregationAction = new UrgencyTriageAggregationAction(action, log, urgencyTriageAggregation);
        urgencyTriageAggregationAction.execute();
    }
}


