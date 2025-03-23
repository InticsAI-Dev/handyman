package in.handyman.raven.lib.custom.kvp.post.processing.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerItemsDetails {
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reusable ObjectMapper

    private String documentType;
    private int tenantId;
    private String sorContainerName;
    private String sorItemNamesJson; // JSON as String from DB

    private List<String> sorItemNames; // Parsed List

    public List<String> getSorItemNames() {
        if (sorItemNames == null && sorItemNamesJson != null) {
            try {
                sorItemNames = objectMapper.readValue(sorItemNamesJson, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parsing JSON to List<String>", e);
            }
        }
        return sorItemNames;
    }

    public void setSorItemNamesJson(String sorItemNamesJson) {
        this.sorItemNamesJson = sorItemNamesJson;
    }
}
