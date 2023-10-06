package in.handyman.raven.lib.model.utModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.TextExtraction.DataExtractionOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UrgencyTriageModelResponse {
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("model_version")
    private String modelVersion;
    private List<UrgencyTriageModelOutput> outputs;
}
