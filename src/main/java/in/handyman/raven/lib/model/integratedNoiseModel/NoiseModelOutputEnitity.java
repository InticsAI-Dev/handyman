package in.handyman.raven.lib.model.integratedNoiseModel;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lib.CoproProcessor;
import jakarta.json.Json;
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

public class NoiseModelOutputEnitity implements CoproProcessor.Entity {
    private String fileId;
    private String originId;
    private Integer paperNo;
    private Integer processId;
    private Integer groupId;
    private Integer tenantId;
    private String inputFilePath;
    private Double consolidatedConfidenceScore;
    private String consolidatedClass;
    private String extractedValue;
    private String model1NoiseDetectionOutput;
    private String model2NoiseDetectionOutput;
    private String model3NoiseDetectionOutput;
    private String model4NoiseDetectionOutput;
    private LocalDateTime createdOn;
    private Long rootPipelineId;
    private String status;
    private String stage;
    private String message;
    @Override
    public List<Object> getRowData() {
        return Stream.of(this.fileId, this.originId, this.paperNo,
                this.processId, this.groupId, this.tenantId,this.inputFilePath, this.consolidatedConfidenceScore,
                this.consolidatedClass,this.extractedValue, this.model1NoiseDetectionOutput,
                this.model2NoiseDetectionOutput, this.model3NoiseDetectionOutput,
                this.model4NoiseDetectionOutput, this.createdOn,this.rootPipelineId,
                this.status,this.stage,this.message).collect(Collectors.toList());
    }
}