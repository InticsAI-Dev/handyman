package in.handyman.raven.lib.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ComparisonDataItem {
    private String sentence;
    private Double similarityPercent;
}
