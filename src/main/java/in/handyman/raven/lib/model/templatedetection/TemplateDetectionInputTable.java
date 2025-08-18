package in.handyman.raven.lib.model.templatedetection;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetectionInputTable implements CoproProcessor.Entity {

    private String originId;
    private Integer paperNo;
    private Integer groupId;
    private String filePath;
    private Long tenantId;
    private String templateId;
    private Long processId;
    private Long rootPipelineId;
    private String questions;
    private Long modelRegistryId;
    private String batchId;

    public Timestamp getCreatedOn() {
        return createdOn;
    }
    @Override
    public String getStatus() {
        return ConsumerProcessApiStatus.ABSENT.getStatusDescription();
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    private Timestamp createdOn;


    public String getQuestions() {
        return questions;
    }

    public void setQuestions(String questions) {
        this.questions = questions;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public Integer getPaperNo() {
        return paperNo;
    }

    public void setPaperNo(Integer paperNo) {
        this.paperNo = paperNo;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getRootPipelineId() {
        return rootPipelineId;
    }

    public void setRootPipelineId(Long rootPipelineId) {
        this.rootPipelineId = rootPipelineId;
    }
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
    public String getBatchId() {
        return batchId;
    }

    public Long getModelRegistryId() {return modelRegistryId; }

    public void setModelRegistryId(Long modelRegistryId) { this.modelRegistryId = modelRegistryId; }

    @Override
    public List<Object> getRowData() {
        return null;
    }
}
