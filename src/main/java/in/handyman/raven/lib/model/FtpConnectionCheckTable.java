package in.handyman.raven.lib.model;

import lombok.*;

import java.time.LocalDateTime;
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FtpConnectionCheckTable {

        private Long id;
        private Long tenantId;
        private Long rootPipelineId;
        private LocalDateTime createdDate;
        private Long createdBy;
        private Long lastModifiedBy;
        private LocalDateTime lastModifiedDate;
        private String status;
        private String message;
        private String type;
        private String info;
        private LocalDateTime lastProcessedOn;
        private boolean ftpConnected;


}
