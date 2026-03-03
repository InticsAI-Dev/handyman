package in.handyman.raven.lib.model.sorautomapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response from automatic field matching
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoMatchResponse {

    @JsonProperty("origin_id")
    private String originId;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("document_type")
    private String documentType;

    /**
     * High confidence matches (>80%) - auto-accepted
     */
    @JsonProperty("highConfidenceMatches")
    private List<FieldMatchResult> highConfidenceMatches;

    /**
     * Medium confidence matches (60-80%) - needs user validation
     */
    @JsonProperty("validationQueueMatches")
    private List<FieldMatchResult> validationQueueMatches;

    /**
     * Low confidence matches (<60%) - requires manual mapping
     */
    @JsonProperty("manualMappingNeeded")
    private List<FieldMatchResult> manualMappingNeeded;

    /**
     * True if there are medium-confidence matches awaiting validation
     */
    @JsonProperty("validationNeeded")
    private Boolean validationNeeded;

    @JsonProperty("status")
    private String status;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("durationTime")
    private Double durationTime;
}
