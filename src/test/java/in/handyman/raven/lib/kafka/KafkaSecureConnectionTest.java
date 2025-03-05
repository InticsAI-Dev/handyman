package in.handyman.raven.lib.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaSecureConnectionTest {
    private static final Logger logger = LoggerFactory.getLogger(KafkaSecureConnectionTest.class);
    private static final String TOPIC = "elv_json";
    private static final String BOOTSTRAP_SERVERS = "kafka1:9092";
    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private final AtomicBoolean keepConsuming = new AtomicBoolean(true);

    @BeforeAll
    void setUp() {
        producer = createProducer();
        consumer = createConsumer();
        consumer.subscribe(Collections.singletonList(TOPIC));
    }

    @AfterAll
    void tearDown() {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }

    private Properties getBaseConfig() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");

        // SSL Configuration
        props.put("ssl.truststore.location", "/etc/kafka/secrets/kafka.truststore.jks");
        props.put("ssl.truststore.password", "truststore-password");
        props.put("ssl.keystore.location", "/etc/kafka/secrets/kafka.keystore.jks");
        props.put("ssl.keystore.password", "keystore-password");
        props.put("ssl.key.password", "keystore-password");
        props.put("ssl.endpoint.identification.algorithm", "");

        // SASL Configuration
        String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                "username=\"elevance\" " +
                "password=\"elevance@123\";";
        props.put("sasl.jaas.config", jaasConfig);

        return props;
    }

    private KafkaProducer<String, String> createProducer() {
        Properties props = getBaseConfig();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);

        return new KafkaProducer<>(props);
    }

    private KafkaConsumer<String, String> createConsumer() {
        Properties props = getBaseConfig();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        return new KafkaConsumer<>(props);
    }

    @Test
    @DisplayName("Test Produce Single Message")
    void testProduceSingleMessage() {
        String message = "Test message " + System.currentTimeMillis();
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);

        try {
            Future<?> future = producer.send(record);
            future.get(); // Wait for acknowledgment
            logger.info("Successfully produced message: {}", message);
            Assertions.assertTrue(true, "Message produced successfully");
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error producing message", e);
            Assertions.fail("Failed to produce message: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Produce and Consume Message")
    void testProduceAndConsumeMessage() {
        String message = "Test message " + System.currentTimeMillis();
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);

        // Produce message
        try {
            producer.send(record).get();
            logger.info("Produced message: {}", message);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error producing message", e);
            Assertions.fail("Failed to produce message: " + e.getMessage());
            return;
        }

        // Consume message
        boolean messageFound = false;
        int retries = 0;
        while (!messageFound && retries < 10) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> consumedRecord : records) {
                logger.info("Consumed message: {}", consumedRecord.value());
                if (consumedRecord.value().equals(message)) {
                    messageFound = true;
                    break;
                }
            }
            retries++;
        }

        Assertions.assertTrue(messageFound, "Produced message was not consumed");
    }

    @Test
    @DisplayName("Test Produce Multiple Messages")
    void testProduceMultipleMessages() {
        int messageCount = 5;
        for (int i = 0; i < messageCount; i++) {
            String message = "Test message " + i + " - " + System.currentTimeMillis();
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);

            try {
                producer.send(record).get();
                logger.info("Produced message {}: {}", i, message);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error producing message " + i, e);
                Assertions.fail("Failed to produce message " + i + ": " + e.getMessage());
                return;
            }
        }

        // Verify messages can be consumed
        int consumedCount = 0;
        int attempts = 0;
        while (consumedCount < messageCount && attempts < 10) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            consumedCount += records.count();
            attempts++;
        }

        Assertions.assertTrue(consumedCount >= messageCount,
                "Not all produced messages were consumed. Expected: " + messageCount + ", Got: " + consumedCount);
    }

    @Test
    @DisplayName("Test Consumer Group Assignment")
    void testConsumerGroupAssignment() {
        consumer.subscribe(Collections.singletonList(TOPIC));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

        Assertions.assertNotNull(consumer.assignment(), "Consumer should be assigned partitions");
        Assertions.assertFalse(consumer.assignment().isEmpty(), "Consumer should be assigned at least one partition");

        logger.info("Consumer assigned partitions: {}", consumer.assignment());
    }
}
