package in.handyman.raven.lib.model.paperItemizer;

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
public class PaperItemizerInputTable implements CoproProcessor.Entity {
    private String originId;
    private Long processId;
    private Integer groupId;
    private String tenantId;
    private String templateId;
    private String filePath;
    private String outputDir;
    private Long rootPipelineId;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}

