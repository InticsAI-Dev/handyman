package in.handyman.raven.lambda.doa.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HandymanExceptionAuditDetails {
    private Integer groupId;
    private Long rootPipelineId;
    private String rootPipelineName;
    private String pipelineName;
    private Long actionId;
    private String actionName;
    private String exceptionInfo;
    private String message;
    private Long processId;
    private Long createdBy;
    private LocalDateTime createdDate;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}