package in.handyman.raven.lib.custom.kvp.post.processing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SorItemNameMappingDetailsDto {
    private String documentType;
    private int tenantId;
    private Map<String, String> sorItemMappings; // Using Map for jsonb_object_agg output

}
