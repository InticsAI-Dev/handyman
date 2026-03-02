package in.handyman.raven.lib.custom.outbound.dao;


import lombok.*;

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
    private String processStartTime;
    private String processEndTime;
    private String processedAt;
    private List<Integer> candidatePapers;
    private String errorMessage;
    private String errorMessageDetail;
    private Integer errorCode;
}
