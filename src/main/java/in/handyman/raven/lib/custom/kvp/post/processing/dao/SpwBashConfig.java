package in.handyman.raven.lib.custom.kvp.post.processing.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpwBashConfig {
    private Long tenantId;
    private Timestamp createdOn;
    private String createdUserId;
    private Timestamp lastUpdatedOn;
    private String lastUpdatedUserId;
    private String callerName;
    private String className;
    private String sourceCode;
    private Long referenceId;
    private Long version;
    private String status;

}
