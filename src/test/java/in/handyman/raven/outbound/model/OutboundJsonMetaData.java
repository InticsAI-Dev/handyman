package in.handyman.raven.outbound.model;

import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    private Long processingTimeMs;
    private LocalDateTime processedAt;
    private Integer pageCount;
    private List<Integer> candidatePaper;
    private Integer overallConfidence;
}
