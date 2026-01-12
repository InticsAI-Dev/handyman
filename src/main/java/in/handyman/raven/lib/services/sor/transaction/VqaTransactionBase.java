package in.handyman.raven.lib.services.sor.transaction;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
