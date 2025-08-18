package in.handyman.raven.lib.model.common.copro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComparisonDataItemCopro {
    private String sentence;
    private Double similarityPercent;
}
