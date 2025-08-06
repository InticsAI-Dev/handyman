package in.handyman.raven.lib.model.multi.member.indicator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class extractedSorItemList {
    private Long minScoreId;
    private Long paperNo;
    private String sorItemName;
    private Long weightScore;
    private String predictedValue;
    private String bBox;
    private Long confidenceScore;
    private Long frequency;
    private Long cummulativeScore;
    private Long questionId;
    private Long synonymId;
    private Long tenantId;
    private String modelRegistry;
    private Long rootPipelineId;
    private String batchId;
}
