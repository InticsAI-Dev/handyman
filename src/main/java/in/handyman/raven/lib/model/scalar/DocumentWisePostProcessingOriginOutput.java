package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Output entity for CoproProcessor that represents a single row to insert
 * Implements CoproProcessor.Entity for use with CoproProcessor
 * getRowData() returns values in the order matching the INSERT SQL columns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWisePostProcessingOriginOutput implements CoproProcessor.Entity {
    
    private DocumentWisePostProcessingInput input;
    private Long defaultCreatedUserId;
    
    @Override
    public List<Object> getRowData() {
        List<Object> rowData = new ArrayList<>();

        rowData.add(input.getTransactionId());
        rowData.add(input.getCreatedOn() != null ? input.getCreatedOn() : LocalDateTime.now());
        rowData.add(input.getCreatedUserId() != null ? input.getCreatedUserId() : defaultCreatedUserId);
        rowData.add(input.getLastUpdatedOn() != null ? input.getLastUpdatedOn() : LocalDateTime.now());
        rowData.add(input.getLastUpdatedUserId() != null ? input.getLastUpdatedUserId() : defaultCreatedUserId);
        rowData.add(input.getStatus() != null ? input.getStatus() : "ACTIVE");
        rowData.add(input.getVersion());
        rowData.add(input.getFeature());
        rowData.add(input.getLabel());
        rowData.add(input.getLeftPos());
        rowData.add(input.getLowerPos());
        rowData.add(input.getRightPos());
        rowData.add(input.getUpperPos());
        rowData.add(input.getBBox());
        rowData.add(input.getPrecision() != null ? String.valueOf(input.getPrecision()) : null);
        rowData.add(input.getPredictedValue() != null ? input.getPredictedValue() : "");
        rowData.add(input.getSectionAlias());
        rowData.add(input.getSorContainerInstance());
        rowData.add(input.getDocumentId());
        rowData.add(input.getTruthId());
        rowData.add(input.getChannelId());
        rowData.add(input.getGroupId());
        rowData.add(input.getOriginId());
        rowData.add(input.getPaperNo());
        rowData.add(input.getQuestionId());
        rowData.add(input.getRootPipelineId());
        rowData.add(input.getScore());
        rowData.add(input.getSorItemName());
        rowData.add(input.getSorQuestion());
        rowData.add(input.getSynonymId());
        rowData.add(input.getTenantId());
        rowData.add(input.getVqaScore());
        rowData.add(input.getCategory());
        rowData.add(input.getStage());
        rowData.add(input.getBatchId());
        rowData.add(input.getLineItemType());
        rowData.add(input.getIsEncrypted() != null ? input.getIsEncrypted() : false);
        rowData.add(input.getEncryptionPolicy());
        rowData.add(input.getIsRemovedAfterFiltering());
        rowData.add(input.getMessage());
        rowData.add(input.getSorContainerId());
        rowData.add(input.getTruthEntityId());
        rowData.add(input.getSorItemId());
        rowData.add(input.getIsMultiEntityEnabled());
        
        return rowData;
    }
    
    @Override
    public String getStatus() {
        return input != null ? "SUCCESS" : "FAILED";
    }
}
