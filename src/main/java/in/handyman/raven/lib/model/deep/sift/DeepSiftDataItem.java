package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeepSiftDataItem {
    private String model;
    @JsonProperty("infer_response")
    private String inferResponse;
    @JsonProperty("confidence_score")
    private Integer confidenceScore;
    private String bboxes;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long rootPipelineId;
    private Integer groupId; // Changed to Integer to match entity.getGroupId()
    private Long actionId;
    private Long tenantId;
    private String inputFilePath;
    private String batchId;
    private String base64Img;
    @JsonProperty("imageDPI")
    private Long imageDPI;
    private Long imageWidth;
    private Long imageHeight;
    private String extractedImageUnit;
    private String processName;
    private String templateName;
    private String pageContent;
}