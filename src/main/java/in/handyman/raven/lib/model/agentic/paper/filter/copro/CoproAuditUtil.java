package in.handyman.raven.lib.model.agentic.paper.filter.copro;

import java.sql.Timestamp;

/**
 * Utility for audit cloning / truncation.
 */
public final class CoproAuditUtil {

    private static final int DEFAULT_MAX_BODY_LENGTH = 6000;
    private final int maxBodyLength;

    public CoproAuditUtil() {
        this.maxBodyLength = DEFAULT_MAX_BODY_LENGTH;
    }

    public CoproAuditUtil(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    public CoproRetryErrorAuditTable cloneFullAuditForAttemptIfNeeded(CoproRetryErrorAuditTable original) {
        if (original == null) {
            return new CoproRetryErrorAuditTable();
        }
        Timestamp createdOnCopy = (original.getCreatedOn() != null) ? new Timestamp(original.getCreatedOn().getTime()) : null;
        Timestamp lastUpdatedOnCopy = (original.getLastUpdatedOn() != null) ? new Timestamp(original.getLastUpdatedOn().getTime()) : null;

        return CoproRetryErrorAuditTable.builder()
                .originId(original.getOriginId())
                .groupId(original.getGroupId())
                .tenantId(original.getTenantId())
                .templateId(original.getTemplateId())
                .processId(original.getProcessId())
                .filePath(original.getFilePath())
                .containerName(original.getContainerName())
                .containerValue(original.getContainerValue())
                .fileName(original.getFileName())
                .paperNo(original.getPaperNo())
                .status(original.getStatus())
                .stage(original.getStage())
                .message(original.getMessage())
                .createdOn(createdOnCopy)
                .rootPipelineId(original.getRootPipelineId())
                .batchId(original.getBatchId())
                .lastUpdatedOn(lastUpdatedOnCopy)
                .request(truncateSafe(original.getRequest()))
                .response(truncateSafe(original.getResponse()))
                .endpoint(original.getEndpoint())
                .attempt(original.getAttempt() + 1)
                .build();
    }

    public void truncateAuditFields(CoproRetryErrorAuditTable audit) {
        if (audit == null) return;
        audit.setRequest(truncateSafe(audit.getRequest()));
        audit.setResponse(truncateSafe(audit.getResponse()));
    }

    public String truncateSafe(String input) {
        if (input == null) return null;
        return input.length() > maxBodyLength ? input.substring(0, maxBodyLength) + "...[TRUNCATED]" : input;
    }
}
