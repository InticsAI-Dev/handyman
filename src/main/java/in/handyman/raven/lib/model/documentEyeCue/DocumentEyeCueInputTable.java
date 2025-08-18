package in.handyman.raven.lib.model.documentEyeCue;

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
public class DocumentEyeCueInputTable implements CoproProcessor.Entity {

    private String originId;
    private Long processId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private String filePath;
    private String outputDir;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp createdOn;
    private String documentId;
    private String fileName;

    @Override
    public List<Object> getRowData() {
        return null;
    }

    @Override
    public String getStatus() {
        return null;
    }
}