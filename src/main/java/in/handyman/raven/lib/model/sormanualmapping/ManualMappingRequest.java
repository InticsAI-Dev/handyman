package in.handyman.raven.lib.model.sormanualmapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for creating manual mapping (user selection from SOT tab)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualMappingRequest {

    @JsonProperty("origin_id")
    private String originId;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("sor_container_id")
    private Long sorContainerId;

    @JsonProperty("sor_item_id")
    private Long sorItemId;

    @JsonProperty("sor_item_name")
    private String sorItemName;

    /**
     * Source field information
     */
    @JsonProperty("source_type")
    private String sourceType; // 'KVP', 'TABLE', 'CHECKBOX'

    @JsonProperty("source_label")
    private String sourceLabel;

    @JsonProperty("source_value")
    private String sourceValue;

    @JsonProperty("page_number")
    private Integer pageNumber;

    @JsonProperty("section_hint")
    private String sectionHint;

    /**
     * User who created this mapping
     */
    @JsonProperty("created_by_user_id")
    private Long createdByUserId;
}
