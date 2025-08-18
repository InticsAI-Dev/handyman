package in.handyman.raven.lib.model.radonbbox.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RadonBboxRequest {
    private String originId;
    private Long paperNumber;
    private String inputFilePath;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String outputDir;
    private Long actionId;
    private String process;
    private String batchId;
    private String base64Img;
    private List<RadonBboxRequestLineItem> radonBboxLineItems;

}
