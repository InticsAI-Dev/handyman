package in.handyman.raven.lib.model.trinityModel;

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
    private String paperType;
    private List<String> attributes;
}
