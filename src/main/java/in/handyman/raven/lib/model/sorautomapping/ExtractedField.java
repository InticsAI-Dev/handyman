package in.handyman.raven.lib.model.sorautomapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single extracted field from KVP/Table/Checkbox
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedField {

    /**
     * Extracted label (e.g., "Company Legal Name")
     */
    @JsonProperty("label")
    private String label;

    /**
     * Extracted value (e.g., "ABC Corporation")
     */
    @JsonProperty("value")
    private String value;

    /**
     * Source type: 'KVP', 'TABLE', 'CHECKBOX'
     */
    @JsonProperty("source_type")
    private String sourceType;

    /**
     * Page number where field was found
     */
    @JsonProperty("page_number")
    private Integer pageNumber;

    /**
     * Confidence score from extraction (0-100)
     */
    @JsonProperty("confidence")
    private Double confidence;

    /**
     * Bounding box coordinates (if available)
     */
    @JsonProperty("bbox")
    private Object bbox;
}
