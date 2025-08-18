package in.handyman.raven.lib.model.templatedetection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TemplateDetectionResponse {

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

        private List<TemplateDetectionOutput> outputs;
    }


