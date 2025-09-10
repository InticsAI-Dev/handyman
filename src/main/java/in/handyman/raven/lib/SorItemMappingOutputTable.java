package in.handyman.raven.lib;

import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
public class SorItemMappingOutputTable implements CoproProcessor.Entity {

    private Timestamp createdOn;
    private String createdUserId;
    private Timestamp lastUpdatedOn;
    private String lastUpdatedUserId;
    private String inputFilePath;
    private String inputResponseJson;
    private String totalResponseJson;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long actionId;
    private String process;
    private Integer groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String status;
    private String stage;
    private String message;
    private String category;
    private String request;
    private String answer;
    private String endpoint;
    private String sorItemName;

    @Override
    public List<Object> getRowData() {
        return Stream.of(
                this.createdOn,
                this.createdUserId,
                this.lastUpdatedOn,
                this.lastUpdatedUserId,
                this.inputFilePath,
                this.inputResponseJson,
                this.totalResponseJson,
                this.paperNo,
                this.originId,
                this.processId,
                this.actionId,
                this.process,
                this.groupId,
                this.tenantId,
                this.rootPipelineId,
                this.batchId,
                this.modelRegistry,
                this.status,
                this.stage,
                this.message,
                this.category,
                this.request,
                this.answer,
                this.endpoint,
                this.sorItemName
        ).collect(Collectors.toList());
    }

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }
}
