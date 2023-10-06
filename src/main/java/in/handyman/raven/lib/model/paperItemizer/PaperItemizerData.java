package in.handyman.raven.lib.model.paperItemizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PaperItemizerData {
    private Long rootPipelineId;
    private String process;
    private String inputFilePath;
    private String outputDir;
    private Long actionId;
}
