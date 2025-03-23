package in.handyman.raven.lib.custom.outbound.delivery.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.custom.outbound.delivery.entity.TableInputQuerySet;
import in.handyman.raven.lib.custom.outbound.delivery.interfaces.OutboundInterface;
import in.handyman.raven.util.InstanceUtil;
import okhttp3.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public class OutboundAdapterProduct implements OutboundInterface {

    private final Logger log;
    private final ObjectMapper objectMapper;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final ActionExecutionAudit actionExecutionAudit;

    public OutboundAdapterProduct(final Logger log, ObjectMapper objectMapper, ActionExecutionAudit actionExecutionAudit) {
        this.log = log;
        this.objectMapper = objectMapper;
        this.actionExecutionAudit = actionExecutionAudit;
    }

    @Override
    public String requestApiCaller(final TableInputQuerySet tableInputQuerySet) {


        String responseBody;
        log.info("Outbound Delivery Notification Action has been started for document id - {}", tableInputQuerySet.getDocumentId());
        final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();
        ObjectNode payloadJson = objectMapper.createObjectNode();
        final ObjectNode objectNode = outboundFileOptions(tableInputQuerySet);
        payloadJson.put("payload", objectNode);
        if (objectNode.isObject()) {
            payloadJson.put("success", true);
        } else {
            payloadJson.put("success", false);
        }

        payloadJson.put("responseTimeStamp", String.valueOf(LocalDateTime.now(Clock.systemDefaultZone())));


        RequestBody requestBody = RequestBody.create(payloadJson.toString(), MediaTypeJSON);
        Request request = new Request.Builder()
                .url(tableInputQuerySet.getEndpoint())
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

        ObjectNode outputJson = objectMapper.createObjectNode();

        try {
            outputJson = readJsonFileToString(tableInputQuerySet.getOutboundJson());
        } catch (IOException e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("exception in handling the input json file", handymanException, actionExecutionAudit);

        }
        return outputJson;

    }

    public static ObjectNode readJsonFileToString(String Outboundjson) throws IOException {
        // Read all bytes from the file and convert to string
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(Outboundjson);

        if (jsonNode.isObject()) {
            return (ObjectNode) jsonNode;
        } else {
            return objectMapper.createObjectNode();
        }
        // Read JSON content from file and parse into JsonNode
    }


}
