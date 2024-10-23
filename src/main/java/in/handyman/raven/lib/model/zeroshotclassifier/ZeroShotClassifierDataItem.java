package in.handyman.raven.lib.model.zeroshotclassifier;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierDataItem {
    @JsonProperty("page_content")
    private String pageContent;
    @JsonProperty("entity_confidence_score")
    private List<ZeroShotClassifierDataEntityConfidenceScore> entityConfidenceScore;
    private String originId;
    private String groupId;
    private Integer paperNo;
    private String batchId;
    private Long tenantId;
    private String rootPipelineId;

}
