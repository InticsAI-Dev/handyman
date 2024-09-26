package in.handyman.raven.lib.model.hwdectection;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HwClassificationOutputTable implements CoproProcessor.Entity{

    private String createdUserId;
    private String lastUpdatedUserId;
    private Long tenantId;
    private String originId;
    private Integer groupId;
    private Long processId;
    private Integer paperNo;
    private String templateId;
    private Long modelId;
    private String documentType;
    private Float confidenceScore;
    private String status;
    private String stage;
    private String message;
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private String batchId;
    private Timestamp createdOn;
    private Timestamp lastUpdatedOn;



    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdOn, this.createdUserId,this.lastUpdatedOn,this.lastUpdatedUserId, this.tenantId,
                this.originId, this.paperNo, this.templateId, this.modelId,
                this.documentType, this.status, this.stage, this.message, this.groupId,this.rootPipelineId, this.confidenceScore, this.modelName,
                this.modelVersion,this.batchId).collect(Collectors.toList());

    }
}

