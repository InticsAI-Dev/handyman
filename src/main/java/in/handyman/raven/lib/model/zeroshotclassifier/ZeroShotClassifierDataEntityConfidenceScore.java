package in.handyman.raven.lib.model.zeroshotclassifier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder



public class ZeroShotClassifierDataEntityConfidenceScore {
    private String key;
    private double score;
    private String truthEntity;

}
