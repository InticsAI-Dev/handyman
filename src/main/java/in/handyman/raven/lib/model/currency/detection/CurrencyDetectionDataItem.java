package in.handyman.raven.lib.model.currency.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyDetectionDataItem {
    private String filePath;
    private String classValue;
    private String asciiValue;
    private Double confidenceScore;

}
