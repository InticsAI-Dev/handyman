package in.handyman.raven.lib.model.common.copro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.DatabaseUtility;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.AgenticPaperFilterAction;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpLineItem;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import lombok.Data;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.sql.Timestamp;
import java.util.*;

/**
 * Shared utility that consolidates the result-writing logic for async copro responses.
 * Used by CoproResultWriterService (intics-lambda) and can also be used by
 * sync ConsumerProcess classes in handyman.
 *
 * <p>Each write method handles: context building, parsing via {@link CoproResponseParser},
 * encryption setup, and row insertion.</p>
 */
public final class CoproAsyncResultWriter {

    private static final Logger logger = LoggerFactory.getLogger(CoproAsyncResultWriter.class);
    private static final Marker aMarker = MarkerFactory.getMarker("CoproAsyncResultWriter");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CoproAsyncResultWriter() {
    }

    /**
     * Write an AGENTIC_PAPER_FILTER result to the output table.
     */
    public static void writeAgenticFilterResult(CoproAsyncContext ctx, JsonNode result,
                                                 String outputTable, int pageContentMinLength, Jdbi jdbi) {
        if (result == null || result.isNull()) {
            logger.warn("AGENTIC_PAPER_FILTER result null for batchId={} originId={}", ctx.getBatchId(), ctx.getOriginId());
            return;
        }

        Timestamp createdOn = ctx.getCreatedOn() != null ? ctx.getCreatedOn() : new Timestamp(System.currentTimeMillis());

        AgenticFilterContext context = AgenticFilterContext.builder()
                .originId(ctx.getOriginId())
                .groupId(ctx.getGroupId())
                .tenantId(ctx.getTenantId())
                .templateId(ctx.getTemplateId())
                .processId(ctx.getProcessId())
                .filePath(ctx.getFilePath())
                .paperNo(ctx.getPageNo())
                .rootPipelineId(ctx.getRootPipelineId())
                .batchId(ctx.getBatchId())
                .createdOn(createdOn)
                .templateName(ctx.getTemplateName())
                .modelName(ctx.getModelName())
                .modelVersion(ctx.getModelVersion())
                .promptType(ctx.getPromptType())
                .build();

        try {
            String rawResponse = objectMapper.writeValueAsString(result);
            List<AgenticPaperFilterOutput> outputs =
                    CoproResponseParser.parseAgenticFilterResponse(rawResponse, context, pageContentMinLength, objectMapper);

            if (outputs.isEmpty()) {
                insertAgenticRows(List.of(CoproResponseParser.buildFailedAgenticOutput(context, "No infer_response")),
                        outputTable, jdbi);
                return;
            }
            insertAgenticRows(outputs, outputTable, jdbi);
        } catch (Exception e) {
            logger.error("Failed to parse AGENTIC_PAPER_FILTER response", e);
            insertAgenticRows(List.of(CoproResponseParser.buildFailedAgenticOutput(context, e.getMessage())),
                    outputTable, jdbi);
        }
    }

    /**
     * Write a CHECKBOX_EXTRACTION result to the output table.
     */
    public static void writeCheckboxExtractionResult(CoproAsyncContext ctx, JsonNode result,
                                                      String outputTable, Jdbi jdbi) {
        if (result == null || result.isNull()) {
            logger.warn("CHECKBOX_EXTRACTION result null for batchId={} originId={}", ctx.getBatchId(), ctx.getOriginId());
            return;
        }

        RadonKvpContext radonCtx = buildRadonKvpContext(ctx);

        try {
            String rawResponse = objectMapper.writeValueAsString(result);
            List<RadonQueryOutputTable> outputs = CoproResponseParser.parseRadonKvpResponse(
                    rawResponse, radonCtx, null, false, objectMapper);
            insertRadonRows(outputs, outputTable, jdbi);
        } catch (Exception e) {
            logger.error("Failed to write CHECKBOX_EXTRACTION for batchId={} originId={}", ctx.getBatchId(), ctx.getOriginId(), e);
            throw new RuntimeException("CHECKBOX_EXTRACTION write failed", e);
        }
    }

    /**
     * Write a SOR_TRANSACTION result to the output table.
     */
    public static void writeSorTransactionResult(CoproAsyncContext ctx, JsonNode result,
                                                  String outputTable, Map<String, String> savedContext,
                                                  String jdbiResourceName, Jdbi jdbi) {
        if (result == null || result.isNull()) {
            logger.warn("SOR_TRANSACTION result null for batchId={} originId={}", ctx.getBatchId(), ctx.getOriginId());
            return;
        }

        InticsIntegrity encryption = buildEncryption(savedContext);
        boolean encryptItemWise = "true".equals(savedContext.get("pipeline.end.to.end.encryption"));

        RadonKvpContext radonKvpContext = buildRadonKvpContext(ctx);

        boolean postProcess = "true".equalsIgnoreCase(ctx.getPostProcess());
        String postProcessClassName = ctx.getPostProcessClassName();

        if (!postProcess && radonKvpContext.getSorContainerId() != null && radonKvpContext.getTenantId() != null) {
            SorContainerInfo sorInfo = lookupSorContainer(radonKvpContext.getSorContainerId(), radonKvpContext.getTenantId(), jdbi);
            if (sorInfo != null && sorInfo.isPostProcess()) {
                postProcess = true;
                postProcessClassName = sorInfo.getPostProcessClassName();
            }
        }

        try {
            String rawResponse = objectMapper.writeValueAsString(result);
            List<RadonQueryOutputTable> outputs;

            if (postProcess && postProcessClassName != null) {
                String bshClassName = savedContext.get(postProcessClassName);
                if (bshClassName == null) {
                    bshClassName = postProcessClassName;
                }
                outputs = handlePostProcessing(rawResponse, radonKvpContext, savedContext, bshClassName, jdbiResourceName);
            } else {
                outputs = CoproResponseParser.parseRadonKvpResponse(rawResponse, radonKvpContext, encryption, encryptItemWise, objectMapper);
            }
            insertRadonRows(outputs, outputTable, jdbi);
        } catch (Exception e) {
            logger.error("Failed to write SOR_TRANSACTION for batchId={} originId={}", ctx.getBatchId(), ctx.getOriginId(), e);
            throw new RuntimeException("SOR_TRANSACTION write failed", e);
        }
    }

    // --- Internal helpers ---

    static RadonKvpContext buildRadonKvpContext(CoproAsyncContext ctx) {
        return RadonKvpContext.builder()
                .originId(ctx.getOriginId())
                .paperNo(ctx.getPageNo())
                .groupId(ctx.getGroupId())
                .processId(ctx.getProcessId())
                .tenantId(ctx.getTenantId())
                .rootPipelineId(ctx.getRootPipelineId())
                .process(ctx.getProcess())
                .inputFilePath(ctx.getInputFilePath())
                .batchId(ctx.getBatchId())
                .modelRegistry(ctx.getModelRegistry())
                .category(ctx.getCategory())
                .apiName(ctx.getApiName())
                .sorContainerId(ctx.getSorContainerId())
                .sorContainerName(ctx.getSorContainerName())
                .createdOn(ctx.getCreatedOn() != null ? ctx.getCreatedOn() : new Timestamp(System.currentTimeMillis()))
                .actionId(ctx.getActionId())
                .build();
    }

    private static void insertAgenticRows(List<AgenticPaperFilterOutput> outputs, String outputTable, Jdbi jdbi) {
        final String insertSql = AgenticPaperFilterAction.INSERT_INTO + outputTable + " ( "
                + AgenticPaperFilterAction.INSERT_COLUMNS_UPDATED + " ) "
                + AgenticPaperFilterAction.INSERT_INTO_VALUES_UPDATED;

        jdbi.useTransaction(handle -> {
            for (AgenticPaperFilterOutput entity : outputs) {
                handle.execute(insertSql, entity.getRowData().toArray());
            }
        });
    }

    private static void insertRadonRows(List<RadonQueryOutputTable> outputs, String outputTable, Jdbi jdbi) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        final String insertSql = "INSERT INTO " + outputTable + "(" + RadonKvpAction.COLUMN_LIST + ") "
                + RadonKvpAction.VAL_STRING_LIST;

        jdbi.useTransaction(handle -> {
            for (RadonQueryOutputTable entity : outputs) {
                handle.execute(insertSql, entity.getRowData().toArray());
            }
        });
    }

    private static InticsIntegrity buildEncryption(Map<String, String> savedContext) {
        ActionExecutionAudit lightweight = new ActionExecutionAudit();
        lightweight.setContext(new HashMap<>(savedContext));
        return SecurityEngine.getInticsIntegrityMethod(lightweight, logger);
    }

    private static SorContainerInfo lookupSorContainer(Long sorContainerId, Long tenantId, Jdbi jdbi) {
        if (sorContainerId == null) return null;
        return jdbi.withHandle(handle -> handle.createQuery(
                        "SELECT post_processing::bool AS post_process, post_process_class_name " +
                                "FROM sor_meta.sor_container " +
                                "WHERE sor_container_id = :id AND tenant_id = :tenantId AND status = 'ACTIVE'")
                .bind("id", sorContainerId)
                .bind("tenantId", tenantId)
                .mapToBean(SorContainerInfo.class)
                .findOne()
                .orElse(null));
    }

    private static List<RadonQueryOutputTable> handlePostProcessing(
            String rawResponse, RadonKvpContext ctx,
            Map<String, String> savedContext, String bshClassName,
            String jdbiResourceName) throws Exception {

        ActionExecutionAudit lightweightAction = new ActionExecutionAudit();
        lightweightAction.setContext(new HashMap<>(savedContext));
        lightweightAction.setRootPipelineId(ctx.getRootPipelineId());

        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(lightweightAction, logger);

        ProviderDataTransformer transformer = new ProviderDataTransformer(
                logger, aMarker, objectMapper, lightweightAction, jdbiResourceName, encryption);

        RadonKvpLineItem lineItem = objectMapper.readValue(rawResponse, RadonKvpLineItem.class);
        RadonQueryInputTable entity = buildInputTableFromContext(ctx);

        Optional<String> sourceCode = DatabaseUtility.fetchBshResultByClassName(
                jdbiResourceName, bshClassName, ctx.getTenantId());

        if (sourceCode.isPresent()) {
            return transformer.processProviderData(
                    sourceCode.get(), bshClassName, lineItem.getInferResponse(),
                    entity, "", rawResponse, "");
        }
        logger.warn("BSH not found for class={}", bshClassName);
        return Collections.emptyList();
    }

    private static RadonQueryInputTable buildInputTableFromContext(RadonKvpContext radonKvpContext) {
        RadonQueryInputTable entity = new RadonQueryInputTable();
        entity.setOriginId(radonKvpContext.getOriginId());
        entity.setPaperNo(radonKvpContext.getPaperNo());
        entity.setGroupId(radonKvpContext.getGroupId());
        entity.setProcessId(radonKvpContext.getProcessId());
        entity.setTenantId(radonKvpContext.getTenantId());
        entity.setRootPipelineId(radonKvpContext.getRootPipelineId());
        entity.setActionId(radonKvpContext.getActionId());
        entity.setProcess(radonKvpContext.getProcess());
        entity.setInputFilePath(radonKvpContext.getInputFilePath());
        entity.setBatchId(radonKvpContext.getBatchId());
        entity.setModelRegistry(radonKvpContext.getModelRegistry());
        entity.setCategory(radonKvpContext.getCategory());
        entity.setApiName(radonKvpContext.getApiName());
        entity.setSorContainerId(radonKvpContext.getSorContainerId());
        entity.setSorContainerName(radonKvpContext.getSorContainerName());
        entity.setCreatedOn(radonKvpContext.getCreatedOn());
        return entity;
    }

    @Data
    public static class SorContainerInfo {
        private boolean postProcess;
        private String postProcessClassName;
    }
}
