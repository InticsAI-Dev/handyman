package in.handyman.raven.lib.model.p2pNameValidation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class P2PNameValidationOutputTable implements CoproProcessor.Entity {

    private String p2pBBox;
    private Double p2pConfidenceScore;
    private String p2pConcatenatedName;
    private Integer groupId;
    private Double p2pMaximumScore;
    private String originId;
    private Integer paperNo;
    private Long rootPipelineId;
    private Integer tenantId;
    private String sorItemName;
    private Integer questionId;
    private Integer synonymId;
    private String modelRegistry;
    private String batchId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.p2pBBox, this.p2pConfidenceScore, this.p2pMaximumScore,
                this.p2pConcatenatedName, this.paperNo, this.rootPipelineId, this.tenantId, this.sorItemName,
                this.questionId, this.synonymId, this.modelRegistry, this.batchId).collect(Collectors.toList());
    }
}
