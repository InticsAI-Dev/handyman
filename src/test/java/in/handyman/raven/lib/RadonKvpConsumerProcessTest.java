//package in.handyman.raven.lib;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import in.handyman.raven.exception.HandymanException;
//import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
//import in.handyman.raven.lib.RadonKvpAction;
//import in.handyman.raven.lib.model.common.CreateTimeStamp;
//import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpConsumerProcess;
//import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
//import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
//import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonKvpExtractionResponse;
//import in.handyman.raven.lib.model.triton.*;
//import in.handyman.raven.lib.utils.FileProcessingUtils;
//import okhttp3.*;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.slf4j.Logger;
//import org.slf4j.Marker;
//
//import java.io.IOException;
//import java.net.URL;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class RadonKvpConsumerProcessTest {
//
//    @Mock
//    private Logger log;
//
//    @Mock
//    private Marker aMarker;
//
//    @Mock
//    private ActionExecutionAudit action;
//
//    @Mock
//    private RadonKvpAction radonKvpAction;
//
//    @Mock
//    private FileProcessingUtils fileProcessingUtils;
//
//    @InjectMocks
//    private RadonKvpConsumerProcess radonKvpConsumerProcess;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @BeforeEach
//    void setUp() {
//
//        when(radonKvpAction.getTimeOut()).thenReturn(10);
//        radonKvpConsumerProcess = new RadonKvpConsumerProcess(log, aMarker, action, radonKvpAction, "BASE64", fileProcessingUtils);
//    }
//
//    @Test
//    void testProcess_Base64Enabled() throws Exception {
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        when(entity.getInputFilePath()).thenReturn("test/path");
//        when(fileProcessingUtils.convertFileToBase64("test/path")).thenReturn("base64EncodedData");
//        when(entity.getRootPipelineId()).thenReturn(12345L);
//
//        URL endpoint = new URL("http://example.com");
//
//        List<RadonQueryOutputTable> result = radonKvpConsumerProcess.process(endpoint, entity);
//
//        verify(fileProcessingUtils, times(1)).convertFileToBase64("test/path");
//        assertNotNull(result);
//    }
//
//    @Test
//    void testProcess_LegacyMode() throws Exception {
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        when(entity.getInputFilePath()).thenReturn("test/path");
//        when(action.getContext()).thenReturn(Collections.singletonMap("triton.request.radon.kvp.activator", "false"));
//
//        URL endpoint = new URL("http://example.com");
//
//        List<RadonQueryOutputTable> result = radonKvpConsumerProcess.process(endpoint, entity);
//
//        verify(log).info(anyString(), any(), any(), any(), any());
//        assertNotNull(result);
//    }
//
//    @Test
//    void testTritonRequestBuilder_ValidResponse() throws IOException {
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
//        String jsonRequest = "{}";
//        URL endpoint = new URL("http://example.com");
//
//        Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaType.parse("application/json"))).build();
//        radonKvpConsumerProcess.tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
//
//        assertTrue(parentObj.isEmpty());
//    }
//
//    @Test
//    void testConvertFormattedJsonStringToJsonNode_ValidJson() {
//        String jsonResponse = "```json\n{\"key\":\"value\"}\n```";
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        assertDoesNotThrow(() -> {
//            assertNotNull(radonKvpConsumerProcess.convertFormattedJsonStringToJsonNode(jsonResponse, objectMapper));
//        });
//    }
//
//    @Test
//    void testCoproResponseBuilder_SuccessfulResponse() throws IOException {
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        when(entity.getInputFilePath()).thenReturn("path");
//
//        Request request = new Request.Builder().url("http://example.com").post(RequestBody.create("", MediaType.parse("application/json"))).build();
//        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
//
//        try (MockWebServer server = new MockWebServer()) {
//            server.start();
//            server.enqueue(new MockResponse().setBody("{\"outputs\": []}").setResponseCode(200));
//
//            radonKvpConsumerProcess.coproResponseBuider(entity, request, parentObj, "{}", new URL(server.url("/").toString()));
//            assertEquals(0, parentObj.size());
//        }
//    }
//
//    @Test
//    void testExtractedCoproOutputResponse_ValidData() throws IOException {
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
//        String radonDataItem = "{\"inferResponse\":\"{}\"}";
//        radonKvpConsumerProcess.extractedCoproOutputResponse(entity, radonDataItem, parentObj, "{}", "{}", "http://example.com");
//
//        assertEquals(1, parentObj.size());
//    }
//
//
//    @Test
//    void testExtractPayloadFromResponse_SuccessfulResponse() throws IOException {
//        // Arrange
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
//        String jsonRequest = "{}";
//        URL endpoint = new URL("http://example.com");
//        Long groupId = 1L;
//        Integer paperNo = 1;
//        Long tenantId = 1L;
//        Long processId = 1L;
//        Long rootPipelineId = 1L;
//
//
//
//
//
//
//        RadonKvpExtractionResponse modelResponse = mock(RadonKvpExtractionResponse.class);
//        when(mockResponse.isSuccessful()).thenReturn(true);
//        when(mockResponse.body()).thenReturn(mockResponseBody);
//        when(mockResponseBody.string()).thenReturn("{\"outputs\": [{\"data\": []}]}");
//        when(mapper.readValue(anyString(), eq(RadonKvpExtractionResponse.class))).thenReturn(modelResponse);
//        when(modelResponse.getOutputs()).thenReturn(new ArrayList<>());
//
//        // Act
//        extractPayloadService.extractPayloadFromResponse(
//                entity, parentObj, jsonRequest, endpoint, mockResponse, groupId, paperNo, tenantId, processId, rootPipelineId
//        );
//
//        // Assert
//        verify(mapper).readValue(anyString(), eq(RadonKvpExtractionResponse.class));
//        assertEquals(0, parentObj.size()); // Ensure parentObj is not modified for successful empty output
//    }
//
//    @Test
//    void testExtractPayloadFromResponse_FailedResponse() throws IOException {
//        // Arrange
//        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
//        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
//        String jsonRequest = "{}";
//        URL endpoint = new URL("http://example.com");
//        Long groupId = 1L;
//        Integer paperNo = 1;
//        Long tenantId = 1L;
//        Long processId = 1L;
//        Long rootPipelineId = 1L;
//
//        when(mockResponse.isSuccessful()).thenReturn(false);
//
//        // Act
//        extractPayloadService.extractPayloadFromResponse(
//                entity, parentObj, jsonRequest, endpoint, mockResponse, groupId, paperNo, tenantId, processId, rootPipelineId
//        );
//
//        // Assert
//        assertEquals(1, parentObj.size()); // Ensure parentObj contains one failed record
//        RadonQueryOutputTable outputTable = parentObj.get(0);
//        assertEquals(ConsumerProcessApiStatus.FAILED.getStatusDescription(), outputTable.getStatus());
//        assertEquals(jsonRequest, outputTable.getRequest());
//        assertEquals(endpoint.toString(), outputTable.getEndpoint());
//    }
//
//
//
//}
