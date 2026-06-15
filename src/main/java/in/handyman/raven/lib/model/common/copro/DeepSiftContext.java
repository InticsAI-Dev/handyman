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
public class DeepSiftContext {
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long processId;
    private String inputFilePath;
    private Integer paperNo;
    private String batchId;
    private Timestamp createdOn;
    private String modelName;
    private Integer modelId;
    private String sourceDocumentType;
    private String endpoint;
    private String dbJsonRequest;
    private Long actionId;
}
