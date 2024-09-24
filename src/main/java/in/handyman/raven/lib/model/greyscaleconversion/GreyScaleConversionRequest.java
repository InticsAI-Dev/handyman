package in.handyman.raven.lib.model.greyscaleconversion;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GreyScaleConversionRequest {
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Integer paperNo;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String outputDir;

}
