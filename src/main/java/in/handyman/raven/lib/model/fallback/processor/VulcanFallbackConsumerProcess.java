package in.handyman.raven.lib.model.fallback.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VulcanFallbackConsumerProcess
        implements CoproProcessor.ConsumerProcess<VulcanFallbackInputTable, VulcanFallbackOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper objectMapper;
    private final OkHttpClient client;

    public VulcanFallbackConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<VulcanFallbackOutputTable> process(URL endpoint, VulcanFallbackInputTable entity) throws Exception {
        log.info(aMarker, "Processing proactive fallback for originId: {}, sorItemName: {}", entity.getOriginId(),
                entity.getSorItemName());

        Map<String, Object> payload = new HashMap<>();
        payload.put("tenantId", entity.getTenantId());
        payload.put("originId", entity.getOriginId());
        payload.put("paperNo", 1);
        payload.put("sorItemName", entity.getSorItemName());
        payload.put("sorQuestion", entity.getSorQuestion());
        payload.put("documentType", entity.getDocumentType());
        payload.put("sorContainer", entity.getSorContainer());
        payload.put("persist", true);
        payload.put("allowedAdapter", entity.getAllowedAdapter());
        payload.put("restrictedAdapter", entity.getRestrictedAdapter());
        payload.put("description", entity.getDescription());

        String jsonPayload = objectMapper.writeValueAsString(payload);
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();

        String status = "FAILED";
        String message = "";

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                status = "COMPLETED";
                log.info(aMarker, "Fallback success for item: {}", entity.getSorItemName());
            } else {
                message = response.body() != null ? response.body().string() : "No response body";
                log.warn(aMarker, "Fallback failed for item: {}. Status: {}, Response: {}", entity.getSorItemName(),
                        response.code(), message);
            }
        } catch (Exception e) {
            message = e.getMessage();
            log.error(aMarker, "Error calling fallback for item: {}", entity.getSorItemName(), e);
        }

        return Collections.singletonList(VulcanFallbackOutputTable.builder()
                .originId(entity.getOriginId())
                .sorItemName(entity.getSorItemName())
                .status(status)
                .message(message)
                .build());
    }
}
