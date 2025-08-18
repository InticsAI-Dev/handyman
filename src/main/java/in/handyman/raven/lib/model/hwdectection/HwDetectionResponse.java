package in.handyman.raven.lib.model.hwdectection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HwDetectionResponse {
    @JsonProperty("model_name")
    private String modelName;
    @JsonProperty("model_version")
    private String modelVersion;
    private List<HwDetectionOutput> outputs;
}
