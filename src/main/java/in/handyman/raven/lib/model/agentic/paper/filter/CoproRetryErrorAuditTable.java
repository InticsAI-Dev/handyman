package in.handyman.raven.lib.model.agentic.paper.filter;

import in.handyman.raven.lambda.doa.Auditable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CoproRetryErrorAuditTable extends Auditable {
    private Long rootPipelineId;
    private String batchId;
    private Integer groupId;
    private String originId;
    private Long processId;
    private String templateId;
    private Integer paperNo;
    private String fileName;
    private int attemptInitiated;
    private String filePath;
    private String sorContainerId;
    private String sorItemId;
    private String status;
    private String stage;
    private String message;
    private String request;
    private String response;
    private String endpoint;
    private Long tenantId;
}
