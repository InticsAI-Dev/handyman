package in.handyman.raven.lib.model.common.copro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * DTO carrying the common fields needed for async copro result writing.
 * Maps from the CoproResponse Kafka message headers/fields.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoproAsyncContext {
    private String originId;
    private String batchId;
    private String requestType;
    private String outputTable;
    private Integer pageNo;
    private Long groupId;
    private Long processId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long actionId;
    private Long sorContainerId;
    private String sorContainerName;
    private String templateId;
    private String templateName;
    private String filePath;
    private String promptType;
    private String modelName;
    private String modelVersion;
    private String process;
    private String modelRegistry;
    private String apiName;
    private String category;
    private String inputFilePath;
    private String postProcess;
    private String postProcessClassName;
    private String postProcessClass;
    private Timestamp createdOn;
}
