package in.handyman.raven.lib.model.zeroShotClassifier;

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
    private String originId;
    private String groupId;
    private Integer paperNo;
    @JsonProperty("page_content")
    private String pageContent;
    @JsonProperty("entity_confidence_score")
    private List<ZeroShotClassifierDataEntityConfidenceScore> entityConfidenceScore;

}
