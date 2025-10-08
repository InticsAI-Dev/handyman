package in.handyman.raven.lib.model.kvp.llm.jsonparser;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LlmJsonQueryOutputTable implements CoproProcessor.Entity {
    private String createdOn;
    private Long tenantId;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private double confidenceScore;
    private String sorItemName;
    private String answer;
    private String boundingBox;
    private Integer paperNo;
    private String originId;
    private Long groupId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String extractedImageUnit;
    private Long imageDpi;
    private Long imageHeight;
    private Long imageWidth;
    private Long sorContainerId;
    private String sorItemLabel;
    private String sectionAlias;
    private String bBoxAsIs;
    private boolean isLabelMatching;
    private String labelMatchMessage;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdOn, this.tenantId, this.createdUserId, this.lastUpdatedOn, this.lastUpdatedUserId,
                this.confidenceScore, this.sorItemName, this.answer, this.boundingBox, this.paperNo,
                this.originId, this.groupId, this.rootPipelineId, this.batchId, this.modelRegistry,
                this.extractedImageUnit, this.imageDpi, this.imageHeight, this.imageWidth,
                this.sorContainerId, this.sorItemLabel, this.sectionAlias, this.bBoxAsIs, this.isLabelMatching,this.labelMatchMessage).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}
