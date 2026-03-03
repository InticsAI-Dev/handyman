package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SOR Auto-Mapping Action Model
 * Handles automatic matching of extracted fields to SOR items using Copro LLM service
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ActionContext(actionName = "SorAutoMapping")
public class SorAutoMapping implements IActionContext {

    /**
     * Action name for identification
     */
    private String name;

    /**
     * Database resource connection name
     */
    private String resourceConn;

    /**
     * Copro endpoint URL for auto-match API
     * Example: "http://copro-service:7999/sor/auto-match-fields"
     */
    private String coproEndpoint;

    /**
     * Model name to use for LLM matching (optional)
     * If not specified, uses default KRYPTON/RADON model
     */
    private String modelName;

    /**
     * Whether to execute this action (conditional execution)
     */
    @Builder.Default
    private Boolean condition = true;

    /**
     * Maximum retries for Copro API calls
     */
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Timeout for Copro API calls in milliseconds
     */
    @Builder.Default
    private Integer timeoutMs = 60000;
}
