package in.handyman.raven.lib.elv.dao;

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
public class ContainerEntityDetails {
    private static final ObjectMapper objectMapper = new ObjectMapper(); // Reusable ObjectMapper

    private String documentType;
    private int tenantId;
    private String sorContainerName;

    @ColumnName("truth_entity_names")
    private String truthEntityNamesJson; // JSON as String from DB

    private List<String> truthEntityNames; // Parsed List

    public List<String> getTruthEntityNames() {
        if (truthEntityNames == null && truthEntityNamesJson != null) {
            try {
                truthEntityNames = objectMapper.readValue(truthEntityNamesJson, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parsing JSON to List<String>", e);
            }
        }
        return truthEntityNames;
    }

    public void setTruthEntityNamesJson(String truthEntityNamesJson) {
        this.truthEntityNamesJson = truthEntityNamesJson;
    }
}
