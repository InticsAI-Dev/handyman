package in.handyman.raven.lib.model.hwDectection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HwDetectionDataItem {
 private String document_status;
 private String confidence_score;

}
