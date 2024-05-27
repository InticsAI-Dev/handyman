package in.handyman.raven.lib.model.krypton.kvp;

import in.handyman.raven.lib.model.paragraph.detection.ParagraphExtractionLineItems;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KryptonKvpExtractionRequest {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String prompt;
    private String textModel;
    private String paperType;
    private String modelRegistry;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String responseFormat;
}
