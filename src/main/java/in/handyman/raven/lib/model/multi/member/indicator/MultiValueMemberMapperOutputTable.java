package in.handyman.raven.lib.model.multi.member.indicator;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiValueMemberMapperOutputTable implements CoproProcessor.Entity{

    private Long minScoreId;
    private String originId;
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


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.minScoreId, this.originId, this.paperNo, this.sorItemName, this.weightScore,
                this.predictedValue, this.bBox, this.confidenceScore, this.frequency, this.cummulativeScore,
                this.questionId, this.synonymId, this.tenantId, this.modelRegistry, this.rootPipelineId,
                this.batchId).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}