package in.handyman.raven.lib.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.EncryptDecrypt;
import in.handyman.raven.util.PropertyHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jdbi.v3.core.Jdbi;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class KafkaPublisherService {



    private final Logger log;

    private final String outputTable;

    private final ActionExecutionAudit action;

    public KafkaPublisherService(Logger log, String outputTable, ActionExecutionAudit action) {
        this.log = log;
        this.outputTable = outputTable;
        this.action = action;
    }


    public KafkaPublishQueryOutput doKafkaPublish(KafkaPublishQueryInput kafkaPublishQueryInput) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String topicName = kafkaPublishQueryInput.getTopicName();
        String responseNode = kafkaPublishQueryInput.getJsonData();
        JsonNode productJson = objectMapper.readTree(responseNode);

        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPublishQueryInput.getEndpoint());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        if (Objects.equals(kafkaPublishQueryInput.getAuthSecurityProtocol(), "NONE")) {
            return publishMessage(objectMapper, kafkaProperties, topicName, kafkaPublishQueryInput, productJson);
        } else {
            configureAuthentication(kafkaProperties, kafkaPublishQueryInput);
            return publishMessage(objectMapper, kafkaProperties, topicName, kafkaPublishQueryInput, productJson);
        }
    }

    private KafkaPublishQueryOutput publishMessage(ObjectMapper objectMapper, Map<String, Object> kafkaProperties, String topicName,
                                KafkaPublishQueryInput kafkaPublishQueryInput, JsonNode productJson) {
        KafkaPublishQueryOutput kafkaPublishQueryOutput=KafkaPublishQueryOutput.builder()
                .documentId(kafkaPublishQueryInput.getDocumentId())
                .checksum(kafkaPublishQueryInput.getFileChecksum())
                .tenantId(String.valueOf(kafkaPublishQueryInput.getTenantId()))
                .originId(kafkaPublishQueryInput.getOriginId())
                .batchId(kafkaPublishQueryInput.getBatchId())
                .topicName(topicName)
                .endpoint(kafkaPublishQueryInput.getEndpoint())
                .authSecurityProtocol(kafkaPublishQueryInput.getAuthSecurityProtocol())
                .saslMechanism(kafkaPublishQueryInput.getSaslMechanism())
                .transactionId(kafkaPublishQueryInput.getTransactionId())
                .build();
        log.info("processing the kafka input data kafkaPublishQueryInput: {}", kafkaPublishQueryInput);
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProperties)) {

            String messageNode = objectMapper.writeValueAsString(productJson);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, messageNode);

            producer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    log.info("Successful in sending the message to kafka topic");
                    int partition = metadata.partition();
                    log.info("Topic: {}, Partition: {}", metadata.topic(), partition);

                    kafkaPublishQueryOutput.setResponse("Successful in sending message to topic");
                    kafkaPublishQueryOutput.setExecStatus(KafkaProps.SUCCESS1);
                    kafkaPublishQueryOutput.setPartition(partition);


                }
            });
        } catch (Exception e) {
            log.error("Error in posting Kafka topic message", e);
            kafkaPublishQueryOutput.setResponse("Error in posting Kafka topic message");
            kafkaPublishQueryOutput.setExecStatus(KafkaProps.FAILED_STATUS);
            kafkaPublishQueryOutput.setPartition(-1);
            throw new HandymanException("Error in converting json data to kafka topic message", e, action);
        }
        return kafkaPublishQueryOutput;
    }

    private String doOptionalMessageEncryption(String messageNode, String encryptionType, String encryptionKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        String message = EncryptDecrypt.encrypt(messageNode, encryptionKey, encryptionType);
        log.info("Encrypted message using algorithm {}", encryptionType);
        return message;

    }

    private void configureAuthentication(Map<String, Object> properties, KafkaPublishQueryInput input) {
        if (PropertyHandler.get(KafkaProps.KAFKA_AUTHENTICATION_SASL_SSL_INCLUDE).equals("certs")) {
            setAuthenticationPropertiesWithCerts(properties, input);
        } else {
            setAuthenticationProperties(properties, input);
        }
    }


    private static void setAuthenticationPropertiesWithCerts(Map<String, Object> properties, KafkaPublishQueryInput input) {
        if (input.getAuthSecurityProtocol().equalsIgnoreCase(KafkaProps.SASL_SSL)) {
            properties.put(KafkaProps.SECURITY_PROTOCOL, KafkaProps.SASL_SSL);
            if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.PLAIN_SASL)) {
                properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.PLAIN_SASL);

                String jaasConfig = getJaasConfig(input);
                properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);
                properties.put(KafkaProps.SSL_TRUSTSTORE_TYPE, PropertyHandler.get("kafka.ssl.truststore.type"));
                properties.put(KafkaProps.SSL_KEYSTORE_TYPE, PropertyHandler.get("kafka.ssl.keystore.type"));
                properties.put(KafkaProps.SSL_TRUSTSTORE_LOCATION, PropertyHandler.get("kafka.ssl.truststore.location"));
                properties.put(KafkaProps.SSL_TRUSTSTORE_PASSWORD, PropertyHandler.get("kafka.ssl.truststore.password"));
                properties.put(KafkaProps.SSL_KEYSTORE_LOCATION, PropertyHandler.get("kafka.ssl.keystore.location"));
                properties.put(KafkaProps.SSL_KEYSTORE_PASSWORD, PropertyHandler.get("kafka.ssl.keystore.password"));
                properties.put(KafkaProps.SSL_KEY_PASSWORD, PropertyHandler.get("kafka.ssl.key.password"));
                properties.put(KafkaProps.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,PropertyHandler.get("kafka.ssl.endpoint.identification.algorithm"));
                properties.put(ProducerConfig.ACKS_CONFIG, PropertyHandler.get("kafka.ssl.acks"));
                properties.put(ProducerConfig.RETRIES_CONFIG, PropertyHandler.get("kafka.ssl.api.retries"));
                properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, PropertyHandler.get("kafka.ssl.request.timeout.ms"));
                properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, PropertyHandler.get("kafka.ssl.delivery.timeout.ms"));  // 5 minutes
                properties.put(ProducerConfig.LINGER_MS_CONFIG, PropertyHandler.get("kafka.ssl.linger.ms"));
            } else {
                if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.SCRAM_SHA_256)) {
                    properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.SCRAM_SHA_256);
                    String jaasConfig = getJaasConfig(input);
                    properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);
                } else {
                    if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.OAUTH_BEARER)) {
                        properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.OAUTH_BEARER);
                        properties.put(KafkaProps.SASL_LOGIN_CALLBACK_HANDLER_CLASS, "your.oauth.LoginCallbackHandler");
                    }
                }
            }
        } else if (input.getAuthSecurityProtocol().equalsIgnoreCase(KafkaProps.SASL_PLAINTEXT)) {
            properties.put(KafkaProps.SECURITY_PROTOCOL, KafkaProps.SASL_PLAINTEXT);
            properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.PLAIN_SASL);
            String jaasConfig = getJaasConfig(input);
            properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);

        }
    }

    @NotNull
    private static String getJaasConfig(KafkaPublishQueryInput input) {
        return String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", input.getUserName(), input.getPassword());
    }


    private static void setAuthenticationProperties(Map<String, Object> properties, KafkaPublishQueryInput input) {
        if (input.getAuthSecurityProtocol().equalsIgnoreCase(KafkaProps.SASL_SSL)) {
            properties.put(input.getAuthSecurityProtocol(), KafkaProps.SASL_SSL);
            if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.PLAIN_SASL)) {
                properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.PLAIN_SASL);
                String jaasConfig = getJaasConfig(input);
                properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);
            } else {
                if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.SCRAM_SHA_256)) {
                    properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.SCRAM_SHA_256);
                    String jaasConfig = getJaasConfig(input);
                    properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);
                } else {
                    if (input.getSaslMechanism().equalsIgnoreCase(KafkaProps.OAUTH_BEARER)) {
                        properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.OAUTH_BEARER);
                        properties.put(KafkaProps.SASL_LOGIN_CALLBACK_HANDLER_CLASS, "your.oauth.LoginCallbackHandler");
                    }
                }
            }
        } else if (input.getAuthSecurityProtocol().equalsIgnoreCase(KafkaProps.SASL_PLAINTEXT)) {
            properties.put(input.getAuthSecurityProtocol(), KafkaProps.SASL_PLAINTEXT);
            properties.put(KafkaProps.SASL_MECHANISM, KafkaProps.PLAIN_SASL);
            String jaasConfig = getJaasConfig(input);
            properties.put(KafkaProps.SASL_JAAS_CONFIG, jaasConfig);

        }
    }
}

