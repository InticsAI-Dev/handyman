package in.handyman.raven.lib.model.soritemhandling;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptionPolicy {
    private Long encryptionPolicyId;
    private String encryptionPolicy;
    private String status;
    private Integer version;
    private String description;
    private java.time.LocalDateTime createdOn;
    private Long createdUserId;
    private java.time.LocalDateTime lastUpdatedOn;
    private Long lastUpdatedUserId;
}
