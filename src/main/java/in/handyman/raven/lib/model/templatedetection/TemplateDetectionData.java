package in.handyman.raven.lib.model.templatedetection;

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
public class TemplateDetectionData {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private List<TrinityInputAttribute> attributes;
    private String paperType;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String modelRegistry;
    private String qnCategory;

}
