package in.handyman.raven.lib.model.utmodel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UrgencyTriageReplicateDataLineItems {

    private Long actionId;
    @JsonProperty("confidence_score")
    private Float confidenceScore;
    private Integer groupId;
    @JsonProperty("infer_response")
    private String inferResponse;
    private String model;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long rootPipelineId;
    private Long tenantId;
    private UrgencyTriageModelBoundingBox bboxes;
    private String batchId;
    private String base64Img;
    private String extractedImageUnit;
    private Integer imageDPI;
    private Integer imageHeight;
    private Integer imageWidth;


}
