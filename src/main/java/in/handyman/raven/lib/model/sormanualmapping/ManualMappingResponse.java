package in.handyman.raven.lib.model.sormanualmapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from manual mapping creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualMappingResponse {

    @JsonProperty("mappingId")
    private Long mappingId;

    @JsonProperty("patternUpdated")
    private Boolean patternUpdated;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;
}
