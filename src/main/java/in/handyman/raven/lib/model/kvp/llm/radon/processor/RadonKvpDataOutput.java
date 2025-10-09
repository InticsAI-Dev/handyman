package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonKvpDataOutput {
    private String ProcessName;
    private String model;
    private String inferResponse;
    private String base64Img;
    private String originId;
    private String paperNo;
    private String processId;
    private String groupId;
    private String tenantId;
    private String rootPipelineId;
    private String actionId;
    private String batchId;
    private ComputationDetails computationDetails;
    private Integer statusCode;
    private String errorMessage;
    private String detail;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;
    private Integer sorContainerId;
    private Integer modelVersion;
    private UUID requestId;

}
