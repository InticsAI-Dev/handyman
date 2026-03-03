package in.handyman.raven.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultiPageTableExtraction;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Action for extracting tables that span multiple pages using VLM models.
 * Calls Copro API endpoint and stores markdown table results in database.
 */
@ActionExecution(
        actionName = "MultiPageTableExtraction"
)
public class MultiPageTableExtractionAction implements IActionExecution {

    private static final String ACTION_NAME = "MultiPageTableExtraction";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final MultiPageTableExtraction model;
    private final Marker marker;
    private final ObjectMapper mapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public MultiPageTableExtractionAction(
            final ActionExecutionAudit actionExecutionAudit,
            final Logger log,
            final Object model) {
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.model = (MultiPageTableExtraction) model;
        this.marker = MarkerFactory.getMarker(ACTION_NAME);
    }

    @Override
    public boolean executeIf() throws Exception {
        return model.getCondition();
    }

    @Override
    public void execute() throws Exception {
        final long actionId = actionExecutionAudit.getActionId();
        final String source = model.getSource();
        final String coproUrl = model.getCoproUrl();
        final String query = model.getQuery();
        final String resultTable = model.getResultTable();

        log.info(marker, "id#{}, Starting multi-page table extraction from Copro: {}", actionId, coproUrl);

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(source);

        try (Connection conn = jdbi.open().getConnection()) {

            // Fetch table groups to extract
            List<TableGroupInput> tableGroups = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    TableGroupInput input = new TableGroupInput();
                    input.originId = rs.getString("origin_id");
                    input.tenantId = rs.getString("tenant_id");
                    input.tableGroupId = rs.getString("table_group_id");

                    // Parse JSON arrays
                    String pagesJson = rs.getString("pages");
                    String filePathsJson = rs.getString("file_paths");

                    input.pages = mapper.readValue(pagesJson, new TypeReference<List<Integer>>() {});
                    input.filePaths = mapper.readValue(filePathsJson, new TypeReference<List<String>>() {});

                    // Optional fields
                    try {
                        input.processId = rs.getLong("process_id");
                    } catch (Exception e) {
                        input.processId = null;
                    }
                    try {
                        input.batchId = rs.getString("batch_id");
                    } catch (Exception e) {
                        input.batchId = null;
                    }
                    try {
                        input.rootPipelineId = rs.getLong("root_pipeline_id");
                    } catch (Exception e) {
                        input.rootPipelineId = null;
                    }

                    tableGroups.add(input);
                }
            }

            log.info(marker, "id#{}, Found {} table groups to extract", actionId, tableGroups.size());

            // Process each table group
            List<TableExtractionResult> results = new ArrayList<>();
            for (TableGroupInput input : tableGroups) {
                try {
                    TableExtractionResult result = extractTable(input, coproUrl, actionId);
                    results.add(result);
                } catch (Exception e) {
                    log.error(marker, "id#{}, Failed to extract table {}: {}",
                            actionId, input.tableGroupId, e.getMessage(), e);

                    // Create error result
                    TableExtractionResult errorResult = new TableExtractionResult();
                    errorResult.originId = input.originId;
                    errorResult.tenantId = input.tenantId;
                    errorResult.tableGroupId = input.tableGroupId;
                    errorResult.status = "FAILED";
                    errorResult.errorMessage = e.getMessage();
                    errorResult.pages = input.pages;
                    results.add(errorResult);
                }
            }

            // Store results in database
            String insertSql = String.format(
                    "INSERT INTO %s (origin_id, tenant_id, table_group_id, pages, markdown_table, status, model_name, extraction_method, error_message, duration_time, created_on) " +
                    "VALUES (?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (origin_id, tenant_id, table_group_id) DO UPDATE SET " +
                    "markdown_table = EXCLUDED.markdown_table, status = EXCLUDED.status, " +
                    "error_message = EXCLUDED.error_message, duration_time = EXCLUDED.duration_time, updated_on = NOW()",
                    resultTable);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (TableExtractionResult result : results) {
                    ps.setString(1, result.originId);
                    ps.setString(2, result.tenantId);
                    ps.setString(3, result.tableGroupId);
                    ps.setString(4, mapper.writeValueAsString(result.pages));
                    ps.setString(5, result.markdownTable);
                    ps.setString(6, result.status);
                    ps.setString(7, result.modelName);
                    ps.setString(8, result.extractionMethod);
                    ps.setString(9, result.errorMessage);
                    if (result.durationTime != null) {
                        ps.setDouble(10, result.durationTime);
                    } else {
                        ps.setNull(10, java.sql.Types.DOUBLE);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            log.info(marker, "id#{}, Successfully stored {} table extraction results", actionId, results.size());

            // Store result count in context
            actionExecutionAudit.getContext().put(model.getName(),
                    String.format("Extracted %d tables", results.size()));

        } catch (Exception e) {
            log.error(marker, "id#{}, Multi-page table extraction failed", actionId, e);
            throw new HandymanException("Multi-page table extraction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Calls Copro API once per page and merges markdown results.
     * Multi-page orchestration happens here in Handyman, not in Copro.
     */
    private TableExtractionResult extractTable(TableGroupInput input, String coproUrl, long actionId) throws Exception {
        log.info(marker, "id#{}, Extracting table {} with {} pages",
                actionId, input.tableGroupId, input.pages.size());

        List<String> markdownParts = new ArrayList<>();
        double totalDuration = 0.0;
        String modelName = null;
        List<Integer> successfulPages = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Process each page individually
        for (int i = 0; i < input.pages.size(); i++) {
            int pageNum = input.pages.get(i);
            String filePath = input.filePaths.get(i);

            try {
                log.info(marker, "id#{}, Calling Copro for page {} of table {}",
                        actionId, pageNum, input.tableGroupId);

                // Read and encode image as base64
                String base64Image = readImageAsBase64(filePath);

                // Build single-page request payload
                Map<String, Object> requestPayload = new HashMap<>();
                requestPayload.put("originId", input.originId);
                requestPayload.put("tenantId", input.tenantId);
                requestPayload.put("tableGroupId", input.tableGroupId);
                requestPayload.put("pageNumber", pageNum);
                requestPayload.put("base64Image", base64Image);

                if (model.getSystemPrompt() != null) {
                    requestPayload.put("systemPrompt", model.getSystemPrompt());
                }

                String userPrompt = model.getUserPrompt() != null
                        ? model.getUserPrompt()
                        : "Extract the table from this page in markdown format.";
                requestPayload.put("userPrompt", userPrompt);

                if (model.getModelName() != null) {
                    requestPayload.put("modelName", model.getModelName());
                }

                if (model.getModelUrl() != null) {
                    requestPayload.put("modelUrl", model.getModelUrl());
                }

                // Optional fields
                if (input.processId != null) {
                    requestPayload.put("processId", input.processId);
                }
                if (input.batchId != null) {
                    requestPayload.put("batchId", input.batchId);
                }
                if (input.rootPipelineId != null) {
                    requestPayload.put("rootPipelineId", input.rootPipelineId);
                }
                requestPayload.put("actionId", actionId);

                String jsonRequest = mapper.writeValueAsString(requestPayload);

                Request request = new Request.Builder()
                        .url(coproUrl)
                        .post(RequestBody.create(jsonRequest, JSON))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        String errorMsg = String.format("Page %d failed: HTTP %d", pageNum, response.code());
                        errors.add(errorMsg);
                        log.warn(marker, "id#{}, {}", actionId, errorMsg);
                        continue;
                    }

                    // Parse response
                    Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

                    String status = (String) responseMap.get("status");
                    if ("SUCCESS".equals(status)) {
                        String markdown = (String) responseMap.get("markdownTable");
                        if (markdown != null && !markdown.trim().isEmpty()) {
                            // Add page marker if multi-page
                            if (input.pages.size() > 1) {
                                markdownParts.add(String.format("<!-- Page %d -->\n%s", pageNum, markdown));
                            } else {
                                markdownParts.add(markdown);
                            }
                            successfulPages.add(pageNum);

                            if (modelName == null) {
                                modelName = (String) responseMap.get("modelName");
                            }

                            Object duration = responseMap.get("durationTime");
                            if (duration != null) {
                                totalDuration += ((Number) duration).doubleValue();
                            }

                            log.info(marker, "id#{}, Successfully extracted page {}", actionId, pageNum);
                        }
                    } else {
                        String errorMsg = (String) responseMap.get("errorMessage");
                        errors.add(String.format("Page %d: %s", pageNum, errorMsg));
                        log.warn(marker, "id#{}, Page {} extraction failed: {}", actionId, pageNum, errorMsg);
                    }
                }

            } catch (Exception e) {
                String errorMsg = String.format("Page %d exception: %s", pageNum, e.getMessage());
                errors.add(errorMsg);
                log.error(marker, "id#{}, Error extracting page {}: {}", actionId, pageNum, e.getMessage(), e);
            }
        }

        // Build final result
        TableExtractionResult result = new TableExtractionResult();
        result.originId = input.originId;
        result.tenantId = input.tenantId;
        result.tableGroupId = input.tableGroupId;
        result.pages = input.pages;
        result.durationTime = totalDuration;
        result.modelName = modelName != null ? modelName : "UNKNOWN";

        if (markdownParts.isEmpty()) {
            result.status = "FAILED";
            result.markdownTable = "";
            result.errorMessage = String.format("All pages failed. Errors: %s", String.join("; ", errors));
        } else if (successfulPages.size() < input.pages.size()) {
            result.status = "PARTIAL_SUCCESS";
            result.markdownTable = String.join("\n\n", markdownParts);
            result.errorMessage = String.format("Only %d/%d pages succeeded. Errors: %s",
                    successfulPages.size(), input.pages.size(), String.join("; ", errors));
        } else {
            result.status = "SUCCESS";
            result.markdownTable = String.join("\n\n", markdownParts);
        }

        log.info(marker, "id#{}, Table extraction completed - Status: {}, Pages: {}/{}, Duration: {}s",
                actionId, result.status, successfulPages.size(), input.pages.size(), totalDuration);

        return result;
    }

    /**
     * Reads an image file and converts it to base64
     */
    private String readImageAsBase64(String filePath) throws Exception {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            throw new HandymanException(String.format("Failed to read image file %s: %s", filePath, e.getMessage()));
        }
    }

    // Data classes
    public static class TableGroupInput {
        public String originId;
        public String tenantId;
        public String tableGroupId;
        public List<Integer> pages;
        public List<String> filePaths;
        public Long processId;
        public String batchId;
        public Long rootPipelineId;
    }

    public static class TableExtractionResult {
        public String originId;
        public String tenantId;
        public String tableGroupId;
        public List<Integer> pages;
        public String markdownTable = "";
        public String status = "PENDING";
        public String modelName = "";
        public String extractionMethod = "MULTIPAGE_TABLE_EXTRACTION";
        public String errorMessage;
        public Double durationTime;
    }
}
