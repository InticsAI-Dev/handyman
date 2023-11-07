package in.handyman.raven.lib.model.ftpConnectionCheck;

import lombok.*;
import org.apache.http.annotation.Contract;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class FtpConnectionCheckOutputTable {


        private Long tenantId;
        private String type;
        private String info;
        private Long rootPipelineId;
        private String status;
        private String message;
        private LocalDateTime lastProcessedOn;
        private boolean ftpConnected;
        private LocalDateTime createdOn;
        private LocalDateTime lastUpdatedOn;
        private Long createdUserId;
        private Long lastUpdatedUserId;
        private Integer version;
        private String executionStatus;
}
