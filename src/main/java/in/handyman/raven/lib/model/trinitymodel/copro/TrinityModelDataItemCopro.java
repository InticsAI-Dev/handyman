package in.handyman.raven.lib.model.trinitymodel.copro;

import in.handyman.raven.lib.model.trinitymodel.TrinityInputAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrinityModelDataItemCopro {
    private List<TrinityModelDataLineItemCopro> attributes;
    private Integer imageDPI;
    private Integer imageWidth;
    private Integer imageHeight;
    private String extractedImageUnit;

    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String paperType;
    private Long paperNo;
    private String originId;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String modelRegistry;
    private String sorItemName;


}
