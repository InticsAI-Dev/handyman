package in.handyman.raven.lib.model.sorautomapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of matching a single extracted field to SOR item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldMatchResult {

    @JsonProperty("extracted_label")
    private String extractedLabel;

    @JsonProperty("extracted_value")
    private String extractedValue;

    @JsonProperty("matched_sor_item_id")
    private Long matchedSorItemId;

    @JsonProperty("matched_sor_item_name")
    private String matchedSorItemName;

    /**
     * Confidence score (0-100)
     */
    @JsonProperty("confidence")
    private Double confidence;

    /**
     * Match method: 'EXACT_MATCH', 'LEARNED_PATTERN', 'FUZZY_MATCH', 'LLM_SEMANTIC', 'NO_MATCH'
     */
    @JsonProperty("match_method")
    private String matchMethod;

    /**
     * LLM's explanation (if LLM was used)
     */
    @JsonProperty("llm_reasoning")
    private String llmReasoning;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("page_number")
    private Integer pageNumber;
}
