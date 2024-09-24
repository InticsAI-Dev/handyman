package in.handyman.raven.lib.model.currency.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CurrencyDetectionRequest {

    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
}
