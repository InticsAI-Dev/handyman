package in.handyman.raven.lambda.doa.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.actor.HandymanActorSystemAccess;
import in.handyman.raven.lambda.doa.AbstractAudit;
import in.handyman.raven.lambda.doa.IAction;
import in.handyman.raven.lambda.process.LambdaEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.event.SubstituteLoggingEvent;

import java.util.ArrayDeque;
import java.util.Optional;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActionExecutionAudit extends AbstractAudit implements IAction {

    @JsonIgnore
    private final ArrayDeque<SubstituteLoggingEvent> eventQueue = new ArrayDeque<>();
    private Long actionId ;
    private String actionName;

    private String macroName;
    private String modelPath;

    private Long pipelineId;
    private JsonNode input;
    private String log;
    private Integer executionGroupId;
    private String threadName;

    @JsonIgnore
    private String inputNode;

    public void setInput(final JsonNode input) {
        this.input = input;
        this.inputNode = Optional.ofNullable(input).map(JsonNode::toString).orElse(null);
    }

    public void updateExecutionStatusId(final Integer executionStatusId) {
        this.setExecutionStatusId(executionStatusId);
        HandymanActorSystemAccess.insert(ActionExecutionStatusAudit.builder().actionId(actionId)
                .rootPipelineId(this.getRootPipelineId())
                .pipelineId(this.pipelineId).executionStatusId(executionStatusId).build());
        LambdaEngine.getLogger(this).info("Action audit has been inserted with ActionId {} as {}", this.actionId, ExecutionStatus.get(executionStatusId));
    }


    @Override
    public String getCurrentTime() {
        return IAction.super.getCurrentTime();
    }

    @Override
    public String getCurrentDate() {
        return IAction.super.getCurrentDate();
    }
}
