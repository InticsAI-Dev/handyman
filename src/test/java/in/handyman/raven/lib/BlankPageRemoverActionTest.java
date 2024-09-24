package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.BlankPageRemover;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class BlankPageRemoverActionTest {


    @Test
            public void setBlankPageRemover() throws Exception {
        BlankPageRemover blankPageRemover= BlankPageRemover.builder()
                .processId(String.valueOf(1234567890))
                .condition(true)
                .name("blank page remover")
                .outputDir("/home/anandh.andrews@zucisystems.com/W-space/pr1-lambdas/agadia/OCR Sample Forms/")
                .querySet("\n" +
                        "SELECT a.origin_id, a.group_id ,c.file_path\n" +
                        "                 from info.preprocess_payload_queue a\n" +
                        "                 join info.source_of_origin b on a.origin_id=b.origin_id\n" +
                        "                 join info.asset c on b.file_id=c.file_id\n" +
                        "                 where a.status='IN_PROGRESS';")
                .resourceConn("intics_agadia_db_conn")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.blank-page-remover.url", "http://localhost:10189/copro/denoise/blank-page-remover");
        action.getContext().put("read.batch.size","1");
        action.getContext().put("write.batch.size","1");
        action.getContext().put("consumer.API.count","1");
        final BlankPageRemoverAction BlankPageRemoverAction = new BlankPageRemoverAction(action, log, blankPageRemover);
        BlankPageRemoverAction.execute();
    }


}