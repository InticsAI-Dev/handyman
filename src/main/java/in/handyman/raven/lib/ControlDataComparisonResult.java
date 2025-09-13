package in.handyman.raven.lib;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class ControlDataComparisonResult implements CoproProcessor.Entity {
    private Long rootPipelineId;
    private LocalDate createdOn;
    private Long groupId;
    private String fileName;
    private String originId;
    private String batchId;
    private Long paperNo;
    private String actualValue;
    private String extractedValue;
    private String matchStatus;
    private Long mismatchCount;
    private Long tenantId;
    private String classification;
    private Long sorContainerId;
    private String sorItemName;
    private Long sorItemId;

    public ControlDataComparisonResult(Long rootPipelineId, LocalDate createdOn, Long groupId, String fileName, String originId,
                                       String batchId, Long paperNo, String actualValue, String extractedValue,
                                       String matchStatus, Long mismatchCount, Long tenantId, String classification,
                                       Long sorContainerId, String sorItemName, Long sorItemId) {
        this.rootPipelineId = rootPipelineId;
        this.createdOn = createdOn;
        this.groupId = groupId;
        this.fileName = fileName;
        this.originId = originId;
        this.batchId = batchId;
        this.paperNo = paperNo;
        this.actualValue = actualValue;
        this.extractedValue = extractedValue;
        this.matchStatus = matchStatus;
        this.mismatchCount = mismatchCount;
        this.tenantId = tenantId;
        this.classification = classification;
        this.sorContainerId = sorContainerId;
        this.sorItemName = sorItemName;
        this.sorItemId = sorItemId;
    }

    @Override
    public List<Object> getRowData() {
        // order must match the insertSql used by CoproProcessor.startConsumer
        return Arrays.asList(
                rootPipelineId,
                createdOn,
                groupId,
                fileName,
                originId,
                batchId,
                paperNo,
                actualValue,
                extractedValue,
                matchStatus,
                mismatchCount,
                tenantId,
                classification,
                sorContainerId,
                sorItemName,
                sorItemId
        );
    }

    @Override
    public String getStatus() {
        return matchStatus;
    }

}
