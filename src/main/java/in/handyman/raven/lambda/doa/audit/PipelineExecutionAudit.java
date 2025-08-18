package in.handyman.raven.lambda.doa.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.lambda.doa.AbstractAudit;
import in.handyman.raven.lambda.doa.IPipeline;
import in.handyman.raven.util.UniqueID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class PipelineExecutionAudit extends AbstractAudit implements IPipeline {

    private Long pipelineId ;

    private String modeOfExecution;
    private String hostName;
    private String threadName;
    private String pipelineLoadType;
    // Input audit
    @Builder.Default
    private String fileContent = "";
    @Builder.Default
    private String requestBody = "";
    @Builder.Default
    private String relativePath = "";


    public void updateExecutionStatusId(final Integer executionStatusId) {
        this.setExecutionStatusId(executionStatusId);
        HandymanActorSystemAccess.insert(PipelineExecutionStatusAudit.builder().pipelineId(this.pipelineId).executionStatusId(executionStatusId).rootPipelineId(this.getRootPipelineId()).build());
        log.info("Pipeline audit has been inserted with PipelineId {} as {}", this.getPipelineName(), ExecutionStatus.get(executionStatusId));
    }

    @Override
    public String getCurrentTime() {
        return IPipeline.super.getCurrentTime();
    }

    @Override
    public String getCurrentDate() {
        return IPipeline.super.getCurrentDate();
    }
}
