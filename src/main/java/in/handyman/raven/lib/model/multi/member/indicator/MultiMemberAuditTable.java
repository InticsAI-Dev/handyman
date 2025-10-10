package in.handyman.raven.lib.model.multi.member.indicator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiMemberAuditTable {
    private Long rootPipelineId;
    private LocalDateTime createdOn = LocalDateTime.now();
    private LocalDateTime lastUpdatedOn;
    private Long createdUserId = -1L;
    private Long lastUpdatedUserId = -1L;
    private String batchId;
    private String originId;
    private Long groupId;
    private String documentType;
    private Long tenantId;
    private String paperNo;
    private String sorItemName;
    private String indicatorType;
    private String comments;
}