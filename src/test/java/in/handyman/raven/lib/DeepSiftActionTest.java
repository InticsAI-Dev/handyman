package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lib.model.DeepSift;
import in.handyman.raven.lib.model.deep.sift.DeepSiftConsumerProcess;
import in.handyman.raven.lib.model.deep.sift.DeepSiftInputTable;
import in.handyman.raven.lib.model.deep.sift.DeepSiftOutputTable;
import in.handyman.raven.core.utils.FileProcessingUtils;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static in.handyman.raven.lib.DeepSiftAction.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeepSiftActionTest {

    @Mock
    private ActionExecutionAudit action;

    @Mock
    private Logger log;

    @Mock
    private Jdbi jdbi;

    @Mock
    private Handle handle;

    @Mock
    private PreparedBatch preparedBatch;

    @Mock
    private FileProcessingUtils fileProcessingUtils;

    @Mock
    private CoproProcessor<DeepSiftInputTable, DeepSiftOutputTable> coproProcessor;

    @Mock
    private DeepSiftConsumerProcess deepSiftConsumerProcess;

    @Mock
    private Arguments arguments;

    @Mock
    private Object sqlObject; // Mock for HandymanRepoImpl's SQL object

    private DeepSift deepSift;
    private DeepSiftAction deepSiftAction;
    private Map<String, String> context;
    private Marker marker;
    private static final String TEST_FILE_PATH = "/tmp/test.pdf";
    private static final String PAGE_CONTENT_MIN_LENGTH_KEY = "page.content.min.length";

    @Before
    public void setUp() throws Exception {
        // Initialize DeepSift
        deepSift = DeepSift.builder()
                .name("TestDeepSift")
                .resourceConn("testConnection")
                .resultTable("test_output_table")
                .endPoint("http://localhost:8080/test4j")
                .processId("12345")
                .querySet("SELECT * FROM test_input_table")
                .condition(true)
                .build();

        // Initialize context
        context = new HashMap<>();
        context.put(READ_BATCH_SIZE, "10");
        context.put(TEXT_EXTRACTION_CONSUMER_API_COUNT, "2");
        context.put(WRITE_BATCH_SIZE, "5");
        context.put(PAGE_CONTENT_MIN_LENGTH_KEY, "100");
        context.put("pipeline.copro.api.process.file.format", "BASE64");
        context.put("copro.request.deep.sift.handler.name", "TEST4J");
        context.put("preprocess.deep.sift.model.name", "KRYPTON");
        context.put("encrypt.text.extraction.output", "true");
        context.put("encrypt.request.response", "true");
        when(action.getContext()).thenReturn(context);

        // Mock ResourceAccess and JDBI interactions
        try (MockedStatic<ResourceAccess> resourceAccess = mockStatic(ResourceAccess.class)) {
            resourceAccess.when(() -> ResourceAccess.rdbmsJDBIConn(anyString())).thenReturn(jdbi);
            when(jdbi.getConfig(Arguments.class)).thenReturn(arguments);
            when(jdbi.open()).thenReturn(handle);
            when(handle.prepareBatch(anyString())).thenReturn(preparedBatch);
            when(preparedBatch.execute()).thenReturn(new int[]{1});
            doNothing().when(arguments).setUntypedNullArgument(any());
            // Mock SQL object used by HandymanRepoImpl
            when(jdbi.onDemand(any(Class.class))).thenReturn(sqlObject);
            when(sqlObject.getClass().getMethod("findOne", String.class).invoke(sqlObject, anyString())).thenReturn(null);

            // Initialize marker
            marker = MarkerFactory.getMarker(" DeepSift:" + deepSift.getName());

            // Initialize DeepSiftAction
            deepSiftAction = new DeepSiftAction(action, log, deepSift);
        }
    }

    @Test
    public void testExecuteWithTest4JHandlerSuccess() throws Exception {
        // Arrange
        String expectedInsertQuery = INSERT_INTO + deepSift.getResultTable() + " ( origin_id, group_id, input_file_path, created_on, created_by, root_pipeline_id, tenant_id, batch_id, extracted_text, paper_no, source_document_type, sor_item_id, sor_item_name, sor_container_id, container_document_type, sor_container_name, model_id, model_name, search_name, status ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        URL expectedUrl = new URL(deepSift.getEndPoint());

        // Sample DeepSiftInputTable
        DeepSiftInputTable inputTable = DeepSiftInputTable.builder()
                .id(1L)
                .rootPipelineId(100L)
                .originId("ORIG123")
                .batchId("BATCH001")
                .inputFilePath(TEST_FILE_PATH)
                .tenantId(200L)
                .groupId(300)
                .paperNo(1)
                .sourceDocumentType("PDF")
                .sorItemId(500L)
                .sorItemName("ItemName")
                .sorContainerId(600)
                .containerDocumentType("ContainerType")
                .sorContainerName("ContainerName")
                .modelId(700)
                .modelName("KRYPTON")
                .searchName("TestSearch")
                .systemPrompt("System prompt")
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .build();

        // Sample DeepSiftOutputTable
        String extractedText = "This is a sample extracted text that exceeds the minimum length threshold.";
        DeepSiftOutputTable outputTable = DeepSiftOutputTable.builder()
                .inputFilePath(TEST_FILE_PATH)
                .extractedText("encrypted_" + extractedText)
                .originId("ORIG123")
                .groupId(300)
                .paperNo(1)
                .status("COMPLETED")
                .tenantId(200L)
                .rootPipelineId(100L)
                .batchId("BATCH001")
                .sourceDocumentType("PDF")
                .sorItemId(500L)
                .sorItemName("ItemName")
                .sorContainerId("600")
                .containerDocumentType("ContainerType")
                .sorContainerName("ContainerName")
                .modelId(700)
                .modelName("KRYPTON")
                .searchName("TestSearch")
                .createdOn(inputTable.getCreatedOn())
                .build();

        // Mock CoproProcessor
        doAnswer(invocation -> {
            LinkedBlockingQueue<DeepSiftInputTable> queue = new LinkedBlockingQueue<>();
            queue.add(inputTable);
            return null;
        }).when(coproProcessor).startProducer(anyString(), anyInt());
        doAnswer(invocation -> {
            when(deepSiftConsumerProcess.process(eq(expectedUrl), eq(inputTable))).thenReturn(Collections.singletonList(outputTable));
            return null;
        }).when(coproProcessor).startConsumer(anyString(), anyInt(), anyInt(), eq(deepSiftConsumerProcess));

        // Act
        deepSiftAction.execute();

        // Assert
        verify(log).info(eq(marker), eq("Data Extraction Action for {} has been started"), eq(deepSift.getName()));
        verify(jdbi).getConfig(Arguments.class);
        verify(jdbi).open();
        verify(handle).prepareBatch(eq(expectedInsertQuery));
        verify(preparedBatch).execute();
        verify(arguments).setUntypedNullArgument(any());
        verify(coproProcessor).startProducer(deepSift.getQuerySet(), 10);
        verify(coproProcessor).startConsumer(eq(expectedInsertQuery), eq(2), eq(5), eq(deepSiftConsumerProcess));
        verify(deepSiftConsumerProcess).process(eq(expectedUrl), eq(inputTable));
        verify(log).info(eq(marker), eq(" Data Extraction Action has been completed {}  "), eq(deepSift.getName()));
        verify(action, never()).getContext().put(eq(deepSift.getName() + ".isSuccessful"), eq("false"));
    }

    @Test
    public void testExecuteWithTest4JHandlerFileNotFound() throws Exception {
        // Arrange
        String expectedInsertQuery = INSERT_INTO + deepSift.getResultTable() + " ( origin_id, group_id, input_file_path, created_on, created_by, root_pipeline_id, tenant_id, batch_id, extracted_text, paper_no, source_document_type, sor_item_id, sor_item_name, sor_container_id, container_document_type, sor_container_name, model_id, model_name, search_name, status ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        URL expectedUrl = new URL(deepSift.getEndPoint());

        // Sample DeepSiftInputTable
        DeepSiftInputTable inputTable = DeepSiftInputTable.builder()
                .id(1L)
                .rootPipelineId(100L)
                .originId("ORIG123")
                .batchId("BATCH001")
                .inputFilePath(TEST_FILE_PATH)
                .tenantId(200L)
                .groupId(300)
                .paperNo(1)
                .sourceDocumentType("PDF")
                .sorItemId(500L)
                .sorItemName("ItemName")
                .sorContainerId(600)
                .containerDocumentType("ContainerType")
                .sorContainerName("ContainerName")
                .modelId(700)
                .modelName("KRYPTON")
                .searchName("TestSearch")
                .systemPrompt("System prompt")
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .build();

        // Sample DeepSiftOutputTable for failure
        DeepSiftOutputTable outputTable = DeepSiftOutputTable.builder()
                .batchId("BATCH001")
                .originId("ORIG123")
                .groupId(300)
                .paperNo(1)
                .status("FAILED")
                .tenantId(200L)
                .rootPipelineId(100L)
                .sourceDocumentType("PDF")
                .sorItemId(500L)
                .sorItemName("ItemName")
                .sorContainerId("600")
                .containerDocumentType("ContainerType")
                .sorContainerName("ContainerName")
                .modelId(700)
                .modelName("KRYPTON")
                .searchName("TestSearch")
                .createdOn(inputTable.getCreatedOn())
                .build();

        // Mock CoproProcessor
        doAnswer(invocation -> {
            LinkedBlockingQueue<DeepSiftInputTable> queue = new LinkedBlockingQueue<>();
            queue.add(inputTable);
            return null;
        }).when(coproProcessor).startProducer(anyString(), anyInt());
        doAnswer(invocation -> {
            when(deepSiftConsumerProcess.process(eq(expectedUrl), eq(inputTable))).thenReturn(Collections.singletonList(outputTable));
            return null;
        }).when(coproProcessor).startConsumer(anyString(), anyInt(), anyInt(), eq(deepSiftConsumerProcess));

        // Act
        deepSiftAction.execute();

        // Assert
        verify(log).info(eq(marker), eq("Data Extraction Action for {} has been started"), eq(deepSift.getName()));
        verify(jdbi).getConfig(Arguments.class);
        verify(jdbi).open();
        verify(handle).prepareBatch(eq(expectedInsertQuery));
        verify(preparedBatch).execute();
        verify(arguments).setUntypedNullArgument(any());
        verify(coproProcessor).startProducer(deepSift.getQuerySet(), 10);
        verify(coproProcessor).startConsumer(eq(expectedInsertQuery), eq(2), eq(5), eq(deepSiftConsumerProcess));
        verify(deepSiftConsumerProcess).process(eq(expectedUrl), eq(inputTable));
        verify(log).info(eq(marker), eq(" Data Extraction Action has been completed {}  "), eq(deepSift.getName()));
        verify(action, never()).getContext().put(eq(deepSift.getName() + ".isSuccessful"), eq("false"));
    }

    @Test
    public void testExecuteWithInvalidUrl() {
        // Arrange
        deepSift.setEndPoint("invalid-url");

        // Act & Assert
        try {
            deepSiftAction.execute();
            fail("Expected HandymanException for invalid URL");
        } catch (HandymanException e) {
            assertTrue(e.getMessage().contains("Error in processing the URL"));
            verify(log).error(eq(marker), eq("Error in processing the URL"), any(java.net.MalformedURLException.class));
            verify(action).getContext().put(deepSift.getName() + ".isSuccessful", "false");
            verify(coproProcessor, never()).startProducer(anyString(), anyInt());
            verify(coproProcessor, never()).startConsumer(anyString(), anyInt(), anyInt(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteIfWhenConditionTrue() throws Exception {
        // Act
        boolean result = deepSiftAction.executeIf();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testExecuteIfWhenConditionFalse() throws Exception {
        // Arrange
        deepSift.setCondition(false);

        // Act
        boolean result = deepSiftAction.executeIf();

        // Assert
        assertFalse(result);
    }
}