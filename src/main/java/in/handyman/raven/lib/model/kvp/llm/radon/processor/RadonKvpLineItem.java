package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonKvpLineItem {
    private String model;
    @JsonProperty("inferResponse")
    private JsonNode inferResponse;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long rootPipelineId;
    private Long groupId;
    private Long actionId;
    private Long tenantId;
    private String inputFilePath;
    private String batchId;
    private String base64Img;
    @JsonProperty("imageDPI")
    private Long imageDPI;
    private Long imageWidth;
    private Long imageHeight;
    private String extractedImageUnit;

}
