package in.handyman.raven.lib.model.greyscaleconversion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GreyScaleConversionDataItem {
    private String processedFilePath;
    private String originId;
    private Integer paperNo;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long actionId;
    private Long rootPipelineId;
    private String process;
    private String inputFilePath;
    private String outputDir;
    private Boolean isColor;


}
