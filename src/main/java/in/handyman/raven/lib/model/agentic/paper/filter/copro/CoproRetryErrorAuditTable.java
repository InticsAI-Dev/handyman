package in.handyman.raven.lib.model.agentic.paper.filter.copro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CoproRetryErrorAuditTable {
    private static final int MAX_BODY_LENGTH = 6000; // truncate long request/response bodies

    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String filePath;
    private String containerName;
    private String containerValue;
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

    public CoproRetryErrorAuditTable cloneForRetry() {
        Timestamp createdOnCopy = createdOn != null ? new Timestamp(createdOn.getTime()) : null;
        Timestamp lastUpdatedOnCopy = lastUpdatedOn != null ? new Timestamp(lastUpdatedOn.getTime()) : null;

        return CoproRetryErrorAuditTable.builder()
                .originId(originId)
                .groupId(groupId)
                .tenantId(tenantId)
                .templateId(templateId)
                .processId(processId)
                .filePath(filePath)
                .containerName(containerName)
                .containerValue(containerValue)
                .fileName(fileName)
                .paperNo(paperNo)
                .status(status)
                .stage(stage)
                .message(message)
                .createdOn(createdOnCopy)
                .rootPipelineId(rootPipelineId)
                .batchId(batchId)
                .lastUpdatedOn(lastUpdatedOnCopy)
                .request(truncate(request))
                .response(truncate(response))
                .endpoint(endpoint)
                .attempt(attempt + 1)
                .build();
    }

    private String truncate(String input) {
        if (input == null) return null;
        return input.length() > MAX_BODY_LENGTH ? input.substring(0, MAX_BODY_LENGTH) + "...[TRUNCATED]" : input;
    }
}
