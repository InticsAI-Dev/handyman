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
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Integer paperNo;
    private Long actionId;
    private String processedFilePaths;

}

