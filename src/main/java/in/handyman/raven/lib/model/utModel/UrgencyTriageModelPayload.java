package in.handyman.raven.lib.model.utModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UrgencyTriageModelPayload {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDir;
}
