package in.handyman.raven.lib.model.currency.detection;

import in.handyman.raven.lib.CoproProcessor;
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
public class CurrencyDetectionOutputQuerySet implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String inputFilePath;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private String detectedValue;
    private String detectedAsciiValue;
    private Double confidenceScore;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId, this.templateId, this.processId,
                this.paperNo, this.status, this.stage, this.message, this.createdOn, this.rootPipelineId,
                this.modelName, this.modelVersion,this.inputFilePath,this.detectedValue,this.detectedAsciiValue,this.confidenceScore,this.batchId).collect(Collectors.toList());
    }
}
