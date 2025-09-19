package in.handyman.raven.lib.model.documentEyeCue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentEyeCueResponse {
    private String originId;
    private String batchId;
    private String processId;
    private String groupId;
    private int tenantId;
    private int rootPipelineId;
    private String process;
    private Integer actionId;
    private String status;
    private String processedPdfPath;
    private String processedPdfBase64;
    private String processedPdfChecksum;
    private Long docEyeCueDurationMs;
    private String errorMessage;
}
