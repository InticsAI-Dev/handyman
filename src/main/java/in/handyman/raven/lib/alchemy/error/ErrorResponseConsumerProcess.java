package in.handyman.raven.lib.alchemy.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.alchemy.common.AlchemyApiPayload;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ErrorResponseConsumerProcess implements CoproProcessor.ConsumerProcess<ErrorResponseInputTable, ErrorResponseOutputTable> {

    private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    public static final String ALCHEMY_AUTH_TENANT_ID = "alchemyAuth.tenantId";
    public static final String ALCHEMY_AUTH_TOKEN = "alchemyAuth.token";
    public static final String PROCESS_NAME = PipelineName.ERROR_RESPONSE.getProcessName();

    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final Long tenantId;
    private final String authToken;

    private final ObjectMapper mapper = JsonMapper.builder()
            .configure(SerializationFeature.INDENT_OUTPUT, true)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()
            .registerModule(new JavaTimeModule());

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public ErrorResponseConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.tenantId = Long.valueOf(action.getContext().get(ALCHEMY_AUTH_TENANT_ID));
        this.authToken = action.getContext().get(ALCHEMY_AUTH_TOKEN);
    }

    @Override
    public List<ErrorResponseOutputTable> process(URL endpoint, ErrorResponseInputTable entity) {
        List<ErrorResponseOutputTable> results = new ArrayList<>();

        try {
            RequestBody requestBody = buildRequestBody(entity);
            Request request = buildHttpRequest(endpoint, entity, requestBody);
            try (Response response = httpClient.newCall(request).execute()) {
                results.add(processResponse(response, entity, request.url().toString(),requestBody,request));
            }
        } catch (Exception e) {
            handleException(entity.getOriginId(), e);
        }

        return results;
    }

    private RequestBody buildRequestBody(ErrorResponseInputTable entity) throws Exception {
        JsonNode errorJsonNode = mapper.readTree(entity.getErrorJson());
        ProductResponseErrorDetailsRequest requestPayload = ProductResponseErrorDetailsRequest.builder()
                .outboundStatus(entity.getOutboundStatus())
                .errorMessage(entity.getErrorMessage())
                .documentId(entity.getDocumentId())
                .errorCode(entity.getErrorCode())
                .errorJson(errorJsonNode)
                .originId(entity.getOriginId())
                .tenantId(entity.getTenantId())
                .transactionId(entity.getTransactionId())
                .batchId(entity.getBatchId())
                .build();

        String jsonPayload = mapper.writeValueAsString(requestPayload);
        return RequestBody.create(jsonPayload, MediaType.parse(APPLICATION_JSON_CHARSET_UTF_8));
    }

    @NotNull
    private Request buildHttpRequest(URL baseUrl, ErrorResponseInputTable entity, RequestBody requestBody) throws MalformedURLException {
        URL url = new URL(baseUrl + "product-outbound/error-details/create/?tenantId=" + tenantId);
        log.info(marker, "Alchemy Error response API called with URL: {}", url);

        return new Request.Builder()
                .url(url)
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
    }

    private ErrorResponseOutputTable processResponse(Response response, ErrorResponseInputTable entity, String triggeredUrl,RequestBody requestBody,Request request) throws Exception {
        String originId = entity.getOriginId();
        Long rootPipelineId = entity.getRootPipelineId();

        if (response.isSuccessful()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            AlchemyApiPayload apiPayload = mapper.readValue(responseBody, AlchemyApiPayload.class);
            JsonNode payload = apiPayload.getPayload();

            if (!payload.isEmpty() && !payload.isNull() && apiPayload.isSuccess()) {
                ErrorResponseOutputTable output = buildSuccessOutput(entity, originId, rootPipelineId, triggeredUrl,payload,requestBody,request);
                output.setProductResponse(String.valueOf(payload));
                return output;
            }
        }

        return buildFailureOutput(entity, originId, rootPipelineId, triggeredUrl,"Error in getting response from Alchemy server.",requestBody,request);
    }

    private ErrorResponseOutputTable buildSuccessOutput(ErrorResponseInputTable entity, String originId, Long rootPipelineId, String url,JsonNode payload, RequestBody requestBody, Request request) {
        return ErrorResponseOutputTable.builder()
                .processId(Long.valueOf(entity.getProcessId()))
                .tenantId(tenantId)
                .groupId(entity.getGroupId().intValue())
                .originId(originId)
                .rootPipelineId(rootPipelineId)
                .stage(PROCESS_NAME)
                .triggeredUrl(url)
                .feature(entity.getFeature())
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .message("Alchemy error response completed for origin_id - " + originId)
                .batchId(entity.getBatchId())
                .response(payload.toString())
                .request(requestBody.toString())
                .triggeredUrl(url)
                .endpoint(request.url().toString())
                .build();
    }

    private ErrorResponseOutputTable buildFailureOutput(ErrorResponseInputTable entity, String originId, Long rootPipelineId, String url, String payload, RequestBody requestBody, Request request) {
        return ErrorResponseOutputTable.builder()
                .processId(Long.valueOf(entity.getProcessId()))
                .tenantId(tenantId)
                .groupId(entity.getGroupId().intValue())
                .originId(originId)
                .rootPipelineId(rootPipelineId)
                .stage(PROCESS_NAME)
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .feature(entity.getFeature())
                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                .message("Alchemy Error response failed for origin_id - " + originId)
                .batchId(entity.getBatchId())
                .response(payload)
                .request(requestBody.toString())
                .triggeredUrl(url)
                .endpoint(request.url().toString())
                .build();
    }

    private void handleException(String originId, Exception e) {
        log.error(marker, "The Exception occurred in product response action", e);
        HandymanException handymanException = new HandymanException(e);
        HandymanException.insertException("Exception occurred in Error Response action for originId - " + originId, handymanException, this.action);
    }
}
