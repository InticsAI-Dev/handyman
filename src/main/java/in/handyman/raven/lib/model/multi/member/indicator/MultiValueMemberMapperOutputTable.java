package in.handyman.raven.lib.model.multi.member.indicator;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MultiValueMemberMapperOutputTable implements CoproProcessor.Entity{

    private LocalDateTime createdOn;
    private Long createdUserId;
    private LocalDateTime lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String status;
    private Integer version;
    private Long frequency;
    private String bBox;
    private Long confidenceScore;
    private String extractedValue;
    private Long filterScore;
    private Long groupId;
    private Long maximumScore;
    private String originId;
    private Long paperNo;
    private Long questionId;
    private Long rootPipelineId;
    private String sorItemName;
    private Long synonymId;
    private Long tenantId;
    private String modelRegistry;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdOn, this.createdUserId, this.lastUpdatedOn, this.lastUpdatedUserId, this.status, this.version, this.frequency, this.bBox, this.confidenceScore,
                this.extractedValue, this.filterScore, this.groupId, this.maximumScore, this.originId, this.paperNo, this.questionId, this.rootPipelineId,
                this.sorItemName, this.synonymId, this.tenantId, this.modelRegistry, this.batchId).collect(Collectors.toList());
    }
}