package in.handyman.raven.lib.services.sor.transform;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class VqaTransactionBase {
    private String transactionId;
    private LocalDateTime createdOn;
    private Long createdUserId;
    private LocalDateTime lastUpdatedOn;
    private Long lastUpdatedUserId;
    private Long rootPipelineId;
    private Long tenantId;
    private String documentId;
    private Integer groupId;
    private String batchId;
    private String originId;
    private Integer paperNo;
    private Integer truthId;
    private String status;
    private String stage;
    private String message;
    private Integer version;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private String sectionPriorityAfterFilter;

    public String buildLoggerBaseInput() {
        return "Root pipeline Id " + this.rootPipelineId +
                " | Batch Id " + this.batchId +
                " | Origin Id " + this.originId +
                " | Paper No " + this.paperNo;
    }
}
