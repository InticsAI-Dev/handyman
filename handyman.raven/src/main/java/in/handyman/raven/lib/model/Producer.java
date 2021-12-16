package in.handyman.raven.lib.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import in.handyman.raven.compiler.RavenParser;
import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import in.handyman.raven.lambda.doa.ResourceConnection;
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
        actionName = "Producer"
)
public class Producer implements IActionContext {

    private Long pcmId;
    private String poison;
    private ResourceConnection source;

    private String name;
    private String stmt;
    private String push;

    @JsonIgnore
    private List<RavenParser.ActionContext> actions = new ArrayList<>();
    private Boolean condition = true;

    private String threadCount;

}
