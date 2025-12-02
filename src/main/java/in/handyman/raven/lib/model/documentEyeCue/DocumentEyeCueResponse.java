package in.handyman.raven.lib.model.documentEyeCue;

import in.handyman.raven.lib.model.kvp.llm.radon.processor.ComputationDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    private ComputationDetails metricsData;
    private Integer statusCode;
    private String detail;
    private UUID requestId;
}
