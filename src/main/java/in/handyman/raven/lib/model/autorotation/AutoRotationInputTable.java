package in.handyman.raven.lib.model.autorotation;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoRotationInputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private String filePath;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private String outputDir;
    private Long rootPipelineId;
    public String process;
    private String batchId;
    private Timestamp createdOn;
    private String base64img;

    @Override
    public List<Object> getRowData() {
        return null;
    }



}
