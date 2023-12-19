package in.handyman.raven.lib.model.hwdectection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HwDetectionDataItem {

 @JsonProperty("document_status")
 private String documentStatus;

 @JsonProperty("confidence_score")

 private Float confidenceScore;
 private String originId;
 private Long processId;
 private Integer groupId;
 private Long tenantId;
}
