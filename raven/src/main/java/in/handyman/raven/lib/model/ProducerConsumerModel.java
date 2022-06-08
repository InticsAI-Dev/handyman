package in.handyman.raven.lib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import in.handyman.raven.compiler.RavenParser;
import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import in.handyman.raven.lambda.doa.config.SpwResourceConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto Generated By Raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
        actionName = "ProducerConsumerModel"
)
public class ProducerConsumerModel implements IActionContext {

    private String name;
    private SpwResourceConfig source;

    private String produceThreadCount;
    private String consumeThreadCount;

    @JsonIgnore
    @Builder.Default
    private List<RavenParser.ProducerContext> produce = new ArrayList<>();
    @JsonIgnore
    @Builder.Default
    private List<RavenParser.ConsumerContext> consume = new ArrayList<>();

    @Builder.Default
    private Boolean condition = true;
}
