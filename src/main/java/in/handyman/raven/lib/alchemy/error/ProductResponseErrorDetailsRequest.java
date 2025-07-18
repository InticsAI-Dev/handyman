package in.handyman.raven.lib.alchemy.error;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseErrorDetailsRequest {
    private String outboundStatus;
    private String errorMessage;
    private String errorCode;
    private JsonNode errorJson;
    private String documentId;
    private String originId;
    private Long tenantId;
    private String transactionId;
    private String batchId;
}