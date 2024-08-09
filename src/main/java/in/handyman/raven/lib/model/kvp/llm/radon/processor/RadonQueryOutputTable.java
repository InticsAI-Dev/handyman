package in.handyman.raven.lib.model.kvp.llm.radon.processor;

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
public class RadonQueryOutputTable implements CoproProcessor.Entity {
    private Long createdUserId;
    private Timestamp createdOn;
    private Long lastUpdatedUserId;
    private Timestamp lastUpdatedOn;
    private String inputFilePath;
    private String totalResponseJson;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private String process;
    private Long groupId;
    private Long tenantId;
    private Long actionId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String status;
    private String stage;
    private String message;


    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdUserId, this.createdOn, this.lastUpdatedUserId, this.lastUpdatedOn,
                this.inputFilePath, this.totalResponseJson,  this.paperNo, this.originId,
                this.processId, this.process, this.groupId, this.tenantId, this.actionId, this.rootPipelineId, this.batchId,
                this.modelRegistry, this.status, this.stage, this.message).collect(Collectors.toList());

    }
}
