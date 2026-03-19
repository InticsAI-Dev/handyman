package in.handyman.raven.lib.model.common.copro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Shared utility for parsing copro inference responses.
 * Single source of truth used by both sync ConsumerProcess classes and
 * async CoproResultWriterService.
 */
public final class CoproResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(CoproResponseParser.class);
    private static final String MODEL_TYPE = "OPTIMUS";
    private static final String PROCESS_NAME = "AGENTIC PAPER FILTER";
    private static final String MODEL = "model";
    private static final String PAGE_CONTENT_NO = "no";
    private static final String PAGE_CONTENT_YES = "yes";

    private CoproResponseParser() {
    }

    /**
     * Parses an agentic paper filter copro response into output rows.
     * Extracted from AgenticPaperFilterConsumerProcess: extractedKryptonOutputDataRequest()
     * + doOptimusParentObjectBuild() + doKryptonParentObjBuild().
     *
     * @param rawResponse          the raw JSON string from copro (may have markdown fences)
     * @param ctx                  context fields for building output rows
     * @param pageContentMinLength threshold for blank page detection
     * @param mapper               ObjectMapper instance
     * @return list of output rows; empty list if inferResponse is null (caller handles error)
     */
    public static List<AgenticPaperFilterOutput> parseAgenticFilterResponse(
            String rawResponse,
            AgenticFilterContext ctx,
            int pageContentMinLength,
            ObjectMapper mapper) throws JsonProcessingException {

        List<AgenticPaperFilterOutput> results = new ArrayList<>();

        String cleanedJson = rawResponse.replace("```json", "").replace("```", "").trim();

        RadonKvpLineItem dataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);
        String inferResponse = dataItem.getInferResponse();

        // 3. Return empty list if inferResponse is null (caller handles error)
        if (inferResponse == null) {
            return results;
        }

        // 4. Detect model from "model" field → OPTIMUS vs Krypton
        JSONObject json = new JSONObject(cleanedJson);
        String modelValue = json.has(MODEL) ? json.getString(MODEL) : null;
        boolean isOptimus = MODEL_TYPE.equalsIgnoreCase(modelValue);

        JsonNode inferResponseNode;
        if (modelValue == null) {
            // No model field — default to Krypton behavior but log it
            logger.warn("No '{}' field in copro response — defaulting to Krypton parsing", MODEL);
            inferResponseNode = mapper.readTree(inferResponse);
        } else if (isOptimus) {
            // OPTIMUS — wrap as text node
            inferResponseNode = TextNode.valueOf(inferResponse.trim());
        } else {
            // Explicit Krypton or any other model value
            inferResponseNode = mapper.readTree(inferResponse);
        }

        // 5. Blank page detection
        String isBlankPage = (inferResponse.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;

        // 6. Build output based on model type
        if (isOptimus) {
            // OPTIMUS: single row
            buildOptimusOutput(dataItem, ctx, inferResponseNode, isBlankPage, results);
        } else {
            // KRYPTON: one row per field entry
            buildKryptonOutput(dataItem, ctx, inferResponseNode, isBlankPage, results);
        }

        return results;
    }

    private static void buildOptimusOutput(RadonKvpLineItem dataItem,
                                           AgenticFilterContext agenticFilterContext,
                                           JsonNode inferResponseNode,
                                           String isBlankPage,
                                           List<AgenticPaperFilterOutput> results) {
        Long groupId = dataItem.getGroupId();
        results.add(AgenticPaperFilterOutput.builder()
                .filePath(agenticFilterContext.getFilePath())
                .originId(dataItem.getOriginId())
                .groupId(groupId != null ? Math.toIntExact(groupId) : 0)
                .paperNo(dataItem.getPaperNo())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(PROCESS_NAME)
                .message("Agentic Paper Filter macro completed with optimus triton api call " + agenticFilterContext.getModelName())
                .createdOn(agenticFilterContext.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .isBlankPage(isBlankPage)
                .tenantId(dataItem.getTenantId())
                .templateId(agenticFilterContext.getTemplateId())
                .processId(dataItem.getProcessId())
                .templateName(agenticFilterContext.getTemplateName())
                .rootPipelineId(dataItem.getRootPipelineId())
                .modelName(agenticFilterContext.getModelName())
                .modelVersion(agenticFilterContext.getModelVersion())
                .batchId(agenticFilterContext.getBatchId())
                .endpoint(agenticFilterContext.getEndpoint())
                .containerValue("yes".equalsIgnoreCase(inferResponseNode.asText()) ? "true" : "false")
                .containerName(agenticFilterContext.getUniqueName())
                .containerId(agenticFilterContext.getUniqueId())
                .promptType(agenticFilterContext.getPromptType())
                .build());
    }

    private static void buildKryptonOutput(RadonKvpLineItem dataItem,
                                           AgenticFilterContext agenticFilterContext,
                                           JsonNode inferResponseNode,
                                           String isBlankPage,
                                           List<AgenticPaperFilterOutput> results) {
        Iterator<Map.Entry<String, JsonNode>> fields = inferResponseNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String containerName = entry.getKey();
            String containerValue = entry.getValue().asText();

            results.add(AgenticPaperFilterOutput.builder()
                    .filePath(agenticFilterContext.getFilePath())
                    .originId(dataItem.getOriginId())
                    .groupId(dataItem.getGroupId() != null ? Math.toIntExact(dataItem.getGroupId()) : null)
                    .paperNo(dataItem.getPaperNo())
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message("Agentic Paper Filter macro completed with krypton triton api call " + agenticFilterContext.getModelName())
                    .createdOn(agenticFilterContext.getCreatedOn())
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .isBlankPage(isBlankPage)
                    .tenantId(dataItem.getTenantId())
                    .templateId(agenticFilterContext.getTemplateId())
                    .processId(dataItem.getProcessId())
                    .templateName(agenticFilterContext.getTemplateName())
                    .rootPipelineId(dataItem.getRootPipelineId())
                    .modelName(agenticFilterContext.getModelName())
                    .modelVersion(agenticFilterContext.getModelVersion())
                    .batchId(agenticFilterContext.getBatchId())
                    .endpoint(agenticFilterContext.getEndpoint())
                    .containerName(containerName)
                    .containerValue(containerValue)
                    .build());
        }
    }

    /**
     * Parses a radon KVP copro response into output rows (non-post-processing path).
     * Extracted from RadonKvpConsumerProcess.extractTritonOutputDataResponse().
     *
     * @param rawResponse    the raw JSON string from copro
     * @param radonKvpContext            context fields for building output rows
     * @param encryption     InticsIntegrity instance (nullable)
     * @param encryptItemWise whether item-wise encryption is enabled
     * @param mapper         ObjectMapper instance
     * @return list of output rows
     */
    public static List<RadonQueryOutputTable> parseRadonKvpResponse(
            String rawResponse,
            RadonKvpContext radonKvpContext,
            InticsIntegrity encryption,
            boolean encryptItemWise,
            ObjectMapper mapper) throws JsonProcessingException {

        List<RadonQueryOutputTable> results = new ArrayList<>();

        // 1. Deserialize to RadonKvpLineItem, get inferResponse
        RadonKvpLineItem modelResponse = mapper.readValue(rawResponse, RadonKvpLineItem.class);
        String extractedContent;

        // 2. If encryptItemWise && encryption != null → encrypt
        if (encryptItemWise && encryption != null) {
            extractedContent = encryption.encrypt(modelResponse.getInferResponse(), "AES256", "RADON_KVP_JSON");
        } else {
            extractedContent = modelResponse.getInferResponse();
        }

        // 3. Build RadonQueryOutputTable
        String sorContainerInstance = radonKvpContext.getSorContainerName() != null
                ? radonKvpContext.getSorContainerName() + "_0" : null;

        results.add(RadonQueryOutputTable.builder()
                .createdOn(radonKvpContext.getCreatedOn())
                .createdUserId(radonKvpContext.getTenantId())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(radonKvpContext.getTenantId())
                .originId(radonKvpContext.getOriginId())
                .paperNo(radonKvpContext.getPaperNo())
                .totalResponseJson(extractedContent)
                .groupId(radonKvpContext.getGroupId())
                .inputFilePath(radonKvpContext.getInputFilePath())
                .actionId(radonKvpContext.getActionId())
                .tenantId(radonKvpContext.getTenantId())
                .processId(radonKvpContext.getProcessId())
                .rootPipelineId(radonKvpContext.getRootPipelineId())
                .process(radonKvpContext.getProcess())
                .batchId(radonKvpContext.getBatchId())
                .modelRegistry(radonKvpContext.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(radonKvpContext.getApiName())
                .category(radonKvpContext.getCategory())
                .message("Radon kvp action macro completed")
                .sorContainerId(radonKvpContext.getSorContainerId())
                .endpoint(radonKvpContext.getEndpoint())
                .sorContainerInstance(sorContainerInstance)
                .build());

        return results;
    }

    /**
     * Builds a failed agentic filter output row.
     * <p>Intended for callers (e.g. CoproResultWriterService) that need a standard
     * error row when {@link #parseAgenticFilterResponse} returns an empty list.
     * The sync path (AgenticPaperFilterConsumerProcess) uses its own handleKryptonErrorResponse()
     * which includes HTTP response details.</p>
     *
     * @param agenticFilterContext context fields
     * @param errorMessage         the error message
     * @return a single failed output entity
     */
    public static AgenticPaperFilterOutput buildFailedAgenticOutput(
            AgenticFilterContext agenticFilterContext, String errorMessage) {
        return AgenticPaperFilterOutput.builder()
                .originId(agenticFilterContext.getOriginId())
                .batchId(agenticFilterContext.getBatchId())
                .groupId(agenticFilterContext.getGroupId() != null ? Math.toIntExact(agenticFilterContext.getGroupId()) : null)
                .paperNo(agenticFilterContext.getPaperNo())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .stage(PROCESS_NAME)
                .tenantId(agenticFilterContext.getTenantId())
                .templateId(agenticFilterContext.getTemplateId())
                .processId(agenticFilterContext.getProcessId())
                .message(errorMessage)
                .createdOn(agenticFilterContext.getCreatedOn())
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .rootPipelineId(agenticFilterContext.getRootPipelineId())
                .templateName(agenticFilterContext.getTemplateName())
                .endpoint(agenticFilterContext.getEndpoint())
                .build();
    }
}
