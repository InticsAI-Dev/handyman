package in.handyman.raven.lib.model.tableextraction.coproprocessor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//2. output pojo for table, which implements CoproProcessor.Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableExtractionOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private Long processId;
    private String templateId;
    private String processedFilePath;
    private String croppedImage;
    private String bboxes;
    private Long paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String tableResponse;
    private String modelName;
    private String truthEntityId;
    private Long sorContainerId;
    private Long channelId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId, this.templateId, this.processedFilePath,
                this.paperNo, this.status, this.stage, this.message, this.createdOn, this.processId,
                this.rootPipelineId, this.tableResponse, this.bboxes, this.croppedImage, this.modelName,
                this.truthEntityId, this.sorContainerId, this.channelId).collect(Collectors.toList());
    }
}
