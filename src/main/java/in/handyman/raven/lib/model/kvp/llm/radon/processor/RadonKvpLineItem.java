package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonKvpLineItem {
    private String model;
    @JsonProperty("infer_response")
    @JsonDeserialize(using = StringToMapDeserializer.class)
    private Map<String, Object> inferResponse;
    @JsonProperty("confidence_score")
    private Integer confidenceScore;
    private String bboxes;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long rootPipelineId;
    private Long groupId;
    private Long actionId;
    private Long tenantId;
    private String inputFilePath;
}