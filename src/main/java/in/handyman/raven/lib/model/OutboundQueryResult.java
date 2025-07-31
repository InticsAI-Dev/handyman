package in.handyman.raven.lib.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboundQueryResult {
    private String predictedValue;
    private int paperNo;
    private String itemName;
    private double precision;
    private double x;
    private double width;
    private double y;
    private double height;
    private String fileExtension;
    private String transactionId;
    private String fileName;
    private LocalDateTime transactionCreatedOn;
    private LocalDateTime sourceCreatedOn;
    private String requestTxnId;
    private String uploadReqAck;
    private String documentId;
    private String originId;
    private Long tenantId;
    private Long rootPipelineId;
    private String groupId;
    private String batchId;
    private String apsaIsCandidatePaper;
    private String lineItemType;
    private String documentType;
}

