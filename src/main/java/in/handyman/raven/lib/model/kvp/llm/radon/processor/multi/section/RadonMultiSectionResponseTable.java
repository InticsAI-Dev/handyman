package in.handyman.raven.lib.model.kvp.llm.radon.processor.multi.section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonMultiSectionResponseTable {
    private Timestamp createdOn;
    private Long createdUserId;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId;
    private String inputFilePath;
    private String totalResponseJson;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long actionId;
    private String process;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String status;
    private String stage;
    private String message;
    private String category;
    private String request;
    private String response;
    private String endpoint;
    private Long sorContainerId;
    private Integer priorityOrder;
    private String postprocessingScript;

}

