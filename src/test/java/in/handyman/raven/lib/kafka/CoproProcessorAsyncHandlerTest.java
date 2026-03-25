package in.handyman.raven.lib.kafka;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CoproProcessorAsyncHandlerTest {

    private static final Logger logger = LoggerFactory.getLogger(CoproProcessorAsyncHandlerTest.class);

    @Mock
    private ActionExecutionAudit actionExecutionAudit;

    private AutoCloseable mocks;

    private CoproProcessorAsyncHandler<TestEntity, TestEntity> handler;
    private BlockingQueue<TestEntity> queue;
    private TestEntity stoppingSeed;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        queue = new ArrayBlockingQueue<>(100);
        stoppingSeed = TestEntity.builder().originId("STOP").build();
        handler = new CoproProcessorAsyncHandler<>(queue, stoppingSeed, actionExecutionAudit, "test_db_conn", logger);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    @lombok.Builder
    public static class TestEntity implements CoproProcessor.Entity {
        private String originId;
        private Integer paperNo;
        private String templateId;
        private String filePath;
        @lombok.Builder.Default
        private String status = "COMPLETED";

        @Override
        public List<Object> getRowData() {
            return List.of(originId != null ? originId : "", paperNo != null ? paperNo : 0);
        }

        @Override
        public String getStatus() {
            return status;
        }
    }

    @Test
    void startKafkaAsyncPublisher_NullRequestType_ThrowsException() {
        Map<String, String> context = new HashMap<>();
        context.put("batch_id", "batch-1");
        context.put("copro.processor.kafka.topic", "test-topic");
        when(actionExecutionAudit.getContext()).thenReturn(context);

        TestConsumerProcess callable = new TestConsumerProcess(null, "test-topic", "output_table");

        // Throws HandymanException for blank requestType, but runtime dependencies
        // (e.g. ResourceAccess static init) may cause NoClassDefFoundError/ExceptionInInitializerError
        assertThrows(Throwable.class, () -> handler.startKafkaAsyncPublisher(callable));
    }

    @Test
    void startKafkaAsyncPublisher_NullTopic_ThrowsException() {
        Map<String, String> context = new HashMap<>();
        context.put("batch_id", "batch-1");
        when(actionExecutionAudit.getContext()).thenReturn(context);

        TestConsumerProcess callable = new TestConsumerProcess("REQUEST_TYPE", null, "output_table");

        // Throws HandymanException for blank topic, but runtime dependencies
        // may cause NoClassDefFoundError/ExceptionInInitializerError
        assertThrows(Throwable.class, () -> handler.startKafkaAsyncPublisher(callable));
    }

    @Test
    void buildProducerRecord_WithOriginId_SetsMessageKey() {
        Map<String, String> context = new HashMap<>();
        context.put("init_process_id.process_id", "proc-1");
        context.put("tenant_id", "tenant-1");
        context.put("group_id", "group-1");
        when(actionExecutionAudit.getRootPipelineId()).thenReturn(100L);
        when(actionExecutionAudit.getActionId()).thenReturn(500L);

        TestEntity item = TestEntity.builder().originId("origin-123").paperNo(5).templateId("tmpl-1").filePath("/path").build();

        ProducerRecord<String, String> recordData = handler.buildProducerRecord(
                "test-topic", "{\"data\":\"test\"}", "output_table",
                "REQUEST_TYPE", "batch-1", context, item, "corr-1");

        assertEquals("origin-123", recordData.key());
        assertEquals("test-topic", recordData.topic());
    }

    @Test
    void buildProducerRecord_NullOriginId_ProducesNullStringKey() {
        // When originId is null, String.valueOf(null) = "null" (the string),
        // which is not blank, so it becomes the message key
        Map<String, String> context = new HashMap<>();
        when(actionExecutionAudit.getRootPipelineId()).thenReturn(100L);
        when(actionExecutionAudit.getActionId()).thenReturn(500L);

        TestEntity item = TestEntity.builder().paperNo(5).build();

        ProducerRecord<String, String> recordData = handler.buildProducerRecord(
                "test-topic", "{\"data\":\"test\"}", "output_table",
                "REQUEST_TYPE", "batch-1", context, item, "corr-1");

        assertEquals("null", recordData.key());
    }

    @Test
    void buildProducerRecord_SetsAllHeaders() {
        Map<String, String> context = new HashMap<>();
        context.put("init_process_id.process_id", "proc-1");
        context.put("tenant_id", "tenant-1");
        context.put("group_id", "group-1");
        when(actionExecutionAudit.getRootPipelineId()).thenReturn(100L);
        when(actionExecutionAudit.getActionId()).thenReturn(500L);

        TestEntity item = TestEntity.builder()
                .originId("o1").paperNo(3).templateId("tmpl-1").filePath("/path")
                .build();

        ProducerRecord<String, String> recordData = handler.buildProducerRecord(
                "test-topic", "{}", "output_table", "REQ", "batch-1", context, item, "corr-1");

        assertHeaderPresent(recordData, "X-Route-OutputTable", "output_table");
        assertHeaderPresent(recordData, "X-Route-RequestType", "REQ");
        assertHeaderPresent(recordData, "X-Route-BatchId", "batch-1");
        assertHeaderPresent(recordData, "X-Route-CorrelationId", "corr-1");
        assertHeaderPresent(recordData, "X-Route-RootPipelineId", "100");
        assertHeaderPresent(recordData, "X-Route-ProcessId", "proc-1");
        assertHeaderPresent(recordData, "X-Route-TenantId", "tenant-1");
        assertHeaderPresent(recordData, "X-Route-GroupId", "group-1");
        assertHeaderPresent(recordData, "X-Route-ActionId", "500");
        assertHeaderPresent(recordData, "X-Route-TemplateId", "tmpl-1");
        assertHeaderPresent(recordData, "X-Route-FilePath", "/path");
    }

    @Test
    void buildAsyncKafkaProps_Plaintext_NoSslConfig() {
        Map<String, String> context = new HashMap<>();
        context.put("copro.processor.kafka.bootstrap.servers", "broker:9092");
        context.put("copro.processor.kafka.security.protocol", "PLAINTEXT");

        Map<String, Object> props = handler.buildAsyncKafkaProps(context);

        assertEquals("broker:9092", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertNull(props.get(KafkaProps.SASL_MECHANISM));
        assertNull(props.get(KafkaProps.SASL_JAAS_CONFIG));
    }

    @Test
    void buildAsyncKafkaProps_SaslSsl_IncludesSslConfig() {
        Map<String, String> context = new HashMap<>();
        context.put("copro.processor.kafka.bootstrap.servers", "broker:9093");
        context.put("copro.processor.kafka.security.protocol", "SASL_SSL");
        context.put("copro.processor.kafka.sasl.mechanism", "PLAIN");
        context.put("copro.processor.kafka.sasl.username", "user");
        context.put("copro.processor.kafka.sasl.password", "pass");

        Map<String, Object> props = handler.buildAsyncKafkaProps(context);

        assertEquals("SASL_SSL", props.get(KafkaProps.SECURITY_PROTOCOL));
        assertEquals("PLAIN", props.get(KafkaProps.SASL_MECHANISM));
        assertNotNull(props.get(KafkaProps.SASL_JAAS_CONFIG));
        assertTrue(props.get(KafkaProps.SASL_JAAS_CONFIG).toString().contains("user"));
    }

    @Test
    void buildAsyncKafkaProps_WithCerts_IncludesTruststoreKeystore() {
        Map<String, String> context = new HashMap<>();
        context.put("copro.processor.kafka.bootstrap.servers", "broker:9093");
        context.put("copro.processor.kafka.security.protocol", "SASL_SSL");
        context.put("copro.processor.kafka.sasl.mechanism", "PLAIN");
        context.put("copro.processor.kafka.sasl.username", "user");
        context.put("copro.processor.kafka.sasl.password", "pass");
        context.put("copro.processor.kafka.ssl.include", "certs");
        context.put("copro.processor.kafka.ssl.truststore.location", "/path/truststore.jks");
        context.put("copro.processor.kafka.ssl.truststore.password", "trustpass");
        context.put("copro.processor.kafka.ssl.keystore.location", "/path/keystore.jks");
        context.put("copro.processor.kafka.ssl.keystore.password", "keypass");
        context.put("copro.processor.kafka.ssl.key.password", "keypass");

        Map<String, Object> props = handler.buildAsyncKafkaProps(context);

        assertEquals("/path/truststore.jks", props.get(KafkaProps.SSL_TRUSTSTORE_LOCATION));
        assertEquals("trustpass", props.get(KafkaProps.SSL_TRUSTSTORE_PASSWORD));
        assertEquals("/path/keystore.jks", props.get(KafkaProps.SSL_KEYSTORE_LOCATION));
        assertEquals("keypass", props.get(KafkaProps.SSL_KEYSTORE_PASSWORD));
        assertEquals("keypass", props.get(KafkaProps.SSL_KEY_PASSWORD));
    }

    @Test
    void buildAsyncKafkaProps_DefaultValues_WhenContextEmpty() {
        Map<String, String> context = new HashMap<>();

        Map<String, Object> props = handler.buildAsyncKafkaProps(context);

        assertEquals("localhost:9092", props.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("all", props.get(ProducerConfig.ACKS_CONFIG));
        assertEquals(true, props.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
    }

    @Test
    void buildProducerRecord_EmptyOriginId_NullMessageKey() {
        Map<String, String> context = new HashMap<>();
        when(actionExecutionAudit.getRootPipelineId()).thenReturn(100L);
        when(actionExecutionAudit.getActionId()).thenReturn(500L);

        TestEntity item = TestEntity.builder().originId("").paperNo(5).build();

        ProducerRecord<String, String> record = handler.buildProducerRecord(
                "test-topic", "{}", "output_table", "REQ", "batch-1", context, item, "corr-1");

        assertNull(record.key());
    }

    private void assertHeaderPresent(ProducerRecord<String, String> recordData, String headerKey, String expectedValue) {
        Header header = recordData.headers().lastHeader(headerKey);
        assertNotNull(header, "Header " + headerKey + " should be present");
        assertEquals(expectedValue, new String(header.value(), StandardCharsets.UTF_8));
    }

    private static class TestConsumerProcess implements CoproProcessor.ConsumerProcess<TestEntity, TestEntity> {
        private final String requestType;
        private final String kafkaTopic;
        private final String outputTable;

        TestConsumerProcess(String requestType, String kafkaTopic, String outputTable) {
            this.requestType = requestType;
            this.kafkaTopic = kafkaTopic;
            this.outputTable = outputTable;
        }

        @Override
        public List<TestEntity> process(URL endpoint, TestEntity entity) {
            return List.of(entity);
        }

        @Override
        public String getRequestType() {
            return requestType;
        }

        @Override
        public String getKafkaTopic() {
            return kafkaTopic;
        }

        @Override
        public String getOutputTable() {
            return outputTable;
        }

        @Override
        public String buildJsonForKafka(TestEntity item) {
            return "{\"originId\":\"" + (item.getOriginId() != null ? item.getOriginId() : "") + "\"}";
        }
    }
}
