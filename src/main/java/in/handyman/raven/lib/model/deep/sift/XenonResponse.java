package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.ComputationDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XenonResponse {

    @JsonProperty("originId")
    private String originId;

    @JsonProperty("batchId")
    private String batchId;

    @JsonProperty("processId")
    private Long processId;

    @JsonProperty("groupId")
    private Long groupId;

    @JsonProperty("tenantId")
    private Long tenantId;

    @JsonProperty("rootPipelineId")
    private Long rootPipelineId;

    @JsonProperty("process")
    private String process;

    @JsonProperty("actionId")
    private Long actionId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("inferResponse")
    private String inferResponse;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("durationTime")
    private Long durationTime;
    private Integer statusCode;
    private String coproLog;
    private UUID requestId;
    private String detail;
    private ComputationDetails computationDetails;


    // Additional helper methods if needed
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    public boolean hasInferResponse() {
        return inferResponse != null;
    }
}
