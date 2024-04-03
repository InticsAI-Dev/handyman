package in.handyman.raven.lib.model.paperitemizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessAuditOutputTable {
    private Long tenantId;
    private String originId;
    private Long rootPipelineId;
    private String batchId;
    private String response;
    private String request;
    private String status;
    private String stage;
    private String message;
}
