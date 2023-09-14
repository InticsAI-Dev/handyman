package in.handyman.raven.lib.model.TextExtraction;

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
public class DataExtractionInputTable implements CoproProcessor.Entity {
    public String process;
    private String originId;
    private Integer groupId;
    private String filePath;
    private Integer paperNo;
    private String tenantId;
    private String templateId;
    private Long processId;
    private Long rootPipelineId;
    private String templateName;

    @Override
    public List<Object> getRowData() {
        return null;
    }

}
