package in.handyman.raven.lib;

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
public class AutoRotationOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private String tenantId;
    private String templateId;
    private Long processId;
    private String processedFilePath;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId, this.templateId, this.processId, this.processedFilePath
                , this.paperNo, this.status, this.stage, this.message, this.createdOn, this.rootPipelineId).collect(Collectors.toList());
    }
}