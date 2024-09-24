package in.handyman.raven.lib.model.kvp.llm.neon.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NeonKvpLineItem {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String textModel;
    private String paperType;
    private String modelRegistry;
    private Long tenantId;
    private String originId;
    private Long groupId;
    private Integer paperNo;
    private Long processId;
    private String responseFormat;
    private Map<String, Object> response;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;
}
