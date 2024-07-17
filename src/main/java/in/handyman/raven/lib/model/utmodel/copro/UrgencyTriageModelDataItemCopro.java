package in.handyman.raven.lib.model.utmodel.copro;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageModelBoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrgencyTriageModelDataItemCopro {
    private String model;
    @JsonProperty("paper_type")
    private String paperType;
    @JsonProperty("confidence_score")
    private Float confidenceScore;
    private UrgencyTriageModelBoundingBox bboxes;
}
