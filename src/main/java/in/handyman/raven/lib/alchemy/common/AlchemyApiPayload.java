package in.handyman.raven.lib.alchemy.common;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlchemyApiPayload {
    private JsonNode payload;
    private boolean success;
    private LocalDateTime responseTimeStamp;
}
