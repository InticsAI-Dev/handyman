package in.handyman.raven.outbound.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthorizationIndicator {
    private String propValue;
    private String propName;
    private Integer page;
    private Double confidence;
    private JsonNode boundingBox;

}
