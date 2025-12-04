package in.handyman.raven.lib.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaProductionResponseAudit {
    private String documentId;
    private String checksum;
    private Long tenantId;
    private String originId;
    private String batchId;
    private String topicName;
    private String endpoint;
    private String authSecurityProtocol;
    private String saslMechanism;
    private String response;
    private Integer partition;
    private String execStatus;
    private String transactionId;
    private Long rootPipelineId;
    private Long createdUserId;
    private Long lastUpdatedUserId;
    private LocalDateTime createdOn;
    private LocalDateTime lastUpdatedOn;
    private String status;
    private Integer version;
    private Integer retryCount;
}
