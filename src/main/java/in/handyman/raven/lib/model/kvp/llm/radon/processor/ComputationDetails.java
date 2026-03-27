package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputationDetails {
    private BeforeMetricsData beforeMetricsData;
    private AfterMetricsData afterMetricsData;
    private Float duration;
    private JsonNode gpuTelemetry;
}
