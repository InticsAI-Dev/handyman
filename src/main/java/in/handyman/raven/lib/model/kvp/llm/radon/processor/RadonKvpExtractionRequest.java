package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
    private String userPrompt;  // Kept as String
    private String systemPrompt; // Kept as String
    private List<Map<String, Object>> transformationSystemPrompts; // Converted from JSON string
    private List<Map<String, Object>> transformationUserPrompts;  // Converted from JSON string
    private String kryptonInferenceMode;
    private String batchId;
    private String base64Img;
}


