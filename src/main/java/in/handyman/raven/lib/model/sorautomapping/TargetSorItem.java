package in.handyman.raven.lib.model.sorautomapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a target SOR item to match against
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TargetSorItem {

    /**
     * SOR item ID
     */
    @JsonProperty("sor_item_id")
    private Long sorItemId;

    /**
     * SOR item name (e.g., "Legal Name")
     */
    @JsonProperty("sor_item_name")
    private String sorItemName;

    /**
     * Field type: 'TEXT', 'DATE', 'NUMBER', etc.
     */
    @JsonProperty("field_type")
    private String fieldType;

    /**
     * Pre-configured known labels/synonyms
     */
    @JsonProperty("known_labels")
    private List<String> knownLabels;
}
