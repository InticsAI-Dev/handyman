package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.replicate.ReplicateResponse;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<RadonQueryInputTable, RadonQueryOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.RADON_KVP_ACTION.getProcessName();
    public static final String RADON_START = "RADON START";
    private final Logger log;
    private final Marker aMarker;
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.activator.handler.name";
    public static final String REPLICATE_API_TOKEN_CONTEXT = "replicate.request.api.token";
    public static final String REPLICATE_RADON_VERSION = "replicate.radon.version";

    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;

    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        List<RadonQueryOutputTable> parentObj = new ArrayList<>();

        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);
        String replicateApiToken = action.getContext().get(REPLICATE_API_TOKEN_CONTEXT);
        String replicateRadonVersion = action.getContext().get(REPLICATE_RADON_VERSION);


        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String prompt = entity.getPrompt();
        String modelRegistry = entity.getModelRegistry();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();

        //payload
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();

        radonKvpExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        radonKvpExtractionRequest.setActionId(actionId);
        radonKvpExtractionRequest.setProcess(PROCESS_NAME);
        radonKvpExtractionRequest.setInputFilePath(filePath);
        radonKvpExtractionRequest.setGroupId(groupId);
        radonKvpExtractionRequest.setPrompt(prompt);
        radonKvpExtractionRequest.setProcessId(String.valueOf(processId));
        radonKvpExtractionRequest.setPaperNo(paperNo);
        radonKvpExtractionRequest.setTenantId(tenantId);
        radonKvpExtractionRequest.setOriginId(originId);
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());
        radonKvpExtractionRequest.setBase64img(entity.getBase64img());



        String jsonInputRequest = mapper.writeValueAsString(radonKvpExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(entity.getApiName());
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and prompt {}", endpoint, filePath, prompt);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("COPRO", coproHandlerName)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and json input {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj, jsonInputRequest, endpoint);
        } else if (Objects.equals("REPLICATE", coproHandlerName)) {
            ReplicateRequest replicateRequest=new ReplicateRequest();
//            replicateRequest.setVersion(replicateRadonVersion);
            replicateRequest.setInput(radonKvpExtractionRequest);
            String replicateJsonRequest = objectMapper.writeValueAsString(replicateRequest);
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(replicateJsonRequest, MEDIA_TYPE_JSON))
                    .addHeader("Authorization", "Bearer " + replicateApiToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "wait")
                    .build();

            replicateRequestBuilder(entity,request, parentObj , replicateJsonRequest, endpoint );
        }
        else if (Objects.equals("TRITON", coproHandlerName)){
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
        }



        return parentObj;
    }

    private void replicateRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (response.isSuccessful()) {
                assert response.body() != null;
                ReplicateResponse modelResponse = mapper.readValue(responseBody, ReplicateResponse.class);
                if (modelResponse.getOutput() != null && !modelResponse.getOutput().isEmpty()) {
                    try {
                        extractTritonOutputDataResponse(entity, String.valueOf(modelResponse.getOutput()), parentObj, jsonRequest, responseBody, endpoint.toString());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .actionId(action.getActionId())
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(responseBody)
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(tenantId)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(tenantId)
                        .category(entity.getCategory())
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response from replicate response {}", responseBody);
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .actionId(action.getActionId())
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from replicate server {}", ExceptionUtil.toString(e));
        }
    }

    private void tritonRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, radonDataItem, parentObj, jsonRequest, responseBody, endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .actionId(action.getActionId())
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(tenantId)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(tenantId)
                        .category(entity.getCategory())
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response from triton response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .actionId(action.getActionId())
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from triton server {}", ExceptionUtil.toString(e));
        }
    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");


                    // Convert the cleaned JSON string to a JsonNode
                    JsonNode rootNode = objectMapper.readTree(jsonString);

                    return rootNode;
                }else {
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    return rootNode;
                }

            } else {
                // Handle the case where the expected markers are not found
                throw new IllegalArgumentException("Input does not contain the required ```json``` markers.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void extractTritonOutputDataResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);
//        JsonNode stringObjectMap=convertFormattedJsonStringToJsonNode(modelResponse.getInferResponse(), objectMapper);


        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(CreateTimeStamp.currentTimestamp())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(modelResponse.getInferResponse())
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .batchId(modelResponse.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(request)
                .response(response)
                .endpoint(String.valueOf(endpoint))
                .build()
        );


    }

    private void coproResponseBuider(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractedCoproOutputResponse(entity, radonDataItem, parentObj, jsonInputRequest, responseBody, endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .actionId(action.getActionId())
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .category(entity.getCategory())
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in converting response from copro server {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .actionId(action.getActionId())
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp action consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from copro server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(modelResponse.getInferResponse())
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .modelRegistry(entity.getModelRegistry())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .message("Radon kvp action macro completed")
                .category(entity.getCategory())
                .request(request)
                .response(response)
                .endpoint(String.valueOf(endpoint))
                .build()
        );
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String json = jsonParser.getText();
        // Remove the ```json block and any leading/trailing spaces
        json = json.replace("```json", "").replace("```", "").trim();
        // Deserialize the cleaned JSON string into a Map
        return objectMapper.readValue(json, Map.class);
    }
}
