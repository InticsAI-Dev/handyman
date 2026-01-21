package in.handyman.raven.outbound.dao;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// Metadata context class
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetadataContext {
    private String requestTxnId;
    private String documentId;
    private String inboundTransactionId;
    private String transactionId;
    private String inboundDocumentName;
    private String documentExtension;
    private String documentType;
    private String uploadStatus;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    private LocalDateTime processedAt;
    private List<Integer> candidatePapers;
    private String errorMessage;
    private String errorMessageDetail;
    private Integer errorCode;
}
