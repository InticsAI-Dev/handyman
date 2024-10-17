package in.handyman.raven.lib.model.noiseModel;
import in.handyman.raven.lib.CoproProcessor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class NoiseModelOutputEntity implements CoproProcessor.Entity {
    private String originId;
    private Long paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private String inputFilePath;
    private Double consolidatedConfidenceScore;
    private String consolidatedClass;
    private String noiseModelsResult;
    private String hwNoiseDetectionOutput;
    private String checkNoiseDetectionOutput;
    private String checkboxMarkDetectionOutput;
    private String speckleNoiseDetectionOutput;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    private String modelName;
    private String modelVersion;
    private String batchId;
    private Timestamp lastUpdatedOn;
    private String request;
    private String response;
    private String endpoint;
    @Override
    public List<Object> getRowData() {
        return Stream.of( this.originId, this.paperNo,
                this.processId, this.groupId, this.tenantId,this.inputFilePath, this.consolidatedConfidenceScore,
                this.consolidatedClass,this.noiseModelsResult, this.hwNoiseDetectionOutput,
                this.checkNoiseDetectionOutput, this.checkboxMarkDetectionOutput,
                this.speckleNoiseDetectionOutput, this.createdOn,this.rootPipelineId,
                this.status,this.stage,this.message,this.modelName,this.modelVersion,this.batchId,
                this.lastUpdatedOn, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }
}