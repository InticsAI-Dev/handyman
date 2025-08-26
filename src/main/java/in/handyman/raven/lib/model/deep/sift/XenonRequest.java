package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XenonRequest {
    private String originId;
    private String batchId;
    private Integer paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String process;
    private String modelName;
    private Long actionId;
    private String inputFilePath;

    @JsonProperty("base64Image")
    private String base64Img;
}
