package in.handyman.raven.lib.model.urgencyTriageBeta;

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
public class UrgencyTriageBetaOutputTable implements CoproProcessor.Entity {

    private String createdUserId;
    private String lastUpdatedUserId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private Long modelId;
    private String status;
    private String stage;
    private String message;
    private Long rootPipelineId;
    private String modelVersion;
    private String batchId;

    private String checkboxPaperType;
    private String handwrittenPaperType;
    private String binaryPaperType;
    private Float checkboxScore;
    private Float handwrittenScore;
    private Float binaryScore;
    private String checkboxBBox;
    private String handwrittenBBox;
    private String binaryBBox;
    private String modelName;
    private String request;
    private String response;
    private String endpoint;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdUserId, this.lastUpdatedUserId, this.processId, this.groupId, this.tenantId,
                this.originId, this.paperNo, this.templateId, this.modelId, this.status, this.stage, this.message,
                this.rootPipelineId, this.modelVersion, this.batchId, this.checkboxPaperType, this.handwrittenPaperType,
                this.binaryPaperType, this.checkboxBBox, this.handwrittenBBox, this.binaryBBox, this.checkboxScore,
                this.handwrittenScore, this.binaryScore, this.modelName, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }
}