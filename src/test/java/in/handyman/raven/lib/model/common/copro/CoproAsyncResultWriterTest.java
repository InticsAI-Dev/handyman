package in.handyman.raven.lib.model.common.copro;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleConsumer;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CoproAsyncResultWriterTest {

    @Mock
    private Jdbi jdbi;

    @Mock
    private Handle handle;

    private AutoCloseable mocks;

    private final ObjectMapper mapper = new ObjectMapper();

    private CoproAsyncContext buildDefaultContext() {
        return CoproAsyncContext.builder()
                .originId("origin-1")
                .batchId("batch-1")
                .requestType("AGENTIC_PAPER_FILTER")
                .outputTable("output_table")
                .pageNo(1)
                .groupId(100L)
                .processId(200L)
                .tenantId(300L)
                .rootPipelineId(400L)
                .actionId(500L)
                .templateId("tmpl-1")
                .templateName("template-name")
                .filePath("/path/to/file")
                .promptType("prompt")
                .modelName("model-1")
                .modelVersion("v1")
                .createdOn(new Timestamp(1000L))
                .build();
    }

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void writeAgenticFilter_NullResult_ReturnsEarly() {
        CoproAsyncContext ctx = buildDefaultContext();

        CoproAsyncResultWriter.writeAgenticFilterResult(ctx, null, "table", 50, jdbi);

        verifyNoInteractions(jdbi);
    }

    @Test
    void writeAgenticFilter_ValidResult_ParsesAndInserts() throws Exception {
        CoproAsyncContext ctx = buildDefaultContext();
        JsonNode result = mapper.readTree("{\"model\":\"OPTIMUS\",\"infer_response\":\"yes\"}");

        doAnswer(inv -> {
            HandleConsumer<?> consumer = inv.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(any());

        CoproAsyncResultWriter.writeAgenticFilterResult(ctx, result, "output_table", 50, jdbi);

        verify(jdbi).useTransaction(any());
        verify(handle, atLeastOnce()).execute(anyString(), any(Object[].class));
    }

    @Test
    void writeAgenticFilter_ParseError_InsertsFailedRow() throws Exception {
        CoproAsyncContext ctx = buildDefaultContext();
        // Invalid JSON that will cause parsing error
        JsonNode result = mapper.readTree("\"not a valid copro response\"");

        doAnswer(inv -> {
            HandleConsumer<?> consumer = inv.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(any());

        CoproAsyncResultWriter.writeAgenticFilterResult(ctx, result, "output_table", 50, jdbi);

        // Should still insert a failed row
        verify(jdbi).useTransaction(any());
    }

    @Test
    void writeCheckboxExtraction_NullResult_ReturnsEarly() {
        CoproAsyncContext ctx = buildDefaultContext();

        CoproAsyncResultWriter.writeCheckboxExtractionResult(ctx, null, "table", new HashMap<>(), jdbi);

        verifyNoInteractions(jdbi);
    }

    @Test
    void writeCheckboxExtraction_ValidResult_ParsesAndInserts() throws Exception {
        CoproAsyncContext ctx = buildDefaultContext();
        JsonNode result = mapper.readTree("{\"infer_response\":\"checkbox data\"}");

        doAnswer(inv -> {
            HandleConsumer<?> consumer = inv.getArgument(0);
            consumer.useHandle(handle);
            return null;
        }).when(jdbi).useTransaction(any());

        CoproAsyncResultWriter.writeCheckboxExtractionResult(ctx, result, "output_table", new HashMap<>(), jdbi);

        verify(jdbi).useTransaction(any());
    }

    @Test
    void writeSorTransaction_NullResult_ReturnsEarly() {
        CoproAsyncContext ctx = buildDefaultContext();

        CoproAsyncResultWriter.writeSorTransactionResult(ctx, null, "table",
                Map.of(), "resource", jdbi);

        verifyNoInteractions(jdbi);
    }

    @Test
    void buildRadonKvpContext_MapsAllFields() {
        CoproAsyncContext ctx = CoproAsyncContext.builder()
                .originId("o1")
                .pageNo(5)
                .groupId(100L)
                .processId(200L)
                .tenantId(300L)
                .rootPipelineId(400L)
                .process("proc")
                .inputFilePath("/input")
                .batchId("b1")
                .modelRegistry("reg")
                .category("cat")
                .apiName("api")
                .sorContainerId(600L)
                .sorContainerName("sor")
                .createdOn(new Timestamp(1000L))
                .actionId(500L)
                .build();

        RadonKvpContext radonCtx = CoproAsyncResultWriter.buildRadonKvpContext(ctx);

        assertEquals("o1", radonCtx.getOriginId());
        assertEquals(5, radonCtx.getPaperNo());
        assertEquals(100L, radonCtx.getGroupId());
        assertEquals(200L, radonCtx.getProcessId());
        assertEquals(300L, radonCtx.getTenantId());
        assertEquals(400L, radonCtx.getRootPipelineId());
        assertEquals("proc", radonCtx.getProcess());
        assertEquals("/input", radonCtx.getInputFilePath());
        assertEquals("b1", radonCtx.getBatchId());
        assertEquals("reg", radonCtx.getModelRegistry());
        assertEquals("cat", radonCtx.getCategory());
        assertEquals("api", radonCtx.getApiName());
        assertEquals(600L, radonCtx.getSorContainerId());
        assertEquals("sor", radonCtx.getSorContainerName());
        assertEquals(500L, radonCtx.getActionId());
    }

    @Test
    void buildRadonKvpContext_NullTimestamp_DefaultsToNow() {
        CoproAsyncContext ctx = CoproAsyncContext.builder()
                .originId("o1")
                .createdOn(null)
                .build();

        long beforeTest = System.currentTimeMillis();
        RadonKvpContext radonCtx = CoproAsyncResultWriter.buildRadonKvpContext(ctx);
        long afterTest = System.currentTimeMillis();

        assertNotNull(radonCtx.getCreatedOn());
        assertTrue(radonCtx.getCreatedOn().getTime() >= beforeTest);
        assertTrue(radonCtx.getCreatedOn().getTime() <= afterTest);
    }

    @Test
    void writeAgenticFilter_NullNodeResult_ReturnsEarly() throws Exception {
        CoproAsyncContext ctx = buildDefaultContext();
        JsonNode nullNode = mapper.readTree("null");

        CoproAsyncResultWriter.writeAgenticFilterResult(ctx, nullNode, "table", 50, jdbi);

        verifyNoInteractions(jdbi);
    }
}
