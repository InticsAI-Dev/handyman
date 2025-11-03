package in.handyman.raven.lib.model.paperitemizer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperItemizerDataItem {
    @JsonProperty("itemizedPath")
    private String itemizedPapers;
    private Long paperNumber;
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String batchId;
    private String rootPipelineId;
    private String base64Img;
    private String processName;
    private Integer actionId;
}



