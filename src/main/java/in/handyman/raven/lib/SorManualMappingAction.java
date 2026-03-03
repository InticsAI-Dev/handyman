package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.handlers.CoproApiHandler;
import in.handyman.raven.lib.model.SorManualMapping;
import in.handyman.raven.lib.model.sormanualmapping.ManualMappingRequest;
import in.handyman.raven.lib.model.sormanualmapping.ManualMappingResponse;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * SOR Manual Mapping Action
 *
 * Handles user-driven mapping of extracted fields to SOR items via UI.
 * When a user selects a value from the SOT (Source of Truth) tab and maps it to a SOR item,
 * this action sends the mapping to Copro to be stored and learned.
 *
 * Flow:
 * 1. User selects extracted field value from UI (SOT tab)
 * 2. User maps it to a target SOR item
 * 3. Frontend calls backend API which triggers this action
 * 4. Action calls Copro /sor/manual-mapping API
 * 5. Copro stores mapping in field_source_mapping table
 * 6. Database trigger updates learned_label_patterns for future auto-mapping
 * 7. Synonym added to sor_meta.sor_tsynonym for backward compatibility
 */
@ActionExecution(actionName = "SorManualMapping")
public class SorManualMappingAction implements IActionExecution {

    private final ActionExecutionAudit action;
    private final Logger log;
    private final SorManualMapping sorManualMapping;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final CoproApiHandler coproApiHandler;

    public SorManualMappingAction(final ActionExecutionAudit action,
                                  final Logger log,
                                  final Object sorManualMapping) {
        this.sorManualMapping = (SorManualMapping) sorManualMapping;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("SorManualMapping:" + this.sorManualMapping.getName());
        this.objectMapper = new ObjectMapper();
        this.coproApiHandler = new CoproApiHandler();
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "SOR Manual Mapping started for {}", sorManualMapping.getName());

            // Step 1: Extract mapping details from context
            String originId = action.getContext().get("origin.id");
            String tenantId = action.getContext().get("tenant.id");
            String documentType = action.getContext().get("document.type");
            String sorContainerId = action.getContext().get("sor.container.id");
            String sorItemId = action.getContext().get("sor.item.id");
            String sorItemName = action.getContext().get("sor.item.name");
            String sourceType = action.getContext().get("source.type");
            String sourceLabel = action.getContext().get("source.label");
            String sourceValue = action.getContext().get("source.value");
            String pageNumber = action.getContext().get("page.number");
            String sectionHint = action.getContext().get("section.hint");
            String userId = action.getContext().get("user.id");

            log.info(aMarker, "Creating manual mapping: {} → {} for originId={}",
                    sourceLabel, sorItemName, originId);

            // Step 2: Build request
            ManualMappingRequest request = ManualMappingRequest.builder()
                    .originId(originId)
                    .tenantId(Long.parseLong(tenantId))
                    .documentType(documentType)
                    .sorContainerId(Long.parseLong(sorContainerId))
                    .sorItemId(Long.parseLong(sorItemId))
                    .sorItemName(sorItemName)
                    .sourceType(sourceType)
                    .sourceLabel(sourceLabel)
                    .sourceValue(sourceValue)
                    .pageNumber(pageNumber != null ? Integer.parseInt(pageNumber) : null)
                    .sectionHint(sectionHint)
                    .createdByUserId(userId != null ? Long.parseLong(userId) : null)
                    .build();

            // Step 3: Call Copro API
            ManualMappingResponse response = callCoproManualMappingAPI(request);

            // Step 4: Update context with results
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                action.getContext().put(sorManualMapping.getName() + ".status", "SUCCESS");
                action.getContext().put(sorManualMapping.getName() + ".mapping.id",
                        String.valueOf(response.getMappingId()));
                action.getContext().put(sorManualMapping.getName() + ".pattern.updated",
                        String.valueOf(response.getPatternUpdated()));

                log.info(aMarker, "Manual mapping created successfully: mappingId={}, patternUpdated={}",
                        response.getMappingId(), response.getPatternUpdated());
            } else {
                action.getContext().put(sorManualMapping.getName() + ".status", "FAILED");
                String errorMsg = response != null ? response.getMessage() : "Unknown error";
                action.getContext().put(sorManualMapping.getName() + ".error", errorMsg);
                log.error(aMarker, "Manual mapping failed: {}", errorMsg);
            }

        } catch (Exception e) {
            action.getContext().put(sorManualMapping.getName() + ".status", "ERROR");
            action.getContext().put(sorManualMapping.getName() + ".error", ExceptionUtil.toString(e));
            log.error(aMarker, "Error in manual mapping execution", e);
            throw new HandymanException("Error in manual mapping execution", e, action);
        }
    }

    /**
     * Call Copro manual mapping API
     */
    private ManualMappingResponse callCoproManualMappingAPI(ManualMappingRequest request) throws Exception {
        URL url = new URL(sorManualMapping.getCoproEndpoint());

        // Build JSON payload
        String jsonRequest = coproApiHandler.buildInputPayload(request, objectMapper);

        log.debug(aMarker, "Calling Copro API: {} with payload: {}", url, jsonRequest);

        // Build HTTP request
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request httpRequest = coproApiHandler.buildRequestApiObject(url, jsonRequest, mediaType, null);

        // Create OkHttpClient with timeout
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(sorManualMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(sorManualMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(sorManualMapping.getTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        // Execute request
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                log.debug(aMarker, "Copro API response: {}", responseBody);

                ManualMappingResponse mappingResponse = objectMapper.readValue(responseBody, ManualMappingResponse.class);
                log.info(aMarker, "Successfully received manual mapping response from Copro");
                return mappingResponse;
            } else {
                String errorMsg = response.body() != null ? response.body().string() : "Empty response";
                log.error(aMarker, "Copro API call failed with status {}: {}", response.code(), errorMsg);
                throw new Exception("HTTP " + response.code() + ": " + errorMsg);
            }
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return sorManualMapping.getCondition();
    }
}
