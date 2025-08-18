package in.handyman.raven.lib.model.paperitemizer;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
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
public class PaperItemizerInputTable implements CoproProcessor.Entity {
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

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}

