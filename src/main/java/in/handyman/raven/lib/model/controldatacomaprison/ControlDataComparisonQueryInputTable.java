package in.handyman.raven.lib.model.controldatacomaprison;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ControlDataComparisonQueryInputTable implements CoproProcessor.Entity {
    private Long id;
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
    private String sorItemName;
    private String encryptionPolicy;
    private String isEncrypted;
    private Long sorItemId;
    private Long sorContainerId;
    private String lineItemType;
    private Long mismatchCount;
    private String matchStatus;

    // Implement CoproProcessor.Entity methods
    @Override
    public List<Object> getRowData() {
        List<Object> row = new ArrayList<>();
        row.add(id);
        row.add(originId);
        row.add(groupId);
        row.add(tenantId);
        row.add(paperNo);
        row.add(fileName);
        row.add(rootPipelineId);
        row.add(batchId);
        row.add(createdOn);
        row.add(actualValue);
        row.add(extractedValue);
        row.add(allowedAdapter);
        row.add(restrictAdapter);
        row.add(charLimit);
        row.add(sorItemName);
        row.add(encryptionPolicy);
        row.add(isEncrypted);
        row.add(sorItemId);
        row.add(sorContainerId);
        row.add(lineItemType);
        return row;
    }

    @Override
    public String getStatus() {
        // You can return match status or some field relevant for your use case
        // For now, returning extractedValue as a placeholder
        return extractedValue != null ? extractedValue : "UNKNOWN";
    }
}
