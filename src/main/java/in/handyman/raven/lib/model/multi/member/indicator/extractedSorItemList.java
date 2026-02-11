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
    private Long paperNo;
    private Long groupId;
    private String sorItemName;
    private String predictedValue;
    private String bBox;
    private Long confidenceScore;
    private Long frequency;
    private Long questionId;
    private Long synonymId;
    private Long tenantId;
    private String modelRegistry;
    private Long rootPipelineId;
    private String batchId;
    private String documentType;
}