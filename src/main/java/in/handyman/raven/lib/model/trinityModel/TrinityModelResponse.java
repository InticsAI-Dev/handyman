package in.handyman.raven.lib.model.trinityModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.autoRotation.AutoRotationOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrinityModelResponse {

        @JsonProperty("model_name")
        private String modelName;

        @JsonProperty("model_version")
        private String modelVersion;
        private List<TrinityModelOutput> outputs;


}
