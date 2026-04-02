package in.handyman.raven.lib.custom.outbound.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalOutboundResponse {

    private String requestTxnId;
    private String status;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String errorMessage;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String errorMessageDetail;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Integer errorCd;
    private String documentId;
    private String inboundTransactionId;

    private OutboundJsonMetaData metadata;
    private MedicalPayload aumipayload;


}