package in.handyman.raven.lib.model.qrExtraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrExtractionData {
    private String rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;

}
