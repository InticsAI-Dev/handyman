package in.handyman.raven.lib.model.common.copro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgenticFilterContext {
    private String originId;
    private Long groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private Integer paperNo;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp createdOn;
    private String templateName;
    private String modelName;
    private String promptType;
    private String modelVersion;
    private String endpoint;
    private String uniqueName;
    private Integer uniqueId;
}
