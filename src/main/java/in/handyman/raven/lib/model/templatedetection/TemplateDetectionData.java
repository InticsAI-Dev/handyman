package in.handyman.raven.lib.model.templatedetection;

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
    private String originId;
    private Integer paperNo;
    private String inputFilePath;
    private List<String> attributes;
    private String paperType;

}
