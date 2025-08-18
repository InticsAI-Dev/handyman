package in.handyman.raven.lib.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ComparisonDataItem {
    private String sentence;
    private String inputSentence;
    private Double similarityPercent;
    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Long actionId;
    private String process;
    private Long rootPipelineId;
    private String batchId;
}
