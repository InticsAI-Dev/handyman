package in.handyman.raven.lib.model;

import com.fasterxml.jackson.databind.JsonNode;
import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Auto Generated By Raven
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(
        actionName = "PushJson"
)
public class PushJson implements IActionContext {
    private String name;

    private String key;

    private JsonNode value;
    @Builder.Default
    private Boolean condition = true;
}
