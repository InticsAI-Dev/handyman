package in.handyman.raven.lib.agadia.outbound.delivery.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.agadia.outbound.delivery.entity.TableInputQuerySet;
import in.handyman.raven.lib.agadia.outbound.delivery.interfaces.OutboundInterface;
import in.handyman.raven.util.EncryptDecrypt;
import in.handyman.raven.util.InstanceUtil;
import okhttp3.*;
import org.slf4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class OutboundAdapterElv implements OutboundInterface {

    private final Logger log;
    private final ObjectMapper objectMapper;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final ActionExecutionAudit action;

    private final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();

    private static final String AES_ENCRYPTION = "AES";

    public OutboundAdapterElv(final Logger log, ObjectMapper objectMapper, ActionExecutionAudit actionExecutionAudit) {
        this.log = log;
        this.objectMapper = objectMapper;
        this.action = actionExecutionAudit;
    }


    @Override
    public String  requestApiCaller(TableInputQuerySet tableInputQuerySet) {
        String documentId = tableInputQuerySet.getDocumentId();
        String outboundJson = tableInputQuerySet.getOutboundJson();
        String endpoint = tableInputQuerySet.getEndpoint();
        String checksum = tableInputQuerySet.getFileChecksum();
        String transactionId = tableInputQuerySet.getTransactionId();

        String encryptionType = tableInputQuerySet.getEncryptionType();
        String encryptionKey = tableInputQuerySet.getEncryptionKey().trim();

        ObjectNode payloadJson = objectMapper.createObjectNode();
        payloadJson.put("documentId", documentId);
        payloadJson.put("checksum", checksum);
        payloadJson.put("transactionId", transactionId);


        String outboundJsonNodeStr;


        try {
            outboundJsonNodeStr = objectMapper.writeValueAsString(outboundJson);
        } catch (JsonProcessingException e) {
            log.error("Error in converting jsonString to Node for file {} with exception {}", documentId, e.getMessage());
            throw new HandymanException("Error in converting jsonString to Node for file " + documentId + " with exception", e, action);
        }

        try {
            if (encryptionType.equalsIgnoreCase(AES_ENCRYPTION)) {
                String outboundExtractionJsonNode = doOptionalMessageEncryption(outboundJsonNodeStr, encryptionType, encryptionKey);
                payloadJson.put("extractionResponse", outboundExtractionJsonNode);
            }
        } catch (HandymanException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 BadPaddingException | InvalidKeyException e) {
            throw new HandymanException("Error in posting kafka topic message", e, action);
        }



        RequestBody requestBody = RequestBody.create(payloadJson.toString(), MediaTypeJSON);
        Request request = new Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build();
        String responseBody;

        log.info("Request for document {} is {}", tableInputQuerySet.getDocumentId(), request);
        try (Response response = httpclient.newCall(request).execute()) {
            responseBody = Objects.requireNonNull(response.body()).string();
            log.info("Response body for document {} is {}", tableInputQuerySet.getDocumentId(), responseBody);
            if (response.isSuccessful()) {
                log.info("Sent response for the document {} for Outbound Delivery Notification Action", tableInputQuerySet.getDocumentId());
            } else {
                log.error("Error in response for Outbound Delivery Notification Action {}", responseBody);
                throw new HandymanException(responseBody);
            }
        } catch (Exception e) {
            log.error("Error occurred for document id {} for Outbound Delivery Notification Action", tableInputQuerySet.getDocumentId(), e);
            throw new HandymanException("Error occurred for document for Outbound Delivery Notification Action", e, action);
        }
        log.info("Outbound Delivery Notification Action has been completed {}", tableInputQuerySet);
        return responseBody;

    }

    private String doOptionalMessageEncryption(String messageNode, String encryptionType, String encryptionKey) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        String message = EncryptDecrypt.encrypt(messageNode, encryptionKey, encryptionType);
        log.info("Encrypted message using algorithm {}", encryptionType);
        return message;
    }


    @Override
    public ObjectNode outboundFileOptions(TableInputQuerySet tableInputQuerySet) {
        return null;
    }
}
