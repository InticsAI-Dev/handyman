package in.handyman.raven.lib.model.checkboxextraction.coproprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Consumer process for checkbox extraction using Krypton (Qwen VLM model)
 * Processes one page at a time and returns JSON checkbox data
 */
public class CheckboxExtractionConsumerProcess
        implements CoproProcessor.ConsumerProcess<CheckboxExtractionInputTable, CheckboxExtractionOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public CheckboxExtractionConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<CheckboxExtractionOutputTable> process(URL endpoint, CheckboxExtractionInputTable entity)
            throws Exception {
        log.info(aMarker, "Checkbox extraction consumer started for page {}",
                entity.getPageNumber());

        List<CheckboxExtractionOutputTable> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            // 1. Read and encode image file
            String base64Image = readAndEncodeImage(entity.getInputFilePath());

            // 2. Build request payload for COPRO /extract-checkbox endpoint
            String requestPayload = buildCheckboxExtractionPayload(entity, base64Image);

            log.info(aMarker, "Calling checkbox extraction API for page {}", entity.getPageNumber());

            // 3. Call Copro API
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(requestPayload, MediaTypeJSON))
                    .build();

            try (Response response = httpclient.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                double durationSeconds = (endTime - startTime) / 1000.0;

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    // 4. Parse response from COPRO /extract-checkbox endpoint
                    JsonNode responseJson = mapper.readTree(responseBody);

                    // Extract checkbox data (JSON string)
                    String checkboxData = responseJson.path("checkboxData").asText("[]");
                    String status = normalizeStatus(responseJson.path("status").asText("COMPLETED"));
                    String modelName = responseJson.path("modelName").asText("KRYPTON_MODEL");
                    String errorMessage = responseJson.path("errorMessage").asText("");

                    log.info(aMarker, "Extracted checkbox data (length={}), status={} for page {}",
                            checkboxData.length(), status, entity.getPageNumber());

                    // Validate that checkboxData is valid JSON
                    try {
                        mapper.readTree(checkboxData); // Just validate
                    } catch (Exception e) {
                        log.warn(aMarker, "Invalid JSON in checkbox data for page {}: {}",
                                entity.getPageNumber(), e.getMessage());
                    }

                    // 5. Build output
                    results.add(CheckboxExtractionOutputTable.builder()
                            .originId(entity.getOriginId())
                            .groupId(parseGroupId(entity.getGroupId()))
                            .tenantId(entity.getTenantId())
                            .batchId(entity.getBatchId())
                            .rootPipelineId(entity.getRootPipelineId())
                            .pageNumber(entity.getPageNumber())
                            .checkboxData(checkboxData)
                            .status(status)
                            .modelName(modelName)
                            .errorMessage(errorMessage)
                            .durationTime(durationSeconds)
                            .processId(entity.getProcessId())
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .stage("CHECKBOX_EXTRACTION")
                            .build());

                } else {
                    // API call failed
                    String errorMsg = response.message();
                    log.error(aMarker, "Checkbox extraction failed for page {}: {}",
                            entity.getPageNumber(), errorMsg);

                    results.add(buildFailedResult(entity, errorMsg, 0.0));
                }
            }

        } catch (Exception e) {
            log.error(aMarker, "Exception in checkbox extraction for page {}", entity.getPageNumber(), e);
            results.add(buildFailedResult(entity, e.getMessage(), 0.0));

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(
                    "Checkbox extraction failed for page " + entity.getPageNumber(),
                    handymanException,
                    this.action);
        }

        return results;
    }

    /**
     * Read image file and encode to base64
     */
    private String readAndEncodeImage(String filePath) throws Exception {
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            HandymanException handymanException = new HandymanException("Image file not found: " + filePath);
            HandymanException.insertException("Image file not found: " + filePath, handymanException, action);
            throw handymanException;
        }

        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Build request payload for COPRO /extract-checkbox endpoint.
     * Aligned with table extraction: tenantId as string,
     * processId/rootPipelineId/actionId as integers (Copro API contract).
     */
    private String buildCheckboxExtractionPayload(CheckboxExtractionInputTable entity, String base64Image)
            throws Exception {
        ObjectNode payload = mapper.createObjectNode();

        payload.put("originId", entity.getOriginId());
        payload.put("tenantId", entity.getTenantId() != null ? String.valueOf(entity.getTenantId()) : null);
        payload.put("pageNumber", entity.getPageNumber() != null ? entity.getPageNumber() : 1);
        payload.put("base64Image", base64Image);
        payload.put("userPrompt", entity.getUserPrompt() != null ? entity.getUserPrompt()
                : "Extract all checkboxes from this image. For each checkbox, provide: " +
                        "1. The label or description associated with the checkbox " +
                        "2. The state (checked or unchecked) " +
                        "3. Any additional context if available " +
                        "Return the results as a JSON array with this format: " +
                        "[{\"label\": \"checkbox label\", \"checked\": true/false, \"context\": \"optional context\"}]");

        if (entity.getSystemPrompt() != null) {
            payload.put("systemPrompt", entity.getSystemPrompt());
        }

        payload.put("modelName", "KRYPTON_MODEL");

        if (entity.getProcessId() != null && !entity.getProcessId().isEmpty()) {
            try {
                payload.put("processId", Integer.parseInt(entity.getProcessId()));
            } catch (NumberFormatException e) {
                log.warn(aMarker, "processId not a valid integer: {}", entity.getProcessId());
            }
        }
        if (entity.getBatchId() != null) {
            payload.put("batchId", entity.getBatchId());
        }
        if (entity.getRootPipelineId() != null) {
            payload.put("rootPipelineId", entity.getRootPipelineId().intValue());
        }
        if (action.getActionId() != null) {
            payload.put("actionId", action.getActionId().intValue());
        }

        return mapper.writeValueAsString(payload);
    }

    /**
     * Build failed result output
     */
    private CheckboxExtractionOutputTable buildFailedResult(CheckboxExtractionInputTable entity,
            String errorMessage,
            double durationSeconds) {
        return CheckboxExtractionOutputTable.builder()
                .originId(entity.getOriginId())
                .groupId(parseGroupId(entity.getGroupId()))
                .rootPipelineId(entity.getRootPipelineId())
                .tenantId(entity.getTenantId())
                .pageNumber(entity.getPageNumber())
                .checkboxData("[]") // Empty JSON array for failed extraction
                .status("FAILED")
                .modelName("KRYPTON_MODEL")
                .errorMessage(errorMessage)
                .durationTime(durationSeconds)
                .batchId(entity.getBatchId())
                .processId(entity.getProcessId())
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .stage("CHECKBOX_EXTRACTION")
                .build();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "COMPLETED";
        }
        if ("SUCCESS".equalsIgnoreCase(status)) {
            return "COMPLETED";
        }
        return status.toUpperCase();
    }

    private Long parseGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(groupId.trim());
        } catch (NumberFormatException e) {
            log.warn(aMarker, "Invalid groupId '{}' for checkbox extraction output. Falling back to 0.", groupId);
            return 0L;
        }
    }
}
