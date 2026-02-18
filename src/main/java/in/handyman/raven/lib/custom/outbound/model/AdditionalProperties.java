package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdditionalProperties {
    private String propValue;
        private String propName;
        private Integer page;
        private Double confidence;
        private JsonNode boundingBox;

}
