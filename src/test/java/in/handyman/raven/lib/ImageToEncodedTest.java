package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ImageToBaseInputTable;
import in.handyman.raven.lib.model.ImageToBaseOutputTable;
import in.handyman.raven.lib.model.ImageToEncodedFile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j

class ImageToEncodedTest {
    // Mock dependencies

    @Test
    public void ImageToEncode() throws Exception {

        ImageToEncodedFile imageToEncodedFile=ImageToEncodedFile.builder()
                .querySet("SELECT originId, paperNo, preprocessedFilePath, tenantId from info.paper_itemizer")
                .resourceConn("intics_agadia_db_conn")
                .name("image-to-encoded-file")
                .outputTable("info.encode_info_details")
                .build();



        // Mock action and its context
        ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("copro.image-encoded-base.url", "http://localhost:10189/copro/denoise/");
        action.getContext().put("read.batch.size", "1");
        action.getContext().put("write.batch.size", "1");
        action.getContext().put("consumer.API.count", "1");

        // Create an instance of ImageToEncodedFileAction
        ImageToEncodedFileAction imageToEncodedFileAction = new ImageToEncodedFileAction(action, log,imageToEncodedFile);

        // Now you can use imageToEncodedFileAction for testing your action execution
        // You may want to use mocking frameworks like Mockito to mock dependencies or responses.

         imageToEncodedFileAction.execute();  // Uncomment this line when you want to execute the action.
    }
}
