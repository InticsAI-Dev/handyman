package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComputationDetails {
    private BeforeMetricsData beforeMetricsData;
    private AfterMetricsData afterMetricsData;
    private Float duration;
}
