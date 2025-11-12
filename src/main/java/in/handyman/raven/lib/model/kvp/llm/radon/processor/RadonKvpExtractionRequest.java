package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonKvpExtractionRequest {
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String userPrompt;
    private String systemPrompt;
    private String batchId;
    private Long sorContainerId;
    private String base64Img;
    private String modelName;
    private UUID requestId;
}
