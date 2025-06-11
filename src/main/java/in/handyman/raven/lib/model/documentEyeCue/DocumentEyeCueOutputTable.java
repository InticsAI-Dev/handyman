package in.handyman.raven.lib.model.documentEyeCue;

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
public class DocumentEyeCueOutputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private Long processId;
    private String templateId;
    private String processedFilePath;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp lastUpdatedOn;
    private String request;
    private String response;
    private String endpoint;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.originId, this.groupId, this.tenantId, this.templateId, this.processedFilePath,
                this.status, this.stage, this.message, this.createdOn, this.processId, this.rootPipelineId,
                this.batchId, this.lastUpdatedOn, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }
}

