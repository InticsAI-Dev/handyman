package in.handyman.raven.lib.model.radonbbox.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.radonbbox.response.RadonResponseBboxLineItem;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonResponsReplicate {
    private String batchId;
    private String groupId;
    private String process;
    private Long actionId;
    private Long imageDPI;
    private String originId;
    private Long tenantId;
    private Long processId;
    private Long imageWidth;
    private Long imageHeight;
    private Long paperNumber;
    private String plottedPath;
    private String inputFilePath;
    private Long rootPipelineId;
    private String extractedImageUnit;
    private List<RadonResponseBboxLineItem> radonBboxLineItems;
}
