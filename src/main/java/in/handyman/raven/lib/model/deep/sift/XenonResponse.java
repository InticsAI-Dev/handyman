package in.handyman.raven.lib.model.deep.sift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.ComputationDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
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
    private InferResponseData inferResponse;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("durationTime")
    private Double durationTime;

    private Integer statusCode;
    private String coproLog;
    private UUID requestId;
    private String detail;
    private ComputationDetails computationDetails;

    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    public boolean hasInferResponse() {
        return inferResponse != null
                && inferResponse.getText() != null
                && !inferResponse.getText().trim().isEmpty();
    }

    public String getInferResponseText() {
        return inferResponse != null ? inferResponse.getText() : null;
    }

    public List<BboxItem> getBboxListSafe() {
        if (inferResponse == null || inferResponse.getBboxList() == null) {
            return Collections.emptyList();
        }
        return inferResponse.getBboxList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InferResponseData {

        @JsonProperty("text")
        private String text;

        @JsonProperty("bbox_list")
        private List<BboxItem> bboxList;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BboxItem {

        @JsonProperty("text")
        private String text;

        @JsonProperty("bbox")
        private List<Integer> bbox;

        @JsonProperty("confidence")
        private Integer confidence;
    }
}