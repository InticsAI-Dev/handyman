package in.handyman.raven.lib.agadia.outbound.delivery.adapters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.agadia.outbound.delivery.entity.TableInputQuerySet;
import in.handyman.raven.lib.agadia.outbound.delivery.interfaces.OutboundInterface;
import in.handyman.raven.util.InstanceUtil;
import okhttp3.*;
import org.apache.pdfbox.util.Hex;
import org.slf4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class OutboundAdapter implements OutboundInterface {

    private final Logger log;
    private final ObjectMapper objectMapper;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final ActionExecutionAudit actionExecutionAudit;

    public OutboundAdapter(final Logger log, ObjectMapper objectMapper, ActionExecutionAudit actionExecutionAudit) {
        this.log = log;
        this.objectMapper = objectMapper;
        this.actionExecutionAudit = actionExecutionAudit;
    }

    @Override
    public String requestApiCaller(final TableInputQuerySet tableInputQuerySet) {


        String responseBody;
        log.info("Outbound Delivery Notification Action has been started for document id - {}", tableInputQuerySet.getDocumentId());
        final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();
        String agadiaSecretKey = tableInputQuerySet.getAppSecretKey();
        ObjectNode objectNode = this.outboundFileOptions(tableInputQuerySet);


        String signature;
        try {
            signature = getSignature(objectMapper.writeValueAsString(objectNode), agadiaSecretKey);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(objectNode.toString(), MediaTypeJSON);
        Request request = new Request.Builder()
                .url(tableInputQuerySet.getEndpoint())
                .addHeader("x-hmac-signature", signature)
                .post(requestBody)
                .build();

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
        } catch (Exception exception) {
            log.error("Error occurred for document id {} for Outbound Delivery Notification Action", tableInputQuerySet.getDocumentId(), exception);
            throw new HandymanException("Error occurred for document for Outbound Delivery Notification Action", exception, actionExecutionAudit);
        }
        log.info("Outbound Delivery Notification Action has been completed {}", tableInputQuerySet);
        return responseBody;
    }

    @Override
    public ObjectNode outboundFileOptions(TableInputQuerySet tableInputQuerySet) {
        final ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("documentId", tableInputQuerySet.getDocumentId());
        objectNode.put("inticsZipUri", tableInputQuerySet.getFileUri());
        objectNode.put("checksum", tableInputQuerySet.getZipFileCheckSum());
        return objectNode;
    }

    private String getSignature(String objectNode, String secretKey) {
        log.info("Initialization for Signature Generation");
        byte[] payloadFormatted = objectNode.getBytes(StandardCharsets.UTF_8);
        byte[] res = secretKey.getBytes(StandardCharsets.UTF_8);
        String signature = null;
        try {
            String algorithm = "HmacSHA256";
            Mac sha256Hmac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(res, algorithm);
            sha256Hmac.init(secretKeySpec);
            byte[] hmacBytes = sha256Hmac.doFinal(payloadFormatted);
            signature = Hex.getString(hmacBytes).toLowerCase();
            log.info("Generated Signature for object:" + objectNode + " -- " + signature);
        } catch (Throwable e) {
            log.error("Error Signature creation: " + e.getMessage());
        }
        return signature;
    }

}
