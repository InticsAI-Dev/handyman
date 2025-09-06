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
public class RadonMultiSectionQueryInputTable {

    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String process;
    private String inputFilePath;
    private String userPrompt;
    private String systemPrompt;
    private String modelRegistry;
    private String batchId;
    private String category;
    private Timestamp createdOn;
    private String apiName;
    private Long sorContainerId;
    private String modelName;
    private String containerName;
    private String entity;
    private String inputResponseJson;
    private String radonProcessName;
    private Integer priorityOrder;
    private String postprocessingScript;

}
