package in.handyman.raven.lib.model.templatedetection;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetectionOutputTable implements CoproProcessor.Entity {
    private String originId;
    private Integer groupId;
    private Long tenantId;
    private String templateId;
    private Long processId;

    private String processedFilePath;
    private String question;
    private String predictedAttributionValue;
    private Float scores;
    private String bboxes;
    private Integer imageWidth;
    private Integer imageHeight;
    private Integer imageDPI;
    private String extractedImageUnit;

    private Integer paperNo;
    private String status;
    private String stage;
    private String message;
    private Timestamp createdOn;
    private Long rootPipelineId;
    private String modelName;
    private String modelVersion;
    private String request;
    private String response;
    private String endpoint;


    public TemplateDetectionOutputTable(String originId, Integer groupId, Long tenantId, String templateId, Long processId, String processedFilePath,  Integer paperNo, String status, String stage, String message, Timestamp createdOn, Long rootPipelineId) {
        this.originId = originId;
        this.groupId = groupId;
        this.tenantId = tenantId;
        this.templateId = templateId;
        this.processId = processId;
        this.processedFilePath = processedFilePath;
        this.paperNo = paperNo;
        this.status = status;
        this.stage = stage;
        this.message = message;
        this.createdOn = createdOn;
        this.rootPipelineId = rootPipelineId;
        this.modelName = modelName;
        this.modelVersion = modelVersion;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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

    public String getProcessedFilePath() {
        return processedFilePath;
    }

    public void setProcessedFilePath(String processedFilePath) {
        this.processedFilePath = processedFilePath;
    }

    public Integer getPaperNo() {
        return paperNo;
    }

    public void setPaperNo(Integer paperNo) {
        this.paperNo = paperNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn;
    }

    public Long getRootPipelineId() {
        return rootPipelineId;
    }

    public void setRootPipelineId(Long rootPipelineId) {
        this.rootPipelineId = rootPipelineId;
    }

    @Override
    public List<Object> getRowData() {
        return Stream.of(this.processId, this.originId, this.paperNo,this.groupId
                ,this.processedFilePath,this.question, this.predictedAttributionValue,this.scores,this.bboxes
                ,this.imageWidth,this.imageHeight,this.imageDPI,this.extractedImageUnit,this.tenantId,this.templateId,this.status,this.stage,this.message,this.createdOn,this.rootPipelineId,this.modelName, this.modelVersion, this.request, this.response, this.endpoint).collect(Collectors.toList());
    }
}
