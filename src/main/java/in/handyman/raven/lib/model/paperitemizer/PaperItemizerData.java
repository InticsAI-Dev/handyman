package in.handyman.raven.lib.model.paperitemizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PaperItemizerData {
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String process;
    private String inputFilePath;
    private String outputDir;
    private Long actionId;
    private String batchId;
    private String base64Img;
}
