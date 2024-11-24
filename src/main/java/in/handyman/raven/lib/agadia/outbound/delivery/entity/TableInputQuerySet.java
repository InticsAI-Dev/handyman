package in.handyman.raven.lib.agadia.outbound.delivery.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableInputQuerySet {
    private String fileName;
    private String fileUri;
    private String zipFileCheckSum;
    private String endpoint;
    private String appId;
    private String appSecretKey;
    private String bearerToken;
    private String documentId;
    private String outboundCondition;
    private String outboundJson;
    private String outboundZip;
    private String uuid;
    private String encryptionType;
    private String encryptionKey;
}