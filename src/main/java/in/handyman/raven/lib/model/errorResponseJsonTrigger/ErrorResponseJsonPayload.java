package in.handyman.raven.lib.model.errorResponseJsonTrigger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseJsonPayload {
    private String outboundStatus;
    private String errorMessage;
    private String errorCode;
    private ErrorJson errorJson;
    private String documentId;
    private String originId;
    private Long tenantId;
    private String transactionId;
    private String batchId;
}
