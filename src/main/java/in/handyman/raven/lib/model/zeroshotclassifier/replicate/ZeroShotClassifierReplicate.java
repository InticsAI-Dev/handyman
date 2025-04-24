package in.handyman.raven.lib.model.zeroshotclassifier.replicate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ZeroShotClassifierReplicate {

    private Long rootPipelineId;
    private String actionId;
    private String processId;
    private String process;
    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private String keysToFilter;
    private String pageContent;
    private String batchId;

}
