package in.handyman.raven.lib.model.greyscaleconversion;

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
public class GreyScaleConversionOutputQuerySet implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String inputFilePath;
    private String processedFilePath;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private boolean isColor;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId, this.templateId, this.processId, this.processedFilePath
                , this.paperNo, this.status, this.stage, this.message, this.createdOn, this.rootPipelineId, this.modelName, this.modelVersion, this.inputFilePath, this.isColor).collect(Collectors.toList());
    }
}
