package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ErrorResponseJsonTriggerInputTable implements CoproProcessor.Entity {

    private String documentId;
    private String batchId;
    private String originId;
    private Long groupId;
    private Timestamp uploadedOn;
    private Timestamp processFailedOn;
    private Long totalProcessedDuration;
    private Integer totalNoOfPages;
    private String failedOnStage;
    private String stages;
    private String message;
    private Long rootPipelineId;
    private String transactionId;
    private Long tenantId;
    private String pipelineStatus;
    private String feature;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}