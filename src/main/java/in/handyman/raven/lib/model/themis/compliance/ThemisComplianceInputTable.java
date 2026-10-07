package in.handyman.raven.lib.model.themis.compliance;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Producer row for the themisCompliance action — one extracted label (+ compliance prompt)
 * to evaluate. Mirrors NeonQueryInputTable with the Themis submission id carried through.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThemisComplianceInputTable implements CoproProcessor.Entity {
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
    private String submissionId;

    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
