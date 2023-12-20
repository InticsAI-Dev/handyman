package in.handyman.raven.lib.model.NoiseModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoiseModelResponse {

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

    private List<PaperItemizerOutput> outputs;
}
