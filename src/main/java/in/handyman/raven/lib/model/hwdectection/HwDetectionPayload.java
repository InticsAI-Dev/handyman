package in.handyman.raven.lib.model.hwdectection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class HwDetectionPayload {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDir;
}
