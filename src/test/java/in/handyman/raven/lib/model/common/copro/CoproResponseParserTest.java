package in.handyman.raven.lib.model.common.copro;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.model.agentic.paper.filter.AgenticPaperFilterOutput;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoproResponseParserTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private AgenticFilterContext buildDefaultContext() {
        return AgenticFilterContext.builder()
                .originId("origin-1")
                .groupId(100L)
                .tenantId(200L)
                .templateId("tmpl-1")
                .processId(300L)
                .filePath("/path/to/file")
                .paperNo(1)
                .rootPipelineId(400L)
                .batchId("batch-1")
                .createdOn(new Timestamp(1000L))
                .templateName("template-name")
                .modelName("model-1")
                .modelVersion("v1")
                .promptType("prompt")
                .endpoint("http://endpoint")
                .uniqueName("unique-name")
                .uniqueId(10)
                .build();
    }

    @Test
    void parseAgenticFilter_OptimusModel_BuildsOptimusOutput() throws Exception {
        String rawResponse = "{\"model\":\"OPTIMUS\",\"infer_response\":\"yes\",\"originId\":\"origin-1\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertEquals(1, results.size());
        AgenticPaperFilterOutput output = results.getFirst();
        assertEquals("true", output.getContainerValue()); // "yes" -> "true"
        assertEquals("unique-name", output.getContainerName());
        assertEquals(10, output.getContainerId());
        assertEquals("origin-1", output.getOriginId());
    }

    @Test
    void parseAgenticFilter_KryptonModel_BuildsKryptonOutput() throws Exception {
        String rawResponse = "{\"infer_response\":\"{\\\"field1\\\":\\\"val1\\\",\\\"field2\\\":\\\"val2\\\"}\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertEquals(2, results.size());
        assertEquals("field1", results.get(0).getContainerName());
        assertEquals("val1", results.get(0).getContainerValue());
        assertEquals("field2", results.get(1).getContainerName());
        assertEquals("val2", results.get(1).getContainerValue());
    }

    @Test
    void parseAgenticFilter_NullInferResponse_ReturnsEmptyList() throws Exception {
        String rawResponse = "{\"model\":\"OPTIMUS\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertTrue(results.isEmpty());
    }

    @Test
    void parseAgenticFilter_BlankPageDetection_ShortContent() throws Exception {
        // infer_response length < pageContentMinLength => isBlankPage = "yes"
        String shortResponse = "short";
        String rawResponse = "{\"model\":\"OPTIMUS\",\"infer_response\":\"" + shortResponse + "\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 100, mapper);

        assertEquals(1, results.size());
        assertEquals("yes", results.getFirst().getIsBlankPage());
    }

    @Test
    void parseAgenticFilter_BlankPageDetection_LongContent() throws Exception {
        // infer_response length > pageContentMinLength => isBlankPage = "no"
        String longResponse = "This is a long response that exceeds the minimum length threshold for blank page detection";
        String rawResponse = "{\"model\":\"OPTIMUS\",\"infer_response\":\"" + longResponse + "\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 10, mapper);

        assertEquals(1, results.size());
        assertEquals("no", results.getFirst().getIsBlankPage());
    }

    @Test
    void parseAgenticFilter_JsonWithMarkdownFence_CleansUp() throws Exception {
        String rawResponse = "```json{\"model\":\"OPTIMUS\",\"infer_response\":\"yes\"}```";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertEquals(1, results.size());
    }

    @Test
    void parseAgenticFilter_OptimusContainerValueMapping_No() throws Exception {
        String rawResponse = "{\"model\":\"OPTIMUS\",\"infer_response\":\"no\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertEquals(1, results.size());
        assertEquals("false", results.getFirst().getContainerValue()); // "no" -> "false"
    }

    @Test
    void parseAgenticFilter_KryptonMultipleFields_MultipleOutputRows() throws Exception {
        String rawResponse = "{\"infer_response\":\"{\\\"a\\\":\\\"1\\\",\\\"b\\\":\\\"2\\\",\\\"c\\\":\\\"3\\\"}\"}";
        AgenticFilterContext ctx = buildDefaultContext();

        List<AgenticPaperFilterOutput> results =
                CoproResponseParser.parseAgenticFilterResponse(rawResponse, ctx, 50, mapper);

        assertEquals(3, results.size());
    }

    @Test
    void parseRadonKvp_NoEncryption_RawResponse() throws Exception {
        String rawResponse = "{\"infer_response\":\"raw content data\"}";
        RadonKvpContext ctx = RadonKvpContext.builder()
                .originId("o1").paperNo(1).groupId(100L).processId(200L)
                .tenantId(300L).rootPipelineId(400L).process("proc")
                .batchId("b1").modelRegistry("reg").category("cat")
                .apiName("api").createdOn(new Timestamp(1000L))
                .build();

        List<RadonQueryOutputTable> results =
                CoproResponseParser.parseRadonKvpResponse(rawResponse, ctx, null, false, mapper);

        assertEquals(1, results.size());
        assertEquals("raw content data", results.getFirst().getTotalResponseJson());
    }

    @Test
    void parseRadonKvp_SorContainerName_AppendsSuffix() throws Exception {
        String rawResponse = "{\"infer_response\":\"data\"}";
        RadonKvpContext ctx = RadonKvpContext.builder()
                .originId("o1").sorContainerName("myContainer")
                .createdOn(new Timestamp(1000L))
                .build();

        List<RadonQueryOutputTable> results =
                CoproResponseParser.parseRadonKvpResponse(rawResponse, ctx, null, false, mapper);

        assertEquals(1, results.size());
        assertEquals("myContainer_0", results.getFirst().getSorContainerInstance());
    }

    @Test
    void parseRadonKvp_NullSorContainerName_NullInstance() throws Exception {
        String rawResponse = "{\"infer_response\":\"data\"}";
        RadonKvpContext ctx = RadonKvpContext.builder()
                .originId("o1").sorContainerName(null)
                .createdOn(new Timestamp(1000L))
                .build();

        List<RadonQueryOutputTable> results =
                CoproResponseParser.parseRadonKvpResponse(rawResponse, ctx, null, false, mapper);

        assertEquals(1, results.size());
        assertNull(results.getFirst().getSorContainerInstance());
    }

    @Test
    void buildFailedAgenticOutput_SetsFailedStatus() {
        AgenticFilterContext ctx = buildDefaultContext();

        AgenticPaperFilterOutput output =
                CoproResponseParser.buildFailedAgenticOutput(ctx, "Something went wrong");

        assertEquals(ConsumerProcessApiStatus.FAILED.getStatusDescription(), output.getStatus());
        assertEquals("Something went wrong", output.getMessage());
        assertEquals("origin-1", output.getOriginId());
        assertEquals("batch-1", output.getBatchId());
    }

    @Test
    void buildFailedAgenticOutput_NullGroupId_SetsNull() {
        AgenticFilterContext ctx = AgenticFilterContext.builder()
                .originId("o1")
                .groupId(null)
                .batchId("b1")
                .createdOn(new Timestamp(1000L))
                .build();

        AgenticPaperFilterOutput output =
                CoproResponseParser.buildFailedAgenticOutput(ctx, "error");

        assertNull(output.getGroupId());
    }
}
