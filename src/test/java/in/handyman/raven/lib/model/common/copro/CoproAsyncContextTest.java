package in.handyman.raven.lib.model.common.copro;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class CoproAsyncContextTest {

    @Test
    void builder_AllFields_SetsCorrectly() {
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        CoproAsyncContext ctx = CoproAsyncContext.builder()
                .originId("origin-1")
                .batchId("batch-1")
                .requestType("AGENTIC_PAPER_FILTER")
                .outputTable("output_table")
                .pageNo(5)
                .groupId(100L)
                .processId(200L)
                .tenantId(300L)
                .rootPipelineId(400L)
                .actionId(500L)
                .sorContainerId(600L)
                .sorContainerName("sor-container")
                .templateId("tmpl-1")
                .templateName("template-name")
                .filePath("/path/to/file")
                .promptType("prompt-type")
                .modelName("model-name")
                .modelVersion("v1")
                .process("process-name")
                .modelRegistry("registry")
                .apiName("api-name")
                .category("category")
                .inputFilePath("/input/path")
                .postProcess("true")
                .postProcessClassName("com.example.PostProcessor")
                .postProcessClass("PostProcessor")
                .createdOn(ts)
                .build();

        assertEquals("origin-1", ctx.getOriginId());
        assertEquals("batch-1", ctx.getBatchId());
        assertEquals("AGENTIC_PAPER_FILTER", ctx.getRequestType());
        assertEquals("output_table", ctx.getOutputTable());
        assertEquals(5, ctx.getPageNo());
        assertEquals(100L, ctx.getGroupId());
        assertEquals(200L, ctx.getProcessId());
        assertEquals(300L, ctx.getTenantId());
        assertEquals(400L, ctx.getRootPipelineId());
        assertEquals(500L, ctx.getActionId());
        assertEquals(600L, ctx.getSorContainerId());
        assertEquals("sor-container", ctx.getSorContainerName());
        assertEquals("tmpl-1", ctx.getTemplateId());
        assertEquals("template-name", ctx.getTemplateName());
        assertEquals("/path/to/file", ctx.getFilePath());
        assertEquals("prompt-type", ctx.getPromptType());
        assertEquals("model-name", ctx.getModelName());
        assertEquals("v1", ctx.getModelVersion());
        assertEquals("process-name", ctx.getProcess());
        assertEquals("registry", ctx.getModelRegistry());
        assertEquals("api-name", ctx.getApiName());
        assertEquals("category", ctx.getCategory());
        assertEquals("/input/path", ctx.getInputFilePath());
        assertEquals("true", ctx.getPostProcess());
        assertEquals("com.example.PostProcessor", ctx.getPostProcessClassName());
        assertEquals("PostProcessor", ctx.getPostProcessClass());
        assertEquals(ts, ctx.getCreatedOn());
    }

    @Test
    void noArgsConstructor_AllFieldsNull() {
        CoproAsyncContext ctx = new CoproAsyncContext();

        assertNull(ctx.getOriginId());
        assertNull(ctx.getBatchId());
        assertNull(ctx.getRequestType());
        assertNull(ctx.getOutputTable());
        assertNull(ctx.getPageNo());
        assertNull(ctx.getGroupId());
        assertNull(ctx.getProcessId());
        assertNull(ctx.getTenantId());
        assertNull(ctx.getRootPipelineId());
        assertNull(ctx.getActionId());
        assertNull(ctx.getSorContainerId());
        assertNull(ctx.getSorContainerName());
        assertNull(ctx.getCreatedOn());
    }

    @Test
    void equals_SameFields_AreEqual() {
        Timestamp ts = new Timestamp(1000L);
        CoproAsyncContext ctx1 = CoproAsyncContext.builder()
                .originId("origin-1")
                .batchId("batch-1")
                .requestType("AGENTIC_PAPER_FILTER")
                .createdOn(ts)
                .build();

        CoproAsyncContext ctx2 = CoproAsyncContext.builder()
                .originId("origin-1")
                .batchId("batch-1")
                .requestType("AGENTIC_PAPER_FILTER")
                .createdOn(ts)
                .build();

        assertEquals(ctx1, ctx2);
        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }
}
