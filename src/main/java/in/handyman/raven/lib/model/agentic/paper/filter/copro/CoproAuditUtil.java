package in.handyman.raven.lib.model.agentic.paper.filter.copro;

import java.sql.Timestamp;

/**
 * Utility class for handling audit operations related to Copro retries.
 */
public class CoproAuditUtil {

    private static final int DEFAULT_MAX_BODY_LENGTH = 6000;
    private static int maxBodyLength;

    /**
     * Creates a CoproAuditUtil with default max body length.
     */
    public CoproAuditUtil() {
        this.maxBodyLength = DEFAULT_MAX_BODY_LENGTH;
    }

    /**
     * Creates a CoproAuditUtil with a specific max body length.
     * @param maxBodyLength the maximum allowed length for request/response fields.
     */
    public CoproAuditUtil(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    /**
     * Clones the given audit object to create a new instance for retry attempts.
     * Performs deep copy for timestamp fields.
     * @param original the original audit object.
     * @return a new CoproRetryErrorAuditTable instance with copied fields.
     */
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
                .request(original.getRequest())
                .response(original.getResponse())
                .endpoint(original.getEndpoint())
                .build();
    }

    /**
     * Truncates request and response fields in the audit object to prevent exceeding max allowed size.
     * @param audit the audit object to truncate.
     */
    public static void truncateAuditFields(CoproRetryErrorAuditTable audit) {
        if (audit == null) return;

        if (audit.getRequest() != null && audit.getRequest().length() > maxBodyLength) {
            audit.setRequest(audit.getRequest().substring(0, maxBodyLength));
        }
        if (audit.getResponse() != null && audit.getResponse().length() > maxBodyLength) {
            audit.setResponse(audit.getResponse().substring(0, maxBodyLength));
        }
    }

    /**
     * Truncates a string safely if it exceeds the max allowed length.
     * @param input the input string.
     * @return the truncated string if necessary.
     */
    public static String truncateSafe(String input) {
        if (input == null) return null;
        return input.length() > maxBodyLength ? input.substring(0, maxBodyLength) + "...[TRUNCATED]" : input;
    }
}
