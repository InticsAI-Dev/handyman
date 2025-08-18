package in.handyman.raven.lib.model.neradaptors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NerAdapterPrediction {

    private String inputName;
    private Long predictionScore;
    private String predictionTag;
    private boolean predictedLabel;

}
