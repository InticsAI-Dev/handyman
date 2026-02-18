package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtractedField {
    private String value;
    private Integer page;
    private Integer confidence;
    private JsonNode boundingBox;
}
