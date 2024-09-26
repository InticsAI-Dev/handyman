package in.handyman.raven.lib.model.utmodel;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UrgencyTriageOutputTable implements CoproProcessor.Entity {
    private String createdUserId;
    private String lastUpdatedUserId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private Float confScore;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private Long modelId;
    private String status;
    private String stage;
    private String message;
    private String utResult;
    private String bbox;
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private String request;
    private String response;
    private String endpoint;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdUserId, this.lastUpdatedUserId, this.processId, this.groupId, this.tenantId, this.confScore,
                this.originId, this.paperNo, this.templateId, this.modelId, this.status, this.stage, this.message, this.utResult, this.bbox, this.rootPipelineId, this.modelName, this.modelVersion, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }

}
