package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Custom Action for Vulcan Proactive Fallback
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(actionName = "VulcanFallback")
public class VulcanFallback implements IActionContext {
    private String name;
    private String resourceConn;
    private String endpoint;
    private String querySet;
    private Boolean condition = true;
}
