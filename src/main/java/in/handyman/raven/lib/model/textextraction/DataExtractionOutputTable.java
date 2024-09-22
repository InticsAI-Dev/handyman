package in.handyman.raven.lib.model.textextraction;

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
public class DataExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private String extractedText;
    private String fileName;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private String isBlankPage;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String templateName;
    private String modelName;
    private String modelVersion;
    private String batchId;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId,this.templateId
                ,this.processId,this.filePath, this.extractedText,this.paperNo,this.fileName
                ,this.status,this.stage,this.message,this.isBlankPage,this.createdOn,this.rootPipelineId,this.templateName, this.modelName, this.modelVersion, this.batchId).collect(Collectors.toList());
    }
}
