package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RadonQueryOutputTable implements CoproProcessor.Entity {
    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String inputFilePath;
    private String totalResponseJson;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long actionId;
    private String process;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String status;
    private String stage;
    private String message;
    private String category;
    private String request;
    private String response;
    private String endpoint;
    private Long sorContainerId;
    private String computationDetails;
    private Integer statusCode;
    private String log;
    private String detail;
    private UUID requestId;




    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdOn,this.createdUserId,  this.lastUpdatedOn,this.lastUpdatedUserId,
                this.inputFilePath, this.totalResponseJson,  this.paperNo, this.originId,
                this.processId,this.actionId, this.process, this.groupId, this.tenantId,  this.rootPipelineId, this.batchId,
                this.modelRegistry, this.status, this.stage, this.message, this.category, this.request, this.response, this.endpoint, this.sorContainerId, this.computationDetails, this.statusCode, this.log, this.detail, this.requestId).collect(Collectors.toList());

    }
}

