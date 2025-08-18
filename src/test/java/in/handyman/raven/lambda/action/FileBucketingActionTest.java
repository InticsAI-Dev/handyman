package in.handyman.raven.lambda.action;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.FileBucketingAction;
import in.handyman.raven.lib.model.FileBucketing;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
public class FileBucketingActionTest {
    @Test
    void execute() throws Exception {
        final FileBucketing fileBucketing = FileBucketing.builder()
                .condition(true)
                .name("FileBucketingTest")
                .inputDirectory("/home/sanjeeya.v@zucisystems.com/Documents/agadia_synthetic_samples/16_templates_only/16_template/")
                .outputDir("/home/sanjeeya.v@zucisystems.com/Documents/agadia_synthetic_samples/output_dir/")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder()
                .build();
        action.getContext().put("min-efficient-threshold", "1");
        action.getContext().put("max-efficient-threshold", "4");
        action.getContext().put("min-intensive-threshold", "5");
        action.getContext().put("max-intensive-threshold", "10");
        final FileBucketingAction scalarAdapterAction = new FileBucketingAction(action, log, fileBucketing);
        scalarAdapterAction.execute();
    }
}
