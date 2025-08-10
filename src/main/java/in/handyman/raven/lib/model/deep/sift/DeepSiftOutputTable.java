package in.handyman.raven.lib.model.deep.sift;

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
public class DeepSiftOutputTable implements CoproProcessor.Entity {

    private Long id;
    private String originId;
    private Integer groupId;
    private String inputFilePath;
    private Timestamp createdOn;
    private String createdBy;
    private Long rootPipelineId;
    private Long tenantId;
    private String batchId;
    private String extractedText;
    private Integer paperNo;
    private String sourceDocumentType;
    private Integer modelId;
    private String modelName;
    private String status;

    @Override
    public List<Object> getRowData() {
        return Stream.of(
                this.originId,
                this.groupId,
                this.inputFilePath,
                this.createdOn,
                this.createdBy != null ? this.createdBy : "-1",
                this.rootPipelineId,
                this.tenantId,
                this.batchId,
                this.extractedText,
                this.paperNo,
                this.sourceDocumentType,
                this.modelId,
                this.modelName
        ).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return this.status != null ? this.status : "ABSENT";
    }
}