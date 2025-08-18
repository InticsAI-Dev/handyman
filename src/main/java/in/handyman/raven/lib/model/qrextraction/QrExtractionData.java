package in.handyman.raven.lib.model.qrextraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrExtractionData {
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long paperNo;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDir;
    private String batchId;
}
