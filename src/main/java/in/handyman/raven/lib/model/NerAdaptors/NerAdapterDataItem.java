package in.handyman.raven.lib.model.NerAdaptors;

import in.handyman.raven.lib.model.zeroShotClassifier.ZeroShotClassifierDataEntityConfidenceScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NerAdapterDataItem {
    private List<NerAdapterPrediction> prediction;

}

