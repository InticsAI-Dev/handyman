package in.handyman.raven.lib.custom.kvp.post.processing.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderMappingDetails {
    ObjectMapper objectMapper=new ObjectMapper();
    private String response;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageWidth;
    private Long imageHeight;
    private Timestamp createdOn;
    private String process;
    private String sorMetaDetail;
    private Long sorContainerId;
    private Map<String, Object> responseMap; // Parsed JSON as Map

    public Map<String, Object> getResponseMap() {
        if (responseMap == null && response != null) {
            try {
                responseMap = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error parsing response JSON to Map<String, String>", e);
            }
        }
        return responseMap;
    }

    public void setResponse(String response) {
        this.response = response;
        this.responseMap = null; // Reset parsed map when setting new response
    }
}
