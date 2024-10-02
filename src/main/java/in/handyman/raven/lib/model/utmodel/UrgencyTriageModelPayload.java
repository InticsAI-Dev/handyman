package in.handyman.raven.lib.model.utmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UrgencyTriageModelPayload {
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDir;
    private String batchId;
    private String prompt;
}
