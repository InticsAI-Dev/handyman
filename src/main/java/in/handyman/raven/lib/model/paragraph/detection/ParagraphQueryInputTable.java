package in.handyman.raven.lib.model.paragraph.detection;

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
public class ParagraphQueryInputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private String filePath;
    private Long tenantId;
    private Long processId;
    private String outputDir;
    private Long rootPipelineId;
    private String process;
    private String prompt;
    private String sectionHeader;
    private Integer synonymId;
    private String batchId;
    @Override
    public List<Object> getRowData() {
        return null;
    }


}
