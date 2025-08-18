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
public class UrgencyTriageModelDataItem {
    private String model;
    @JsonProperty("paper_type")
    private String paperType;
    @JsonProperty("confidence_score")
    private Float confidenceScore;
    private UrgencyTriageModelBoundingBox bboxes;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String batchId;
}

