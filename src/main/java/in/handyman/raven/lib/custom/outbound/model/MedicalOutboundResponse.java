package in.handyman.raven.lib.custom.outbound.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalOutboundResponse {

    private String requestTxnId;
    private String status;
    private String errorMessage;
    private String errorMessageDetail;
    private Integer errorCd;
    private String documentId;
    private String inboundTransactionId;

    private OutboundJsonMetaData metadata;
    private MedicalPayload aumipayload;


}