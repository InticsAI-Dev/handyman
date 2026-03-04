package in.handyman.raven.lib.model.tableextraction.coproprocessor;

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


public class TableExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<TableExtractionInputTable, TableExtractionOutputTable> {

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

    public TableExtractionConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<TableExtractionOutputTable> process(URL endpoint, TableExtractionInputTable entity) throws Exception {
        log.info(aMarker, "Table extraction consumer started for page {} of table group {}",
                entity.getPageNumber(), entity.getGroupId());

        List<TableExtractionOutputTable> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            // 1. Read and encode image file
            String base64Image = readAndEncodeImage(entity.getInputFilePath());

            // 2. Build request payload in Triton/KServe format
            String requestPayload = buildTritonPayload(entity, base64Image);

            log.info(aMarker, "Calling table extraction API for page {}", entity.getPageNumber());

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

                    // 4. Parse Triton/KServe response format
                    JsonNode responseJson = mapper.readTree(responseBody);

                    // Extract markdown table from Triton/KServe outputs array
                    String markdownTable = "";
                    String status = "SUCCESS";

                    if (responseJson.has("outputs") && responseJson.get("outputs").isArray() &&
                        responseJson.get("outputs").size() > 0) {
                        // Triton/KServe format: outputs[0].data[0] contains the result
                        JsonNode firstOutput = responseJson.get("outputs").get(0);
                        if (firstOutput.has("data") && firstOutput.get("data").isArray() &&
                            firstOutput.get("data").size() > 0) {
                            String dataStr = firstOutput.get("data").get(0).asText();
                            // Data is a JSON string containing infer_response field
                            try {
                                JsonNode dataJson = mapper.readTree(dataStr);
                                // Extract infer_response which contains the markdown table
                                markdownTable = dataJson.path("infer_response").asText("");
                                status = "SUCCESS";
                            } catch (Exception e) {
                                log.error(aMarker, "Failed to parse data JSON for page {}: {}",
                                        entity.getPageNumber(), e.getMessage());
                                markdownTable = dataStr;
                            }
                        }
                    } else {
                        // Fallback: try direct fields (old format)
                        markdownTable = responseJson.path("markdownTable").asText("");
                        status = responseJson.path("status").asText("SUCCESS");
                    }

                    log.info(aMarker, "Extracted markdown table (length={}), status={} for page {}",
                            markdownTable.length(), status, entity.getPageNumber());

                    // 5. Build output
                    results.add(TableExtractionOutputTable.builder()
                            .originId(entity.getOriginId())
                            .tenantId(entity.getTenantId())
                            .groupId(parseGroupId(entity.getGroupId()))
                            .pageNumber(entity.getPageNumber())
                            .markdownTable(markdownTable)
                            .status(status)
                            .modelName("")
                            .errorMessage(null)
                            .durationTime(durationSeconds)
                            .batchId(entity.getBatchId())
                            .processId(entity.getProcessId())
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .build());

                } else {
                    // API call failed
                    String errorMsg = response.message();
                    log.error(aMarker, "Table extraction failed for page {}: {}",
                            entity.getPageNumber(), errorMsg);

                    results.add(buildFailedResult(entity, errorMsg, 0.0));
                }
            }

        } catch (Exception e) {
            log.error(aMarker, "Exception in table extraction for page {}", entity.getPageNumber(), e);
            results.add(buildFailedResult(entity, e.getMessage(), 0.0));

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException(
                "Table extraction failed for page " + entity.getPageNumber(),
                handymanException,
                this.action
            );
        }

        return results;
    }

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


    private String buildTritonPayload(TableExtractionInputTable entity, String base64Image) throws Exception {
        // Build nested data structure
        ObjectNode nestedData = mapper.createObjectNode();
        nestedData.put("originId", entity.getOriginId());
        nestedData.put("tenantId", entity.getTenantId());
        nestedData.put("tableGroupId", entity.getGroupId());
        nestedData.put("pageNumber", entity.getPageNumber());
        nestedData.put("base64Img", base64Image);
        nestedData.put("userPrompt", entity.getUserPrompt());
        nestedData.put("systemPrompt", entity.getSystemPrompt());
        nestedData.put("process", "DATA_EXTRACTION");
        nestedData.put("processId", entity.getProcessId());
        nestedData.put("batchId", entity.getBatchId());
        nestedData.put("rootPipelineId", entity.getRootPipelineId());

        // Build Triton/KServe wrapper
        ObjectNode tritonPayload = mapper.createObjectNode();
        ObjectNode input = mapper.createObjectNode();
        input.put("name", "KRYPTON START");
        input.putArray("shape").add(1).add(1);
        input.put("datatype", "BYTES");
        input.putArray("data").add(nestedData.toString());

        tritonPayload.putArray("inputs").add(input);

        return mapper.writeValueAsString(tritonPayload);
    }


    private TableExtractionOutputTable buildFailedResult(TableExtractionInputTable entity,
                                                          String errorMessage,
                                                          double durationSeconds) {
        return TableExtractionOutputTable.builder()
                .originId(entity.getOriginId())
                .tenantId(entity.getTenantId())
                .groupId(parseGroupId(entity.getGroupId()))
                .pageNumber(entity.getPageNumber())
                .markdownTable(null)
                .status("FAILED")
                .modelName("")
                .errorMessage(errorMessage)
                .durationTime(durationSeconds)
                .batchId(entity.getBatchId())
                .processId(entity.getProcessId())
                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                .build();
    }

    private Long parseGroupId(String groupId) {
        if (groupId == null || groupId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(groupId);
        } catch (NumberFormatException ex) {
            log.warn(aMarker, "Invalid groupId '{}', defaulting to 0", groupId);
            return 0L;
        }
    }
}
