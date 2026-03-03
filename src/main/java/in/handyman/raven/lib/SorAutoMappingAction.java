package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.handlers.CoproApiHandler;
import in.handyman.raven.lib.model.SorAutoMapping;
import in.handyman.raven.lib.model.sorautomapping.AutoMatchRequest;
import in.handyman.raven.lib.model.sorautomapping.AutoMatchResponse;
import in.handyman.raven.lib.model.sorautomapping.ExtractedField;
import in.handyman.raven.lib.model.sorautomapping.TargetSorItem;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SOR Auto-Mapping Action
 *
 * Automatically matches extracted fields (KVP/Table/Checkbox) to SOR items using
 * Copro's LLM-based semantic matching service.
 *
 * Flow:
 * 1. Fetch extracted fields from sor_transaction.llm_json_parser_output_audit
 * 2. Fetch target SOR items for document type from sor_meta
 * 3. Call Copro /sor/auto-match-fields API
 * 4. High confidence matches (>80%) automatically stored in sor_aggregated_data
 * 5. Medium confidence matches (60-80%) added to validation queue for user review
 * 6. Low confidence matches (<60%) flagged for manual mapping
 */
@ActionExecution(actionName = "SorAutoMapping")
public class SorAutoMappingAction implements IActionExecution {

    private final ActionExecutionAudit action;
    private final Logger log;
    private final SorAutoMapping sorAutoMapping;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final CoproApiHandler coproApiHandler;

    public SorAutoMappingAction(final ActionExecutionAudit action,
                                final Logger log,
                                final Object sorAutoMapping) {
        this.sorAutoMapping = (SorAutoMapping) sorAutoMapping;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("SorAutoMapping:" + this.sorAutoMapping.getName());
        this.objectMapper = new ObjectMapper();
        this.coproApiHandler = new CoproApiHandler();
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "SOR Auto-Mapping started for {}", sorAutoMapping.getName());

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(sorAutoMapping.getResourceConn());

            // Step 1: Extract context values
            String originId = action.getContext().get("origin.id");
            String tenantId = action.getContext().get("tenant.id");
            String batchId = action.getContext().get("batch.id");
            String rootPipelineId = action.getContext().get("root.pipeline.id");
            String documentType = action.getContext().get("document.type");
            String sorContainerId = action.getContext().get("sor.container.id");

            log.info(aMarker, "Processing SOR auto-mapping for originId={}, documentType={}",
                    originId, documentType);

            // Step 2: Fetch extracted KVP fields
            List<ExtractedField> extractedFields = fetchExtractedKvpFields(jdbi, originId,
                    Long.parseLong(tenantId), batchId);

            log.info(aMarker, "Fetched {} extracted fields", extractedFields.size());

            if (extractedFields.isEmpty()) {
                log.warn(aMarker, "No extracted fields found for originId={}. Skipping auto-mapping.", originId);
                action.getContext().put(sorAutoMapping.getName() + ".status", "NO_FIELDS");
                return;
            }

            // Step 3: Fetch target SOR items
            List<TargetSorItem> targetSorItems = fetchTargetSorItems(jdbi, Long.parseLong(tenantId),
                    Long.parseLong(sorContainerId), documentType);

            log.info(aMarker, "Fetched {} target SOR items", targetSorItems.size());

            if (targetSorItems.isEmpty()) {
                log.warn(aMarker, "No target SOR items configured for documentType={}. Skipping auto-mapping.",
                        documentType);
                action.getContext().put(sorAutoMapping.getName() + ".status", "NO_SOR_ITEMS");
                return;
            }

            // Step 4: Build request payload
            AutoMatchRequest request = AutoMatchRequest.builder()
                    .originId(originId)
                    .tenantId(Long.parseLong(tenantId))
                    .batchId(batchId)
                    .rootPipelineId(rootPipelineId != null ? Long.parseLong(rootPipelineId) : null)
                    .documentType(documentType)
                    .sorContainerId(Long.parseLong(sorContainerId))
                    .extractedFields(extractedFields)
                    .targetSorItems(targetSorItems)
                    .modelName(sorAutoMapping.getModelName())
                    .build();

            // Step 5: Call Copro API
            AutoMatchResponse response = callCoproAutoMatchAPI(request);

            // Step 6: Update context with results
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                action.getContext().put(sorAutoMapping.getName() + ".status", "SUCCESS");
                action.getContext().put(sorAutoMapping.getName() + ".high.confidence.count",
                        String.valueOf(response.getHighConfidenceMatches().size()));
                action.getContext().put(sorAutoMapping.getName() + ".validation.queue.count",
                        String.valueOf(response.getValidationQueueMatches().size()));
                action.getContext().put(sorAutoMapping.getName() + ".manual.mapping.count",
                        String.valueOf(response.getManualMappingNeeded().size()));
                action.getContext().put(sorAutoMapping.getName() + ".validation.needed",
                        String.valueOf(response.getValidationNeeded()));

                log.info(aMarker, "SOR auto-mapping completed successfully: {} high confidence, {} validation queue, {} manual",
                        response.getHighConfidenceMatches().size(),
                        response.getValidationQueueMatches().size(),
                        response.getManualMappingNeeded().size());
            } else {
                action.getContext().put(sorAutoMapping.getName() + ".status", "FAILED");
                String errorMsg = response != null ? response.getErrorMessage() : "Unknown error";
                action.getContext().put(sorAutoMapping.getName() + ".error", errorMsg);
                log.error(aMarker, "SOR auto-mapping failed: {}", errorMsg);
            }

        } catch (Exception e) {
            action.getContext().put(sorAutoMapping.getName() + ".status", "ERROR");
            action.getContext().put(sorAutoMapping.getName() + ".error", ExceptionUtil.toString(e));
            log.error(aMarker, "Error in SOR auto-mapping execution", e);
            throw new HandymanException("Error in SOR auto-mapping execution", e, action);
        }
    }

    /**
     * Fetch extracted KVP fields from llm_json_parser_output_audit table
     */
    private List<ExtractedField> fetchExtractedKvpFields(Jdbi jdbi, String originId, Long tenantId, String batchId) {
        String query = "SELECT DISTINCT " +
                "    sor_item_label as label, " +
                "    answer as value, " +
                "    'KVP' as source_type, " +
                "    paper_no as page_number, " +
                "    confidence, " +
                "    bbox " +
                "FROM sor_transaction.llm_json_parser_output_audit " +
                "WHERE origin_id = :originId " +
                "  AND tenant_id = :tenantId " +
                "  AND batch_id = :batchId " +
                "  AND sor_item_label IS NOT NULL " +
                "  AND sor_item_label != '' " +
                "  AND answer IS NOT NULL " +
                "ORDER BY paper_no, sor_item_label";

        return jdbi.withHandle(handle ->
                handle.createQuery(query)
                        .bind("originId", originId)
                        .bind("tenantId", tenantId)
                        .bind("batchId", batchId)
                        .map((rs, ctx) -> ExtractedField.builder()
                                .label(rs.getString("label"))
                                .value(rs.getString("value"))
                                .sourceType(rs.getString("source_type"))
                                .pageNumber(rs.getInt("page_number"))
                                .confidence(rs.getObject("confidence") != null ? rs.getDouble("confidence") : null)
                                .bbox(rs.getObject("bbox"))
                                .build())
                        .list()
        );
    }

    /**
     * Fetch target SOR items for the document type
     */
    private List<TargetSorItem> fetchTargetSorItems(Jdbi jdbi, Long tenantId, Long sorContainerId, String documentType) {
        String query = "SELECT " +
                "    si.sor_item_id, " +
                "    si.sor_item_name, " +
                "    si.field_type " +
                "FROM sor_meta.sor_item si " +
                "JOIN sor_meta.sor_container sc ON si.sor_container_id = sc.sor_container_id " +
                "WHERE si.tenant_id = :tenantId " +
                "  AND si.sor_container_id = :sorContainerId " +
                "  AND sc.document_type = :documentType " +
                "  AND si.status = 'ACTIVE' " +
                "ORDER BY si.sor_item_name";

        return jdbi.withHandle(handle ->
                handle.createQuery(query)
                        .bind("tenantId", tenantId)
                        .bind("sorContainerId", sorContainerId)
                        .bind("documentType", documentType)
                        .map((rs, ctx) -> TargetSorItem.builder()
                                .sorItemId(rs.getLong("sor_item_id"))
                                .sorItemName(rs.getString("sor_item_name"))
                                .fieldType(rs.getString("field_type"))
                                .knownLabels(new ArrayList<>()) // Populated by Copro from learned patterns
                                .build())
                        .list()
        );
    }

    /**
     * Call Copro auto-match API
     */
    private AutoMatchResponse callCoproAutoMatchAPI(AutoMatchRequest request) throws Exception {
        URL url = new URL(sorAutoMapping.getCoproEndpoint());

        // Build JSON payload
        String jsonRequest = coproApiHandler.buildInputPayload(request, objectMapper);

        log.debug(aMarker, "Calling Copro API: {} with payload: {}", url, jsonRequest);

        // Build HTTP request
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request httpRequest = coproApiHandler.buildRequestApiObject(url, jsonRequest, mediaType, null);

        // Create OkHttpClient with timeouts
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(sorAutoMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(sorAutoMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(sorAutoMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        // Execute request with retry logic
        int retries = 0;
        Exception lastException = null;

        while (retries < sorAutoMapping.getMaxRetries()) {
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    log.debug(aMarker, "Copro API response: {}", responseBody);

                    AutoMatchResponse matchResponse = objectMapper.readValue(responseBody, AutoMatchResponse.class);
                    log.info(aMarker, "Successfully received auto-match response from Copro");
                    return matchResponse;
                } else {
                    String errorMsg = response.body() != null ? response.body().string() : "Empty response";
                    log.warn(aMarker, "Copro API call failed with status {}: {}", response.code(), errorMsg);
                    lastException = new Exception("HTTP " + response.code() + ": " + errorMsg);
                }
            } catch (Exception e) {
                log.warn(aMarker, "Copro API call attempt {} failed: {}", retries + 1, e.getMessage());
                lastException = e;
            }

            retries++;
            if (retries < sorAutoMapping.getMaxRetries()) {
                Thread.sleep(1000 * retries); // Exponential backoff
            }
        }

        throw new HandymanException("Copro API call failed after " + sorAutoMapping.getMaxRetries() + " retries",
                lastException, action);
    }

    @Override
    public boolean executeIf() throws Exception {
        return sorAutoMapping.getCondition();
    }
}
