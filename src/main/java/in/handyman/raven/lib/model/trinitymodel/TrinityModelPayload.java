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
    private List<String> attributes;
    private String paperType;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String modelRegistry;
}
