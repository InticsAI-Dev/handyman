package in.handyman.raven.lib.model.radonbbox.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonBboxResponseData {
    private String originId;
    private Long paperNumber;
    private String inputFilePath;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String plottedPath;
    private Long actionId;
    private String process;
    private String batchId;
    private String base64Img;
    private List<RadonResponseBboxLineItem> radonBboxLineItems;


}
