package in.handyman.raven.lib.model.krypton.kvp;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.trinitymodel.TrinityModelOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KryptonKvpExtractionResponse {
    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;
    private List<KryptonKvpOutput> outputs;

}
