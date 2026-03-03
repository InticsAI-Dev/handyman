package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SOR Manual Mapping Action Model
 * Handles user-driven mapping of extracted fields to SOR items via Copro service
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(actionName = "SorManualMapping")
public class SorManualMapping implements IActionContext {

    /**
     * Action name for identification
     */
    private String name;

    /**
     * Database resource connection name
     */
    private String resourceConn;

    /**
     * Copro endpoint URL for manual mapping API
     * Example: "http://copro-service:7999/sor/manual-mapping"
     */
    private String coproEndpoint;

    /**
     * Whether to execute this action (conditional execution)
     */
    @Builder.Default
    private Boolean condition = true;

    /**
     * Timeout for Copro API calls in milliseconds
     */
    @Builder.Default
    private Integer timeoutMs = 30000;
}
