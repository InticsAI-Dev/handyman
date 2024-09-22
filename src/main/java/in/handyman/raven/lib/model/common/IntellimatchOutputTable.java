package in.handyman.raven.lib.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntellimatchOutputTable implements CoproProcessor.Entity {
    private String fileName;
    private String originId;
    private Integer groupId;
    private Long tenantId;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String actualValue;
    private String extractedValue;
    private Integer confidenceScore;
    private Double intelliMatch;
    private String status;
    private String stage;
    private String message;
    private String modelName;
    private String modelVersion;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(
                this.fileName,
                this.originId,
                this.groupId,
                this.createdOn,
                this.rootPipelineId,
                this.actualValue,
                this.extractedValue,
                this.confidenceScore,
                this.intelliMatch,
                this.status,
                this.stage,
                this.message,
                this.modelName,
                this.modelVersion,
                this.tenantId,
                this.batchId

        ).collect(Collectors.toList());
    }
}



