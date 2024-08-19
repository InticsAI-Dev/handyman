package in.handyman.raven.lib.model.urgencyTriageBeta;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.utmodel.UrgencyTriageModelBoundingBox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BetaUrgencyModelItems {
    private String model;
    @JsonProperty("paper_type")
    private String paperType;
    @JsonProperty("confidence_score")
    private Float confidenceScore;
    @JsonProperty("bboxes")
    private UrgencyTriageModelBoundingBox boundingBox;
}