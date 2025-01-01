package in.handyman.raven.lib.alchemy.common;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlchemyApiPayload {
    private JsonNode payload;
    private boolean success;
    private Integer errorCode;
    private String errorMsg;
    private LocalDateTime responseTimeStamp;
}
