package in.handyman.raven.lib.model;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor

public class FtpConnectionCheckTable{
    private Long tenantId;
    private String type;
    private String info;
    private Long rootPipelineId;
    private String status;
    private String message;
    private LocalDateTime lastProcessedOn;
    private boolean ftpConnected;
    private LocalDateTime createdOn = LocalDateTime.now();
    private LocalDateTime lastUpdatedOn;
    private Long createdUserId;
    private Long lastUpdatedUserId;
    private Integer version = 1;
}


