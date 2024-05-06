package in.handyman.raven.lib.model.trinitymodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TrinityModelPayload {
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private List<TrinityInputAttribute> attributes;
    private String paperType;
    private Long paperNo;
    private String originId;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String modelRegistry;
    private String qnCategory;
    private Long modelRegistryId;
}
