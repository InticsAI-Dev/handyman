package in.handyman.raven.lib.model.checkboxAttribution;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckboxAttributionLineItem {
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
    private Long groupId;
    private Long actionId;
    private Long tenantId;
    private String inputFilePath;
}
