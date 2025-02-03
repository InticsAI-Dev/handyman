package in.handyman.raven.lib.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KafkaPublishQueryInput {

    private String endpoint;
    private String topicName;
    private String authSecurityProtocol;
    private String saslMechanism;
    private String userName;
    private String password;
    private String encryptionType;
    private String encryptionKey;
    private String jsonData;
    private String documentId;
    private String fileChecksum;
    private String originId;
    private String batchId;
    private Long tenantId;
    private String transactionId;

}
