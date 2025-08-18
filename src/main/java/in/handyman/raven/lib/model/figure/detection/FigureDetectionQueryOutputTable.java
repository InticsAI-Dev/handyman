package in.handyman.raven.lib.model.figure.detection;

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

public class FigureDetectionQueryOutputTable implements CoproProcessor.Entity {

    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String originId;
    private Integer paperNo;
    private String encode;
    private Long groupId;
    private String filePath;
    private Long tenantId;
    private Long processId;
    private Long rootPipelineId;
    private String process;
    private String status;
    private String stage;
    private String message;
    private String modelName;
    private String modelVersion;
    private String batchId;
    private String request;
    private String response;
    private String endpoint;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdOn, this.createdUserId, this.lastUpdatedOn, this.lastUpdatedUserId, this.originId, this.paperNo,
                this.encode, this.groupId, this.filePath, this.tenantId, this.processId, this.rootPipelineId, this.process,
                this.status, this.stage, this.message, this.modelName, this.modelVersion, this.batchId, this.request, this.response, this.endpoint).collect(Collectors.toList());

    }
}