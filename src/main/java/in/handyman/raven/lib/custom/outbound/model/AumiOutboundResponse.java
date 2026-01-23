package in.handyman.raven.lib.custom.outbound.model;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AumiOutboundResponse {

    private Long id;

    private String requestTxnId;
    private String status;
    private String errorMessage;
    private String errorMessageDetail;
    private Integer errorCd;
    private String documentId;

    private OutboundJsonMetaData metadata;
    private AumiPayload aumipayload;


}