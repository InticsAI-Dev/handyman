package in.handyman.raven.lib.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaPublishQueryOutput {
    private String documentId;
    private String checksum;
    private String tenantId;
    private String originId;
    private String batchId;
    private String topicName;
    private String endpoint;
    private String authSecurityProtocol;
    private String saslMechanism;
    private String response;
    private int partition;
    private String execStatus;
    private String transactionId;
}
