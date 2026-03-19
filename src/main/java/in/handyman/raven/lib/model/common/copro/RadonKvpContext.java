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
public class RadonKvpContext {
    private String originId;
    private Integer paperNo;
    private Long groupId;
    private Long processId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String batchId;
    private String modelRegistry;
    private String category;
    private String apiName;
    private Long sorContainerId;
    private String sorContainerName;
    private Timestamp createdOn;
    private String endpoint;
}
