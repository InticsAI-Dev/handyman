package in.handyman.raven.lib.custom.outbound.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboundJsonMetaData {
    private String documentType;
    private String documentExtension;
    private String transactionId;
    private String inboundDocumentName;
    private String processStartTime;
    private String processEndTime;
    private Long processingTimeMs;
    private String processedAt;
    private Integer pageCount;
    private List<Integer> candidatePaper;
    private Integer overallConfidence;
}
