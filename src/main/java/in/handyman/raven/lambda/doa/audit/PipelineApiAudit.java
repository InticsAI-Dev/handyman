package in.handyman.raven.lambda.doa.audit;

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
public class PipelineApiAudit {
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private Long actionId;
    private Long requestId;
    private Long timeTakenSec;
    private Long retryCount;
    private String endpoint;
    private String originId;
    private Integer paperNo;
    private String request;
    private String response;
    private String stage;
    private String status;
    private String message;
    private java.time.LocalDateTime createdOn;
    private java.time.LocalDateTime completedOn;

}
