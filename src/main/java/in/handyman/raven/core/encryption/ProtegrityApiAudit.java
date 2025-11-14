package in.handyman.raven.core.encryption;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class ProtegrityApiAudit {
    private String key;
    private String encryptionType;
    private String endpoint;
    private Long rootPipelineId;
    private Long actionId;
    private String threadName;
    private String uuid;
    private String status;
    private String message;
    private Timestamp startedOn;
    private Timestamp completedOn;
}
