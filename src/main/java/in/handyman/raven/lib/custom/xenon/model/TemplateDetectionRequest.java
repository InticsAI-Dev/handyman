package in.handyman.raven.lib.custom.xenon.model;

import java.util.List;

public class TemplateDetectionRequest {

    private String  inputFilePath;
    private Long rootPipelineId;
    private Long actionId;
    private String process;
    private List<String> attributes;


    public Long getActionId() {
        return actionId;
    }

    public Long getRootPipelineId() {
        return rootPipelineId;
    }

    public void setRootPipelineId(Long rootPipelineId) {
        this.rootPipelineId = rootPipelineId;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<String> attributes) {
        this.attributes = attributes;
    }
}
