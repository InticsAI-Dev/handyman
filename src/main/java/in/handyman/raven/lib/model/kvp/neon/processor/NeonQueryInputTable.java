package in.handyman.raven.lib.model.kvp.neon.processor;

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
public class NeonQueryInputTable implements CoproProcessor.Entity {
    private String inputFilePath;
    private String prompt;
    private String textModel;
    private String process;
    private String paperType;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private String batchId;
    private String modelRegistry;
    private String responseFormat;


    @Override
    public List<Object> getRowData() {
        return null;
    }
}
