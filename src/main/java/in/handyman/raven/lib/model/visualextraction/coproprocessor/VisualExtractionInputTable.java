package in.handyman.raven.lib.model.visualextraction.coproprocessor;

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
public class VisualExtractionInputTable implements CoproProcessor.Entity {
    private Long tenantId;
    private Long rootPipelineId;
    private Long groupId;
    private String originId;
    private Long paperNo;
    private String documentType;
    private String filePath;
    private String modelName;
    private String batchId;

    @Override
    public String getStatus() {
        return "ABSENT";
    }

    @Override
    public List<Object> getRowData() {
        return null; // Not needed
    }
}
