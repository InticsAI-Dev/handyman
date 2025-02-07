package in.handyman.raven.lib.model.controldatacomaprison;

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
public class ControlDataComparisonQueryInputTable {
    private String originId;

    private Long groupId;
    private Long tenantId;
    private Long paperNo;
    private String fileName;
    private Long rootPipelineId;
    private String batchId;
    private Timestamp createdOn;
    private String actualValue;
    private String extractedValue;
    private String allowedAdapter;
    private String restrictAdapter;
    private Long charLimit;

}
