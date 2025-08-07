package in.handyman.raven.lib;

import com.anthem.acma.commonclient.storecontent.dto.StoreContentResponseDto;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.documentEyeCue.DocumentEyeCueInputTable;
import in.handyman.raven.lib.model.documentEyeCue.StoreContent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadStoreContentAction {

    private final ActionExecutionAudit action;
    private final DocumentEyeCueInputTable entity;
    private final String filePath;

    public UploadStoreContentAction(ActionExecutionAudit action, DocumentEyeCueInputTable entity, String filePath) {
        this.action = action;
        this.entity = entity;
        this.filePath = filePath;
    }

    public void execute() {
        try {
            String repository = action.getContext().get("doc.eyecue.storecontent.repository");
            String applicationId = action.getContext().get("doc.eyecue.storecontent.application.id");

            StoreContent storeContent = new StoreContent();
            StoreContentResponseDto response = storeContent.execute(
                    filePath,
                    repository,
                    applicationId,
                    entity,
                    action
            );

            log.info("StoreContent upload done for document_id: {} | contentId: {}",
                    entity.getDocumentId(), response.getContentID());
        } catch (Exception e) {
            log.error("StoreContent upload failed for document_id {}: {}",
                    entity.getDocumentId(), e.getMessage(), e);
        }
    }
}