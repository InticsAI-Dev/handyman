package in.handyman.raven.lib.model.utmodel;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UrgencyTriageInputTable implements CoproProcessor.Entity {
    private String createdUserId;
    private String lastUpdatedUserId;
    private Long tenantId;
    private Long processId;
    private Integer groupId;
    private String originId;
    private Integer paperNo;
    private String templateId;
    private Long modelId;
    private String inputFilePath;
    private Long rootPipelineId;
    private String batchId;
    private String prompt;
    private Timestamp createdOn;

    @Override
    public List<Object> getRowData() {
        return null;
    }
}

