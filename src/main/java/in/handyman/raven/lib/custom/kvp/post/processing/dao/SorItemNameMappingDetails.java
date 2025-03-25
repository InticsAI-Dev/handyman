package in.handyman.raven.lib.custom.kvp.post.processing.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SorItemNameMappingDetails {
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reusable mapper

    private String documentType;
    private int tenantId;
    private String sorItemMappingsJson; // JSON as String from DB

    private Map<String, String> sorItemMappings; // Parsed Map

    public Map<String, String> getSorItemMappings() {
        if (sorItemMappings == null && sorItemMappingsJson != null) {
            try {
                sorItemMappings = objectMapper.readValue(sorItemMappingsJson, new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parsing JSON to Map", e);
            }
        }
        return sorItemMappings;
    }

    public void setSorItemMappingsJson(String sorItemMappingsJson) {
        this.sorItemMappingsJson = sorItemMappingsJson;
    }
}
