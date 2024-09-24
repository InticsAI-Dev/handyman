package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AlchemyResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class AlchemyResponseActionTest {

    @Test
    void execute() throws Exception {

        AlchemyResponse alchemyResponse = AlchemyResponse.builder()
                .name("alchemy response action")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjg5MzU1MjA2LCJpYXQiOjE2ODkzMTIwMDYsImVtYWlsIjoiZGVtb0BpbnRpY3MuY29tIn0.0AUSfuHkA1iEGHr0qQ0EYl2zdJci0ZhOFWylE1wdHxM")
                .tenantId(1L)
                .querySet("select\t\tter.origin_id,ter.root_pipeline_id,\n" +
                        "                                ter.paper_no,\n" +
                        "                                ter.tenant_id,\n" +
                        "                                'FACE_DETECTION' as feature,\n" +
                        "                                ter.predicted_value ,\n" +
                        "                                ter.precision as confidence_score,\n" +
                        "                                ter.encode,\n" +
                        "                                ter.left_pos,\n" +
                        "                                ter.upper_pos,\n" +
                        "                                ter.right_pos,\n" +
                        "                                ter.lower_pos\n" +
                        "                        FROM face_detection.face_detection_result ter\n" +
                        "                        where ter.batch_id ='BATCH-94_0'")
                .resourceConn("intics_zio_db_conn")
                .condition(true).build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("alchemy.origin.valuation.url","http://localhost:8189/alchemy/api/v1/valuation/origin");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjg5MzU1MjA2LCJpYXQiOjE2ODkzMTIwMDYsImVtYWlsIjoiZGVtb0BpbnRpY3MuY29tIn0.0AUSfuHkA1iEGHr0qQ0EYl2zdJci0ZhOFWylE1wdHxM");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");

        AlchemyResponseAction alchemyResponseAction = new AlchemyResponseAction(actionExecutionAudit, log, alchemyResponse);
        alchemyResponseAction.execute();

    }
}