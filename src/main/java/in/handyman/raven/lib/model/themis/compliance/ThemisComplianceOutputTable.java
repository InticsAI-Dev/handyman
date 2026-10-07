package in.handyman.raven.lib.model.themis.compliance;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Consumer row written by the themisCompliance action. Carries the LLM compliance report
 * (totalResponseJson) plus the deterministically-computed verdict the action derives from it
 * (score / overallStatus / pass-warn-fail counts). getRowData() order MUST match the
 * output-table column list in ThemisComplianceAction.COLUMN_LIST.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThemisComplianceOutputTable implements CoproProcessor.Entity {
    private Long createdUserId;
    private Timestamp createdOn;
    private Long lastUpdatedUserId;
    private Timestamp lastUpdatedOn;
    private String inputFilePath;
    private String totalResponseJson;
    private Integer complianceScore;
    private String overallStatus;
    private Integer passCount;
    private Integer warnCount;
    private Integer failCount;
    private String textModel;
    private String paperType;
    private Integer paperNo;
    private String originId;
    private Long processId;
    private String process;
    private Long groupId;
    private Long tenantId;
    private Long actionId;
    private Long rootPipelineId;
    private String batchId;
    private String submissionId;
    private String modelRegistry;
    private String responseFormat;
    private String status;
    private String stage;
    private String message;

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.createdUserId, this.createdOn, this.lastUpdatedUserId, this.lastUpdatedOn,
                this.inputFilePath, this.totalResponseJson, this.complianceScore, this.overallStatus,
                this.passCount, this.warnCount, this.failCount, this.textModel, this.paperType, this.paperNo,
                this.originId, this.processId, this.process, this.groupId, this.tenantId, this.actionId,
                this.rootPipelineId, this.batchId, this.submissionId, this.modelRegistry, this.responseFormat,
                this.status, this.stage, this.message).collect(Collectors.toList());
    }
}
