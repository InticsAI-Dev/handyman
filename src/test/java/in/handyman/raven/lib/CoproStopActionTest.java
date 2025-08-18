//package in.handyman.raven.lib;
//
//import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//@Slf4j
//class CoproStopActionTest {
//    @Test
//    void execute() throws Exception {
//        final CoproStop build = CoproStop.builder()
//                .condition(true)
//                .name("Test Copro start action")
//                .processID("1234")
//                .moduleName("ZERO_SHOT_CLASSIFIER")
//                .command("gunicorn zscApi:app --workers 2 --worker-class uvicorn.workers.UvicornWorker --bind 0.0.0.0:10183 --timeout 6000 --thread 2")
//                .resourceConn("intics_agadia_db_conn")
//                .build();
//        final ActionExecutionAudit action = ActionExecutionAudit.builder()
//                .build();
//        action.setRootPipelineId(11011L);
//        action.getContext().put("copro.admin.stop.server.url", "http://localhost:8999/copro/admin/process-stop");
//        final CoproStopAction coproStopAction = new CoproStopAction(action, log, build);
//        coproStopAction.execute();
//    }
//}