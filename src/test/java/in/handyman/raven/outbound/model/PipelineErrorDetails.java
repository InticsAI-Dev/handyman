package in.handyman.raven.outbound.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineErrorDetails {
    private Long id;

    private Long tenantId;
    private String transactionId;
    private String originId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private String errorMessageDetail;
    private String failureStage;
    private LocalDateTime failureTimestamp;
    private LocalDateTime createdOn;
}