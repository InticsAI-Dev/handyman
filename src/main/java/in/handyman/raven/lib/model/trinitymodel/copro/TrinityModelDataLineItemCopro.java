package in.handyman.raven.lib.model.trinitymodel.copro;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrinityModelDataLineItemCopro {
    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private JsonNode bboxes;
}