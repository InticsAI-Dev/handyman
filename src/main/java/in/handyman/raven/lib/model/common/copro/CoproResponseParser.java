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
import in.handyman.raven.lib.model.deep.sift.DeepSiftOutputTable;
import in.handyman.raven.lib.adapters.scalar.WordCountAdapter;
import in.handyman.raven.lib.model.deep.sift.XenonResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
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
     */
    public static List<AgenticPaperFilterOutput> parseAgenticFilterResponse(
            String rawResponse,
            AgenticFilterContext ctx,
            int pageContentMinLength,
            ObjectMapper mapper) throws JsonProcessingException {

        List<AgenticPaperFilterOutput> results = new ArrayList<>();

        String cleanedJson = rawResponse.replace("```json", "").replace("```", "").trim();

        RadonKvpLineItem dataExtractionDataItem = mapper.readValue(cleanedJson, RadonKvpLineItem.class);
        String inferResponse = dataExtractionDataItem.getInferResponse();

        if (inferResponse == null) {
            return results;
        }

        JSONObject json = new JSONObject(cleanedJson);
        String modelValue = json.has(MODEL) ? json.getString(MODEL) : null;
        boolean isOptimus = MODEL_TYPE.equalsIgnoreCase(modelValue);

        JsonNode inferResponseNode;
        if (modelValue == null) {
            logger.warn("No '{}' field in copro response — defaulting to Krypton parsing", MODEL);
            inferResponseNode = mapper.readTree(inferResponse);
        } else if (isOptimus) {
            inferResponseNode = TextNode.valueOf(inferResponse.trim());
        } else {
            inferResponseNode = mapper.readTree(inferResponse);
        }

        String isBlankPage = (inferResponse.length() > pageContentMinLength) ? PAGE_CONTENT_NO : PAGE_CONTENT_YES;

        if (isOptimus) {
            buildOptimusOutput(dataExtractionDataItem, ctx, inferResponseNode, isBlankPage, results);
        } else {
            buildKryptonOutput(dataExtractionDataItem, ctx, inferResponseNode, isBlankPage, results);
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
     */
    public static List<RadonQueryOutputTable> parseRadonKvpResponse(
            String rawResponse,
            RadonKvpContext radonKvpContext,
            InticsIntegrity encryption,
            boolean encryptItemWise,
            ObjectMapper mapper) throws JsonProcessingException {

        List<RadonQueryOutputTable> results = new ArrayList<>();

        RadonKvpLineItem modelResponse = mapper.readValue(rawResponse, RadonKvpLineItem.class);
        String extractedContent;

        if (encryptItemWise && encryption != null) {
            extractedContent = encryption.encrypt(modelResponse.getInferResponse(), "AES256", "RADON_KVP_JSON");
        } else {
            extractedContent = modelResponse.getInferResponse();
        }

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

    /**
     * Parses a deep sift copro response into output rows.
     */
    public static List<DeepSiftOutputTable> parseDeepSiftResponse(
            String rawResponse,
            DeepSiftContext deepSiftContext,
            int blankPageThreshold,
            WordCountAdapter wordCountAdapter,
            InticsIntegrity encryption,
            boolean encryptOutput,
            boolean encryptRequestResponse,
            ObjectMapper mapper) throws JsonProcessingException {

        List<DeepSiftOutputTable> results = new ArrayList<>();

        XenonResponse modelResponse = mapper.readValue(rawResponse, XenonResponse.class);

        if (!modelResponse.isSuccess() || !modelResponse.hasInferResponse()) {
            return results;
        }

        String extractedContent = modelResponse.getInferResponse();

        int wordCount;
        try {
            wordCount = wordCountAdapter.getThresholdScore(extractedContent);
        } catch (Exception e) {
            logger.error("Error computing word count for originId: {}, paperNo: {}", deepSiftContext.getOriginId(), deepSiftContext.getPaperNo(), e);
            wordCount = 0;
        }

        boolean isBlankPage = wordCount < blankPageThreshold;

        String finalExtractedContent = extractedContent;
        if (encryptOutput && encryption != null) {
            finalExtractedContent = encryption.encrypt(extractedContent, "AES256", "TEXT_DATA_TYPE");
        }

        if (modelResponse.getOriginId() == null || modelResponse.getGroupId() == null ||
                modelResponse.getTenantId() == null || modelResponse.getRootPipelineId() == null) {
            logger.error("Invalid response from model {}: missing required fields", deepSiftContext.getModelName());
            return results;
        }

        Timestamp createdOn = deepSiftContext.getCreatedOn();

        long elapsedTimeMs = 0;
        if (createdOn != null) {
            elapsedTimeMs = System.currentTimeMillis() - createdOn.getTime();
        }

        String dbJsonRequest = deepSiftContext.getDbJsonRequest();
        String responseContent = rawResponse;

        if (encryptRequestResponse && encryption != null) {
            if (dbJsonRequest != null) {
                dbJsonRequest = encryption.encrypt(dbJsonRequest, "AES256", "TEXT_DATA_TYPE");
            }
            if (responseContent != null) {
                responseContent = encryption.encrypt(responseContent, "AES256", "TEXT_DATA_TYPE");
            }
        }

        results.add(DeepSiftOutputTable.builder()
                .inputFilePath(deepSiftContext.getInputFilePath())
                .extractedText(finalExtractedContent)
                .originId(modelResponse.getOriginId())
                .groupId(modelResponse.getGroupId().intValue())
                .paperNo(deepSiftContext.getPaperNo())
                .createdOn(createdOn)
                .createdBy(deepSiftContext.getTenantId() != null ? deepSiftContext.getTenantId().toString() : null)
                .rootPipelineId(modelResponse.getRootPipelineId())
                .tenantId(modelResponse.getTenantId())
                .batchId(modelResponse.getBatchId())
                .sourceDocumentType(deepSiftContext.getSourceDocumentType())
                .modelId(deepSiftContext.getModelId())
                .modelName(modelResponse.getModelName())
                .timeTakenMS(elapsedTimeMs)
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .request(dbJsonRequest)
                .response(responseContent)
                .endpoint(deepSiftContext.getEndpoint())
                .wordCount(wordCount)
                .isBlankPage(isBlankPage)
                .build());

        return results;
    }

    /**
     * Builds a failed deep sift output row.
     */
    public static DeepSiftOutputTable buildFailedDeepSiftOutput(
            DeepSiftContext ctx, String errorMessage) {
        return DeepSiftOutputTable.builder()
                .batchId(ctx.getBatchId())
                .originId(ctx.getOriginId())
                .groupId(ctx.getGroupId() != null ? ctx.getGroupId().intValue() : null)
                .paperNo(ctx.getPaperNo())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .tenantId(ctx.getTenantId())
                .createdOn(ctx.getCreatedOn())
                .rootPipelineId(ctx.getRootPipelineId())
                .request(ctx.getDbJsonRequest())
                .response(errorMessage)
                .endpoint(ctx.getEndpoint())
                .build();
    }
}
