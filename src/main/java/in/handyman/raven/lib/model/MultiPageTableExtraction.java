package in.handyman.raven.lib.model;

import in.handyman.raven.lambda.action.ActionContext;
import in.handyman.raven.lambda.action.IActionContext;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Model for Multi-Page Table Extraction Action
 * Extracts tables that span multiple pages using VLM models (Qwen, Gemini, etc.)
 */
@Data
@EqualsAndHashCode
@NoArgsConstructor
@ActionContext(
        actionName = "MultiPageTableExtraction"
)
public class MultiPageTableExtraction implements IActionContext {

    /**
     * Action alias name for context storage
     */
    private String name;

    /**
     * JDBC resource connection name for database operations
     */
    private String source;

    /**
     * Copro API endpoint URL for table extraction
     * Example: "http://localhost:7999/extract-multipage-table"
     */
    private String coproUrl;

    /**
     * Model name (e.g., "KRYPTON", "KRYPTON_MODEL")
     */
    private String modelName;

    /**
     * Optional: Custom model URL for different backends (Qwen, Gemini, etc.)
     * If null, uses default from Copro configuration
     */
    private String modelUrl;

    /**
     * System prompt for VLM model
     */
    private String systemPrompt;

    /**
     * User prompt for table extraction instructions
     */
    private String userPrompt;

    /**
     * SQL query to fetch table groups to extract
     * Expected columns: origin_id, tenant_id, table_group_id, pages (JSON array), file_paths (JSON array)
     */
    private String query;

    /**
     * Database table to store extraction results
     * Example: "valuation.table_extraction_multipage"
     */
    private String resultTable;

    /**
     * Condition for executing this action (default: true)
     */
    private Boolean condition = true;
}
