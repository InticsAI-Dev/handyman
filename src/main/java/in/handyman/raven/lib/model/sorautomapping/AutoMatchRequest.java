package in.handyman.raven.lib.model.sorautomapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for automatic field matching to SOR items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoMatchRequest {

    @JsonProperty("origin_id")
    private String originId;

    @JsonProperty("tenant_id")
    private Long tenantId;

    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("root_pipeline_id")
    private Long rootPipelineId;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("sor_container_id")
    private Long sorContainerId;

    /**
     * Extracted fields to match
     */
    @JsonProperty("extracted_fields")
    private List<ExtractedField> extractedFields;

    /**
     * Target SOR items for this document type
     */
    @JsonProperty("target_sor_items")
    private List<TargetSorItem> targetSorItems;

    /**
     * Model name (optional)
     */
    @JsonProperty("model_name")
    private String modelName;
}
