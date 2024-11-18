package in.handyman.raven.lib.model.checkboxAttribution;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpOutput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckboxAttributionResponse {
    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;
    private List<RadonKvpOutput> outputs;

}