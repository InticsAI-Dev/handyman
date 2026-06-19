package in.handyman.raven.lib.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.utils.FileProcessingUtils;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.deep.sift.DeepSiftConsumerProcess;
import in.handyman.raven.lib.model.deep.sift.DeepSiftInputTable;
import com.fasterxml.jackson.core.type.TypeReference;
import in.handyman.raven.lib.model.deep.sift.DeepSiftOutputTable;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test to verify actual message posting to a real Kafka cluster for Deep Sift.
 * This test uses a real KafkaProducer without mocking the Kafka infrastructure.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeepSiftKafkaIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DeepSiftKafkaIntegrationTest.class);
    private static final String BOOTSTRAP_SERVERS = "kafka-1:29092"; // Adjust if testing against a different broker
    private static final String TOPIC = "intics_copro_deep_sift_request";

    private KafkaProducer<String, String> producer;
    private DeepSiftConsumerProcess process;

    @BeforeAll
    void setUp() {
        // Setup the consumer process to generate the payload
        Marker marker = MarkerFactory.getMarker("DeepSiftKafkaIntegrationTest");
        ActionExecutionAudit actionExecutionAudit = mock(ActionExecutionAudit.class);
        FileProcessingUtils fileProcessingUtils = mock(FileProcessingUtils.class);

        Map<String, String> context;
        try {
            String contextStr = "{}"; // Usually replaced with JSON context
            context = new ObjectMapper().readValue(contextStr, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse context JSON", e);
        }
        
        context.put("vulcan.copro.processor.kafka.ssl.include", "certs");
        context.putIfAbsent("copro.metrics.activator", "true");
        
        when(actionExecutionAudit.getContext()).thenReturn(context);
        when(actionExecutionAudit.getActionId()).thenReturn(2020L);

        producer = createProducer(actionExecutionAudit, context);

        try {
            // Mock base64 conversion so we don't need a real file
            when(fileProcessingUtils.convertFileToBase64("dummy-path.pdf")).thenReturn("dGVzdC1iYXNlNjQ=");
        } catch (Exception e) {
            log.error("Failed to mock file processing", e);
        }

        // We initialize the process. Note: This requires the DB connection (HandymanRepoImpl) 
        // to succeed if run in an environment, or it might throw an ExceptionInInitializerError 
        // if no DB is present locally.
        try {
            process = new DeepSiftConsumerProcess(log, marker, actionExecutionAudit, fileProcessingUtils, "BASE64", "output_table", "DATA_EXTRACTION", TOPIC);
        } catch (Exception | Error e) {
            log.warn("Failed to initialize DeepSiftConsumerProcess (this is expected if DB is not available in the test environment): ", e);
        }
    }

    @AfterAll
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
    }

    private KafkaProducer<String, String> createProducer(ActionExecutionAudit actionExecutionAudit, Map<String, String> context) {
        BlockingQueue<DeepSiftInputTable> queue = new ArrayBlockingQueue<>(10);
        DeepSiftInputTable stoppingSeed = DeepSiftInputTable.builder().build();
        
        CoproProcessorAsyncHandler<DeepSiftInputTable, DeepSiftOutputTable> handler =
                new CoproProcessorAsyncHandler<>(queue, stoppingSeed, actionExecutionAudit, "test_db_conn", log);

        context.put("vulcan.copro.processor.kafka.bootstrap.servers", BOOTSTRAP_SERVERS);
        context.put("vulcan.copro.processor.kafka.security.protocol", "SASL_SSL");
        context.put("vulcan.copro.processor.kafka.sasl.mechanism", "PLAIN");
        context.put("vulcan.copro.processor.kafka.ssl.truststore.location", "/Users/manikandan.tm/workspace/deployment-scripts/client.truststore.jks");
        context.put("vulcan.copro.processor.kafka.ssl.truststore.password", "keystore-pass");
        context.put("vulcan.copro.processor.kafka.ssl.keystore.location", "/Users/manikandan.tm/workspace/deployment-scripts/client.keystore.jks");
        context.put("vulcan.copro.processor.kafka.ssl.keystore.password", "keystore-pass");
        context.put("vulcan.copro.processor.kafka.ssl.key.password", "keystore-pass");
        context.put("vulcan.copro.processor.kafka.ssl.endpoint.identification.algorithm", "");
        context.put("vulcan.copro.processor.kafka.sasl.username", "admin");
        context.put("vulcan.copro.processor.kafka.sasl.password", "admin-password");
        context.put("vulcan.copro.processor.kafka.sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-password\";");
        final Map<String, Object> kafkaProducerProps = handler.buildAsyncKafkaProps(context);

        log.info("========== Kafka Producer Properties ==========");
        kafkaProducerProps.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        log.info("{} = {}", entry.getKey(), entry.getValue()));
        log.info("==============================================");

        return new KafkaProducer<>(kafkaProducerProps);
    }

    @Test
    @DisplayName("Test Produce Deep Sift Async Payload to Kafka")
    void testProduceDeepSiftMessageToKafka() throws IOException {
        Assumptions.assumeTrue(process != null, "DeepSiftConsumerProcess failed to initialize, skipping test.");


        String filePath = "/Users/manikandan.tm/Downloads/ilovepdf_pages-to-jpg/MPA-2_page-0005.jpg";

        byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
        String base64Content = Base64.getEncoder().encodeToString(fileBytes);
        // 1. Create a dummy DeepSift input table entity
        DeepSiftInputTable input = DeepSiftInputTable.builder()
                .originId("ORIGIN-1")
                .batchId("ORIGIN-1")
                .tenantId(1L)
                .groupId(10)
                .rootPipelineId(1L)
                .modelName("XENON")
                .inputFilePath("/Users/manikandan.tm/Downloads/ilovepdf_pages-to-jpg/MPA-2_page-0005.jpg")
                .base64Img(base64Content)
                .paperNo(5)
                .createdOn(new Timestamp(System.currentTimeMillis()))
                .build();

        // 2. Generate the JSON payload using the target class method
        String jsonPayload;
        try {
            jsonPayload = process.buildJsonForKafka(input);
            Assertions.assertNotNull(jsonPayload, "Generated JSON payload should not be null");
        } catch (Exception e) {
            Assertions.fail("Failed to build JSON payload: " + e.getMessage());
            return;
        }

        // 3. Post to Kafka cluster
        ProducerRecord<String, String> record = new ProducerRecord<>(process.getKafkaTopic(), input.getOriginId(), jsonPayload);

        try {
            Future<?> future = producer.send(record);
            future.get(); // Wait for acknowledgment from the broker
            log.info("Successfully produced Deep Sift message to Kafka topic {} with Origin ID: {}", process.getKafkaTopic(), input.getOriginId());
            Assertions.assertTrue(true, "Message produced successfully to Kafka");
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error producing message to Kafka cluster. Ensure Kafka is running at {}", BOOTSTRAP_SERVERS, e);
            Assertions.fail("Failed to produce message to Kafka: " + e.getMessage());
        }
    }
}
