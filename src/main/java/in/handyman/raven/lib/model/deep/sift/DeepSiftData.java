package in.handyman.raven.lib.model.deep.sift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeepSiftData {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String originId;
    private Integer paperNumber;
    private Long processId;
    private String inputFilePath;
    private Integer groupId;
    private Long tenantId;
    private String templateName;
    private String batchId;
    private String base64Img;
    private String model;
}
