package in.handyman.raven.lib.model.trinitymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrinityModelResult {
    private String question;
    private String predictedAttributionValue;
    private JsonNode bboxes;
    private float scores;
}
