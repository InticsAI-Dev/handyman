package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RadonQueryInputTable implements CoproProcessor.Entity{

    private String originId;
    private Integer paperNo;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private String inputFilePath;
    private String prompt;
    private String modelRegistry;
    private String batchId;
    private String category;


    @Override
    public List<Object> getRowData() {
        return List.of();
    }
}
