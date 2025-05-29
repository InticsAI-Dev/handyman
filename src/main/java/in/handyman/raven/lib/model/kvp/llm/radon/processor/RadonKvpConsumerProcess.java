package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import bsh.EvalError;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.replicate.ReplicateRequest;
import in.handyman.raven.lib.utils.FileProcessingUtils;
import in.handyman.raven.lib.utils.ProcessFileFormatE;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.PropertyHandler;
import okhttp3.*;

import java.net.http.HttpClient;
import java.time.Instant;
import java.time.Duration;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.TimeoutException;

import static in.handyman.raven.lib.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.lib.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;
import static in.handyman.raven.lib.utils.DatabaseUtility.fetchBshResultByClassName;

public class RadonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<RadonQueryInputTable, RadonQueryOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.RADON_KVP_ACTION.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final FileProcessingUtils fileProcessingUtils;
    private final String processBase64;
    public static final String PIPELINE_REQ_RES_ENCRYPTION = "pipeline.req.res.encryption";
    public static final String REQUEST_ACTIVATOR_HANDLER_NAME = "copro.request.kvp.activator.handler.name";
    public final Jdbi jdbi;
    HttpClient client = HttpClient.newHttpClient();


    private final ProviderDataTransformer providerDataTransformer;

    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction, final String processBase64, final FileProcessingUtils fileProcessingUtils, Jdbi jdbi, ProviderDataTransformer providerDataTransformer) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.providerDataTransformer = providerDataTransformer;
        int timeOut = aAction.getTimeOut();
        this.processBase64 = processBase64;
        this.fileProcessingUtils = fileProcessingUtils;
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
        this.jdbi = jdbi;
    }


    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        String coproHandlerName = action.getContext().get(REQUEST_ACTIVATOR_HANDLER_NAME);

        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String userPrompt = "";
        String systemPrompt = entity.getSystemPrompt();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();

        if (Objects.equals(action.getContext().get("bbox.radon_bbox_activator"), "true")
                && Objects.equals(entity.getProcess(), "RADON_KVP_ACTION")) {

            log.info("RADON_KVP_ACTION process started. BBox activator is enabled.");

            String inputResponseJsonstr = entity.getInputResponseJson();
            String inputResponseJson;
            String encryptOutputJsonContent = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);

            log.info("Checking if end-to-end encryption is enabled: {}", encryptOutputJsonContent);

            InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);

            if (Objects.equals(encryptOutputJsonContent, "true")) {
                log.info("Encrypting input response JSON...");
                inputResponseJson = encryption.decrypt(inputResponseJsonstr, "AES256", "RADON_KVP_JSON");
                log.info("Encryption completed successfully.");
            } else {
                log.info("Encryption is disabled. Using raw input response JSON.");
                inputResponseJson = inputResponseJsonstr;
            }

            String base64Activator = action.getContext().get("sor.transaction.prompt.base64.activator");
            log.info("Checking if Base64 activator is enabled: {}", base64Activator);

            if (Objects.equals(base64Activator, "true")) {
                log.info("Base64 activator is turned ON for process: {}", entity.getProcess());

                Base64toActualVaue base64Caller = new Base64toActualVaue();
                String base64Value = base64Caller.base64toActual(entity.getUserPrompt());

                log.info("Decoded Base64 value successfully.");

                byte[] decodedBytes = Base64.getDecoder().decode(base64Value);
                String decodedPrompt = new String(decodedBytes);

                log.info("Decoded prompt before replacing placeholder: {}", decodedPrompt);

                String updatedPrompt = decodedPrompt.replace(
                        action.getContext().get("prompt.bbox.json.placeholder.name"), inputResponseJson);

                userPrompt = Base64.getEncoder().encodeToString(updatedPrompt.getBytes());

                log.info("Updated prompt encoded back to Base64 successfully.");
            } else {
                log.info("Base64 activator is OFF. Using plain text prompt.");

                String actualUserPrompt = entity.getUserPrompt();
                log.info("Original user prompt before replacing placeholder: {}", actualUserPrompt);

                userPrompt = actualUserPrompt.replace(
                        action.getContext().get("prompt.bbox.json.placeholder.name"), inputResponseJson);

                log.info("Updated user prompt in plain text.");
            }
        } else {
            log.info("BBox activator is disabled or process is not RADON_KVP_ACTION. Using original user prompt.");
            userPrompt = entity.getUserPrompt();
        }


        //payload
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();

        radonKvpExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        radonKvpExtractionRequest.setActionId(actionId);
        radonKvpExtractionRequest.setProcess(entity.getProcess());
        radonKvpExtractionRequest.setInputFilePath(filePath);
        radonKvpExtractionRequest.setGroupId(groupId);
        radonKvpExtractionRequest.setUserPrompt(userPrompt);
        radonKvpExtractionRequest.setSystemPrompt(systemPrompt);
        radonKvpExtractionRequest.setBase64Img("");
        radonKvpExtractionRequest.setProcessId(processId);
        radonKvpExtractionRequest.setPaperNo(paperNo);
        radonKvpExtractionRequest.setTenantId(tenantId);
        radonKvpExtractionRequest.setOriginId(originId);
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());


        if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
            radonKvpExtractionRequest.setBase64Img(fileProcessingUtils.convertFileToBase64(filePath));
        } else {
            radonKvpExtractionRequest.setBase64Img("");
        }

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
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {}, userPrompt {} and systemPrompt {}", endpoint, filePath, userPrompt, systemPrompt);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("COPRO", coproHandlerName)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuilder(entity, request, parentObj, jsonInputRequest, endpoint);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            String jsonResponseEnc = encryptRequestResponse(jsonRequest);

            tritonRequestBuilder(entity, request, parentObj, jsonResponseEnc, endpoint);
        }

        log.info(aMarker, "Radon kvp consumer process output parent object entities size {}", parentObj.size());
        return parentObj;
    }

    private void tritonRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        Instant start = Instant.now();
        log.info("\t\tInput request time in sec for api call: "+start+ "\t\t");
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = response.body().string();
                Instant end = Instant.now();
                // Calculate the duration between the two instants
                Duration duration = Duration.between(start, end);
                // Get the difference in seconds
                long seconds = duration.getSeconds();
                log.info("\nTotal time duration in sec for api call: "+seconds+ "\n");
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, radonDataItem, parentObj, jsonRequest, responseBody, endpoint.toString());
                        } catch (IOException | EvalError e) {
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
                            log.error(aMarker, "The Exception occurred in converting the response from triton server output {}", ExceptionUtil.toString(e));
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
                        .request(encryptRequestResponse(jsonRequest))
                        .response(encryptRequestResponse(response.message()))
                        .endpoint(String.valueOf(endpoint))
                        .build());
                HandymanException handymanException = new HandymanException("Non-successful response: " + response.message());
                HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);

                log.info(aMarker, "Error in getting response from triton api {}", response.message());
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
                log.info("Input contains the required ```json``` markers. So processing it based on the ```json``` markers.");
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");
                    // Convert the cleaned JSON string to a JsonNode
                    jsonResponse = repairJson(jsonString);
                    if (!jsonResponse.isEmpty()) {
                        return objectMapper.readTree(jsonResponse);
                    } else {
                        return null;
                    }
                } else {
                    jsonResponse = repairJson(jsonResponse);
                    return objectMapper.readTree(jsonResponse);
                }
            } else if (jsonResponse.contains("{")) {
                log.info("Input does not contain the required ```json``` markers. So processing it based on the indication of object literals.");
                jsonResponse = repairJson(jsonResponse);
                return objectMapper.readTree(jsonResponse);
            } else {
                log.info("Input does not contain the required ```json``` markers or any indication of object literals. So returning null.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode getJsonNodeFromInferResponse(ObjectMapper objectMapper, String jsonString) throws JsonProcessingException {
        try {

            jsonString = jsonString.replace("\n", "");

            // Convert the cleaned JSON string to a JsonNode
            JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode;
        } catch (Exception e) {
            throw new IllegalArgumentException("Input does not have a json structure .");
        }


    }

    private void extractTritonOutputDataResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws IOException, EvalError {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();
        String extractedContent;
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        String encryptOutputJsonContent = action.getContext().get(ENCRYPT_ITEM_WISE_ENCRYPTION);
        InticsIntegrity encryption = SecurityEngine.getInticsIntegrityMethod(action);
        log.info(aMarker, "checking provider data found for the given input {}", Boolean.TRUE.equals(entity.getPostProcess()));
        if (Boolean.TRUE.equals(entity.getPostProcess())) {
            log.info(aMarker, "Provider data found for the given input started the post process with value {}", entity.getPostProcess());
            String providerClassName = action.getContext().get(entity.getPostProcessClassName());
            Optional<String> sourceCode = fetchBshResultByClassName(jdbi, providerClassName);
            if (sourceCode.isPresent()) {
                List<RadonQueryOutputTable> providerParentObj = providerDataTransformer.processProviderData(sourceCode.get(), providerClassName, modelResponse.getInferResponse(), entity, request, response, endpoint);
                parentObj.addAll(providerParentObj);
            }

        } else {
            if (Objects.equals(encryptOutputJsonContent, "true")) {
                extractedContent = encryption.encrypt(modelResponse.getInferResponse(), "AES256", "RADON_KVP_JSON");
            } else {
                extractedContent = modelResponse.getInferResponse();
            }

            parentObj.add(RadonQueryOutputTable.builder()
                    .createdOn(CreateTimeStamp.currentTimestamp())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .originId(originId)
                    .paperNo(paperNo)
                    .totalResponseJson(extractedContent)
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
                    .request(encryptRequestResponse(request))
                    .response(encryptRequestResponse(response))
                    .sorContainerId(entity.getSorContainerId())
                    .endpoint(String.valueOf(endpoint))
                    .build()
            );
        }


    }

    private void coproResponseBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
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
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
                            log.error(aMarker, "The Exception occurred in converting response from triton server output {}", ExceptionUtil.toString(e));
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
                        .request(encryptRequestResponse(jsonInputRequest))
                        .response(encryptRequestResponse(response.message()))
                        .endpoint(String.valueOf(endpoint))
                        .sorContainerId(entity.getSorContainerId())
                        .build());
                HandymanException handymanException = new HandymanException("Non-successful response: " + response.message());
                HandymanException.insertException("Agentic paper filter consumer failed for batch/group " + entity.getGroupId(), handymanException, this.action);

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
                    .sorContainerId(entity.getSorContainerId())
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
                .request(encryptRequestResponse(request))
                .response(encryptRequestResponse(response))
                .endpoint(String.valueOf(endpoint))
                .sorContainerId(entity.getSorContainerId())
                .build()
        );
    }

    private String repairJson(String jsonString) {

        // Ensure keys and string values are enclosed in double quotes
        jsonString = addMissingQuotes(jsonString);

        // Balance braces and brackets
        jsonString = balanceBracesAndBrackets(jsonString);

        // Assign empty strings to keys with no values
        jsonString = assignEmptyValues(jsonString);

        return jsonString;
    }

    private String addMissingQuotes(String jsonString) {
        // Ensure keys are enclosed in double quotes
        jsonString = jsonString.replaceAll("(\\{|,\\s*)(\\w+)(?=\\s*:)", "$1\"$2\"");

        // Ensure string values are enclosed in double quotes
        // This regex matches values that are not already enclosed in quotes
        jsonString = jsonString.replaceAll("(?<=:)\\s*([^\"\\s,\\n}\\]]+)(?=\\s*(,|}|\\n|\\]))", "\"$1\"");

        return jsonString;
    }


    private String balanceBracesAndBrackets(String jsonString) {
        // Balance braces and brackets
        int openBraces = 0;
        int closeBraces = 0;
        int openBrackets = 0;
        int closeBrackets = 0;

        for (char c : jsonString.toCharArray()) {
            if (c == '{') openBraces++;
            if (c == '}') closeBraces++;
            if (c == '[') openBrackets++;
            if (c == ']') closeBrackets++;
        }

        // Add missing closing braces
        while (openBraces > closeBraces) {
            jsonString += "}";
            closeBraces++;
        }

        // Add missing closing brackets
        while (openBrackets > closeBrackets) {
            jsonString += "]";
            closeBrackets++;
        }

        return jsonString;
    }

    private String assignEmptyValues(String jsonString) {
        // Assign empty strings to keys with no values
        jsonString = jsonString.replaceAll("(?<=:)\\s*(?=,|\\s*}|\\s*\\])", "\"\"");
        return jsonString;
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

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }


    private void replicateRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode  results = mapper.readTree(responseBody);
            JsonNode rootNode = checkStatus(results, endpoint);
            JsonNode outputNode = rootNode.path("output");

            if (response.isSuccessful()) {
                assert response.body() != null;

                if (outputNode != null && !outputNode.isEmpty()) {
                    try {
                        extractTritonOutputDataResponse(entity, String.valueOf(outputNode), parentObj, jsonRequest, responseBody, endpoint.toString());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    } catch (EvalError e) {
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
                        .sipType(entity.getSipType())
                        .truthEntityId(entity.getTruthEntityId())
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
                    .sipType(entity.getSipType())
                    .truthEntityId(entity.getTruthEntityId())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from replicate server {}", ExceptionUtil.toString(e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode checkStatus(JsonNode initialResponse, URL endpoint) throws Exception {
        final String jobId = initialResponse.path("id").asText();
        final String initialStatus = initialResponse.path("status").asText();

        if ("COMPLETED".equals(initialStatus) || "FAILED".equals(initialStatus)) {
            log.info("Job " + jobId + " initial status: " + initialStatus);
            return initialResponse;
        }

        final String url = String.format(action.getContext().get("runpod.status.endpoint")+"%s", jobId);
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).getSeconds() < (long) Integer.parseInt(action.getContext().get("kvp.runpod.check.status"))) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + PropertyHandler.get("runpod.api.token.v1"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> finalResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (finalResponse.  statusCode() / 100 != 2) {
                throw new IOException("Failed to get status: " + finalResponse.body());
            }

            JsonNode json = mapper.readTree(finalResponse.body());
            String jobStatus = json.path("status").asText();
            log.info("Job " + jobId + " polled status: " + jobStatus);

            if ("COMPLETED".equals(jobStatus) || "FAILED".equals(jobStatus)) {
                return json;
            }

            Thread.sleep(5 * 1000L);
        }

        throw new TimeoutException(String.format("Job %s did not complete within %d seconds", jobId, (long) 4000));
    }
}