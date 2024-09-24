package in.handyman.raven.lib.model.kvp.llm.neon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NeonKvpExtractionRequest {
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
