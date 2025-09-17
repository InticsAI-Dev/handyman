package in.handyman.raven.core.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequestContext {
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private Long actionId;
    private Long requestId;
    private String originId;
    private Integer paperNo;
    private String stage;
    private Long retryCount;
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null ? 0L : this.retryCount) + 1L;
    }
}
