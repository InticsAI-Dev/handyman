package in.handyman.raven.lib.model.autorotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoRotationDataItem {
    private String processedFilePaths;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long actionId;
    private Long rootPipelineId;
    private String batchId;
    private String base64Img;
}

