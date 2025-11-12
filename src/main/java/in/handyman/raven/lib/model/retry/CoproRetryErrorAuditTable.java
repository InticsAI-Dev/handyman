package in.handyman.raven.lib.model.retry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CoproRetryErrorAuditTable {
    private Long id;
    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private String fileName;
    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp lastUpdatedOn;
    private String request;
    private String response;
    private String endpoint;
    @Builder.Default
    private int attempt = 0;
    private String coproServiceId;
    private String computationDetails;
    private Integer coproStatusCode;
    private String coproLog;
    private String coproErrorDetails;
    private String requestId;
    private Long imageDpi;
    private Long imageWidth;
    private Long imageHeight;
    private String extractedImageUnit;

}
