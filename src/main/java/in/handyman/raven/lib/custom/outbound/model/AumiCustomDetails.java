package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AumiCustomDetails {
    private JsonNode customResponse;
}
